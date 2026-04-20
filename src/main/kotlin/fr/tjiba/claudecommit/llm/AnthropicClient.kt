package fr.tjiba.claudecommit.llm

import fr.tjiba.claudecommit.i18n.ClaudeCommitBundle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class AnthropicClient(
    private val apiKey: String,
    private val model: String,
    private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun generateCommitMessage(prompt: String): Result<String> {
        return runCatching {
            val payload = MessageRequest(
                model = model,
                maxTokens = 220,
                temperature = 0.2,
                messages = listOf(Message("user", prompt))
            )

            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString(json.encodeToString(MessageRequest.serializer(), payload)))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                val body = response.body()
                val errorMsg = when {
                    response.statusCode() == 429 -> "API Rate Limited - Please wait before retrying"
                    response.statusCode() == 401 -> "Invalid API Key or insufficient permissions"
                    response.statusCode() == 400 && body.contains("quota", ignoreCase = true) -> 
                        "Insufficient quota on your Anthropic account. Please check your account and billing at console.anthropic.com"
                    response.statusCode() == 400 && body.contains("usage", ignoreCase = true) ->
                        "Usage limit exceeded on your Anthropic account. Please check your account settings"
                    else -> "Anthropic API error ${response.statusCode()}: ${body.take(400)}"
                }
                error(errorMsg)
            }

            val decoded = json.decodeFromString(MessageResponse.serializer(), response.body())
            val text = decoded.content.firstOrNull { it.type == "text" }?.text?.trim().orEmpty()
            require(text.isNotBlank()) { ClaudeCommitBundle.message("error.anthropic.emptyResponse") }
            text
        }
    }

    @Serializable
    private data class MessageRequest(
        val model: String,
        @SerialName("max_tokens") val maxTokens: Int,
        val temperature: Double,
        val messages: List<Message>
    )

    @Serializable
    private data class Message(val role: String, val content: String)

    @Serializable
    private data class MessageResponse(val content: List<Content>)

    @Serializable
    private data class Content(val type: String, val text: String = "")
}


