package com.valencia.streamhub.features.users.data.datasources.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val originalRequest = chain.request()

        return if (token != null && originalRequest.url.encodedPath.contains("/protected/") || originalRequest.url.encodedPath.contains("/api/streams")) {
            val authenticatedRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

