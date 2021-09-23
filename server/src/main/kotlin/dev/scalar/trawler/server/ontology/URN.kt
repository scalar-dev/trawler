package dev.scalar.trawler.server.ontology


data class URNScheme(
    val template: String,
    val facetMapping: Map<String, Any>
)

data class URNSpace(
    val name: String,
    val scheme: URNScheme,
) {
    fun parse(s: String): Map<String, Any> {
        throw NotImplementedError()
    }
}
