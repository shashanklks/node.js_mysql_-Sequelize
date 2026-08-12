package com.khatabook.clone.data.remote

import com.khatabook.clone.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the Retrofit service. Both the auth token and the API host can change
 * at runtime (login, logout, the API server field in settings), so they are read
 * through providers instead of being baked into the client.
 */
object ApiClient {

    @Volatile
    var token: String? = null

    @Volatile
    var baseUrl: String = BuildConfig.DEFAULT_BASE_URL

    private var cachedBaseUrl: String? = null
    private var cachedService: ApiService? = null

    private val okHttp: OkHttpClient by lazy {
        val auth = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            token?.let { builder.header("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        }
        OkHttpClient.Builder()
            .addInterceptor(auth)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                    )
                }
            }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Synchronized
    fun service(): ApiService {
        val url = baseUrl
        val cached = cachedService
        if (cached != null && cachedBaseUrl == url) return cached

        val service = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        cachedBaseUrl = url
        cachedService = service
        return service
    }
}
