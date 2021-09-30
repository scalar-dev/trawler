package dev.scalar.trawler.server.graphql

import graphql.schema.idl.SchemaPrinter
import java.io.File

object DumpSchema {

    @JvmStatic
    fun main(args: Array<String>) {
        File("./schema.graphql").writeText(SchemaPrinter().print(schema))
    }
}
