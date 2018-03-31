--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.8
-- Dumped by pg_dump version 9.6.8

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: tablefunc; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS tablefunc WITH SCHEMA public;


--
-- Name: EXTENSION tablefunc; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION tablefunc IS 'functions that manipulate whole tables, including crosstab';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: admin_login; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.admin_login (
    id integer NOT NULL,
    membership integer,
    username character varying(50),
    password character varying(50),
    super boolean
);


ALTER TABLE public.admin_login OWNER TO staffing;

--
-- Name: assignable_area; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.assignable_area (
    id integer NOT NULL,
    anyone boolean,
    name character varying(50),
    public_facing boolean,
    short_name character varying(10),
    type integer,
    formarea_id integer
);


ALTER TABLE public.assignable_area OWNER TO staffing;

--
-- Name: form_area; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.form_area (
    id integer NOT NULL,
    dontmind boolean,
    name character varying(50)
);


ALTER TABLE public.form_area OWNER TO staffing;

--
-- Name: volunteer; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.volunteer (
    id integer NOT NULL,
    callsign character varying(40),
    camping boolean NOT NULL,
    cellar boolean NOT NULL,
    comment text,
    confirmed boolean NOT NULL,
    email character varying(100),
    emailverified boolean NOT NULL,
    firstaid boolean NOT NULL,
    forename character varying(50),
    forklift boolean NOT NULL,
    instructions boolean NOT NULL,
    managervouch character varying(100),
    membership character varying(20),
    other boolean NOT NULL,
    password character varying(40),
    picture bytea,
    role character varying(40),
    sia boolean NOT NULL,
    surname character varying(50),
    tshirt integer,
    uuid character varying(36),
    verified boolean NOT NULL
);


ALTER TABLE public.volunteer OWNER TO staffing;

--
-- Name: volunteer_area; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.volunteer_area (
    areaid integer NOT NULL,
    volunteerid integer NOT NULL,
    preference integer
);


ALTER TABLE public.volunteer_area OWNER TO staffing;

--
-- Name: area_selector; Type: VIEW; Schema: public; Owner: staffing
--

CREATE VIEW public.area_selector AS
 SELECT DISTINCT v.id AS volunteerid,
    a.id AS areaid,
    a.name,
        CASE
            WHEN (va.preference IS NULL) THEN 0
            ELSE va.preference
        END AS preference
   FROM (((public.volunteer v
     JOIN public.form_area a ON ((a.id = a.id)))
     JOIN public.assignable_area aa ON ((aa.formarea_id = a.id)))
     LEFT JOIN public.volunteer_area va ON (((va.volunteerid = v.id) AND (va.areaid = aa.id))))
UNION
 SELECT 0 AS volunteerid,
    a.id AS areaid,
    a.name,
    (a.dontmind)::integer AS preference
   FROM public.form_area a;


ALTER TABLE public.area_selector OWNER TO staffing;

--
-- Name: area_session; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.area_session (
    required integer,
    areaid integer NOT NULL,
    sessionid integer NOT NULL
);


ALTER TABLE public.area_session OWNER TO staffing;

--
-- Name: assigned_counts; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.assigned_counts (
    assigned bigint,
    worked bigint,
    areaid integer,
    sessionid integer,
    required integer
);

ALTER TABLE ONLY public.assigned_counts REPLICA IDENTITY NOTHING;


ALTER TABLE public.assigned_counts OWNER TO staffing;

--
-- Name: session; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.session (
    id integer NOT NULL,
    finish timestamp without time zone,
    name character varying(50),
    night boolean,
    open boolean,
    setup boolean,
    special boolean,
    start timestamp without time zone,
    takedown boolean
);


ALTER TABLE public.session OWNER TO staffing;

--
-- Name: volunteer_session; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.volunteer_session (
    comment character varying(255),
    finish timestamp without time zone,
    locked boolean,
    start timestamp without time zone,
    tokens integer,
    worked boolean,
    sessionid integer NOT NULL,
    volunteerid integer NOT NULL,
    areaid integer
);


ALTER TABLE public.volunteer_session OWNER TO staffing;

--
-- Name: main_view; Type: VIEW; Schema: public; Owner: staffing
--

CREATE VIEW public.main_view AS
 SELECT (((v.id * 10000) + (vs.sessionid * 100)) + va.areaid) AS id,
    v.id AS volunteerid,
    v.forename,
    v.surname,
    vs.sessionid,
    s.name AS session_name,
    va.areaid,
    aa.name AS area_name,
    (va.areaid = vs.areaid) AS current,
    ac.assigned,
    COALESCE(ac.worked, (0)::bigint) AS worked,
    ac.required
   FROM (((((public.volunteer v
     JOIN public.volunteer_area va ON ((va.volunteerid = v.id)))
     JOIN public.volunteer_session vs ON ((vs.volunteerid = v.id)))
     JOIN public.assignable_area aa ON ((aa.id = va.areaid)))
     JOIN public.session s ON ((s.id = vs.sessionid)))
     JOIN public.assigned_counts ac ON (((ac.areaid = aa.id) AND (ac.sessionid = s.id))));


ALTER TABLE public.main_view OWNER TO staffing;

--
-- Name: possible_session; Type: VIEW; Schema: public; Owner: staffing
--

CREATE VIEW public.possible_session AS
 SELECT v.id AS volunteerid,
    s.id AS sessionid,
    s.name,
    s.start,
    s.finish,
    s.night,
    s.open,
    s.setup,
    s.takedown,
    vs.finish AS volunteerfinish,
    vs.start AS volunteerstart,
        CASE
            WHEN (vs.sessionid IS NULL) THEN 0
            ELSE 1
        END AS assigned
   FROM ((public.session s
     JOIN public.volunteer v ON ((v.id = v.id)))
     LEFT JOIN public.volunteer_session vs ON (((vs.sessionid = s.id) AND (vs.volunteerid = v.id))))
  ORDER BY s.start;


ALTER TABLE public.possible_session OWNER TO staffing;

--
-- Name: sequence; Type: TABLE; Schema: public; Owner: staffing
--

CREATE TABLE public.sequence (
    seq_name character varying(50) NOT NULL,
    seq_count numeric(38,0)
);


ALTER TABLE public.sequence OWNER TO staffing;

--
-- Name: view_assignment_selector; Type: VIEW; Schema: public; Owner: staffing
--

CREATE VIEW public.view_assignment_selector AS
 SELECT vs.volunteerid,
    vs.sessionid,
    aa.id AS areaid,
    aa.name,
    va.preference,
    c.assigned,
    c.required,
        CASE
            WHEN (vs.areaid = aa.id) THEN 1
            ELSE 0
        END AS selected
   FROM (((public.assignable_area aa
     JOIN public.volunteer_area va ON ((va.areaid = aa.id)))
     JOIN public.assigned_counts c ON ((c.areaid = va.areaid)))
     JOIN public.volunteer_session vs ON (((vs.sessionid = c.sessionid) AND (vs.volunteerid = va.volunteerid))));


ALTER TABLE public.view_assignment_selector OWNER TO staffing;

--
-- Name: view_volunteer_session; Type: VIEW; Schema: public; Owner: staffing
--

CREATE VIEW public.view_volunteer_session AS
 SELECT v.id AS volunteerid,
    v.forename,
    v.surname,
    s.id AS sessionid,
    s.name AS sessionname,
    a.id AS areaid,
    a.name AS areaname,
    vs.locked,
    vs.start,
    vs.finish,
    s.start AS sessionstart,
    s.finish AS sessionfinish,
    vs.comment,
    vs.worked,
    vs.tokens,
    c.assigned,
    c.required
   FROM ((((public.volunteer_session vs
     JOIN public.assigned_counts c ON (((c.areaid = vs.areaid) AND (c.sessionid = vs.sessionid))))
     JOIN public.volunteer v ON ((v.id = vs.volunteerid)))
     JOIN public.session s ON ((s.id = vs.sessionid)))
     JOIN public.assignable_area a ON ((a.id = vs.areaid)));


ALTER TABLE public.view_volunteer_session OWNER TO staffing;

--
-- Data for Name: admin_login; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.admin_login (id, membership, username, password, super) FROM stdin;
1	176340	\N	\N	t
\.


--
-- Data for Name: area_session; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.area_session (required, areaid, sessionid) FROM stdin;
0	-1	0
0	-1	1
0	-1	2
0	-1	3
0	-1	10
0	-1	11
0	-1	12
0	-1	20
0	-1	21
0	-1	22
0	-1	30
0	-1	31
0	-1	32
0	-1	33
0	-1	39
0	-1	40
0	-1	41
0	-1	42
0	-1	43
0	-1	44
0	-1	50
0	-1	51
0	-1	52
0	-1	53
0	-1	54
0	-1	60
0	-1	61
0	-1	62
0	-1	63
0	-1	71
0	-1	72
0	-1	73
0	-1	74
0	-1	80
0	-1	81
0	0	0
0	0	1
0	0	2
0	0	3
0	0	10
0	0	11
0	0	12
0	0	20
0	0	21
0	0	22
0	0	30
0	0	31
0	0	32
0	0	33
0	0	39
0	0	40
0	0	41
0	0	42
0	0	43
0	0	44
0	0	50
0	0	51
0	0	52
0	0	53
0	0	54
0	0	60
0	0	61
0	0	62
0	0	63
0	0	71
0	0	72
0	0	73
0	0	74
0	0	80
0	0	81
0	1	0
0	1	1
0	1	2
0	1	3
0	1	10
0	1	11
0	1	12
0	1	20
0	1	21
0	1	22
0	1	30
16	1	31
16	1	32
0	1	33
16	1	39
16	1	40
16	1	41
20	1	42
20	1	43
0	1	44
16	1	50
16	1	51
20	1	52
20	1	53
0	1	54
11	1	60
11	1	61
0	1	62
0	1	63
0	1	71
0	1	72
0	1	73
0	1	74
0	1	80
0	1	81
0	2	0
0	2	1
0	2	2
0	2	3
0	2	10
0	2	11
0	2	12
0	2	20
0	2	21
0	2	22
0	2	30
16	2	31
16	2	32
0	2	33
16	2	39
16	2	40
16	2	41
20	2	42
20	2	43
0	2	44
16	2	50
16	2	51
20	2	52
20	2	53
0	2	54
11	2	60
11	2	61
0	2	62
0	2	63
0	2	71
0	2	72
0	2	73
0	2	74
0	2	80
0	2	81
0	3	0
0	3	1
0	3	2
0	3	3
0	3	10
0	3	11
0	3	12
0	3	20
0	3	21
0	3	22
0	3	30
16	3	31
16	3	32
0	3	33
16	3	39
16	3	40
16	3	41
20	3	42
20	3	43
0	3	44
16	3	50
16	3	51
20	3	52
20	3	53
0	3	54
11	3	60
11	3	61
0	3	62
0	3	63
0	3	71
0	3	72
0	3	73
0	3	74
0	3	80
0	3	81
0	4	0
0	4	1
0	4	2
0	4	3
0	4	10
0	4	11
0	4	12
0	4	20
0	4	21
0	4	22
0	4	30
16	4	31
16	4	32
0	4	33
16	4	39
16	4	40
16	4	41
20	4	42
20	4	43
0	4	44
16	4	50
16	4	51
20	4	52
20	4	53
0	4	54
11	4	60
11	4	61
0	4	62
0	4	63
0	4	71
0	4	72
0	4	73
0	4	74
0	4	80
0	4	81
2	8	0
4	8	1
4	8	2
0	8	3
4	8	10
4	8	11
0	8	12
4	8	20
4	8	21
0	8	22
8	8	30
10	8	31
12	8	32
0	8	33
10	8	39
12	8	40
15	8	41
18	8	42
18	8	43
0	8	44
12	8	50
15	8	51
18	8	52
18	8	53
0	8	54
10	8	60
10	8	61
10	8	62
0	8	63
4	8	71
4	8	72
0	8	73
0	8	74
0	8	80
0	8	81
2	9	0
2	9	1
2	9	2
0	9	3
2	9	10
2	9	11
0	9	12
2	9	20
2	9	21
0	9	22
3	9	30
8	9	31
8	9	32
0	9	33
8	9	39
8	9	40
8	9	41
10	9	42
10	9	43
0	9	44
8	9	50
8	9	51
10	9	52
10	9	53
0	9	54
6	9	60
6	9	61
0	9	62
0	9	63
0	9	71
0	9	72
0	9	73
0	9	74
0	9	80
0	9	81
0	10	0
0	10	1
0	10	2
0	10	3
0	10	10
0	10	11
0	10	12
0	10	20
0	10	21
0	10	22
0	10	30
4	10	31
4	10	32
0	10	33
4	10	39
4	10	40
4	10	41
5	10	42
5	10	43
0	10	44
4	10	50
4	10	51
5	10	52
5	10	53
0	10	54
4	10	60
4	10	61
0	10	62
0	10	63
0	10	71
0	10	72
0	10	73
0	10	74
0	10	80
0	10	81
0	11	0
0	11	1
0	11	2
0	11	3
0	11	10
0	11	11
0	11	12
0	11	20
0	11	21
0	11	22
0	11	30
12	11	31
14	11	32
0	11	33
12	11	39
14	11	40
14	11	41
14	11	42
14	11	43
0	11	44
14	11	50
14	11	51
14	11	52
14	11	53
0	11	54
14	11	60
14	11	61
0	11	62
0	11	63
0	11	71
0	11	72
0	11	73
0	11	74
0	11	80
0	11	81
0	12	0
0	12	1
0	12	2
0	12	3
0	12	10
0	12	11
0	12	12
0	12	20
0	12	21
0	12	22
0	12	30
3	12	31
3	12	32
0	12	33
3	12	39
3	12	40
3	12	41
3	12	42
3	12	43
0	12	44
3	12	50
3	12	51
3	12	52
3	12	53
0	12	54
3	12	60
3	12	61
0	12	62
0	12	63
0	12	71
0	12	72
0	12	73
0	12	74
0	12	80
0	12	81
0	13	0
0	13	1
0	13	2
0	13	3
0	13	10
0	13	11
0	13	12
0	13	20
0	13	21
0	13	22
0	13	30
0	13	31
0	13	32
0	13	33
0	13	39
0	13	40
0	13	41
0	13	42
0	13	43
0	13	44
0	13	50
0	13	51
0	13	52
0	13	53
0	13	54
0	13	60
0	13	61
0	13	62
0	13	63
0	13	71
0	13	72
0	13	73
0	13	74
0	13	80
0	13	81
2	14	0
5	14	1
5	14	2
4	14	3
5	14	10
5	14	11
4	14	12
5	14	20
5	14	21
4	14	22
20	14	30
38	14	31
42	14	32
4	14	33
38	14	39
42	14	40
48	14	41
48	14	42
48	14	43
6	14	44
42	14	50
48	14	51
48	14	52
48	14	53
6	14	54
34	14	60
42	14	61
10	14	62
6	14	63
5	14	71
5	14	72
5	14	73
4	14	74
0	14	80
5	14	81
0	15	0
0	15	1
0	15	2
0	15	3
0	15	10
0	15	11
0	15	12
0	15	20
0	15	21
0	15	22
0	15	30
9	15	31
9	15	32
0	15	33
9	15	39
9	15	40
9	15	41
9	15	42
9	15	43
0	15	44
9	15	50
9	15	51
9	15	52
9	15	53
0	15	54
9	15	60
9	15	61
0	15	62
0	15	63
0	15	71
0	15	72
0	15	73
0	15	74
0	15	80
0	15	81
0	16	0
0	16	1
0	16	2
0	16	3
0	16	10
0	16	11
0	16	12
0	16	20
0	16	21
0	16	22
0	16	30
2	16	31
2	16	32
0	16	33
2	16	39
2	16	40
2	16	41
2	16	42
2	16	43
0	16	44
2	16	50
2	16	51
2	16	52
2	16	53
0	16	54
2	16	60
2	16	61
0	16	62
0	16	63
0	16	71
0	16	72
0	16	73
0	16	74
0	16	80
0	16	81
0	17	0
0	17	1
0	17	2
0	17	3
0	17	10
0	17	11
0	17	12
0	17	20
0	17	21
0	17	22
0	17	30
0	17	31
0	17	32
0	17	33
0	17	39
0	17	40
0	17	41
0	17	42
0	17	43
0	17	44
0	17	50
0	17	51
0	17	52
0	17	53
0	17	54
0	17	60
0	17	61
0	17	62
0	17	63
0	17	71
0	17	72
0	17	73
0	17	74
0	17	80
0	17	81
0	18	0
0	18	1
0	18	2
0	18	3
0	18	10
0	18	11
0	18	12
0	18	20
0	18	21
0	18	22
0	18	30
0	18	31
0	18	32
0	18	33
0	18	39
0	18	40
0	18	41
0	18	42
0	18	43
0	18	44
0	18	50
0	18	51
0	18	52
0	18	53
0	18	54
0	18	60
0	18	61
0	18	62
0	18	63
0	18	71
0	18	72
0	18	73
0	18	74
0	18	80
0	18	81
0	19	0
0	19	1
0	19	2
0	19	3
0	19	10
0	19	11
0	19	12
2	19	20
2	19	21
0	19	22
0	19	30
2	19	31
2	19	32
0	19	33
2	19	39
2	19	40
2	19	41
2	19	42
2	19	43
0	19	44
2	19	50
2	19	51
2	19	52
2	19	53
0	19	54
2	19	60
2	19	61
0	19	62
0	19	63
0	19	71
0	19	72
0	19	73
0	19	74
0	19	80
0	19	81
1	23	0
4	23	1
4	23	2
0	23	3
4	23	10
4	23	11
0	23	12
4	23	20
4	23	21
0	23	22
0	23	30
0	23	31
0	23	32
0	23	33
0	23	39
0	23	40
0	23	41
0	23	42
0	23	43
0	23	44
0	23	50
0	23	51
0	23	52
0	23	53
0	23	54
0	23	60
0	23	61
0	23	62
0	23	63
0	23	71
0	23	72
0	23	73
0	23	74
0	23	80
0	23	81
0	24	0
0	24	1
0	24	2
0	24	3
0	24	10
0	24	11
0	24	12
0	24	20
0	24	21
0	24	22
5	24	30
5	24	31
0	24	32
0	24	33
5	24	39
0	24	40
0	24	41
0	24	42
0	24	43
0	24	44
0	24	50
0	24	51
0	24	52
0	24	53
0	24	54
0	24	60
0	24	61
0	24	62
0	24	63
0	24	71
0	24	72
0	24	73
0	24	74
0	24	80
0	24	81
1	25	0
1	25	1
1	25	2
0	25	3
1	25	10
1	25	11
0	25	12
1	25	20
1	25	21
0	25	22
1	25	30
4	25	31
4	25	32
0	25	33
4	25	39
4	25	40
5	25	41
5	25	42
5	25	43
0	25	44
5	25	50
5	25	51
5	25	52
5	25	53
0	25	54
4	25	60
5	25	61
5	25	62
0	25	63
0	25	71
0	25	72
0	25	73
0	25	74
0	25	80
0	25	81
0	26	0
0	26	1
0	26	2
0	26	3
0	26	10
0	26	11
0	26	12
0	26	20
0	26	21
0	26	22
0	26	30
0	26	31
0	26	32
0	26	33
0	26	39
0	26	40
0	26	41
0	26	42
0	26	43
0	26	44
0	26	50
0	26	51
0	26	52
0	26	53
0	26	54
0	26	60
0	26	61
0	26	62
0	26	63
0	26	71
0	26	72
0	26	73
0	26	74
0	26	81
3	27	0
3	27	1
3	27	2
0	27	3
3	27	10
3	27	11
0	27	12
3	27	20
3	27	21
0	27	22
3	27	30
3	27	31
3	27	32
0	27	33
3	27	39
3	27	40
3	27	41
3	27	42
3	27	43
0	27	44
3	27	50
3	27	51
3	27	52
3	27	53
0	27	54
3	27	60
3	27	61
0	27	62
0	27	63
0	27	71
0	27	72
0	27	73
0	27	74
0	27	80
0	27	81
5	28	0
10	28	1
10	28	2
0	28	3
10	28	10
10	28	11
0	28	12
10	28	20
10	28	21
0	28	22
10	28	30
10	28	31
10	28	32
0	28	33
10	28	39
10	28	40
10	28	41
10	28	42
10	28	43
0	28	44
10	28	50
10	28	51
10	28	52
10	28	53
0	28	54
10	28	60
10	28	61
10	28	62
0	28	63
10	28	71
10	28	72
10	28	73
0	28	74
0	28	80
10	28	81
0	29	0
0	29	1
0	29	2
0	29	3
0	29	10
0	29	11
0	29	12
0	29	20
0	29	21
0	29	22
0	29	30
0	29	31
0	29	32
0	29	33
0	29	39
0	29	40
0	29	41
0	29	42
0	29	43
0	29	44
0	29	50
0	29	51
0	29	52
0	29	53
0	29	54
0	29	60
0	29	61
0	29	62
0	29	63
0	29	71
0	29	72
0	29	73
0	29	74
0	29	80
0	29	81
10	30	0
10	30	1
10	30	2
10	30	3
10	30	10
10	30	11
10	30	12
10	30	20
10	30	21
10	30	22
10	30	30
10	30	31
10	30	32
10	30	33
10	30	39
10	30	40
10	30	41
10	30	42
10	30	43
10	30	44
10	30	50
10	30	51
10	30	52
10	30	53
10	30	54
10	30	60
10	30	61
10	30	62
10	30	63
10	30	71
10	30	72
10	30	73
10	30	74
10	30	80
10	30	81
25	38	0
25	38	1
25	38	2
0	38	3
16	38	10
16	38	11
0	38	12
16	38	20
16	38	21
0	38	22
16	38	30
10	38	31
0	38	32
0	38	33
10	38	39
0	38	40
0	38	41
0	38	42
0	38	43
0	38	44
0	38	50
0	38	51
0	38	52
0	38	53
0	38	54
0	38	60
0	38	61
16	38	62
0	38	63
16	38	71
16	38	72
16	38	73
0	38	74
10	38	80
16	38	81
0	40	0
0	40	1
0	40	2
0	40	3
0	40	10
0	40	11
0	40	12
0	40	20
0	40	21
0	40	22
0	40	30
0	40	31
0	40	32
0	40	33
0	40	39
0	40	40
0	40	41
0	40	42
0	40	43
0	40	44
0	40	50
0	40	51
0	40	52
0	40	53
0	40	54
0	40	60
0	40	61
0	40	62
0	40	63
0	40	71
0	40	72
0	40	73
0	40	74
0	40	80
0	40	81
0	41	0
0	41	1
0	41	2
0	41	3
0	41	10
0	41	11
0	41	12
0	41	20
0	41	21
0	41	22
0	41	30
2	41	31
2	41	32
2	41	33
0	41	39
2	41	40
2	41	41
2	41	42
2	41	43
0	41	44
2	41	50
2	41	51
2	41	52
2	41	53
0	41	54
2	41	60
2	41	61
0	41	62
0	41	63
0	41	71
0	41	72
0	41	73
0	41	74
0	41	80
0	41	81
0	99	0
0	99	1
0	99	2
0	99	3
0	99	10
0	99	11
0	99	12
0	99	20
0	99	21
0	99	22
0	99	30
0	99	31
0	99	32
0	99	33
0	99	39
0	99	40
0	99	41
0	99	42
0	99	43
0	99	44
0	99	50
0	99	51
0	99	52
0	99	53
0	99	54
0	99	60
0	99	61
0	99	62
0	99	63
0	99	71
0	99	72
0	99	73
0	99	74
0	99	80
0	99	81
\.


--
-- Data for Name: assignable_area; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.assignable_area (id, anyone, name, public_facing, short_name, type, formarea_id) FROM stdin;
-1	t	Unassigned	f	\N	0	\N
0	t	Not needed	f	\N	0	\N
1	f	Beer Bar A	t	Beer A	0	1
2	f	Beer Bar B	t	Beer B	0	1
3	f	Beer Bar C	t	Beer C	0	1
4	f	Beer Bar D	t	Beer D	0	1
8	f	Cider Bar	t	Cider	0	2
9	f	Foreign Beer Bar	t	Foreign	0	3
10	f	Wine Bar	t	Wine	0	4
11	f	Entrance and Glasses	t	Entrance	0	5
12	f	CAMRA Stand	t	CAMRA	0	6
13	f	CAMRA Products	t	Product	0	7
14	f	Stewards	t	Stewards	0	8
15	f	Games	t	Games	0	9
16	f	Tombola	t	Tombola	0	10
17	f	Cider Judging	f	CJ	0	\N
18	f	Behind the Scenes	f	BTS	0	12
19	f	Litter Picking	f	Litter	0	13
23	f	Setup Catering	f	Kitchen	0	12
24	f	Beer Judge Assist	f	CBOB Ast	0	12
25	f	Finance Hut	f	Hut	0	12
26	f	Office	f	Office	0	12
27	f	Staffing Office	f	Staffing	0	12
28	f	Beer Cellar	f	Cellar	0	12
29	f	Cider Judge Assist	f	CdrJ Ast	0	12
30	f	Site Team	f	Site	0	12
38	t	Setup/Takedown	f	Build	0	\N
40	f	Traffic Management	f	Traffic	0	11
41	f	Clickers	t	Clickers	0	12
99	t	Cuddly Toys	f	Toys	0	\N
\.


--
-- Data for Name: form_area; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.form_area (id, dontmind, name) FROM stdin;
1	t	Beer Bar
2	t	Cider Bar
3	t	Foreign Beer Bar
4	t	Wine Bar
5	t	Entrance and Glasses
6	t	CAMRA Membership
7	t	CAMRA Products
8	t	Stewards
9	t	Games
10	t	Tombola
11	t	Traffic Management
12	t	Behind the Scenes
13	t	Litter Picking
\.


--
-- Data for Name: sequence; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.sequence (seq_name, seq_count) FROM stdin;
SEQ_GEN	2000
\.


--
-- Data for Name: session; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.session (id, finish, name, night, open, setup, special, start, takedown) FROM stdin;
0	2017-04-24 13:00:00	Monday Setup 1	f	f	t	f	2017-04-24 09:00:00	f
1	2017-04-24 17:00:00	Monday Setup 2	f	f	t	f	2017-04-24 13:00:00	f
2	2017-04-24 21:00:00	Monday Setup 3	f	f	t	f	2017-04-24 17:00:00	f
3	2017-04-25 08:00:00	Monday Night	t	f	t	t	2017-04-24 21:00:00	f
10	2017-04-25 13:00:00	Tuesday Setup 1	f	f	t	f	2017-04-25 09:00:00	f
11	2017-04-25 17:00:00	Tuesday Setup 2	f	f	t	f	2017-04-25 13:00:00	f
12	2017-04-27 08:00:00	Tuesday Night	t	f	t	t	2017-04-25 22:30:00	f
20	2017-04-26 13:00:00	Wednesday Setup 1	f	f	t	f	2017-04-26 09:00:00	f
21	2017-04-26 17:00:00	Wednesday Setup 2	f	f	t	f	2017-04-26 13:00:00	f
22	2017-04-27 08:00:00	Wednesday Night	t	f	t	t	2017-04-26 22:30:00	f
30	2017-04-27 13:00:00	Thursday Setup	f	f	t	t	2017-04-27 10:00:00	f
31	2017-04-27 20:00:00	Thursday Open 1	f	t	f	f	2017-04-27 16:00:00	f
32	2017-04-27 23:30:00	Thursday Open 2	f	t	f	f	2017-04-27 19:30:00	f
33	2017-04-28 08:00:00	Thursday Night	t	f	f	t	2017-04-27 23:00:00	f
39	2017-04-27 16:00:00	Thursday Trade Session	f	t	f	f	2017-04-27 13:00:00	f
40	2017-04-28 15:30:00	Friday Open 1	f	t	f	f	2017-04-28 10:00:00	f
41	2017-04-28 20:00:00	Friday Open 2	f	t	f	f	2017-04-28 15:00:00	f
42	2017-04-28 21:30:00	Friday Open 3	f	t	f	f	2017-04-28 19:30:00	f
43	2017-04-28 23:30:00	Friday Open 4	f	t	f	f	2017-04-28 21:00:00	f
44	2017-04-29 08:00:00	Friday Night	t	f	f	t	2017-04-28 23:00:00	f
50	2017-04-29 15:30:00	Saturday Open 1	f	t	f	f	2017-04-29 10:00:00	f
51	2017-04-29 20:00:00	Saturday Open 2	f	t	f	f	2017-04-29 15:00:00	f
52	2017-04-29 21:30:00	Saturday Open 3	f	t	f	f	2017-04-29 19:30:00	f
53	2017-04-29 23:30:00	Saturday Open 4	f	t	f	f	2017-04-29 21:00:00	f
54	2017-04-30 08:00:00	Saturday Night	t	f	f	t	2017-04-29 23:00:00	f
60	2017-04-30 16:30:00	Sunday Open 1	f	t	f	f	2017-04-30 11:00:00	f
61	2017-04-30 21:00:00	Sunday Open 2	f	t	f	f	2017-04-30 16:00:00	f
62	2017-04-30 22:30:00	Sunday Takedown	f	f	f	f	2017-04-30 20:30:00	t
63	2017-05-01 08:00:00	Sunday Night	t	f	f	t	2017-04-30 22:30:00	t
71	2017-05-01 13:00:00	Monday Takedown 1	f	f	f	f	2017-05-01 09:00:00	t
72	2017-05-01 17:00:00	Monday Takedown 2	f	f	f	f	2017-05-01 13:00:00	t
73	2017-05-01 21:00:00	Monday Takedown 3	f	f	f	f	2017-05-01 17:00:00	t
74	2017-05-02 08:00:00	Monday Night	t	f	f	t	2017-05-01 23:00:00	t
80	2017-05-02 17:00:00	Tuesday Takedown	f	f	f	f	2017-05-02 09:00:00	t
81	2017-05-03 17:00:00	Wednesday Takedown	f	f	f	f	2017-05-03 09:00:00	t
\.


