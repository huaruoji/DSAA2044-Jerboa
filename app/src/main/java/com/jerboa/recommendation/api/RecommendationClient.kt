package com.jerboa.recommendation.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jerboa.api.API
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RecommendationClient {
    // Replace with a configurable base URL if needed
    private const val BASE_URL = "http://10.4.138.233:5000/api/"

    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()  // Allow malformed JSON (e.g., unescaped characters in URLs)
            .create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(API.httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: RecommendationApi by lazy { retrofit.create(RecommendationApi::class.java) }
}

