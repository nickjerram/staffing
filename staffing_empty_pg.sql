--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: assignable_area; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.assignable_area (
    id integer NOT NULL,
    anyone boolean not null default false,
    name character varying(50),
    public_facing boolean not null default false,
    short_name character varying(10),
    type integer,
    formarea_id integer
);


ALTER TABLE public.assignable_area OWNER TO staffing;

--
-- Name: form_area; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.form_area (
    id integer NOT NULL,
    dontmind boolean not null default false,
    name character varying(50)
);


ALTER TABLE public.form_area OWNER TO staffing;

--
-- Name: volunteer; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.volunteer (
    id integer NOT NULL,
    callsign character varying(40),
    camping integer,
    cellar boolean not null default false,
    comment text,
    confirmed boolean not null default false,
    email character varying(100),
    emailverified boolean not null default false,
    firstaid boolean not null default false,
    forename character varying(50),
    forklift boolean not null default false,
    instructions boolean not null default false,
    managervouch character varying(100),
    membership character varying(20),
    other boolean not null default false,
    password character varying(40),
    picture bytea,
    role character varying(40),
    sia boolean not null default false,
    surname character varying(50),
    tshirt integer,
    uuid character varying(36),
    verified boolean not null default false
);


ALTER TABLE public.volunteer OWNER TO staffing;

--
-- Name: volunteer_area; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.volunteer_area (
    areaid integer NOT NULL,
    volunteerid integer NOT NULL,
    preference integer
);


ALTER TABLE public.volunteer_area OWNER TO staffing;

--
-- Name: area_selector; Type: VIEW; Schema: public; Owner: nick
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


ALTER TABLE public.area_selector OWNER TO nick;

--
-- Name: area_session; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.area_session (
    required integer,
    areaid integer NOT NULL,
    sessionid integer NOT NULL
);


ALTER TABLE public.area_session OWNER TO staffing;

--
-- Name: assigned_counts; Type: TABLE; Schema: public; Owner: nick; Tablespace: 
--

CREATE TABLE public.assigned_counts (
    assigned bigint,
    worked bigint,
    areaid integer,
    sessionid integer,
    required integer
);


ALTER TABLE public.assigned_counts OWNER TO nick;

--
-- Name: session; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.session (
    id integer NOT NULL,
    finish timestamp without time zone,
    name character varying(50),
    night boolean not null default false,
    open boolean not null default false,
    setup boolean not null default false,
    special boolean not null default false,
    start timestamp without time zone,
    takedown boolean not null default false
);


ALTER TABLE public.session OWNER TO staffing;

--
-- Name: volunteer_session; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.volunteer_session (
    comment character varying(255),
    finish timestamp without time zone,
    locked boolean not null default false,
    start timestamp without time zone,
    tokens integer,
    worked boolean not null default false,
    sessionid integer NOT NULL,
    volunteerid integer NOT NULL,
    areaid integer
);


ALTER TABLE public.volunteer_session OWNER TO staffing;

--
-- Name: possible_session; Type: VIEW; Schema: public; Owner: nick
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


ALTER TABLE public.possible_session OWNER TO nick;

--
-- Name: sequence; Type: TABLE; Schema: public; Owner: staffing; Tablespace: 
--

CREATE TABLE public.sequence (
    seq_name character varying(50) NOT NULL,
    seq_count numeric(38,0)
);


ALTER TABLE public.sequence OWNER TO staffing;

--
-- Name: view_assignment_selector; Type: VIEW; Schema: public; Owner: nick
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


ALTER TABLE public.view_assignment_selector OWNER TO nick;

--
-- Name: view_volunteer_session; Type: VIEW; Schema: public; Owner: nick
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


ALTER TABLE public.view_volunteer_session OWNER TO nick;

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
SEQ_GEN	0
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
-- Name: area_session_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.area_session
    ADD CONSTRAINT area_session_pkey PRIMARY KEY (areaid, sessionid);


--
-- Name: assignable_area_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.assignable_area
    ADD CONSTRAINT assignable_area_pkey PRIMARY KEY (id);


--
-- Name: form_area_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.form_area
    ADD CONSTRAINT form_area_pkey PRIMARY KEY (id);


--
-- Name: sequence_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.sequence
    ADD CONSTRAINT sequence_pkey PRIMARY KEY (seq_name);


--
-- Name: session_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.session
    ADD CONSTRAINT session_pkey PRIMARY KEY (id);


--
-- Name: volunteer_area_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.volunteer_area
    ADD CONSTRAINT volunteer_area_pkey PRIMARY KEY (areaid, volunteerid);


--
-- Name: volunteer_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.volunteer
    ADD CONSTRAINT volunteer_pkey PRIMARY KEY (id);


--
-- Name: volunteer_session_pkey; Type: CONSTRAINT; Schema: public; Owner: staffing; Tablespace: 
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT volunteer_session_pkey PRIMARY KEY (sessionid, volunteerid);


--
-- Name: assigned_counts _RETURN; Type: RULE; Schema: public; Owner: nick
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
-- Name: fk_area_session_areaid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.area_session
    ADD CONSTRAINT fk_area_session_areaid FOREIGN KEY (areaid) REFERENCES public.assignable_area(id);


--
-- Name: fk_area_session_sessionid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.area_session
    ADD CONSTRAINT fk_area_session_sessionid FOREIGN KEY (sessionid) REFERENCES public.session(id);


--
-- Name: fk_assignable_area_formarea_id; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.assignable_area
    ADD CONSTRAINT fk_assignable_area_formarea_id FOREIGN KEY (formarea_id) REFERENCES public.form_area(id);


--
-- Name: fk_volunteer_area_areaid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_area
    ADD CONSTRAINT fk_volunteer_area_areaid FOREIGN KEY (areaid) REFERENCES public.assignable_area(id);


--
-- Name: fk_volunteer_area_volunteerid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_area
    ADD CONSTRAINT fk_volunteer_area_volunteerid FOREIGN KEY (volunteerid) REFERENCES public.volunteer(id);


--
-- Name: fk_volunteer_session_areaid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT fk_volunteer_session_areaid FOREIGN KEY (areaid, volunteerid) REFERENCES public.volunteer_area(areaid, volunteerid);


--
-- Name: fk_volunteer_session_sessionid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT fk_volunteer_session_sessionid FOREIGN KEY (sessionid) REFERENCES public.session(id);


--
-- Name: fk_volunteer_session_volunteerid; Type: FK CONSTRAINT; Schema: public; Owner: staffing
--

ALTER TABLE ONLY public.volunteer_session
    ADD CONSTRAINT fk_volunteer_session_volunteerid FOREIGN KEY (volunteerid) REFERENCES public.volunteer(id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

