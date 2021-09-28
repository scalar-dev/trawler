
INSERT INTO entity_type(id, uri, name) VALUES('76ef5961-b588-4988-923c-883909756a0a', 'http://trawler.dev/schema/core/0.1#Table', 'Table') ON CONFLICT DO NOTHING;
INSERT INTO entity_type(id, uri, name) VALUES('c3b321b7-3c98-4b76-9117-4c8f2a408c93', 'http://trawler.dev/schema/core/0.1#Field', 'Field') ON CONFLICT DO NOTHING;

INSERT INTO facet_type(id, uri, meta_type, name) VALUES('b3255046-12e8-420e-81de-d2f7517dae27', 'http://schema.org/name', 'string', 'Name') ON CONFLICT DO NOTHING;
INSERT INTO facet_type(id, uri, meta_type, name) VALUES('a294c5f0-c1f5-4970-8ccf-699be2cd0b29', 'http://trawler.dev/schema/core/0.1#hasFields', 'relationship_owned', 'hasFields') ON CONFLICT DO NOTHING;
INSERT INTO facet_type(id, uri, meta_type, name) VALUES('1bc89b90-064c-428c-b45c-262d5c6c4b60', 'http://trawler.dev/schema/core/0.1#type', 'string', 'type') ON CONFLICT DO NOTHING;

