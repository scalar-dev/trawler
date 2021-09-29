package dev.scalar.trawler.server

import dev.scalar.trawler.server.graphql.schema
import graphql.schema.idl.SchemaPrinter
import java.io.File
import java.io.FileOutputStream

object DumpSchema {

    @JvmStatic
    fun main(args: Array<String>) {
        File("./schema.graphql").writeText(SchemaPrinter().print(schema))
    }
}