--
-- Data for Name: volunteer; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.volunteer (id, callsign, camping, cellar, comment, confirmed, email, emailverified, firstaid, forename, forklift, instructions, managervouch, membership, other, password, picture, role, sia, surname, tshirt, uuid, verified) FROM stdin;
51	\N	f	f	\N	f		f	f		f	f	\N		f	\N	\N	\N	f		\N	\N	f
1951	\N	f	f	\N	f	nick.jerram@gmail.com	f	f	A	f	f	\N	\N	f	\N	\N	\N	f	A	\N	cb871fb3-6262-4669-a217-e8b6276c7e44	f
101	\N	f	f		f	\N	f	f	Fred	f	f	\N	\N	f	\N	\N		f	Flintstone	\N	\N	f
654	\N	f	f		f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Elliott	\N	\N	f
1751	\N	f	f		f	nick.jerram@googlemail.com	t	f	Nick	f	t		176340	f	\N	\N	\N	f	Jerram	\N	111	t
1801	\N	f	f	\N	f	a@a	f	f	a	f	f	\N	\N	f	\N	\N	\N	f	a	\N	e31ed14e-5988-41d6-821e-0107bee35fbe	f
724	\N	f	f		f	\N	f	f	Elvis	f	f	\N	\N	f	\N	\N		f	Evans	\N	\N	f
725	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Waterfall	\N	\N	f
726	\N	f	f		f	\N	f	f	Stuart	f	f	\N	\N	f	\N	\N		f	Evans	\N	\N	f
727	\N	f	f	Wish to work alongside Stuart Evans. 	f	\N	f	f	Lisa	f	f	\N	\N	f	\N	\N		f	Ruffell	\N	\N	f
728	\N	f	f	Past games volunteer	f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Smith	\N	\N	f
729	\N	f	f	Friday I will need to leave at about 6.30pm\n\\\nWork in Cash Hut	f	\N	f	f	Sandra	f	f	\N	\N	f	\N	\N		f	Gill	\N	\N	f
730	\N	f	f	Sorry to be picky, but I can work between 10.15 and 18.00 (could hang on if it's really busy or you're short-staffed). Hope this fits with what you need. If not, please let me know.	f	\N	f	f	Nick	f	f	\N	\N	f	\N	\N		f	Browne	\N	\N	f
731	\N	f	f		f	\N	f	f	Giles	f	f	\N	\N	f	\N	\N		f	Nuttall	\N	\N	f
732	\N	f	f		f	\N	f	f	Soffi	f	f	\N	\N	f	\N	\N		f	James	\N	\N	f
733	\N	f	f	Ideally with Graham May	f	\N	f	f	Philip	f	f	\N	\N	f	\N	\N		f	Rassell	\N	\N	f
734	\N	f	f	Work the same sessions as my wife member no 536223 (Ann Martin-Jones)	f	\N	f	f	Martin	f	f	\N	\N	f	\N	\N		f	Jones	\N	\N	f
735	\N	f	f	Work the same sessions as my husband - member no 536222 ( Martin Jones) 	f	\N	f	f	Ann	f	f	\N	\N	f	\N	\N		f	Martin Jones	\N	\N	f
736	\N	f	f	Can you please allocate me to back of house.\n\\\n\n\\\nI will be sorting cask ends and helping cover on entrance and some other bits for the few days I am on site.	f	\N	f	f	Adam	f	f	\N	\N	f	\N	\N		f	Gent	\N	\N	f
737	\N	f	f	Please can I work on the same bar as Evelyn Harrison-Bullock.	f	\N	f	f	Josh	f	f	\N	\N	f	\N	\N		f	Harrison-Bullock	\N	\N	f
738	\N	f	f		f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Rickson	\N	\N	f
740	\N	f	f	For the last few years I've worked on the wine bar, so I'm familiar with the products and procedures. If possible I would like to work on the wine bar again this year - thanks!	f	\N	f	f	Frances	f	f	\N	\N	f	\N	\N		f	McFadden	\N	\N	f
741	\N	f	f		f	\N	f	f	Neil	f	f	\N	\N	f	\N	\N		f	Lavington	\N	\N	f
742	\N	f	f		f	\N	f	f	grace	f	f	\N	\N	f	\N	\N		f	Bradbrook	\N	\N	f
743	\N	f	f	Would like to do a few hours on the Thursday evening, this would depend on what time I am able to finish work so you need to confirm.\n\\\nAlso a few hours on the Friday late morning / early afternoon.\n\\\nI'd like to work with Grace Bradbrook, or Graham or Sharon who are also volunteering. 	f	\N	f	f	lesley	f	f	\N	\N	f	\N	\N		f	buckley	\N	\N	f
744	\N	f	f		f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	Milne	\N	\N	f
745	\N	f	f		f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Hanson	\N	\N	f
746	\N	f	f		f	\N	f	f	Antony	f	f	\N	\N	f	\N	\N		f	Walbank	\N	\N	f
813	\N	f	f	Work same area/times as Eileen Bartley #324903\n\\\n	f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Robinson	\N	\N	f
814	\N	f	f		f	\N	f	f	Gregory	f	f	\N	\N	f	\N	\N		f	Cloney	\N	\N	f
815	\N	f	f		f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	McNally	\N	\N	f
853	\N	f	f	I have lots of cider bar experience being cider officer for North London, but am happy to work on beer bars including foreign beer.	f	\N	f	f	Jessica	f	f	\N	\N	f	\N	\N		f	Marsh	\N	\N	f
854	\N	f	f		f	\N	f	f	tim	f	f	\N	\N	f	\N	\N		f	burchell	\N	\N	f
855	\N	f	f	Sharing a caravan with Rod Sprigg - bar manager.  He is hopefully arriving Wednesday	f	\N	f	f	Hazel	f	f	\N	\N	f	\N	\N		f	Sprigg	\N	\N	f
856	\N	f	f		f	\N	f	f	Dipesh	f	f	\N	\N	f	\N	\N		f	Amin	\N	\N	f
857	\N	f	f		f	\N	f	f	Robert	f	f	\N	\N	f	\N	\N		f	Cruse	\N	\N	f
858	\N	f	f		f	\N	f	f	Alan	f	f	\N	\N	f	\N	\N		f	Harper	\N	\N	f
860	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Abramson	\N	\N	f
861	\N	f	f	partner of Ryan Shook. 	f	\N	f	f	natalie 	f	f	\N	\N	f	\N	\N		f	harrington	\N	\N	f
862	\N	f	f		f	\N	f	f	Ivan	f	f	\N	\N	f	\N	\N		f	Szrejder	\N	\N	f
609	\N	f	f		f	\N	f	f	Duncan	f	f	\N	\N	f	\N	\N		f	Ward	\N	\N	f
610	\N	f	f		f	\N	f	f	Roy	f	f	\N	\N	f	\N	\N	Beer Bar Manager	f	Garraway	\N	\N	f
611	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Kitney	\N	\N	f
612	\N	f	f	Hi Nick\n\\\n\n\\\nI will be doing the Friday night of set up until the night team arrives as per usual and then joining the site team for the Saturday of set up. A T shirt would be nice this year as last year I did set up and later in the week and received none.\n\\\n\n\\\nI've put down for the Monday of set up so that the form becomes active. I will not be around on the Monday of Set up.\n\\\n\n\\\nCheers James\n\\\n	f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	Shipp	\N	\N	f
613	\N	f	f	If you get short staffed I may be able to do more hours than I have marked.\n\\\nI have worked your last 3 beer fests so I should be in your volunteer records.  	f	\N	f	f	Lee	f	f	\N	\N	f	\N	\N		f	Keates	\N	\N	f
614	\N	f	f	I may be in at 11.00 on the Friday morning.	f	\N	f	f	Alan	f	f	\N	\N	f	\N	\N		f	Hawkins	\N	\N	f
615	\N	f	f		f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Berry	\N	\N	f
616	\N	f	f		f	\N	f	f	Anthony	f	f	\N	\N	f	\N	\N		f	Collins	\N	\N	f
617	\N	f	f	I have said I will do Monday 1st on takedown, this is going to be suject to weather I have to work or not as it's a Bank Holiday I won't know until the week before.	f	\N	f	f	Trevor	f	f	\N	\N	f	\N	\N		f	Pemberton	\N	\N	f
618	\N	f	f		f	\N	f	f	Lyndon	f	f	\N	\N	f	\N	\N		f	Sharpe	\N	\N	f
619	\N	f	f		f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Jarvis	\N	\N	f
620	\N	f	f		f	\N	f	f	Barry	f	f	\N	\N	f	\N	\N		f	Webb	\N	\N	f
621	\N	f	f		f	\N	f	f	Greg	f	f	\N	\N	f	\N	\N		f	Davies	\N	\N	f
622	\N	f	f	I may want a camping space and if I do will work more sessions but I will know by the end of this month	f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Mcgrath	\N	\N	f
623	\N	f	f		f	\N	f	f	Tracy	f	f	\N	\N	f	\N	\N		f	Tester	\N	\N	f
624	\N	f	f	I'm part of the games team that's worked on there for many years so please can I get put there again.	f	\N	f	f	Tes	f	f	\N	\N	f	\N	\N		f	Matthews	\N	\N	f
625	\N	f	f		f	\N	f	f	Kirk	f	f	\N	\N	f	\N	\N		f	Winkler	\N	\N	f
626	\N	f	f	I will be on site from around 11 am. to 5 pm. to oversee the finals of the National Cider and Perry Championships.	f	\N	f	f	Andrea	f	f	\N	\N	f	\N	\N		f	Briers	\N	\N	f
627	\N	f	f	Kennet Morris. Serving in kit. With the rest of the side if possible. 	f	\N	f	f	Glenn	f	f	\N	\N	f	\N	\N		f	Barrett	\N	\N	f
628	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	McGarvey	\N	\N	f
629	\N	f	f		f	\N	f	f	Eugene	f	f	\N	\N	f	\N	\N		f	McSorley	\N	\N	f
630	\N	f	f	Will be with the Kennet Morris Men for their Sessions .	f	\N	f	f	Max	f	f	\N	\N	f	\N	\N		f	Beare	\N	\N	f
631	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Maiden	\N	\N	f
632	\N	f	f		f	\N	f	f	Jennifer	f	f	\N	\N	f	\N	\N	Deputy Games Manager	f	Farley	\N	\N	f
633	\N	f	f	Normally I work on the Foreign Beer Bar clearing empties,  replenishing shelves and generally keeping the back area tidy as well as working as a relief on Camra Products and Glasses.\n\\\n	f	\N	f	f	Laurence	f	f	\N	\N	f	\N	\N		f	Hansford	\N	\N	f
634	\N	f	f		f	\N	f	f	Iain	f	f	\N	\N	f	\N	\N		f	Cresswell	\N	\N	f
635	\N	f	f	Hoping to man one of the bars in kit with the Kennet Morris Men.	f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Tunnicliffe	\N	\N	f
636	\N	f	f	Can you put me with the lovely Miss Kate (Hannah) Martin? Thanks!	f	\N	f	f	Caroline	f	f	\N	\N	f	\N	\N		f	Middlehurst	\N	\N	f
637	\N	f	f		f	\N	f	f	Christopher	f	f	\N	\N	f	\N	\N		f	Hinton	\N	\N	f
638	\N	f	f	Work with Caroline Middlehurst for the Friday and Saturday shifts please	f	\N	f	f	Kate	f	f	\N	\N	f	\N	\N		f	Martin	\N	\N	f
640	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Abramson	\N	\N	f
641	\N	f	f		f	\N	f	f	Malcolm	f	f	\N	\N	f	\N	\N		f	Graham	\N	\N	f
642	\N	f	f	Deputy Bar Manager on Foreign Bar	f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N	Deputy Foreign Bar Manager	f	Jackson	\N	\N	f
643	\N	f	f		f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	Moore	\N	\N	f
644	\N	f	f		f	\N	f	f	Roger	f	f	\N	\N	f	\N	\N		f	Butland	\N	\N	f
645	\N	f	f		f	\N	f	f	Neil	f	f	\N	\N	f	\N	\N		f	Munkman	\N	\N	f
646	\N	f	f		f	\N	f	f	Michael	f	f	\N	\N	f	\N	\N		f	Oliver	\N	\N	f
647	\N	f	f	Might be around for more sessions if needed but won't know until closer to the time	f	\N	f	f	Dom	f	f	\N	\N	f	\N	\N		f	Humphries	\N	\N	f
648	\N	f	f	I am cellar manager this year	f	\N	f	f	Timothy	f	f	\N	\N	f	\N	\N	Cellar Manager	f	Lloyd	\N	\N	f
649	\N	f	f	Deputy Entrance :-)	f	\N	f	f	Oliver	f	f	\N	\N	f	\N	\N	Deputy Entrance Manager	f	Seaman	\N	\N	f
650	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Young	\N	\N	f
651	\N	f	f	I am happy to assist Brian Jones in the Beer Tasting panels.\n\\\nI am happy to work on Keith Jordan's bar and be Deputy Bar Manager for him.	f	\N	f	f	Bob	f	f	\N	\N	f	\N	\N	Deputy Beer Bar Manager	f	Smith	\N	\N	f
652	\N	f	f		f	\N	f	f	Bob	f	f	\N	\N	f	\N	\N		f	Brodie	\N	\N	f
603	\N	f	f		f	\N	f	f	Robert	f	f	\N	\N	f	\N	\N		f	Hussey	\N	\N	f
653	\N	f	f	Monday to Wednesday on cellar team\n\\\nThursday to Sunday as bar manager	f	\N	f	f	Ian	f	f	\N	\N	f	\N	\N	Deputy Beer Bar Manager	f	Davey	\N	\N	f
655	\N	f	f		f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Zajac	\N	\N	f
656	\N	f	f		f	\N	f	f	Claudia	f	f	\N	\N	f	\N	\N		f	Wittkowske	\N	\N	f
657	\N	f	f		f	\N	f	f	Matthew	f	f	\N	\N	f	\N	\N		f	Little	\N	\N	f
658	\N	f	f	I'd like to work with Jamie Ryan please.	f	\N	f	f	Laura	f	f	\N	\N	f	\N	\N		f	Jackaman	\N	\N	f
659	\N	f	f	I'll be assisting Brian Jones et al with fetching and carrying for the Thursday afternoon ales judging.	f	\N	f	f	Peter	f	f	\N	\N	f	\N	\N		f	de Courcy	\N	\N	f
660	\N	f	f	I would like to work with Laura Jackaman please.	f	\N	f	f	Jamie	f	f	\N	\N	f	\N	\N		f	Ryan	\N	\N	f
661	\N	f	f	I may be available for other session but will not know until nearer Festival.	f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Lawton	\N	\N	f
662	\N	f	f		f	\N	f	f	Judy	f	f	\N	\N	f	\N	\N		f	Beale	\N	\N	f
663	\N	f	f	Working in the cellar team as in previous years.	f	\N	f	f	Derek	f	f	\N	\N	f	\N	\N		f	Jones	\N	\N	f
664	\N	f	f	I would like to please work in the beer bar with my son Oliver Billing.	f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Billing	\N	\N	f
665	\N	f	f	Will be working with the cellar team. I might be able to work Saturday, need to check if I'm due to be going to football or not.	f	\N	f	f	Adrian	f	f	\N	\N	f	\N	\N		f	Bean	\N	\N	f
672	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Cresswell	\N	\N	f
673	\N	f	f	Nick,\n\\\nDue to the fact that I am on crutches this year - following an operation in December - I am restricted in what I can do.  I have spoken with Dave and agreed that I will provide support where possible, with the primary task being the set-up and support for the office, along with other tasks that I can achieve.\n\\\n\n\\\nThanking you\n\\\n\n\\\nArfs	f	\N	f	f	Arthur	f	f	\N	\N	f	\N	\N		f	Pounder	\N	\N	f
674	\N	f	f	Kennet Morris man\n\\\nDancing Saturday day\n\\\nBeer judging Thursday day\n\\\nTraffic or barrel shifting\n\\\nMonday to Thursday lunchtime	f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Bracegirdle	\N	\N	f
675	\N	f	f		f	\N	f	f	Douglas	f	f	\N	\N	f	\N	\N		f	Cross	\N	\N	f
676	\N	f	f		f	\N	f	f	Wayne	f	f	\N	\N	f	\N	\N		f	Steel	\N	\N	f
678	\N	f	f	Monday and weds day to help with staff catering	f	\N	f	f	SUSAN	f	f	\N	\N	f	\N	\N		f	TAYLOR	\N	\N	f
679	\N	f	f	Part of Site Team. probably about for 22nd & 23rd as well	f	\N	f	f	Ron	f	f	\N	\N	f	\N	\N		f	Haskins	\N	\N	f
680	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Thompson	\N	\N	f
681	\N	f	f		f	\N	f	f	Patrick	f	f	\N	\N	f	\N	\N		f	Grant	\N	\N	f
684	\N	f	f	On days where I've volunteered for late sessions I'll need to leave at 22:45 in order to catch last bus home.	f	\N	f	f	Laura	f	f	\N	\N	f	\N	\N		f	Bilbe	\N	\N	f
685	\N	f	f		f	\N	f	f	Charles	f	f	\N	\N	f	\N	\N		f	Brinley Codd	\N	\N	f
778	\N	f	f		f	\N	f	f	Hayley	f	f	\N	\N	f	\N	\N		f	Price	\N	\N	f
775	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Johnson	\N	\N	f
574	\N	f	f	Can I be in the same area as David Hassell and Rachel  Siertsema\n\\\n\n\\\nThis is the third time I have submitted this form. I tried on 5th and 18th April and\n\\\nhave not heard back if I have been accepted or not. I also sent a direct email on 19th April. Both David and Rachel mentioned above got an accetance email on the 14th. I did get an automated reply when submitting the form from staffing@readingbeerfestival.org.uk so I don't think it is a spam filter issue, if I auctually have been sent an accetance email.\n\\\n\n\\\nAn alternative email is sswilson9876@gmail.com but I can't change this in the form above.\n\\\n\n\\\nCould you let me know ASAP if I've been accepted?	f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Wilson	\N	\N	f
577	\N	f	f		f	\N	f	f	Andy	f	f	\N	\N	f	\N	\N		f	Pinkard	\N	\N	f
578	\N	f	f		f	\N	f	f	Elspeth	f	f	\N	\N	f	\N	\N		f	Brady	\N	\N	f
579	\N	f	f		f	\N	f	f	Richard	f	f	\N	\N	f	\N	\N		f	Russell	\N	\N	f
580	\N	f	f	Request to work with Will Burchell #288853	f	\N	f	f	Mike	f	f	\N	\N	f	\N	\N		f	Garner	\N	\N	f
581	\N	f	f	Same area as Ashish Naik	f	\N	f	f	Aaron	f	f	\N	\N	f	\N	\N		f	Edgcumbe	\N	\N	f
582	\N	f	f		f	\N	f	f	Alex	f	f	\N	\N	f	\N	\N		f	Harkness	\N	\N	f
583	\N	f	f		f	\N	f	f	Nicky	f	f	\N	\N	f	\N	\N		f	Rhoods	\N	\N	f
584	\N	f	f		f	\N	f	f	Kenneth	f	f	\N	\N	f	\N	\N		f	Baker	\N	\N	f
585	\N	f	f	I am aiming to arrive on Wednesday 19th and stay to the end of takedown. I will be bringing my office equipment in my van and will need a parking space for it on site. I have agreed to be the radio manager again but will be able to help out on other jobs throughout setup and takedown.	f	\N	f	f	David	f	f	\N	\N	f	\N	\N	Radio Manager	f	Jones	\N	\N	f
586	\N	f	f		f	\N	f	f	Jennie	f	f	\N	\N	f	\N	\N		f	Walker	\N	\N	f
587	\N	f	f		f	\N	f	f	Kathryn	f	f	\N	\N	f	\N	\N		f	Jarvis	\N	\N	f
588	\N	f	f	Takedown Tuesday 2 May only able to work untill 13.00. Prefer to work with AJD Wheatcroft	f	\N	f	f	Cleo	f	f	\N	\N	f	\N	\N		f	Wheatcroft	\N	\N	f
589	\N	f	f	Only able to work up to 13.00 on Tuesday 02 May. Prefer to work with C. Wheatcroft	f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Wheatcroft	\N	\N	f
590	\N	f	f		f	\N	f	f	Katy	f	f	\N	\N	f	\N	\N		f	Aldridge	\N	\N	f
591	\N	f	f		f	\N	f	f	Sarah	f	f	\N	\N	f	\N	\N		f	Rigby	\N	\N	f
669	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N	Foreign Beer Orderer	f	Thirlaway	\N	\N	f
881	\N	f	f	I'm helping Chris Rouse with a tasting panel for Cider competition, as I'm about I am happy to be put to work once my business is done.	f	\N	f	f	Paul	f	f	\N	\N	f	\N	\N		f	Sanders	\N	\N	f
882	\N	f	f	Work with Matthew Shewring if possible please	f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Cook	\N	\N	f
884	\N	f	f		f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Cooley	\N	\N	f
869	\N	f	f		f	\N	f	f	Samuel	f	f	\N	\N	f	\N	\N		f	Catterall-Young	\N	\N	f
593	\N	f	f	My husband will volunteer on the same session , can we work the same bar - his CAMRA number is 277293	f	\N	f	f	Christine	f	f	\N	\N	f	\N	\N		f	Watson	\N	\N	f
594	\N	f	f	Thought I could make it, unfortunately it turns out I have an exam that week and so cannot. Please don't allocate. Sorry	f	\N	f	f	Alexei	f	f	\N	\N	f	\N	\N		f	Samarenko	\N	\N	f
596	\N	f	f	 I would be best suited on entrance and glasses. Cannot do any heavy lifting. 	f	\N	f	f	Rachel	f	f	\N	\N	f	\N	\N		f	Bulcock	\N	\N	f
597	\N	f	f		f	\N	f	f	Jim	f	f	\N	\N	f	\N	\N		f	Kullander	\N	\N	f
598	\N	f	f		f	\N	f	f	Anne	f	f	\N	\N	f	\N	\N		f	Thomas	\N	\N	f
599	\N	f	f	If possible could I have a 3XL T-shirt if available if not XXL will do but could be a little tight.	f	\N	f	f	Nicholas	f	f	\N	\N	f	\N	\N		f	Cornish	\N	\N	f
600	\N	f	f	I volunteered last year and find that i'm best suited behind the bar, happy to help on the camera stand as well :)\n\\\nForeign beer is my speciality knowledge.	f	\N	f	f	Zoe	f	f	\N	\N	f	\N	\N		f	Andrews	\N	\N	f
601	\N	f	f		f	\N	f	f	Mitch	f	f	\N	\N	f	\N	\N		f	Bateman 	\N	\N	f
602	\N	f	f		f	\N	f	f	Katy	f	f	\N	\N	f	\N	\N		f	Turgoose	\N	\N	f
682	\N	f	f		f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Bumstead	\N	\N	f
604	\N	f	f	Applying for stewards but after previous conversation with the CS I would like to continue training and working in the radio control room. 	f	\N	f	f	Rhea	f	f	\N	\N	f	\N	\N		f	Sloman	\N	\N	f
605	\N	f	f		f	\N	f	f	Greg	f	f	\N	\N	f	\N	\N		f	Clarke	\N	\N	f
606	\N	f	f		f	\N	f	f	Graham	f	f	\N	\N	f	\N	\N		f	Bradbrook	\N	\N	f
607	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Maiden	\N	\N	f
608	\N	f	f		f	\N	f	f	Tony	f	f	\N	\N	f	\N	\N		f	Girling	\N	\N	f
666	\N	f	f		f	\N	f	f	Tom	f	f	\N	\N	f	\N	\N		f	Hughes	\N	\N	f
667	\N	f	f		f	\N	f	f	Ben	f	f	\N	\N	f	\N	\N		f	Skidmore	\N	\N	f
748	\N	f	f	I have helped in setup + cellar team) at ascot festival, but have no qualifications! I am available during the day w/c 23/4 and possibly week after. But not every day! Could fit in if u have particular need? Definitely no evenings until Saturday 29th if that makes sense.	f	\N	f	f	Peter	f	f	\N	\N	f	\N	\N		f	Lucey	\N	\N	f
749	\N	f	f	Work with Kirk Winkler	f	\N	f	f	Cheryl	f	f	\N	\N	f	\N	\N		f	Irwin	\N	\N	f
750	\N	f	f	Bar manager (Bar D?)\n\\\nWill work set up Wednesday if necessary, but would prefer a day off between working set up and bar manager, if possible.	f	\N	f	f	Keith	f	f	\N	\N	f	\N	\N	Beer Bar Manager	f	Jordan	\N	\N	f
751	\N	f	f	Would like time off on Thursday evening to join the quiz.	f	\N	f	f	Sheila	f	f	\N	\N	f	\N	\N		f	Jordan	\N	\N	f
752	\N	f	f		f	\N	f	f	Thomas	f	f	\N	\N	f	\N	\N		f	Burke	\N	\N	f
753	\N	f	f	Cider Bar Manager\n\\\nWhat setup sessions I do depends on when the cider deliveries are.\n\\\nI will be there Saturday 22 Apr for cider stillage and bar construction (unless it's a different day).	f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N	Cider Bar Manager	f	Rouse	\N	\N	f
754	\N	f	f	All timings approximate as dependent on what shift I get at work on the day.	f	\N	f	f	Michael	f	f	\N	\N	f	\N	\N		f	Dunstan	\N	\N	f
755	\N	f	f		f	\N	f	f	Helen	f	f	\N	\N	f	\N	\N		f	Toomey	\N	\N	f
756	\N	f	f	Would like to work with James Elsmore	f	\N	f	f	Tim	f	f	\N	\N	f	\N	\N		f	Freeman	\N	\N	f
757	\N	f	f		f	\N	f	f	Ian	f	f	\N	\N	f	\N	\N		f	Tanner	\N	\N	f
758	\N	f	f		f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Surridge	\N	\N	f
759	\N	f	f	I have worked on set-up over the last few years and I have decided to do it again this year. I have also been on the Cellar Team at Ascot Racecourse Beer Festival for the last few years also. I may do set-up on Wednesday, but will see how Monday and Tuesday go.	f	\N	f	f	Clive	f	f	\N	\N	f	\N	\N		f	Doran	\N	\N	f
760	\N	f	f	I need to leave at 2300 to catch the last train home.	f	\N	f	f	Norman	f	f	\N	\N	f	\N	\N		f	Sutton	\N	\N	f
761	\N	f	f		f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Harper	\N	\N	f
762	\N	f	f		f	\N	f	f	Jonathan	f	f	\N	\N	f	\N	\N		f	Inglis	\N	\N	f
763	\N	f	f	Working in the treasury.	f	\N	f	f	Phillip	f	f	\N	\N	f	\N	\N		f	Gill	\N	\N	f
764	\N	f	f		f	\N	f	f	Heather	f	f	\N	\N	f	\N	\N		f	Lawn	\N	\N	f
765	\N	f	f	Would prefer to work with member 466192 but not essential.	f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Ray	\N	\N	f
767	\N	f	f	hi would like to work with simon crisp. if not possible then at least the same shift pattern. thanks.	f	\N	f	f	marcus	f	f	\N	\N	f	\N	\N		f	jones	\N	\N	f
768	\N	f	f		f	\N	f	f	becki	f	f	\N	\N	f	\N	\N		f	stringer	\N	\N	f
769	\N	f	f	would be great to work with laura jones. would need to be the same shifts as laura jones, Simon Crisp and Marcus Jones.	f	\N	f	f	becki	f	f	\N	\N	f	\N	\N		f	stringer	\N	\N	f
770	\N	f	f		f	\N	f	f	Ian	f	f	\N	\N	f	\N	\N		f	Johnson	\N	\N	f
771	\N	f	f		f	\N	f	f	Anthony	f	f	\N	\N	f	\N	\N		f	Springall	\N	\N	f
772	\N	f	f	I won't be able to get there until 11am  & can only work until 21:00	f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Ostrowski	\N	\N	f
773	\N	f	f		f	\N	f	f	robert 	f	f	\N	\N	f	\N	\N		f	ellis	\N	\N	f
774	\N	f	f	Radio log please	f	\N	f	f	Laura	f	f	\N	\N	f	\N	\N		f	Dunn	\N	\N	f
776	\N	f	f	I will be coming with Andy Johnson who is one of the cider bar managers under Chris Rouse. 	f	\N	f	f	Sandy	f	f	\N	\N	f	\N	\N		f	Osman	\N	\N	f
777	\N	f	f		f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Mcgrath	\N	\N	f
779	\N	f	f		f	\N	f	f	Paul	f	f	\N	\N	f	\N	\N		f	Gerrard	\N	\N	f
780	\N	f	f	Deputy bar manager, aka uncle Roy's carer.	f	\N	f	f	Roger	f	f	\N	\N	f	\N	\N	Deputy Beer Bar Manager	f	Brown	\N	\N	f
781	\N	f	f		f	\N	f	f	Nicholas	f	f	\N	\N	f	\N	\N		f	Wooldridge	\N	\N	f
782	\N	f	f	I'll be doing the beer judging again this year on Thursday (presume that counts as 'behind the scenes'). Other times I've said to James Moore I'll help him out on membership, seeing as I've run it before!	f	\N	f	f	Quinten	f	f	\N	\N	f	\N	\N		f	Taylor	\N	\N	f
783	\N	f	f	My wife, Christine Watson, (CAMRA 277294) has already volunteered & will also be working on the bar during my shifts.  Because of the restraining order, could you please ensure we don't come within 1000 metres of each other. &#128514; ( Just kidding. We love working bar together if possible !)	f	\N	f	f	Roger	f	f	\N	\N	f	\N	\N		f	Watson	\N	\N	f
784	\N	f	f		f	\N	f	f	Ian	f	f	\N	\N	f	\N	\N		f	Read	\N	\N	f
785	\N	f	f		f	\N	f	f	Teresa	f	f	\N	\N	f	\N	\N		f	Read	\N	\N	f
786	\N	f	f	I am manager of the games department.	f	\N	f	f	NICK	f	f	\N	\N	f	\N	\N		f	HAMES	\N	\N	f
787	\N	f	f		f	\N	f	f	ewfd	f	f	\N	\N	f	\N	\N		f	sedvf	\N	\N	f
788	\N	f	f	I'd like to work in the same area as Rachel Siertsema and Simon Wilson. Thanks.	f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Hassell	\N	\N	f
789	\N	f	f	Please could I work with David Hassell & Simon Wilson, thanks.	f	\N	f	f	Rachel	f	f	\N	\N	f	\N	\N		f	Siertsema	\N	\N	f
790	\N	f	f	Have completed the CAMRA bar managers course.  \n\\\nWorked as bar manager at Hitchin, Maidenhead and Ascot Festivals.\n\\\nWorked as Foreign beer bar manager at Hitchin - know far too much about Belgian Beers and spent last two summer holidays in Munich and Bamberg - so can now bore about German beer!! 	f	\N	f	f	Paul	f	f	\N	\N	f	\N	\N		f	Beardsley	\N	\N	f
850	\N	f	f	I'm coming to help Tanya in the kitchen	f	\N	f	f	Jenni	f	f	\N	\N	f	\N	\N		f	Leask	\N	\N	f
851	\N	f	f	I'm vouched for by Nick Hames	f	\N	f	f	Chip	f	f	\N	\N	f	\N	\N		f	Charlesworth	\N	\N	f
852	\N	f	f	If I could work in the same area as James Richardson and/or Roger Brown, that would be great.	f	\N	f	f	Martin	f	f	\N	\N	f	\N	\N		f	McCloud	\N	\N	f
791	\N	f	f	I heard there is a Cider Judging event. I would love to be a judge if that is possible! I am a very keen/quite discerning cider drinker. There are some great ciders made locally to this area so I would enjoy this immensely!\n\\\n\n\\\nI work at JMTC with Edd Bilbe who told me about this event and has volunteered for a few years I believe- it would be nice to work with him but it would be even nicer to be with cider. \n\\\n\n\\\n	f	\N	f	f	Emily	f	f	\N	\N	f	\N	\N		f	Brooke	\N	\N	f
792	\N	f	f		f	\N	f	f	Neil	f	f	\N	\N	f	\N	\N		f	Rippon	\N	\N	f
793	\N	f	f		f	\N	f	f	Barry	f	f	\N	\N	f	\N	\N		f	Garber	\N	\N	f
794	\N	f	f		f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Goodall	\N	\N	f
795	\N	f	f		f	\N	f	f	Tanya	f	f	\N	\N	f	\N	\N		f	Godfrey	\N	\N	f
796	\N	f	f	Assisting with Beer Judging	f	\N	f	f	Rachael	f	f	\N	\N	f	\N	\N		f	Wall	\N	\N	f
797	\N	f	f		f	\N	f	f	Tony	f	f	\N	\N	f	\N	\N		f	Dickenson	\N	\N	f
798	\N	f	f	Would like to work in same area as Janine Shlackman (Member No. 530072).\n\\\n\n\\\nWe are new members and this is our first time volunteering so may need showing the ropes!	f	\N	f	f	Paul	f	f	\N	\N	f	\N	\N		f	Shlackman	\N	\N	f
799	\N	f	f	Would like to work in same area as Paul Shlackman (Member No. 530071).\n\\\n\n\\\nWe are new members and this is our first time volunteering so may need showing the ropes!	f	\N	f	f	Janine	f	f	\N	\N	f	\N	\N		f	Shlackman	\N	\N	f
800	\N	f	f		f	\N	f	f	Maurice	f	f	\N	\N	f	\N	\N		f	Quirke	\N	\N	f
801	\N	f	f	Kennet Morris Men 	f	\N	f	f	Tony	f	f	\N	\N	f	\N	\N		f	Bartlett	\N	\N	f
802	\N	f	f		f	\N	f	f	PETER	f	f	\N	\N	f	\N	\N		f	FREWIN	\N	\N	f
803	\N	f	f		f	\N	f	f	Stephen	f	f	\N	\N	f	\N	\N		f	Bates	\N	\N	f
804	\N	f	f		f	\N	f	f	Jonathan	f	f	\N	\N	f	\N	\N		f	Lacey	\N	\N	f
868	\N	f	f	Please don't assign me to any areas as I'm already on site team. I'll be on site every day from Sat 22nd Apr  to Tues 2nd May.	f	\N	f	f	Naomi	f	f	\N	\N	f	\N	\N	General Open Manager	f	Withey	\N	\N	f
870	\N	f	f	Site Team	f	\N	f	f	Gareth	f	f	\N	\N	f	\N	\N		f	Llewellyn	\N	\N	f
805	\N	f	f	Could I work alongside Martin McCloud please.  We are good friends of Roger Brown, one of the bar managers and we always try to work with Roger.\n\\\nI am also trying to persuade a good friend, previous (good) volunteer, to assist at the same session (his name is Marc Ingram).\n\\\nAlso\n\\\nIf I can, I will try to arrive a little early - but I have to attend a birthday party in Reading Town Hall that afternoon so I cannot promise.  If I can, I will turn up as early a possible and sign in.\n\\\nMany thanks!	f	\N	f	f	Pascal	f	f	\N	\N	f	\N	\N		f	Richardson	\N	\N	f
807	\N	f	f		f	\N	f	f	Mitchell	f	f	\N	\N	f	\N	\N		f	Walmsley	\N	\N	f
808	\N	f	f	I know sign language (BSL)	f	\N	f	f	Lee	f	f	\N	\N	f	\N	\N		f	Dalkin	\N	\N	f
809	\N	f	f		f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Hale	\N	\N	f
810	\N	f	f	Please could I work on the same bar as Josh Harrison-Bullock. 	f	\N	f	f	Evelyn	f	f	\N	\N	f	\N	\N		f	Harrison-Bullock	\N	\N	f
811	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Whitby	\N	\N	f
812	\N	f	f		f	\N	f	f	Richard	f	f	\N	\N	f	\N	\N		f	Croton	\N	\N	f
816	\N	f	f		f	\N	f	f	Timothy	f	f	\N	\N	f	\N	\N		f	Thomas	\N	\N	f
817	\N	f	f		f	\N	f	f	Felix	f	f	\N	\N	f	\N	\N		f	Iliff	\N	\N	f
818	\N	f	f	I would like to work in the same area as Felix Iliff if possible please. 	f	\N	f	f	Elspeth	f	f	\N	\N	f	\N	\N		f	Iliff	\N	\N	f
819	\N	f	f	I would like to work in the same area as Phil Rassell if possible.	f	\N	f	f	Tom	f	f	\N	\N	f	\N	\N		f	Gamble	\N	\N	f
820	\N	f	f	Apologies, for late entry I have only just found out about a meeting.\n\\\nPlease can I work with Steve Leyfield as usual.\n\\\nI have to go to Birmingham on Saturday for a meeting I will do as much as I can around this.\n\\\nThanks	f	\N	f	f	Angela	f	f	\N	\N	f	\N	\N		f	Aspin	\N	\N	f
821	\N	f	f	As discussed in the first meeting I have been put forward as Deputy Bar Manager by Tim Lloyd	f	\N	f	f	Luke	f	f	\N	\N	f	\N	\N	Deputy Beer Bar Manager	f	Ambrose	\N	\N	f
822	\N	f	f	Hi,\n\\\nPrefer the Pre-paid entrance as last year.\n\\\nI prefer my surname not to be displaydd on my badge.\n\\\nLast year we put a small dot to partially cover it, that is also fine as it is easier to mange.\n\\\nReason: Having a unique name in UK, prefer strangers not to Google it!	f	\N	f	f	Parkash	f	f	\N	\N	f	\N	\N		f	Mankoo	\N	\N	f
823	\N	f	f	### NOTE:  I have a first aid course booked for Monday 24th.  I've never failed one, so put me down as qualified.\n\\\n[Course name:  Emergency first aid at work (previously 'Appointed Persons')]\n\\\n\n\\\nTim Thomas says he's working Thu/Fri afternoon sessions, so if I could be on the same bar, great, if not, no worries.	f	\N	f	f	Steven	f	f	\N	\N	f	\N	\N		f	Kelly	\N	\N	f
824	\N	f	f	NB I have also been invited to be a Beer judge on the Thursday p.m. \n\\\n\n\\\nI will probably work part sessions of the two I have indicated e.g. 11.30 till 6. 	f	\N	f	f	John 	f	f	\N	\N	f	\N	\N		f	Dearing	\N	\N	f
825	\N	f	f		f	\N	f	f	Michael	f	f	\N	\N	f	\N	\N		f	Leigh	\N	\N	f
826	\N	f	f	Working for Brian Jones for Beer Judging only	f	\N	f	f	Judy	f	f	\N	\N	f	\N	\N		f	Jones	\N	\N	f
827	\N	f	f	Also, may be available for part of the Saturday daytime sessions if required	f	\N	f	f	Allan	f	f	\N	\N	f	\N	\N		f	Conner	\N	\N	f
828	\N	f	f	I will probably also be able to volunteer on Saturday lunch time if requred.	f	\N	f	f	Melissa	f	f	\N	\N	f	\N	\N		f	Reed	\N	\N	f
829	\N	f	f		f	\N	f	f	Steve	f	f	\N	\N	f	\N	\N		f	Heming	\N	\N	f
830	\N	f	f		f	\N	f	f	Tim	f	f	\N	\N	f	\N	\N		f	Ross	\N	\N	f
831	\N	f	f	Parking required for Van. Will be using small tent. I may be able to work on a little later on Monday, it depends upon what my job requires of me on Tuesday. Previously worked GBBF (Set up/Take down Elec, Volly Arms set up and working, Glasses), NWAF (Derby (Bar) & Norwich (CWBOB Bar Technical)), Great Welsh (Various), W.Dorset October Fest (Org, Set up, all festival jobs, Clear up), plus others.	f	\N	f	f	Jez	f	f	\N	\N	f	\N	\N		f	Armitage	\N	\N	f
832	\N	f	f	I may be able to help on thursday during tge day but unlikely 	f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Marsh	\N	\N	f
833	\N	f	f		f	\N	f	f	Karen	f	f	\N	\N	f	\N	\N		f	Garber	\N	\N	f
834	\N	f	f	Work with Chris Robinson - member 324902 (already registered)	f	\N	f	f	Eileen	f	f	\N	\N	f	\N	\N		f	Bartley	\N	\N	f
835	\N	f	f	I am going to be judging two of the Championship cider & perry rounds which I am informed start at 12:30 ish. I should be available after a short break which should tie in with the 15:00 session start I hope. 	f	\N	f	f	Linda	f	f	\N	\N	f	\N	\N		f	Thompson	\N	\N	f
836	\N	f	f		f	\N	f	f	Tanya	f	f	\N	\N	f	\N	\N		f	Kynaston	\N	\N	f
837	\N	f	f	I'd like to work in the same beer bar area as Margaret King 121836	f	\N	f	f	Ian	f	f	\N	\N	f	\N	\N		f	King	\N	\N	f
838	\N	f	f	I would like to work with my dad Richard Hand who is doing the 3pm-8pm session 	f	\N	f	f	Daniel	f	f	\N	\N	f	\N	\N		f	Hand	\N	\N	f
839	\N	f	f	Hi. I have lost my card and don't know my membership number. I have put Nick as a contact but he doesn't know me except via email a few times last year. 	f	\N	f	f	Matthew	f	f	\N	\N	f	\N	\N		f	Shewring	\N	\N	f
840	\N	f	f		f	\N	f	f	Dean	f	f	\N	\N	f	\N	\N		f	Cornwall	\N	\N	f
841	\N	f	f	Required by John & Sue Thirlaway to assist with Foreign Beer Deliveries on Tues PM of Setup.	f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Grist	\N	\N	f
842	\N	f	f		f	\N	f	f	Rodney	f	f	\N	\N	f	\N	\N		f	Sprigg	\N	\N	f
843	\N	f	f		f	\N	f	f	Rodney	f	f	\N	\N	f	\N	\N	Beer Bar Manager	f	Sprigg	\N	\N	f
844	\N	f	f		f	\N	f	f	Sophie	f	f	\N	\N	f	\N	\N		f	Neal	\N	\N	f
845	\N	f	f		f	\N	f	f	Nathan	f	f	\N	\N	f	\N	\N		f	Heywood	\N	\N	f
846	\N	f	f		f	\N	f	f	Connor	f	f	\N	\N	f	\N	\N		f	Froude	\N	\N	f
847	\N	f	f	Ewan Tolladay is the person whom put me onto volunteering for the beer festival and i'd really love to help out :) 	f	\N	f	f	Robert Mathue James Phillip	f	f	\N	\N	f	\N	\N		f	Hart	\N	\N	f
848	\N	f	f		f	\N	f	f	Timothy	f	f	\N	\N	f	\N	\N		f	Harden	\N	\N	f
849	\N	f	f		f	\N	f	f	Angela	f	f	\N	\N	f	\N	\N		f	Jones	\N	\N	f
668	\N	f	f		f	\N	f	f	Sue	f	f	\N	\N	f	\N	\N	Foreign Bar Manager	f	Thirlaway	\N	\N	f
677	\N	f	f		f	\N	f	f	A	f	f	\N	\N	f	\N	\N		f	A	\N	\N	f
151	\N	f	f		f	\N	f	f	Flash	f	f	\N	\N	f	\N	\N		f	Gordon	\N	\N	f
501	\N	f	f		f	\N	f	f	Nick	f	f	\N	\N	f	\N	\N	Staffing Manager	f	Jerram	\N	\N	f
502	\N	f	f		f	\N	f	f	Joy	f	f	\N	\N	f	\N	\N	Administration	f	Jerram	\N	\N	f
503	\N	f	f	Festival publicity.	f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N	Publicity Manager	f	Johnston	\N	\N	f
504	\N	f	f	Site Team\n\\\n	f	\N	f	f	Katrina	f	f	\N	\N	f	\N	\N	Setup Coordinator	f	Fletcher	\N	\N	f
505	\N	f	f	Please dont allocate me a specific tak for any of the sessions. I will be ther most of the time as Organiser and DPS	f	\N	f	f	David	f	f	\N	\N	f	\N	\N	Festival Organizer	f	Scott	\N	\N	f
506	\N	f	f		f	\N	f	f	Martin	f	f	\N	\N	f	\N	\N	Deputy Administration	f	Harbor	\N	\N	f
507	\N	f	f	As site manager this year I have put extra effort into roping people to volunteer. Anyone who can spell my name is probably kosher but will confirm via email.	f	\N	f	f	Edward	f	f	\N	\N	f	\N	\N	Site Manager	f	Bilbe	\N	\N	f
511	\N	f	f		f	\N	f	f	Andrew	f	f	\N	\N	f	\N	\N		f	Turner	\N	\N	f
671	\N	f	f		f	\N	f	f	Brian	f	f	\N	\N	f	\N	\N	Beer Judging Organizer	f	Jones	\N	\N	f
592	\N	f	f	Have worked the Foreign bier bar for many years but don't mind working on the normal bars if short.\n\\\n\n\\\nCheers. David Newman.	f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Newman	\N	\N	f
806	\N	f	f		f	\N	f	f	Tim	f	f	\N	\N	f	\N	\N		f	Andrews	\N	\N	f
508	\N	f	f	I am part of the site team. We are still to work out how coverage will work while the festival is open. Please check with me before assigning me outside of site team. -Ryan	f	\N	f	f	Ryan	f	f	\N	\N	f	\N	\N		f	Shook	\N	\N	f
509	\N	f	f	If I'm on entrance then could a stall be provided as I have difficulty standing for long periods I have a folding chair if I'm on a stall if not possible please let me know and I'll try to sort something out	f	\N	f	f	Ross	f	f	\N	\N	f	\N	\N		f	Chester	\N	\N	f
510	\N	f	f		f	\N	f	f	Nick	f	f	\N	\N	f	\N	\N	Chief Steward	f	O`Reilly	\N	\N	f
512	\N	f	f	I will have a couple of half days off at some point during the above, tba depending on what's happening on site and other people's availabilities	f	\N	f	f	Pat	f	f	\N	\N	f	\N	\N	Deputy Organizer	f	Rapley	\N	\N	f
513	\N	f	f		f	\N	f	f	Test	f	f	\N	\N	f	\N	\N		f	Test	\N	\N	f
551	\N	f	f	I shall be working on Friday 28th 11.30 - 7.00pm.  On Beer Bar.\n\\\nMany Thanks\n\\\nTony	f	\N	f	f	Tony	f	f	\N	\N	f	\N	\N		f	Crawley	\N	\N	f
552	\N	f	f	My principal job is supervising the cider judging (Fri up to at least 7pm, Sat up to 3pm), may be able to do some serving on cider bar. I need car parking (not camping) from Thursday pm until Sunday am.	f	\N	f	f	Chris	f	f	\N	\N	f	\N	\N		f	Rogers	\N	\N	f
553	\N	f	f		f	\N	f	f	Paul	f	f	\N	\N	f	\N	\N		f	Rayner	\N	\N	f
554	\N	f	f	I have over 4 years bar work experience and specialized in Ciders. 	f	\N	f	f	Dean	f	f	\N	\N	f	\N	\N		f	Noakes	\N	\N	f
555	\N	f	f	Might be a little late some mornings.	f	\N	f	f	Dickon	f	f	\N	\N	f	\N	\N	Deputy Beer Bar Manager	f	Hood	\N	\N	f
556	\N	f	f	Have previously volunteered the last few years	f	\N	f	f	Ashish	f	f	\N	\N	f	\N	\N		f	Naik	\N	\N	f
558	\N	f	f		f	\N	f	f	Danielle	f	f	\N	\N	f	\N	\N		f	Miller	\N	\N	f
595	\N	f	f		f	\N	f	f	Mario	f	f	\N	\N	f	\N	\N		f	Mendolicchio	\N	\N	f
670	\N	f	f	I would please like to work in the beer bar with my father Mark Billing.	f	\N	f	f	Oliver	f	f	\N	\N	f	\N	\N		f	Billing	\N	\N	f
686	\N	f	f	I can work until 1700 on Friday 28th	f	\N	f	f	Nick	f	f	\N	\N	f	\N	\N		f	Swift	\N	\N	f
687	\N	f	f		f	\N	f	f	Graham	f	f	\N	\N	f	\N	\N		f	May	\N	\N	f
557	\N	f	f	Request to work with Mike Garner #242522	f	\N	f	f	Will	f	f	\N	\N	f	\N	\N		f	Burchell	\N	\N	f
559	\N	f	f		f	\N	f	f	Katherine	f	f	\N	\N	f	\N	\N		f	Lilley	\N	\N	f
560	\N	f	f		f	\N	f	f	Tim	f	f	\N	\N	f	\N	\N		f	Winter	\N	\N	f
561	\N	f	f	Night Team Manager (Please can my T-shirt reflect this)\n\\\n\n\\\nWill be on site Friday 21st.   Depending on what time I finish work on Friday 21st I will arrive sometime during the evening. \n\\\n\n\\\nDepart Sunday 30th am as I return to work on Monday 1st May\n\\\n\n\\\nCheck in to accommodation Saturday 22nd. Check out of accommodation Sunday 30th am after breakfast 	f	\N	f	f	Richard	f	f	\N	\N	f	\N	\N		f	Shervington	\N	\N	f
562	\N	f	f	I will be helping with the beer judging on Thursday, as on previous years, working with more fellow-Kennet Morris Men..	f	\N	f	f	Nic	f	f	\N	\N	f	\N	\N		f	Yannacopoulos	\N	\N	f
563	\N	f	f		f	\N	f	f	Brian	f	f	\N	\N	f	\N	\N		f	Oxnard	\N	\N	f
564	\N	f	f		f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	West	\N	\N	f
565	\N	f	f		f	\N	f	f	Patrick	f	f	\N	\N	f	\N	\N		f	Nolan	\N	\N	f
566	\N	f	f		f	\N	f	f	Peter	f	f	\N	\N	f	\N	\N		f	Weekes	\N	\N	f
568	\N	f	f		f	\N	f	f	Philip	f	f	\N	\N	f	\N	\N		f	Chappell	\N	\N	f
569	\N	f	f		f	\N	f	f	Lydia	f	f	\N	\N	f	\N	\N		f	Charles	\N	\N	f
570	\N	f	f	Friends: James Elsmore, Tim Freeman, Douglas McDougall	f	\N	f	f	Nicholas	f	f	\N	\N	f	\N	\N		f	Mackerness	\N	\N	f
571	\N	f	f		f	\N	f	f	Jo	f	f	\N	\N	f	\N	\N		f	Nicolson	\N	\N	f
572	\N	f	f		f	\N	f	f	Stephanie	f	f	\N	\N	f	\N	\N		f	Henderson	\N	\N	f
573	\N	f	f		f	\N	f	f	Michael	f	f	\N	\N	f	\N	\N		f	Brady	\N	\N	f
575	\N	f	f		f	\N	f	f	David	f	f	\N	\N	f	\N	\N		f	Price	\N	\N	f
576	\N	f	f	Prefer to work Cider Bar, but can be flexible if really needed else where	f	\N	f	f	Brendan	f	f	\N	\N	f	\N	\N		f	Sothcott	\N	\N	f
747	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Buckley	\N	\N	f
688	\N	f	f	Prefer bar on Saturday and Entrance Sunday	f	\N	f	f	Mike	f	f	\N	\N	f	\N	\N		f	Smith	\N	\N	f
689	\N	f	f	Deputy Games Manager (shared with Jennie Farley)	f	\N	f	f	Madeleine	f	f	\N	\N	f	\N	\N	Deputy Games Manager	f	Markey	\N	\N	f
690	\N	f	f		f	\N	f	f	Camilla	f	f	\N	\N	f	\N	\N		f	Ford	\N	\N	f
691	\N	f	f	I would like to work in the same area as my husband, Ian King ( 121835) who has requested the same time beer bar slots as me.	f	\N	f	f	Margaret	f	f	\N	\N	f	\N	\N		f	King	\N	\N	f
692	\N	f	f	Can I work together with Alex HARKNESS Please. Thank You Reading Camra.	f	\N	f	f	Steve	f	f	\N	\N	f	\N	\N		f	LAWRENCE	\N	\N	f
683	\N	f	f		f	\N	f	f	Ben	f	f	\N	\N	f	\N	\N		f	Hart	\N	\N	f
766	\N	f	f	I am disabled, so may require sit down quick breaks throughout the session	f	\N	f	f	Jo	f	f	\N	\N	f	\N	\N		f	Toovey	\N	\N	f
693	\N	f	f	On Monday afternoon I intend to work until ~3pm, although if I am still full of energy I may continue until 5pm hauling casks. 	f	\N	f	f	Nicholas	f	f	\N	\N	f	\N	\N		f	Mayes	\N	\N	f
694	\N	f	f		f	\N	f	f	Doug	f	f	\N	\N	f	\N	\N		f	McDougall	\N	\N	f
695	\N	f	f	I have injured my back over the weekend so will not be able to volunteer tomorrow.  I am still hopeful for Thursday but it will have to be light duties.\n\\\n\n\\\nColin	f	\N	f	f	Colin	f	f	\N	\N	f	\N	\N		f	Palmer	\N	\N	f
696	\N	f	f		f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Gravenor	\N	\N	f
697	\N	f	f	Would like to work with my son Daniel Hand who will sign up for same sessions and roles.	f	\N	f	f	Richard	f	f	\N	\N	f	\N	\N		f	Hand	\N	\N	f
698	\N	f	f	Setup: I've told Martin Hoare I'll help him to get the PA wiring up.  I haven't ticked any sessions for that because it never goes to schedule!\n\\\nOpen: I can't stand up all day because of back pain.  I'll last longer and get more done if you can mix a sit-down job with the bar work.  Thanks.	f	\N	f	f	Sue	f	f	\N	\N	f	\N	\N		f	White	\N	\N	f
699	\N	f	f	I am Qualified First Aid at work.	f	\N	f	f	Thomas	f	f	\N	\N	f	\N	\N		f	Ruane	\N	\N	f
700	\N	f	f		f	\N	f	f	Karen	f	f	\N	\N	f	\N	\N		f	Pratt	\N	\N	f
701	\N	f	f		f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	Elsmore	\N	\N	f
702	\N	f	f	I would like to work alongside my friends, Ashish Naik and Richard Silley if possible please	f	\N	f	f	Ralph	f	f	\N	\N	f	\N	\N		f	McFadyen	\N	\N	f
703	\N	f	f	Would like to work with Natalie New if possible. Member 514340	f	\N	f	f	Jonbob	f	f	\N	\N	f	\N	\N		f	New	\N	\N	f
704	\N	f	f	Please could I work the same time and around the same area with Jonbob New, it doesn't have to be alongside him. 	f	\N	f	f	Natalie	f	f	\N	\N	f	\N	\N		f	New	\N	\N	f
705	\N	f	f		f	\N	f	f	Darren	f	f	\N	\N	f	\N	\N		f	Stock	\N	\N	f
706	\N	f	f	Cider bar manager and yes please i'd like a shirt.	f	\N	f	f	Ewan	f	f	\N	\N	f	\N	\N	Cider Bar Manager	f	Tolladay	\N	\N	f
707	\N	f	f		f	\N	f	f	Jo	f	f	\N	\N	f	\N	\N		f	Metcalf	\N	\N	f
708	\N	f	f		f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Treder	\N	\N	f
709	\N	f	f		f	\N	f	f	Kevin	f	f	\N	\N	f	\N	\N		f	Brady	\N	\N	f
710	\N	f	f		f	\N	f	f	Reshma	f	f	\N	\N	f	\N	\N		f	Thakkar	\N	\N	f
711	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Abramson	\N	\N	f
712	\N	f	f	Hi there! Ideally I'd like to help with games, as I know a couple of the team already (Andrew Waterfall and Tes Matthews) and it suits my personality down to the ground!	f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Haigh	\N	\N	f
639	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Breakwell	\N	\N	f
739	\N	f	f		f	\N	f	f	Tim	f	f	\N	\N	f	\N	\N		f	Gough	\N	\N	f
713	\N	f	f	member of kennet morris men will be in kit pm\n\\\nagreed to help Brian Jones Thursday afternoon with beer judging	f	\N	f	f	Clive	f	f	\N	\N	f	\N	\N		f	Allen	\N	\N	f
714	\N	f	f		f	\N	f	f	john	f	f	\N	\N	f	\N	\N		f	brown	\N	\N	f
715	\N	f	f		f	\N	f	f	Kate	f	f	\N	\N	f	\N	\N		f	Martin	\N	\N	f
716	\N	f	f		f	\N	f	f	Darren	f	f	\N	\N	f	\N	\N		f	Streat	\N	\N	f
717	\N	f	f		f	\N	f	f	Linda	f	f	\N	\N	f	\N	\N		f	Thompson	\N	\N	f
718	\N	f	f	Suffer with fibromyalgia so may not be able to work full shifts but will do my best to do so.	f	\N	f	f	Gavin	f	f	\N	\N	f	\N	\N		f	Jenkins	\N	\N	f
719	\N	f	f	I am helping to run the cider competition so will not be behind the bar while this is running	f	\N	f	f	Mike	f	f	\N	\N	f	\N	\N		f	Gilroy	\N	\N	f
720	\N	f	f		f	\N	f	f	Simon	f	f	\N	\N	f	\N	\N		f	Andrews	\N	\N	f
721	\N	f	f		f	\N	f	f	Jonathan	f	f	\N	\N	f	\N	\N		f	Meek	\N	\N	f
722	\N	f	f	My friend are SUPPOSED to be comming Saturday; but what normally happens is that they don't turn up and I'm spare to work on Saturday as well - i know you are always short.\n\\\nI don't mind doing ONE shift on the cider bar, but i can't handle any more.	f	\N	f	f	Adrian	f	f	\N	\N	f	\N	\N		f	Samler	\N	\N	f
723	\N	f	f	I will be available on Wednesday evening to set up the CAMERA Sales and can pop in between 9.00 and 10.30 to help with set-up catering. I have a food hygiene certificate and will happily fill baguettes.	f	\N	f	f	Tina	f	f	\N	\N	f	\N	\N		f	Bilbe	\N	\N	f
859	\N	f	f	I have spoken with Chris Rouse about helping out with cider comp starting about 12:30pm on Friday and Saturday afternoons, he told me to sign up as staff. I will also be available to work on the cider bar after the competition finishes on friday and before it starts on the saturday. Cheers	f	\N	f	f	ALISTAIR	f	f	\N	\N	f	\N	\N		f	SMITH	\N	\N	f
863	\N	f	f	If he's working the bar too, I'd like Ricky Moysey to be put with me.	f	\N	f	f	Jamie	f	f	\N	\N	f	\N	\N	Beer Orderer	f	Duffield	\N	\N	f
864	\N	f	f		f	\N	f	f	ross	f	f	\N	\N	f	\N	\N		f	Chester	\N	\N	f
865	\N	f	f		f	\N	f	f	Bart	f	f	\N	\N	f	\N	\N		f	Weeks	\N	\N	f
866	\N	f	f		f	\N	f	f	Ricky	f	f	\N	\N	f	\N	\N	Beer Orderer	f	Moysey	\N	\N	f
867	\N	f	f		f	\N	f	f	Liam	f	f	\N	\N	f	\N	\N		f	Kavanagh	\N	\N	f
871	\N	f	f	Site Team.	f	\N	f	f	Bret	f	f	\N	\N	f	\N	\N		f	Colloff	\N	\N	f
872	\N	f	f		f	\N	f	f	Peter	f	f	\N	\N	f	\N	\N		f	Gibbins	\N	\N	f
873	\N	f	f	PA system and Thursday PA for events such as Paul Sinha Quiz.\n\\\n	f	\N	f	f	Martin	f	f	\N	\N	f	\N	\N		f	Hoare	\N	\N	f
874	\N	f	f	Teddy bear shirt size	f	\N	f	f	Doris	f	f	\N	\N	f	\N	\N	Chief Executive Officer	f	Panda	\N	\N	f
875	\N	f	f		f	\N	f	f	Laura	f	f	\N	\N	f	\N	\N		f	Meegan	\N	\N	f
876	\N	f	f		f	\N	f	f	SIMON	f	f	\N	\N	f	\N	\N		f	NUTTALL	\N	\N	f
877	\N	f	f		f	\N	f	f	Michael	f	f	\N	\N	f	\N	\N		f	Hammer	\N	\N	f
878	\N	f	f		f	\N	f	f	Anthony	f	f	\N	\N	f	\N	\N		f	Saunders	\N	\N	f
879	\N	f	f		f	\N	f	f	Adam	f	f	\N	\N	f	\N	\N		f	Miller	\N	\N	f
880	\N	f	f	I could actually work further sessions if needed. \n\\\nI doubt if this would help but I am a French speaking Belgian.\n\\\nI'm 70 but fit.\n\\\nLooking forward to the festival.	f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	Murphy	\N	\N	f
883	\N	f	f	Helping Tim Loyd as part of cellar management 	f	\N	f	f	Kevin	f	f	\N	\N	f	\N	\N		f	Black	\N	\N	f
885	\N	f	f		f	\N	f	f	Mark	f	f	\N	\N	f	\N	\N		f	Rickson 	\N	\N	f
886	\N	f	f		f	\N	f	f	Paul	f	f	\N	\N	f	\N	\N		f	Anderson	\N	\N	f
887	\N	f	f		f	\N	f	f	James	f	f	\N	\N	f	\N	\N		f	Marshall	\N	\N	f
888	\N	f	f		f	\N	f	f	Geoff	f	f	\N	\N	f	\N	\N		f	Keen	\N	\N	f
889	\N	f	f		f	\N	f	f	Eric	f	f	\N	\N	f	\N	\N		f	Warner	\N	\N	f
890	\N	f	f		f	\N	f	f	Laura	f	f	\N	\N	f	\N	\N		f	Meegan	\N	\N	f
891	\N	f	f		f	\N	f	f	John	f	f	\N	\N	f	\N	\N		f	Sims	\N	\N	f
892	\N	f	f		f	\N	f	f	Graham	f	f	\N	\N	f	\N	\N		f	Bishop	\N	\N	f
893	\N	f	f		f	\N	f	f	Matthew	f	f	\N	\N	f	\N	\N		f	Debney	\N	\N	f
1851	\N	f	f	\N	f	a@a	f	f	a@a	f	f	\N	\N	f	\N	\N	\N	f	a@a	\N	9ecf7f10-a883-4d55-987b-ba9102b67788	f
1	\N	f	f	\N	f		f	f		f	f	\N		f	\N	\N	\N	f		\N	\N	f
2	\N	f	f	\N	f		f	f		f	f	\N		f	\N	\N	\N	f		\N	\N	f
1901	\N	f	f		f	nick.jerram@gmail.com	t	f	Nick	f	t		66535	f	\N	\N	\N	f	Jerram	\N	a537265b-fc03-43eb-8a74-8673ea479e70	f
\.


--
-- Data for Name: volunteer_area; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.volunteer_area (areaid, volunteerid, preference) FROM stdin;
-1	501	1
-1	502	1
-1	503	1
-1	504	1
-1	505	1
-1	506	1
-1	507	1
-1	508	1
-1	509	1
-1	510	1
-1	511	1
-1	512	1
-1	551	1
-1	552	1
-1	553	1
-1	554	1
-1	555	1
-1	556	1
-1	557	1
-1	558	1
-1	559	1
-1	560	1
-1	561	1
-1	562	1
-1	563	1
-1	564	1
-1	565	1
-1	566	1
-1	568	1
-1	569	1
-1	570	1
-1	571	1
-1	573	1
-1	574	1
-1	575	1
-1	576	1
-1	578	1
-1	579	1
-1	580	1
-1	581	1
-1	582	1
-1	583	1
-1	584	1
-1	585	1
-1	587	1
-1	588	1
-1	589	1
-1	590	1
-1	591	1
-1	592	1
-1	593	1
-1	594	1
-1	595	1
-1	596	1
-1	598	1
-1	599	1
-1	600	1
-1	601	1
-1	602	1
-1	603	1
-1	604	1
-1	605	1
-1	606	1
-1	607	1
-1	608	1
-1	609	1
-1	610	1
-1	611	1
-1	612	1
-1	613	1
-1	614	1
-1	616	1
-1	617	1
-1	618	1
-1	619	1
-1	620	1
-1	621	1
-1	622	1
-1	623	1
-1	624	1
-1	625	1
-1	626	1
-1	627	1
-1	628	1
-1	629	1
-1	630	1
-1	631	1
-1	632	1
-1	633	1
-1	634	1
-1	635	1
-1	636	1
-1	637	1
-1	638	1
-1	641	1
-1	642	1
-1	643	1
-1	644	1
-1	645	1
-1	646	1
-1	647	1
-1	648	1
-1	649	1
-1	651	1
-1	652	1
-1	653	1
-1	654	1
-1	655	1
-1	658	1
-1	659	1
-1	660	1
-1	661	1
-1	662	1
-1	663	1
-1	664	1
-1	665	1
-1	666	1
-1	667	1
-1	668	1
-1	669	1
-1	670	1
-1	671	1
-1	672	1
-1	673	1
-1	674	1
-1	675	1
-1	676	1
-1	678	1
-1	679	1
-1	680	1
-1	681	1
-1	682	1
-1	684	1
-1	685	1
-1	686	1
-1	687	1
-1	688	1
-1	689	1
-1	690	1
-1	691	1
-1	692	1
-1	693	1
-1	694	1
-1	695	1
-1	696	1
-1	697	1
-1	698	1
-1	699	1
-1	700	1
-1	701	1
-1	702	1
-1	703	1
-1	704	1
-1	706	1
-1	707	1
-1	708	1
-1	709	1
-1	710	1
-1	711	1
-1	712	1
-1	713	1
-1	714	1
-1	716	1
-1	718	1
-1	719	1
-1	720	1
-1	721	1
-1	722	1
-1	723	1
-1	724	1
-1	725	1
-1	726	1
-1	727	1
-1	728	1
-1	729	1
-1	730	1
-1	731	1
-1	732	1
-1	733	1
-1	734	1
-1	735	1
-1	736	1
-1	737	1
-1	739	1
-1	740	1
-1	741	1
-1	742	1
-1	743	1
-1	748	1
-1	749	1
-1	750	1
-1	751	1
-1	752	1
-1	753	1
-1	754	1
-1	755	1
-1	756	1
-1	757	1
-1	758	1
-1	759	1
-1	760	1
-1	761	1
-1	762	1
-1	763	1
-1	764	1
-1	765	1
-1	766	1
-1	767	1
-1	769	1
-1	771	1
-1	772	1
-1	773	1
-1	774	1
-1	775	1
-1	776	1
-1	778	1
-1	779	1
-1	780	1
-1	781	1
-1	782	1
-1	783	1
-1	784	1
-1	785	1
-1	786	1
-1	788	1
-1	789	1
-1	790	1
-1	791	1
-1	792	1
-1	793	1
-1	794	1
-1	795	1
-1	796	1
-1	797	1
-1	798	1
-1	799	1
-1	800	1
-1	801	1
-1	802	1
-1	803	1
-1	804	1
-1	805	1
-1	806	1
-1	808	1
-1	809	1
-1	810	1
-1	811	1
-1	812	1
-1	813	1
-1	814	1
-1	815	1
-1	816	1
-1	817	1
-1	818	1
-1	819	1
-1	820	1
-1	821	1
-1	822	1
-1	823	1
-1	824	1
-1	825	1
-1	826	1
-1	827	1
-1	828	1
-1	829	1
-1	830	1
-1	831	1
-1	832	1
-1	833	1
-1	834	1
-1	835	1
-1	836	1
-1	837	1
-1	838	1
-1	839	1
-1	840	1
-1	841	1
-1	843	1
-1	844	1
-1	845	1
-1	846	1
-1	847	1
-1	849	1
-1	850	1
-1	851	1
-1	852	1
-1	853	1
-1	854	1
-1	855	1
-1	857	1
-1	858	1
-1	859	1
-1	861	1
-1	862	1
-1	863	1
-1	865	1
-1	866	1
-1	867	1
-1	868	1
-1	869	1
-1	870	1
-1	871	1
-1	872	1
-1	873	1
-1	874	1
-1	875	1
-1	877	1
-1	878	1
-1	879	1
-1	880	1
-1	881	1
-1	882	1
-1	883	1
-1	884	1
-1	885	1
-1	886	1
-1	887	1
-1	888	1
-1	889	1
0	501	1
0	502	1
0	503	1
0	504	1
0	505	1
0	506	1
0	507	1
0	508	1
0	509	1
0	510	1
0	511	1
0	512	1
0	551	1
0	552	1
0	553	1
0	554	1
0	555	1
0	556	1
0	557	1
0	558	1
0	559	1
0	560	1
0	561	1
0	562	1
0	563	1
0	564	1
0	565	1
0	566	1
0	568	1
0	569	1
0	570	1
0	571	1
0	573	1
0	574	1
0	575	1
0	576	1
0	578	1
0	579	1
0	580	1
0	581	1
0	582	1
0	583	1
0	584	1
0	585	1
0	587	1
0	588	1
0	589	1
0	590	1
0	591	1
0	592	1
0	593	1
0	594	1
0	595	1
0	596	1
0	598	1
0	599	1
0	600	1
0	601	1
0	602	1
0	603	1
0	604	1
0	605	1
0	606	1
0	607	1
0	608	1
0	609	1
0	610	1
0	611	1
0	612	1
0	613	1
0	614	1
0	616	1
0	617	1
0	618	1
0	619	1
0	620	1
0	621	1
0	622	1
0	623	1
0	624	1
0	625	1
0	626	1
0	627	1
0	628	1
0	629	1
0	630	1
0	631	1
0	632	1
0	633	1
0	634	1
0	635	1
0	636	1
0	637	1
0	638	1
0	641	1
0	642	1
0	643	1
0	644	1
0	645	1
0	646	1
0	647	1
0	648	1
0	649	1
0	651	1
0	652	1
0	653	1
0	654	1
0	655	1
0	658	1
0	659	1
0	660	1
0	661	1
0	662	1
0	663	1
0	664	1
0	665	1
0	666	1
0	667	1
0	668	1
0	669	1
0	670	1
0	671	1
0	672	1
0	673	1
0	674	1
0	675	1
0	676	1
0	678	1
0	679	1
0	680	1
0	681	1
0	682	1
0	684	1
0	685	1
0	686	1
0	687	1
0	688	1
0	689	1
0	690	1
0	691	1
0	692	1
0	693	1
0	694	1
0	695	1
0	696	1
0	697	1
0	698	1
0	699	1
0	700	1
0	701	1
0	702	1
0	703	1
0	704	1
0	706	1
0	707	1
0	708	1
0	709	1
0	710	1
0	711	1
0	712	1
0	713	1
0	714	1
0	716	1
0	718	1
0	719	1
0	720	1
0	721	1
0	722	1
0	723	1
0	724	1
0	725	1
0	726	1
0	727	1
0	728	1
0	729	1
0	730	1
0	731	1
0	732	1
0	733	1
0	734	1
0	735	1
0	736	1
0	737	1
0	739	1
0	740	1
0	741	1
0	742	1
0	743	1
0	748	1
0	749	1
0	750	1
0	751	1
0	752	1
0	753	1
0	754	1
0	755	1
0	756	1
0	757	1
0	758	1
0	759	1
0	760	1
0	761	1
0	762	1
0	763	1
0	764	1
0	765	1
0	766	1
0	767	1
0	769	1
0	771	1
0	772	1
0	773	1
0	774	1
0	775	1
0	776	1
0	778	1
0	779	1
0	780	1
0	781	1
0	782	1
0	783	1
0	784	1
0	785	1
0	786	1
0	788	1
0	789	1
0	790	1
0	791	1
0	792	1
0	793	1
0	794	1
0	795	1
0	796	1
0	797	1
0	798	1
0	799	1
0	800	1
0	801	1
0	802	1
0	803	1
0	804	1
0	805	1
0	806	1
0	808	1
0	809	1
0	810	1
0	811	1
0	812	1
0	813	1
0	814	1
0	815	1
0	816	1
0	817	1
0	818	1
0	819	1
0	820	1
0	821	1
0	822	1
0	823	1
0	824	1
0	825	1
0	826	1
0	827	1
0	828	1
0	829	1
0	830	1
0	831	1
0	832	1
0	833	1
0	834	1
0	835	1
0	836	1
0	837	1
0	838	1
0	839	1
0	840	1
0	841	1
0	843	1
0	844	1
0	845	1
0	846	1
0	847	1
0	849	1
0	850	1
0	851	1
0	852	1
0	853	1
0	854	1
0	855	1
0	857	1
0	858	1
0	859	1
0	861	1
0	862	1
0	863	1
0	865	1
0	866	1
0	867	1
0	868	1
0	869	1
0	870	1
0	871	1
0	872	1
0	873	1
0	874	1
0	875	1
0	877	1
0	878	1
0	879	1
0	880	1
0	881	1
0	882	1
0	883	1
0	884	1
0	885	1
0	886	1
0	887	1
0	888	1
0	889	1
1	101	2
1	151	1
1	501	1
1	502	1
1	503	1
1	506	1
1	507	1
1	508	2
1	510	1
1	511	1
1	512	1
1	551	2
1	553	2
1	554	2
1	555	2
1	556	2
1	558	2
1	560	2
1	561	1
1	563	2
1	568	2
1	570	2
1	571	1
1	574	2
1	575	2
1	578	1
1	579	2
1	581	2
1	582	2
1	583	1
1	584	1
1	585	1
1	590	1
1	591	2
1	592	1
1	593	2
1	594	2
1	595	2
1	596	1
1	598	2
1	600	2
1	601	1
1	602	1
1	603	1
1	604	1
1	605	1
1	606	2
1	607	2
1	608	2
1	609	1
1	610	2
1	611	2
1	612	1
1	613	2
1	614	1
1	616	2
1	618	2
1	620	1
1	622	2
1	625	2
1	627	1
1	628	2
1	629	1
1	630	1
1	631	2
1	634	1
1	635	2
1	636	2
1	638	2
1	643	1
1	644	2
1	645	2
1	646	2
1	647	2
1	651	1
1	652	2
1	653	1
1	658	2
1	659	2
1	660	2
1	661	2
1	663	2
1	664	2
1	665	1
1	666	2
1	667	2
1	670	2
1	671	2
1	673	1
1	675	2
1	676	1
1	678	2
1	680	2
1	682	2
1	687	2
1	688	2
1	691	2
1	692	2
1	693	2
1	694	2
1	695	1
1	696	2
1	697	2
1	698	1
1	699	1
1	700	1
1	701	2
1	702	2
1	703	2
1	704	1
1	707	1
1	708	1
1	709	2
1	710	2
1	711	1
1	712	1
1	713	2
1	714	2
1	716	1
1	719	1
1	720	1
1	721	2
1	722	2
1	723	1
1	724	1
1	725	1
1	726	2
1	727	2
1	730	2
1	733	2
1	734	1
1	735	1
1	736	1
1	737	2
1	739	2
1	740	1
1	741	2
1	748	1
1	749	2
1	750	2
1	752	2
1	754	2
1	755	2
1	756	2
1	757	2
1	759	1
1	760	2
1	761	2
1	762	1
1	764	2
1	765	2
1	766	2
1	767	2
1	769	1
1	771	2
1	772	2
1	773	1
1	774	1
1	778	1
1	780	2
1	781	2
1	783	2
1	784	2
1	785	2
1	788	2
1	789	2
1	790	2
1	791	1
1	793	2
1	794	2
1	797	2
1	798	2
1	799	2
1	800	1
1	801	2
1	802	2
1	803	2
1	804	2
1	805	2
1	806	2
1	809	1
1	810	2
1	811	2
1	813	2
1	814	1
1	815	2
1	816	2
1	817	2
1	818	2
1	819	2
1	820	2
1	821	2
1	823	2
1	825	2
1	827	2
1	828	1
1	830	1
1	831	1
1	832	2
1	834	2
1	835	1
1	836	2
1	837	2
1	838	2
1	839	2
1	840	1
1	841	1
1	843	2
1	844	2
1	845	1
1	846	1
1	847	1
1	849	1
1	852	2
1	853	2
1	854	1
1	855	1
1	858	2
1	859	1
1	861	1
1	862	2
1	863	2
1	865	2
1	866	1
1	867	1
1	869	1
1	872	2
1	875	1
1	877	2
1	878	2
1	880	1
1	881	1
1	882	2
1	883	2
1	884	1
1	885	1
1	886	2
1	887	1
1	888	2
1	889	2
2	101	2
2	151	1
2	501	1
2	502	1
2	503	1
2	506	1
2	507	1
2	508	2
2	510	1
2	511	1
2	512	1
2	551	2
2	553	2
2	554	2
2	555	2
2	556	2
2	558	2
2	560	2
2	561	1
2	563	2
2	568	2
2	570	2
2	571	1
2	574	2
2	575	2
2	578	1
2	579	2
2	581	2
2	582	2
2	583	1
2	584	1
2	585	1
2	590	1
2	591	2
2	592	1
2	593	2
2	594	2
2	595	2
2	596	1
2	598	2
2	600	2
2	601	1
2	602	1
2	603	1
2	604	1
2	605	1
2	606	2
2	607	2
2	608	2
2	609	1
2	610	2
2	611	2
2	612	1
2	613	2
2	614	1
2	616	2
2	618	2
2	620	1
2	622	2
2	625	2
2	627	1
2	628	2
2	629	1
2	630	1
2	631	2
2	634	1
2	635	2
2	636	2
2	638	2
2	643	1
2	644	2
2	645	2
2	646	2
2	647	2
2	651	1
2	652	2
2	653	1
2	658	2
2	659	2
2	660	2
2	661	2
2	663	2
2	664	2
2	665	1
2	666	2
2	667	2
2	670	2
2	671	2
2	673	1
2	675	2
2	676	1
2	678	2
2	680	2
2	682	2
2	687	2
2	688	2
2	691	2
2	692	2
2	693	2
2	694	2
2	695	1
2	696	2
2	697	2
2	698	1
2	699	1
2	700	1
2	701	2
2	702	2
2	703	2
2	704	1
2	707	1
2	708	1
2	709	2
2	710	2
2	711	1
2	712	1
2	713	2
2	714	2
2	716	1
2	719	1
2	720	1
2	721	2
2	722	2
2	723	1
2	724	1
2	725	1
2	726	2
2	727	2
2	730	2
2	733	2
2	734	1
2	735	1
2	736	1
2	737	2
2	739	2
2	740	1
2	741	2
2	748	1
2	749	2
2	750	2
2	752	2
2	754	2
2	755	2
2	756	2
2	757	2
2	759	1
2	760	2
2	761	2
2	762	1
2	764	2
2	765	2
2	766	2
2	767	2
2	769	1
2	771	2
2	772	2
2	773	1
2	774	1
2	778	1
2	780	2
2	781	2
2	783	2
2	784	2
2	785	2
2	788	2
2	789	2
2	790	2
2	791	1
2	793	2
2	794	2
2	797	2
2	798	2
2	799	2
2	800	1
2	801	2
2	802	2
2	803	2
2	804	2
2	805	2
2	806	2
2	809	1
2	810	2
2	811	2
2	813	2
2	814	1
2	815	2
2	816	2
2	817	2
2	818	2
2	819	2
2	820	2
2	821	2
2	823	2
2	825	2
2	827	2
2	828	1
2	830	1
2	831	1
2	832	2
2	834	2
2	835	1
2	836	2
2	837	2
2	838	2
2	839	2
2	840	1
2	841	1
2	843	2
2	844	2
2	845	1
2	846	1
2	847	1
2	849	1
2	852	2
2	853	2
2	854	1
2	855	1
2	858	2
2	859	1
2	861	1
2	862	2
2	863	2
2	865	2
2	866	1
2	867	1
2	869	1
2	872	2
2	875	1
2	877	2
2	878	2
2	880	1
2	881	1
2	882	2
2	883	2
2	884	1
2	885	1
2	886	2
2	887	1
2	888	2
2	889	2
3	101	2
3	151	1
3	501	1
3	502	1
3	503	1
3	506	1
3	507	1
3	508	2
3	510	1
3	511	1
3	512	1
3	551	2
3	553	2
3	554	2
3	555	2
3	556	2
3	558	2
3	560	2
3	561	1
3	563	2
3	568	2
3	570	2
3	571	1
3	574	2
3	575	2
3	578	1
3	579	2
3	581	2
3	582	2
3	583	1
3	584	1
3	585	1
3	590	1
3	591	2
3	592	1
3	593	2
3	594	2
3	595	2
3	596	1
3	598	2
3	600	2
3	601	1
3	602	1
3	603	1
3	604	1
3	605	1
3	606	2
3	607	2
3	608	2
3	609	1
3	610	2
3	611	2
3	612	1
3	613	2
3	614	1
3	616	2
3	618	2
3	620	1
3	622	2
3	625	2
3	627	1
3	628	2
3	629	1
3	630	1
3	631	2
3	634	1
3	635	2
3	636	2
3	638	2
3	643	1
3	644	2
3	645	2
3	646	2
3	647	2
3	651	1
3	652	2
3	653	1
3	658	2
3	659	2
3	660	2
3	661	2
3	663	2
3	664	2
3	665	1
3	666	2
3	667	2
3	670	2
3	671	2
3	673	1
3	675	2
3	676	1
3	678	2
3	680	2
3	682	2
3	687	2
3	688	2
3	691	2
3	692	2
3	693	2
3	694	2
3	695	1
3	696	2
3	697	2
3	698	1
3	699	1
3	700	1
3	701	2
3	702	2
3	703	2
3	704	1
3	707	1
3	708	1
3	709	2
3	710	2
3	711	1
3	712	1
3	713	2
3	714	2
3	716	1
3	719	1
3	720	1
3	721	2
3	722	2
3	723	1
3	724	1
3	725	1
3	726	2
3	727	2
3	730	2
3	733	2
3	734	1
3	735	1
3	736	1
3	737	2
3	739	2
3	740	1
3	741	2
3	748	1
3	749	2
3	750	2
3	752	2
3	754	2
3	755	2
3	756	2
3	757	2
3	759	1
3	760	2
3	761	2
3	762	1
3	764	2
3	765	2
3	766	2
3	767	2
3	769	1
3	771	2
3	772	2
3	773	1
3	774	1
3	778	1
3	780	2
3	781	2
3	783	2
3	784	2
3	785	2
3	788	2
3	789	2
3	790	2
3	791	1
3	793	2
3	794	2
3	797	2
3	798	2
3	799	2
3	800	1
3	801	2
3	802	2
3	803	2
3	804	2
3	805	2
3	806	2
3	809	1
3	810	2
3	811	2
3	813	2
3	814	1
3	815	2
3	816	2
3	817	2
3	818	2
3	819	2
3	820	2
3	821	2
3	823	2
3	825	2
3	827	2
3	828	1
3	830	1
3	831	1
3	832	2
3	834	2
3	835	1
3	836	2
3	837	2
3	838	2
3	839	2
3	840	1
3	841	1
3	843	2
3	844	2
3	845	1
3	846	1
3	847	1
3	849	1
3	852	2
3	853	2
3	854	1
3	855	1
3	858	2
3	859	1
3	861	1
3	862	2
3	863	2
3	865	2
3	866	1
3	867	1
3	869	1
3	872	2
3	875	1
3	877	2
3	878	2
3	880	1
3	881	1
3	882	2
3	883	2
3	884	1
3	885	1
3	886	2
3	887	1
3	888	2
3	889	2
4	101	2
4	151	1
4	501	1
4	502	1
4	503	1
4	506	1
4	507	1
4	508	2
4	510	1
4	511	1
4	512	1
4	551	2
4	553	2
4	554	2
4	555	2
4	556	2
4	558	2
4	560	2
4	561	1
4	563	2
4	568	2
4	570	2
4	571	1
4	574	2
4	575	2
4	578	1
4	579	2
4	581	2
4	582	2
4	583	1
4	584	1
4	585	1
4	590	1
4	591	2
4	592	1
4	593	2
4	594	2
4	595	2
4	596	1
4	598	2
4	600	2
4	601	1
4	602	1
4	603	1
4	604	1
4	605	1
4	606	2
4	607	2
4	608	2
4	609	1
4	610	2
4	611	2
4	612	1
4	613	2
4	614	1
4	616	2
4	618	2
4	620	1
4	622	2
4	625	2
4	627	1
4	628	2
4	629	1
4	630	1
4	631	2
4	634	1
4	635	2
4	636	2
4	638	2
4	643	1
4	644	2
4	645	2
4	646	2
4	647	2
4	651	1
4	652	2
4	653	1
4	658	2
4	659	2
4	660	2
4	661	2
4	663	2
4	664	2
4	665	1
4	666	2
4	667	2
4	670	2
4	671	2
4	673	1
4	675	2
4	676	1
4	678	2
4	680	2
4	682	2
4	687	2
4	688	2
4	691	2
4	692	2
4	693	2
4	694	2
4	695	1
4	696	2
4	697	2
4	698	1
4	699	1
4	700	1
4	701	2
4	702	2
4	703	2
4	704	1
4	707	1
4	708	1
4	709	2
4	710	2
4	711	1
4	712	1
4	713	2
4	714	2
4	716	1
4	719	1
4	720	1
4	721	2
4	722	2
4	723	1
4	724	1
4	725	1
4	726	2
4	727	2
4	730	2
4	733	2
4	734	1
4	735	1
4	736	1
4	737	2
4	739	2
4	740	1
4	741	2
4	748	1
4	749	2
4	750	2
4	752	2
4	754	2
4	755	2
4	756	2
4	757	2
4	759	1
4	760	2
4	761	2
4	762	1
4	764	2
4	765	2
4	766	2
4	767	2
4	769	1
4	771	2
4	772	2
4	773	1
4	774	1
4	778	1
4	780	2
4	781	2
4	783	2
4	784	2
4	785	2
4	788	2
4	789	2
4	790	2
4	791	1
4	793	2
4	794	2
4	797	2
4	798	2
4	799	2
4	800	1
4	801	2
4	802	2
4	803	2
4	804	2
4	805	2
4	806	2
4	809	1
4	810	2
4	811	2
4	813	2
4	814	1
4	815	2
4	816	2
4	817	2
4	818	2
4	819	2
4	820	2
4	821	2
4	823	2
4	825	2
4	827	2
4	828	1
4	830	1
4	831	1
4	832	2
4	834	2
4	835	1
4	836	2
4	837	2
4	838	2
4	839	2
4	840	1
4	841	1
4	843	2
4	844	2
4	845	1
4	846	1
4	847	1
4	849	1
4	852	2
4	853	2
4	854	1
4	855	1
4	858	2
4	859	1
4	861	1
4	862	2
4	863	2
4	865	2
4	866	1
4	867	1
4	869	1
4	872	2
4	875	1
4	877	2
4	878	2
4	880	1
4	881	1
4	882	2
4	883	2
4	884	1
4	885	1
4	886	2
4	887	1
4	888	2
4	889	2
8	151	2
8	502	1
8	503	1
8	506	1
8	507	1
8	510	1
8	511	1
8	512	1
8	552	2
8	554	2
8	556	1
8	558	2
8	560	1
8	561	1
8	563	1
8	568	1
8	571	1
8	573	1
8	576	2
8	578	1
8	579	2
8	583	1
8	585	1
8	590	1
8	594	1
8	596	1
8	601	1
8	602	1
8	604	1
8	605	1
8	606	1
8	607	1
8	608	1
8	609	1
8	612	1
8	613	2
8	614	2
8	620	1
8	621	2
8	622	1
8	626	2
8	627	1
8	628	1
8	629	1
8	631	1
8	634	1
8	635	1
8	636	1
8	638	1
8	641	1
8	643	1
8	645	1
8	647	1
8	653	1
8	665	1
8	667	2
8	671	1
8	673	1
8	676	1
8	684	1
8	695	1
8	696	2
8	697	2
8	698	2
8	699	1
8	700	1
8	701	1
8	703	2
8	704	1
8	706	2
8	707	1
8	708	1
8	710	2
8	711	1
8	712	1
8	713	1
8	714	1
8	716	1
8	719	2
8	720	1
8	721	1
8	723	1
8	724	2
8	725	1
8	730	1
8	733	1
8	734	1
8	735	1
8	736	1
8	741	2
8	742	1
8	743	2
8	748	1
8	750	1
8	751	2
8	753	2
8	756	1
8	759	1
8	760	2
8	761	2
8	762	1
8	764	2
8	765	2
8	766	1
8	767	2
8	769	1
8	771	1
8	773	1
8	774	1
8	775	2
8	776	2
8	778	2
8	780	1
8	781	1
8	785	1
8	791	2
8	795	1
8	798	1
8	799	1
8	800	1
8	801	2
8	802	1
8	803	2
8	805	1
8	806	2
8	808	1
8	809	1
8	814	1
8	815	1
8	817	1
8	818	1
8	820	1
8	825	2
8	831	1
8	835	2
8	838	2
8	840	1
8	844	2
8	845	1
8	846	1
8	847	1
8	849	1
8	852	1
8	853	2
8	854	1
8	855	1
8	859	2
8	861	2
8	862	2
8	865	2
8	866	1
8	867	1
8	869	1
8	875	1
8	877	1
8	878	1
8	880	1
8	881	2
8	883	1
8	884	1
8	885	1
8	887	1
8	888	1
8	889	1
9	151	1
9	502	1
9	503	1
9	506	1
9	507	1
9	510	1
9	511	1
9	553	1
9	554	2
9	556	2
9	558	1
9	561	1
9	563	1
9	564	1
9	566	2
9	568	1
9	570	1
9	571	1
9	573	1
9	574	2
9	578	1
9	583	1
9	585	1
9	590	1
9	591	1
9	592	1
9	594	1
9	596	1
9	600	2
9	601	1
9	602	1
9	604	1
9	605	1
9	606	1
9	607	1
9	608	1
9	609	1
9	612	1
9	613	2
9	614	1
9	618	1
9	620	1
9	627	1
9	628	1
9	629	1
9	630	1
9	631	1
9	633	2
9	634	1
9	635	1
9	636	1
9	638	1
9	641	1
9	642	2
9	643	1
9	645	1
9	647	1
9	653	1
9	665	1
9	666	1
9	667	2
9	668	2
9	669	2
9	671	1
9	673	1
9	676	1
9	682	1
9	695	2
9	696	2
9	697	1
9	699	1
9	700	1
9	701	2
9	703	2
9	704	1
9	706	1
9	707	1
9	708	1
9	710	2
9	711	1
9	712	1
9	713	2
9	714	2
9	716	1
9	719	1
9	720	1
9	721	1
9	723	1
9	724	1
9	725	1
9	730	1
9	733	1
9	734	1
9	735	1
9	736	1
9	740	1
9	741	1
9	748	1
9	750	1
9	752	1
9	756	1
9	759	1
9	760	1
9	761	2
9	762	1
9	764	2
9	765	1
9	767	2
9	769	1
9	771	1
9	773	1
9	774	1
9	778	1
9	780	1
9	781	2
9	784	1
9	790	2
9	791	1
9	794	1
9	798	1
9	799	1
9	800	1
9	803	1
9	805	1
9	806	1
9	809	1
9	814	1
9	815	2
9	816	1
9	820	1
9	821	1
9	825	2
9	827	1
9	828	2
9	831	1
9	838	1
9	840	1
9	841	2
9	844	2
9	846	1
9	847	1
9	849	1
9	852	1
9	853	1
9	854	1
9	861	1
9	865	2
9	866	1
9	867	1
9	869	1
9	872	2
9	873	2
9	874	2
9	875	1
9	877	1
9	878	1
9	879	2
9	880	1
9	883	1
9	885	1
9	887	1
9	888	1
9	889	1
10	151	1
10	502	1
10	503	1
10	506	1
10	507	1
10	510	1
10	511	1
10	512	1
10	554	2
10	556	1
10	558	2
10	561	1
10	563	1
10	564	2
10	568	1
10	571	2
10	573	1
10	578	1
10	583	1
10	585	1
10	590	1
10	594	1
10	596	1
10	601	1
10	602	1
10	604	1
10	605	1
10	607	1
10	608	1
10	609	1
10	612	1
10	613	2
10	620	1
10	627	1
10	628	1
10	629	1
10	631	1
10	634	1
10	635	1
10	636	1
10	638	1
10	641	1
10	643	1
10	645	1
10	647	1
10	653	1
10	655	1
10	665	1
10	666	1
10	667	2
10	671	1
10	673	1
10	676	1
10	678	1
10	684	2
10	687	1
10	695	1
10	696	2
10	697	1
10	699	1
10	700	1
10	701	1
10	703	1
10	704	1
10	706	1
10	707	1
10	708	1
10	710	2
10	711	1
10	712	1
10	713	1
10	714	2
10	716	1
10	719	1
10	720	1
10	721	1
10	723	1
10	725	1
10	730	1
10	733	1
10	734	1
10	735	1
10	736	1
10	740	2
10	742	1
10	743	2
10	748	1
10	750	1
10	751	1
10	755	1
10	756	1
10	759	1
10	760	1
10	761	2
10	762	1
10	764	2
10	765	1
10	767	1
10	769	1
10	771	1
10	773	1
10	774	1
10	778	1
10	780	1
10	784	1
10	785	1
10	791	1
10	792	2
10	795	1
10	798	1
10	799	1
10	805	1
10	814	1
10	815	1
10	820	1
10	825	2
10	838	1
10	840	1
10	844	1
10	847	1
10	849	1
10	853	1
10	854	1
10	855	1
10	861	2
10	865	2
10	866	1
10	869	1
10	875	1
10	877	1
10	878	1
10	880	1
10	881	1
10	883	1
10	885	1
10	887	1
10	888	1
10	889	1
11	101	1
11	151	2
11	501	1
11	502	1
11	503	1
11	506	1
11	508	2
11	509	2
11	510	1
11	511	1
11	512	1
11	561	1
11	563	1
11	568	1
11	571	2
11	573	1
11	578	1
11	583	1
11	584	2
11	585	1
11	590	1
11	592	1
11	594	1
11	596	2
11	601	1
11	602	1
11	603	2
11	604	1
11	605	1
11	607	1
11	608	1
11	609	1
11	612	1
11	620	1
11	622	2
11	627	1
11	628	1
11	629	1
11	630	1
11	631	1
11	633	1
11	635	1
11	637	2
11	641	1
11	643	1
11	644	1
11	647	1
11	649	2
11	651	1
11	653	1
11	655	1
11	665	1
11	666	1
11	673	1
11	676	1
11	685	1
11	688	2
11	695	1
11	696	2
11	697	1
11	698	1
11	699	1
11	700	1
11	703	1
11	704	1
11	707	1
11	708	1
11	710	1
11	711	1
11	712	1
11	713	1
11	714	1
11	716	1
11	719	1
11	720	1
11	721	2
11	723	1
11	725	1
11	734	1
11	735	1
11	736	1
11	742	2
11	743	2
11	748	1
11	750	1
11	751	1
11	756	1
11	759	1
11	761	2
11	762	1
11	764	1
11	765	1
11	767	1
11	769	1
11	771	1
11	773	1
11	774	1
11	778	2
11	780	1
11	781	1
11	784	1
11	790	2
11	794	1
11	797	1
11	798	1
11	799	1
11	800	1
11	805	1
11	809	2
11	813	2
11	814	1
11	815	1
11	818	1
11	819	1
11	822	2
11	825	1
11	833	1
11	834	2
11	836	1
11	838	1
11	840	1
11	844	1
11	845	1
11	846	1
11	847	1
11	849	1
11	852	1
11	854	1
11	855	1
11	861	2
11	865	2
11	866	1
11	869	1
11	874	2
11	875	1
11	878	1
11	881	1
11	883	1
11	885	1
11	887	1
11	888	2
11	889	1
12	151	1
12	502	1
12	503	1
12	506	1
12	507	1
12	509	2
12	510	1
12	511	1
12	512	1
12	561	1
12	568	1
12	571	2
12	573	1
12	578	1
12	583	1
12	585	1
12	590	1
12	594	1
12	596	1
12	600	2
12	602	1
12	603	2
12	604	1
12	605	1
12	607	1
12	608	1
12	609	1
12	612	1
12	613	1
12	620	1
12	622	1
12	627	1
12	628	1
12	629	1
12	630	1
12	631	1
12	635	1
12	637	2
12	641	1
12	643	2
12	647	1
12	653	1
12	661	1
12	665	1
12	666	1
12	673	1
12	676	1
12	685	1
12	695	1
12	696	2
12	697	1
12	698	1
12	701	1
12	707	1
12	711	1
12	712	1
12	714	1
12	719	1
12	720	1
12	721	1
12	723	2
12	725	1
12	730	1
12	734	1
12	735	1
12	736	1
12	740	1
12	748	1
12	750	1
12	751	1
12	752	1
12	755	1
12	756	1
12	759	1
12	761	1
12	762	1
12	764	1
12	765	1
12	767	1
12	769	1
12	773	1
12	774	1
12	778	1
12	780	1
12	782	2
12	790	2
12	794	1
12	798	1
12	799	1
12	800	1
12	805	1
12	814	1
12	815	1
12	816	1
12	824	1
12	825	2
12	833	1
12	838	1
12	840	1
12	844	1
12	847	1
12	849	1
12	854	1
12	866	1
12	869	1
12	872	1
12	874	2
12	875	1
12	878	1
12	880	1
12	881	1
12	883	1
12	885	1
12	887	1
12	888	1
12	889	1
13	151	1
13	502	1
13	503	1
13	506	1
13	509	2
13	510	1
13	511	1
13	512	1
13	561	1
13	568	1
13	571	2
13	573	1
13	578	1
13	583	1
13	584	1
13	585	1
13	590	1
13	592	1
13	594	1
13	596	1
13	600	2
13	602	1
13	603	2
13	604	1
13	605	1
13	607	1
13	608	1
13	609	1
13	612	1
13	613	1
13	620	1
13	627	1
13	628	1
13	629	1
13	630	1
13	631	1
13	633	1
13	635	1
13	637	1
13	641	1
13	643	2
13	647	1
13	651	1
13	653	1
13	661	1
13	665	1
13	666	1
13	673	1
13	676	1
13	685	1
13	695	1
13	696	2
13	697	1
13	698	1
13	701	1
13	703	1
13	707	1
13	708	1
13	711	1
13	712	1
13	714	1
13	719	1
13	720	1
13	721	2
13	723	2
13	725	1
13	730	1
13	734	1
13	735	1
13	736	1
13	748	1
13	750	1
13	751	1
13	752	1
13	759	1
13	760	1
13	761	1
13	762	1
13	764	1
13	767	1
13	769	1
13	771	1
13	773	1
13	774	1
13	778	1
13	780	1
13	785	1
13	790	2
13	794	1
13	798	1
13	799	1
13	805	1
13	809	1
13	814	1
13	815	1
13	816	1
13	825	1
13	833	1
13	838	1
13	840	1
13	844	1
13	847	1
13	849	1
13	854	1
13	861	1
13	862	1
13	866	1
13	869	1
13	872	1
13	875	1
13	880	1
13	881	1
13	883	1
13	885	1
13	887	1
13	888	1
13	889	1
14	101	2
14	151	1
14	501	1
14	502	1
14	503	1
14	506	1
14	507	1
14	510	2
14	511	1
14	512	1
14	559	2
14	561	1
14	568	1
14	573	2
14	578	2
14	583	2
14	585	2
14	590	1
14	599	2
14	601	1
14	602	2
14	603	1
14	604	2
14	605	2
14	607	1
14	608	1
14	609	2
14	612	2
14	613	1
14	614	1
14	617	2
14	620	1
14	623	2
14	628	1
14	629	1
14	630	1
14	631	1
14	635	1
14	641	1
14	643	1
14	647	1
14	653	1
14	655	1
14	662	2
14	665	1
14	666	1
14	673	2
14	674	2
14	676	1
14	685	1
14	686	2
14	693	1
14	695	1
14	696	2
14	697	1
14	698	1
14	707	1
14	711	1
14	712	1
14	714	1
14	718	2
14	719	1
14	720	2
14	721	1
14	723	1
14	730	1
14	734	1
14	735	1
14	736	1
14	748	1
14	750	1
14	751	1
14	755	1
14	756	1
14	758	2
14	759	1
14	761	1
14	762	2
14	764	1
14	766	1
14	767	1
14	769	2
14	771	1
14	773	1
14	774	2
14	778	2
14	779	2
14	780	1
14	781	1
14	785	1
14	795	1
14	798	1
14	799	1
14	805	1
14	814	2
14	821	1
14	830	2
14	836	1
14	838	1
14	840	2
14	844	1
14	845	1
14	846	1
14	847	1
14	849	2
14	852	1
14	854	1
14	866	1
14	869	1
14	875	1
14	878	1
14	880	1
14	881	1
14	883	1
14	885	1
14	887	1
14	889	1
15	151	1
15	502	1
15	503	1
15	506	1
15	507	1
15	510	1
15	511	1
15	557	2
15	561	1
15	565	2
15	568	1
15	569	2
15	573	1
15	578	1
15	580	2
15	583	1
15	585	1
15	587	2
15	590	2
15	594	1
15	596	1
15	601	1
15	602	1
15	604	1
15	605	1
15	607	1
15	608	1
15	609	1
15	612	1
15	613	1
15	619	2
15	620	1
15	624	2
15	627	1
15	628	1
15	631	1
15	632	2
15	635	1
15	636	1
15	637	1
15	638	1
15	641	1
15	643	1
15	647	1
15	653	1
15	654	2
15	661	1
15	665	1
15	666	1
15	667	1
15	673	1
15	676	2
15	685	1
15	689	2
15	690	2
15	695	1
15	698	1
15	703	2
15	704	1
15	707	1
15	710	2
15	711	1
15	712	2
15	713	1
15	714	1
15	719	1
15	720	1
15	721	1
15	723	1
15	725	2
15	728	2
15	730	1
15	731	2
15	732	2
15	734	1
15	735	1
15	736	1
15	740	1
15	748	1
15	750	1
15	751	1
15	759	1
15	761	1
15	762	1
15	764	1
15	767	1
15	769	1
15	773	1
15	774	1
15	778	1
15	780	1
15	785	1
15	786	2
15	794	1
15	795	2
15	800	1
15	805	1
15	808	2
15	809	1
15	814	1
15	824	1
15	829	2
15	838	1
15	840	1
15	844	1
15	846	1
15	847	1
15	849	1
15	851	2
15	852	1
15	854	2
15	862	1
15	866	1
15	869	2
15	875	1
15	878	1
15	881	1
15	883	1
15	885	1
15	887	1
15	888	1
15	889	1
16	151	1
16	502	1
16	503	1
16	506	1
16	509	2
16	510	1
16	511	1
16	512	1
16	561	1
16	568	1
16	571	1
16	573	1
16	578	1
16	583	1
16	585	1
16	590	1
16	594	1
16	596	1
16	602	1
16	604	1
16	605	1
16	607	1
16	608	1
16	609	1
16	612	1
16	620	1
16	627	1
16	628	1
16	631	1
16	635	1
16	637	2
16	641	1
16	643	1
16	647	1
16	653	1
16	661	1
16	665	1
16	666	1
16	673	1
16	676	1
16	695	1
16	698	1
16	703	1
16	704	1
16	707	1
16	710	2
16	711	1
16	714	1
16	719	1
16	720	1
16	721	1
16	723	1
16	725	1
16	730	1
16	736	1
16	740	1
16	748	1
16	750	1
16	751	1
16	759	1
16	761	1
16	762	1
16	764	1
16	767	1
16	769	1
16	773	1
16	774	1
16	778	1
16	780	1
16	785	1
16	794	1
16	798	1
16	799	1
16	800	1
16	805	1
16	814	1
16	815	1
16	824	2
16	838	1
16	840	1
16	844	1
16	846	1
16	847	1
16	849	1
16	854	1
16	866	1
16	869	1
16	875	1
16	878	1
16	881	1
16	883	1
16	885	1
16	887	1
16	889	1
17	502	1
17	503	1
17	506	1
17	507	1
17	510	1
17	511	1
17	512	1
17	552	2
17	554	2
17	556	1
17	558	2
17	560	1
17	561	1
17	563	1
17	568	1
17	571	1
17	573	1
17	576	2
17	578	1
17	579	2
17	583	1
17	585	1
17	590	1
17	594	1
17	596	1
17	601	1
17	602	1
17	604	1
17	605	1
17	606	1
17	607	1
17	608	1
17	609	1
17	612	1
17	613	2
17	614	2
17	620	1
17	621	2
17	622	1
17	626	2
17	627	1
17	628	1
17	629	1
17	631	1
17	634	1
17	635	1
17	636	1
17	638	1
17	641	1
17	643	1
17	645	1
17	647	1
17	653	1
17	665	1
17	667	2
17	671	1
17	673	1
17	676	1
17	684	1
17	695	1
17	696	2
17	697	2
17	698	2
17	699	1
17	700	1
17	701	1
17	703	2
17	704	1
17	706	2
17	707	1
17	708	1
17	710	2
17	711	1
17	712	1
17	713	1
17	714	1
17	716	1
17	719	2
17	720	1
17	721	1
17	723	1
17	724	2
17	725	1
17	730	1
17	733	1
17	734	1
17	735	1
17	736	1
17	741	2
17	742	1
17	743	2
17	748	1
17	750	1
17	751	2
17	753	2
17	756	1
17	759	1
17	760	2
17	761	2
17	762	1
17	764	2
17	765	2
17	766	1
17	767	2
17	769	1
17	771	1
17	773	1
17	774	1
17	775	2
17	776	2
17	778	2
17	780	1
17	781	1
17	785	1
17	791	2
17	795	1
17	798	1
17	799	1
17	800	1
17	801	2
17	802	1
17	803	2
17	805	1
17	806	2
17	808	1
17	809	1
17	814	1
17	815	1
17	817	1
17	818	1
17	820	1
17	825	2
17	831	1
17	835	2
17	838	2
17	840	1
17	844	2
17	845	1
17	846	1
17	847	1
17	849	1
17	852	1
17	853	2
17	854	1
17	855	1
17	859	2
17	861	2
17	862	2
17	865	2
17	866	1
17	867	1
17	869	1
17	875	1
17	877	1
17	878	1
17	880	1
17	881	2
17	883	1
17	884	1
17	885	1
17	887	1
17	888	1
17	889	1
18	151	1
18	501	2
18	502	2
18	503	2
18	504	2
18	505	2
18	506	1
18	507	2
18	508	2
18	510	1
18	511	1
18	512	1
18	561	1
18	562	2
18	568	1
18	571	1
18	573	1
18	576	1
18	578	1
18	583	1
18	585	1
18	590	1
18	594	1
18	596	1
18	599	1
18	601	1
18	602	1
18	603	2
18	604	2
18	605	1
18	606	1
18	607	1
18	608	1
18	609	1
18	612	2
18	613	1
18	617	1
18	620	1
18	622	1
18	627	1
18	628	1
18	629	1
18	631	1
18	633	1
18	635	1
18	637	1
18	641	1
18	643	1
18	644	1
18	647	1
18	648	2
18	651	1
18	653	2
18	655	1
18	661	1
18	663	2
18	665	1
18	666	1
18	671	2
18	672	2
18	673	2
18	676	1
18	679	2
18	681	1
18	685	1
18	695	1
18	696	2
18	697	1
18	698	1
18	699	1
18	700	1
18	701	1
18	703	2
18	706	2
18	707	1
18	708	1
18	711	1
18	712	1
18	713	2
18	714	1
18	716	1
18	719	1
18	720	1
18	721	1
18	723	1
18	724	1
18	725	1
18	729	2
18	730	1
18	734	1
18	735	1
18	736	2
18	748	1
18	750	1
18	751	1
18	755	1
18	756	1
18	759	1
18	761	1
18	762	1
18	763	2
18	764	1
18	765	2
18	767	1
18	769	1
18	771	1
18	773	2
18	774	1
18	778	1
18	779	1
18	780	1
18	782	2
18	785	1
18	790	2
18	794	1
18	796	2
18	797	1
18	798	1
18	799	1
18	805	1
18	812	2
18	814	1
18	815	1
18	816	1
18	817	1
18	820	2
18	821	1
18	823	1
18	825	1
18	826	2
18	831	2
18	836	1
18	838	1
18	840	1
18	843	1
18	844	1
18	845	1
18	846	1
18	847	1
18	849	1
18	850	2
18	854	1
18	857	2
18	859	1
18	861	1
18	862	1
18	863	1
18	866	2
18	867	1
18	868	2
18	869	1
18	870	2
18	871	2
18	873	2
18	874	2
18	875	1
18	878	1
18	881	1
18	883	1
18	885	1
18	887	1
18	888	2
18	889	1
19	151	1
19	502	1
19	503	1
19	506	1
19	507	1
19	510	1
19	511	1
19	512	1
19	561	1
19	568	1
19	573	1
19	578	1
19	583	1
19	585	1
19	588	2
19	589	2
19	590	1
19	591	1
19	594	1
19	601	1
19	602	1
19	604	1
19	605	1
19	606	1
19	607	1
19	608	1
19	609	1
19	612	1
19	614	1
19	620	1
19	627	1
19	628	1
19	629	1
19	630	1
19	631	1
19	633	1
19	635	1
19	637	1
19	641	1
19	643	1
19	644	1
19	647	1
19	651	1
19	653	1
19	655	1
19	661	1
19	665	1
19	666	1
19	667	1
19	672	1
19	673	1
19	676	1
19	681	1
19	685	1
19	695	1
19	696	1
19	697	1
19	698	1
19	700	1
19	701	1
19	707	1
19	708	1
19	711	1
19	714	1
19	716	1
19	719	1
19	720	1
19	721	1
19	723	1
19	725	1
19	736	1
19	748	1
19	750	1
19	751	1
19	755	1
19	756	1
19	759	1
19	761	1
19	762	1
19	764	1
19	767	1
19	769	1
19	773	1
19	774	1
19	778	1
19	780	1
19	785	1
19	790	1
19	794	1
19	798	1
19	799	1
19	800	1
19	805	1
19	812	2
19	814	1
19	815	1
19	816	1
19	830	1
19	833	2
19	838	1
19	840	1
19	844	1
19	845	1
19	847	1
19	849	1
19	854	1
19	861	1
19	866	1
19	869	1
19	875	1
19	878	1
19	881	1
19	883	1
19	885	2
19	887	1
19	888	1
19	889	1
23	151	1
23	501	2
23	502	2
23	503	2
23	504	2
23	505	2
23	506	1
23	507	2
23	508	2
23	510	1
23	511	1
23	512	1
23	561	1
23	562	2
23	568	1
23	571	1
23	573	1
23	576	1
23	578	1
23	583	1
23	585	1
23	590	1
23	594	1
23	596	1
23	599	1
23	601	1
23	602	1
23	603	2
23	604	2
23	605	1
23	606	1
23	607	1
23	608	1
23	609	1
23	612	2
23	613	1
23	617	1
23	620	1
23	622	1
23	627	1
23	628	1
23	629	1
23	631	1
23	633	1
23	635	1
23	637	1
23	641	1
23	643	1
23	644	1
23	647	1
23	648	2
23	651	1
23	653	2
23	655	1
23	661	1
23	663	2
23	665	1
23	666	1
23	671	2
23	672	2
23	673	2
23	676	1
23	679	2
23	681	1
23	685	1
23	695	1
23	696	2
23	697	1
23	698	1
23	699	1
23	700	1
23	701	1
23	703	2
23	706	2
23	707	1
23	708	1
23	711	1
23	712	1
23	713	2
23	714	1
23	716	1
23	719	1
23	720	1
23	721	1
23	723	1
23	724	1
23	725	1
23	729	2
23	730	1
23	734	1
23	735	1
23	736	2
23	748	1
23	750	1
23	751	1
23	755	1
23	756	1
23	759	1
23	761	1
23	762	1
23	763	2
23	764	1
23	765	2
23	767	1
23	769	1
23	771	1
23	773	2
23	774	1
23	778	1
23	779	1
23	780	1
23	782	2
23	785	1
23	790	2
23	794	1
23	796	2
23	797	1
23	798	1
23	799	1
23	805	1
23	812	2
23	814	1
23	815	1
23	816	1
23	817	1
23	820	2
23	821	1
23	823	1
23	825	1
23	826	2
23	831	2
23	836	1
23	838	1
23	840	1
23	843	1
23	844	1
23	845	1
23	846	1
23	847	1
23	849	1
23	850	2
23	854	1
23	857	2
23	859	1
23	861	1
23	862	1
23	863	1
23	866	2
23	867	1
23	868	2
23	869	1
23	870	2
23	871	2
23	873	2
23	874	2
23	875	1
23	878	1
23	881	1
23	883	1
23	885	1
23	887	1
23	888	2
23	889	1
24	151	1
24	501	2
24	502	2
24	503	2
24	504	2
24	505	2
24	506	1
24	507	2
24	508	2
24	510	1
24	511	1
24	512	1
24	561	1
24	562	2
24	568	1
24	571	1
24	573	1
24	576	1
24	578	1
24	583	1
24	585	1
24	590	1
24	594	1
24	596	1
24	599	1
24	601	1
24	602	1
24	603	2
24	604	2
24	605	1
24	606	1
24	607	1
24	608	1
24	609	1
24	612	2
24	613	1
24	617	1
24	620	1
24	622	1
24	627	1
24	628	1
24	629	1
24	631	1
24	633	1
24	635	1
24	637	1
24	641	1
24	643	1
24	644	1
24	647	1
24	648	2
24	651	1
24	653	2
24	655	1
24	661	1
24	663	2
24	665	1
24	666	1
24	671	2
24	672	2
24	673	2
24	676	1
24	679	2
24	681	1
24	685	1
24	695	1
24	696	2
24	697	1
24	698	1
24	699	1
24	700	1
24	701	1
24	703	2
24	706	2
24	707	1
24	708	1
24	711	1
24	712	1
24	713	2
24	714	1
24	716	1
24	719	1
24	720	1
24	721	1
24	723	1
24	724	1
24	725	1
24	729	2
24	730	1
24	734	1
24	735	1
24	736	2
24	748	1
24	750	1
24	751	1
24	755	1
24	756	1
24	759	1
24	761	1
24	762	1
24	763	2
24	764	1
24	765	2
24	767	1
24	769	1
24	771	1
24	773	2
24	774	1
24	778	1
24	779	1
24	780	1
24	782	2
24	785	1
24	790	2
24	794	1
24	796	2
24	797	1
24	798	1
24	799	1
24	805	1
24	812	2
24	814	1
24	815	1
24	816	1
24	817	1
24	820	2
24	821	1
24	823	1
24	825	1
24	826	2
24	831	2
24	836	1
24	838	1
24	840	1
24	843	1
24	844	1
24	845	1
24	846	1
24	847	1
24	849	1
24	850	2
24	854	1
24	857	2
24	859	1
24	861	1
24	862	1
24	863	1
24	866	2
24	867	1
24	868	2
24	869	1
24	870	2
24	871	2
24	873	2
24	874	2
24	875	1
24	878	1
24	881	1
24	883	1
24	885	1
24	887	1
24	888	2
24	889	1
25	151	1
25	501	2
25	502	2
25	503	2
25	504	2
25	505	2
25	506	1
25	507	2
25	508	2
25	510	1
25	511	1
25	512	1
25	561	1
25	562	2
25	568	1
25	571	1
25	573	1
25	576	1
25	578	1
25	583	1
25	585	1
25	590	1
25	594	1
25	596	1
25	599	1
25	601	1
25	602	1
25	603	2
25	604	2
25	605	1
25	606	1
25	607	1
25	608	1
25	609	1
25	612	2
25	613	1
25	617	1
25	620	1
25	622	1
25	627	1
25	628	1
25	629	1
25	631	1
25	633	1
25	635	1
25	637	1
25	641	1
25	643	1
25	644	1
25	647	1
25	648	2
25	651	1
25	653	2
25	655	1
25	661	1
25	663	2
25	665	1
25	666	1
25	671	2
25	672	2
25	673	2
25	676	1
25	679	2
25	681	1
25	685	1
25	695	1
25	696	2
25	697	1
25	698	1
25	699	1
25	700	1
25	701	1
25	703	2
25	706	2
25	707	1
25	708	1
25	711	1
25	712	1
25	713	2
25	714	1
25	716	1
25	719	1
25	720	1
25	721	1
25	723	1
25	724	1
25	725	1
25	729	2
25	730	1
25	734	1
25	735	1
25	736	2
25	748	1
25	750	1
25	751	1
25	755	1
25	756	1
25	759	1
25	761	1
25	762	1
25	763	2
25	764	1
25	765	2
25	767	1
25	769	1
25	771	1
25	773	2
25	774	1
25	778	1
25	779	1
25	780	1
25	782	2
25	785	1
25	790	2
25	794	1
25	796	2
25	797	1
25	798	1
25	799	1
25	805	1
25	812	2
25	814	1
25	815	1
25	816	1
25	817	1
25	820	2
25	821	1
25	823	1
25	825	1
25	826	2
25	831	2
25	836	1
25	838	1
25	840	1
25	843	1
25	844	1
25	845	1
25	846	1
25	847	1
25	849	1
25	850	2
25	854	1
25	857	2
25	859	1
25	861	1
25	862	1
25	863	1
25	866	2
25	867	1
25	868	2
25	869	1
25	870	2
25	871	2
25	873	2
25	874	2
25	875	1
25	878	1
25	881	1
25	883	1
25	885	1
25	887	1
25	888	2
25	889	1
26	151	1
26	501	2
26	502	2
26	503	2
26	504	2
26	505	2
26	506	1
26	507	2
26	508	2
26	510	1
26	511	1
26	512	1
26	561	1
26	562	2
26	568	1
26	571	1
26	573	1
26	576	1
26	578	1
26	583	1
26	585	1
26	590	1
26	594	1
26	596	1
26	599	1
26	601	1
26	602	1
26	603	2
26	604	2
26	605	1
26	606	1
26	607	1
26	608	1
26	609	1
26	612	2
26	613	1
26	617	1
26	620	1
26	622	1
26	627	1
26	628	1
26	629	1
26	631	1
26	633	1
26	635	1
26	637	1
26	641	1
26	643	1
26	644	1
26	647	1
26	648	2
26	651	1
26	653	2
26	655	1
26	661	1
26	663	2
26	665	1
26	666	1
26	671	2
26	672	2
26	673	2
26	676	1
26	679	2
26	681	1
26	685	1
26	695	1
26	696	2
26	697	1
26	698	1
26	699	1
26	700	1
26	701	1
26	703	2
26	706	2
26	707	1
26	708	1
26	711	1
26	712	1
26	713	2
26	714	1
26	716	1
26	719	1
26	720	1
26	721	1
26	723	1
26	724	1
26	725	1
26	729	2
26	730	1
26	734	1
26	735	1
26	736	2
26	748	1
26	750	1
26	751	1
26	755	1
26	756	1
26	759	1
26	761	1
26	762	1
26	763	2
26	764	1
26	765	2
26	767	1
26	769	1
26	771	1
26	773	2
26	774	1
26	778	1
26	779	1
26	780	1
26	782	2
26	785	1
26	790	2
26	794	1
26	796	2
26	797	1
26	798	1
26	799	1
26	805	1
26	812	2
26	814	1
26	815	1
26	816	1
26	817	1
26	820	2
26	821	1
26	823	1
26	825	1
26	826	2
26	831	2
26	836	1
26	838	1
26	840	1
26	843	1
26	844	1
26	845	1
26	846	1
26	847	1
26	849	1
26	850	2
26	854	1
26	857	2
26	859	1
26	861	1
26	862	1
26	863	1
26	866	2
26	867	1
26	868	2
26	869	1
26	870	2
26	871	2
26	873	2
26	874	2
26	875	1
26	878	1
26	881	1
26	883	1
26	885	1
26	887	1
26	888	2
26	889	1
27	151	1
27	501	2
27	502	2
27	503	2
27	504	2
27	505	2
27	506	1
27	507	2
27	508	2
27	510	1
27	511	1
27	512	1
27	561	1
27	562	2
27	568	1
27	571	1
27	573	1
27	576	1
27	578	1
27	583	1
27	585	1
27	590	1
27	594	1
27	596	1
27	599	1
27	601	1
27	602	1
27	603	2
27	604	2
27	605	1
27	606	1
27	607	1
27	608	1
27	609	1
27	612	2
27	613	1
27	617	1
27	620	1
27	622	1
27	627	1
27	628	1
27	629	1
27	631	1
27	633	1
27	635	1
27	637	1
27	641	1
27	643	1
27	644	1
27	647	1
27	648	2
27	651	1
27	653	2
27	655	1
27	661	1
27	663	2
27	665	1
27	666	1
27	671	2
27	672	2
27	673	2
27	676	1
27	679	2
27	681	1
27	685	1
27	695	1
27	696	2
27	697	1
27	698	1
27	699	1
27	700	1
27	701	1
27	703	2
27	706	2
27	707	1
27	708	1
27	711	1
27	712	1
27	713	2
27	714	1
27	716	1
27	719	1
27	720	1
27	721	1
27	723	1
27	724	1
27	725	1
27	729	2
27	730	1
27	734	1
27	735	1
27	736	2
27	748	1
27	750	1
27	751	1
27	755	1
27	756	1
27	759	1
27	761	1
27	762	1
27	763	2
27	764	1
27	765	2
27	767	1
27	769	1
27	771	1
27	773	2
27	774	1
27	778	1
27	779	1
27	780	1
27	782	2
27	785	1
27	790	2
27	794	1
27	796	2
27	797	1
27	798	1
27	799	1
27	805	1
27	812	2
27	814	1
27	815	1
27	816	1
27	817	1
27	820	2
27	821	1
27	823	1
27	825	1
27	826	2
27	831	2
27	836	1
27	838	1
27	840	1
27	843	1
27	844	1
27	845	1
27	846	1
27	847	1
27	849	1
27	850	2
27	854	1
27	857	2
27	859	1
27	861	1
27	862	1
27	863	1
27	866	2
27	867	1
27	868	2
27	869	1
27	870	2
27	871	2
27	873	2
27	874	2
27	875	1
27	878	1
27	881	1
27	883	1
27	885	1
27	887	1
27	888	2
27	889	1
28	151	1
28	501	2
28	502	2
28	503	2
28	504	2
28	505	2
28	506	1
28	507	2
28	508	2
28	510	1
28	511	1
28	512	1
28	561	1
28	562	2
28	568	1
28	571	1
28	573	1
28	576	1
28	578	1
28	583	1
28	585	1
28	590	1
28	594	1
28	596	1
28	599	1
28	601	1
28	602	1
28	603	2
28	604	2
28	605	1
28	606	1
28	607	1
28	608	1
28	609	1
28	612	2
28	613	1
28	617	1
28	620	1
28	622	1
28	627	1
28	628	1
28	629	1
28	631	1
28	633	1
28	635	1
28	637	1
28	641	1
28	643	1
28	644	1
28	647	1
28	648	2
28	651	1
28	653	2
28	655	1
28	661	1
28	663	2
28	665	1
28	666	1
28	671	2
28	672	2
28	673	2
28	676	1
28	679	2
28	681	1
28	685	1
28	695	1
28	696	2
28	697	1
28	698	1
28	699	1
28	700	1
28	701	1
28	703	2
28	706	2
28	707	1
28	708	1
28	711	1
28	712	1
28	713	2
28	714	1
28	716	1
28	719	1
28	720	1
28	721	1
28	723	1
28	724	1
28	725	1
28	729	2
28	730	1
28	734	1
28	735	1
28	736	2
28	748	1
28	750	1
28	751	1
28	755	1
28	756	1
28	759	1
28	761	1
28	762	1
28	763	2
28	764	1
28	765	2
28	767	1
28	769	1
28	771	1
28	773	2
28	774	1
28	778	1
28	779	1
28	780	1
28	782	2
28	785	1
28	790	2
28	794	1
28	796	2
28	797	1
28	798	1
28	799	1
28	805	1
28	812	2
28	814	1
28	815	1
28	816	1
28	817	1
28	820	2
28	821	1
28	823	1
28	825	1
28	826	2
28	831	2
28	836	1
28	838	1
28	840	1
28	843	1
28	844	1
28	845	1
28	846	1
28	847	1
28	849	1
28	850	2
28	854	1
28	857	2
28	859	1
28	861	1
28	862	1
28	863	1
28	866	2
28	867	1
28	868	2
28	869	1
28	870	2
28	871	2
28	873	2
28	874	2
28	875	1
28	878	1
28	881	1
28	883	1
28	885	1
28	887	1
28	888	2
28	889	1
29	151	1
29	501	2
29	502	2
29	503	2
29	504	2
29	505	2
29	506	1
29	507	2
29	508	2
29	510	1
29	511	1
29	512	1
29	561	1
29	562	2
29	568	1
29	571	1
29	573	1
29	576	1
29	578	1
29	583	1
29	585	1
29	590	1
29	594	1
29	596	1
29	599	1
29	601	1
29	602	1
29	603	2
29	604	2
29	605	1
29	606	1
29	607	1
29	608	1
29	609	1
29	612	2
29	613	1
29	617	1
29	620	1
29	622	1
29	627	1
29	628	1
29	629	1
29	631	1
29	633	1
29	635	1
29	637	1
29	641	1
29	643	1
29	644	1
29	647	1
29	648	2
29	651	1
29	653	2
29	655	1
29	661	1
29	663	2
29	665	1
29	666	1
29	671	2
29	672	2
29	673	2
29	676	1
29	679	2
29	681	1
29	685	1
29	695	1
29	696	2
29	697	1
29	698	1
29	699	1
29	700	1
29	701	1
29	703	2
29	706	2
29	707	1
29	708	1
29	711	1
29	712	1
29	713	2
29	714	1
29	716	1
29	719	1
29	720	1
29	721	1
29	723	1
29	724	1
29	725	1
29	729	2
29	730	1
29	734	1
29	735	1
29	736	2
29	748	1
29	750	1
29	751	1
29	755	1
29	756	1
29	759	1
29	761	1
29	762	1
29	763	2
29	764	1
29	765	2
29	767	1
29	769	1
29	771	1
29	773	2
29	774	1
29	778	1
29	779	1
29	780	1
29	782	2
29	785	1
29	790	2
29	794	1
29	796	2
29	797	1
29	798	1
29	799	1
29	805	1
29	812	2
29	814	1
29	815	1
29	816	1
29	817	1
29	820	2
29	821	1
29	823	1
29	825	1
29	826	2
29	831	2
29	836	1
29	838	1
29	840	1
29	843	1
29	844	1
29	845	1
29	846	1
29	847	1
29	849	1
29	850	2
29	854	1
29	857	2
29	859	1
29	861	1
29	862	1
29	863	1
29	866	2
29	867	1
29	868	2
29	869	1
29	870	2
29	871	2
29	873	2
29	874	2
29	875	1
29	878	1
29	881	1
29	883	1
29	885	1
29	887	1
29	888	2
29	889	1
30	151	1
30	501	2
30	502	2
30	503	2
30	504	2
30	505	2
30	506	1
30	507	2
30	508	2
30	510	1
30	511	1
30	512	1
30	561	1
30	562	2
30	568	1
30	571	1
30	573	1
30	576	1
30	578	1
30	583	1
30	585	1
30	590	1
30	594	1
30	596	1
30	599	1
30	601	1
30	602	1
30	603	2
30	604	2
30	605	1
30	606	1
30	607	1
30	608	1
30	609	1
30	612	2
30	613	1
30	617	1
30	620	1
30	622	1
30	627	1
30	628	1
30	629	1
30	631	1
30	633	1
30	635	1
30	637	1
30	641	1
30	643	1
30	644	1
30	647	1
30	648	2
30	651	1
30	653	2
30	655	1
30	661	1
30	663	2
30	665	1
30	666	1
30	671	2
30	672	2
30	673	2
30	676	1
30	679	2
30	681	1
30	685	1
30	695	1
30	696	2
30	697	1
30	698	1
30	699	1
30	700	1
30	701	1
30	703	2
30	706	2
30	707	1
30	708	1
30	711	1
30	712	1
30	713	2
30	714	1
30	716	1
30	719	1
30	720	1
30	721	1
30	723	1
30	724	1
30	725	1
30	729	2
30	730	1
30	734	1
30	735	1
30	736	2
30	748	1
30	750	1
30	751	1
30	755	1
30	756	1
30	759	1
30	761	1
30	762	1
30	763	2
30	764	1
30	765	2
30	767	1
30	769	1
30	771	1
30	773	2
30	774	1
30	778	1
30	779	1
30	780	1
30	782	2
30	785	1
30	790	2
30	794	1
30	796	2
30	797	1
30	798	1
30	799	1
30	805	1
30	812	2
30	814	1
30	815	1
30	816	1
30	817	1
30	820	2
30	821	1
30	823	1
30	825	1
30	826	2
30	831	2
30	836	1
30	838	1
30	840	1
30	843	1
30	844	1
30	845	1
30	846	1
30	847	1
30	849	1
30	850	2
30	854	1
30	857	2
30	859	1
30	861	1
30	862	1
30	863	1
30	866	2
30	867	1
30	868	2
30	869	1
30	870	2
30	871	2
30	873	2
30	874	2
30	875	1
30	878	1
30	881	1
30	883	1
30	885	1
30	887	1
30	888	2
30	889	1
38	501	1
38	502	1
38	503	1
38	504	1
38	505	1
38	506	1
38	507	1
38	508	1
38	509	1
38	510	1
38	511	1
38	512	1
38	551	1
38	552	1
38	553	1
38	554	1
38	555	1
38	556	1
38	557	1
38	558	1
38	559	1
38	560	1
38	561	1
38	562	1
38	563	1
38	564	1
38	565	1
38	566	1
38	568	1
38	569	1
38	570	1
38	571	1
38	573	1
38	574	1
38	575	1
38	576	1
38	578	1
38	579	1
38	580	1
38	581	1
38	582	1
38	583	1
38	584	1
38	585	1
38	587	1
38	588	1
38	589	1
38	590	1
38	591	1
38	592	1
38	593	1
38	594	1
38	595	1
38	596	1
38	598	1
38	599	1
38	600	1
38	601	1
38	602	1
38	603	1
38	604	1
38	605	1
38	606	1
38	607	1
38	608	1
38	609	1
38	610	1
38	611	1
38	612	1
38	613	1
38	614	1
38	616	1
38	617	1
38	618	1
38	619	1
38	620	1
38	621	1
38	622	1
38	623	1
38	624	1
38	625	1
38	626	1
38	627	1
38	628	1
38	629	1
38	630	1
38	631	1
38	632	1
38	633	1
38	634	1
38	635	1
38	636	1
38	637	1
38	638	1
38	641	1
38	642	1
38	643	1
38	644	1
38	645	1
38	646	1
38	647	1
38	648	1
38	649	1
38	651	1
38	652	1
38	653	1
38	654	1
38	655	1
38	658	1
38	659	1
38	660	1
38	661	1
38	662	1
38	663	1
38	664	1
38	665	1
38	666	1
38	667	1
38	668	1
38	669	1
38	670	1
38	671	1
38	672	1
38	673	1
38	674	1
38	675	1
38	676	1
38	678	1
38	679	1
38	680	1
38	681	1
38	682	1
38	684	1
38	685	1
38	686	1
38	687	1
38	688	1
38	689	1
38	690	1
38	691	1
38	692	1
38	693	1
38	694	1
38	695	1
38	696	1
38	697	1
38	698	1
38	699	1
38	700	1
38	701	1
38	702	1
38	703	1
38	704	1
38	706	1
38	707	1
38	708	1
38	709	1
38	710	1
38	711	1
38	712	1
38	713	1
38	714	1
38	716	1
38	718	1
38	719	1
38	720	1
38	721	1
38	722	1
38	723	1
38	724	1
38	725	1
38	726	1
38	727	1
38	728	1
38	729	1
38	730	1
38	731	1
38	732	1
38	733	1
38	734	1
38	735	1
38	736	1
38	737	1
38	739	1
38	740	1
38	741	1
38	742	1
38	743	1
38	748	1
38	749	1
38	750	1
38	751	1
38	752	1
38	753	1
38	754	1
38	755	1
38	756	1
38	757	1
38	758	1
38	759	1
38	760	1
38	761	1
38	762	1
38	763	1
38	764	1
38	765	1
38	766	1
38	767	1
38	769	1
38	771	1
38	772	1
38	773	1
38	774	1
38	775	1
38	776	1
38	778	1
38	779	1
38	780	1
38	781	1
38	782	1
38	783	1
38	784	1
38	785	1
38	786	1
38	788	1
38	789	1
38	790	1
38	791	1
38	792	1
38	793	1
38	794	1
38	795	1
38	796	1
38	797	1
38	798	1
38	799	1
38	800	1
38	801	1
38	802	1
38	803	1
38	804	1
38	805	1
38	806	1
38	808	1
38	809	1
38	810	1
38	811	1
38	812	1
38	813	1
38	814	1
38	815	1
38	816	1
38	817	1
38	818	1
38	819	1
38	820	1
38	821	1
38	822	1
38	823	1
38	824	1
38	825	1
38	826	1
38	827	1
38	828	1
38	829	1
38	830	1
38	831	1
38	832	1
38	833	1
38	834	1
38	835	1
38	836	1
38	837	1
38	838	1
38	839	1
38	840	1
38	841	1
38	843	1
38	844	1
38	845	1
38	846	1
38	847	1
38	849	1
38	850	1
38	851	1
38	852	1
38	853	1
38	854	1
38	855	1
38	857	1
38	858	1
38	859	1
38	861	1
38	862	1
38	863	1
38	865	1
38	866	1
38	867	1
38	868	1
38	869	1
38	870	1
38	871	1
38	872	1
38	873	1
38	874	1
38	875	1
38	877	1
38	878	1
38	879	1
38	880	1
38	881	1
38	882	1
38	883	1
38	884	1
38	885	1
38	887	1
38	888	1
38	889	1
40	151	1
40	502	1
40	503	1
40	506	1
40	507	1
40	508	1
40	510	1
40	511	1
40	512	1
40	559	2
40	561	1
40	568	1
40	573	1
40	578	1
40	583	2
40	585	1
40	590	1
40	599	2
40	601	1
40	602	1
40	603	1
40	604	1
40	605	1
40	607	1
40	608	1
40	609	2
40	612	1
40	617	1
40	620	1
40	627	1
40	628	1
40	629	1
40	630	1
40	631	1
40	635	1
40	637	1
40	641	1
40	643	1
40	647	1
40	651	1
40	653	1
40	655	1
40	665	1
40	666	1
40	673	1
40	674	2
40	676	1
40	681	1
40	685	1
40	695	1
40	697	1
40	698	1
40	707	1
40	708	1
40	711	1
40	716	1
40	719	1
40	720	1
40	721	1
40	723	1
40	725	1
40	730	1
40	734	1
40	735	1
40	736	1
40	748	1
40	750	1
40	751	1
40	759	1
40	761	1
40	762	1
40	767	1
40	769	1
40	771	1
40	773	1
40	774	1
40	778	1
40	780	1
40	785	1
40	794	1
40	805	1
40	812	1
40	814	1
40	815	1
40	838	1
40	840	1
40	844	1
40	845	1
40	847	1
40	849	1
40	854	1
40	866	1
40	869	1
40	874	2
40	875	1
40	878	1
40	881	1
40	883	1
40	885	1
40	887	1
40	888	1
40	889	1
41	151	1
41	501	2
41	502	2
41	503	2
41	504	2
41	505	2
41	506	1
41	507	2
41	508	2
41	510	1
41	511	1
41	512	1
41	561	1
41	562	2
41	568	1
41	571	1
41	573	1
41	576	1
41	578	1
41	583	1
41	585	1
41	590	1
41	594	1
41	596	1
41	599	1
41	601	1
41	602	1
41	603	2
41	604	2
41	605	1
41	606	1
41	607	1
41	608	1
41	609	1
41	612	2
41	613	1
41	617	1
41	620	1
41	622	1
41	627	1
41	628	1
41	629	1
41	631	1
41	633	1
41	635	1
41	637	1
41	641	1
41	643	1
41	644	1
41	647	1
41	648	2
41	651	1
41	653	2
41	655	1
41	661	1
41	663	2
41	665	1
41	666	1
41	671	2
41	672	2
41	673	2
41	676	1
41	679	2
41	681	1
41	685	1
41	695	1
41	696	2
41	697	1
41	698	1
41	699	1
41	700	1
41	701	1
41	703	2
41	706	2
41	707	1
41	708	1
41	711	1
41	712	1
41	713	2
41	714	1
41	716	1
41	719	1
41	720	1
41	721	1
41	723	1
41	724	1
41	725	1
41	729	2
41	730	1
41	734	1
41	735	1
41	736	2
41	748	1
41	750	1
41	751	1
41	755	1
41	756	1
41	759	1
41	761	1
41	762	1
41	763	2
41	764	1
41	765	2
41	767	1
41	769	1
41	771	1
41	773	2
41	774	1
41	778	1
41	779	1
41	780	1
41	782	2
41	785	1
41	790	2
41	794	1
41	796	2
41	797	1
41	798	1
41	799	1
41	805	1
41	812	2
41	814	1
41	815	1
41	816	1
41	817	1
41	820	2
41	821	1
41	823	1
41	825	1
41	826	2
41	831	2
41	836	1
41	838	1
41	840	1
41	843	1
41	844	1
41	845	1
41	846	1
41	847	1
41	849	1
41	850	2
41	854	1
41	857	2
41	859	1
41	861	1
41	862	1
41	863	1
41	866	2
41	867	1
41	868	2
41	869	1
41	870	2
41	871	2
41	873	2
41	874	2
41	875	1
41	878	1
41	881	1
41	883	1
41	885	1
41	887	1
41	888	2
41	889	1
99	501	1
99	502	1
99	503	1
99	504	1
99	505	1
99	506	1
99	507	1
99	508	1
99	509	1
99	510	1
99	511	1
99	512	1
99	551	1
99	552	1
99	553	1
99	554	1
99	555	1
99	556	1
99	557	1
99	558	1
99	559	1
99	560	1
99	561	1
99	562	1
99	563	1
99	564	1
99	565	1
99	566	1
99	568	1
99	569	1
99	570	1
99	571	1
99	573	1
99	574	1
99	575	1
99	576	1
99	578	1
99	579	1
99	580	1
99	581	1
99	582	1
99	583	1
99	584	1
99	585	1
99	587	1
99	588	1
99	589	1
99	590	1
99	591	1
99	592	1
99	593	1
99	594	1
99	595	1
99	596	1
99	598	1
99	599	1
99	600	1
99	601	1
99	602	1
99	603	1
99	604	1
99	605	1
99	606	1
99	607	1
99	608	1
99	609	1
99	610	1
99	611	1
99	612	1
99	613	1
99	614	1
99	616	1
99	617	1
99	618	1
99	619	1
99	620	1
99	621	1
99	622	1
99	623	1
99	624	1
99	625	1
99	626	1
99	627	1
99	628	1
99	629	1
99	630	1
99	631	1
99	632	1
99	633	1
99	634	1
99	635	1
99	636	1
99	637	1
99	638	1
99	641	1
99	642	1
99	643	1
99	644	1
99	645	1
99	646	1
99	647	1
99	648	1
99	649	1
99	651	1
99	652	1
99	653	1
99	654	1
99	655	1
99	658	1
99	659	1
99	660	1
99	661	1
99	662	1
99	663	1
99	664	1
99	665	1
99	666	1
99	667	1
99	668	1
99	669	1
99	670	1
99	671	1
99	672	1
99	673	1
99	674	1
99	675	1
99	676	1
99	678	1
99	679	1
99	680	1
99	681	1
99	682	1
99	684	1
99	685	1
99	686	1
99	687	1
99	688	1
99	689	1
99	690	1
99	691	1
99	692	1
99	693	1
99	694	1
99	695	1
99	696	1
99	697	1
99	698	1
99	699	1
99	700	1
99	701	1
99	702	1
99	703	1
99	704	1
99	706	1
99	707	1
99	708	1
99	709	1
99	710	1
99	711	1
99	712	1
99	713	1
99	714	1
99	716	1
99	718	1
99	719	1
99	720	1
99	721	1
99	722	1
99	723	1
99	724	1
99	725	1
99	726	1
99	727	1
99	728	1
99	729	1
99	730	1
99	731	1
99	732	1
99	733	1
99	734	1
99	735	1
99	736	1
99	737	1
99	739	1
99	740	1
99	741	1
99	742	1
99	743	1
99	748	1
99	749	1
99	750	1
99	751	1
99	752	1
99	753	1
99	754	1
99	755	1
99	756	1
99	757	1
99	758	1
99	759	1
99	760	1
99	761	1
99	762	1
99	763	1
99	764	1
99	765	1
99	766	1
99	767	1
99	769	1
99	771	1
99	772	1
99	773	1
99	774	1
99	775	1
99	776	1
99	778	1
99	779	1
99	780	1
99	781	1
99	782	1
99	783	1
99	784	1
99	785	1
99	786	1
99	788	1
99	789	1
99	790	1
99	791	1
99	792	1
99	793	1
99	794	1
99	795	1
99	796	1
99	797	1
99	798	1
99	799	1
99	800	1
99	801	1
99	802	1
99	803	1
99	804	1
99	805	1
99	806	1
99	808	1
99	809	1
99	810	1
99	811	1
99	812	1
99	813	1
99	814	1
99	815	1
99	816	1
99	817	1
99	818	1
99	819	1
99	820	1
99	821	1
99	822	1
99	823	1
99	824	1
99	825	1
99	826	1
99	827	1
99	828	1
99	829	1
99	830	1
99	831	1
99	832	1
99	833	1
99	834	1
99	835	1
99	836	1
99	837	1
99	838	1
99	839	1
99	840	1
99	841	1
99	843	1
99	844	1
99	845	1
99	846	1
99	847	1
99	849	1
99	850	1
99	851	1
99	852	1
99	853	1
99	854	1
99	855	1
99	857	1
99	858	1
99	859	1
99	861	1
99	862	1
99	863	1
99	865	1
99	866	1
99	867	1
99	868	1
99	869	1
99	870	1
99	871	1
99	872	1
99	873	1
99	874	1
99	875	1
99	877	1
99	878	1
99	879	1
99	880	1
99	881	1
99	882	1
99	883	1
99	884	1
99	885	1
99	887	1
99	888	1
99	889	1
0	1751	1
-1	1751	1
4	1751	2
1	1751	2
2	1751	2
3	1751	2
9	1751	1
10	1751	2
0	1801	1
-1	1801	1
-1	1851	1
0	1851	1
0	1901	1
-1	1901	1
4	1901	1
3	1901	1
14	1901	2
2	1901	1
8	1901	2
1	1901	1
14	1751	2
-1	1951	1
0	1951	1
\.


--
-- Data for Name: volunteer_session; Type: TABLE DATA; Schema: public; Owner: staffing
--

COPY public.volunteer_session (comment, finish, locked, start, tokens, worked, sessionid, volunteerid, areaid) FROM stdin;
\N	\N	f	\N	0	f	0	501	27
\N	\N	f	\N	0	f	0	502	25
\N	\N	f	\N	0	f	0	504	30
\N	\N	f	\N	0	f	0	505	38
\N	\N	f	\N	0	f	0	506	38
\N	\N	f	\N	0	f	0	507	30
\N	\N	f	\N	0	f	0	508	30
\N	\N	f	\N	0	f	0	511	38
\N	\N	f	\N	0	f	0	512	38
\N	\N	f	\N	0	f	0	583	14
\N	\N	f	\N	0	f	0	585	38
\N	\N	f	\N	0	f	0	588	38
\N	\N	f	\N	0	f	0	589	38
\N	\N	f	\N	0	f	0	594	38
\N	\N	f	\N	0	f	0	612	38
\N	\N	f	\N	0	f	0	642	9
\N	\N	f	\N	0	f	0	648	28
\N	\N	f	\N	0	f	0	653	28
\N	\N	f	\N	0	f	0	671	26
\N	\N	f	\N	0	f	0	672	30
\N	\N	f	\N	0	f	0	674	38
\N	\N	f	\N	0	f	0	678	38
\N	\N	f	\N	0	f	0	679	30
\N	\N	f	\N	0	f	0	681	38
\N	\N	f	\N	0	f	0	684	38
\N	\N	f	\N	0	f	0	693	38
\N	\N	f	\N	0	f	0	706	8
\N	\N	f	\N	0	f	0	707	0
\N	\N	f	\N	0	f	0	748	38
\N	\N	f	\N	0	f	0	750	38
\N	\N	f	\N	0	f	0	753	8
\N	\N	f	\N	0	f	0	759	38
\N	\N	f	\N	0	f	0	812	38
\N	\N	f	\N	0	f	0	844	9
\N	\N	f	\N	0	f	0	850	23
\N	\N	f	\N	0	f	0	857	30
\N	\N	f	\N	0	f	0	863	38
\N	\N	f	\N	0	f	0	866	28
\N	\N	f	\N	0	f	0	868	30
\N	\N	f	\N	0	f	0	870	30
\N	\N	f	\N	0	f	0	871	30
\N	\N	f	\N	0	f	0	873	30
\N	\N	f	\N	0	f	0	874	99
\N	\N	f	\N	0	f	0	878	30
\N	\N	f	\N	0	f	1	501	27
\N	\N	f	\N	0	f	1	502	25
\N	\N	f	\N	0	f	1	504	30
\N	\N	f	\N	0	f	1	505	38
\N	\N	f	\N	0	f	1	506	38
\N	\N	f	\N	0	f	1	507	30
\N	\N	f	\N	0	f	1	508	30
\N	\N	f	\N	0	f	1	511	38
\N	\N	f	\N	0	f	1	512	30
\N	\N	f	\N	0	f	1	583	38
\N	\N	f	\N	0	f	1	585	38
\N	\N	f	\N	0	f	1	642	38
\N	\N	f	\N	0	f	1	648	38
\N	\N	f	\N	0	f	1	651	38
\N	\N	f	\N	0	f	1	653	28
\N	\N	f	\N	0	f	1	661	38
\N	\N	f	\N	0	f	1	671	38
\N	\N	f	\N	0	f	1	672	30
\N	\N	f	\N	0	f	1	673	38
\N	\N	f	\N	0	f	1	674	38
\N	\N	f	\N	0	f	1	679	30
\N	\N	f	\N	0	f	1	681	38
\N	\N	f	\N	0	f	1	684	38
\N	\N	f	\N	0	f	1	693	38
\N	\N	f	\N	0	f	1	699	38
\N	\N	f	\N	0	f	1	700	38
\N	\N	f	\N	0	f	1	706	8
\N	\N	f	\N	0	f	1	707	0
\N	\N	f	\N	0	f	1	734	38
\N	\N	f	\N	0	f	1	735	38
\N	\N	f	\N	0	f	1	748	38
\N	\N	f	\N	0	f	1	750	38
\N	\N	f	\N	0	f	1	753	8
\N	\N	f	\N	0	f	1	759	38
\N	\N	f	\N	0	f	1	812	38
\N	\N	f	\N	0	f	1	844	8
\N	\N	f	\N	0	f	1	846	38
\N	\N	f	\N	0	f	1	857	30
\N	\N	f	\N	0	f	1	863	38
\N	\N	f	\N	0	f	1	866	28
\N	\N	f	\N	0	f	1	868	30
\N	\N	f	\N	0	f	1	870	30
\N	\N	f	\N	0	f	1	871	30
\N	\N	f	\N	0	f	1	873	30
\N	\N	f	\N	0	f	1	874	99
\N	\N	f	\N	0	f	1	875	38
\N	\N	f	\N	0	f	1	878	30
\N	\N	f	\N	0	f	2	501	38
\N	\N	f	\N	0	f	2	502	38
\N	\N	f	\N	0	f	2	504	30
\N	\N	f	\N	0	f	2	505	30
\N	\N	f	\N	0	f	2	506	38
\N	\N	f	\N	0	f	2	507	30
\N	\N	f	\N	0	f	2	508	30
\N	\N	f	\N	0	f	2	512	38
\N	\N	f	\N	0	f	2	583	14
\N	\N	f	\N	0	f	2	585	38
\N	\N	f	\N	0	f	2	606	38
\N	\N	f	\N	0	f	2	642	38
\N	\N	f	\N	0	f	2	651	38
\N	\N	f	\N	0	f	2	672	38
\N	\N	f	\N	0	f	2	673	38
\N	\N	f	\N	0	f	2	674	38
\N	\N	f	\N	0	f	2	679	30
\N	\N	f	\N	0	f	2	681	38
\N	\N	f	\N	0	f	2	685	38
\N	\N	f	\N	0	f	2	699	38
\N	\N	f	\N	0	f	2	700	38
\N	\N	f	\N	0	f	2	706	8
\N	\N	f	\N	0	f	2	707	0
\N	\N	f	\N	0	f	2	716	38
\N	\N	f	\N	0	f	2	748	38
\N	\N	f	\N	0	f	2	750	38
\N	\N	f	\N	0	f	2	753	8
\N	\N	f	\N	0	f	2	844	8
\N	\N	f	\N	0	f	2	846	38
\N	\N	f	\N	0	f	2	857	30
\N	\N	f	\N	0	f	2	866	28
\N	\N	f	\N	0	f	2	868	30
\N	\N	f	\N	0	f	2	870	30
\N	\N	f	\N	0	f	2	871	30
\N	\N	f	\N	0	f	2	875	38
\N	\N	f	\N	0	f	2	878	30
\N	\N	f	\N	0	f	3	504	30
\N	\N	f	\N	0	f	3	507	0
\N	\N	f	\N	0	f	3	561	14
\N	\N	f	\N	0	f	3	762	14
\N	\N	f	\N	0	f	3	806	-1
\N	\N	f	\N	0	f	10	501	27
\N	\N	f	\N	0	f	10	502	38
\N	\N	f	\N	0	f	10	504	30
\N	\N	f	\N	0	f	10	505	30
\N	\N	f	\N	0	f	10	506	38
\N	\N	f	\N	0	f	10	507	30
\N	\N	f	\N	0	f	10	508	30
\N	\N	f	\N	0	f	10	511	8
\N	\N	f	\N	0	f	10	512	30
\N	\N	f	\N	0	f	10	583	14
\N	\N	f	\N	0	f	10	585	14
\N	\N	f	\N	0	f	10	588	38
\N	\N	f	\N	0	f	10	589	38
\N	\N	f	\N	0	f	10	599	14
\N	\N	f	\N	0	f	10	642	38
\N	\N	f	\N	0	f	10	648	28
\N	\N	f	\N	0	f	10	653	28
\N	\N	f	\N	0	f	10	665	38
\N	\N	f	\N	0	f	10	668	9
\N	\N	f	\N	0	f	10	669	9
\N	\N	f	\N	0	f	10	671	38
\N	\N	f	\N	0	f	10	672	30
\N	\N	f	\N	0	f	10	674	14
\N	\N	f	\N	0	f	10	679	30
\N	\N	f	\N	0	f	10	680	38
\N	\N	f	\N	0	f	10	681	38
\N	\N	f	\N	0	f	10	706	8
\N	\N	f	\N	0	f	10	707	38
\N	\N	f	\N	0	f	10	708	8
\N	\N	f	\N	0	f	10	750	38
\N	\N	f	\N	0	f	10	753	8
\N	\N	f	\N	0	f	10	758	14
\N	\N	f	\N	0	f	10	759	38
\N	\N	f	\N	0	f	10	812	38
\N	\N	f	\N	0	f	10	844	38
\N	\N	f	\N	0	f	10	850	23
\N	\N	f	\N	0	f	10	857	30
\N	\N	f	\N	0	f	10	863	38
\N	\N	f	\N	0	f	10	866	28
\N	\N	f	\N	0	f	10	868	30
\N	\N	f	\N	0	f	10	870	30
\N	\N	f	\N	0	f	10	871	30
\N	\N	f	\N	0	f	10	873	30
\N	\N	f	\N	0	f	10	874	99
\N	\N	f	\N	0	f	10	878	30
\N	\N	f	\N	0	f	11	501	27
\N	\N	f	\N	0	f	11	502	25
\N	\N	f	\N	0	f	11	504	30
\N	\N	f	\N	0	f	11	505	30
\N	\N	f	\N	0	f	11	506	38
\N	\N	f	\N	0	f	11	507	30
\N	\N	f	\N	0	f	11	508	30
\N	\N	f	\N	0	f	11	511	38
\N	\N	f	\N	0	f	11	512	30
\N	\N	f	\N	0	f	11	583	14
\N	\N	f	\N	0	f	11	585	14
\N	\N	f	\N	0	f	11	599	14
\N	\N	f	\N	0	f	11	642	9
\N	\N	f	\N	0	f	11	648	28
\N	\N	f	\N	0	f	11	651	38
\N	\N	f	\N	0	f	11	653	28
\N	\N	f	\N	0	f	11	665	28
\N	\N	f	\N	0	f	11	668	9
\N	\N	f	\N	0	f	11	669	9
\N	\N	f	\N	0	f	11	671	38
\N	\N	f	\N	0	f	11	672	30
\N	\N	f	\N	0	f	11	673	38
\N	\N	f	\N	0	f	11	674	14
\N	\N	f	\N	0	f	11	679	30
\N	\N	f	\N	0	f	11	680	38
\N	\N	f	\N	0	f	11	681	38
\N	\N	f	\N	0	f	11	706	8
\N	\N	f	\N	0	f	11	707	38
\N	\N	f	\N	0	f	11	708	38
\N	\N	f	\N	0	f	11	734	38
\N	\N	f	\N	0	f	11	735	38
\N	\N	f	\N	0	f	11	750	38
\N	\N	f	\N	0	f	11	753	8
\N	\N	f	\N	0	f	11	758	14
\N	\N	f	\N	0	f	11	759	38
\N	\N	f	\N	0	f	11	812	38
\N	\N	f	\N	0	f	11	816	38
\N	\N	f	\N	0	f	11	841	9
\N	\N	f	\N	0	f	11	844	38
\N	\N	f	\N	0	f	11	846	38
\N	\N	f	\N	0	f	11	857	30
\N	\N	f	\N	0	f	11	863	38
\N	\N	f	\N	0	f	11	866	28
\N	\N	f	\N	0	f	11	868	30
\N	\N	f	\N	0	f	11	870	30
\N	\N	f	\N	0	f	11	871	30
\N	\N	f	\N	0	f	11	873	30
\N	\N	f	\N	0	f	11	874	99
\N	\N	f	\N	0	f	11	878	30
\N	\N	f	\N	0	f	12	504	30
\N	\N	f	\N	0	f	12	507	30
\N	\N	f	\N	0	f	12	561	14
\N	\N	f	\N	0	f	12	762	14
\N	\N	f	\N	0	f	20	501	27
\N	\N	f	\N	0	f	20	502	25
\N	\N	f	\N	0	f	20	504	30
\N	\N	f	\N	0	f	20	505	30
\N	\N	f	\N	0	f	20	506	38
""	\N	f	\N	0	f	20	507	30
\N	\N	f	\N	0	f	20	508	30
\N	\N	f	\N	0	f	20	511	38
\N	\N	f	\N	0	f	20	512	30
\N	\N	f	\N	0	f	20	583	14
\N	\N	f	\N	0	f	20	585	14
\N	\N	f	\N	0	f	20	599	14
\N	\N	f	\N	0	f	20	613	8
\N	\N	f	\N	0	f	20	641	38
\N	\N	f	\N	0	f	20	642	9
\N	\N	f	\N	0	f	20	647	38
\N	\N	f	\N	0	f	20	648	28
\N	\N	f	\N	0	f	20	653	28
\N	\N	f	\N	0	f	20	665	38
\N	\N	f	\N	0	f	20	668	9
\N	\N	f	\N	0	f	20	669	9
\N	\N	f	\N	0	f	20	672	30
\N	\N	f	\N	0	f	20	674	14
\N	\N	f	\N	0	f	20	678	38
\N	\N	f	\N	0	f	20	679	30
\N	\N	f	\N	0	f	20	680	38
\N	\N	f	\N	0	f	20	681	38
\N	\N	f	\N	0	f	20	684	38
\N	\N	f	\N	0	f	20	706	8
\N	\N	f	\N	0	f	20	707	0
\N	\N	f	\N	0	f	20	736	28
\N	\N	f	\N	0	f	20	752	38
\N	\N	f	\N	0	f	20	753	8
\N	\N	f	\N	0	f	20	758	14
\N	\N	f	\N	0	f	20	812	38
\N	\N	f	\N	0	f	20	844	38
\N	\N	f	\N	0	f	20	850	23
\N	\N	f	\N	0	f	20	857	30
\N	\N	f	\N	0	f	20	863	38
\N	\N	f	\N	0	f	20	866	28
\N	\N	f	\N	0	f	20	868	30
\N	\N	f	\N	0	f	20	870	30
\N	\N	f	\N	0	f	20	873	30
\N	\N	f	\N	0	f	20	874	99
\N	\N	f	\N	0	f	20	875	38
\N	\N	f	\N	0	f	20	878	30
\N	\N	f	\N	0	f	20	883	-1
\N	\N	f	\N	0	f	20	886	-1
\N	\N	f	\N	0	f	21	501	27
\N	\N	f	\N	0	f	21	502	25
\N	\N	f	\N	0	f	21	504	30
\N	\N	f	\N	0	f	21	505	30
\N	\N	f	\N	0	f	21	506	38
\N	\N	f	\N	0	f	21	507	30
\N	\N	f	\N	0	f	21	508	30
\N	\N	f	\N	0	f	21	510	14
\N	\N	f	\N	0	f	21	511	38
\N	\N	f	\N	0	f	21	512	30
\N	\N	f	\N	0	f	21	573	14
\N	\N	f	\N	0	f	21	583	14
\N	\N	f	\N	0	f	21	585	14
\N	\N	f	\N	0	f	21	599	14
\N	\N	f	\N	0	f	21	613	38
\N	\N	f	\N	0	f	21	617	14
\N	\N	f	\N	0	f	21	641	8
\N	\N	f	\N	0	f	21	642	9
\N	\N	f	\N	0	f	21	647	38
\N	\N	f	\N	0	f	21	648	28
\N	\N	f	\N	0	f	21	649	38
\N	\N	f	\N	0	f	21	653	28
\N	\N	f	\N	0	f	21	661	38
\N	\N	f	\N	0	f	21	665	28
\N	\N	f	\N	0	f	21	668	9
\N	\N	f	\N	0	f	21	669	9
\N	\N	f	\N	0	f	21	671	38
\N	\N	f	\N	0	f	21	672	30
\N	\N	f	\N	0	f	21	673	38
\N	\N	f	\N	0	f	21	674	14
\N	\N	f	\N	0	f	21	679	30
\N	\N	f	\N	0	f	21	680	38
\N	\N	f	\N	0	f	21	681	38
\N	\N	f	\N	0	f	21	684	38
\N	\N	f	\N	0	f	21	706	8
\N	\N	f	\N	0	f	21	707	0
\N	\N	f	\N	0	f	21	714	38
\N	\N	f	\N	0	f	21	734	38
\N	\N	f	\N	0	f	21	735	38
\N	\N	f	\N	0	f	21	736	28
\N	\N	f	\N	0	f	21	752	38
\N	\N	f	\N	0	f	21	753	8
\N	\N	f	\N	0	f	21	758	38
\N	\N	f	\N	0	f	21	786	38
\N	\N	f	\N	0	f	21	806	-1
\N	\N	f	\N	0	f	21	812	38
"Hello Angela"	\N	f	\N	0	f	21	820	28
\N	\N	f	\N	0	f	21	825	8
\N	\N	f	\N	0	f	21	843	2
\N	\N	f	\N	0	f	21	844	38
\N	\N	f	\N	0	f	21	846	38
\N	\N	f	\N	0	f	21	857	30
\N	\N	f	\N	0	f	21	863	38
\N	\N	f	\N	0	f	21	866	28
\N	\N	f	\N	0	f	21	868	30
\N	\N	f	\N	0	f	21	870	30
\N	\N	f	\N	0	f	21	873	30
\N	\N	f	\N	0	f	21	874	99
\N	\N	f	\N	0	f	21	878	30
\N	\N	f	\N	0	f	21	883	-1
\N	\N	f	\N	0	f	21	886	-1
\N	\N	f	\N	0	f	22	504	30
\N	\N	f	\N	0	f	22	507	30
\N	\N	f	\N	0	f	22	561	14
\N	\N	f	\N	0	f	22	662	14
\N	\N	f	\N	0	f	22	762	14
\N	\N	f	\N	0	f	30	501	27
\N	\N	f	\N	0	f	30	502	25
\N	\N	f	\N	0	f	30	503	18
\N	\N	f	\N	0	f	30	504	30
\N	\N	f	\N	0	f	30	505	30
\N	\N	f	\N	0	f	30	506	38
\N	\N	f	\N	0	f	30	507	30
\N	\N	f	\N	0	f	30	508	30
\N	\N	f	\N	0	f	30	510	14
\N	\N	f	\N	0	f	30	511	38
\N	\N	f	\N	0	f	30	512	30
\N	\N	f	\N	0	f	30	559	14
\N	\N	f	\N	0	f	30	573	14
\N	\N	f	\N	0	f	30	578	14
\N	\N	f	\N	0	f	30	583	14
\N	\N	f	\N	0	f	30	585	14
\N	\N	f	\N	0	f	30	596	38
\N	\N	f	\N	0	f	30	599	14
\N	\N	f	\N	0	f	30	610	38
\N	\N	f	\N	0	f	30	617	14
\N	\N	f	\N	0	f	30	620	38
\N	\N	f	\N	0	f	30	630	38
\N	\N	f	\N	0	f	30	632	38
\N	\N	f	\N	0	f	30	641	8
\N	\N	f	\N	0	f	30	642	9
\N	\N	f	\N	0	f	30	644	38
\N	\N	f	\N	0	f	30	648	28
\N	\N	f	\N	0	f	30	649	38
\N	\N	f	\N	0	f	30	653	28
\N	\N	f	\N	0	f	30	654	38
\N	\N	f	\N	0	f	30	665	28
\N	\N	f	\N	0	f	30	668	9
\N	\N	f	\N	0	f	30	669	9
\N	\N	f	\N	0	f	30	671	38
\N	\N	f	\N	0	f	30	672	30
\N	\N	f	\N	0	f	30	673	14
\N	\N	f	\N	0	f	30	674	14
\N	\N	f	\N	0	f	30	676	38
\N	\N	f	\N	0	f	30	679	30
\N	\N	f	\N	0	f	30	680	38
\N	\N	f	\N	0	f	30	681	38
\N	\N	f	\N	0	f	30	684	38
\N	\N	f	\N	0	f	30	689	38
\N	\N	f	\N	0	f	30	696	14
\N	\N	f	\N	0	f	30	706	8
\N	\N	f	\N	0	f	30	707	38
\N	\N	f	\N	0	f	30	725	38
\N	\N	f	\N	0	f	30	736	28
\N	\N	f	\N	0	f	30	750	38
\N	\N	f	\N	0	f	30	751	8
\N	\N	f	\N	0	f	30	753	8
\N	\N	f	\N	0	f	30	758	14
\N	\N	f	\N	0	f	30	763	38
\N	\N	f	\N	0	f	30	786	38
\N	\N	f	\N	0	f	30	812	28
\N	\N	f	\N	0	f	30	814	14
"Hello Angela"	\N	f	\N	0	f	30	820	28
\N	\N	f	\N	0	f	30	821	28
\N	\N	f	\N	0	f	30	823	38
\N	\N	f	\N	0	f	30	826	24
\N	\N	f	\N	0	f	30	843	2
\N	\N	f	\N	0	f	30	844	8
\N	\N	f	\N	0	f	30	845	38
\N	\N	f	\N	0	f	30	846	0
\N	\N	f	\N	0	f	30	849	14
\N	\N	f	\N	0	f	30	857	30
\N	\N	f	\N	0	f	30	863	38
\N	\N	f	\N	0	f	30	866	28
\N	\N	f	\N	0	f	30	868	30
\N	\N	f	\N	0	f	30	870	30
\N	\N	f	\N	0	f	30	873	30
\N	\N	f	\N	0	f	30	874	99
\N	\N	f	\N	0	f	30	878	30
\N	\N	f	\N	0	f	30	880	-1
\N	\N	f	\N	0	f	30	883	-1
\N	\N	f	\N	0	f	31	501	27
\N	\N	f	\N	0	f	31	502	25
\N	\N	f	\N	0	f	31	503	18
\N	\N	f	\N	0	f	31	504	30
\N	\N	f	\N	0	f	31	505	30
\N	\N	f	\N	0	f	31	506	25
\N	\N	f	\N	0	f	31	507	30
\N	\N	f	\N	0	f	31	508	30
\N	\N	f	\N	0	f	31	509	11
\N	\N	f	\N	0	f	31	510	14
\N	\N	f	\N	0	f	31	512	38
\N	\N	f	\N	0	f	31	552	8
\N	\N	f	\N	0	f	31	553	2
\N	\N	f	\N	0	f	31	555	1
\N	\N	f	\N	0	f	31	557	15
\N	\N	f	\N	0	f	31	559	14
\N	\N	f	\N	0	f	31	560	8
\N	\N	f	\N	0	f	31	563	3
\N	\N	f	\N	0	f	31	566	9
\N	2017-04-27 18:00:00	f	\N	0	f	31	568	4
\N	\N	f	\N	0	f	31	570	3
\N	\N	f	\N	0	f	31	573	14
\N	\N	f	\N	0	f	31	574	9
\N	\N	f	\N	0	f	31	578	14
\N	\N	f	\N	0	f	31	580	15
\N	\N	f	\N	0	f	31	582	4
\N	\N	f	\N	0	f	31	583	14
\N	\N	f	\N	0	f	31	584	11
\N	\N	f	\N	0	f	31	585	14
\N	\N	f	\N	0	f	31	590	15
\N	\N	f	\N	0	f	31	591	3
\N	\N	f	\N	0	f	31	592	11
\N	\N	f	\N	0	f	31	593	3
\N	\N	f	\N	0	f	31	599	14
\N	\N	f	\N	0	f	31	600	2
\N	\N	f	\N	0	f	31	601	4
\N	\N	f	\N	0	f	31	603	28
\N	\N	f	\N	0	f	31	605	14
\N	\N	f	\N	0	f	31	606	1
\N	\N	f	\N	0	f	31	608	1
\N	\N	f	\N	0	f	31	609	14
\N	\N	f	\N	0	f	31	610	3
\N	\N	f	\N	0	f	31	613	2
\N	\N	f	\N	0	f	31	614	8
\N	\N	f	\N	0	f	31	616	2
\N	\N	f	\N	0	f	31	617	14
\N	\N	f	\N	0	f	31	620	14
\N	\N	f	\N	0	f	31	621	8
\N	\N	f	\N	0	f	31	622	1
\N	\N	f	\N	0	f	31	623	14
\N	\N	f	\N	0	f	31	625	1
""	\N	f	\N	0	f	31	627	9
\N	\N	f	\N	0	f	31	632	15
\N	\N	f	\N	0	f	31	638	1
\N	\N	f	\N	0	f	31	641	8
\N	\N	f	\N	0	f	31	642	9
\N	\N	f	\N	0	f	31	643	12
\N	\N	f	\N	0	f	31	645	3
\N	\N	f	\N	0	f	31	646	2
\N	\N	f	\N	0	f	31	648	28
\N	\N	f	\N	0	f	31	649	11
\N	\N	f	\N	0	f	31	651	4
\N	\N	f	\N	0	f	31	652	2
\N	\N	f	\N	0	f	31	653	1
\N	\N	f	\N	0	f	31	654	15
\N	\N	f	\N	0	f	31	655	11
\N	\N	f	\N	0	f	31	661	2
\N	\N	f	\N	0	f	31	665	28
\N	\N	f	\N	0	f	31	667	2
\N	\N	f	\N	0	f	31	668	9
\N	\N	f	\N	0	f	31	669	9
\N	\N	f	\N	0	f	31	671	3
\N	\N	f	\N	0	f	31	673	14
\N	\N	f	\N	0	f	31	674	14
\N	\N	f	\N	0	f	31	675	3
\N	\N	f	\N	0	f	31	676	15
\N	\N	f	\N	0	f	31	678	3
\N	\N	f	\N	0	f	31	680	1
\N	\N	f	\N	0	f	31	682	3
\N	\N	f	\N	0	f	31	684	10
\N	\N	f	\N	0	f	31	686	14
\N	\N	f	\N	0	f	31	689	15
\N	\N	f	\N	0	f	31	692	1
\N	\N	f	\N	0	f	31	694	3
\N	\N	f	\N	0	f	31	695	9
\N	\N	f	\N	0	f	31	698	8
\N	\N	f	\N	0	f	31	701	1
\N	\N	f	\N	0	f	31	706	8
\N	\N	f	\N	0	f	31	707	2
\N	\N	f	\N	0	f	31	708	8
\N	\N	f	\N	0	f	31	711	11
\N	\N	f	\N	0	f	31	713	29
\N	\N	f	\N	0	f	31	718	14
\N	\N	f	\N	0	f	31	724	8
\N	\N	f	\N	0	f	31	725	15
\N	\N	f	\N	0	f	31	728	15
\N	\N	f	\N	0	f	31	729	28
\N	\N	f	\N	0	f	31	736	28
\N	\N	f	\N	0	f	31	739	2
\N	\N	f	\N	0	f	31	740	3
\N	\N	f	\N	0	f	31	742	11
\N	\N	f	\N	0	f	31	743	11
\N	\N	f	\N	0	f	31	749	1
\N	\N	f	\N	0	f	31	750	4
\N	\N	f	\N	0	f	31	751	8
\N	\N	f	\N	0	f	31	753	8
\N	\N	f	\N	0	f	31	754	4
\N	\N	f	\N	0	f	31	755	2
\N	\N	f	\N	0	f	31	756	1
\N	\N	f	\N	0	f	31	758	14
\N	\N	f	\N	0	f	31	763	25
\N	\N	f	\N	0	f	31	773	28
\N	\N	f	\N	0	f	31	774	14
\N	\N	f	\N	0	f	31	778	14
\N	\N	f	\N	0	f	31	780	3
\N	\N	f	\N	0	f	31	781	4
\N	\N	f	\N	0	f	31	782	12
\N	\N	f	\N	0	f	31	783	1
\N	\N	f	\N	0	f	31	786	15
\N	\N	f	\N	0	f	31	788	4
\N	\N	f	\N	0	f	31	789	4
\N	\N	f	\N	0	f	31	793	2
\N	\N	f	\N	0	f	31	795	15
\N	\N	f	\N	0	f	31	796	24
\N	\N	f	\N	0	f	31	797	4
\N	\N	f	\N	0	f	31	804	4
\N	\N	f	\N	0	f	31	806	-1
\N	\N	f	\N	0	f	31	811	3
\N	\N	f	\N	0	f	31	814	14
\N	\N	f	\N	0	f	31	817	4
\N	\N	f	\N	0	f	31	818	4
"Hello Angela"	\N	f	\N	0	f	31	820	28
\N	\N	f	\N	0	f	31	821	1
\N	\N	f	\N	0	f	31	823	4
\N	\N	f	\N	0	f	31	826	24
\N	\N	f	\N	0	f	31	827	4
\N	\N	f	\N	0	f	31	830	14
\N	\N	f	\N	0	f	31	833	19
\N	\N	f	\N	0	f	31	843	2
\N	\N	f	\N	0	f	31	844	2
\N	\N	f	\N	0	f	31	845	11
\N	\N	f	\N	0	f	31	849	14
\N	\N	f	\N	0	f	31	858	2
\N	\N	f	\N	0	f	31	868	30
\N	\N	f	\N	0	f	31	870	30
\N	\N	f	\N	0	f	31	871	30
\N	\N	f	\N	0	f	31	873	30
\N	\N	f	\N	0	f	31	874	99
\N	\N	f	\N	0	f	31	877	-1
\N	\N	f	\N	0	f	31	878	30
\N	\N	f	\N	0	f	31	879	9
\N	\N	f	\N	0	f	31	880	-1
\N	\N	f	\N	0	f	31	882	-1
\N	\N	f	\N	0	f	31	889	-1
\N	\N	f	\N	0	f	32	501	27
\N	\N	f	\N	0	f	32	502	25
\N	\N	f	\N	0	f	32	503	18
\N	\N	f	\N	0	f	32	504	30
\N	\N	f	\N	0	f	32	505	30
\N	\N	f	\N	0	f	32	506	25
\N	\N	f	\N	0	f	32	507	8
\N	\N	f	\N	0	f	32	508	30
\N	\N	f	\N	0	f	32	510	14
\N	\N	f	\N	0	f	32	512	30
\N	\N	f	\N	0	f	32	553	1
\N	\N	f	\N	0	f	32	555	1
\N	\N	f	\N	0	f	32	557	15
\N	\N	f	\N	0	f	32	559	14
\N	\N	f	\N	0	f	32	563	3
\N	\N	f	\N	0	f	32	564	10
\N	\N	f	\N	0	f	32	566	9
\N	\N	f	\N	0	f	32	570	2
\N	\N	f	\N	0	f	32	573	14
\N	\N	f	\N	0	f	32	575	1
\N	\N	f	\N	0	f	32	578	14
\N	\N	f	\N	0	f	32	580	15
\N	\N	f	\N	0	f	32	582	4
\N	\N	f	\N	0	f	32	583	14
\N	\N	f	\N	0	f	32	584	11
\N	\N	f	\N	0	f	32	585	14
\N	\N	f	\N	0	f	32	590	15
\N	\N	f	\N	0	f	32	599	14
\N	\N	f	\N	0	f	32	600	2
\N	\N	f	\N	0	f	32	605	14
\N	\N	f	\N	0	f	32	608	1
\N	\N	f	\N	0	f	32	609	14
\N	\N	f	\N	0	f	32	610	3
\N	\N	f	\N	0	f	32	613	2
\N	\N	f	\N	0	f	32	614	8
\N	\N	f	\N	0	f	32	617	14
\N	\N	f	\N	0	f	32	620	14
\N	\N	f	\N	0	f	32	623	14
\N	\N	f	\N	0	f	32	624	15
\N	\N	f	\N	0	f	32	627	3
\N	\N	f	\N	0	f	32	630	3
\N	\N	f	\N	0	f	32	632	15
\N	\N	f	\N	0	f	32	635	3
\N	\N	f	\N	0	f	32	638	1
\N	\N	f	\N	0	f	32	641	8
\N	\N	f	\N	0	f	32	642	9
\N	\N	f	\N	0	f	32	643	12
\N	\N	f	\N	0	f	32	645	1
\N	\N	f	\N	0	f	32	646	2
\N	\N	f	\N	0	f	32	648	28
\N	\N	f	\N	0	f	32	649	11
\N	\N	f	\N	0	f	32	653	1
\N	\N	f	\N	0	f	32	659	3
\N	\N	f	\N	0	f	32	665	28
\N	\N	f	\N	0	f	32	667	8
\N	\N	f	\N	0	f	32	668	9
\N	\N	f	\N	0	f	32	669	9
\N	\N	f	\N	0	f	32	671	3
\N	\N	f	\N	0	f	32	673	14
\N	\N	f	\N	0	f	32	676	15
\N	\N	f	\N	0	f	32	678	10
\N	\N	f	\N	0	f	32	684	10
\N	\N	f	\N	0	f	32	686	14
\N	\N	f	\N	0	f	32	687	1
\N	\N	f	\N	0	f	32	689	15
\N	\N	f	\N	0	f	32	692	1
\N	\N	f	\N	0	f	32	695	9
\N	\N	f	\N	0	f	32	698	8
\N	\N	f	\N	0	f	32	699	11
\N	\N	f	\N	0	f	32	700	4
\N	\N	f	\N	0	f	32	706	8
\N	\N	f	\N	0	f	32	712	15
\N	\N	f	\N	0	f	32	713	3
\N	\N	f	\N	0	f	32	718	14
\N	\N	f	\N	0	f	32	722	4
\N	\N	f	\N	0	f	32	723	15
\N	\N	f	\N	0	f	32	724	8
\N	\N	f	\N	0	f	32	728	15
\N	\N	f	\N	0	f	32	729	25
\N	\N	f	\N	0	f	32	733	2
\N	\N	f	\N	0	f	32	736	28
\N	\N	f	\N	0	f	32	739	2
\N	\N	f	\N	0	f	32	742	11
\N	\N	f	\N	0	f	32	743	8
\N	\N	f	\N	0	f	32	750	2
\N	\N	f	\N	0	f	32	753	8
\N	\N	f	\N	0	f	32	754	4
\N	\N	f	\N	0	f	32	755	2
\N	\N	f	\N	0	f	32	758	14
\N	\N	f	\N	0	f	32	763	25
\N	\N	f	\N	0	f	32	773	28
\N	\N	f	\N	0	f	32	774	14
\N	\N	f	\N	0	f	32	778	14
\N	\N	f	\N	0	f	32	780	3
\N	\N	f	\N	0	f	32	786	15
\N	\N	f	\N	0	f	32	795	15
\N	\N	f	\N	0	f	32	801	3
\N	\N	f	\N	0	f	32	803	4
""	\N	f	\N	0	f	32	806	8
\N	\N	f	\N	0	f	32	814	14
\N	\N	f	\N	0	f	32	817	4
\N	\N	f	\N	0	f	32	818	4
"Hello Angela"	\N	f	\N	0	f	32	820	28
\N	\N	f	\N	0	f	32	827	4
\N	\N	f	\N	0	f	32	830	14
\N	\N	f	\N	0	f	32	843	2
\N	\N	f	\N	0	f	32	844	1
\N	\N	f	\N	0	f	32	845	2
\N	\N	f	\N	0	f	32	849	14
\N	\N	f	\N	0	f	32	854	1
\N	\N	f	\N	0	f	32	858	2
\N	\N	f	\N	0	f	32	868	30
\N	\N	f	\N	0	f	32	870	30
\N	\N	f	\N	0	f	32	871	30
\N	\N	f	\N	0	f	32	873	30
\N	\N	f	\N	0	f	32	874	99
\N	\N	f	\N	0	f	32	878	30
\N	\N	f	\N	0	f	32	879	9
\N	\N	f	\N	0	f	33	504	41
\N	\N	f	\N	0	f	33	507	30
\N	\N	f	\N	0	f	33	561	14
\N	\N	f	\N	0	f	33	662	14
\N	\N	f	\N	0	f	33	762	14
\N	\N	f	\N	0	f	39	501	27
\N	\N	f	\N	0	f	39	502	25
\N	\N	f	\N	0	f	39	503	18
\N	\N	f	\N	0	f	39	504	30
\N	\N	f	\N	0	f	39	505	30
\N	\N	f	\N	0	f	39	506	25
\N	\N	f	\N	0	f	39	507	30
\N	\N	f	\N	0	f	39	508	30
\N	\N	f	\N	0	f	39	510	14
\N	\N	f	\N	0	f	39	512	30
\N	\N	f	\N	0	f	39	553	3
\N	\N	f	\N	0	f	39	555	1
\N	\N	f	\N	0	f	39	559	14
\N	\N	f	\N	0	f	39	560	4
\N	\N	f	\N	0	f	39	562	38
\N	\N	f	\N	0	f	39	566	9
\N	\N	f	\N	0	f	39	570	1
\N	\N	f	\N	0	f	39	573	14
\N	\N	f	\N	0	f	39	578	14
\N	\N	f	\N	0	f	39	583	14
\N	\N	f	\N	0	f	39	585	14
\N	\N	f	\N	0	f	39	592	2
\N	\N	f	\N	0	f	39	593	1
\N	\N	f	\N	0	f	39	596	2
\N	\N	f	\N	0	f	39	599	14
\N	\N	f	\N	0	f	39	601	1
\N	\N	f	\N	0	f	39	603	11
\N	\N	f	\N	0	f	39	605	14
\N	\N	f	\N	0	f	39	610	3
\N	\N	f	\N	0	f	39	611	1
\N	\N	f	\N	0	f	39	613	2
\N	\N	f	\N	0	f	39	614	1
\N	\N	f	\N	0	f	39	616	1
\N	\N	f	\N	0	f	39	617	14
\N	\N	f	\N	0	f	39	620	14
\N	\N	f	\N	0	f	39	621	8
\N	\N	f	\N	0	f	39	622	2
\N	\N	f	\N	0	f	39	625	3
\N	\N	f	\N	0	f	39	629	3
\N	\N	f	\N	0	f	39	630	3
\N	\N	f	\N	0	f	39	632	15
\N	\N	f	\N	0	f	39	637	11
\N	\N	f	\N	0	f	39	638	2
\N	\N	f	\N	0	f	39	641	8
\N	\N	f	\N	0	f	39	642	9
\N	\N	f	\N	0	f	39	643	12
\N	\N	f	\N	0	f	39	644	4
\N	\N	f	\N	0	f	39	648	28
\N	\N	f	\N	0	f	39	649	11
\N	\N	f	\N	0	f	39	651	38
\N	\N	f	\N	0	f	39	652	2
\N	\N	f	\N	0	f	39	653	1
\N	\N	f	\N	0	f	39	654	15
\N	\N	f	\N	0	f	39	655	38
\N	\N	f	\N	0	f	39	665	28
\N	\N	f	\N	0	f	39	667	4
\N	\N	f	\N	0	f	39	668	9
\N	\N	f	\N	0	f	39	669	9
\N	\N	f	\N	0	f	39	671	3
\N	\N	f	\N	0	f	39	673	14
\N	\N	f	\N	0	f	39	674	14
\N	\N	f	\N	0	f	39	675	2
\N	\N	f	\N	0	f	39	676	1
\N	\N	f	\N	0	f	39	680	2
\N	\N	f	\N	0	f	39	682	1
\N	\N	f	\N	0	f	39	684	10
\N	\N	f	\N	0	f	39	686	14
\N	\N	f	\N	0	f	39	689	15
\N	\N	f	\N	0	f	39	694	1
\N	\N	f	\N	0	f	39	695	9
\N	\N	f	\N	0	f	39	696	1
\N	\N	f	\N	0	f	39	698	8
\N	\N	f	\N	0	f	39	701	3
\N	\N	f	\N	0	f	39	706	8
\N	\N	f	\N	0	f	39	707	1
\N	\N	f	\N	0	f	39	708	3
\N	\N	f	\N	0	f	39	711	11
\N	\N	f	\N	0	f	39	713	3
\N	\N	f	\N	0	f	39	714	4
\N	\N	f	\N	0	f	39	718	14
"Go to wine bar!"	\N	f	\N	0	t	39	720	10
\N	\N	f	\N	0	f	39	724	8
\N	\N	f	\N	0	f	39	725	15
\N	\N	f	\N	0	f	39	728	15
\N	\N	f	\N	0	f	39	734	11
\N	\N	f	\N	0	f	39	735	3
\N	\N	f	\N	0	f	39	736	28
\N	\N	f	\N	0	f	39	739	2
\N	\N	f	\N	0	f	39	749	2
\N	\N	f	\N	0	f	39	750	4
\N	\N	f	\N	0	f	39	751	8
\N	\N	f	\N	0	f	39	753	8
\N	\N	f	\N	0	f	39	756	1
\N	\N	f	\N	0	f	39	758	14
\N	\N	f	\N	0	f	39	763	25
\N	\N	f	\N	0	f	39	764	4
\N	\N	f	\N	0	f	39	765	4
\N	\N	f	\N	0	f	39	773	28
\N	\N	f	\N	0	f	39	774	14
\N	\N	f	\N	0	f	39	778	14
\N	\N	f	\N	0	f	39	780	3
\N	\N	f	\N	0	f	39	782	12
\N	\N	f	\N	0	f	39	786	15
\N	\N	f	\N	0	f	39	793	2
\N	\N	f	\N	0	f	39	796	24
\N	\N	f	\N	0	f	39	804	4
\N	\N	f	\N	0	f	39	811	4
\N	\N	f	\N	0	f	39	812	19
\N	\N	f	\N	0	f	39	814	14
\N	\N	f	\N	0	f	39	816	38
"Hello Angela"	\N	f	\N	0	f	39	820	28
\N	\N	f	\N	0	f	39	821	1
\N	\N	f	\N	0	f	39	822	11
\N	\N	f	\N	0	f	39	823	4
\N	\N	f	\N	0	f	39	825	2
\N	\N	f	\N	0	f	39	826	24
\N	\N	f	\N	0	f	39	830	14
\N	\N	f	\N	0	f	39	833	19
\N	\N	f	\N	0	f	39	843	2
\N	\N	f	\N	0	f	39	844	4
\N	\N	f	\N	0	f	39	845	11
\N	\N	f	\N	0	f	39	849	14
\N	\N	f	\N	0	f	39	866	2
\N	\N	f	\N	0	f	39	868	30
\N	\N	f	\N	0	f	39	870	30
\N	\N	f	\N	0	f	39	871	30
\N	\N	f	\N	0	f	39	873	30
\N	\N	f	\N	0	f	39	874	99
\N	\N	f	\N	0	f	39	877	-1
\N	\N	f	\N	0	f	39	878	30
\N	\N	f	\N	0	f	39	879	38
\N	\N	f	\N	0	f	39	882	-1
\N	\N	f	\N	0	f	39	887	-1
\N	\N	f	\N	0	f	39	889	-1
\N	\N	f	\N	0	t	40	501	27
\N	\N	f	\N	0	f	40	502	25
\N	\N	f	\N	0	f	40	504	28
\N	\N	f	\N	0	f	40	505	30
\N	\N	f	\N	0	f	40	506	25
\N	\N	f	\N	0	f	40	508	30
\N	\N	f	\N	0	f	40	510	14
\N	\N	f	\N	0	f	40	512	30
\N	\N	f	\N	0	f	40	551	3
\N	\N	f	\N	0	f	40	552	8
\N	\N	f	\N	0	f	40	555	1
\N	\N	f	\N	0	f	40	559	14
\N	\N	f	\N	0	f	40	560	3
\N	\N	f	\N	0	f	40	564	9
\N	\N	f	\N	0	f	40	565	15
\N	\N	f	\N	0	f	40	566	9
\N	\N	f	\N	0	f	40	569	15
\N	\N	f	\N	0	f	40	573	14
\N	\N	f	\N	0	f	40	576	8
\N	\N	f	\N	0	f	40	578	14
\N	\N	f	\N	0	f	40	579	0
\N	\N	f	\N	0	f	40	582	1
\N	\N	f	\N	0	f	40	583	14
\N	\N	f	\N	0	f	40	585	14
\N	\N	f	\N	0	f	40	587	15
\N	\N	f	\N	0	f	40	588	19
\N	\N	f	\N	0	f	40	589	19
\N	\N	f	\N	0	f	40	593	1
\N	\N	f	\N	0	f	40	596	11
\N	\N	f	\N	0	f	40	598	3
\N	\N	f	\N	0	f	40	599	14
\N	\N	f	\N	0	f	40	602	14
\N	\N	f	\N	0	f	40	603	11
\N	\N	f	\N	0	f	40	604	14
\N	\N	f	\N	0	f	40	605	14
\N	\N	f	\N	0	f	40	606	1
\N	\N	f	\N	0	f	40	607	3
\N	\N	f	\N	0	f	40	609	14
\N	\N	f	\N	0	f	40	610	3
\N	\N	f	\N	0	f	40	611	2
\N	\N	f	\N	0	f	40	613	8
\N	\N	f	\N	0	f	40	614	8
\N	\N	f	\N	0	f	40	616	2
\N	\N	f	\N	0	f	40	617	14
\N	\N	f	\N	0	f	40	618	2
\N	\N	f	\N	0	f	40	619	15
\N	\N	f	\N	0	f	40	620	14
\N	\N	f	\N	0	f	40	622	11
\N	\N	f	\N	0	f	40	623	14
\N	\N	f	\N	0	f	40	625	2
\N	\N	f	\N	0	f	40	629	14
\N	\N	f	\N	0	f	40	630	2
\N	\N	f	\N	0	f	40	631	3
\N	\N	f	\N	0	f	40	632	15
\N	\N	f	\N	0	f	40	633	11
\N	\N	f	\N	0	f	40	636	2
\N	\N	f	\N	0	f	40	638	1
\N	\N	f	\N	0	f	40	641	8
\N	\N	f	\N	0	f	40	642	9
\N	\N	f	\N	0	f	40	643	12
\N	\N	f	\N	0	f	40	648	28
\N	\N	f	\N	0	f	40	649	11
\N	\N	f	\N	0	f	40	652	4
\N	\N	f	\N	0	f	40	653	1
\N	\N	f	\N	0	f	40	663	1
\N	\N	f	\N	0	f	40	665	28
\N	\N	f	\N	0	f	40	666	4
\N	\N	f	\N	0	f	40	667	3
\N	\N	f	\N	0	f	40	668	9
\N	\N	f	\N	0	f	40	669	9
\N	\N	f	\N	0	f	40	673	14
\N	\N	f	\N	0	f	40	682	1
\N	\N	f	\N	0	f	40	686	14
\N	\N	f	\N	0	f	40	689	15
\N	\N	f	\N	0	f	40	692	2
\N	\N	f	\N	0	f	40	698	8
\N	\N	f	\N	0	f	40	706	8
\N	\N	f	\N	0	f	40	707	2
\N	\N	f	\N	0	f	40	709	3
\N	\N	f	\N	0	f	40	710	4
\N	\N	f	\N	0	f	40	714	4
\N	\N	f	\N	0	f	40	718	14
\N	\N	f	\N	0	f	40	719	8
\N	\N	f	\N	0	f	40	720	14
\N	\N	f	\N	0	f	40	724	8
\N	\N	f	\N	0	f	40	725	15
\N	\N	f	\N	0	f	40	726	1
\N	\N	f	\N	0	f	40	727	3
\N	\N	f	\N	0	f	40	728	15
\N	\N	f	\N	0	f	40	729	25
\N	\N	f	\N	0	f	40	730	3
\N	\N	f	\N	0	f	40	734	11
\N	\N	f	\N	0	f	40	735	1
\N	\N	f	\N	0	f	40	736	28
\N	\N	f	\N	0	f	40	737	4
\N	\N	f	\N	0	f	40	739	2
\N	\N	f	\N	0	f	40	742	11
\N	\N	f	\N	0	f	40	743	11
\N	\N	f	\N	0	f	40	749	3
\N	\N	f	\N	0	f	40	750	4
\N	\N	f	\N	0	f	40	751	8
\N	\N	f	\N	0	f	40	753	8
\N	\N	f	\N	0	f	40	755	2
\N	\N	f	\N	0	f	40	757	4
\N	\N	f	\N	0	f	40	758	14
\N	\N	f	\N	0	f	40	760	1
\N	\N	f	\N	0	f	40	763	25
\N	\N	f	\N	0	f	40	764	4
\N	\N	f	\N	0	f	40	765	4
\N	\N	f	\N	0	f	40	771	1
\N	\N	f	\N	0	f	40	772	4
\N	\N	f	\N	0	f	40	774	14
\N	\N	f	\N	0	f	40	775	8
\N	\N	f	\N	0	f	40	776	8
\N	\N	f	\N	0	f	40	778	14
\N	\N	f	\N	0	f	40	779	14
\N	\N	f	\N	0	f	40	780	3
\N	\N	f	\N	0	f	40	782	12
\N	\N	f	\N	0	f	40	786	15
\N	\N	f	\N	0	f	40	792	10
\N	\N	f	\N	0	f	40	795	15
\N	\N	f	\N	0	f	40	801	-1
\N	\N	f	\N	0	f	40	802	4
\N	\N	f	\N	0	f	40	804	2
\N	\N	f	\N	0	f	40	806	-1
\N	\N	f	\N	0	f	40	808	0
\N	\N	f	\N	0	f	40	809	11
\N	\N	f	\N	0	f	40	810	4
\N	\N	f	\N	0	f	40	812	28
\N	\N	f	\N	0	f	40	814	14
"Hello Angela"	\N	f	\N	0	f	40	820	10
\N	\N	f	\N	0	f	40	821	1
\N	\N	f	\N	0	f	40	823	4
\N	\N	f	\N	0	f	40	824	16
\N	\N	f	\N	0	f	40	831	-1
\N	\N	f	\N	0	f	40	832	2
\N	\N	f	\N	0	f	40	839	2
\N	\N	f	\N	0	f	40	843	2
\N	\N	f	\N	0	f	40	844	4
\N	\N	f	\N	0	f	40	845	11
\N	\N	f	\N	0	f	40	849	14
\N	\N	f	\N	0	f	40	853	3
\N	\N	f	\N	0	f	40	854	3
\N	\N	f	\N	0	f	40	867	2
\N	\N	f	\N	0	f	40	868	30
\N	\N	f	\N	0	f	40	870	30
\N	\N	f	\N	0	f	40	873	30
\N	\N	f	\N	0	f	40	874	99
\N	\N	f	\N	0	f	40	879	9
\N	\N	f	\N	0	f	40	880	-1
\N	\N	f	\N	0	f	40	881	-1
\N	\N	f	\N	0	f	40	882	-1
\N	\N	f	\N	0	f	40	883	-1
\N	\N	f	\N	0	f	40	884	-1
\N	\N	f	\N	0	f	40	888	-1
\N	\N	f	\N	0	f	40	889	-1
\N	\N	f	\N	0	t	41	501	25
\N	\N	f	\N	0	f	41	502	25
\N	\N	f	\N	0	f	41	504	28
\N	\N	f	\N	0	f	41	506	25
\N	\N	f	\N	0	f	41	508	30
\N	\N	f	\N	0	f	41	509	12
\N	\N	f	\N	0	f	41	510	14
\N	\N	f	\N	0	f	41	512	30
\N	\N	f	\N	0	f	41	551	2
\N	\N	f	\N	0	f	41	552	8
\N	\N	f	\N	0	f	41	553	3
\N	\N	f	\N	0	f	41	555	1
\N	\N	f	\N	0	f	41	557	15
\N	\N	f	\N	0	f	41	559	14
\N	\N	f	\N	0	f	41	563	1
\N	\N	f	\N	0	f	41	564	9
\N	\N	f	\N	0	f	41	565	15
\N	\N	f	\N	0	f	41	566	9
\N	\N	f	\N	0	f	41	568	4
\N	\N	f	\N	0	f	41	569	15
\N	\N	f	\N	0	f	41	573	14
\N	\N	f	\N	0	f	41	576	8
\N	\N	f	\N	0	f	41	578	14
\N	\N	f	\N	0	f	41	580	15
\N	\N	f	\N	0	f	41	582	1
\N	\N	f	\N	0	f	41	583	14
\N	\N	f	\N	0	f	41	585	14
\N	\N	f	\N	0	f	41	587	15
\N	\N	f	\N	0	f	41	588	19
\N	\N	f	\N	0	f	41	589	19
""	\N	f	\N	0	f	41	590	16
\N	\N	f	\N	0	f	41	592	9
\N	\N	f	\N	0	f	41	593	1
\N	\N	f	\N	0	f	41	595	2
\N	\N	f	\N	0	f	41	598	2
\N	\N	f	\N	0	f	41	599	14
\N	\N	f	\N	0	f	41	601	14
\N	\N	f	\N	0	f	41	602	14
\N	\N	f	\N	0	f	41	603	12
\N	\N	f	\N	0	f	41	604	14
\N	\N	f	\N	0	f	41	605	14
\N	\N	f	\N	0	f	41	607	3
\N	\N	f	\N	0	f	41	609	14
\N	\N	f	\N	0	f	41	610	3
\N	\N	f	\N	0	f	41	613	2
\N	\N	f	\N	0	f	41	614	8
\N	\N	f	\N	0	f	41	617	14
\N	\N	f	\N	0	f	41	619	15
\N	\N	f	\N	0	f	41	620	14
\N	\N	f	\N	0	f	41	622	11
\N	\N	f	\N	0	f	41	623	14
\N	\N	f	\N	0	f	41	625	2
\N	\N	f	\N	0	f	41	631	3
\N	\N	f	\N	0	f	41	632	15
\N	\N	f	\N	0	f	41	633	9
\N	\N	f	\N	0	f	41	637	11
\N	\N	f	\N	0	f	41	641	8
\N	\N	f	\N	0	f	41	642	9
\N	\N	f	\N	0	f	41	643	12
\N	\N	f	\N	0	f	41	648	28
\N	\N	f	\N	0	f	41	649	11
\N	\N	f	\N	0	f	41	651	4
\N	\N	f	\N	0	f	41	653	1
\N	\N	f	\N	0	f	41	654	15
\N	\N	f	\N	0	f	41	661	2
\N	\N	f	\N	0	f	41	663	3
\N	\N	f	\N	0	f	41	665	28
\N	\N	f	\N	0	f	41	666	1
\N	\N	f	\N	0	f	41	668	9
\N	\N	f	\N	0	f	41	669	9
\N	\N	f	\N	0	f	41	671	4
\N	\N	f	\N	0	f	41	673	14
\N	\N	f	\N	0	f	41	675	3
\N	\N	f	\N	0	f	41	676	15
\N	\N	f	\N	0	f	41	685	12
\N	\N	f	\N	0	f	41	687	1
\N	\N	f	\N	0	f	41	689	15
\N	\N	f	\N	0	f	41	691	3
\N	\N	f	\N	0	f	41	692	1
\N	\N	f	\N	0	f	41	698	8
\N	\N	f	\N	0	f	41	706	8
\N	\N	f	\N	0	f	41	707	41
\N	\N	f	\N	0	f	41	709	1
\N	\N	f	\N	0	f	41	710	10
\N	\N	f	\N	0	f	41	718	14
\N	\N	f	\N	0	f	41	724	8
\N	\N	f	\N	0	f	41	728	15
\N	\N	f	\N	0	f	41	729	25
\N	\N	f	\N	0	f	41	730	3
\N	\N	f	\N	0	f	41	733	1
\N	\N	f	\N	0	f	41	736	28
\N	\N	f	\N	0	f	41	739	4
\N	\N	f	\N	0	f	41	740	10
\N	\N	f	\N	0	f	41	749	2
\N	\N	f	\N	0	f	41	750	4
\N	\N	f	\N	0	f	41	751	8
\N	\N	f	\N	0	f	41	753	8
\N	\N	f	\N	0	f	41	757	4
\N	\N	f	\N	0	f	41	758	14
\N	\N	f	\N	0	f	41	760	8
\N	\N	f	\N	0	f	41	763	25
\N	\N	f	\N	0	f	41	772	4
\N	\N	f	\N	0	f	41	774	14
\N	\N	f	\N	0	f	41	775	8
\N	\N	f	\N	0	f	41	776	8
\N	\N	f	\N	0	f	41	778	14
\N	\N	f	\N	0	f	41	779	14
\N	\N	f	\N	0	f	41	780	3
\N	\N	f	\N	0	f	41	782	12
\N	\N	f	\N	0	f	41	786	15
\N	\N	f	\N	0	f	41	792	10
\N	\N	f	\N	0	f	41	793	4
\N	\N	f	\N	0	f	41	795	15
\N	\N	f	\N	0	f	41	804	4
\N	\N	f	\N	0	f	41	806	-1
\N	\N	f	\N	0	f	41	808	8
\N	\N	f	\N	0	f	41	809	11
\N	\N	f	\N	0	f	41	811	4
\N	\N	f	\N	0	f	41	814	14
\N	\N	f	\N	0	f	41	816	4
\N	\N	f	\N	0	f	41	817	3
\N	\N	f	\N	0	f	41	818	2
\N	\N	f	\N	0	f	41	819	2
\N	\N	f	\N	0	f	41	821	1
\N	\N	f	\N	0	f	41	823	2
\N	\N	f	\N	0	f	41	824	16
\N	\N	f	\N	0	f	41	827	4
\N	\N	f	\N	0	f	41	828	9
\N	\N	f	\N	0	f	41	830	14
\N	\N	f	\N	0	f	41	832	2
\N	\N	f	\N	0	f	41	835	3
\N	\N	f	\N	0	f	41	837	3
\N	\N	f	\N	0	f	41	839	4
\N	\N	f	\N	0	f	41	840	14
\N	\N	f	\N	0	f	41	843	2
\N	\N	f	\N	0	f	41	844	1
\N	\N	f	\N	0	f	41	845	1
\N	\N	f	\N	0	f	41	849	14
\N	\N	f	\N	0	f	41	853	3
\N	\N	f	\N	0	f	41	854	1
\N	\N	f	\N	0	f	41	859	8
\N	\N	f	\N	0	f	41	863	3
\N	\N	f	\N	0	f	41	867	2
\N	\N	f	\N	0	f	41	868	30
\N	\N	f	\N	0	f	41	869	2
\N	\N	f	\N	0	f	41	870	30
\N	\N	f	\N	0	f	41	872	4
\N	\N	f	\N	0	f	41	873	30
\N	\N	f	\N	0	f	41	874	99
\N	\N	f	\N	0	f	41	879	9
\N	\N	f	\N	0	f	41	881	-1
\N	\N	f	\N	0	f	41	884	-1
\N	\N	f	\N	0	f	41	889	-1
\N	\N	f	\N	0	t	42	501	27
\N	\N	f	\N	0	f	42	502	25
\N	\N	f	\N	0	f	42	504	30
\N	\N	f	\N	0	f	42	505	30
\N	\N	f	\N	0	f	42	506	25
\N	\N	f	\N	0	f	42	508	30
\N	\N	f	\N	0	f	42	510	14
\N	\N	f	\N	0	f	42	512	30
\N	\N	f	\N	0	f	42	552	8
\N	\N	f	\N	0	f	42	553	1
\N	\N	f	\N	0	f	42	554	10
\N	\N	f	\N	0	f	42	555	1
\N	\N	f	\N	0	f	42	556	2
\N	\N	f	\N	0	f	42	558	4
\N	\N	f	\N	0	f	42	559	14
\N	\N	f	\N	0	f	42	563	3
\N	\N	f	\N	0	f	42	564	9
\N	\N	f	\N	0	f	42	565	15
\N	\N	f	\N	0	f	42	566	9
\N	\N	f	\N	0	f	42	569	15
\N	\N	f	\N	0	f	42	573	14
\N	\N	f	\N	0	f	42	578	14
\N	\N	f	\N	0	f	42	581	4
\N	\N	f	\N	0	f	42	582	2
\N	\N	f	\N	0	f	42	583	14
\N	\N	f	\N	0	f	42	585	14
\N	\N	f	\N	0	f	42	587	15
\N	\N	f	\N	0	f	42	588	19
\N	\N	f	\N	0	f	42	589	19
\N	\N	f	\N	0	f	42	590	15
\N	\N	f	\N	0	f	42	595	3
\N	\N	f	\N	0	f	42	599	14
\N	\N	f	\N	0	f	42	601	11
\N	\N	f	\N	0	f	42	602	14
\N	\N	f	\N	0	f	42	604	14
\N	\N	f	\N	0	f	42	605	14
\N	\N	f	\N	0	f	42	609	14
\N	\N	f	\N	0	f	42	610	3
\N	\N	f	\N	0	f	42	613	8
\N	\N	f	\N	0	f	42	614	8
\N	\N	f	\N	0	f	42	617	14
\N	\N	f	\N	0	f	42	619	15
\N	\N	f	\N	0	f	42	620	4
\N	\N	f	\N	0	f	42	623	14
\N	\N	f	\N	0	f	42	624	15
\N	\N	f	\N	0	f	42	630	11
\N	\N	f	\N	0	f	42	632	15
\N	\N	f	\N	0	f	42	633	9
\N	\N	f	\N	0	f	42	634	1
\N	\N	f	\N	0	f	42	641	8
\N	\N	f	\N	0	f	42	642	9
\N	\N	f	\N	0	f	42	643	12
\N	\N	f	\N	0	f	42	645	1
\N	\N	f	\N	0	f	42	648	28
\N	\N	f	\N	0	f	42	649	11
\N	\N	f	\N	0	f	42	653	1
\N	\N	f	\N	0	f	42	654	15
\N	\N	f	\N	0	f	42	663	1
\N	\N	f	\N	0	f	42	664	3
\N	\N	f	\N	0	f	42	665	28
\N	\N	f	\N	0	f	42	668	9
\N	\N	f	\N	0	f	42	669	9
\N	\N	f	\N	0	f	42	670	2
\N	\N	f	\N	0	f	42	673	14
\N	\N	f	\N	0	f	42	676	15
\N	\N	f	\N	0	f	42	678	3
\N	\N	f	\N	0	f	42	685	11
\N	\N	f	\N	0	f	42	689	15
\N	\N	f	\N	0	f	42	691	3
\N	\N	f	\N	0	f	42	692	2
\N	\N	f	\N	0	f	42	696	1
\N	\N	f	\N	0	f	42	698	8
\N	\N	f	\N	0	f	42	702	3
\N	\N	f	\N	0	f	42	707	11
\N	\N	f	\N	0	f	42	710	2
\N	\N	f	\N	0	f	42	718	14
\N	\N	f	\N	0	f	42	720	14
\N	\N	f	\N	0	f	42	722	2
\N	\N	f	\N	0	f	42	723	10
\N	\N	f	\N	0	f	42	724	8
\N	\N	f	\N	0	f	42	728	15
\N	\N	f	\N	0	f	42	739	2
\N	\N	f	\N	0	f	42	750	4
\N	\N	f	\N	0	f	42	751	8
\N	\N	f	\N	0	f	42	753	8
\N	\N	f	\N	0	f	42	755	1
\N	\N	f	\N	0	f	42	757	4
\N	\N	f	\N	0	f	42	758	14
\N	\N	f	\N	0	f	42	760	1
\N	\N	f	\N	0	f	42	761	4
\N	\N	f	\N	0	f	42	763	25
\N	\N	f	\N	0	f	42	772	4
\N	\N	f	\N	0	f	42	773	28
\N	\N	f	\N	0	f	42	774	14
\N	\N	f	\N	0	f	42	775	8
\N	\N	f	\N	0	f	42	776	8
\N	\N	f	\N	0	f	42	778	14
\N	\N	f	\N	0	f	42	779	14
\N	\N	f	\N	0	f	42	780	3
\N	\N	f	\N	0	f	42	786	15
\N	\N	f	\N	0	f	42	790	2
\N	\N	f	\N	0	f	42	792	10
\N	\N	f	\N	0	f	42	794	4
\N	\N	f	\N	0	f	42	795	15
\N	\N	f	\N	0	f	42	800	2
\N	\N	f	\N	0	f	42	806	-1
\N	\N	f	\N	0	f	42	808	8
\N	\N	f	\N	0	f	42	814	14
\N	\N	f	\N	0	f	42	815	4
\N	\N	f	\N	0	f	42	817	4
\N	\N	f	\N	0	f	42	818	3
\N	\N	f	\N	0	f	42	821	1
\N	\N	f	\N	0	f	42	827	4
\N	\N	f	\N	0	f	42	828	9
\N	\N	f	\N	0	f	42	830	14
\N	\N	f	\N	0	f	42	831	-1
\N	\N	f	\N	0	f	42	832	2
\N	\N	f	\N	0	f	42	837	3
\N	\N	f	\N	0	f	42	839	4
\N	\N	f	\N	0	f	42	840	14
\N	\N	f	\N	0	f	42	841	9
\N	\N	f	\N	0	f	42	843	2
\N	\N	f	\N	0	f	42	844	2
\N	\N	f	\N	0	f	42	845	2
\N	\N	f	\N	0	f	42	847	11
\N	\N	f	\N	0	f	42	849	14
\N	\N	f	\N	0	f	42	853	4
\N	\N	f	\N	0	f	42	854	1
\N	\N	f	\N	0	f	42	855	11
\N	\N	f	\N	0	f	42	859	8
\N	\N	f	\N	0	f	42	863	3
\N	\N	f	\N	0	f	42	865	3
\N	\N	f	\N	0	f	42	868	30
\N	\N	f	\N	0	f	42	869	1
\N	\N	f	\N	0	f	42	870	30
\N	\N	f	\N	0	f	42	873	30
\N	\N	f	\N	0	f	42	874	99
\N	\N	f	\N	0	f	42	878	30
\N	\N	f	\N	0	f	42	879	9
\N	\N	f	\N	0	f	42	881	-1
\N	\N	f	\N	0	t	43	501	27
\N	\N	f	\N	0	f	43	502	25
\N	\N	f	\N	0	f	43	504	28
\N	\N	f	\N	0	f	43	505	30
\N	\N	f	\N	0	f	43	506	25
\N	\N	f	\N	0	f	43	508	30
\N	\N	f	\N	0	f	43	510	14
\N	\N	f	\N	0	f	43	512	30
\N	\N	f	\N	0	f	43	554	3
\N	\N	f	\N	0	f	43	555	1
\N	\N	f	\N	0	f	43	558	2
\N	\N	f	\N	0	f	43	563	2
\N	\N	f	\N	0	f	43	564	10
\N	\N	f	\N	0	f	43	566	9
\N	\N	f	\N	0	f	43	569	15
\N	\N	f	\N	0	f	43	573	14
\N	\N	f	\N	0	f	43	578	14
\N	\N	f	\N	0	f	43	581	1
\N	\N	f	\N	0	f	43	582	2
\N	\N	f	\N	0	f	43	583	14
\N	\N	f	\N	0	f	43	585	14
\N	\N	f	\N	0	f	43	588	19
\N	\N	f	\N	0	f	43	589	19
\N	\N	f	\N	0	f	43	590	15
\N	\N	f	\N	0	f	43	595	1
\N	\N	f	\N	0	f	43	599	14
\N	\N	f	\N	0	f	43	602	14
\N	\N	f	\N	0	f	43	604	14
\N	\N	f	\N	0	f	43	605	14
\N	\N	f	\N	0	f	43	609	14
\N	\N	f	\N	0	f	43	610	3
\N	\N	f	\N	0	f	43	613	10
\N	\N	f	\N	0	f	43	614	8
\N	\N	f	\N	0	f	43	617	14
\N	\N	f	\N	0	f	43	620	14
\N	\N	f	\N	0	f	43	623	14
\N	\N	f	\N	0	f	43	624	15
\N	\N	f	\N	0	f	43	630	2
\N	\N	f	\N	0	f	43	632	15
\N	\N	f	\N	0	f	43	633	9
\N	\N	f	\N	0	f	43	641	8
\N	\N	f	\N	0	f	43	642	9
\N	\N	f	\N	0	f	43	643	12
\N	\N	f	\N	0	f	43	645	1
\N	\N	f	\N	0	f	43	648	28
\N	\N	f	\N	0	f	43	649	11
\N	\N	f	\N	0	f	43	653	1
\N	\N	f	\N	0	f	43	654	15
\N	\N	f	\N	0	f	43	664	4
\N	\N	f	\N	0	f	43	665	28
\N	\N	f	\N	0	f	43	668	9
\N	\N	f	\N	0	f	43	669	9
\N	\N	f	\N	0	f	43	670	4
\N	\N	f	\N	0	f	43	673	14
\N	\N	f	\N	0	f	43	678	1
\N	\N	f	\N	0	f	43	685	11
\N	\N	f	\N	0	f	43	689	15
\N	\N	f	\N	0	f	43	692	2
\N	\N	f	\N	0	f	43	698	8
\N	\N	f	\N	0	f	43	702	1
\N	\N	f	\N	0	f	43	706	8
\N	\N	f	\N	0	f	43	710	15
\N	\N	f	\N	0	f	43	720	14
\N	\N	f	\N	0	f	43	722	4
\N	\N	f	\N	0	f	43	723	12
\N	\N	f	\N	0	f	43	724	8
\N	\N	f	\N	0	f	43	728	15
\N	\N	f	\N	0	f	43	750	4
\N	\N	f	\N	0	f	43	751	8
\N	\N	f	\N	0	f	43	753	8
\N	\N	f	\N	0	f	43	755	1
\N	\N	f	\N	0	f	43	758	14
\N	\N	f	\N	0	f	43	760	3
\N	\N	f	\N	0	f	43	761	3
\N	\N	f	\N	0	f	43	763	25
\N	\N	f	\N	0	f	43	773	28
\N	\N	f	\N	0	f	43	774	14
\N	\N	f	\N	0	f	43	775	8
\N	\N	f	\N	0	f	43	776	8
\N	\N	f	\N	0	f	43	778	14
\N	\N	f	\N	0	f	43	779	14
\N	\N	f	\N	0	f	43	780	3
\N	\N	f	\N	0	f	43	786	15
\N	\N	f	\N	0	f	43	790	2
\N	\N	f	\N	0	f	43	792	10
\N	\N	f	\N	0	f	43	794	4
\N	\N	f	\N	0	f	43	795	15
\N	\N	f	\N	0	f	43	800	2
\N	\N	f	\N	0	f	43	806	-1
\N	\N	f	\N	0	f	43	808	8
\N	\N	f	\N	0	f	43	814	14
\N	\N	f	\N	0	f	43	817	4
\N	\N	f	\N	0	f	43	818	3
\N	\N	f	\N	0	f	43	821	1
\N	\N	f	\N	0	f	43	827	4
\N	\N	f	\N	0	f	43	828	9
\N	\N	f	\N	0	f	43	831	-1
\N	\N	f	\N	0	f	43	840	14
\N	\N	f	\N	0	f	43	841	9
\N	\N	f	\N	0	f	43	843	2
\N	\N	f	\N	0	f	43	844	2
\N	\N	f	\N	0	f	43	845	2
\N	\N	f	\N	0	f	43	849	14
\N	\N	f	\N	0	f	43	854	3
\N	\N	f	\N	0	f	43	859	8
\N	\N	f	\N	0	f	43	863	3
\N	\N	f	\N	0	f	43	865	3
\N	\N	f	\N	0	f	43	868	30
\N	\N	f	\N	0	f	43	870	30
\N	\N	f	\N	0	f	43	873	30
\N	\N	f	\N	0	f	43	874	99
\N	\N	f	\N	0	f	43	878	30
\N	\N	f	\N	0	f	43	879	9
\N	\N	f	\N	0	f	44	504	30
\N	\N	f	\N	0	f	44	507	30
\N	\N	f	\N	0	f	44	561	14
\N	\N	f	\N	0	f	44	662	14
\N	\N	f	\N	0	f	44	762	14
\N	\N	f	\N	0	f	50	501	27
\N	\N	f	\N	0	f	50	502	25
\N	\N	f	\N	0	f	50	504	28
\N	\N	f	\N	0	f	50	505	30
\N	\N	f	\N	0	f	50	506	25
\N	\N	f	\N	0	f	50	507	30
\N	\N	f	\N	0	f	50	508	30
\N	\N	f	\N	0	f	50	510	14
\N	\N	f	\N	0	f	50	512	30
\N	\N	f	\N	0	f	50	552	8
\N	\N	f	\N	0	f	50	555	1
\N	\N	f	\N	0	f	50	559	14
\N	\N	f	\N	0	f	50	560	2
\N	\N	f	\N	0	f	50	564	10
\N	\N	f	\N	0	f	50	566	9
\N	\N	f	\N	0	f	50	569	15
\N	\N	f	\N	0	f	50	573	14
\N	\N	f	\N	0	f	50	578	14
\N	\N	f	\N	0	f	50	582	4
\N	\N	f	\N	0	f	50	583	14
\N	\N	f	\N	0	f	50	584	11
\N	\N	f	\N	0	f	50	585	14
\N	\N	f	\N	0	f	50	587	15
\N	\N	f	\N	0	f	50	588	19
\N	\N	f	\N	0	f	50	589	19
\N	\N	f	\N	0	f	50	590	15
\N	\N	f	\N	0	f	50	595	2
\N	\N	f	\N	0	f	50	598	3
\N	\N	f	\N	0	f	50	599	14
\N	\N	f	\N	0	f	50	602	14
\N	\N	f	\N	0	f	50	603	11
\N	\N	f	\N	0	f	50	604	14
\N	\N	f	\N	0	f	50	605	14
\N	\N	f	\N	0	f	50	606	2
\N	\N	f	\N	0	f	50	609	14
\N	\N	f	\N	0	f	50	610	3
\N	\N	f	\N	0	f	50	613	9
\N	\N	f	\N	0	f	50	614	4
\N	\N	f	\N	0	f	50	616	3
\N	\N	f	\N	0	f	50	617	14
\N	\N	f	\N	0	f	50	620	11
\N	\N	f	\N	0	f	50	623	14
\N	\N	f	\N	0	f	50	624	15
\N	\N	f	\N	0	f	50	625	1
\N	\N	f	\N	0	f	50	626	8
\N	\N	f	\N	0	f	50	632	15
\N	\N	f	\N	0	f	50	633	11
\N	\N	f	\N	0	f	50	634	4
\N	\N	f	\N	0	f	50	636	4
\N	\N	f	\N	0	f	50	638	3
\N	\N	f	\N	0	f	50	641	8
\N	\N	f	\N	0	f	50	642	9
\N	\N	f	\N	0	f	50	643	12
\N	\N	f	\N	0	f	50	648	28
\N	\N	f	\N	0	f	50	649	11
\N	\N	f	\N	0	f	50	652	2
\N	\N	f	\N	0	f	50	653	1
\N	\N	f	\N	0	f	50	654	15
\N	\N	f	\N	0	f	50	658	2
\N	\N	f	\N	0	f	50	660	1
\N	\N	f	\N	0	f	50	663	2
\N	\N	f	\N	0	f	50	664	1
\N	\N	f	\N	0	f	50	667	9
\N	\N	f	\N	0	f	50	668	9
\N	\N	f	\N	0	f	50	669	9
\N	\N	f	\N	0	f	50	670	1
\N	\N	f	\N	0	f	50	673	14
\N	\N	f	\N	0	f	50	676	15
\N	\N	f	\N	0	f	50	685	14
\N	\N	f	\N	0	f	50	686	14
\N	\N	f	\N	0	f	50	687	4
\N	\N	f	\N	0	f	50	689	15
\N	\N	f	\N	0	f	50	690	15
\N	\N	f	\N	0	f	50	692	4
\N	\N	f	\N	0	f	50	693	1
\N	\N	f	\N	0	f	50	695	9
\N	\N	f	\N	0	f	50	698	8
\N	\N	f	\N	0	f	50	706	8
\N	\N	f	\N	0	f	50	707	1
\N	\N	f	\N	0	f	50	710	2
\N	\N	f	\N	0	f	50	712	15
\N	\N	f	\N	0	f	50	718	14
\N	\N	f	\N	0	f	50	719	8
\N	\N	f	\N	0	f	50	723	12
\N	\N	f	\N	0	f	50	724	8
\N	\N	f	\N	0	f	50	728	15
\N	\N	f	\N	0	f	50	731	0
\N	\N	f	\N	0	f	50	732	15
\N	\N	f	\N	0	f	50	733	4
\N	\N	f	\N	0	f	50	739	2
\N	\N	f	\N	0	f	50	749	3
\N	\N	f	\N	0	f	50	751	8
\N	\N	f	\N	0	f	50	753	8
\N	\N	f	\N	0	f	50	755	4
\N	\N	f	\N	0	f	50	758	14
\N	\N	f	\N	0	f	50	761	1
\N	\N	f	\N	0	f	50	766	3
\N	\N	f	\N	0	f	50	767	3
\N	\N	f	\N	0	f	50	769	14
\N	\N	f	\N	0	f	50	774	14
\N	\N	f	\N	0	f	50	775	8
\N	\N	f	\N	0	f	50	776	8
\N	\N	f	\N	0	f	50	778	14
\N	\N	f	\N	0	f	50	779	14
\N	\N	f	\N	0	f	50	780	3
\N	\N	f	\N	0	f	50	784	3
\N	\N	f	\N	0	f	50	785	2
\N	\N	f	\N	0	f	50	786	15
\N	\N	f	\N	0	f	50	790	11
\N	\N	f	\N	0	f	50	792	10
\N	\N	f	\N	0	f	50	794	1
\N	\N	f	\N	0	f	50	795	15
\N	\N	f	\N	0	f	50	808	8
\N	\N	f	\N	0	f	50	809	11
\N	\N	f	\N	0	f	50	814	14
\N	\N	f	\N	0	f	50	817	3
\N	\N	f	\N	0	f	50	818	2
\N	\N	f	\N	0	f	50	819	4
\N	\N	f	\N	0	f	50	821	1
\N	\N	f	\N	0	f	50	825	1
\N	\N	f	\N	0	f	50	829	0
\N	\N	f	\N	0	f	50	830	14
\N	\N	f	\N	0	f	50	831	-1
\N	\N	f	\N	0	f	50	836	4
\N	\N	f	\N	0	f	50	840	14
\N	\N	f	\N	0	f	50	843	2
\N	\N	f	\N	0	f	50	844	2
\N	\N	f	\N	0	f	50	847	11
\N	\N	f	\N	0	f	50	849	14
\N	\N	f	\N	0	f	50	851	0
\N	\N	f	\N	0	f	50	854	3
\N	\N	f	\N	0	f	50	859	8
\N	\N	f	\N	0	f	50	868	30
\N	\N	f	\N	0	f	50	870	30
\N	\N	f	\N	0	f	50	873	30
\N	\N	f	\N	0	f	50	874	99
\N	\N	f	\N	0	f	50	878	30
\N	\N	f	\N	0	f	50	879	9
\N	\N	f	\N	0	f	50	880	-1
\N	\N	f	\N	0	f	50	888	-1
\N	\N	f	\N	0	f	51	501	27
\N	\N	f	\N	0	f	51	502	25
\N	\N	f	\N	0	f	51	504	30
\N	\N	f	\N	0	f	51	505	30
\N	\N	f	\N	0	f	51	506	25
\N	\N	f	\N	0	f	51	507	30
\N	\N	f	\N	0	f	51	508	30
\N	\N	f	\N	0	f	51	509	12
\N	\N	f	\N	0	f	51	510	14
\N	\N	f	\N	0	f	51	512	30
\N	\N	f	\N	0	f	51	555	1
\N	\N	f	\N	0	f	51	559	14
\N	\N	f	\N	0	f	51	560	1
\N	\N	f	\N	0	f	51	564	10
\N	\N	f	\N	0	f	51	566	9
\N	\N	f	\N	0	f	51	569	15
\N	\N	f	\N	0	f	51	571	11
\N	\N	f	\N	0	f	51	573	14
\N	\N	f	\N	0	f	51	578	14
\N	\N	f	\N	0	f	51	582	4
\N	\N	f	\N	0	f	51	583	14
\N	\N	f	\N	0	f	51	584	11
\N	\N	f	\N	0	f	51	585	14
\N	\N	f	\N	0	f	51	587	15
\N	\N	f	\N	0	f	51	588	19
\N	\N	f	\N	0	f	51	589	19
\N	\N	f	\N	0	f	51	591	4
\N	\N	f	\N	0	f	51	592	9
\N	\N	f	\N	0	f	51	593	3
\N	\N	f	\N	0	f	51	595	1
\N	\N	f	\N	0	f	51	598	2
\N	\N	f	\N	0	f	51	599	14
\N	\N	f	\N	0	f	51	600	1
\N	\N	f	\N	0	f	51	601	11
\N	\N	f	\N	0	f	51	602	14
\N	\N	f	\N	0	f	51	603	11
\N	\N	f	\N	0	f	51	604	14
\N	\N	f	\N	0	f	51	605	14
\N	\N	f	\N	0	f	51	609	14
\N	\N	f	\N	0	f	51	610	3
\N	\N	f	\N	0	f	51	613	9
\N	\N	f	\N	0	f	51	614	4
\N	\N	f	\N	0	f	51	617	14
\N	\N	f	\N	0	f	51	620	11
\N	\N	f	\N	0	f	51	623	14
\N	\N	f	\N	0	f	51	625	1
\N	\N	f	\N	0	f	51	626	8
\N	\N	f	\N	0	f	51	628	4
\N	\N	f	\N	0	f	51	632	15
\N	\N	f	\N	0	f	51	633	11
\N	\N	f	\N	0	f	51	634	1
\N	\N	f	\N	0	f	51	641	8
\N	\N	f	\N	0	f	51	642	9
\N	\N	f	\N	0	f	51	643	12
\N	\N	f	\N	0	f	51	648	28
\N	\N	f	\N	0	f	51	649	11
\N	\N	f	\N	0	f	51	651	4
\N	\N	f	\N	0	f	51	653	1
\N	\N	f	\N	0	f	51	654	15
\N	\N	f	\N	0	f	51	661	1
\N	\N	f	\N	0	f	51	663	4
\N	\N	f	\N	0	f	51	664	2
\N	\N	f	\N	0	f	51	667	1
\N	\N	f	\N	0	f	51	668	9
\N	\N	f	\N	0	f	51	669	9
\N	\N	f	\N	0	f	51	670	2
\N	\N	f	\N	0	f	51	673	14
\N	\N	f	\N	0	f	51	675	3
\N	\N	f	\N	0	f	51	676	15
\N	\N	f	\N	0	f	51	685	14
\N	\N	f	\N	0	f	51	686	14
\N	\N	f	\N	0	f	51	688	2
\N	\N	f	\N	0	f	51	689	15
\N	\N	f	\N	0	f	51	690	15
\N	\N	f	\N	0	f	51	691	1
\N	\N	f	\N	0	f	51	692	4
\N	\N	f	\N	0	f	51	695	9
\N	\N	f	\N	0	f	51	697	3
\N	\N	f	\N	0	f	51	698	8
\N	\N	f	\N	0	f	51	706	8
\N	\N	f	\N	0	f	51	707	2
\N	\N	f	\N	0	f	51	710	3
\N	\N	f	\N	0	f	51	718	14
\N	\N	f	\N	0	f	51	723	12
\N	\N	f	\N	0	f	51	724	8
\N	\N	f	\N	0	f	51	728	15
\N	\N	f	\N	0	f	51	731	15
\N	\N	f	\N	0	f	51	732	15
\N	\N	f	\N	0	f	51	739	1
\N	\N	f	\N	0	f	51	741	2
\N	\N	f	\N	0	f	51	748	28
\N	\N	f	\N	0	f	51	749	2
\N	\N	f	\N	0	f	51	750	4
\N	\N	f	\N	0	f	51	751	8
\N	\N	f	\N	0	f	51	753	8
\N	\N	f	\N	0	f	51	754	4
\N	\N	f	\N	0	f	51	758	14
\N	\N	f	\N	0	f	51	761	2
\N	\N	f	\N	0	f	51	767	9
\N	\N	f	\N	0	f	51	769	14
\N	\N	f	\N	0	f	51	774	14
\N	\N	f	\N	0	f	51	775	8
\N	\N	f	\N	0	f	51	776	8
\N	\N	f	\N	0	f	51	778	14
\N	\N	f	\N	0	f	51	779	14
\N	\N	f	\N	0	f	51	780	3
\N	\N	f	\N	0	f	51	781	3
\N	\N	f	\N	0	f	51	783	2
\N	\N	f	\N	0	f	51	786	15
\N	\N	f	\N	0	f	51	790	3
\N	\N	f	\N	0	f	51	792	10
\N	\N	f	\N	0	f	51	800	4
\N	\N	f	\N	0	f	51	813	11
\N	\N	f	\N	0	f	51	814	14
\N	\N	f	\N	0	f	51	817	3
\N	\N	f	\N	0	f	51	818	2
\N	\N	f	\N	0	f	51	821	1
\N	\N	f	\N	0	f	51	829	0
\N	\N	f	\N	0	f	51	830	14
\N	\N	f	\N	0	f	51	834	4
\N	\N	f	\N	0	f	51	836	2
\N	\N	f	\N	0	f	51	837	1
\N	\N	f	\N	0	f	51	838	4
\N	\N	f	\N	0	f	51	840	14
\N	\N	f	\N	0	f	51	841	9
\N	\N	f	\N	0	f	51	843	2
\N	\N	f	\N	0	f	51	844	8
\N	\N	f	\N	0	f	51	847	8
\N	\N	f	\N	0	f	51	849	14
\N	\N	f	\N	0	f	51	851	0
\N	\N	f	\N	0	f	51	852	3
\N	\N	f	\N	0	f	51	854	3
\N	\N	f	\N	0	f	51	868	30
\N	\N	f	\N	0	f	51	869	2
\N	\N	f	\N	0	f	51	870	30
\N	\N	f	\N	0	f	51	873	30
\N	\N	f	\N	0	f	51	874	99
\N	\N	f	\N	0	f	51	878	30
\N	\N	f	\N	0	f	51	879	9
\N	\N	f	\N	0	f	51	883	-1
\N	\N	f	\N	0	f	51	884	-1
\N	\N	f	\N	0	f	52	501	27
\N	\N	f	\N	0	f	52	502	25
\N	\N	f	\N	0	f	52	504	30
\N	\N	f	\N	0	f	52	505	30
\N	\N	f	\N	0	f	52	506	25
\N	\N	f	\N	0	f	52	507	30
\N	\N	f	\N	0	f	52	508	30
\N	\N	f	\N	0	f	52	510	14
\N	\N	f	\N	0	f	52	512	30
\N	\N	f	\N	0	f	52	554	8
\N	\N	f	\N	0	f	52	555	1
\N	\N	f	\N	0	f	52	559	14
\N	\N	f	\N	0	f	52	564	10
\N	\N	f	\N	0	f	52	566	9
\N	\N	f	\N	0	f	52	569	15
\N	\N	f	\N	0	f	52	573	14
\N	\N	f	\N	0	f	52	578	14
\N	\N	f	\N	0	f	52	583	14
\N	\N	f	\N	0	f	52	584	11
\N	\N	f	\N	0	f	52	585	14
\N	\N	f	\N	0	f	52	588	19
\N	\N	f	\N	0	f	52	589	19
\N	\N	f	\N	0	f	52	595	1
\N	\N	f	\N	0	f	52	599	14
\N	\N	f	\N	0	f	52	600	2
\N	\N	f	\N	0	f	52	601	1
\N	\N	f	\N	0	f	52	602	14
\N	\N	f	\N	0	f	52	604	14
\N	\N	f	\N	0	f	52	605	14
\N	\N	f	\N	0	f	52	609	14
\N	\N	f	\N	0	f	52	610	3
\N	\N	f	\N	0	f	52	613	2
\N	\N	f	\N	0	f	52	614	2
\N	\N	f	\N	0	f	52	617	14
\N	\N	f	\N	0	f	52	620	2
\N	\N	f	\N	0	f	52	623	14
\N	\N	f	\N	0	f	52	630	4
\N	\N	f	\N	0	f	52	632	15
\N	\N	f	\N	0	f	52	637	11
\N	\N	f	\N	0	f	52	641	8
\N	\N	f	\N	0	f	52	642	9
\N	\N	f	\N	0	f	52	643	12
\N	\N	f	\N	0	f	52	644	1
\N	\N	f	\N	0	f	52	645	4
\N	\N	f	\N	0	f	52	648	28
\N	\N	f	\N	0	f	52	649	11
\N	\N	f	\N	0	f	52	653	1
\N	\N	f	\N	0	f	52	663	1
\N	\N	f	\N	0	f	52	664	3
\N	\N	f	\N	0	f	52	667	2
\N	\N	f	\N	0	f	52	668	9
\N	\N	f	\N	0	f	52	669	9
\N	\N	f	\N	0	f	52	670	3
\N	\N	f	\N	0	f	52	673	14
\N	\N	f	\N	0	f	52	684	10
\N	\N	f	\N	0	f	52	686	14
\N	\N	f	\N	0	f	52	692	4
\N	\N	f	\N	0	f	52	695	9
\N	\N	f	\N	0	f	52	696	14
\N	\N	f	\N	0	f	52	698	8
\N	\N	f	\N	0	f	52	706	8
\N	\N	f	\N	0	f	52	707	4
\N	\N	f	\N	0	f	52	710	3
\N	\N	f	\N	0	f	52	718	14
\N	\N	f	\N	0	f	52	721	2
\N	\N	f	\N	0	f	52	723	12
\N	\N	f	\N	0	f	52	724	8
\N	\N	f	\N	0	f	52	725	15
\N	\N	f	\N	0	f	52	728	15
\N	\N	f	\N	0	f	52	731	15
\N	\N	f	\N	0	f	52	732	15
\N	\N	f	\N	0	f	52	741	1
\N	\N	f	\N	0	f	52	748	28
\N	\N	f	\N	0	f	52	751	8
\N	\N	f	\N	0	f	52	753	8
\N	\N	f	\N	0	f	52	754	2
\N	\N	f	\N	0	f	52	755	1
\N	\N	f	\N	0	f	52	758	14
\N	\N	f	\N	0	f	52	767	9
\N	\N	f	\N	0	f	52	769	14
\N	\N	f	\N	0	f	52	773	28
\N	\N	f	\N	0	f	52	774	14
\N	\N	f	\N	0	f	52	775	8
\N	\N	f	\N	0	f	52	776	8
\N	\N	f	\N	0	f	52	778	14
\N	\N	f	\N	0	f	52	779	14
\N	\N	f	\N	0	f	52	780	3
\N	\N	f	\N	0	f	52	781	3
\N	\N	f	\N	0	f	52	786	15
\N	\N	f	\N	0	f	52	790	4
\N	\N	f	\N	0	f	52	792	10
\N	\N	f	\N	0	f	52	805	-1
\N	\N	f	\N	0	f	52	814	14
\N	\N	f	\N	0	f	52	821	1
\N	\N	f	\N	0	f	52	827	3
\N	\N	f	\N	0	f	52	828	9
\N	\N	f	\N	0	f	52	830	14
\N	\N	f	\N	0	f	52	831	-1
\N	\N	f	\N	0	f	52	836	4
\N	\N	f	\N	0	f	52	840	14
\N	\N	f	\N	0	f	52	843	2
\N	\N	f	\N	0	f	52	844	2
\N	\N	f	\N	0	f	52	847	8
\N	\N	f	\N	0	f	52	849	14
\N	\N	f	\N	0	f	52	851	15
\N	\N	f	\N	0	f	52	852	3
\N	\N	f	\N	0	f	52	854	15
\N	\N	f	\N	0	f	52	855	11
\N	\N	f	\N	0	f	52	861	11
\N	\N	f	\N	0	f	52	865	4
\N	\N	f	\N	0	f	52	868	30
\N	\N	f	\N	0	f	52	869	4
\N	\N	f	\N	0	f	52	870	30
\N	\N	f	\N	0	f	52	873	30
\N	\N	f	\N	0	f	52	874	99
\N	\N	f	\N	0	f	52	878	30
\N	\N	f	\N	0	f	52	879	9
\N	\N	f	\N	0	f	52	885	-1
\N	\N	f	\N	0	f	53	501	27
\N	\N	f	\N	0	f	53	502	25
\N	\N	f	\N	0	f	53	504	30
\N	\N	f	\N	0	f	53	505	30
\N	\N	f	\N	0	f	53	506	25
\N	\N	f	\N	0	f	53	507	30
\N	\N	f	\N	0	f	53	508	30
\N	\N	f	\N	0	f	53	510	14
\N	\N	f	\N	0	f	53	512	30
\N	\N	f	\N	0	f	53	554	8
\N	\N	f	\N	0	f	53	555	1
\N	\N	f	\N	0	f	53	559	14
\N	\N	f	\N	0	f	53	564	10
\N	\N	f	\N	0	f	53	566	9
\N	\N	f	\N	0	f	53	569	15
\N	\N	f	\N	0	f	53	573	14
\N	\N	f	\N	0	f	53	578	14
\N	\N	f	\N	0	f	53	583	14
\N	\N	f	\N	0	f	53	585	14
\N	\N	f	\N	0	f	53	588	19
\N	\N	f	\N	0	f	53	589	19
\N	\N	f	\N	0	f	53	595	1
\N	\N	f	\N	0	f	53	599	14
\N	\N	f	\N	0	f	53	601	1
\N	\N	f	\N	0	f	53	602	14
\N	\N	f	\N	0	f	53	604	14
\N	\N	f	\N	0	f	53	609	14
\N	\N	f	\N	0	f	53	610	3
\N	\N	f	\N	0	f	53	613	2
\N	\N	f	\N	0	f	53	614	2
\N	\N	f	\N	0	f	53	617	14
\N	\N	f	\N	0	f	53	620	2
\N	\N	f	\N	0	f	53	623	14
\N	\N	f	\N	0	f	53	628	4
\N	\N	f	\N	0	f	53	630	3
\N	\N	f	\N	0	f	53	632	15
\N	\N	f	\N	0	f	53	641	8
\N	\N	f	\N	0	f	53	642	9
\N	\N	f	\N	0	f	53	643	12
\N	\N	f	\N	0	f	53	644	1
\N	\N	f	\N	0	f	53	645	4
\N	\N	f	\N	0	f	53	648	28
\N	\N	f	\N	0	f	53	649	11
\N	\N	f	\N	0	f	53	653	1
\N	\N	f	\N	0	f	53	667	2
\N	\N	f	\N	0	f	53	668	9
\N	\N	f	\N	0	f	53	669	9
\N	\N	f	\N	0	f	53	673	14
\N	\N	f	\N	0	f	53	684	10
\N	\N	f	\N	0	f	53	686	14
\N	\N	f	\N	0	f	53	695	9
\N	\N	f	\N	0	f	53	706	8
\N	\N	f	\N	0	f	53	710	3
\N	\N	f	\N	0	f	53	721	2
\N	\N	f	\N	0	f	53	723	12
\N	\N	f	\N	0	f	53	724	8
\N	\N	f	\N	0	f	53	725	15
\N	\N	f	\N	0	f	53	728	15
\N	\N	f	\N	0	f	53	750	4
\N	\N	f	\N	0	f	53	751	8
\N	\N	f	\N	0	f	53	753	8
\N	\N	f	\N	0	f	53	755	1
\N	\N	f	\N	0	f	53	758	14
\N	\N	f	\N	0	f	53	767	9
\N	\N	f	\N	0	f	53	769	14
\N	\N	f	\N	0	f	53	773	28
\N	\N	f	\N	0	f	53	774	14
\N	\N	f	\N	0	f	53	775	8
\N	\N	f	\N	0	f	53	776	8
\N	\N	f	\N	0	f	53	778	14
\N	\N	f	\N	0	f	53	779	14
\N	\N	f	\N	0	f	53	780	3
\N	\N	f	\N	0	f	53	786	15
\N	\N	f	\N	0	f	53	792	10
\N	\N	f	\N	0	f	53	813	11
\N	\N	f	\N	0	f	53	814	14
"Hello Angela"	\N	f	\N	0	f	53	820	28
\N	\N	f	\N	0	f	53	821	1
\N	\N	f	\N	0	f	53	827	3
\N	\N	f	\N	0	f	53	828	9
\N	\N	f	\N	0	f	53	831	-1
\N	\N	f	\N	0	f	53	834	11
\N	\N	f	\N	0	f	53	836	4
\N	\N	f	\N	0	f	53	840	14
\N	\N	f	\N	0	f	53	843	2
\N	\N	f	\N	0	f	53	844	2
\N	\N	f	\N	0	f	53	849	14
\N	\N	f	\N	0	f	53	851	15
\N	\N	f	\N	0	f	53	854	15
\N	\N	f	\N	0	f	53	861	8
\N	\N	f	\N	0	f	53	865	4
\N	\N	f	\N	0	f	53	868	30
\N	\N	f	\N	0	f	53	870	30
\N	\N	f	\N	0	f	53	873	30
\N	\N	f	\N	0	f	53	874	99
\N	\N	f	\N	0	f	53	878	30
\N	\N	f	\N	0	f	53	879	9
\N	\N	f	\N	0	f	54	504	30
\N	\N	f	\N	0	f	54	561	14
\N	\N	f	\N	0	f	54	662	14
\N	\N	f	\N	0	f	54	762	14
\N	\N	f	\N	0	f	60	501	27
\N	\N	f	\N	0	f	60	502	25
\N	\N	f	\N	0	f	60	504	27
\N	\N	f	\N	0	f	60	505	30
\N	\N	f	\N	0	f	60	506	25
\N	\N	f	\N	0	f	60	507	30
\N	\N	f	\N	0	f	60	510	14
\N	\N	f	\N	0	f	60	512	30
\N	\N	f	\N	0	f	60	555	1
\N	\N	f	\N	0	f	60	559	14
\N	\N	f	\N	0	f	60	564	10
\N	\N	f	\N	0	f	60	566	9
\N	\N	f	\N	0	f	60	568	1
\N	\N	f	\N	0	f	60	569	15
\N	\N	f	\N	0	f	60	573	14
\N	\N	f	\N	0	f	60	578	14
\N	\N	f	\N	0	f	60	582	2
\N	\N	f	\N	0	f	60	583	14
\N	\N	f	\N	0	f	60	585	14
\N	\N	f	\N	0	f	60	590	15
\N	\N	f	\N	0	f	60	595	4
\N	\N	f	\N	0	f	60	599	14
\N	\N	f	\N	0	f	60	600	3
\N	\N	f	\N	0	f	60	601	2
\N	\N	f	\N	0	f	60	602	14
\N	\N	f	\N	0	f	60	603	11
\N	\N	f	\N	0	f	60	604	14
\N	\N	f	\N	0	f	60	605	14
\N	\N	f	\N	0	f	60	609	14
\N	\N	f	\N	0	f	60	610	3
\N	\N	f	\N	0	f	60	614	3
\N	\N	f	\N	0	f	60	617	14
\N	\N	f	\N	0	f	60	620	2
\N	\N	f	\N	0	f	60	623	14
\N	\N	f	\N	0	f	60	624	15
\N	\N	f	\N	0	f	60	632	15
\N	\N	f	\N	0	f	60	634	2
\N	\N	f	\N	0	f	60	641	8
\N	\N	f	\N	0	f	60	642	9
\N	\N	f	\N	0	f	60	643	12
\N	\N	f	\N	0	f	60	648	28
\N	\N	f	\N	0	f	60	649	11
\N	\N	f	\N	0	f	60	653	1
\N	\N	f	\N	0	f	60	663	4
\N	\N	f	\N	0	f	60	668	9
\N	\N	f	\N	0	f	60	669	9
\N	\N	f	\N	0	f	60	673	14
\N	\N	f	\N	0	f	60	684	10
\N	\N	f	\N	0	f	60	685	11
\N	\N	f	\N	0	f	60	686	14
\N	\N	f	\N	0	f	60	688	11
\N	\N	f	\N	0	f	60	689	15
\N	\N	f	\N	0	f	60	692	1
\N	\N	f	\N	0	f	60	695	9
\N	\N	f	\N	0	f	60	696	14
\N	\N	f	\N	0	f	60	698	8
\N	\N	f	\N	0	f	60	706	8
\N	\N	f	\N	0	f	60	707	11
\N	\N	f	\N	0	f	60	710	9
""	\N	f	\N	0	f	60	720	15
\N	\N	f	\N	0	f	60	722	4
\N	\N	f	\N	0	f	60	723	12
\N	\N	f	\N	0	f	60	724	8
\N	\N	f	\N	0	f	60	725	15
\N	\N	f	\N	0	f	60	728	15
\N	\N	f	\N	0	f	60	729	25
\N	\N	f	\N	0	f	60	741	1
\N	\N	f	\N	0	f	60	748	28
\N	\N	f	\N	0	f	60	750	4
\N	\N	f	\N	0	f	60	751	8
\N	\N	f	\N	0	f	60	752	3
\N	\N	f	\N	0	f	60	753	8
\N	\N	f	\N	0	f	60	754	1
\N	\N	f	\N	0	f	60	755	3
\N	\N	f	\N	0	f	60	758	14
\N	\N	f	\N	0	f	60	761	4
\N	\N	f	\N	0	f	60	763	25
\N	\N	f	\N	0	f	60	767	8
\N	\N	f	\N	0	f	60	769	14
\N	\N	f	\N	0	f	60	773	28
\N	\N	f	\N	0	f	60	774	14
\N	\N	f	\N	0	f	60	778	14
\N	\N	f	\N	0	f	60	779	14
\N	\N	f	\N	0	f	60	780	3
\N	\N	f	\N	0	f	60	782	12
\N	\N	f	\N	0	f	60	786	15
\N	\N	f	\N	0	f	60	790	11
\N	\N	f	\N	0	f	60	791	8
\N	\N	f	\N	0	f	60	792	10
\N	\N	f	\N	0	f	60	794	2
\N	\N	f	\N	0	f	60	795	15
\N	\N	f	\N	0	f	60	804	3
\N	\N	f	\N	0	f	60	808	15
\N	\N	f	\N	0	f	60	813	11
\N	\N	f	\N	0	f	60	814	14
"Hello Angela"	\N	f	\N	0	f	60	820	28
\N	\N	f	\N	0	f	60	821	1
\N	\N	f	\N	0	f	60	827	2
\N	\N	f	\N	0	f	60	828	9
\N	\N	f	\N	0	f	60	830	14
\N	\N	f	\N	0	f	60	831	-1
\N	\N	f	\N	0	f	60	834	4
\N	\N	f	\N	0	f	60	836	2
\N	\N	f	\N	0	f	60	840	14
\N	\N	f	\N	0	f	60	843	2
\N	\N	f	\N	0	f	60	844	1
\N	\N	f	\N	0	f	60	849	14
\N	\N	f	\N	0	f	60	854	4
\N	\N	f	\N	0	f	60	862	4
\N	\N	f	\N	0	f	60	868	30
\N	\N	f	\N	0	f	60	870	30
\N	\N	f	\N	0	f	60	873	30
\N	\N	f	\N	0	f	60	874	99
\N	\N	f	\N	0	f	60	878	30
\N	\N	f	\N	0	f	60	879	9
\N	\N	f	\N	0	f	61	501	27
\N	\N	f	\N	0	f	61	502	25
\N	\N	f	\N	0	f	61	504	27
\N	\N	f	\N	0	f	61	505	30
\N	\N	f	\N	0	f	61	506	25
\N	\N	f	\N	0	f	61	507	30
\N	\N	f	\N	0	f	61	509	11
\N	\N	f	\N	0	f	61	510	14
\N	\N	f	\N	0	f	61	512	30
\N	\N	f	\N	0	f	61	555	1
\N	\N	f	\N	0	f	61	558	1
\N	\N	f	\N	0	f	61	559	14
\N	\N	f	\N	0	f	61	564	10
\N	\N	f	\N	0	f	61	565	15
\N	\N	f	\N	0	f	61	566	9
\N	\N	f	\N	0	f	61	569	15
\N	\N	f	\N	0	f	61	573	14
\N	\N	f	\N	0	f	61	578	14
\N	\N	f	\N	0	f	61	583	14
\N	\N	f	\N	0	f	61	585	14
\N	\N	f	\N	0	f	61	587	15
\N	\N	f	\N	0	f	61	590	15
\N	\N	f	\N	0	f	61	595	4
\N	\N	f	\N	0	f	61	599	14
\N	\N	f	\N	0	f	61	602	14
\N	\N	f	\N	0	f	61	603	11
\N	\N	f	\N	0	f	61	604	14
\N	\N	f	\N	0	f	61	605	14
\N	\N	f	\N	0	f	61	609	14
\N	\N	f	\N	0	f	61	610	3
\N	\N	f	\N	0	f	61	614	4
\N	\N	f	\N	0	f	61	617	14
\N	\N	f	\N	0	f	61	619	15
\N	\N	f	\N	0	f	61	620	1
\N	\N	f	\N	0	f	61	623	14
\N	\N	f	\N	0	f	61	628	1
\N	\N	f	\N	0	f	61	632	15
\N	\N	f	\N	0	f	61	641	8
\N	\N	f	\N	0	f	61	642	9
\N	\N	f	\N	0	f	61	643	12
\N	\N	f	\N	0	f	61	648	28
\N	\N	f	\N	0	f	61	649	11
\N	\N	f	\N	0	f	61	653	1
\N	\N	f	\N	0	f	61	654	15
\N	\N	f	\N	0	f	61	668	9
\N	\N	f	\N	0	f	61	669	9
\N	\N	f	\N	0	f	61	671	2
\N	\N	f	\N	0	f	61	673	14
\N	\N	f	\N	0	f	61	676	15
\N	\N	f	\N	0	f	61	684	10
\N	\N	f	\N	0	f	61	685	12
\N	\N	f	\N	0	f	61	686	14
\N	\N	f	\N	0	f	61	689	15
\N	\N	f	\N	0	f	61	690	15
\N	\N	f	\N	0	f	61	695	9
\N	\N	f	\N	0	f	61	698	8
\N	\N	f	\N	0	f	61	703	2
\N	\N	f	\N	0	f	61	704	2
\N	\N	f	\N	0	f	61	706	8
\N	\N	f	\N	0	f	61	707	11
\N	\N	f	\N	0	f	61	710	9
\N	\N	f	\N	0	f	61	720	1
\N	\N	f	\N	0	f	61	722	3
\N	\N	f	\N	0	f	61	723	12
\N	\N	f	\N	0	f	61	724	8
\N	\N	f	\N	0	f	61	725	15
\N	\N	f	\N	0	f	61	728	15
\N	\N	f	\N	0	f	61	729	25
\N	\N	f	\N	0	f	61	748	28
\N	\N	f	\N	0	f	61	750	4
\N	\N	f	\N	0	f	61	751	8
\N	\N	f	\N	0	f	61	753	8
\N	\N	f	\N	0	f	61	754	3
\N	\N	f	\N	0	f	61	755	2
\N	\N	f	\N	0	f	61	758	14
\N	\N	f	\N	0	f	61	761	4
\N	\N	f	\N	0	f	61	763	25
\N	\N	f	\N	0	f	61	767	8
\N	\N	f	\N	0	f	61	769	14
\N	\N	f	\N	0	f	61	773	28
\N	\N	f	\N	0	f	61	774	14
\N	\N	f	\N	0	f	61	778	14
\N	\N	f	\N	0	f	61	779	14
\N	\N	f	\N	0	f	61	780	3
\N	\N	f	\N	0	f	61	782	12
\N	\N	f	\N	0	f	61	786	15
\N	\N	f	\N	0	f	61	790	2
\N	\N	f	\N	0	f	61	792	10
\N	\N	f	\N	0	f	61	795	15
\N	\N	f	\N	0	f	61	798	1
\N	\N	f	\N	0	f	61	799	1
\N	\N	f	\N	0	f	61	804	4
\N	\N	f	\N	0	f	61	808	8
\N	\N	f	\N	0	f	61	814	14
"Hello Angela"	\N	f	\N	0	f	61	820	28
\N	\N	f	\N	0	f	61	821	1
\N	\N	f	\N	0	f	61	825	4
\N	\N	f	\N	0	f	61	827	2
\N	\N	f	\N	0	f	61	828	9
\N	\N	f	\N	0	f	61	830	14
\N	\N	f	\N	0	f	61	831	-1
\N	\N	f	\N	0	f	61	836	3
\N	\N	f	\N	0	f	61	840	14
\N	\N	f	\N	0	f	61	843	2
\N	\N	f	\N	0	f	61	844	3
\N	\N	f	\N	0	f	61	849	14
\N	\N	f	\N	0	f	61	854	4
\N	\N	f	\N	0	f	61	868	30
\N	\N	f	\N	0	f	61	870	30
\N	\N	f	\N	0	f	61	873	30
\N	\N	f	\N	0	f	61	874	99
\N	\N	f	\N	0	f	61	878	30
\N	\N	f	\N	0	f	61	879	9
\N	\N	f	\N	0	f	61	883	-1
\N	\N	f	\N	0	f	62	501	38
\N	\N	f	\N	0	f	62	502	25
\N	\N	f	\N	0	f	62	504	30
\N	\N	f	\N	0	f	62	505	30
\N	\N	f	\N	0	f	62	506	25
\N	\N	f	\N	0	f	62	507	30
\N	\N	f	\N	0	f	62	510	14
\N	\N	f	\N	0	f	62	512	30
\N	\N	f	\N	0	f	62	558	38
\N	\N	f	\N	0	f	62	559	14
\N	\N	f	\N	0	f	62	565	38
\N	\N	f	\N	0	f	62	573	14
\N	\N	f	\N	0	f	62	578	14
\N	\N	f	\N	0	f	62	583	14
\N	\N	f	\N	0	f	62	585	14
\N	\N	f	\N	0	f	62	590	38
\N	\N	f	\N	0	f	62	599	14
\N	\N	f	\N	0	f	62	604	14
\N	\N	f	\N	0	f	62	605	14
\N	\N	f	\N	0	f	62	609	14
\N	\N	f	\N	0	f	62	614	38
\N	\N	f	\N	0	f	62	619	38
\N	\N	f	\N	0	f	62	620	38
\N	\N	f	\N	0	f	62	623	14
\N	\N	f	\N	0	f	62	628	38
\N	\N	f	\N	0	f	62	632	38
\N	\N	f	\N	0	f	62	668	38
\N	\N	f	\N	0	f	62	669	38
\N	\N	f	\N	0	f	62	673	14
\N	\N	f	\N	0	f	62	676	38
\N	\N	f	\N	0	f	62	679	30
\N	\N	f	\N	0	f	62	685	38
\N	\N	f	\N	0	f	62	689	38
\N	\N	f	\N	0	f	62	695	38
\N	\N	f	\N	0	f	62	698	8
\N	\N	f	\N	0	f	62	716	38
\N	\N	f	\N	0	f	62	750	38
\N	\N	f	\N	0	f	62	753	8
\N	\N	f	\N	0	f	62	755	38
\N	\N	f	\N	0	f	62	758	14
\N	\N	f	\N	0	f	62	763	25
\N	\N	f	\N	0	f	62	773	28
\N	\N	f	\N	0	f	62	786	38
\N	\N	f	\N	0	f	62	792	38
\N	\N	f	\N	0	f	62	795	8
\N	\N	f	\N	0	f	62	814	14
"Hello Angela"	\N	f	\N	0	f	62	820	28
\N	\N	f	\N	0	f	62	831	-1
\N	\N	f	\N	0	f	62	840	14
\N	\N	f	\N	0	f	62	843	2
\N	\N	f	\N	0	f	62	844	38
\N	\N	f	\N	0	f	62	849	14
\N	\N	f	\N	0	f	62	868	30
\N	\N	f	\N	0	f	62	870	30
\N	\N	f	\N	0	f	62	878	30
\N	\N	f	\N	0	f	63	504	30
\N	\N	f	\N	0	f	63	507	30
\N	\N	f	\N	0	f	63	662	14
\N	\N	f	\N	0	f	71	501	38
\N	\N	f	\N	0	f	71	502	38
\N	\N	f	\N	0	f	71	504	30
\N	\N	f	\N	0	f	71	505	30
\N	\N	f	\N	0	f	71	506	38
\N	\N	f	\N	0	f	71	507	30
\N	\N	f	\N	0	f	71	508	30
\N	\N	f	\N	0	f	71	512	30
\N	\N	f	\N	0	f	71	573	14
\N	\N	f	\N	0	f	71	578	14
\N	\N	f	\N	0	f	71	583	14
\N	\N	f	\N	0	f	71	585	14
\N	\N	f	\N	0	f	71	588	38
\N	\N	f	\N	0	f	71	589	38
\N	\N	f	\N	0	f	71	599	14
\N	\N	f	\N	0	f	71	601	38
\N	\N	f	\N	0	f	71	609	14
\N	\N	f	\N	0	f	71	617	14
\N	\N	f	\N	0	f	71	620	38
\N	\N	f	\N	0	f	71	623	14
\N	\N	f	\N	0	f	71	644	38
\N	\N	f	\N	0	f	71	655	38
\N	\N	f	\N	0	f	71	661	38
\N	\N	f	\N	0	f	71	679	30
\N	\N	f	\N	0	f	71	684	38
\N	\N	f	\N	0	f	71	695	38
\N	\N	f	\N	0	f	71	698	8
\N	\N	f	\N	0	f	71	707	38
\N	\N	f	\N	0	f	71	722	38
\N	\N	f	\N	0	f	71	753	8
\N	\N	f	\N	0	f	71	773	28
\N	\N	f	\N	0	f	71	814	14
"Hello Angela"	\N	f	\N	0	f	71	820	28
\N	\N	f	\N	0	f	71	830	38
\N	\N	f	\N	0	f	71	831	-1
\N	\N	f	\N	0	f	71	840	38
\N	\N	f	\N	0	f	71	844	38
\N	\N	f	\N	0	f	71	849	14
\N	\N	f	\N	0	f	71	868	30
\N	\N	f	\N	0	f	71	870	30
\N	\N	f	\N	0	f	71	871	30
\N	\N	f	\N	0	f	71	873	30
\N	\N	f	\N	0	f	71	874	99
\N	\N	f	\N	0	f	71	878	30
\N	\N	f	\N	0	f	71	880	-1
\N	\N	f	\N	0	f	72	501	38
\N	\N	f	\N	0	f	72	502	38
\N	\N	f	\N	0	f	72	504	30
\N	\N	f	\N	0	f	72	505	30
\N	\N	f	\N	0	f	72	506	38
\N	\N	f	\N	0	f	72	507	30
\N	\N	f	\N	0	f	72	508	30
\N	\N	f	\N	0	f	72	512	28
\N	\N	f	\N	0	f	72	583	14
\N	\N	f	\N	0	f	72	585	14
\N	\N	f	\N	0	f	72	599	14
\N	\N	f	\N	0	f	72	601	38
\N	\N	f	\N	0	f	72	617	14
\N	\N	f	\N	0	f	72	673	14
\N	\N	f	\N	0	f	72	679	30
\N	\N	f	\N	0	f	72	684	38
\N	\N	f	\N	0	f	72	695	38
\N	\N	f	\N	0	f	72	698	8
\N	\N	f	\N	0	f	72	707	38
\N	\N	f	\N	0	f	72	714	38
\N	\N	f	\N	0	f	72	753	8
\N	\N	f	\N	0	f	72	773	28
\N	\N	f	\N	0	f	72	814	14
"Hello Angela"	\N	f	\N	0	f	72	820	28
\N	\N	f	\N	0	f	72	830	38
\N	\N	f	\N	0	f	72	831	-1
\N	\N	f	\N	0	f	72	844	38
\N	\N	f	\N	0	f	72	849	14
\N	\N	f	\N	0	f	72	868	30
\N	\N	f	\N	0	f	72	870	30
\N	\N	f	\N	0	f	72	871	30
\N	\N	f	\N	0	f	72	873	30
\N	\N	f	\N	0	f	72	874	99
\N	\N	f	\N	0	f	72	878	30
\N	\N	f	\N	0	f	73	501	38
\N	\N	f	\N	0	f	73	504	30
\N	\N	f	\N	0	f	73	505	30
\N	\N	f	\N	0	f	73	506	38
\N	\N	f	\N	0	f	73	507	30
\N	\N	f	\N	0	f	73	508	30
\N	\N	f	\N	0	f	73	512	30
\N	\N	f	\N	0	f	73	583	14
\N	\N	f	\N	0	f	73	585	14
\N	\N	f	\N	0	f	73	599	14
\N	\N	f	\N	0	f	73	673	14
\N	\N	f	\N	0	f	73	679	30
\N	\N	f	\N	0	f	73	684	38
\N	\N	f	\N	0	f	73	695	38
\N	\N	f	\N	0	f	73	707	38
\N	\N	f	\N	0	f	73	716	38
\N	\N	f	\N	0	f	73	773	28
\N	\N	f	\N	0	f	73	814	14
\N	\N	f	\N	0	f	73	844	38
\N	\N	f	\N	0	f	73	849	14
\N	\N	f	\N	0	f	73	868	30
\N	\N	f	\N	0	f	73	870	30
\N	\N	f	\N	0	f	73	871	30
\N	\N	f	\N	0	f	73	878	30
\N	\N	f	\N	0	f	74	504	30
\N	\N	f	\N	0	f	74	507	30
\N	\N	f	\N	0	f	80	501	38
\N	\N	f	\N	0	f	80	504	30
\N	\N	f	\N	0	f	80	505	30
\N	\N	f	\N	0	f	80	506	38
\N	\N	f	\N	0	f	80	507	30
\N	\N	f	\N	0	f	80	512	30
\N	\N	f	\N	0	f	80	583	14
\N	\N	f	\N	0	f	80	585	14
\N	\N	f	\N	0	f	80	588	38
\N	\N	f	\N	0	f	80	589	38
\N	\N	f	\N	0	f	80	599	14
\N	\N	f	\N	0	f	80	684	38
\N	\N	f	\N	0	f	80	698	38
\N	\N	f	\N	0	f	80	707	38
\N	\N	f	\N	0	f	80	814	38
\N	\N	f	\N	0	f	80	825	38
\N	\N	f	\N	0	f	80	844	38
\N	\N	f	\N	0	f	80	868	30
\N	\N	f	\N	0	f	80	870	30
\N	\N	f	\N	0	f	80	878	30
\N	\N	f	\N	0	f	81	504	30
\N	\N	f	\N	0	f	81	583	14
\N	\N	f	\N	0	f	81	585	14
\N	\N	f	\N	0	f	81	613	38
\N	\N	f	\N	0	f	81	647	38
\N	\N	f	\N	0	f	81	651	38
\N	\N	f	\N	0	f	81	707	38
\N	\N	f	\N	0	f	81	844	38
\N	\N	f	\N	0	f	81	870	30
\N	\N	f	\N	0	f	81	878	30
	\N	f	\N	0	f	41	720	13
	\N	f	\N	0	f	51	1751	2
	\N	f	\N	0	f	60	1751	4
	\N	f	\N	0	f	50	1751	10
\N	\N	f	\N	0	f	61	1751	-1
\N	\N	f	\N	0	f	52	1751	-1
	\N	f	\N	0	f	30	711	10
	\N	f	\N	0	f	40	590	14
\.


--
-- Name: admin_login admin_login_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.admin_login
    ADD CONSTRAINT admin_login_pkey PRIMARY KEY (id);


--
-- Name: area_session area_session_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.area_session
    ADD CONSTRAINT area_session_pkey PRIMARY KEY (areaid, sessionid);


--
-- Name: assignable_area assignable_area_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.assignable_area
    ADD CONSTRAINT assignable_area_pkey PRIMARY KEY (id);


--
-- Name: form_area form_area_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.form_area
    ADD CONSTRAINT form_area_pkey PRIMARY KEY (id);


--
-- Name: sequence sequence_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.sequence
    ADD CONSTRAINT sequence_pkey PRIMARY KEY (seq_name);


--
-- Name: session session_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.session
    ADD CONSTRAINT session_pkey PRIMARY KEY (id);


--
-- Name: volunteer_area volunteer_area_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_area
    ADD CONSTRAINT volunteer_area_pkey PRIMARY KEY (areaid, volunteerid);


--
-- Name: volunteer volunteer_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer
    ADD CONSTRAINT volunteer_pkey PRIMARY KEY (id);


--
-- Name: volunteer_session volunteer_session_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT volunteer_session_pkey PRIMARY KEY (sessionid, volunteerid);


--
-- Name: assigned_counts _RETURN; Type: RULE; Schema: public; Owner: staffing
--

CREATE RULE "_RETURN" AS
    ON SELECT TO public.assigned_counts DO INSTEAD  SELECT count(vs.volunteerid) AS assigned,
    sum((vs.worked)::integer) AS worked,
    ars.areaid,
    ars.sessionid,
    ars.required
   FROM (public.area_session ars
     LEFT JOIN public.volunteer_session vs ON (((vs.sessionid = ars.sessionid) AND (vs.areaid = ars.areaid))))
  GROUP BY ars.areaid, ars.sessionid;


--
-- Name: area_session fk_area_session_areaid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.area_session
    ADD CONSTRAINT fk_area_session_areaid FOREIGN KEY (areaid) REFERENCES public.assignable_area(id);


--
-- Name: area_session fk_area_session_sessionid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.area_session
    ADD CONSTRAINT fk_area_session_sessionid FOREIGN KEY (sessionid) REFERENCES public.session(id);


--
-- Name: assignable_area fk_assignable_area_formarea_id; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.assignable_area
    ADD CONSTRAINT fk_assignable_area_formarea_id FOREIGN KEY (formarea_id) REFERENCES public.form_area(id);


--
-- Name: volunteer_area fk_volunteer_area_areaid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_area
    ADD CONSTRAINT fk_volunteer_area_areaid FOREIGN KEY (areaid) REFERENCES public.assignable_area(id);


--
-- Name: volunteer_area fk_volunteer_area_volunteerid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_area
    ADD CONSTRAINT fk_volunteer_area_volunteerid FOREIGN KEY (volunteerid) REFERENCES public.volunteer(id);


--
-- Name: volunteer_session fk_volunteer_session_areaid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT fk_volunteer_session_areaid FOREIGN KEY (areaid, volunteerid) REFERENCES public.volunteer_area(areaid, volunteerid);


--
-- Name: volunteer_session fk_volunteer_session_sessionid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT fk_volunteer_session_sessionid FOREIGN KEY (sessionid) REFERENCES public.session(id);


--
-- Name: volunteer_session fk_volunteer_session_volunteerid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT fk_volunteer_session_volunteerid FOREIGN KEY (volunteerid) REFERENCES public.volunteer(id);


--
-- PostgreSQL database dump complete
--

