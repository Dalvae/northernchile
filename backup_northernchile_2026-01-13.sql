--
-- PostgreSQL database dump
--

\restrict 3UHETgPccnZ82mbX7uTcik4x6Dqge6tdzgPYd2dEsM93MH14GINcKkKQJKljQnb

-- Dumped from database version 17.7 (bdc8956)
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.audit_logs (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    user_email character varying(255) NOT NULL,
    user_role character varying(255) NOT NULL,
    action character varying(100) NOT NULL,
    entity_type character varying(100) NOT NULL,
    entity_id uuid,
    entity_description text,
    old_values jsonb,
    new_values jsonb,
    ip_address character varying(50),
    user_agent text,
    created_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.audit_logs OWNER TO neondb_owner;

--
-- Name: bookings; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.bookings (
    id uuid NOT NULL,
    schedule_id uuid NOT NULL,
    user_id uuid NOT NULL,
    tour_date date NOT NULL,
    language_code character varying(5) NOT NULL,
    subtotal numeric(19,4) NOT NULL,
    tax_amount numeric(19,4) NOT NULL,
    total_amount numeric(19,4) NOT NULL,
    status character varying(30),
    special_requests text,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    deleted_at timestamp(6) with time zone,
    reminder_sent_at timestamp with time zone
);


ALTER TABLE public.bookings OWNER TO neondb_owner;

--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.cart_items (
    id uuid NOT NULL,
    cart_id uuid NOT NULL,
    schedule_id uuid NOT NULL,
    num_participants integer NOT NULL,
    created_at timestamp(6) with time zone
);


ALTER TABLE public.cart_items OWNER TO neondb_owner;

--
-- Name: carts; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.carts (
    id uuid NOT NULL,
    user_id uuid,
    status character varying(20),
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone
);


ALTER TABLE public.carts OWNER TO neondb_owner;

--
-- Name: contact_messages; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.contact_messages (
    id uuid NOT NULL,
    name character varying(100) NOT NULL,
    email character varying(255) NOT NULL,
    phone character varying(50),
    message text NOT NULL,
    status character varying(20) DEFAULT 'NEW'::character varying NOT NULL,
    created_at timestamp without time zone NOT NULL,
    replied_at timestamp without time zone,
    CONSTRAINT chk_contact_status CHECK (((status)::text = ANY ((ARRAY['NEW'::character varying, 'READ'::character varying, 'REPLIED'::character varying, 'ARCHIVED'::character varying])::text[])))
);


ALTER TABLE public.contact_messages OWNER TO neondb_owner;

--
-- Name: email_verification_tokens; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.email_verification_tokens (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    used boolean DEFAULT false NOT NULL,
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.email_verification_tokens OWNER TO neondb_owner;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO neondb_owner;

--
-- Name: media; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.media (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    owner_id uuid NOT NULL,
    tour_id uuid,
    schedule_id uuid,
    s3_key character varying(512) NOT NULL,
    url character varying(1024) NOT NULL,
    alt_translations jsonb DEFAULT '{"en": "", "es": "", "pt": ""}'::jsonb,
    caption_translations jsonb DEFAULT '{"en": "", "es": "", "pt": ""}'::jsonb,
    tags text[] DEFAULT '{}'::text[],
    size_bytes bigint NOT NULL,
    content_type character varying(100) NOT NULL,
    original_filename character varying(512),
    variants jsonb DEFAULT '{}'::jsonb,
    exif_data jsonb DEFAULT '{}'::jsonb,
    uploaded_at timestamp with time zone DEFAULT now() NOT NULL,
    taken_at timestamp with time zone,
    display_order integer DEFAULT 0,
    is_hero boolean DEFAULT false,
    is_featured boolean DEFAULT false,
    CONSTRAINT media_ref_check CHECK ((((tour_id IS NOT NULL) AND (schedule_id IS NULL)) OR (schedule_id IS NOT NULL) OR ((tour_id IS NULL) AND (schedule_id IS NULL))))
);


ALTER TABLE public.media OWNER TO neondb_owner;

--
-- Name: TABLE media; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON TABLE public.media IS 'Stores all media files (photos) for tours, schedules, and loose media';


--
-- Name: COLUMN media.tour_id; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.tour_id IS 'If set, this media belongs to a tour (but not assigned to gallery yet)';


--
-- Name: COLUMN media.schedule_id; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.schedule_id IS 'If set, this media belongs to a specific schedule instance';


--
-- Name: COLUMN media.variants; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.variants IS 'JSON with thumbnail, medium, and full URLs generated by background job';


--
-- Name: COLUMN media.taken_at; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.taken_at IS 'Date/time photo was taken (from EXIF or manual entry)';


--
-- Name: COLUMN media.display_order; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.display_order IS 'Order of media in tour/schedule gallery';


--
-- Name: COLUMN media.is_hero; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.is_hero IS 'Whether this is the hero image for the tour';


--
-- Name: COLUMN media.is_featured; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.media.is_featured IS 'Whether this media is featured in listings';


--
-- Name: participants; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.participants (
    id uuid NOT NULL,
    booking_id uuid NOT NULL,
    full_name character varying(255) NOT NULL,
    email character varying(255),
    phone_number character varying(20),
    document_id character varying(100) NOT NULL,
    nationality character varying(100),
    date_of_birth date,
    age integer,
    pickup_address character varying(500),
    special_requirements text,
    created_at timestamp(6) with time zone
);


ALTER TABLE public.participants OWNER TO neondb_owner;

--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.password_reset_tokens (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    token character varying(255) NOT NULL,
    used boolean DEFAULT false NOT NULL,
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone NOT NULL
);


ALTER TABLE public.password_reset_tokens OWNER TO neondb_owner;

--
-- Name: payment_sessions; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.payment_sessions (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    items jsonb NOT NULL,
    total_amount numeric(19,4) NOT NULL,
    currency character varying(3) DEFAULT 'CLP'::character varying NOT NULL,
    language_code character varying(5) DEFAULT 'es'::character varying NOT NULL,
    user_email character varying(255),
    provider character varying(20),
    payment_method character varying(20),
    external_payment_id character varying(255),
    token character varying(500),
    payment_url text,
    qr_code text,
    pix_code text,
    return_url text,
    cancel_url text,
    provider_response jsonb,
    error_message text,
    is_test boolean DEFAULT false NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);


ALTER TABLE public.payment_sessions OWNER TO neondb_owner;

--
-- Name: TABLE payment_sessions; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON TABLE public.payment_sessions IS 'Stores checkout data temporarily. Bookings are created only after successful payment confirmation.';


--
-- Name: payments; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.payments (
    id uuid NOT NULL,
    booking_id uuid,
    provider character varying(20) NOT NULL,
    payment_method character varying(20) NOT NULL,
    external_payment_id character varying(255),
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    amount numeric(19,4) NOT NULL,
    currency character varying(3) DEFAULT 'CLP'::character varying NOT NULL,
    payment_url text,
    details_url text,
    qr_code text,
    pix_code text,
    token character varying(500),
    expires_at timestamp(6) with time zone,
    provider_response jsonb,
    error_message text,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    is_test boolean DEFAULT false NOT NULL,
    idempotency_key character varying(100),
    payment_session_id uuid
);


ALTER TABLE public.payments OWNER TO neondb_owner;

--
-- Name: COLUMN payments.is_test; Type: COMMENT; Schema: public; Owner: neondb_owner
--

COMMENT ON COLUMN public.payments.is_test IS 'Flag indicating if this is a test payment (excluded from financial reports). Automatically set based on PAYMENT_TEST_MODE configuration.';


--
-- Name: private_tour_requests; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.private_tour_requests (
    id uuid NOT NULL,
    user_id uuid,
    customer_name character varying(255) NOT NULL,
    customer_email character varying(255) NOT NULL,
    customer_phone character varying(255),
    requested_tour_type character varying(255) NOT NULL,
    requested_start_datetime date,
    num_participants integer NOT NULL,
    special_requests text,
    status character varying(255),
    quoted_price numeric(38,2),
    payment_link_id character varying(255),
    created_at timestamp(6) with time zone
);


ALTER TABLE public.private_tour_requests OWNER TO neondb_owner;

--
-- Name: tour_schedules; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.tour_schedules (
    id uuid NOT NULL,
    tour_id uuid NOT NULL,
    start_datetime timestamp(6) with time zone NOT NULL,
    max_participants integer NOT NULL,
    status character varying(20) DEFAULT 'OPEN'::character varying,
    assigned_guide_id uuid,
    created_at timestamp(6) with time zone
);


ALTER TABLE public.tour_schedules OWNER TO neondb_owner;

--
-- Name: tours; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.tours (
    id uuid NOT NULL,
    owner_id uuid NOT NULL,
    name_translations jsonb NOT NULL,
    description_blocks_translations jsonb,
    itinerary_translations jsonb,
    equipment_translations jsonb,
    additional_info_translations jsonb,
    wind_sensitive boolean DEFAULT false NOT NULL,
    moon_sensitive boolean DEFAULT false NOT NULL,
    cloud_sensitive boolean DEFAULT false NOT NULL,
    content_key character varying(100),
    slug character varying(255) NOT NULL,
    guide_name character varying(255),
    category character varying(50) NOT NULL,
    price numeric(19,4) NOT NULL,
    default_max_participants integer NOT NULL,
    duration_hours integer NOT NULL,
    default_start_time time(6) without time zone,
    recurring boolean DEFAULT false,
    recurrence_rule character varying(100),
    status character varying(20) DEFAULT 'DRAFT'::character varying NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    deleted_at timestamp(6) with time zone
);


ALTER TABLE public.tours OWNER TO neondb_owner;

--
-- Name: users; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255),
    full_name character varying(255) NOT NULL,
    nationality character varying(255),
    phone_number character varying(20),
    date_of_birth date,
    role character varying(50) NOT NULL,
    auth_provider character varying(50) DEFAULT 'LOCAL'::character varying,
    provider_id character varying(255),
    email_verified boolean DEFAULT false NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    deleted_at timestamp(6) with time zone
);


ALTER TABLE public.users OWNER TO neondb_owner;

--
-- Name: weather_alerts; Type: TABLE; Schema: public; Owner: neondb_owner
--

CREATE TABLE public.weather_alerts (
    id uuid NOT NULL,
    tour_schedule_id uuid NOT NULL,
    alert_type character varying(255) NOT NULL,
    severity character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    message text,
    wind_speed double precision,
    cloud_coverage integer,
    moon_phase double precision,
    resolution character varying(255),
    resolved_by character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    resolved_at timestamp(6) with time zone
);


ALTER TABLE public.weather_alerts OWNER TO neondb_owner;

