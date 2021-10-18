import dev.scalar.trawler.ontology.FacetMetaType
import dev.scalar.trawler.ontology.Ontology
import jakarta.json.Json
import jakarta.json.JsonObject

val SCHEMA_ORG = "http://schema.org/"
val CORE = "http://trawler.dev/schema/core"
val METRICS = "http://trawler.dev/schema/metrics"

val aliasNamespaces = mapOf(
    CORE to "tr",
    METRICS to "metrics"
)

fun ontologyToContext(ontology: Ontology): JsonObject? {
    val aliases = ontology.facetTypes().map { facetType ->
        val key = if (facetType.uri.startsWith("http://trawler.dev")) {
            val bits = facetType.uri.split("#")

            if (bits.size == 2 && bits[0] in aliasNamespaces) {
                "${aliasNamespaces[bits[0]]}:${bits[1]}"
            } else {
                facetType.uri
            }
        } else if (facetType.uri.startsWith(SCHEMA_ORG)) {
            facetType.uri.substringAfter(SCHEMA_ORG)
        } else {
            facetType.uri
        }

        if (facetType.metaType == FacetMetaType.RELATIONSHIP) {
            key to mapOf("@id" to facetType.uri, "@type" to "@id")
        } else if (facetType.metaType == FacetMetaType.JSON) {
            key to mapOf("@id" to facetType.uri, "@type" to "@json")
        } else {
            key to facetType.uri
        }
    }

    val namespaces = listOf(
        "dcat" to "http://www.w3.org/ns/dcat#",
        "prov" to "http://www.w3.org/ns/prov#",
        "tr" to "http://trawler.dev/schema/core#",
        "metrics" to "http://trawler.dev/schema/metrics#",
    )

    val doc = mapOf("@context" to (namespaces + aliases).toMap())
    return Json.createObjectBuilder(doc).build()
}
