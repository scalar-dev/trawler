package dev.scalar.trawler.server.collect

data class CollectResponse(
    val facetsIngested: Int,
    val entitiesIngested: Int,
    val timeTakenMilis: Long
)
