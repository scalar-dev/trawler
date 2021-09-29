package dev.scalar.trawler.server.collect

import java.util.*

data class CollectResponse(val transactionId: UUID, val unrecognisedFacetTypes: Set<String>, val unrecognisedEntityTypes: Set<String>)
