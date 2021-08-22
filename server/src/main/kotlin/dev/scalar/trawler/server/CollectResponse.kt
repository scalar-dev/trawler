package dev.scalar.trawler.server

data class CollectResponse(
    val facetsIngested: Int,
    val entitiesIngested: Int,
    val timeTakenMilis: Long
)
