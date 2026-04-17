package com.valencia.streamhub.features.users.data.datasources.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val originalRequest = chain.request()

        val path = originalRequest.url.encodedPath
        return if (token != null && (path.contains("/protected/") || path.contains("/api/streams") || path.contains("/api/notifications") || path.contains("/api/followers"))) {
            val authenticatedRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

