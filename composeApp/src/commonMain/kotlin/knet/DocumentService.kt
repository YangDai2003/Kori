package knet

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class DocumentService {
    private val client = DocumentClient.instance

    suspend fun fetchNetDocument(rawUrl: String): Result<String> {
        return try {
            val response = client.get(rawUrl)
            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Failed to fetch document: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}