CREATE SEQUENCE shopping_list_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE shopping_list_items (
    id bigint DEFAULT nextval('shopping_list_item_id_seq'::regclass) PRIMARY KEY,
    shopping_list_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity bigint DEFAULT 1,
    CONSTRAINT shopping_list_item_item_fk FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    CONSTRAINT shopping_list_item_shopping_list_fk FOREIGN KEY (shopping_list_id) REFERENCES shopping_lists(id) ON DELETE CASCADE
);

CREATE INDEX fki_shopping_list_item_item_fk ON shopping_list_items USING btree (item_id);

CREATE INDEX fki_shopping_list_item_shopping_list_fk ON shopping_list_items USING btree (shopping_list_id);
