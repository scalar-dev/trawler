package dev.scalar.trawler.ontology

import java.util.*

data class FacetType(
   val uri: String,
   val id: UUID,
   val metaType: FacetMetaType
)
