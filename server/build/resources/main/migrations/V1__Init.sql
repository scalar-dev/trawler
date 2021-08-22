
CREATE TABLE project(
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE entity_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL
);

CREATE TABLE entity(
    id UUID PRIMARY KEY,
    type_id UUID REFERENCES entity_type NOT NULL,
    urn TEXT UNIQUE NOT NULL,
    project_id UUID REFERENCES project NOT NULL,
    UNIQUE(urn, project_id)
);

CREATE TABLE facet_type(
    id UUID PRIMARY KEY,
    uri TEXT UNIQUE NOT NULL,
    meta_type TEXT NOT NULL
);

CREATE TABLE facet(
    id UUID PRIMARY KEY,
    entity_id UUID REFERENCES entity NOT NULL,
    type_id UUID REFERENCES facet_type NOT NULL,
    latest_version BIGINT NOT NULL,
    UNIQUE(entity_id, type_id)
);

CREATE TABLE facet_log(
    id UUID PRIMARY KEY,
    facet_id UUID REFERENCES facet NOT NULL,
    index BIGINT NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP,
    value_string TEXT,
    value_double DOUBLE PRECISION,
    value_long BIGINT,
    value_entity_id UUID REFERENCES entity NOT NULL,
    value_target_entity_id UUID REFERENCES entity,
    UNIQUE(facet_id, version, index)
);


CREATE TABLE facet_index(
    id UUID PRIMARY KEY,
    facet_id UUID REFERENCES facet NOT NULL,
    version BIGINT NOT NULL,
    index BIGINT NOT NULL,
    timestamp TIMESTAMP,
    value_string TEXT,
    value_double DOUBLE PRECISION,
    value_long BIGINT,
    value_entity_id UUID REFERENCES entity NOT NULL,
    value_target_entity_id UUID REFERENCES entity,
    UNIQUE(facet_id, index)
);
