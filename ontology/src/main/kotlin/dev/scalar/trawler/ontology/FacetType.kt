package dev.scalar.trawler.ontology

import java.util.*

data class FacetType(
   val uri: String,
   val id: UUID,
   val name: String,
   val metaType: FacetMetaType,
   val projectId: UUID?
)
