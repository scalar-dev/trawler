package dev.scalar.trawler.server.graphql

import graphql.Assert
import graphql.language.*
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.GraphQLScalarType
import java.util.*
import java.util.stream.Collectors

object Scalars {

    var uuid = GraphQLScalarType.newScalar()
        .name("UUID")
        .description("UUID")
        .coercing(object : Coercing<UUID, String> {
            override fun serialize(input: Any): String {
                return input.toString()
            }

            override fun parseValue(input: Any): UUID {
                return UUID.fromString(input as String)
            }

            override fun parseLiteral(input: Any): UUID? {
                return if (input is StringValue) {
                    UUID.fromString(input.value)
                } else {
                    null
                }
            }
        })
        .build()

    var json = GraphQLScalarType.newScalar()
        .name("Json")
        .description("A JSON blob")
        .coercing(object : Coercing<Any, Any> {
            fun typeName(input: Any?): String {
                return if (input == null) {
                    "null"
                } else input.javaClass.simpleName
            }

            override fun serialize(input: Any) = input

            override fun parseValue(input: Any): Any = input

            override fun parseLiteral(input: Any) = parseLiteral(input, emptyMap())

            override fun parseLiteral(input: Any, variables: Map<String, Any>): Any? {
                if (input !is Value<*>) {
                    throw CoercingParseLiteralException(
                        "Expected AST type 'StringValue' but was '" + typeName(input) + "'."
                    )
                }
                if (input is NullValue) {
                    return null
                }
                if (input is FloatValue) {
                    return input.value
                }
                if (input is StringValue) {
                    return input.value
                }
                if (input is IntValue) {
                    return input.value
                }
                if (input is BooleanValue) {
                    return input.isValue
                }
                if (input is EnumValue) {
                    return input.name
                }
                if (input is VariableReference) {
                    val varName = input.name
                    return variables[varName]
                }
                if (input is ArrayValue) {
                    val values = input.values
                    return values.stream()
                        .map { v -> parseLiteral(v, variables) }
                        .collect(Collectors.toList())
                }
                if (input is ObjectValue) {
                    val values = input.objectFields
                    val parsedValues = LinkedHashMap<String, Any?>()
                    values.forEach { fld ->
                        val parsedValue = parseLiteral(fld.value, variables)
                        parsedValues[fld.name] = parsedValue
                    }
                    return parsedValues
                }
                return Assert.assertShouldNeverHappen("We have covered all Value types")
            }
        })
        .build()
}