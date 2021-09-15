class Namespace:
    def __init__(self, base):
        self._base = base

    def entity(self, uri, **kwargs):
        pass

def namespace(base):
    return Namespace(base)

CORE = "http://trawler.dev/schema/core#"

def entity(uri, **kwargs):
    pass

def facet(uri, **kwargs):
    pass

facet("http://schema.org/name")

postgres_connector = facet(
    uri="http://trawler.dev/schema/core#postgresConnector",
    meta="json",
    json_schema={
        "type": "object"
    }
)

database_type = facet(
    uri="http://trawler.dev/schema/core#databaseType",
)



database_type = facet(
    uri="http://trawler.dev/schema/core#databaseType",
)

hostname = facet(
    uri="http://trawler.dev/schema/core#databaseType",
)

Field = entity(
    uri = CORE + "Field",
    urn = "urn:table:${type}"
)

Table = entity(
    "http://trawler.dev/schema/core#Table",
    base="table",
    schemes={
        "sql": {
            "format": "/$type/$host/$database/$table",
            "facetConstructors": {
               postgres_connector: {
                   "type": "$type",
                   "host": "$host",
                   "database": "$database",
                   "table": "$table"
               },
               hostname: "$host"
            }
        },
        "dataframe": "/${job}/${name}"
    },
)

# Are these really subtypes?
# What do we want to do e.g. about environments, you might want the environment to appear in your URN and be automatically put onto a facet.

# urn:63255f7a-e383-457a-9c30-4c7f95308749:table:sql:/postgres/db.example.com/mydb/table1
# urn::table:sql:/postgres/db.example.co/mydb/table1

# urn:<project>:<base>:<scheme>:<identifier>

has = facet(
    uri="http://trawler.dev/schema/core#has",
    meta="relationship"
)


