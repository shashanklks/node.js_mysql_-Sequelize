package com.khatabook.clone.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body body: SendOtpRequest): ApiEnvelope<SendOtpData>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): ApiEnvelope<VerifyOtpData>

    @GET("api/profile")
    suspend fun profile(): ApiEnvelope<UserData>

    @PUT("api/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ApiEnvelope<UserData>

    @GET("api/parties")
    suspend fun parties(
        @Query("type") type: String?,
        @Query("search") search: String?,
        @Query("sort") sort: String?,
    ): ApiEnvelope<PartyListData>

    @POST("api/parties")
    suspend fun createParty(@Body body: CreatePartyRequest): ApiEnvelope<PartyData>

    @GET("api/parties/{id}")
    suspend fun party(@Path("id") id: Int): ApiEnvelope<PartyData>

    @DELETE("api/parties/{id}")
    suspend fun deleteParty(@Path("id") id: Int): ApiEnvelope<Unit>

    @GET("api/parties/{id}/entries")
    suspend fun entries(@Path("id") partyId: Int): ApiEnvelope<EntryListData>

    @POST("api/parties/{id}/entries")
    suspend fun createEntry(
        @Path("id") partyId: Int,
        @Body body: CreateEntryRequest,
    ): ApiEnvelope<EntryData>

    @DELETE("api/entries/{id}")
    suspend fun deleteEntry(@Path("id") id: Int): ApiEnvelope<Unit>

    @GET("api/reports/summary")
    suspend fun reportSummary(): ApiEnvelope<SummaryData>

    @GET("api/reports/transactions")
    suspend fun reportTransactions(
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("partyType") partyType: String?,
    ): ApiEnvelope<ReportData>
}