--
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.audit_logs (id, user_id, user_email, user_role, action, entity_type, entity_id, entity_description, old_values, new_values, ip_address, user_agent, created_at) FROM stdin;
c6aabb55-7f09-4b3d-af47-5f2df4c0bf15	855ef29d-4275-4015-9fab-8d78bdea5fe2	jeanette@northernchile.com	ROLE_SUPER_ADMIN	CREATE	USER	855ef29d-4275-4015-9fab-8d78bdea5fe2	jeanette@northernchile.com	\N	{"id": "855ef29d-4275-4015-9fab-8d78bdea5fe2", "role": "ROLE_SUPER_ADMIN", "email": "jeanette@northernchile.com", "fullName": "Jeanette De izquierdo"}	148.222.205.123	Mozilla/5.0 (X11; Linux x86_64; rv:145.0) Gecko/20100101 Firefox/145.0	2025-11-26 20:15:28.498752+00
cc25c0ea-e147-43b9-a066-843ae22cbf9a	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	d013b442-c9c2-48e8-a3ba-5f459c1b832b	Tour Lagunas Escondidas de Baltinache & Vallecito - 2025-11-29T22:02:00Z	\N	{"id": "d013b442-c9c2-48e8-a3ba-5f459c1b832b", "status": "OPEN", "tourId": "26e2ac01-37e9-4483-9ce7-506e6782538b", "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "startDatetime": "2025-11-29T22:02:00Z"}	138.84.34.179	Mozilla/5.0 (X11; Linux x86_64; rv:145.0) Gecko/20100101 Firefox/145.0	2025-11-28 23:10:43.543835+00
bc328732-d180-4fdd-918e-e815e98b6f87	6766affb-d0eb-4a54-83e3-4195ef7824b3	simagico@gmail.com	ROLE_CLIENT	UPDATE	BOOKING	dea8f468-f7eb-4187-b2ea-da9d44f0e1dc	Tour Lagunas Escondidas de Baltinache & Vallecito - Diego Alvarez (Updated details)	\N	\N	138.84.34.179	Mozilla/5.0 (X11; Linux x86_64; rv:145.0) Gecko/20100101 Firefox/145.0	2025-11-29 02:35:17.973015+00
c3d311ef-20fd-41ec-9441-7f32502804a8	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	e1110060-a7a9-4d79-a7c8-1b9a81c7c836	Tour Valle del Arcoíris & Hierbas Buenas - 2025-11-30T08:00:00Z	\N	{"id": "e1110060-a7a9-4d79-a7c8-1b9a81c7c836", "status": "OPEN", "tourId": "5a23925b-e532-4101-bf02-b5281ecb7eb9", "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "startDatetime": "2025-11-30T08:00:00Z"}	54.91.39.234	node	2025-11-29 04:23:03.246621+00
178b4bde-adb4-45f8-8a89-b7efb6a46c9c	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	126ad837-23d0-4638-a726-d60da1bd942a	Tour Astronómico: Arqueoastronomía y Cosmovisión Andina - 2025-12-01T21:00:00Z	\N	{"id": "126ad837-23d0-4638-a726-d60da1bd942a", "status": "OPEN", "tourId": "d43e8d8d-d09c-4564-ba84-e89364d7e028", "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "startDatetime": "2025-12-01T21:00:00Z"}	18.232.101.55	node	2025-11-30 00:15:21.706586+00
4f986aff-45d5-47b0-a2a3-85f741cd4fc2	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	UPDATE	TOUR	26e2ac01-37e9-4483-9ce7-506e6782538b	Tour Lagunas Escondidas de Baltinache & Vallecito	{"name": "Tour Lagunas Escondidas de Baltinache & Vallecito", "price": "45000.0000", "status": "PUBLISHED", "category": "REGULAR"}	{"name": "Tour Lagunas Escondidas de Baltinache & Vallecito", "price": "45000", "status": "PUBLISHED", "category": "REGULAR"}	44.212.94.100	node	2025-12-03 21:04:23.670664+00
52f08b88-effe-4e05-8165-8774e79a2bb0	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	176b854d-5b23-48bd-bec7-1378029a4a33	Tour Astronómico: Arqueoastronomía y Cosmovisión Andina - 2025-12-31T21:00:00Z	\N	{"id": "176b854d-5b23-48bd-bec7-1378029a4a33", "status": "OPEN", "tourId": "d43e8d8d-d09c-4564-ba84-e89364d7e028", "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "startDatetime": "2025-12-31T21:00:00Z"}	54.242.72.26	node	2025-12-04 16:07:40.204548+00
d618b790-d4b7-4a95-b101-74071951bc2b	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	a1d26c37-77fc-4944-8667-e8d032bf67da	Tour Valle del Arcoíris & Hierbas Buenas - 2025-12-30T08:00:00Z	\N	{"id": "a1d26c37-77fc-4944-8667-e8d032bf67da", "status": "OPEN", "tourId": "5a23925b-e532-4101-bf02-b5281ecb7eb9", "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "startDatetime": "2025-12-30T08:00:00Z"}	3.236.9.111	node	2025-12-04 16:07:50.665612+00
26e5b65a-1053-4889-b18f-621664acbaea	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	03c15999-e03d-4075-a8a9-17f52711085e	Tour Valle del Arcoíris & Hierbas Buenas - 2025-12-29T08:00:00Z	\N	{"id": "03c15999-e03d-4075-a8a9-17f52711085e", "status": "OPEN", "tourId": "5a23925b-e532-4101-bf02-b5281ecb7eb9", "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "startDatetime": "2025-12-29T08:00:00Z"}	3.236.9.111	node	2025-12-04 16:07:58.871674+00
87927402-c52c-4f3d-a196-2f0e03238684	855ef29d-4275-4015-9fab-8d78bdea5fe2	jeanette@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	425a30f1-4857-4261-be28-f6b61aca690d	Tour Valle del Arcoíris & Hierbas Buenas - 2026-01-01T20:00:00Z	\N	{"id": "425a30f1-4857-4261-be28-f6b61aca690d", "status": "OPEN", "tourId": "5a23925b-e532-4101-bf02-b5281ecb7eb9", "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "startDatetime": "2026-01-01T20:00:00Z"}	54.174.16.202	node	2025-12-09 14:43:06.789867+00
77297158-c584-45d9-8473-654f017917b6	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	4cfb344b-b463-4d6c-b294-1daec9152775	Tour Valle del Arcoíris & Hierbas Buenas - 2026-01-08T08:00:00Z	\N	{"id": "4cfb344b-b463-4d6c-b294-1daec9152775", "status": "OPEN", "tourId": "5a23925b-e532-4101-bf02-b5281ecb7eb9", "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "startDatetime": "2026-01-08T08:00:00Z"}	3.208.71.139	node	2026-01-06 16:54:37.509323+00
a6e4e8bb-9c62-4d66-a1f4-e2aebe9fad05	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	223e67d9-44f5-413a-9b57-78bca0b71982	Tour Lagunas Escondidas de Baltinache & Vallecito - 2026-01-09T21:00:00Z	\N	{"id": "223e67d9-44f5-413a-9b57-78bca0b71982", "status": "OPEN", "tourId": "26e2ac01-37e9-4483-9ce7-506e6782538b", "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "startDatetime": "2026-01-09T21:00:00Z"}	3.208.71.139	node	2026-01-06 16:54:57.745824+00
05f356ff-a767-4709-847e-e189b2535b7a	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	74f67af0-9ef2-4416-92e0-49da7dde83e3	Tour Valle del Arcoíris & Hierbas Buenas - 2026-01-10T08:00:00Z	\N	{"id": "74f67af0-9ef2-4416-92e0-49da7dde83e3", "status": "OPEN", "tourId": "5a23925b-e532-4101-bf02-b5281ecb7eb9", "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "startDatetime": "2026-01-10T08:00:00Z"}	3.208.71.139	node	2026-01-06 16:55:06.320078+00
2a82ee03-0446-467a-b06f-1e0f28406ac5	f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	ROLE_SUPER_ADMIN	CREATE	SCHEDULE	a42456d5-7ca1-4679-993b-79e76508e133	Tour Astronómico: Arqueoastronomía y Cosmovisión Andina - 2026-01-11T21:00:00Z	\N	{"id": "a42456d5-7ca1-4679-993b-79e76508e133", "status": "OPEN", "tourId": "d43e8d8d-d09c-4564-ba84-e89364d7e028", "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "startDatetime": "2026-01-11T21:00:00Z"}	3.208.71.139	node	2026-01-06 16:55:13.185346+00
\.


--
-- Data for Name: bookings; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.bookings (id, schedule_id, user_id, tour_date, language_code, subtotal, tax_amount, total_amount, status, special_requests, created_at, updated_at, deleted_at, reminder_sent_at) FROM stdin;
dea8f468-f7eb-4187-b2ea-da9d44f0e1dc	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED		2025-11-29 02:08:00.882532+00	2025-11-29 02:41:43.760514+00	\N	\N
c7f1bad1-389d-4cba-84e0-403352066e74	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED	\N	2025-11-29 03:13:59.736161+00	2025-11-29 03:45:21.553942+00	\N	\N
241f1e5c-587d-426c-a069-e9cb0736208b	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED	\N	2025-11-29 03:14:11.271638+00	2025-11-29 03:45:21.636112+00	\N	\N
ca6b3f12-8dd1-4087-91b4-174c58ba763a	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED	\N	2025-11-29 03:20:11.113672+00	2025-11-29 03:52:44.867499+00	\N	\N
4951d815-7c42-4aa8-8a7b-779b445c3e8e	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED	\N	2025-11-29 04:35:50.676643+00	2025-11-29 05:06:57.603965+00	\N	\N
528d207a-b3a7-4e30-8097-68db77fb6db3	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED	\N	2025-11-29 04:36:26.236676+00	2025-11-29 05:06:57.67952+00	\N	\N
889c6656-3fa4-476d-a966-91873f78c48e	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	37815.0000	7185.0000	45000.0000	CANCELLED	\N	2025-11-29 05:04:45.044197+00	2025-11-29 05:36:57.593555+00	\N	\N
a52c7fe0-9f0e-40aa-920e-234f7362431a	d013b442-c9c2-48e8-a3ba-5f459c1b832b	6766affb-d0eb-4a54-83e3-4195ef7824b3	2025-11-29	es	45000.0000	0.0000	45000.0000	CONFIRMED	\N	2025-11-29 15:34:44.637249+00	2025-11-29 18:00:00.713781+00	\N	2025-11-29 18:00:00.329768+00
08c0bc40-cfd0-4550-a85e-d1e833460a9f	126ad837-23d0-4638-a726-d60da1bd942a	a762c59a-8a68-42de-8c69-9b14b2871696	2025-12-01	es	55000.0000	0.0000	55000.0000	CONFIRMED	\N	2025-11-30 01:42:59.024059+00	2025-11-30 20:00:00.740336+00	\N	2025-11-30 20:00:00.561342+00
d5b7b5c0-a745-4f3f-b572-fe4960debd48	03c15999-e03d-4075-a8a9-17f52711085e	032d759e-62e4-4f77-9a4c-958da92a5aa4	2025-12-29	es	40000.0000	0.0000	40000.0000	CONFIRMED	\N	2025-12-09 14:59:13.27329+00	2025-12-09 14:59:13.27331+00	\N	\N
19940620-d94c-4447-8ef5-8b77cc93e20d	74f67af0-9ef2-4416-92e0-49da7dde83e3	9a96969f-c381-4c20-a8ac-91591d905e05	2026-01-10	es	40000.0000	0.0000	40000.0000	CANCELLED	\N	2026-01-07 00:12:17.614228+00	2026-01-07 00:19:23.159167+00	\N	\N
67cde883-800c-4f53-822c-9a692ff39ae9	223e67d9-44f5-413a-9b57-78bca0b71982	ff9d43b9-a699-4e24-833d-3e90b9fcf7ea	2026-01-09	es	45000.0000	0.0000	45000.0000	CONFIRMED	\N	2026-01-06 20:26:43.678258+00	2026-01-08 20:00:00.367843+00	\N	2026-01-08 20:00:00.349771+00
2211d641-db62-4e5a-96e7-f18802914646	74f67af0-9ef2-4416-92e0-49da7dde83e3	9a96969f-c381-4c20-a8ac-91591d905e05	2026-01-10	es	40000.0000	0.0000	40000.0000	CONFIRMED	\N	2026-01-06 22:52:09.096463+00	2026-01-09 07:00:00.397476+00	\N	2026-01-09 07:00:00.360098+00
56146a1c-a6f9-4c8b-ae0e-666a01604b65	a42456d5-7ca1-4679-993b-79e76508e133	9a96969f-c381-4c20-a8ac-91591d905e05	2026-01-11	es	55000.0000	0.0000	55000.0000	CONFIRMED	\N	2026-01-06 23:24:38.710742+00	2026-01-10 20:00:00.537775+00	\N	2026-01-10 20:00:00.492835+00
5ddc1b83-f649-47ab-b22a-db04984cf8b5	a42456d5-7ca1-4679-993b-79e76508e133	9a96969f-c381-4c20-a8ac-91591d905e05	2026-01-11	es	55000.0000	0.0000	55000.0000	CONFIRMED	\N	2026-01-06 23:10:58.873145+00	2026-01-10 20:00:00.60801+00	\N	2026-01-10 20:00:00.49519+00
79eaff84-b6a0-46cf-825a-49c2133c5264	a42456d5-7ca1-4679-993b-79e76508e133	9a96969f-c381-4c20-a8ac-91591d905e05	2026-01-11	es	55000.0000	0.0000	55000.0000	CONFIRMED	\N	2026-01-06 23:30:51.45533+00	2026-01-10 20:00:00.67916+00	\N	2026-01-10 20:00:00.50077+00
d52aabc2-41cc-4282-9a09-db7f1cbaa8b4	a42456d5-7ca1-4679-993b-79e76508e133	9a96969f-c381-4c20-a8ac-91591d905e05	2026-01-11	es	55000.0000	0.0000	55000.0000	CONFIRMED	\N	2026-01-06 23:32:44.918426+00	2026-01-10 20:00:00.748909+00	\N	2026-01-10 20:00:00.508086+00
\.


--
-- Data for Name: cart_items; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.cart_items (id, cart_id, schedule_id, num_participants, created_at) FROM stdin;
\.


--
-- Data for Name: carts; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.carts (id, user_id, status, created_at, expires_at) FROM stdin;
\.


--
-- Data for Name: contact_messages; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.contact_messages (id, name, email, phone, message, status, created_at, replied_at) FROM stdin;
\.


--
-- Data for Name: email_verification_tokens; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.email_verification_tokens (id, user_id, token, used, created_at, expires_at) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	initial schema	SQL	V1__initial_schema.sql	-1124660460	neondb_owner	2025-11-16 22:38:02.378359	3563	t
2	2	add media management	SQL	V2__add_media_management.sql	1965686276	neondb_owner	2025-11-17 14:26:12.262872	1660	t
3	3	add is featured to tour media	SQL	V3__add_is_featured_to_tour_media.sql	429414834	neondb_owner	2025-11-21 17:58:43.479167	588	t
4	4	add contact messages and indexes	SQL	V4__add_contact_messages_and_indexes.sql	1225706016	neondb_owner	2025-11-26 12:03:04.251822	1725	t
5	5	add is test to payments	SQL	V5__add_is_test_to_payments.sql	499559398	neondb_owner	2025-11-26 18:41:00.1727	514	t
6	6	add idempotency key to payments	SQL	V6__add_idempotency_key_to_payments.sql	411282047	neondb_owner	2025-11-27 18:34:11.450876	453	t
7	7	change private tour requested datetime to date	SQL	V7__change_private_tour_requested_datetime_to_date.sql	-1940943528	neondb_owner	2025-11-29 02:42:26.147623	393	t
8	8	add payment sessions	SQL	V8__add_payment_sessions.sql	1574772182	neondb_owner	2025-11-29 05:52:14.774313	1091	t
9	9	add reminder sent at to bookings	SQL	V9__add_reminder_sent_at_to_bookings.sql	-277644775	neondb_owner	2025-11-29 17:22:11.896373	476	t
10	10	add performance indexes	SQL	V10__add_performance_indexes.sql	-189109980	neondb_owner	2026-01-01 22:04:47.886005	989	t
11	11	simplify media architecture	SQL	V11__simplify_media_architecture.sql	1644982171	neondb_owner	2026-01-05 17:10:56.738581	1438	t
\.


--
-- Data for Name: media; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.media (id, owner_id, tour_id, schedule_id, s3_key, url, alt_translations, caption_translations, tags, size_bytes, content_type, original_filename, variants, exif_data, uploaded_at, taken_at, display_order, is_hero, is_featured) FROM stdin;
96afea45-cbb7-42f5-a3ac-fab811e73591	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/734bb0c2-e4e9-4243-a1ae-5afb4c53438b.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/734bb0c2-e4e9-4243-a1ae-5afb4c53438b.webp	\N	\N	{noche,astrotour}	447454	image/webp	20250531231834_IMG_4155.webp	\N	\N	2025-12-03 00:06:41.292116+00	\N	0	f	f
08f93989-aa4e-4b73-9656-6f7bc5d07d51	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/e3efd9ef-d100-4ac1-aebb-0834b9487502.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/e3efd9ef-d100-4ac1-aebb-0834b9487502.webp	\N	\N	{noche,astrotour}	379490	image/webp	20250531232055_IMG_4156.webp	\N	\N	2025-12-03 00:06:45.849226+00	\N	0	f	f
6e720f48-35e3-4499-af0c-c662ca283313	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/b7852adb-9148-4bc9-9e45-d61c827ee562.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/b7852adb-9148-4bc9-9e45-d61c827ee562.webp	\N	\N	{noche,astrotour}	159440	image/webp	20250407002835_IMG_2961 (1).webp	\N	\N	2025-12-03 00:05:52.192963+00	\N	6	f	f
81e10722-4c72-466e-9a06-cfdfaee0e570	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/af28bb66-a391-47b5-a784-72f3f609c383.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/af28bb66-a391-47b5-a784-72f3f609c383.webp	\N	\N	{noche,astrotour}	221864	image/webp	20250407003049_IMG_2962.webp	\N	\N	2025-12-03 00:05:55.326166+00	\N	7	f	f
5662b6ec-6961-4416-9493-cc3f4fc248fe	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/e54a2d14-2709-4157-8b39-a119d8f4b22a.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/e54a2d14-2709-4157-8b39-a119d8f4b22a.webp	\N	\N	{noche,astrotour}	351744	image/webp	20250519223015_IMG_3798.webp	\N	\N	2025-12-03 00:05:58.494971+00	\N	8	f	f
a62da8aa-ccf1-4548-95b0-0a3dbabea6d2	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/d00036d5-ef8a-4108-97f2-edc794664aba.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/d00036d5-ef8a-4108-97f2-edc794664aba.webp	\N	\N	{noche,astrotour}	503754	image/webp	20250526221249_IMG_3966.webp	\N	\N	2025-12-03 00:06:02.561655+00	\N	9	f	f
78be9e06-02cd-4096-b4e3-cfdfb7bf67b8	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/38801f94-37bd-41ee-821b-7eb3fda29dd4.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/38801f94-37bd-41ee-821b-7eb3fda29dd4.webp	\N	\N	{noche,astrotour}	514068	image/webp	20250526222100_IMG_3974.webp	\N	\N	2025-12-03 00:06:06.326939+00	\N	10	f	f
5b03b46d-fad7-4955-83ca-f083bf360ad0	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/798c4c18-899a-4d94-9875-7433985015f6.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/798c4c18-899a-4d94-9875-7433985015f6.webp	\N	\N	{noche,astrotour}	405940	image/webp	20250531225606_IMG_4139.webp	\N	\N	2025-12-03 00:06:13.244642+00	\N	11	f	f
7669edb6-a97d-4de2-b3b6-1705edcd4a23	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/e989b40b-6d1a-4d9a-b355-db484244e247.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/e989b40b-6d1a-4d9a-b355-db484244e247.webp	\N	\N	{noche,astrotour}	315498	image/webp	20250531225910_IMG_4141.webp	\N	\N	2025-12-03 00:06:16.301339+00	\N	12	f	f
df0e65f1-19a8-4eba-b488-dea444002aac	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/da93b1e5-c5fb-4ebf-b007-4090d3db884e.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/da93b1e5-c5fb-4ebf-b007-4090d3db884e.webp	\N	\N	{noche,astrotour}	423390	image/webp	20250531230009_IMG_4142.webp	\N	\N	2025-12-03 00:06:19.312213+00	\N	13	f	f
86ed6d7a-84c3-4022-8298-114c3623d8fa	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/b3d69dbe-6203-4a23-858f-eee8f6d42ebc.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/b3d69dbe-6203-4a23-858f-eee8f6d42ebc.webp	\N	\N	{noche,astrotour}	474536	image/webp	20250531230102_IMG_4143.webp	\N	\N	2025-12-03 00:06:22.682466+00	\N	14	f	f
88ab16ca-b70c-4050-9dea-37e3354e4d0f	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/380f51c3-7960-42c1-9430-b80c56312c5a.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/380f51c3-7960-42c1-9430-b80c56312c5a.webp	\N	\N	{noche,astrotour}	351292	image/webp	20250531230656_IMG_4148.webp	\N	\N	2025-12-03 00:06:25.752022+00	\N	15	f	f
c570916b-e2c1-4898-a408-5257ba6ce83b	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/61d4013e-900c-4aa7-abb9-0eaae8706ba9.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/61d4013e-900c-4aa7-abb9-0eaae8706ba9.webp	\N	\N	{noche,astrotour}	330376	image/webp	20250531230812_IMG_4149.webp	\N	\N	2025-12-03 00:06:28.761586+00	\N	16	f	t
7fcee68f-3376-406a-955d-1a88d9463f13	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/f4537c10-116f-4bad-a696-864edc672f8f.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/f4537c10-116f-4bad-a696-864edc672f8f.webp	\N	\N	{noche,astrotour}	378614	image/webp	20250531231049_IMG_4150.webp	\N	\N	2025-12-03 00:06:31.714417+00	\N	17	t	f
36208dde-8034-4535-a136-23e50216e87a	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/02d8d328-208c-4874-9fdf-f7fcb8722d46.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/02d8d328-208c-4874-9fdf-f7fcb8722d46.webp	\N	\N	{noche,astrotour}	378840	image/webp	20250531231049_IMG_4150 (1).webp	\N	\N	2025-12-03 00:06:34.683498+00	\N	18	f	f
c23bb89a-7434-4073-80b1-3811d27d9d48	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/08dd7a03-8d00-4954-9ce9-4e372afc2608.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/08dd7a03-8d00-4954-9ce9-4e372afc2608.webp	\N	\N	{noche,astrotour}	424258	image/webp	20250531231437_IMG_4152.webp	\N	\N	2025-12-03 00:06:37.827447+00	\N	19	f	f
6407cb56-0910-4812-9872-6a86888e4159	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/d237f390-6409-434a-a1bc-df3dbb3d4a3e.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/d237f390-6409-434a-a1bc-df3dbb3d4a3e.webp	\N	\N	\N	154296	image/webp	20250701223600_IMG_4687 (1).webp	\N	\N	2025-12-03 00:06:52.771129+00	\N	0	f	f
d58e81f6-ac66-4c21-9107-ed04271bf5b9	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/cc8e2b37-1da3-41e7-a351-1bf68c0ff1f7.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/cc8e2b37-1da3-41e7-a351-1bf68c0ff1f7.webp	\N	\N	\N	169066	image/webp	20250701223715_IMG_4688 (1).webp	\N	\N	2025-12-03 00:06:55.307688+00	\N	0	f	f
ae9c311a-ce74-493d-b97a-5697225be0e4	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/bcb6ff99-933c-4b27-acc7-7192a8e49f0a.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/bcb6ff99-933c-4b27-acc7-7192a8e49f0a.webp	\N	\N	\N	167476	image/webp	20250701224050_IMG_4690 (2).webp	\N	\N	2025-12-03 00:06:57.765972+00	\N	0	f	f
6b6bf88f-4c92-4976-bb91-c16456ba7271	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/fea1b2c1-09d9-4387-a70c-9e5bc3921503.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/fea1b2c1-09d9-4387-a70c-9e5bc3921503.webp	\N	\N	\N	165634	image/webp	20250701224147_IMG_4691 (1).webp	\N	\N	2025-12-03 00:07:00.365829+00	\N	0	f	f
0d4a5779-05b2-4e5f-bb7c-10168b4fe595	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/9be46d97-ab81-4dd6-80cd-73abb9f4f9ea.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/9be46d97-ab81-4dd6-80cd-73abb9f4f9ea.webp	\N	\N	\N	167646	image/webp	20250701224316_IMG_4692 (2).webp	\N	\N	2025-12-03 00:07:02.958627+00	\N	0	f	f
3b59bed1-f49e-4da4-9ecc-df3e2c4ce4c1	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/db0013ec-b4be-4367-8dd0-5008943604a1.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/db0013ec-b4be-4367-8dd0-5008943604a1.webp	\N	\N	\N	162638	image/webp	20250701224705_IMG_4696 (1).webp	\N	\N	2025-12-03 00:07:05.470239+00	\N	0	f	f
5c9a382e-7ce1-4509-bd59-2db16c928af9	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/1ae6497c-9541-4a18-8cfd-0068ea0a7197.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/1ae6497c-9541-4a18-8cfd-0068ea0a7197.webp	\N	\N	\N	188550	image/webp	flux.webp	\N	\N	2025-12-03 00:07:08.065695+00	\N	0	f	f
89bd8c20-722a-4c9b-a1fa-9fbace104eb4	855ef29d-4275-4015-9fab-8d78bdea5fe2	\N	\N	media/f9233e53-d358-4c90-b709-74f23694009d.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/f9233e53-d358-4c90-b709-74f23694009d.webp	\N	\N	\N	101692	image/webp	Gemini1.webp	\N	\N	2025-12-03 00:07:10.44668+00	\N	0	f	f
6ee69c63-5680-4029-bd2b-924320684466	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/87cbf6a1-5daa-4ba3-9f58-23562be8306a.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/87cbf6a1-5daa-4ba3-9f58-23562be8306a.webp	\N	\N	\N	73676	image/webp	Valle-del-Arcoiris-4.webp	\N	\N	2025-12-03 20:52:37.153048+00	\N	4	t	f
02a33f43-4282-4e49-a0e4-19f331b0b63a	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/6a20bb52-779b-4316-a89e-596c9b1e2a93.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/6a20bb52-779b-4316-a89e-596c9b1e2a93.webp	\N	\N	\N	230542	image/webp	Valle-del-Arcoiris-3.webp	\N	\N	2025-12-03 20:52:40.702916+00	\N	5	f	t
2ae8d1e2-65b6-44c3-bc0b-9b99ed6d8085	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/a13b7403-3582-43a5-9490-7b652368dd82.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/a13b7403-3582-43a5-9490-7b652368dd82.webp	\N	\N	\N	234430	image/webp	Valle-del-Arcoiris-2.webp	\N	\N	2025-12-03 20:52:44.167496+00	\N	6	f	t
16bb9fc8-fc0e-4b64-95b6-20ad54d20976	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/601d1437-e2d0-4f11-bddf-86a09140a77a.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/601d1437-e2d0-4f11-bddf-86a09140a77a.webp	\N	\N	\N	258904	image/webp	petro1.webp	\N	\N	2025-12-03 20:52:34.540386+00	\N	3	f	t
93583adc-9020-464f-aa1f-77eb0b5905fc	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/4393bf66-0a96-4b0b-a272-380906458b07.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/4393bf66-0a96-4b0b-a272-380906458b07.webp	\N	\N	\N	140170	image/webp	petro2.webp	\N	\N	2025-12-03 20:52:31.057526+00	\N	2	f	t
52849dd8-361b-4fc4-ba2d-ac563a6ed5c7	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/b45453b0-c38e-4cf8-b1c9-416e79a7f010.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/b45453b0-c38e-4cf8-b1c9-416e79a7f010.webp	\N	\N	\N	197404	image/webp	petro3.webp	\N	\N	2025-12-03 20:52:28.209406+00	\N	1	f	t
56e7ca94-6ec9-4fea-8cfe-71d49cbe39ed	855ef29d-4275-4015-9fab-8d78bdea5fe2	5a23925b-e532-4101-bf02-b5281ecb7eb9	\N	media/3c94747f-43b6-4cb0-96a3-5ae10df41497.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/3c94747f-43b6-4cb0-96a3-5ae10df41497.webp	\N	\N	\N	270228	image/webp	petro4.webp	\N	\N	2025-12-03 20:52:24.811268+00	\N	0	f	t
ab379646-37cd-4d08-967e-4752f7565bee	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/fac678c9-7461-4559-bc59-daf110182709.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/fac678c9-7461-4559-bc59-daf110182709.webp	{"es": "Hero"}	\N	{Hero,Noche,Astrotour}	130886	image/webp	upscalemedia-transformed.webp	\N	\N	2025-12-02 23:54:50.809625+00	\N	0	f	f
1b6d42cc-a9a1-47b3-8d74-c7d477c8be72	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/f2a91a39-dafd-450b-8780-67586360fd11.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/f2a91a39-dafd-450b-8780-67586360fd11.webp	\N	\N	\N	434900	image/webp	20250531225247_IMG_4138.webp	\N	\N	2025-12-03 00:06:10.093161+00	\N	1	f	f
c90ef1b4-1ffe-4e80-973f-db22dfacfbc1	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/4d862510-f11f-4ca9-91a7-25e7f3067981.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/4d862510-f11f-4ca9-91a7-25e7f3067981.webp	\N	\N	\N	99352	image/webp	20250630202957_IMG_4630 (1).webp	\N	\N	2025-12-03 00:06:48.050701+00	\N	2	f	f
53afc481-c454-439b-b136-c1c684fbdd51	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/cb5eb74b-6bc2-4b8e-8672-f000aab2c45a.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/cb5eb74b-6bc2-4b8e-8672-f000aab2c45a.webp	\N	\N	{noche,astrotour}	237296	image/webp	20250331230104_IMG_2755.webp	\N	\N	2025-12-03 00:05:46.260157+00	\N	3	f	f
5c0a11e6-205d-41d6-8b20-c593477e2a39	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/2da912c1-7613-4678-9689-c1f03689b91d.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/2da912c1-7613-4678-9689-c1f03689b91d.webp	\N	\N	\N	57540	image/webp	20250630202957_IMG_4630 (2).webp	\N	\N	2025-12-03 00:06:50.260164+00	\N	4	f	f
8168b800-65be-4b54-bc7f-2357fd1caf3c	855ef29d-4275-4015-9fab-8d78bdea5fe2	d43e8d8d-d09c-4564-ba84-e89364d7e028	\N	media/905bc420-6a93-49aa-a805-abdb7bcbcf12.webp	https://northern-chile-assets.s3.sa-east-1.amazonaws.com/media/905bc420-6a93-49aa-a805-abdb7bcbcf12.webp	\N	\N	{noche,astrotour}	244290	image/webp	20250407002802_IMG_2960.webp	\N	\N	2025-12-03 00:05:49.545568+00	\N	5	f	f
\.


--
-- Data for Name: participants; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.participants (id, booking_id, full_name, email, phone_number, document_id, nationality, date_of_birth, age, pickup_address, special_requirements, created_at) FROM stdin;
1c55e864-e5cc-469d-8afc-fc0efe545978	dea8f468-f7eb-4187-b2ea-da9d44f0e1dc	Diego Alvarez	simagico@gmail.com	+56942877633	17776934-7	CL	1990-12-11	34	aca al lado	nada	2025-11-29 02:08:00.972089+00
1e2ec63b-9490-486a-88bc-27afb99e351e	c7f1bad1-389d-4cba-84e0-403352066e74	Diego Alvarez	simagico@gmail.com	+56942877633	17776934-7	CL	1990-12-11	34	cosa	test	2025-11-29 03:13:59.811079+00
b11579fc-94c1-47b3-9429-b01ea2501bfb	241f1e5c-587d-426c-a069-e9cb0736208b	Diego Alvarez	simagico@gmail.com	+56942877633	17776934-7	CL	1990-12-11	34	cosa	test	2025-11-29 03:14:11.342639+00
5870e393-7e5e-4c08-8c0a-540a9e14eb16	ca6b3f12-8dd1-4087-91b4-174c58ba763a	Diego Alvarez	simagico@gmail.com	+56942877633	17776934-7	CL	1990-12-11	34	cosa	test	2025-11-29 03:20:11.186461+00
1f06b110-1ed4-44ff-85a0-38a4902bcbea	4951d815-7c42-4aa8-8a7b-779b445c3e8e	Diego Alvarez	simagico@gmail.com	+56942877633	177769347	CL	1990-12-11	34	al lado	test	2025-11-29 04:35:50.759901+00
3be75298-b41b-4f4a-a2a0-5f13d27f65ef	528d207a-b3a7-4e30-8097-68db77fb6db3	Diego Alvarez	simagico@gmail.com	+56942877633	177769347	CL	1990-12-11	34	al lado	test	2025-11-29 04:36:26.311306+00
76b4656b-9dfd-433a-9576-cb27dbdbd7ad	889c6656-3fa4-476d-a966-91873f78c48e	Diego Alvarez	simagico@gmail.com	+56942877633	177769347	CL	1990-12-11	34	al lado	test	2025-11-29 05:04:45.120413+00
532d4025-1456-44c1-a0b3-9ab7d5add087	a52c7fe0-9f0e-40aa-920e-234f7362431a	Diego Alvarez	simagico@gmail.com	+56942877633	17776934-7	CL	1990-12-11	34	aca al lado	\N	2025-11-29 15:34:44.721299+00
23388961-583a-4acb-be81-464ea1739b65	08c0bc40-cfd0-4550-a85e-d1e833460a9f	Carolina Barraza	cbarrazamorgado@gmail.com	+5691389559	27277212-2	CL	\N	\N	Al lado 	\N	2025-11-30 01:42:59.104462+00
0ea21c75-dbb7-4543-a1e7-1033ba77696f	d5b7b5c0-a745-4f3f-b572-fe4960debd48	cosito	vodisdsdfsdf@northernchile.com	+56912877633	17776934-7	CL	\N	\N	al lado	\N	2025-12-09 14:59:13.347625+00
a09ac888-dc75-4871-93e4-e4c285ced081	67cde883-800c-4f53-822c-9a692ff39ae9	usuariotest	davenac878@cameltok.com	+56942877633	8128128181	CL	1990-11-14	35	919219292121	\N	2026-01-06 20:26:43.861338+00
59786924-06d5-411f-9ec0-b923ccc241d6	2211d641-db62-4e5a-96e7-f18802914646	testuserdos	tedaca6099@emaxasp.com	+565692877633	18918281212	CL	1990-03-12	35	123123123123	123123123	2026-01-06 22:52:09.175264+00
f8023396-1a47-4c29-8219-3c0514c1ac6b	5ddc1b83-f649-47ab-b22a-db04984cf8b5	testuserdos	tedaca6099@emaxasp.com	+565692877633	18918281212	CL	1990-03-12	35	123123123123	123123123	2026-01-06 23:10:58.943339+00
7794be45-f35d-4b30-b3ba-3ac017796e85	56146a1c-a6f9-4c8b-ae0e-666a01604b65	testuserdos	tedaca6099@emaxasp.com	+569230121212	211231231	CL	1990-03-13	35	12313123	\N	2026-01-06 23:24:38.781572+00
27001872-702c-4a16-b106-8364fc4493fd	79eaff84-b6a0-46cf-825a-49c2133c5264	testuserdos	tedaca6099@emaxasp.com	+5612312132	121312313	CL	1990-03-13	35	123123123	\N	2026-01-06 23:30:51.526737+00
396becff-f77c-4108-901e-5261ca20f062	d52aabc2-41cc-4282-9a09-db7f1cbaa8b4	testuserdos	tedaca6099@emaxasp.com	+5612812781821	12312313123	CL	2000-11-12	25	123123123123	\N	2026-01-06 23:32:44.98808+00
c2994bf1-e991-4c31-b82c-7b99830b2177	19940620-d94c-4447-8ef5-8b77cc93e20d	testuserdos	tedaca6099@emaxasp.com	+56123123123123	123123123	CL	1212-12-12	813	123123123123	\N	2026-01-07 00:12:17.686622+00
\.


--
-- Data for Name: password_reset_tokens; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.password_reset_tokens (id, user_id, token, used, created_at, expires_at) FROM stdin;
\.


--
-- Data for Name: payment_sessions; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.payment_sessions (id, user_id, status, items, total_amount, currency, language_code, user_email, provider, payment_method, external_payment_id, token, payment_url, qr_code, pix_code, return_url, cancel_url, provider_response, error_message, is_test, expires_at, created_at, updated_at) FROM stdin;
02a6073f-73d4-466b-8557-59df0d7a48af	9a96969f-c381-4c20-a8ac-91591d905e05	EXPIRED	[{"tourDate": [2026, 1, 10], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 40000.0000, "scheduleId": "74f67af0-9ef2-4416-92e0-49da7dde83e3", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "12828821", "dateOfBirth": [2009, 12, 14], "nationality": "CL", "phoneNumber": "+56123123123123", "pickupAddress": "12313123", "specialRequirements": null}], "pricePerPerson": 40000.0000, "numParticipants": 1, "specialRequests": null}]	40000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	02a6073f-73d4-466b-8557-59df0d7a48af	01abc939ca2cc5a7871c0ce516d1ad75013b034efacbbddc2bf796aee7374835	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-07 00:10:19.560897+00	2026-01-06 23:40:20.315311+00	2026-01-06 23:40:20.38565+00
72267b6d-301d-46ec-a5d7-63e50f2d333d	9a96969f-c381-4c20-a8ac-91591d905e05	EXPIRED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "123123123", "dateOfBirth": [1212, 12, 12], "nationality": "CL", "phoneNumber": "+56123123123123", "pickupAddress": "123123123123", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	72267b6d-301d-46ec-a5d7-63e50f2d333d	01ab721b8cb0a86ab1a3d83868663d6a55b241c77bdb2d3fd7772e129d7fdaf3	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-07 00:21:28.76798+00	2026-01-06 23:51:29.59533+00	2026-01-06 23:51:29.666125+00
297878cf-65b7-4fc9-8251-b6d1ad22cecb	6766affb-d0eb-4a54-83e3-4195ef7824b3	EXPIRED	[{"tourDate": [2025, 11, 30], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 80000.0000, "scheduleId": "e1110060-a7a9-4d79-a7c8-1b9a81c7c836", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego Alvarez", "documentId": "177769347", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "al lado", "specialRequirements": "nada"}, {"email": null, "fullName": "caro", "documentId": "16665327", "dateOfBirth": [1990, 12, 12], "nationality": "CL", "phoneNumber": null, "pickupAddress": "aca", "specialRequirements": null}], "pricePerPerson": 40000.0000, "numParticipants": 2, "specialRequests": null}]	80000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01abdf07ff759fdd7bc8f577a3d8cfb66f11ec2c738a99d7660da5a6114eb8f3	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-29 14:03:40.614289+00	2025-11-29 13:33:41.828466+00	2025-11-29 13:33:42.1057+00
74c19050-ef40-4af3-a2e8-a27d2d60ab47	6766affb-d0eb-4a54-83e3-4195ef7824b3	EXPIRED	[{"tourDate": [2025, 11, 30], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 80000.0000, "scheduleId": "e1110060-a7a9-4d79-a7c8-1b9a81c7c836", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego Alvarez", "documentId": "166622912", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "sisiiss", "specialRequirements": null}, {"email": null, "fullName": "nanausu", "documentId": "21882182", "dateOfBirth": null, "nationality": "CL", "phoneNumber": null, "pickupAddress": "mism ", "specialRequirements": null}], "pricePerPerson": 40000.0000, "numParticipants": 2, "specialRequests": null}]	80000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01abaeef392ccad7ff652702afe84a6dd8a0809b2c942be841cad15bb5331787	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-29 14:32:36.055044+00	2025-11-29 14:02:36.941296+00	2025-11-29 14:02:37.097712+00
74c73829-0a79-4207-87aa-2a382f6387db	6766affb-d0eb-4a54-83e3-4195ef7824b3	EXPIRED	[{"tourDate": [2025, 11, 30], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 80000.0000, "scheduleId": "e1110060-a7a9-4d79-a7c8-1b9a81c7c836", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego Alvarez", "documentId": "177769347", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "al lado", "specialRequirements": "nada"}, {"email": null, "fullName": "caro", "documentId": "16665327", "dateOfBirth": [1990, 12, 12], "nationality": "CL", "phoneNumber": null, "pickupAddress": "aca", "specialRequirements": null}], "pricePerPerson": 40000.0000, "numParticipants": 2, "specialRequests": null}]	80000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01ab0ef7edc52f143f823346c4ee2e921aa3721417b56ed1d19e921cfdc476eb	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-29 14:04:04.998538+00	2025-11-29 13:34:05.892926+00	2025-11-29 13:34:06.04922+00
052bf181-8d02-4041-b76d-d9bc029e1f87	6766affb-d0eb-4a54-83e3-4195ef7824b3	FAILED	[{"tourDate": [2025, 11, 29], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "d013b442-c9c2-48e8-a3ba-5f459c1b832b", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego", "documentId": "28188221", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "veamos ", "specialRequirements": "dejamos"}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01ab517c77d6b72c9354e2969c6b8c40d07ce6578ce73638f37dc19f8f25604b	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	Payment not completed by provider	t	2025-11-29 15:34:41.802569+00	2025-11-29 15:04:42.133632+00	2025-11-29 15:12:55.293256+00
130c8978-5f01-4320-a926-0a498ba2de41	6766affb-d0eb-4a54-83e3-4195ef7824b3	EXPIRED	[{"tourDate": [2025, 11, 29], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "d013b442-c9c2-48e8-a3ba-5f459c1b832b", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego Alvarez", "documentId": "177769347", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "hostal al lado ", "specialRequirements": null}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01ab4bde800d31cfaccaa0d026793ec797dad1b472fd40bbf3b91b63b034f752	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-29 15:17:20.424055+00	2025-11-29 14:47:21.293828+00	2025-11-29 14:47:21.521069+00
a4fd3b5c-b1eb-4db3-aec6-60c42c63f97c	6766affb-d0eb-4a54-83e3-4195ef7824b3	COMPLETED	[{"tourDate": [2025, 11, 29], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "d013b442-c9c2-48e8-a3ba-5f459c1b832b", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego Alvarez", "documentId": "17776934-7", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "aca al lado", "specialRequirements": null}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01ab0a8bab540b2bed7237f7f5197c49583162cccd0eb0b1452efd67bf558506	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-29 16:01:50.919466+00	2025-11-29 15:31:52.071657+00	2025-11-29 15:34:44.798457+00
dc1fc5eb-415c-4d00-9cef-8f10315e9330	6766affb-d0eb-4a54-83e3-4195ef7824b3	EXPIRED	[{"tourDate": [2025, 11, 29], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "d013b442-c9c2-48e8-a3ba-5f459c1b832b", "participants": [{"email": "simagico@gmail.com", "fullName": "Diego", "documentId": "28188221", "dateOfBirth": [1990, 12, 11], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "veamos ", "specialRequirements": "dejamos"}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	simagico@gmail.com	TRANSBANK	WEBPAY	\N	01abab7b240c6e9ce5dbc4193c5375278690f266e4fa6f3bdea8f716e91140d4	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-29 15:33:47.690383+00	2025-11-29 15:03:48.916933+00	2025-11-29 15:03:49.331744+00
7cbadf9b-7098-49d5-8a4a-0ef1d4a67de4	a762c59a-8a68-42de-8c69-9b14b2871696	EXPIRED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	MERCADOPAGO	PIX	\N	3018101972-c229eb89-037d-4bd5-bec6-ae4e0e8eb123	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-c229eb89-037d-4bd5-bec6-ae4e0e8eb123	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-30 00:48:26.530171+00	2025-11-30 00:18:27.310102+00	2025-11-30 00:18:27.617335+00
f89d8efd-0a5f-4253-8003-e6728420e40f	a762c59a-8a68-42de-8c69-9b14b2871696	EXPIRED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	MERCADOPAGO	PIX	\N	3018101972-671e04f4-77ee-4b55-b38b-ea0618f71842	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-671e04f4-77ee-4b55-b38b-ea0618f71842	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-30 00:51:01.012965+00	2025-11-30 00:21:01.487088+00	2025-11-30 00:21:01.638541+00
3102e222-f160-4319-8a05-8f0266c59075	a762c59a-8a68-42de-8c69-9b14b2871696	EXPIRED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	MERCADOPAGO	CREDIT_CARD	\N	3018101972-c891925f-aee4-496f-8664-16842855bf5a	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-c891925f-aee4-496f-8664-16842855bf5a	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-30 00:55:48.691576+00	2025-11-30 00:25:49.159668+00	2025-11-30 00:25:49.310305+00
3d8f5515-02dc-4038-9d7b-7a05e7ae7661	a762c59a-8a68-42de-8c69-9b14b2871696	EXPIRED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	MERCADOPAGO	CREDIT_CARD	\N	3018101972-6ab3b09e-9f9b-47aa-8832-f097e910ddb3	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-6ab3b09e-9f9b-47aa-8832-f097e910ddb3	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-30 00:59:06.128369+00	2025-11-30 00:29:06.622136+00	2025-11-30 00:29:06.775024+00
3ea044eb-6c8b-4c76-9f76-f4d65ec5a2a6	a762c59a-8a68-42de-8c69-9b14b2871696	FAILED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	TRANSBANK	WEBPAY	\N	01ab998d2facf3ed3d31c2676322da78ff3d787722b05bb6738008b749c83a59	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	Payment not completed by provider	t	2025-11-30 01:44:58.917583+00	2025-11-30 01:14:59.839554+00	2025-11-30 01:16:35.6447+00
489f71d5-8850-45d1-b5aa-c0726532b8ea	a762c59a-8a68-42de-8c69-9b14b2871696	COMPLETED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	TRANSBANK	WEBPAY	\N	01ab0483acc4ef09e5fd52ebe92be89dbc71908336e10dc98f117e3f5f91c44d	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-30 02:12:17.061675+00	2025-11-30 01:42:18.083732+00	2025-11-30 01:42:59.179982+00
219b8db7-98e1-4948-be8b-5a98dec67130	a762c59a-8a68-42de-8c69-9b14b2871696	EXPIRED	[{"tourDate": [2025, 12, 1], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "126ad837-23d0-4638-a726-d60da1bd942a", "participants": [{"email": "cbarrazamorgado@gmail.com", "fullName": "Carolina Barraza", "documentId": "27277212-2", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+5691389559", "pickupAddress": "Al lado ", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	cbarrazamorgado@gmail.com	TRANSBANK	WEBPAY	\N	01ab6ad1bcc242f5ea0f5f634bf53f9b52fc48168a557cc5dbfed1ae34e1b3d5	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-11-30 02:11:12.134019+00	2025-11-30 01:41:45.072021+00	2025-11-30 01:41:45.224685+00
7f60e888-3380-478f-819b-1468d1eb5650	032d759e-62e4-4f77-9a4c-958da92a5aa4	COMPLETED	[{"tourDate": [2025, 12, 29], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 40000.0000, "scheduleId": "03c15999-e03d-4075-a8a9-17f52711085e", "participants": [{"email": "vodisdsdfsdf@northernchile.com", "fullName": "cosito", "documentId": "17776934-7", "dateOfBirth": null, "nationality": "CL", "phoneNumber": "+56912877633", "pickupAddress": "al lado", "specialRequirements": null}], "pricePerPerson": 40000.0000, "numParticipants": 1, "specialRequests": null}]	40000.0000	CLP	es	vodisdsdfsdf@northernchile.com	TRANSBANK	WEBPAY	\N	01abc8fbea829be3ca5809f2aed37a8b47e952eab4d6f2609fcd42871b054a00	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2025-12-09 15:26:50.68392+00	2025-12-09 14:56:51.593044+00	2025-12-09 14:59:13.417527+00
7cb14029-c0f3-40c5-be17-955c34f792f9	32c62332-1bda-4f50-af00-211324b5df8e	EXPIRED	[{"tourDate": [2026, 1, 9], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "223e67d9-44f5-413a-9b57-78bca0b71982", "participants": [{"email": "yihira3681@icousd.com", "fullName": "Usuariopruebadiego", "documentId": "1213123123", "dateOfBirth": [1990, 11, 14], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "al lado", "specialRequirements": "nada"}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	yihira3681@icousd.com	MERCADOPAGO	CREDIT_CARD	7cb14029-c0f3-40c5-be17-955c34f792f9	3018101972-51ffbc07-ffff-412e-8e65-f01a508f57bd	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-51ffbc07-ffff-412e-8e65-f01a508f57bd	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-06 17:26:54.901479+00	2026-01-06 16:56:55.765964+00	2026-01-06 16:56:55.93559+00
d975729b-6c7f-409e-8159-7770ccb68965	ff9d43b9-a699-4e24-833d-3e90b9fcf7ea	COMPLETED	[{"tourDate": [2026, 1, 9], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "223e67d9-44f5-413a-9b57-78bca0b71982", "participants": [{"email": "davenac878@cameltok.com", "fullName": "usuariotest", "documentId": "8128128181", "dateOfBirth": [1990, 11, 14], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "919219292121", "specialRequirements": null}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	davenac878@cameltok.com	MERCADOPAGO	CREDIT_CARD	d975729b-6c7f-409e-8159-7770ccb68965	3018101972-3bc85ead-e3a8-4b6b-9ef9-ec4d29e9b9a7	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-3bc85ead-e3a8-4b6b-9ef9-ec4d29e9b9a7	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-06 20:37:29.030064+00	2026-01-06 20:07:29.483218+00	2026-01-06 20:26:43.938649+00
ada5dba9-667c-492f-b5f1-cf3c91b7c008	ff9d43b9-a699-4e24-833d-3e90b9fcf7ea	EXPIRED	[{"tourDate": [2026, 1, 9], "tourName": "Tour Lagunas Escondidas de Baltinache & Vallecito", "itemTotal": 45000.0000, "scheduleId": "223e67d9-44f5-413a-9b57-78bca0b71982", "participants": [{"email": "davenac878@cameltok.com", "fullName": "usuariotest", "documentId": "8128128181", "dateOfBirth": [1990, 11, 14], "nationality": "CL", "phoneNumber": "+56942877633", "pickupAddress": "919219292121", "specialRequirements": null}], "pricePerPerson": 45000.0000, "numParticipants": 1, "specialRequests": null}]	45000.0000	CLP	es	davenac878@cameltok.com	MERCADOPAGO	CREDIT_CARD	ada5dba9-667c-492f-b5f1-cf3c91b7c008	3018101972-be4923fc-c44b-4de8-845f-ab569dc4efcd	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-be4923fc-c44b-4de8-845f-ab569dc4efcd	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-06 20:34:51.653758+00	2026-01-06 20:04:52.42926+00	2026-01-06 20:04:52.6642+00
f64723a0-b05c-4a32-b8f4-f806f71b5785	9a96969f-c381-4c20-a8ac-91591d905e05	COMPLETED	[{"tourDate": [2026, 1, 10], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 40000.0000, "scheduleId": "74f67af0-9ef2-4416-92e0-49da7dde83e3", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "18918281212", "dateOfBirth": [1990, 3, 12], "nationality": "CL", "phoneNumber": "+565692877633", "pickupAddress": "123123123123", "specialRequirements": "123123123"}], "pricePerPerson": 40000.0000, "numParticipants": 1, "specialRequests": null}]	40000.0000	CLP	es	tedaca6099@emaxasp.com	MERCADOPAGO	CREDIT_CARD	f64723a0-b05c-4a32-b8f4-f806f71b5785	3018101972-4ebe8a87-5453-4dbc-a49c-e11b6b78ece7	https://sandbox.mercadopago.cl/checkout/v1/redirect?pref_id=3018101972-4ebe8a87-5453-4dbc-a49c-e11b6b78ece7	\N	\N	https://www.northernchile.com/payment/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-06 23:19:37.867398+00	2026-01-06 22:49:38.773248+00	2026-01-06 22:52:09.540742+00
6cebb440-3fe2-4eae-a40c-3c8370d65c92	9a96969f-c381-4c20-a8ac-91591d905e05	COMPLETED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "18918281212", "dateOfBirth": [1990, 3, 12], "nationality": "CL", "phoneNumber": "+565692877633", "pickupAddress": "123123123123", "specialRequirements": "123123123"}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	6cebb440-3fe2-4eae-a40c-3c8370d65c92	01ab2cb185490b5c7a6e416b3317fd4f6c07c2854800814147c8a53ca8199c68	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-06 23:40:16.306624+00	2026-01-06 23:10:17.172557+00	2026-01-06 23:10:59.331632+00
718cb25f-39e0-4446-bc5f-7ba4c1a369dc	9a96969f-c381-4c20-a8ac-91591d905e05	COMPLETED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "211231231", "dateOfBirth": [1990, 3, 13], "nationality": "CL", "phoneNumber": "+569230121212", "pickupAddress": "12313123", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	718cb25f-39e0-4446-bc5f-7ba4c1a369dc	01ab9aa061137c6be4f4d9ba676c7e36f906becd196810992aa169dd6e94b981	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-06 23:54:00.081681+00	2026-01-06 23:24:01.095314+00	2026-01-06 23:24:39.266623+00
a6c98c1e-ef10-4334-983f-ff8d5a162334	9a96969f-c381-4c20-a8ac-91591d905e05	FAILED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "121312313", "dateOfBirth": [1990, 3, 13], "nationality": "CL", "phoneNumber": "+5612312132", "pickupAddress": "123123123", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	a6c98c1e-ef10-4334-983f-ff8d5a162334	01ab230b02889898f2cb23c0889b0e7239bb4bf8e25eac7d354e15a28379d3d2	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	Payment not completed by provider	t	2026-01-06 23:56:03.745632+00	2026-01-06 23:26:04.557033+00	2026-01-06 23:27:01.487328+00
b5d36134-5439-4261-8165-3b2af774ab10	9a96969f-c381-4c20-a8ac-91591d905e05	COMPLETED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "121312313", "dateOfBirth": [1990, 3, 13], "nationality": "CL", "phoneNumber": "+5612312132", "pickupAddress": "123123123", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	b5d36134-5439-4261-8165-3b2af774ab10	01ab593c0b697e715e78af3650c6a0f7098ea23fd374cee8b6c76b7baf767cff	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-07 00:00:03.166127+00	2026-01-06 23:30:03.966884+00	2026-01-06 23:30:51.904572+00
efe5afb9-133f-4b63-83f1-15dffaeb05ae	9a96969f-c381-4c20-a8ac-91591d905e05	COMPLETED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "12312313123", "dateOfBirth": [2000, 11, 12], "nationality": "CL", "phoneNumber": "+5612812781821", "pickupAddress": "123123123123", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	efe5afb9-133f-4b63-83f1-15dffaeb05ae	01ab6fa0df34681c48e579b9781cca843c2e91e2ec9c59898f9d50334f2fd5e8	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-07 00:02:10.761659+00	2026-01-06 23:32:11.105907+00	2026-01-06 23:32:45.361328+00
59913cab-dbfa-4009-af3c-304589821b65	9a96969f-c381-4c20-a8ac-91591d905e05	FAILED	[{"tourDate": [2026, 1, 11], "tourName": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "itemTotal": 55000.0000, "scheduleId": "a42456d5-7ca1-4679-993b-79e76508e133", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "12828821", "dateOfBirth": [2009, 12, 14], "nationality": "CL", "phoneNumber": "+56123123123123", "pickupAddress": "12313123", "specialRequirements": null}], "pricePerPerson": 55000.0000, "numParticipants": 1, "specialRequests": null}]	55000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	59913cab-dbfa-4009-af3c-304589821b65	01ab4c08190a698651bf78d4f81d3b38e76f04b0b945a3fa05a2ff5d6cc52200	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	Payment not completed by provider	t	2026-01-07 00:04:42.33997+00	2026-01-06 23:34:42.673909+00	2026-01-06 23:35:15.963313+00
32730c5a-5c39-4288-946d-4219b0765381	9a96969f-c381-4c20-a8ac-91591d905e05	REFUNDED	[{"tourDate": [2026, 1, 10], "tourName": "Tour Valle del Arcoíris & Hierbas Buenas", "itemTotal": 40000.0000, "scheduleId": "74f67af0-9ef2-4416-92e0-49da7dde83e3", "participants": [{"email": "tedaca6099@emaxasp.com", "fullName": "testuserdos", "documentId": "123123123", "dateOfBirth": [1212, 12, 12], "nationality": "CL", "phoneNumber": "+56123123123123", "pickupAddress": "123123123123", "specialRequirements": null}], "pricePerPerson": 40000.0000, "numParticipants": 1, "specialRequests": null}]	40000.0000	CLP	es	tedaca6099@emaxasp.com	TRANSBANK	WEBPAY	32730c5a-5c39-4288-946d-4219b0765381	01ab6861e6e820611755ba96a154535b0d0f5380f22f9f9f378ead5a5dfabc76	https://webpay3gint.transbank.cl/webpayserver/initTransaction	\N	\N	https://www.northernchile.com/api/payments/callback	https://www.northernchile.com/checkout	\N	\N	t	2026-01-07 00:41:47.506093+00	2026-01-07 00:11:48.55006+00	2026-01-07 00:19:23.231794+00
\.


--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.payments (id, booking_id, provider, payment_method, external_payment_id, status, amount, currency, payment_url, details_url, qr_code, pix_code, token, expires_at, provider_response, error_message, created_at, updated_at, is_test, idempotency_key, payment_session_id) FROM stdin;
\.


--
-- Data for Name: private_tour_requests; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.private_tour_requests (id, user_id, customer_name, customer_email, customer_phone, requested_tour_type, requested_start_datetime, num_participants, special_requests, status, quoted_price, payment_link_id, created_at) FROM stdin;
02bc40b7-b885-4b66-a691-77071c02b170	\N	usuaiotest	test@user.com	+56942877633	ASTRONOMICAL	2025-12-12	2	test	PENDING	\N	\N	2025-11-29 03:02:53.412863+00
\.


--
-- Data for Name: tour_schedules; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.tour_schedules (id, tour_id, start_datetime, max_participants, status, assigned_guide_id, created_at) FROM stdin;
d013b442-c9c2-48e8-a3ba-5f459c1b832b	26e2ac01-37e9-4483-9ce7-506e6782538b	2025-11-29 22:02:00+00	10	CLOSED	\N	2025-11-28 23:10:43.817637+00
e1110060-a7a9-4d79-a7c8-1b9a81c7c836	5a23925b-e532-4101-bf02-b5281ecb7eb9	2025-11-30 08:00:00+00	20	CLOSED	\N	2025-11-29 04:23:03.54431+00
126ad837-23d0-4638-a726-d60da1bd942a	d43e8d8d-d09c-4564-ba84-e89364d7e028	2025-12-01 21:00:00+00	10	CLOSED	\N	2025-11-30 00:15:21.996863+00
176b854d-5b23-48bd-bec7-1378029a4a33	d43e8d8d-d09c-4564-ba84-e89364d7e028	2025-12-31 21:00:00+00	10	CLOSED	\N	2025-12-04 16:07:40.480234+00
a1d26c37-77fc-4944-8667-e8d032bf67da	5a23925b-e532-4101-bf02-b5281ecb7eb9	2025-12-30 08:00:00+00	10	CLOSED	\N	2025-12-04 16:07:50.808212+00
03c15999-e03d-4075-a8a9-17f52711085e	5a23925b-e532-4101-bf02-b5281ecb7eb9	2025-12-29 08:00:00+00	10	CLOSED	\N	2025-12-04 16:07:59.011927+00
425a30f1-4857-4261-be28-f6b61aca690d	5a23925b-e532-4101-bf02-b5281ecb7eb9	2026-01-01 20:00:00+00	30	CLOSED	\N	2025-12-09 14:43:06.936301+00
4cfb344b-b463-4d6c-b294-1daec9152775	5a23925b-e532-4101-bf02-b5281ecb7eb9	2026-01-08 08:00:00+00	10	CLOSED	\N	2026-01-06 16:54:37.837256+00
223e67d9-44f5-413a-9b57-78bca0b71982	26e2ac01-37e9-4483-9ce7-506e6782538b	2026-01-09 21:00:00+00	14	CLOSED	\N	2026-01-06 16:54:57.894063+00
74f67af0-9ef2-4416-92e0-49da7dde83e3	5a23925b-e532-4101-bf02-b5281ecb7eb9	2026-01-10 08:00:00+00	10	CLOSED	\N	2026-01-06 16:55:06.466005+00
a42456d5-7ca1-4679-993b-79e76508e133	d43e8d8d-d09c-4564-ba84-e89364d7e028	2026-01-11 21:00:00+00	10	CLOSED	\N	2026-01-06 16:55:13.330475+00
\.


--
-- Data for Name: tours; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.tours (id, owner_id, name_translations, description_blocks_translations, itinerary_translations, equipment_translations, additional_info_translations, wind_sensitive, moon_sensitive, cloud_sensitive, content_key, slug, guide_name, category, price, default_max_participants, duration_hours, default_start_time, recurring, recurrence_rule, status, created_at, updated_at, deleted_at) FROM stdin;
26e2ac01-37e9-4483-9ce7-506e6782538b	855ef29d-4275-4015-9fab-8d78bdea5fe2	{"en": "Hidden Lagoons of Baltinache & Vallecito Tour", "es": "Tour Lagunas Escondidas de Baltinache & Vallecito", "pt": "Tour Lagoas Escondidas de Baltinache & Vallecito"}	{"en": [{"type": "paragraph", "content": "Observe salt mirrors, golden dunes and living geology on an intimate sunrise route."}, {"type": "heading", "content": "Hidden Lagoons of Baltinache"}, {"type": "paragraph", "content": "This complex of seven saltwater pools forms a surreal oasis in the middle of the desert, located in the Llano de la Paciencia, at the foot of the Cordillera de la Sal and the Cordillera de Domeyko. Its geological origin dates back to the Cretaceous period, when the rise of the Andes trapped marine arms that, through evaporation and millions of years of aridity, formed this salt flat with extreme salinity of 220 grams per liter—almost six times that of the Dead Sea. The lagoons, with an average diameter of only 5 meters, are natural mirrors where the desert sky reflects in turquoise and crystalline waters that create a striking contrast with the terracotta and reddish tones of the surrounding landscape. Only two of the seven lagoons are enabled for bathing, offering a unique floating experience while the other five remain protected, preserving their virgin beauty. In the last lagoon, reddish gelatinous formations compatible with extremophile bacteria have even been detected, evidence of life in extreme conditions that fascinates scientists and visitors alike."}, {"type": "paragraph", "content": "Visitors experience a deep connection with living geology: when immersing themselves in the hyper-saline waters, they feel on their skin the history of a prehistoric ocean, while the desert silence amplifies the perception of this landscape that combines the austere majesty of the Cordillera de la Sal with the delicate transparency of the lagoons. Access via a reddish dirt road dotted with cacti intensifies the feeling of arriving at a secret place, almost untouched since its formation millions of years ago."}, {"type": "heading", "content": "Vallecito & Magic Bus"}, {"type": "paragraph", "content": "Located precisely in the Llano de la Paciencia, Vallecito represents the less crowded but equally spectacular part of the Cordillera de la Sal, where time and nature have sculpted landscapes that seem from another planet. The striking formation of the Hidden Magic Bus emerges as a contemporary icon in this prehistoric setting, creating a surreal scene that contrasts recent human footprints with the immensity of the desert. The impressive rock formations of Vallecito display a palette of changing colors according to the daylight, from intense ochers to ethereal violets, revealing sedimentary layers that narrate the tectonic history of the region."}, {"type": "paragraph", "content": "The geological experience intensifies when walking on the high dunes, where the soft sand underfoot transmits the energy of the wind, the main architect of this landscape. From the summits, panoramic views extend as far as the eye can see, showing textures and erosive patterns that only millions of years of wind and aridity can create. When exploring the ancient channels used by pre-Hispanic inhabitants, visitors connect with human ingenuity adapted to this extreme environment, completing an experience that unites geology, history and the primordial force of the Atacama Desert."}], "es": [{"type": "paragraph", "content": "Observa espejos de sal, dunas doradas y geología viva en una ruta íntima al amanecer."}, {"type": "heading", "content": "Lagunas Escondidas de Baltinache"}, {"type": "paragraph", "content": "Este complejo de siete pozas de agua salada forma un oasis surreal en medio del desierto, ubicado en el Llano de la Paciencia, al pie de la Cordillera de la Sal y la Cordillera de Domeyko. Su origen geológico se remonta al Cretácico, cuando el surgimiento de los Andes atrapó brazos marinos que, mediante evaporación y millones de años de aridez, formaron este salar con una salinidad extrema de 220 gramos por litro—casi seis veces más que el mar Muerto. Las lagunas, con solo 5 metros de diámetro promedio, son espejos naturales donde el cielo desértico se refleja en aguas turquesas y cristalinas que crean un impactante contraste con los tonos terracota y rojizos del paisaje circundante. Solo dos de las siete lagunas están habilitadas para el baño, ofreciendo una experiencia de flotación única mientras las otras cinco permanecen protegidas, conservando su belleza virgen. En la última laguna, incluso se han detectado formaciones rojizas gelatinosas compatibles con bacterias extremófilas, evidencia de vida en condiciones límites que fascina a científicos y visitantes por igual."}, {"type": "paragraph", "content": "Los visitantes experimentan una conexión profunda con la geología viva: al sumergirse en las aguas hiper salinas, sienten en su piel la historia de un océano prehistórico, mientras el silencio del desierto amplifica la percepción de este paisaje que combina la austera majestuosidad de la Cordillera de la Sal con la delicada transparencia de las lagunas. El acceso por un camino de tierra rojiza salpicado de cactus intensifica la sensación de llegar a un lugar secreto, casi intacto desde su formación hace millones de años."}, {"type": "heading", "content": "Vallecito & Bus Mágico"}, {"type": "paragraph", "content": "Ubicado precisamente en el Llano de la Paciencia, Vallecito representa la parte menos concurrida pero igualmente espectacular de la Cordillera de la Sal, donde el tiempo y la naturaleza han esculpido paisajes que parecen de otro planeta. La llamativa formación del Bus Mágico Escondido surge como un ícono contemporáneo en este escenario prehistórico, creando una escena surreal que contrasta la huella humana reciente con la inmensidad del desierto. Las formaciones rocosas impresionantes de Vallecito exhiben una paleta de colores cambiantes según la luz del día, desde ocres intensos hasta violetas etéreos, revelando capas sedimentarias que narran la historia tectónica de la región."}, {"type": "paragraph", "content": "La experiencia geológica se intensifica al caminar por las altas dunas, donde la suave arena bajo los pies transmite la energía del viento, el principal arquitecto de este paisaje. Desde las cumbres, las vistas panorámicas se extienden hasta donde alcanza la vista, mostrando texturas y patrones erosivos que solo millones de años de viento y aridez pueden crear. Al explorar los antiguos canales utilizados por habitantes prehispánicos, los visitantes conectan con el ingenio humano adaptado a este entorno extremo, completando una experiencia que une geología, historia y la fuerza primigenia del desierto de Atacama."}], "pt": [{"type": "paragraph", "content": "Observe espelhos de sal, dunas douradas e geologia viva em uma rota íntima ao amanhecer."}, {"type": "heading", "content": "Lagoas Escondidas de Baltinache"}, {"type": "paragraph", "content": "Este complexo de sete poças de água salgada forma um oásis surreal no meio do deserto, localizado no Llano de la Paciencia, ao pé da Cordilheira do Sal e da Cordilheira de Domeyko. Sua origem geológica remonta ao Cretáceo, quando o surgimento dos Andes aprisionou braços marinhos que, mediante evaporação e milhões de anos de aridez, formaram este salar com uma salinidade extrema de 220 gramas por litro—quase seis vezes mais que o Mar Morto. As lagoas, com apenas 5 metros de diâmetro em média, são espelhos naturais onde o céu desértico se reflete em águas turquesa e cristalinas que criam um contraste impactante com os tons terracota e avermelhados da paisagem circundante. Apenas duas das sete lagoas estão habilitadas para banho, oferecendo uma experiência de flutuação única enquanto as outras cinco permanecem protegidas, conservando sua beleza virgem. Na última lagoa, inclusive, foram detectadas formações gelatinosas avermelhadas compatíveis com bactérias extremófilas, evidência de vida em condições limites que fascina cientistas e visitantes por igual."}, {"type": "paragraph", "content": "Os visitantes experimentam uma conexão profunda com a geologia viva: ao se submergir nas águas hipersalinas, sentem na pele a história de um oceano pré-histórico, enquanto o silêncio do deserto amplifica a percepção desta paisagem que combina a austera majestade da Cordilheira do Sal com a delicada transparência das lagoas. O acesso por um caminho de terra avermelhada salpicado de cactos intensifica a sensação de chegar a um lugar secreto, quase intacto desde sua formação há milhões de anos."}, {"type": "heading", "content": "Vallecito & Ônibus Mágico"}, {"type": "paragraph", "content": "Localizado precisamente no Llano de la Paciencia, Vallecito representa a parte menos concorrida mas igualmente espetacular da Cordilheira do Sal, onde o tempo e a natureza esculpiram paisagens que parecem de outro planeta. A chamativa formação do Ônibus Mágico Escondido surge como um ícone contemporâneo neste cenário pré-histórico, criando uma cena surreal que contrasta a pegada humana recente com a imensidão do deserto. As impressionantes formações rochosas de Vallecito exibem uma paleta de cores cambiantes conforme a luz do dia, desde ocre intenso até violetas etéreos, revelando camadas sedimentares que narram a história tectônica da região."}, {"type": "paragraph", "content": "A experiência geológica se intensifica ao caminhar pelas altas dunas, onde a suave areia sob os pés transmite a energia do vento, o principal arquiteto desta paisagem. Desde os cumes, as vistas panorâmicas se estendem até onde alcança a vista, mostrando texturas e padrões erosivos que apenas milhões de anos de vento e aridez podem criar. Ao explorar os antigos canais utilizados por habitantes pré-hispânicos, os visitantes conectam com o engenho humano adaptado a este ambiente extremo, completando uma experiência que une geologia, história e a força primigênia do deserto do Atacama."}]}	{"en": [{"time": "07:30 – 08:00", "description": "Pickup and departure from San Pedro de Atacama."}, {"time": "08:00 – 09:30", "description": "Hidden Lagoons of Baltinache: floating in hypersaline waters and geological explanation."}, {"time": "09:30 – 10:30", "description": "Transfer through Llano de la Paciencia, viewpoints and landscape interpretation."}, {"time": "10:30 – 11:30", "description": "Vallecito and Magic Bus: walk through dunes and rock formations."}, {"time": "11:30 – 12:30", "description": "Light cocktail, tour closure and return to San Pedro."}], "es": [{"time": "07:30 – 08:00", "description": "Recogida y salida desde San Pedro de Atacama."}, {"time": "08:00 – 09:30", "description": "Lagunas Escondidas de Baltinache: flotación en aguas hipersalinas y explicación geológica."}, {"time": "09:30 – 10:30", "description": "Traslado por el Llano de la Paciencia, miradores y lectura del paisaje."}, {"time": "10:30 – 11:30", "description": "Vallecito y Bus Mágico: caminata por dunas y formaciones rocosas."}, {"time": "11:30 – 12:30", "description": "Cóctel ligero, cierre del tour y retorno a San Pedro."}], "pt": [{"time": "07:30 – 08:00", "description": "Busca e saída de San Pedro de Atacama."}, {"time": "08:00 – 09:30", "description": "Lagoas Escondidas de Baltinache: flutuação em águas hipersalinas e explicação geológica."}, {"time": "09:30 – 10:30", "description": "Translado pelo Llano de la Paciencia, mirantes e leitura da paisagem."}, {"time": "10:30 – 11:30", "description": "Vallecito e Ônibus Mágico: caminhada por dunas e formações rochosas."}, {"time": "11:30 – 12:30", "description": "Coquetel leve, encerramento do tour e retorno a San Pedro."}]}	\N	{"en": ["The Llano de la Paciencia is not just a route, but a geological sub-basin of the Atacama Salt Flat where the absence of vegetation and the presence of mineral salts create a perfect lunar scenario to understand evaporation processes and salt flat formation.", "The Cordillera de la Sal, which frames much of this route, is part of the emblematic Valle de la Muerte, an outcrop of sedimentary rocks and evaporites that evidences the ancient presence of prehistoric salt lakes."], "es": ["El Llano de la Paciencia no es solo un trayecto, sino una subcuenca geológica del Salar de Atacama donde la ausencia de vegetación y la presencia de sales minerales crean un escenario lunar perfecto para entender los procesos de evaporación y formación de salares.", "La Cordillera de la Sal, que enmarca gran parte de esta ruta, es parte del emblemático Valle de la Muerte, un afloramiento de rocas sedimentarias y evaporitas que evidencia la antigua presencia de lagos salinos prehistóricos."], "pt": ["O Llano de la Paciencia não é apenas um trajeto, mas uma sub-bacia geológica do Salar de Atacama onde a ausência de vegetação e a presença de sais minerais criam um cenário lunar perfeito para entender os processos de evaporação e formação de salares.", "A Cordilheira do Sal, que emoldura grande parte desta rota, é parte do emblemático Vale da Morte, um afloramento de rochas sedimentares e evaporitos que evidencia a antiga presença de lagos salinos pré-históricos."]}	t	f	f	\N	tour-lagunas-escondidas-de-baltinache-vallecito	\N	REGULAR	45000.0000	15	5	\N	f	\N	PUBLISHED	\N	2025-12-03 21:04:24.015153+00	\N
d43e8d8d-d09c-4564-ba84-e89364d7e028	855ef29d-4275-4015-9fab-8d78bdea5fe2	{"en": "Astronomical Tour: Archaeoastronomy and Andean Cosmovision", "es": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina", "pt": "Tour Astronômico: Arqueoastronomia e Cosmovisão Andina"}	{"en": [{"type": "paragraph", "content": "An experience under the purest skies on the planet. In the heart of the Atacama Desert, where silence is as profound as the sky, we invite you to experience a real connection with the universe."}, {"type": "paragraph", "content": "At Northern Chile Astronomy, we combine science, history, and emotion to offer you an experience that transcends simple astronomical observation: here the sky is understood, felt, and shared."}, {"type": "heading", "content": "🔭 Astronomy with soul:"}, {"type": "paragraph", "content": "You will not just look at stars: you will learn to read them. Our guides, experts in astronomy and Andean cosmovision, reveal the secrets of the cosmos and how ancient Andean peoples interpreted it."}, {"type": "heading", "content": "📸 Memories of the universe:"}, {"type": "paragraph", "content": "Each visitor receives professional astrophotography: personal portraits under the stars and real deep space captures. Unique images, taken with high-end telescopes and cameras."}, {"type": "heading", "content": "🥂 Cocktail under the stars:"}, {"type": "paragraph", "content": "Between one talk and a nebula, enjoy wine, pisco, or hot drinks depending on the season, accompanied by snacks and good company."}, {"type": "heading", "content": "🚐 Comfort and safety:"}, {"type": "paragraph", "content": "Includes transportation from and to your hotel in San Pedro (or coordinated meeting point). We travel in safe and comfortable vehicles, designed for the desert environment."}, {"type": "heading", "content": "🌄 An incomparable natural setting"}, {"type": "paragraph", "content": "Our observatory is located on the dunes, far from town lights, in a pure and silent environment. Here, darkness is your ally and the sky your mirror. Every night is different, every visitor unique."}, {"type": "heading", "content": "🌠 A night you will never forget"}, {"type": "paragraph", "content": "More than a tour, it is an invitation to feel part of the universe. At Northern Chile Astronomy, the sky is not observed... it is lived."}], "es": [{"type": "paragraph", "content": "Una experiencia bajo los cielos más puros del planeta. En el corazón del desierto de Atacama, donde el silencio es tan profundo como el cielo, te invitamos a vivir una conexión real con el universo."}, {"type": "paragraph", "content": "En Northern Chile Astronomy, combinamos ciencia, historia y emoción para ofrecerte una experiencia que trasciende la simple observación astronómica: aquí el cielo se entiende, se siente y se comparte."}, {"type": "heading", "content": "🔭 Astronomía con alma:"}, {"type": "paragraph", "content": "No solo mirarás estrellas: aprenderás a leerlas. Nuestros guías, expertos en astronomía y cosmovisión andina, revelan los secretos del cosmos y cómo los antiguos pueblos de los Andes lo interpretaron."}, {"type": "heading", "content": "📸 Recuerdos del universo:"}, {"type": "paragraph", "content": "Cada visitante recibe astrofotografías profesionales: retratos personales bajo las estrellas y capturas reales del espacio profundo. Imágenes únicas, tomadas con telescopios y cámaras de alta gama."}, {"type": "heading", "content": "🥂 Cóctel bajo las estrellas:"}, {"type": "paragraph", "content": "Entre una charla y una nebulosa, disfruta de vino, pisco o bebidas calientes según la temporada, acompañadas de snacks y buena compañía."}, {"type": "heading", "content": "🚐 Comodidad y seguridad:"}, {"type": "paragraph", "content": "Incluye traslado desde y hacia tu hotel en San Pedro (o punto de encuentro coordinado). Viajamos en vehículos seguros y cómodos, pensados para el entorno del desierto."}, {"type": "heading", "content": "🌄 Un escenario natural incomparable"}, {"type": "paragraph", "content": "Nuestro observatorio se encuentra sobre las dunas, lejos de las luces del pueblo, en un entorno puro y silencioso. Aquí, la oscuridad es tu aliada y el cielo tu espejo. Cada noche es distinta, cada visitante, único."}, {"type": "heading", "content": "🌠 Una noche que no olvidarás"}, {"type": "paragraph", "content": "Más que un tour, es una invitación a sentirte parte del universo. En Northern Chile Astronomy, el cielo no se observa… se vive."}], "pt": [{"type": "paragraph", "content": "Uma experiência sob os céus mais puros do planeta. No coração do Deserto do Atacama, onde o silêncio é tão profundo quanto o céu, convidamos você a viver uma conexão real com o universo."}, {"type": "paragraph", "content": "Na Northern Chile Astronomy, combinamos ciência, história e emoção para oferecer uma experiência que transcende a simples observação astronômica: aqui o céu é compreendido, sentido e compartilhado."}, {"type": "heading", "content": "🔭 Astronomia com alma:"}, {"type": "paragraph", "content": "Você não apenas verá estrelas: aprenderá a lê-las. Nossos guias, especialistas em astronomia e cosmovisão andina, revelam os segredos do cosmos e como os antigos povos dos Andes o interpretaram."}, {"type": "heading", "content": "📸 Memórias do universo:"}, {"type": "paragraph", "content": "Cada visitante recebe astrofotografias profissionais: retratos pessoais sob as estrelas e capturas reais do espaço profundo. Imagens únicas, tiradas com telescópios e câmeras de alta gama."}, {"type": "heading", "content": "🥂 Coquetel sob as estrelas:"}, {"type": "paragraph", "content": "Entre uma conversa e uma nebulosa, aproveite vinho, pisco ou bebidas quentes conforme a temporada, acompanhados de snacks e boa companhia."}, {"type": "heading", "content": "🚐 Conforto e segurança:"}, {"type": "paragraph", "content": "Inclui traslado de ida e volta do seu hotel em San Pedro (ou ponto de encontro coordenado). Viajamos em veículos seguros e confortáveis, pensados para o ambiente desértico."}, {"type": "heading", "content": "🌄 Um cenário natural incomparável"}, {"type": "paragraph", "content": "Nosso observatório fica sobre as dunas, longe das luzes da cidade, em um ambiente puro e silencioso. Aqui, a escuridão é sua aliada e o céu seu espelho. Cada noite é diferente, cada visitante, único."}, {"type": "heading", "content": "🌠 Uma noite inesquecível"}, {"type": "paragraph", "content": "Mais que um tour, é um convite para se sentir parte do universo. Na Northern Chile Astronomy, o céu não se observa... se vive."}]}	{"en": [{"time": "21:00 – 21:30", "description": "Pickup at your accommodation or meeting point."}, {"time": "21:30 – 21:45", "description": "Welcome, presentation and astronomical talk."}, {"time": "21:45 – 23:30", "description": "Observation with telescopes, astrophotography and cocktail."}, {"time": "23:45", "description": "Return to hotels (flexible schedule depending on group)."}], "es": [{"time": "21:00 – 21:30", "description": "Recogida en tu alojamiento o punto de encuentro."}, {"time": "21:30 – 21:45", "description": "Bienvenida, presentación y charla astronómica."}, {"time": "21:45 – 23:30", "description": "Observación con telescopios, astrofotografía y cóctel."}, {"time": "23:45", "description": "Retorno a los hoteles (horario flexible según grupo)."}], "pt": [{"time": "21:00 – 21:30", "description": "Busca em seu alojamento ou ponto de encontro."}, {"time": "21:30 – 21:45", "description": "Boas-vindas, apresentação e palestra astronômica."}, {"time": "21:45 – 23:30", "description": "Observação com telescópios, astrofotografia e coquetel."}, {"time": "23:45", "description": "Retorno aos hotéis (horário flexível conforme o grupo)."}]}	{"en": ["Celestron NexStar 8SE (203 mm)", "Celestron 114 mm GoTo", "Sky-Watcher 130 EQ", "SWO SEESTAR S50 Smart Telescope", "Canon Mark III camera + wide angle lens"], "es": ["Celestron NexStar 8SE (203 mm)", "Celestron 114 mm GoTo", "Sky-Watcher 130 EQ", "SWO SEESTAR S50 Smart Telescope", "Cámara Canon Mark III + lente gran angular"], "pt": ["Celestron NexStar 8SE (203 mm)", "Celestron 114 mm GoTo", "Sky-Watcher 130 EQ", "SWO SEESTAR S50 Smart Telescope", "Câmera Canon Mark III + lente grande angular"]}	{"en": ["Bring warm clothes: the desert is cold at night.", "Children are welcome, always with adult supervision."], "es": ["Lleva ropa abrigada: el desierto es frío por la noche.", "Los niños son bienvenidos, siempre con supervisión de adultos."], "pt": ["Leve roupas quentes: o deserto é frio à noite.", "Crianças são bem-vindas, sempre com supervisão de adultos."]}	t	t	t	\N	tour-astronomico-arqueoastronomia-y-cosmovision-andina	\N	ASTRONOMICAL	55000.0000	15	3	21:00:00	t	0 21 * * *	PUBLISHED	\N	2026-01-05 17:06:03.122031+00	\N
5a23925b-e532-4101-bf02-b5281ecb7eb9	855ef29d-4275-4015-9fab-8d78bdea5fe2	{"en": "Rainbow Valley & Hierbas Buenas Tour", "es": "Tour Valle del Arcoíris & Hierbas Buenas", "pt": "Tour Vale do Arco-Íris & Hierbas Buenas"}	{"en": [{"type": "paragraph", "content": "We begin the day with breakfast among mountains that awaken with the first light of the sun. The road leads us towards Rainbow Valley, where minerals paint the earth with intense tones, forming one of the most astonishing landscapes of the Cordillera de Domeyko."}, {"type": "heading", "content": "Rainbow Valley: Mineral Palette of the Cordillera de Domeyko"}, {"type": "paragraph", "content": "Located 90 kilometers from San Pedro de Atacama at 3,500 meters above sea level, this geological valley is a mausoleum of color and time. Its chromatic spectrum is not mere decoration, but the autobiography of 90 million years of history. The rock formations belong to the Purilactis Formation (Late Cretaceous) and Tonel Formation, composed of siltstones and fine sandstones that were deposited in ancient fluvio-lacustrine systems. The intense reddish tones come from iron oxides (hematite) formed in oxidizing environments; the greens, from chlorite and epidote generated by hydrothermal alteration of hypabyssal intrusives; the yellows and ochers, from iron sulfides and evaporitic salts; while the whites are gypsum and halite, vestiges of ancient salt lagoons that shine like snow under the desert sun."}, {"type": "paragraph", "content": "The experience is an immersion in a living palette: walking among these formations eroded by wind and water over millions of years allows touching sedimentary layers that narrate the rise of the Andes. The morning, with low light, creates shadow plays that enhance each stratum, offering unique photographic moments where minerals seem to ignite from within. It is a natural laboratory where geology becomes art and every tone is a chapter of the tectonic evolution of the Atacama Puna."}, {"type": "heading", "content": "Hierbas Buenas: Amphitheater of Stone and Memory"}, {"type": "paragraph", "content": "This archaeological site, located 65 km north of San Pedro at the confluence of the Cordillera de Domeyko, the Cordillera de la Sal and the Andes (3,050 meters above sea level), is the largest rock art center in the entire San Pedro de Atacama archaeological area. In its 'natural amphitheater' of soft volcanic rocks and unique folding processes, over 1,000 petroglyphs are preserved representing four main cultural traditions: the naturalistic Taira, the Angostura tradition, Style 2 with Aguada influence, and Inca styles, with dates spanning from the Early Formative to the Colonial period."}, {"type": "paragraph", "content": "Visitors encounter a millennial dialogue sculpted in stone: anthropomorphs, camelids, Andean felines and surprising Amazonian primates (evidence of the extensive trade routes that connected the Altiplano with the Pacific Ocean). The petroglyphs are not simple drawings, but a ritual language that marked routes, indicated water sources and transmitted cosmovision. The morning sun scrapes the oxidized surfaces, highlighting the figures with golden light while the desert silence allows hearing the footsteps of the caravans that a thousand years ago traveled towards the oasis. It is a tangible connection with the Atacameño culture, where every engraving is a message of survival and spirituality in the most arid desert in the world."}], "es": [{"type": "paragraph", "content": "Iniciamos el día con un desayuno entre montañas que despiertan con la primera luz del sol. El camino nos conduce hacia el Valle del Arcoíris, donde los minerales pintan la tierra con tonos intensos, formando uno de los paisajes más asombrosos de la Cordillera de Domeyko."}, {"type": "heading", "content": "Valle del Arcoíris: Paleta Mineral de la Cordillera de Domeyko"}, {"type": "paragraph", "content": "A 90 kilómetros de San Pedro de Atacama y a 3.500 metros de altitud, este valle geológico es un mausoleo de color y tiempo. Su espectro cromático no es mera decoración, sino la autobiografía de 90 millones de años de historia. Las formaciones rocosas pertenecen a la Formación Purilactis (Cretácico tardío) y Formación Tonel, compuestas por limolitas y areniscas finas que fueron depositadas en antiguos sistemas fluvio-lacustres. Los intensos tonos rojizos provienen de óxidos de hierro (hematita) formados en ambientes oxidantes; los verdes, de clorita y epidota generadas por alteración hidrotermal de intrusivos hipabisales; los amarillos y ocres, de sulfuros de hierro y sales evaporíticas; mientras que los blancos son yeso y halita, vestigios de antiguas lagunas salinas que brillan como nieve bajo el sol desértico."}, {"type": "paragraph", "content": "La experiencia es una inmersión en una paleta viva: caminar entre estas formaciones erosionadas por el viento y el agua durante millones de años permite tocar capas sedimentarias que narran el alzamiento de los Andes. La mañana, con la luz baja, crea juegos de sombras que realzan cada estrato, ofreciendo momentos fotográficos únicos donde los minerales parecen encenderse desde adentro. Es un laboratorio natural donde la geología se hace arte y cada tono es un capítulo de la evolución tectónica de la Puna de Atacama."}, {"type": "heading", "content": "Hierbas Buenas: Anfiteatro de Piedra y Memoria"}, {"type": "paragraph", "content": "Este sitio arqueológico, ubicado a 65 km al norte de San Pedro en la confluencia de la Cordillera de Domeyko, la Cordillera de la Sal y los Andes (3.050 m.s.n.m.), es el mayor centro de arte rupestre de toda la zona arqueológica de San Pedro de Atacama. En su 'anfiteatro natural' de rocas blandas volcánicas y procesos de plegamiento únicos, se conservan más de 1.000 petroglifos que representan cuatro tradiciones culturales principales: la naturalista de Taira, la tradición Angostura, el Estilo 2 de influencia Aguada y estilos Incásicos, con dataciones que abarcan desde el Formativo Temprano hasta el período Colonial."}, {"type": "paragraph", "content": "Los visitantes se encuentran con un diálogo milenario esculpido en piedra: antropomorfos, camelidae, felinos andinos y sorprendentes primates amazónicos (evidencia de las extensas rutas de trueque que conectaban el Altiplano con el océano Pacífico). Los petroglifos no son simples dibujos, sino un lenguaje ritual que señalaba rutas, marcaba aguadas y transmitía cosmovisión. El sol de la mañana raspa las superficies oxidadas, resaltando las figuras con luz dorada mientras el silencio del desierto permite escuchar los pasos de las caravanas que hace mil años transitaban hacia el oasis. Es una conexión tangible con la cultura atacameña, donde cada grabado es un mensaje de supervivencia y espiritualidad en el desierto más árido del mundo."}], "pt": [{"type": "paragraph", "content": "Iniciamos o dia com café da manhã entre montanhas que despertam com a primeira luz do sol. O caminho nos conduz ao Vale do Arco-Íris, onde os minerais pintam a terra com tons intensos, formando uma das paisagens mais surpreendentes da Cordilheira de Domeyko."}, {"type": "heading", "content": "Vale do Arco-Íris: Paleta Mineral da Cordilheira de Domeyko"}, {"type": "paragraph", "content": "Localizado a 90 quilômetros de San Pedro de Atacama e a 3.500 metros de altitude, este vale geológico é um mausoléu de cor e tempo. Seu espectro cromático não é mera decoração, mas a autobiografia de 90 milhões de anos de história. As formações rochosas pertencem à Formação Purilactis (Cretáceo Superior) e Formação Tonel, compostas por siltitos e arenitos finos que foram depositados em antigos sistemas fluvio-lacustres. Os intensos tons avermelhados provêm de óxidos de ferro (hematita) formados em ambientes oxidantes; os verdes, de clorita e epidota geradas por alteração hidrotermal de intrusivos hipabissais; os amarelos e ocre, de sulfetos de ferro e sais evaporíticos; enquanto os brancos são gipsita e halita, vestígios de antigas lagoas salinas que brilham como neve sob o sol desértico."}, {"type": "paragraph", "content": "A experiência é uma imersão em uma paleta viva: caminhar entre estas formações erodidas pelo vento e pela água durante milhões de anos permite tocar camadas sedimentares que narram o soerguimento dos Andes. A manhã, com a luz baixa, cria jogos de sombras que realçam cada estrato, oferecendo momentos fotográficos únicos onde os minerais parecem acender-se desde dentro. É um laboratório natural onde a geologia se faz arte e cada tom é um capítulo da evolução tectônica da Puna de Atacama."}, {"type": "heading", "content": "Hierbas Buenas: Anfiteatro de Pedra e Memória"}, {"type": "paragraph", "content": "Este sítio arqueológico, localizado a 65 km ao norte de San Pedro na confluência da Cordilheira de Domeyko, a Cordilheira do Sal e os Andes (3.050 m acima do nível do mar), é o maior centro de arte rupestre de toda a zona arqueológica de San Pedro de Atacama. Em seu 'anfiteatro natural' de rochas vulcânicas macias e processos de dobramento únicos, conservam-se mais de 1.000 petróglifos que representam quatro tradições culturais principais: a naturalista de Taira, a tradição Angostura, o Estilo 2 de influência Aguada e estilos Incas, com datações que abrangem desde o Formativo Inicial até o período Colonial."}, {"type": "paragraph", "content": "Os visitantes se deparam com um diálogo milenar esculpido em pedra: antropomorfos, camelídeos, felinos andinos e surpreendentes primatas amazônicos (evidência das extensas rotas de troca que conectavam o Altiplano com o oceano Pacífico). Os petróglifos não são simples desenhos, mas uma linguagem ritual que sinalizava rotas, marcava fontes de água e transmitia cosmovisão. O sol da manhã raspa as superfícies oxidadas, destacando as figuras com luz dourada enquanto o silêncio do deserto permite ouvir os passos das caravanas que há mil anos transitavam rumo ao oásis. É uma conexão tangível com a cultura atacamenha, onde cada gravura é uma mensagem de sobrevivência e espiritualidade no deserto mais árido do mundo."}]}	{"en": [{"time": "08:00 – 08:30", "description": "Departure from San Pedro de Atacama and presentation of the tour."}, {"time": "08:30 – 10:00", "description": "Visit to Rainbow Valley: viewpoints, geological explanation and photography."}, {"time": "10:00 – 11:00", "description": "Transfer to Hierbas Buenas with landscape interpretation and local history."}, {"time": "11:00 – 12:00", "description": "Guided tour of Hierbas Buenas: rock art, cosmovision and tour closure."}], "es": [{"time": "08:00 – 08:30", "description": "Salida desde San Pedro de Atacama y presentación del recorrido."}, {"time": "08:30 – 10:00", "description": "Visita al Valle del Arcoíris: miradores, explicación geológica y fotografía."}, {"time": "10:00 – 11:00", "description": "Traslado hacia Hierbas Buenas con lectura del paisaje e historia local."}, {"time": "11:00 – 12:00", "description": "Recorrido guiado por Hierbas Buenas: arte rupestre, cosmovisión y cierre del tour."}], "pt": [{"time": "08:00 – 08:30", "description": "Saída de San Pedro de Atacama e apresentação do roteiro."}, {"time": "08:30 – 10:00", "description": "Visita ao Vale do Arco-Íris: mirantes, explicação geológica e fotografia."}, {"time": "10:00 – 11:00", "description": "Translado para Hierbas Buenas com leitura da paisagem e história local."}, {"time": "11:00 – 12:00", "description": "Percurso guiado por Hierbas Buenas: arte rupestre, cosmovisão e encerramento do tour."}]}	\N	\N	f	f	f	\N	tour-valle-del-arcoiris-y-hierbas-buenas	\N	REGULAR	40000.0000	15	4	08:00:00	f	\N	PUBLISHED	\N	2026-01-01 22:20:28.772355+00	\N
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.users (id, email, password_hash, full_name, nationality, phone_number, date_of_birth, role, auth_provider, provider_id, email_verified, created_at, updated_at, deleted_at) FROM stdin;
83dc1caa-071f-4721-95b7-910e9b7cbe97	alex@northernchile.com	$2a$10$Ndjwjk2JkM2bkVR9VV4z7uiiLXOOZKKGxk4agwkZO7RoI76MX.xgm	alex	\N	\N	\N	ROLE_SUPER_ADMIN	LOCAL	\N	f	2025-11-16 22:38:29.862596+00	2025-11-16 22:38:29.862646+00	\N
7dc41a3e-d63f-456f-8ad8-3fcdd8a310b8	david@northernchile.com	$2a$10$drmJvOSF63h./LBEp.i0wupEDHCLxv8pU5o6wQVllrMj.EWxC9YLG	david	\N	\N	\N	ROLE_PARTNER_ADMIN	LOCAL	\N	f	2025-11-16 22:38:30.192478+00	2025-11-16 22:38:30.192523+00	\N
f6dd03b6-e6d3-4cf1-866f-bc582dc9f301	diego@northernchile.com	$2a$10$0cnSNsPE3AbDfNgTzOTssevTwEHtf/7T5wh72FlDXM.qaHjgIRvX6	diego	\N	\N	\N	ROLE_SUPER_ADMIN	LOCAL	\N	f	2025-11-16 22:38:30.486614+00	2025-11-16 22:38:30.486625+00	\N
27d8b011-554a-48bd-b3b5-be0805d2bd23	diego.alvarez.e@ug.uchile.cl	$2a$10$PNvYmcciFlB3St0.J054ne6Vl42zQmiUDQb0LzKFzeqFy10PzcTwO	Diego test user	CL	+56942877633	\N	ROLE_CLIENT	LOCAL	\N	f	2025-11-28 21:21:58.825142+00	2025-11-28 23:03:16.666166+00	\N
6766affb-d0eb-4a54-83e3-4195ef7824b3	simagico@gmail.com	$2a$10$1MQy44r8ywcPXk56kdzAA.kf941yhCbQZ7hKp9KtHesL6MQrq/Lma	Diego Alvarez	CL	+56942877633	\N	ROLE_CLIENT	LOCAL	\N	f	2025-11-29 00:30:31.786949+00	2025-11-29 00:30:31.786968+00	\N
a762c59a-8a68-42de-8c69-9b14b2871696	cbarrazamorgado@gmail.com	$2a$10$A7aCuMmj.1YPOVGY0GGAL.tuMtDVteeIWXMUPzSwRsqFZsSjwk9ty	Carolina Barraza	CL	+5691389559	\N	ROLE_CLIENT	LOCAL	\N	f	2025-11-30 00:18:24.521975+00	2025-11-30 00:18:24.521995+00	\N
032d759e-62e4-4f77-9a4c-958da92a5aa4	vodisdsdfsdf@northernchile.com	$2a$10$vUc4zrZIzyEJwz5gXLsCq.XWIKG/aY0u8gWeQE.r2R/ANXNukoEEa	cosito	CL	+56912877633	\N	ROLE_CLIENT	LOCAL	\N	f	2025-12-09 14:56:48.259175+00	2025-12-09 14:56:48.259189+00	\N
32c62332-1bda-4f50-af00-211324b5df8e	yihira3681@icousd.com	$2a$10$O31YgSvCCLGUSIS7umZ4fOfIERP2.hfjgnvjI7VVlr7Fp7OO5PdJy	Usuariopruebadiego	CL	+56942877633	\N	ROLE_CLIENT	LOCAL	\N	f	2026-01-06 16:56:52.302005+00	2026-01-06 16:56:52.30202+00	\N
ff9d43b9-a699-4e24-833d-3e90b9fcf7ea	davenac878@cameltok.com	$2a$10$JWoQk1mahfMwiGaEhshCPO651UhQmIRtXAU/TSgfIiRen7Us.yen6	usuariotest	CL	+56942877633	\N	ROLE_CLIENT	LOCAL	\N	f	2026-01-06 20:04:48.875353+00	2026-01-06 20:04:48.875365+00	\N
9a96969f-c381-4c20-a8ac-91591d905e05	tedaca6099@emaxasp.com	$2a$10$Wf3358bFQrwZAr6V2Z4m3.zXWFzrURHjm3FRBSKIW2uCBRo3c0gze	testuserdos	CL	+565692877633	\N	ROLE_CLIENT	LOCAL	\N	f	2026-01-06 22:49:33.918347+00	2026-01-06 22:49:33.918364+00	\N
855ef29d-4275-4015-9fab-8d78bdea5fe2	contacto@northernchile.com	$2a$10$3uzgYY6G/.NI3MJ.AXnS0uOaP0yNPAmCzJ0I51bVHXFxsXRa.Uuwm	Jeanette De izquierdo	CL	\N	\N	ROLE_SUPER_ADMIN	LOCAL	\N	f	2025-11-26 20:15:28.542427+00	2026-01-07 00:33:29.627829+00	\N
\.


--
-- Data for Name: weather_alerts; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.weather_alerts (id, tour_schedule_id, alert_type, severity, status, message, wind_speed, cloud_coverage, moon_phase, resolution, resolved_by, created_at, resolved_at) FROM stdin;
\.


--
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- Name: bookings bookings_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT bookings_pkey PRIMARY KEY (id);


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- Name: carts carts_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT carts_pkey PRIMARY KEY (id);


--
-- Name: contact_messages contact_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.contact_messages
    ADD CONSTRAINT contact_messages_pkey PRIMARY KEY (id);


--
-- Name: email_verification_tokens email_verification_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT email_verification_tokens_pkey PRIMARY KEY (id);


--
-- Name: email_verification_tokens email_verification_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT email_verification_tokens_token_key UNIQUE (token);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: media media_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.media
    ADD CONSTRAINT media_pkey PRIMARY KEY (id);


--
-- Name: media media_s3_key_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.media
    ADD CONSTRAINT media_s3_key_key UNIQUE (s3_key);


--
-- Name: participants participants_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT participants_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);


--
-- Name: password_reset_tokens password_reset_tokens_token_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_token_key UNIQUE (token);


--
-- Name: payment_sessions payment_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.payment_sessions
    ADD CONSTRAINT payment_sessions_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: private_tour_requests private_tour_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.private_tour_requests
    ADD CONSTRAINT private_tour_requests_pkey PRIMARY KEY (id);


--
-- Name: tour_schedules tour_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tour_schedules
    ADD CONSTRAINT tour_schedules_pkey PRIMARY KEY (id);


--
-- Name: tours tours_content_key_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_content_key_key UNIQUE (content_key);


--
-- Name: tours tours_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_pkey PRIMARY KEY (id);


--
-- Name: tours tours_slug_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT tours_slug_key UNIQUE (slug);


--
-- Name: tour_schedules uk_tour_schedule_datetime; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tour_schedules
    ADD CONSTRAINT uk_tour_schedule_datetime UNIQUE (tour_id, start_datetime);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: weather_alerts weather_alerts_pkey; Type: CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.weather_alerts
    ADD CONSTRAINT weather_alerts_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_audit_logs_action; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_audit_logs_action ON public.audit_logs USING btree (action);


--
-- Name: idx_audit_logs_created_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_audit_logs_created_at ON public.audit_logs USING btree (created_at);


--
-- Name: idx_audit_logs_entity_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_audit_logs_entity_id ON public.audit_logs USING btree (entity_id);


--
-- Name: idx_audit_logs_entity_type; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_audit_logs_entity_type ON public.audit_logs USING btree (entity_type);


--
-- Name: idx_audit_logs_user_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_audit_logs_user_id ON public.audit_logs USING btree (user_id);


--
-- Name: idx_bookings_created_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_created_at ON public.bookings USING btree (created_at DESC);


--
-- Name: idx_bookings_reminder_sent_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_reminder_sent_at ON public.bookings USING btree (reminder_sent_at) WHERE (reminder_sent_at IS NULL);


--
-- Name: idx_bookings_schedule_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_schedule_id ON public.bookings USING btree (schedule_id);


--
-- Name: idx_bookings_schedule_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_schedule_status ON public.bookings USING btree (schedule_id, status);


--
-- Name: idx_bookings_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_status ON public.bookings USING btree (status);


--
-- Name: idx_bookings_tour_date; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_tour_date ON public.bookings USING btree (tour_date);


--
-- Name: idx_bookings_user_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_bookings_user_id ON public.bookings USING btree (user_id);


--
-- Name: idx_cart_items_cart_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_cart_items_cart_id ON public.cart_items USING btree (cart_id);


--
-- Name: idx_cart_items_schedule_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_cart_items_schedule_id ON public.cart_items USING btree (schedule_id);


--
-- Name: idx_carts_expires_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_carts_expires_at ON public.carts USING btree (expires_at);


--
-- Name: idx_carts_user_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_carts_user_id ON public.carts USING btree (user_id);


--
-- Name: idx_contact_messages_created_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_contact_messages_created_at ON public.contact_messages USING btree (created_at DESC);


--
-- Name: idx_contact_messages_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_contact_messages_status ON public.contact_messages USING btree (status);


--
-- Name: idx_email_verification_tokens_user_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_email_verification_tokens_user_id ON public.email_verification_tokens USING btree (user_id);


--
-- Name: idx_media_display_order; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_display_order ON public.media USING btree (tour_id, display_order) WHERE (tour_id IS NOT NULL);


--
-- Name: idx_media_hero; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE UNIQUE INDEX idx_media_hero ON public.media USING btree (tour_id) WHERE (is_hero = true);


--
-- Name: idx_media_owner; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_owner ON public.media USING btree (owner_id);


--
-- Name: idx_media_schedule; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_schedule ON public.media USING btree (schedule_id) WHERE (schedule_id IS NOT NULL);


--
-- Name: idx_media_schedule_display_order; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_schedule_display_order ON public.media USING btree (schedule_id, display_order) WHERE (schedule_id IS NOT NULL);


--
-- Name: idx_media_tags; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_tags ON public.media USING gin (tags);


--
-- Name: idx_media_taken_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_taken_at ON public.media USING btree (taken_at) WHERE (taken_at IS NOT NULL);


--
-- Name: idx_media_tour; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_tour ON public.media USING btree (tour_id) WHERE (tour_id IS NOT NULL);


--
-- Name: idx_media_uploaded_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_media_uploaded_at ON public.media USING btree (uploaded_at DESC);


--
-- Name: idx_participants_booking_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_participants_booking_id ON public.participants USING btree (booking_id);


--
-- Name: idx_password_reset_tokens_user_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_password_reset_tokens_user_id ON public.password_reset_tokens USING btree (user_id);


--
-- Name: idx_payment_booking; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_booking ON public.payments USING btree (booking_id);


--
-- Name: idx_payment_external_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_external_id ON public.payments USING btree (external_payment_id);


--
-- Name: idx_payment_idempotency; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE UNIQUE INDEX idx_payment_idempotency ON public.payments USING btree (idempotency_key) WHERE (idempotency_key IS NOT NULL);


--
-- Name: idx_payment_session_expires; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_session_expires ON public.payment_sessions USING btree (expires_at);


--
-- Name: idx_payment_session_external_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_session_external_id ON public.payment_sessions USING btree (external_payment_id);


--
-- Name: idx_payment_session_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_session_id ON public.payments USING btree (payment_session_id);


--
-- Name: idx_payment_session_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_session_status ON public.payment_sessions USING btree (status);


--
-- Name: idx_payment_session_token; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_session_token ON public.payment_sessions USING btree (token);


--
-- Name: idx_payment_session_user; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_session_user ON public.payment_sessions USING btree (user_id);


--
-- Name: idx_payment_sessions_status_expires; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_sessions_status_expires ON public.payment_sessions USING btree (status, expires_at);


--
-- Name: idx_payment_sessions_token; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_sessions_token ON public.payment_sessions USING btree (token);


--
-- Name: idx_payment_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payment_status ON public.payments USING btree (status);


--
-- Name: idx_payments_is_test; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_payments_is_test ON public.payments USING btree (is_test);


--
-- Name: idx_private_tour_requests_created_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_private_tour_requests_created_at ON public.private_tour_requests USING btree (created_at DESC);


--
-- Name: idx_private_tour_requests_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_private_tour_requests_status ON public.private_tour_requests USING btree (status);


--
-- Name: idx_private_tour_requests_user_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_private_tour_requests_user_id ON public.private_tour_requests USING btree (user_id);


--
-- Name: idx_tour_schedules_start_datetime; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tour_schedules_start_datetime ON public.tour_schedules USING btree (start_datetime);


--
-- Name: idx_tour_schedules_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tour_schedules_status ON public.tour_schedules USING btree (status);


--
-- Name: idx_tour_schedules_status_start; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tour_schedules_status_start ON public.tour_schedules USING btree (status, start_datetime);


--
-- Name: idx_tour_schedules_tour_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tour_schedules_tour_id ON public.tour_schedules USING btree (tour_id);


--
-- Name: idx_tours_category; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tours_category ON public.tours USING btree (category);


--
-- Name: idx_tours_deleted_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tours_deleted_at ON public.tours USING btree (deleted_at);


--
-- Name: idx_tours_owner_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tours_owner_id ON public.tours USING btree (owner_id);


--
-- Name: idx_tours_slug; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tours_slug ON public.tours USING btree (slug);


--
-- Name: idx_tours_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_tours_status ON public.tours USING btree (status);


--
-- Name: idx_users_deleted_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_users_deleted_at ON public.users USING btree (deleted_at);


--
-- Name: idx_weather_alerts_created_at; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_weather_alerts_created_at ON public.weather_alerts USING btree (created_at DESC);


--
-- Name: idx_weather_alerts_schedule_id; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_weather_alerts_schedule_id ON public.weather_alerts USING btree (tour_schedule_id);


--
-- Name: idx_weather_alerts_status; Type: INDEX; Schema: public; Owner: neondb_owner
--

CREATE INDEX idx_weather_alerts_status ON public.weather_alerts USING btree (status);


--
-- Name: audit_logs fk_audit_logs_user; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: bookings fk_bookings_schedule; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT fk_bookings_schedule FOREIGN KEY (schedule_id) REFERENCES public.tour_schedules(id);


--
-- Name: bookings fk_bookings_user; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.bookings
    ADD CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: cart_items fk_cart_items_cart; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES public.carts(id) ON DELETE CASCADE;


--
-- Name: cart_items fk_cart_items_schedule; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fk_cart_items_schedule FOREIGN KEY (schedule_id) REFERENCES public.tour_schedules(id);


--
-- Name: carts fk_carts_user; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: email_verification_tokens fk_email_verification_tokens_user; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: participants fk_participants_booking; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT fk_participants_booking FOREIGN KEY (booking_id) REFERENCES public.bookings(id) ON DELETE CASCADE;


--
-- Name: password_reset_tokens fk_password_reset_tokens_user; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: payments fk_payments_booking; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES public.bookings(id);


--
-- Name: private_tour_requests fk_private_tour_requests_user; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.private_tour_requests
    ADD CONSTRAINT fk_private_tour_requests_user FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: tour_schedules fk_tour_schedules_guide; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tour_schedules
    ADD CONSTRAINT fk_tour_schedules_guide FOREIGN KEY (assigned_guide_id) REFERENCES public.users(id);


--
-- Name: tour_schedules fk_tour_schedules_tour; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tour_schedules
    ADD CONSTRAINT fk_tour_schedules_tour FOREIGN KEY (tour_id) REFERENCES public.tours(id);


--
-- Name: tours fk_tours_owner; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.tours
    ADD CONSTRAINT fk_tours_owner FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: weather_alerts fk_weather_alerts_schedule; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.weather_alerts
    ADD CONSTRAINT fk_weather_alerts_schedule FOREIGN KEY (tour_schedule_id) REFERENCES public.tour_schedules(id) ON DELETE CASCADE;


--
-- Name: media media_owner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.media
    ADD CONSTRAINT media_owner_id_fkey FOREIGN KEY (owner_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: media media_schedule_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.media
    ADD CONSTRAINT media_schedule_id_fkey FOREIGN KEY (schedule_id) REFERENCES public.tour_schedules(id) ON DELETE CASCADE;


--
-- Name: media media_tour_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.media
    ADD CONSTRAINT media_tour_id_fkey FOREIGN KEY (tour_id) REFERENCES public.tours(id) ON DELETE CASCADE;


--
-- Name: payment_sessions payment_sessions_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.payment_sessions
    ADD CONSTRAINT payment_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: payments payments_payment_session_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: neondb_owner
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_payment_session_id_fkey FOREIGN KEY (payment_session_id) REFERENCES public.payment_sessions(id);


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: public; Owner: cloud_admin
--

ALTER DEFAULT PRIVILEGES FOR ROLE cloud_admin IN SCHEMA public GRANT ALL ON SEQUENCES TO neon_superuser WITH GRANT OPTION;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: public; Owner: cloud_admin
--

ALTER DEFAULT PRIVILEGES FOR ROLE cloud_admin IN SCHEMA public GRANT ALL ON TABLES TO neon_superuser WITH GRANT OPTION;


--
-- PostgreSQL database dump complete
--

\unrestrict 3UHETgPccnZ82mbX7uTcik4x6Dqge6tdzgPYd2dEsM93MH14GINcKkKQJKljQnb

