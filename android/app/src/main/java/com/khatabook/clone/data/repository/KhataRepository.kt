package com.khatabook.clone.data.repository

import com.khatabook.clone.data.local.SessionStore
import com.khatabook.clone.data.remote.ApiClient
import com.khatabook.clone.data.remote.ApiEnvelope
import com.khatabook.clone.data.remote.ApiService
import com.khatabook.clone.data.remote.CreateEntryRequest
import com.khatabook.clone.data.remote.CreatePartyRequest
import com.khatabook.clone.data.remote.EntryListData
import com.khatabook.clone.data.remote.PartyDto
import com.khatabook.clone.data.remote.PartyListData
import com.khatabook.clone.data.remote.ReportData
import com.khatabook.clone.data.remote.SendOtpData
import com.khatabook.clone.data.remote.SummaryData
import com.khatabook.clone.data.remote.UpdateProfileRequest
import com.khatabook.clone.data.remote.UserDto
import com.khatabook.clone.data.remote.VerifyOtpData
import com.khatabook.clone.data.remote.VerifyOtpRequest
import com.khatabook.clone.data.remote.SendOtpRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class KhataRepository(private val session: SessionStore) {

    private val api: ApiService get() = ApiClient.service()

    /* ------------------------------ auth ------------------------------ */

    suspend fun sendOtp(phone: String): Result<SendOtpData> =
        call { api.sendOtp(SendOtpRequest(phone)) }

    suspend fun verifyOtp(phone: String, otp: String): Result<VerifyOtpData> =
        call { api.verifyOtp(VerifyOtpRequest(phone, otp)) }.onSuccess { data ->
            ApiClient.token = data.token
            session.saveToken(data.token)
            session.saveProfile(
                phone = data.user?.phone ?: phone,
                name = data.user?.name,
                businessName = data.user?.businessName,
            )
        }

    suspend fun profile(): Result<UserDto> =
        call { api.profile() }.mapCatching { it.user ?: error("Profile missing") }

    suspend fun updateProfile(
        name: String? = null,
        businessName: String? = null,
        language: String? = null,
    ): Result<UserDto> =
        call { api.updateProfile(UpdateProfileRequest(name, businessName, language)) }
            .mapCatching { it.user ?: error("Profile missing") }
            .onSuccess { session.saveProfile(it.phone, it.name, it.businessName) }

    suspend fun logout() {
        ApiClient.token = null
        session.clear()
    }

    /* ----------------------------- parties ---------------------------- */

    suspend fun parties(type: String, search: String?, sort: String = "recent"): Result<PartyListData> =
        call { api.parties(type, search?.takeIf { it.isNotBlank() }, sort) }

    suspend fun createParty(
        name: String,
        phone: String?,
        type: String,
        address: String? = null,
    ): Result<PartyDto> =
        call {
            api.createParty(
                CreatePartyRequest(
                    name = name.trim(),
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                    address = address?.trim()?.takeIf { it.isNotEmpty() },
                    type = type,
                )
            )
        }.map { it.party }

    suspend fun deleteParty(id: Int): Result<Unit> = call { api.deleteParty(id) }.map { }

    /* ----------------------------- entries ---------------------------- */

    suspend fun entries(partyId: Int): Result<EntryListData> = call { api.entries(partyId) }

    suspend fun addEntry(
        partyId: Int,
        amount: Double,
        type: String,
        note: String?,
        entryDate: String,
    ): Result<Unit> =
        call {
            api.createEntry(
                partyId,
                CreateEntryRequest(
                    amount = amount,
                    type = type,
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    entryDate = entryDate,
                ),
            )
        }.map { }

    suspend fun deleteEntry(id: Int): Result<Unit> = call { api.deleteEntry(id) }.map { }

    /* ----------------------------- reports ---------------------------- */

    suspend fun summary(): Result<SummaryData> = call { api.reportSummary() }

    suspend fun transactions(from: String?, to: String?, partyType: String?): Result<ReportData> =
        call { api.reportTransactions(from, to, partyType) }

    /* ------------------------------------------------------------------ */

    /**
     * Runs a call off the main thread and turns both transport failures and
     * `success: false` envelopes into a [Result] carrying the server's message.
     */
    private suspend fun <T> call(block: suspend () -> ApiEnvelope<T>): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                val envelope = block()
                val body = envelope.data
                when {
                    !envelope.success -> Result.failure(ApiException(envelope.message ?: GENERIC_ERROR))
                    body == null -> @Suppress("UNCHECKED_CAST") Result.success(Unit as T)
                    else -> Result.success(body)
                }
            } catch (e: HttpException) {
                if (e.code() == 401) logout()
                Result.failure(ApiException(e.serverMessage()))
            } catch (e: IOException) {
                Result.failure(ApiException("Cannot reach the server. Check that it is running and that the API address is correct."))
            } catch (e: JsonSyntaxException) {
                Result.failure(ApiException("Unexpected response from the server"))
            }
        }

    private fun HttpException.serverMessage(): String = try {
        val raw = response()?.errorBody()?.string()
        val parsed = raw?.let { Gson().fromJson(it, ApiEnvelope::class.java) }
        parsed?.message ?: GENERIC_ERROR
    } catch (e: Exception) {
        GENERIC_ERROR
    }

    private companion object {
        const val GENERIC_ERROR = "Something went wrong"
    }
}

class ApiException(override val message: String) : Exception(message)
