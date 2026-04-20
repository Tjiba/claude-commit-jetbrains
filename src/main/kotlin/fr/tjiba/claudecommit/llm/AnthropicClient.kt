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
    private val effortLevel: String = "medium",
    private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build()
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun generateCommitMessage(prompt: String): Result<String> {
        return runCatching {
            val (maxTokens, temperature) = getParametersForEffort(effortLevel)
            val payload = MessageRequest(
                model = model,
                maxTokens = maxTokens,
                temperature = temperature,
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
                val errorMessage = when (response.statusCode()) {
                    401 -> ClaudeCommitBundle.message("error.anthropic.invalidCredentials")
                    402 -> ClaudeCommitBundle.message("error.anthropic.insufficientQuota")
                    429 -> ClaudeCommitBundle.message("error.anthropic.rateLimited")
                    else -> {
                        val body = try {
                            response.body().take(400)
                        } catch (e: Exception) {
                            "Unable to parse error response"
                        }
                        "Anthropic API error ${response.statusCode()}: $body"
                    }
                }
                error(errorMessage)
            }

            val decoded = json.decodeFromString(MessageResponse.serializer(), response.body())
            val text = decoded.content.firstOrNull { it.type == "text" }?.text?.trim().orEmpty()
            require(text.isNotBlank()) { ClaudeCommitBundle.message("error.anthropic.emptyResponse") }
            text
        }
    }

    private fun getParametersForEffort(effort: String): Pair<Int, Double> {
        return when (effort.lowercase()) {
            "minimal" -> Pair(100, 0.0)      // Minimal: fastest, most deterministic
            "low" -> Pair(150, 0.1)          // Low: fast, focused
            "medium" -> Pair(220, 0.2)       // Medium: balanced (default)
            "high" -> Pair(350, 0.4)         // High: more creative and detailed
            "maximum" -> Pair(500, 0.7)      // Maximum: most thorough and creative
            else -> Pair(220, 0.2)           // Default to MEDIUM
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


