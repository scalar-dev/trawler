
CREATE TABLE account(
    id UUID PRIMARY KEY,
    password TEXT NOT NULL
);

CREATE TABLE account_info(
    account_id UUID UNIQUE NOT NULL REFERENCES account,
    email TEXT UNIQUE NOT NULL,
    first_name TEXT,
    last_name TEXT
);

CREATE TABLE account_role(
    account_id UUID NOT NULL REFERENCES account,
    project_id UUID NOT NULL REFERENCES project,
    role TEXT NOT NULL,
    UNIQUE(account_id, project_id, role)
);