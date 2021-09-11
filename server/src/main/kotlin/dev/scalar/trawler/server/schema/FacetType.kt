package dev.scalar.trawler.server.schema

import dev.scalar.trawler.server.db.FacetType
import java.util.*

data class FacetType(
   val uri: String,
   val id: UUID,
   val metaType: FacetType.MetaType
)
