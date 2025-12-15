package com.jurobil.progressian.data.remote.ai.model

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<RequestContent>,
    val generationConfig: GenerationConfig = GenerationConfig()
)

data class RequestContent(
    val parts: List<RequestPart>,
    val role: String = "user"
)

data class RequestPart(
    val text: String
)

data class GenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 1024,
    val responseMimeType: String = "application/json"
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ResponseContent?
)

data class ResponseContent(
    val parts: List<ResponsePart>?
)

data class ResponsePart(
    val text: String?
)