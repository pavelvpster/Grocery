--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.4
-- Dumped by pg_dump version 9.5.4

-- Started on 2016-09-30 22:06:16 OMST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 7 (class 2615 OID 16439)
-- Name: grocery; Type: SCHEMA; Schema: -; Owner: grocery
--

CREATE SCHEMA grocery;


ALTER SCHEMA grocery OWNER TO grocery;

--
-- TOC entry 1 (class 3079 OID 12397)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2214 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = grocery, pg_catalog;

--
-- TOC entry 182 (class 1259 OID 16440)
-- Name: item_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE item_id_seq OWNER TO grocery;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 183 (class 1259 OID 16442)
-- Name: items; Type: TABLE; Schema: grocery; Owner: grocery
--

CREATE TABLE items (
    id bigint DEFAULT nextval('item_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE items OWNER TO grocery;

--
-- TOC entry 184 (class 1259 OID 16446)
-- Name: purchase_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE purchase_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE purchase_id_seq OWNER TO grocery;

--
-- TOC entry 185 (class 1259 OID 16448)
-- Name: purchases; Type: TABLE; Schema: grocery; Owner: grocery
--

CREATE TABLE purchases (
    id bigint DEFAULT nextval('purchase_id_seq'::regclass) NOT NULL,
    visit_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity bigint NOT NULL,
    price numeric(10,2)
);


ALTER TABLE purchases OWNER TO grocery;

--
-- TOC entry 186 (class 1259 OID 16452)
-- Name: shop_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE shop_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE shop_id_seq OWNER TO grocery;

--
-- TOC entry 190 (class 1259 OID 16490)
-- Name: shopping_list_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE shopping_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE shopping_list_id_seq OWNER TO grocery;

--
-- TOC entry 192 (class 1259 OID 16498)
-- Name: shopping_list_item_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE shopping_list_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE shopping_list_item_id_seq OWNER TO grocery;

--
-- TOC entry 193 (class 1259 OID 16500)
-- Name: shopping_list_items; Type: TABLE; Schema: grocery; Owner: grocery
--

CREATE TABLE shopping_list_items (
    id bigint DEFAULT nextval('shopping_list_item_id_seq'::regclass) NOT NULL,
    shopping_list_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity bigint DEFAULT 1
);


ALTER TABLE shopping_list_items OWNER TO grocery;

--
-- TOC entry 191 (class 1259 OID 16492)
-- Name: shopping_lists; Type: TABLE; Schema: grocery; Owner: grocery
--

CREATE TABLE shopping_lists (
    id bigint DEFAULT nextval('shopping_list_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE shopping_lists OWNER TO grocery;

--
-- TOC entry 187 (class 1259 OID 16454)
-- Name: shops; Type: TABLE; Schema: grocery; Owner: grocery
--

CREATE TABLE shops (
    id bigint DEFAULT nextval('shop_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE shops OWNER TO grocery;

--
-- TOC entry 188 (class 1259 OID 16458)
-- Name: visit_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE visit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE visit_id_seq OWNER TO grocery;

--
-- TOC entry 189 (class 1259 OID 16460)
-- Name: visits; Type: TABLE; Schema: grocery; Owner: grocery
--

CREATE TABLE visits (
    id bigint DEFAULT nextval('visit_id_seq'::regclass) NOT NULL,
    shop_id bigint NOT NULL,
    started timestamp without time zone,
    completed timestamp without time zone,
    shopping_list_id bigint
);


ALTER TABLE visits OWNER TO grocery;

--
-- TOC entry 2215 (class 0 OID 0)
-- Dependencies: 182
-- Name: item_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('item_id_seq', 1, false);


--
-- TOC entry 2196 (class 0 OID 16442)
-- Dependencies: 183
-- Data for Name: items; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY items (id, name) FROM stdin;
\.


--
-- TOC entry 2216 (class 0 OID 0)
-- Dependencies: 184
-- Name: purchase_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('purchase_id_seq', 1, false);


--
-- TOC entry 2198 (class 0 OID 16448)
-- Dependencies: 185
-- Data for Name: purchases; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY purchases (id, visit_id, item_id, quantity, price) FROM stdin;
\.


--
-- TOC entry 2217 (class 0 OID 0)
-- Dependencies: 186
-- Name: shop_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('shop_id_seq', 1, false);


--
-- TOC entry 2218 (class 0 OID 0)
-- Dependencies: 190
-- Name: shopping_list_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('shopping_list_id_seq', 1, false);


--
-- TOC entry 2219 (class 0 OID 0)
-- Dependencies: 192
-- Name: shopping_list_item_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('shopping_list_item_id_seq', 1, false);


--
-- TOC entry 2206 (class 0 OID 16500)
-- Dependencies: 193
-- Data for Name: shopping_list_items; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY shopping_list_items (id, shopping_list_id, item_id, quantity) FROM stdin;
\.


--
-- TOC entry 2204 (class 0 OID 16492)
-- Dependencies: 191
-- Data for Name: shopping_lists; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY shopping_lists (id, name) FROM stdin;
\.


--
-- TOC entry 2200 (class 0 OID 16454)
-- Dependencies: 187
-- Data for Name: shops; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY shops (id, name) FROM stdin;
\.


--
-- TOC entry 2220 (class 0 OID 0)
-- Dependencies: 188
-- Name: visit_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('visit_id_seq', 1, false);


--
-- TOC entry 2202 (class 0 OID 16460)
-- Dependencies: 189
-- Data for Name: visits; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY visits (id, shop_id, started, completed, shopping_list_id) FROM stdin;
\.


--
-- TOC entry 2060 (class 2606 OID 16465)
-- Name: item_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY items
    ADD CONSTRAINT item_pk PRIMARY KEY (id);


--
-- TOC entry 2064 (class 2606 OID 16467)
-- Name: purchase_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY purchases
    ADD CONSTRAINT purchase_pk PRIMARY KEY (id);


--
-- TOC entry 2066 (class 2606 OID 16469)
-- Name: shop_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY shops
    ADD CONSTRAINT shop_pk PRIMARY KEY (id);


--
-- TOC entry 2075 (class 2606 OID 16504)
-- Name: shopping_list_item_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY shopping_list_items
    ADD CONSTRAINT shopping_list_item_pk PRIMARY KEY (id);


--
-- TOC entry 2071 (class 2606 OID 16496)
-- Name: shopping_list_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY shopping_lists
    ADD CONSTRAINT shopping_list_pk PRIMARY KEY (id);


--
-- TOC entry 2069 (class 2606 OID 16471)
-- Name: visit_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY visits
    ADD CONSTRAINT visit_pk PRIMARY KEY (id);


--
-- TOC entry 2061 (class 1259 OID 16472)
-- Name: fki_purchase_item_fk; Type: INDEX; Schema: grocery; Owner: grocery
--

CREATE INDEX fki_purchase_item_fk ON purchases USING btree (item_id);


--
-- TOC entry 2062 (class 1259 OID 16473)
-- Name: fki_purchase_visit_fk; Type: INDEX; Schema: grocery; Owner: grocery
--

CREATE INDEX fki_purchase_visit_fk ON purchases USING btree (visit_id);


--
-- TOC entry 2072 (class 1259 OID 16518)
-- Name: fki_shopping_list_item_item_fk; Type: INDEX; Schema: grocery; Owner: grocery
--

CREATE INDEX fki_shopping_list_item_item_fk ON shopping_list_items USING btree (item_id);


--
-- TOC entry 2073 (class 1259 OID 16512)
-- Name: fki_shopping_list_item_shopping_list_fk; Type: INDEX; Schema: grocery; Owner: grocery
--

CREATE INDEX fki_shopping_list_item_shopping_list_fk ON shopping_list_items USING btree (shopping_list_id);


--
-- TOC entry 2067 (class 1259 OID 16474)
-- Name: fki_visit_shop_fk; Type: INDEX; Schema: grocery; Owner: grocery
--

CREATE INDEX fki_visit_shop_fk ON visits USING btree (shop_id);


--
-- TOC entry 2076 (class 2606 OID 16475)
-- Name: purchase_item_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY purchases
    ADD CONSTRAINT purchase_item_fk FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;


--
-- TOC entry 2077 (class 2606 OID 16480)
-- Name: purchase_visit_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY purchases
    ADD CONSTRAINT purchase_visit_fk FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE;


--
-- TOC entry 2080 (class 2606 OID 16513)
-- Name: shopping_list_item_item_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY shopping_list_items
    ADD CONSTRAINT shopping_list_item_item_fk FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;


--
-- TOC entry 2079 (class 2606 OID 16507)
-- Name: shopping_list_item_shopping_list_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY shopping_list_items
    ADD CONSTRAINT shopping_list_item_shopping_list_fk FOREIGN KEY (shopping_list_id) REFERENCES shopping_lists(id) ON DELETE CASCADE;


--
-- TOC entry 2078 (class 2606 OID 16485)
-- Name: visit_shop_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY visits
    ADD CONSTRAINT visit_shop_fk FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE;


--
-- TOC entry 2213 (class 0 OID 0)
-- Dependencies: 8
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2016-09-30 22:06:17 OMST

--
-- PostgreSQL database dump complete
--

