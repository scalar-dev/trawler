
INSERT INTO entity_type(id, uri) VALUES('76ef5961-b588-4988-923c-883909756a0a', 'http://trawler.dev/schema/core/0.1#Table') ON CONFLICT DO NOTHING;
INSERT INTO entity_type(id, uri) VALUES('c3b321b7-3c98-4b76-9117-4c8f2a408c93', 'http://trawler.dev/schema/core/0.1#Field') ON CONFLICT DO NOTHING;

INSERT INTO facet_type(id, uri, meta_type) VALUES('b3255046-12e8-420e-81de-d2f7517dae27', 'http://schema.org/name', 'string') ON CONFLICT DO NOTHING;
INSERT INTO facet_type(id, uri, meta_type) VALUES('a294c5f0-c1f5-4970-8ccf-699be2cd0b29', 'http://trawler.dev/schema/core/0.1#hasFields', 'relationship_owned') ON CONFLICT DO NOTHING;
