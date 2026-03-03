package com.avinashpatil.app.admindisha.di

import com.avinashpatil.app.admindisha.api.ApiService
import com.avinashpatil.app.admindisha.api.LocalDateAdapter
import com.avinashpatil.app.admindisha.api.LocalTimeAdapter
import com.avinashpatil.app.admindisha.api.QuizApiService
import com.avinashpatil.app.admindisha.api.interceptor.AuthInterceptor
import com.avinashpatil.app.admindisha.api.interceptor.RefreshTokenApi
import com.avinashpatil.app.admindisha.api.interceptor.RefreshTokenAuthenticator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // 10.0.2.2 is the special IP for Android emulator to access host machine's localhost
    //private const val BASE_URL = "http://10.0.2.2:8080/"
    //private const val BASE_URL = "http://localhost:8080/"
    //private const val BASE_URL = "http://localhost:9090/"
    //private const val BASE_URL = "http://10.0.2.2:9090/"
    //private const val BASE_URL = "http://192.168.1.2:9090/"
    //private const val BASE_URL = "http://192.168.1.3:9090/"
    //private const val BASE_URL = "http://192.168.1.5:9090/"
    //private const val BASE_URL = "https://unheritable-precognizantly-sienna.ngrok-free.dev/"
    private const val BASE_URL = "http://203.57.85.108:9090/"
    private const val TIMEOUT_SECONDS = 30L // Increased timeout for better connectivity

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                try {
                    // Enhanced logging for enrollment API calls
                    if (message.contains("enrollments/enroll") || message.contains("PaymentDto")) {
                        android.util.Log.i("EnrollmentAPI", "ENROLLMENT: $message")
                        
                        // Special tracking for PaymentDto paidAmount field
                        if (message.contains("paidAmount")) {
                            android.util.Log.w("PaymentAmount", "PAYMENT_PAIDAMOUNT_TRACKING: $message")
                        }
                        
                        // Track request body for enrollment
                        if (message.contains("{") && message.contains("paidAmount")) {
                            android.util.Log.e("PaymentBody", "PAYMENT_REQUEST_BODY: $message")
                        }
                        
                        // Track response body for enrollment
                        if (message.contains("paidAmount") || message.contains("paymentStatus")) {
                            android.util.Log.e("PaymentResponse", "PAYMENT_RESPONSE_BODY: $message")
                        }
                    }
                    
                    // Log all HTTP traffic for debugging
                    android.util.Log.d("OkHttp", message)
                } catch (e: Exception) {
                    // Safely handle logging exceptions
                    android.util.Log.e("NetworkLogging", "Logging error: ${e.message}")
                }
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY // Changed to BODY for detailed enrollment tracking
            redactHeader("Authorization") // Don't log auth tokens for security
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        refreshTokenAuthenticator: RefreshTokenAuthenticator
    ): OkHttpClient {
        val dispatcher = Dispatcher().apply {
            maxRequests = 20      // Default is 64, reduce to avoid overwhelming
            maxRequestsPerHost = 5 // Default is 5, keep as is
        }
        
        return OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                val maxRetries = 3
                val retryDelay = 1000L
                var attempt = 0
                var lastException: IOException? = null

                while (attempt <= maxRetries) {
                    try {
                        return@addInterceptor chain.proceed(request)
                    } catch (e: IOException) {
                        lastException = e
                        android.util.Log.w(
                            "NetworkRetry",
                            "Network error on attempt ${attempt + 1}/$maxRetries: ${e.message}"
                        )
                        if (attempt == maxRetries) {
                            throw lastException ?: e
                        }
                        Thread.sleep(retryDelay)
                        attempt++
                    }
                }

                throw lastException ?: IOException("Network request failed")
            }
            .authenticator(refreshTokenAuthenticator)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(okhttp3.ConnectionPool(3, 60, TimeUnit.SECONDS)) // Reduced connections, increased keep-alive
            .followRedirects(true)
            .followSslRedirects(true)
            .callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS) // Overall call timeout
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()
    }

    @MainRetrofit
    @Provides
    @Singleton
    fun provideMainRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @RefreshRetrofit
    @Provides
    @Singleton
    fun provideRefreshRetrofit(gson: Gson): Retrofit {
        // Create a custom OkHttpClient specifically for refresh token calls
        // This avoids using the same client that might have the logging interceptor causing issues
        val refreshClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // Reduced logging level to avoid buffer issues
            })
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(refreshClient) // Use the simplified client for refresh token calls
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@MainRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizApiService(@MainRetrofit retrofit: Retrofit): QuizApiService {
        return retrofit.create(QuizApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRefreshTokenApi(@RefreshRetrofit retrofit: Retrofit): RefreshTokenApi {
        return retrofit.create(RefreshTokenApi::class.java)
    }
}
