CREATE SEQUENCE shopping_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE shopping_lists (
    id bigint DEFAULT nextval('shopping_list_id_seq'::regclass) PRIMARY KEY,
    name character varying(255) NOT NULL
);
