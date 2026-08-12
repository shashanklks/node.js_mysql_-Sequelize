package com.khatabook.clone.data.remote

import com.google.gson.annotations.SerializedName

/** Every endpoint answers with this envelope. */
data class ApiEnvelope<T>(
    val statusCode: Int = 0,
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null,
)

data class SendOtpRequest(val phone: String)

data class SendOtpData(
    val phone: String = "",
    val expiresInSeconds: Int = 300,
    /** Present only while the server runs outside production. */
    val otp: String? = null,
)

data class VerifyOtpRequest(val phone: String, val otp: String)

data class VerifyOtpData(
    val token: String = "",
    val isNewUser: Boolean = false,
    val user: UserDto? = null,
)

data class UserDto(
    val id: Int = 0,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val businessName: String? = null,
    val language: String = "en",
    val profileCompleted: Boolean = false,
)

data class UserData(val user: UserDto? = null)

data class UpdateProfileRequest(
    val name: String? = null,
    val businessName: String? = null,
    val language: String? = null,
)

data class PartyDto(
    val id: Int = 0,
    val name: String = "",
    val phone: String? = null,
    val address: String? = null,
    val type: String = PartyType.CUSTOMER,
    val gave: Double = 0.0,
    val got: Double = 0.0,
    /** Positive: they owe you. Negative: you owe them. */
    val balance: Double = 0.0,
    val lastEntryDate: String? = null,
)

object PartyType {
    const val CUSTOMER = "CUSTOMER"
    const val SUPPLIER = "SUPPLIER"
}

object EntryType {
    const val GAVE = "GAVE"
    const val GOT = "GOT"
}

data class BookSummary(
    val youWillGet: Double = 0.0,
    val youWillGive: Double = 0.0,
    val count: Int = 0,
)

data class PartyListData(
    val parties: List<PartyDto> = emptyList(),
    val summary: BookSummary = BookSummary(),
    val count: Int = 0,
)

data class PartyData(val party: PartyDto = PartyDto())

data class CreatePartyRequest(
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val type: String = PartyType.CUSTOMER,
)

data class EntryDto(
    val id: Int = 0,
    val partyId: Int = 0,
    val amount: Double = 0.0,
    val type: String = EntryType.GAVE,
    val note: String? = null,
    val entryDate: String = "",
    val runningBalance: Double = 0.0,
)

data class LedgerSummary(
    val gave: Double = 0.0,
    val got: Double = 0.0,
    val balance: Double = 0.0,
)

data class EntryListData(
    @SerializedName("party") val party: PartyDto = PartyDto(),
    val entries: List<EntryDto> = emptyList(),
    val summary: LedgerSummary = LedgerSummary(),
)

data class EntryData(val entry: EntryDto = EntryDto())

data class CreateEntryRequest(
    val amount: Double,
    val type: String,
    val note: String? = null,
    val entryDate: String,
)

data class SummaryData(
    val customers: BookSummary = BookSummary(),
    val suppliers: BookSummary = BookSummary(),
    val overall: BookSummary = BookSummary(),
)

data class ReportEntryDto(
    val id: Int = 0,
    val partyId: Int = 0,
    val partyName: String? = null,
    val partyType: String? = null,
    val amount: Double = 0.0,
    val type: String = EntryType.GAVE,
    val note: String? = null,
    val entryDate: String = "",
)

data class ReportSummary(
    val gave: Double = 0.0,
    val got: Double = 0.0,
    val net: Double = 0.0,
    val count: Int = 0,
)

data class ReportData(
    val entries: List<ReportEntryDto> = emptyList(),
    val summary: ReportSummary = ReportSummary(),
)
