package dev.scalar.trawler.ontology

enum class FacetMetaType(val value: String) {
    STRING("string"),
    BOOLEAN("boolean"),
    INT("int"),
    DOUBLE("double"),
    JSON("json"),
    RELATIONSHIP("relationship"),
    TYPE_REFERENCE("type_reference")
}