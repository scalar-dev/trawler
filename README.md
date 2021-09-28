
# Ingest
- POST a json-ld document to API endpoint
- match entity types and attributes against ontology of known types/facets
- Update database representation
- Certain facets may be marked as index_db. In this case they should be written to some kind of index table
- Certain facets may be marked as index_search. In this case they should be written to ES or PG FTS


# Schema

## entity
- id
- type_id
- urn
- is_deleted

## facet
- id
- entity_id
- version
- timestamp
- type_id
- is_deleted

## entity_type
- id
- uri

## facet_type
- id
- uri
- meta_type

## facet_values
- id
- facet_id
- value_numeric
- value_string
- value_target_id


Dataset
urn: urn:trawler:dataset:/blah/blah
name: foo
trawler.dev/schema/core/hasColumns: [col1, col2]


entities:
id: <entity_id>
type_id: <dataset_type_id>
urn: urn:trawler:dataset:/blah/blah

id: <col1_id>
type_id: <column_type_id>
urn: urn:trawler:column:/blah/blah/col1

id: <col2_id>
type_id: <column_type_id>
urn: urn:trawler:column:/blah/blah/col2

facets:
id: <>
entity_id: <entity_id>
type_id: <name_type_id>
version: 1

id: <entity_has_column_id1>
entity_id: <entity_id>
type_id: <has_column_type_id>
version: 1

id: <>
entity_id: <entity_id>
type_id: <has_column_type_id>
version: 1

id: <>
entity_id: <col1_id>
type_id: <belongs_to_type_id>
version: 1

id: <>
entity_id: <col2_id>
type_id: <belongs_to_type_id>
version: 1

facet_values:
id: <>
facet_id: <entity_has_column_id1>
value_id: <entity_id>
