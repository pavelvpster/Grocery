--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.14
-- Dumped by pg_dump version 9.3.14
-- Started on 2016-08-31 12:12:34 OMST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 8 (class 2615 OID 47502)
-- Name: grocery; Type: SCHEMA; Schema: -; Owner: grocery
--

CREATE SCHEMA grocery;


ALTER SCHEMA grocery OWNER TO grocery;

--
-- TOC entry 1 (class 3079 OID 11787)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2018 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = grocery, pg_catalog;

--
-- TOC entry 173 (class 1259 OID 47506)
-- Name: item_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE grocery.item_id_seq OWNER TO grocery;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 172 (class 1259 OID 47503)
-- Name: items; Type: TABLE; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE TABLE items (
    id bigint DEFAULT nextval('item_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE grocery.items OWNER TO grocery;

--
-- TOC entry 179 (class 1259 OID 47602)
-- Name: purchase_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE purchase_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE grocery.purchase_id_seq OWNER TO grocery;

--
-- TOC entry 178 (class 1259 OID 47599)
-- Name: purchases; Type: TABLE; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE TABLE purchases (
    id bigint DEFAULT nextval('purchase_id_seq'::regclass) NOT NULL,
    visit_id bigint NOT NULL,
    item_id bigint NOT NULL,
    quantity bigint NOT NULL,
    price numeric(10,2)
);


ALTER TABLE grocery.purchases OWNER TO grocery;

--
-- TOC entry 175 (class 1259 OID 47512)
-- Name: shop_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE shop_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE grocery.shop_id_seq OWNER TO grocery;

--
-- TOC entry 174 (class 1259 OID 47509)
-- Name: shops; Type: TABLE; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE TABLE shops (
    id bigint DEFAULT nextval('shop_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE grocery.shops OWNER TO grocery;

--
-- TOC entry 177 (class 1259 OID 47587)
-- Name: visit_id_seq; Type: SEQUENCE; Schema: grocery; Owner: grocery
--

CREATE SEQUENCE visit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE grocery.visit_id_seq OWNER TO grocery;

--
-- TOC entry 176 (class 1259 OID 47584)
-- Name: visits; Type: TABLE; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE TABLE visits (
    id bigint DEFAULT nextval('visit_id_seq'::regclass) NOT NULL,
    shop_id bigint NOT NULL,
    started timestamp without time zone,
    completed timestamp without time zone
);


ALTER TABLE grocery.visits OWNER TO grocery;

--
-- TOC entry 2019 (class 0 OID 0)
-- Dependencies: 173
-- Name: item_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('item_id_seq', 1, false);


--
-- TOC entry 2003 (class 0 OID 47503)
-- Dependencies: 172
-- Data for Name: items; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY items (id, name) FROM stdin;
\.


--
-- TOC entry 2020 (class 0 OID 0)
-- Dependencies: 179
-- Name: purchase_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('purchase_id_seq', 1, false);


--
-- TOC entry 2009 (class 0 OID 47599)
-- Dependencies: 178
-- Data for Name: purchases; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY purchases (id, visit_id, item_id, quantity, price) FROM stdin;
\.


--
-- TOC entry 2021 (class 0 OID 0)
-- Dependencies: 175
-- Name: shop_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('shop_id_seq', 1, false);


--
-- TOC entry 2005 (class 0 OID 47509)
-- Dependencies: 174
-- Data for Name: shops; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY shops (id, name) FROM stdin;
\.


--
-- TOC entry 2022 (class 0 OID 0)
-- Dependencies: 177
-- Name: visit_id_seq; Type: SEQUENCE SET; Schema: grocery; Owner: grocery
--

SELECT pg_catalog.setval('visit_id_seq', 1, false);


--
-- TOC entry 2007 (class 0 OID 47584)
-- Dependencies: 176
-- Data for Name: visits; Type: TABLE DATA; Schema: grocery; Owner: grocery
--

COPY visits (id, shop_id, started, completed) FROM stdin;
\.


--
-- TOC entry 1883 (class 2606 OID 47521)
-- Name: item_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery; Tablespace: 
--

ALTER TABLE ONLY items
    ADD CONSTRAINT item_pk PRIMARY KEY (id);


--
-- TOC entry 1892 (class 2606 OID 47605)
-- Name: purchase_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery; Tablespace: 
--

ALTER TABLE ONLY purchases
    ADD CONSTRAINT purchase_pk PRIMARY KEY (id);


--
-- TOC entry 1885 (class 2606 OID 47523)
-- Name: shop_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery; Tablespace: 
--

ALTER TABLE ONLY shops
    ADD CONSTRAINT shop_pk PRIMARY KEY (id);


--
-- TOC entry 1888 (class 2606 OID 47590)
-- Name: visit_pk; Type: CONSTRAINT; Schema: grocery; Owner: grocery; Tablespace: 
--

ALTER TABLE ONLY visits
    ADD CONSTRAINT visit_pk PRIMARY KEY (id);


--
-- TOC entry 1889 (class 1259 OID 47630)
-- Name: fki_purchase_item_fk; Type: INDEX; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE INDEX fki_purchase_item_fk ON purchases USING btree (item_id);


--
-- TOC entry 1890 (class 1259 OID 47624)
-- Name: fki_purchase_visit_fk; Type: INDEX; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE INDEX fki_purchase_visit_fk ON purchases USING btree (visit_id);


--
-- TOC entry 1886 (class 1259 OID 47598)
-- Name: fki_visit_shop_fk; Type: INDEX; Schema: grocery; Owner: grocery; Tablespace: 
--

CREATE INDEX fki_visit_shop_fk ON visits USING btree (shop_id);


--
-- TOC entry 1895 (class 2606 OID 47625)
-- Name: purchase_item_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY purchases
    ADD CONSTRAINT purchase_item_fk FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;


--
-- TOC entry 1894 (class 2606 OID 47619)
-- Name: purchase_visit_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY purchases
    ADD CONSTRAINT purchase_visit_fk FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE CASCADE;


--
-- TOC entry 1893 (class 2606 OID 47593)
-- Name: visit_shop_fk; Type: FK CONSTRAINT; Schema: grocery; Owner: grocery
--

ALTER TABLE ONLY visits
    ADD CONSTRAINT visit_shop_fk FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE;


--
-- TOC entry 2017 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2016-08-31 12:12:34 OMST

--
-- PostgreSQL database dump complete
--

