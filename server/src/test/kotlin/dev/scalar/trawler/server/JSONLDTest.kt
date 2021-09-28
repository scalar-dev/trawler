package dev.scalar.trawler.server

import com.apicatalog.jsonld.JsonLd
import com.apicatalog.jsonld.document.JsonDocument
import com.apicatalog.jsonld.loader.DocumentLoader
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.io.FileInputStream

class JSONLDTest {
    @Test
    fun it_works() {
        val doc = JsonDocument.of(FileInputStream("../python/test.json"))
        val loader = DocumentLoader { url, options ->
            if (url.toString() == "http://trawler.dev/schema/core/0.1") {
                JsonDocument.of(FileInputStream("../core.jsonld"))
            } else {
                throw Exception()
            }
        }
        val objectMapper = ObjectMapper()
        val expanded = objectMapper.readValue<List<Any>>(JsonLd.flatten(doc).loader(loader).get().toString())
        println(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(expanded))
    }
}