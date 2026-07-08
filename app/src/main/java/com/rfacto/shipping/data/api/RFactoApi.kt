package com.rfacto.shipping.data.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import com.rfacto.shipping.BuildConfig
import com.squareup.moshi.JsonClass

interface RFactoApi {

    @POST("api/auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleSignInRequest
    ): Response<AuthResponse>

    @POST("api/payments/create-payment-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): Response<PaymentIntentResponse>

    // --- Nouveaux endpoints pour le Profil & Photos ---

    @GET("api/profile/upload-url")
    suspend fun getProfileUploadUrl(
        @Query("fileName") fileName: String,
        @Query("fileType") fileType: String
    ): Response<UploadUrlResponse>

    @POST("api/profile/update")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<StatusResponse>

    @POST("api/colis/upload-url")
    suspend fun getColisUploadUrl(
        @Query("fileName") fileName: String
    ): Response<UploadUrlResponse>

    companion object {
        private var instance: RFactoApi? = null

        fun getInstance(): RFactoApi {
            if (instance == null) {
                val baseUrl = if (BuildConfig.REMOTE_API_URL.endsWith("/")) {
                    BuildConfig.REMOTE_API_URL
                } else {
                    "${BuildConfig.REMOTE_API_URL}/"
                }
                
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                instance = retrofit.create(RFactoApi::class.java)
            }
            return instance!!
        }
    }
}

@JsonClass(generateAdapter = true)
data class GoogleSignInRequest(
    val email: String,
    val fullName: String,
    val idToken: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val status: String,
    val token: String,
    val userId: Int,
    val role: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class CreatePaymentIntentRequest(
    val parcelId: Int,
    val amountCad: Double,
    val userId: Int,
    val currency: String = "CAD"
)

@JsonClass(generateAdapter = true)
data class PaymentIntentResponse(
    val paymentIntentClientSecret: String,
    val ephemeralKeySecret: String,
    val customerId: String,
    val publishableKey: String
)

@JsonClass(generateAdapter = true)
data class UploadUrlResponse(
    val uploadUrl: String,
    val publicUrl: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val userId: Int,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val photoUrl: String,
    val ville: String,
    val adresse: String
)

@JsonClass(generateAdapter = true)
data class StatusResponse(
    val status: String,
    val message: String
)
