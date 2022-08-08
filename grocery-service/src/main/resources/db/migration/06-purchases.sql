CREATE SEQUENCE purchase_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE purchases (
    id bigint DEFAULT nextval('purchase_id_seq'::regclass) PRIMARY KEY,
    visit_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity bigint NOT NULL,
    price numeric(10,2),
    CONSTRAINT purchase_item_fk FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT purchase_visit_fk FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE
);

CREATE INDEX fki_purchase_item_fk ON purchases USING btree (item_id);

CREATE INDEX fki_purchase_visit_fk ON purchases USING btree (visit_id);
