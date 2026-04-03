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

class AnthropicModelsClient(
    private val apiKey: String,
    private val httpClient: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchModelIds(): Result<List<String>> {
        return runCatching {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/models"))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                error(ClaudeCommitBundle.message("error.models.fetchFailed", response.statusCode()))
            }

            val decoded = json.decodeFromString(ModelsResponse.serializer(), response.body())
            decoded.data.map { it.id }.filter { it.isNotBlank() }
        }
    }

    @Serializable
    private data class ModelsResponse(val data: List<ModelData> = emptyList())

    @Serializable
    private data class ModelData(
        val id: String,
        @SerialName("display_name") val displayName: String? = null,
    )
}




