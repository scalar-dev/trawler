package dev.scalar.trawler.server

import dev.scalar.trawler.RecordRequest
import dev.scalar.trawler.RecordResponse
import dev.scalar.trawler.TrawlerGrpcKt
import java.util.UUID

class Trawler : TrawlerGrpcKt.TrawlerCoroutineImplBase() {
    override suspend fun record(request: RecordRequest): RecordResponse {
        var id: UUID? = null

        return RecordResponse.newBuilder().setMessageId(id.toString()).build()
    }
}