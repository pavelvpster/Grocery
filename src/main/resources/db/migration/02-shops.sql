CREATE SEQUENCE shop_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE shops (
    id bigint DEFAULT nextval('shop_id_seq'::regclass) PRIMARY KEY,
    name character varying(255) NOT NULL
);
