package com.simats.cakeordering.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "ApiClient"
    
    // Set to true for detailed logging (slows down app)
    private const val ENABLE_DETAILED_LOGGING = false

    // ✅ BASE URL - Using LOCAL IP for FAST responses
    private const val BASE_URL = "http://192.168.137.162/Custom-Cake-Ordering/"

    // ✅ Lenient Gson to handle malformed JSON from PHP
    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    // ✅ Optimized retry interceptor with faster delays
    private class RetryInterceptor(private val maxRetries: Int = 2) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var lastException: IOException? = null
            var retryCount = 0
            
            while (retryCount < maxRetries) {
                try {
                    val request = chain.request()
                    if (retryCount > 0) {
                        Log.d(TAG, "Retry ${retryCount}/$maxRetries: ${request.url()}")
                    }
                    return chain.proceed(request)
                } catch (e: SocketTimeoutException) {
                    lastException = e
                    retryCount++
                    Log.w(TAG, "Timeout on attempt $retryCount")
                    // Faster retry: 500ms, 1000ms
                    Thread.sleep((500L * retryCount).coerceAtMost(1500L))
                } catch (e: IOException) {
                    lastException = e
                    retryCount++
                    Log.w(TAG, "Connection error: ${e.message}")
                    Thread.sleep((500L * retryCount).coerceAtMost(1500L))
                }
            }
            
            throw lastException ?: IOException("Request failed after $maxRetries retries")
        }
    }

    // ✅ Lightweight logging interceptor (minimal overhead)
    private class LightLoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val startTime = System.currentTimeMillis()
            
            Log.d(TAG, "→ ${request.method()} ${request.url().encodedPath()}")
            
            val response = chain.proceed(request)
            
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "← ${response.code()} (${duration}ms)")
            
            return response
        }
    }

    // ✅ Detailed logging interceptor (only when ENABLE_DETAILED_LOGGING is true)
    private class DetailedLoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            Log.d(TAG, "=== HTTP Request ===")
            Log.d(TAG, "URL: ${request.url()}")
            Log.d(TAG, "Method: ${request.method()}")
            
            // Log request body if present
            request.body()?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                Log.d(TAG, "Body: ${buffer.readUtf8()}")
            }
            
            val response = chain.proceed(request)
            
            Log.d(TAG, "=== HTTP Response ===")
            Log.d(TAG, "Code: ${response.code()}")
            
            // Read response body
            val responseBody = response.body()
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE)
            @Suppress("DEPRECATION")
            val buffer = source?.buffer()
            val responseString = buffer?.clone()?.readUtf8() ?: ""
            Log.d(TAG, "Response Body: $responseString")
            
            return response
        }
    }

    // ✅ OkHttpClient with increased timeout for slow networks
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)   // Increased for slow networks
            .readTimeout(30, TimeUnit.SECONDS)      // Increased for slow networks
            .writeTimeout(30, TimeUnit.SECONDS)     // Increased for slow networks
            .retryOnConnectionFailure(true)         // Auto retry on connection issues
            .addInterceptor(RetryInterceptor(3))    // Retry up to 3 times
            .build()
    }

    // ✅ Long-running OkHttpClient for AI generation (needs more time)
    private val longRunningClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)    // 2 minutes for AI generation
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Retrofit instance with long timeout for AI calls
    private val longRunningRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(longRunningClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Use this for AI generation calls
    val aiApi: ApiService by lazy {
        longRunningRetrofit.create(ApiService::class.java)
    }
}
