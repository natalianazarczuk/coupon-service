create table coupons
(
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    max_usages     INTEGER      NOT NULL,
    current_usages INTEGER      NOT NULL DEFAULT 0,
    country        VARCHAR(2)   NOT NULL
);