package dev.scalar.trawler.ontology

import io.vertx.json.schema.Schema
import io.vertx.json.schema.ValidationException
import java.util.UUID


data class FacetType(
   val uri: String,
   val id: UUID,
   val name: String,
   val metaType: FacetMetaType,
   val projectId: UUID?,
   val indexTimeSeries: Boolean,
   val jsonSchema: Schema?
) {
   fun validate(value: Any?): Boolean {
      if (jsonSchema != null) {
         try {
            jsonSchema.validateSync(value)
         } catch (e: ValidationException) {
            println(value)
            println(e.message)
            return false
         }
      }

      return true
   }
}
