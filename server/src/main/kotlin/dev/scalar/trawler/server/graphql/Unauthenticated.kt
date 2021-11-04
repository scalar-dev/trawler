package dev.scalar.trawler.server.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import graphql.introspection.Introspection

@GraphQLDirective(
    name = "unauthenticated",
    description = "This field does not require authentication",
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class Unauthenticated()
