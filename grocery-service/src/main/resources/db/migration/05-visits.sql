CREATE SEQUENCE visit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE visits (
    id bigint DEFAULT nextval('visit_id_seq'::regclass) PRIMARY KEY,
    shop_id bigint NOT NULL,
    started timestamp without time zone,
    completed timestamp without time zone,
    shopping_list_id bigint,
    CONSTRAINT visit_shop_fk FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
);

CREATE INDEX fki_visit_shop_fk ON visits USING btree (shop_id);
