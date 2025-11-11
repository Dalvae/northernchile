-- ====================================================================================
-- SCRIPT DE SEEDING MULTILINGÜE Y COMPLETO CON CONTENIDO ESTRUCTURADO - V3 (CORREGIDO)
-- ====================================================================================
-- Inserta tours principales usando el esquema actual de "tours".
-- Campos relevantes (\d+ tours):
--  id, owner_id, name_translations,
--  wind_sensitive, moon_sensitive, cloud_sensitive,
--  content_key, slug, category, price,
--  default_max_participants, duration_hours, default_start_time,
--  recurring, recurrence_rule, status,
--  guide_name,
--  itinerary_translations, equipment_translations, additional_info_translations,
--  description_blocks_translations
-- ====================================================================================

DO $$
DECLARE
    admin_user_id UUID;
BEGIN
    -- PASO 1: ENCONTRAR EL ID DEL USUARIO ADMINISTRADOR
    RAISE NOTICE 'Buscando ID para el usuario administrador...';
    SELECT id INTO admin_user_id
    FROM users
    WHERE email = 'diego@example.com'
      AND role = 'ROLE_SUPER_ADMIN'
    LIMIT 1;

    IF admin_user_id IS NULL THEN
        RAISE EXCEPTION 'Usuario SUPER_ADMIN con email ''admin@northernchile.cl'' no encontrado. Por favor, crea el usuario primero o actualiza el email en este script.';
    END IF;
    RAISE NOTICE 'Usuario administrador encontrado con ID: %', admin_user_id;

    -- ====================================================================================
    -- TOUR 1: ASTRONÓMICO (RECURRENTE DIARIO 21:00)
    -- ====================================================================================

    RAISE NOTICE 'Insertando Tour Astronómico con contenido completo en 3 idiomas...';

    INSERT INTO tours (
        id,
        owner_id,
        name_translations,
        description_blocks_translations,
        category,
        price,
        default_max_participants,
        duration_hours,
        default_start_time,
        recurring,
        recurrence_rule,
        status,
        slug,
        moon_sensitive,
        wind_sensitive,
        cloud_sensitive,
        itinerary_translations,
        equipment_translations,
        additional_info_translations
    ) VALUES (
        gen_random_uuid(),
        admin_user_id,
        '{
            "es": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina",
            "en": "Astronomical Tour: Archaeoastronomy and Andean Cosmovision",
            "pt": "Tour Astronômico: Arqueoastronomia e Cosmovisão Andina"
        }'::jsonb,
        '{
            "es": [
                {"type": "paragraph", "content": "Una experiencia bajo los cielos más puros del planeta. En el corazón del desierto de Atacama, donde el silencio es tan profundo como el cielo, te invitamos a vivir una conexión real con el universo."},
                {"type": "paragraph", "content": "En Northern Chile Astronomy, combinamos ciencia, historia y emoción para ofrecerte una experiencia que trasciende la simple observación astronómica: aquí el cielo se entiende, se siente y se comparte."},
                {"type": "heading", "content": "Astronomía con alma:"},
                {"type": "paragraph", "content": "No solo mirarás estrellas: aprenderás a leerlas. Nuestros guías, expertos en astronomía y cosmovisión andina, revelan los secretos del cosmos y cómo los antiguos pueblos de los Andes lo interpretaron."},
                {"type": "heading", "content": "Recuerdos del universo:"},
                {"type": "paragraph", "content": "Cada visitante recibe astrofotografías profesionales: retratos personales bajo las estrellas y capturas reales del espacio profundo."},
                {"type": "heading", "content": "Cóctel bajo las estrellas:"},
                {"type": "paragraph", "content": "Entre una charla y una nebulosa, disfruta de vino, pisco o bebidas calientes según la temporada."},
                {"type": "heading", "content": "Comodidad y seguridad:"},
                {"type": "paragraph", "content": "Incluye traslado desde y hacia tu hotel en San Pedro."}
            ],
            "en": [
                {"type": "paragraph", "content": "An experience under the purest skies on the planet."},
                {"type": "paragraph", "content": "We combine science, history, and emotion to offer an experience that transcends simple astronomical observation."},
                {"type": "heading", "content": "Astronomy with soul:"},
                {"type": "paragraph", "content": "Learn to read the stars with expert guides in astronomy and Andean cosmovision."}
            ],
            "pt": [
                {"type": "paragraph", "content": "Uma experiência sob os céus mais puros do planeta."},
                {"type": "paragraph", "content": "Combinamos ciência, história e emoção para oferecer uma experiência que transcende a simples observação astronômica."},
                {"type": "heading", "content": "Astronomia com alma:"},
                {"type": "paragraph", "content": "Aprenda a ler as estrelas com guias especialistas em astronomia e cosmovisão andina."}
            ]
        }'::jsonb,
        'ASTRONOMICAL',
        15000,
        15,
        3,
        '21:00',
        TRUE,
        '0 21 * * *',
        'PUBLISHED',
        'tour-astronomico-arqueoastronomia-y-cosmovision-andina',
        TRUE,
        TRUE,
        TRUE,
        '{
            "es": [
                {"time": "21:00", "description": "Recogida en tu alojamiento o punto de encuentro."},
                {"time": "21:30", "description": "Charla astronómica e introducción."},
                {"time": "21:45", "description": "Observación con telescopios y astrofotografía."},
                {"time": "23:30", "description": "Retorno a los hoteles."}
            ],
            "en": [
                {"time": "21:00", "description": "Pickup at your accommodation or meeting point."},
                {"time": "21:30", "description": "Astronomical introduction."},
                {"time": "21:45", "description": "Telescope observation and astrophotography."},
                {"time": "23:30", "description": "Return to hotels."}
            ],
            "pt": [
                {"time": "21:00", "description": "Recolha no alojamento ou ponto de encontro."},
                {"time": "21:30", "description": "Introdução astronômica."},
                {"time": "21:45", "description": "Observação com telescópios e astrofotografia."},
                {"time": "23:30", "description": "Retorno aos hotéis."}
            ]
        }'::jsonb,
        '{
            "es": ["Celestron NexStar 8SE", "Telescopios adicionales"],
            "en": ["Celestron NexStar 8SE", "Additional telescopes"],
            "pt": ["Celestron NexStar 8SE", "Telescópios adicionais"]
        }'::jsonb,
        '{
            "es": ["Lleva ropa abrigada."],
            "en": ["Bring warm clothes."],
            "pt": ["Leve roupas quentes."]
        }'::jsonb
    ) ON CONFLICT (slug) DO NOTHING;

    -- ====================================================================================
    -- TOUR 2: LAGUNAS ESCONDIDAS DE BALTINACHE & VALLECITO (NO RECURRENTE, EJEMPLO)
    -- ====================================================================================

    RAISE NOTICE 'Insertando Tour Lagunas Escondidas de Baltinache & Vallecito con contenido completo...';

    INSERT INTO tours (
        id,
        owner_id,
        name_translations,
        description_blocks_translations,
        category,
        price,
        default_max_participants,
        duration_hours,
        default_start_time,
        recurring,
        recurrence_rule,
        status,
        slug,
        moon_sensitive,
        wind_sensitive,
        cloud_sensitive,
        itinerary_translations,
        additional_info_translations
    ) VALUES (
        gen_random_uuid(),
        admin_user_id,
        '{
            "es": "Tour Lagunas Escondidas de Baltinache & Vallecito",
            "en": "Hidden Lagoons of Baltinache & Vallecito Tour",
            "pt": "Tour Lagoas Escondidas de Baltinache & Vallecito"
        }'::jsonb,
        '{
            "es": [
                {"type": "paragraph", "content": "Observa espejos de sal, dunas doradas y geología viva en una ruta íntima al amanecer."}
            ]
        }'::jsonb,
        'REGULAR',
        25000,
        15,
        5,
        NULL,
        FALSE,
        NULL,
        'PUBLISHED',
        'tour-lagunas-escondidas-de-baltinache-y-vallecito',
        FALSE,
        FALSE,
        FALSE,
        '{
            "es": [
                {"time": "07:30", "description": "Salida desde San Pedro de Atacama."}
            ]
        }'::jsonb,
        '{
            "es": ["El Llano de la Paciencia es una subcuenca geológica del Salar de Atacama."]
        }'::jsonb
    ) ON CONFLICT (slug) DO NOTHING;

    -- ====================================================================================
    -- TOUR 3: VALLE DEL ARCOÍRIS & HIERBAS BUENAS (NO RECURRENTE, EJEMPLO)
    -- ====================================================================================

    RAISE NOTICE 'Insertando Tour Valle del Arcoíris & Hierbas Buenas con contenido completo...';

    INSERT INTO tours (
        id,
        owner_id,
        name_translations,
        description_blocks_translations,
        category,
        price,
        default_max_participants,
        duration_hours,
        default_start_time,
        recurring,
        recurrence_rule,
        status,
        slug,
        moon_sensitive,
        wind_sensitive,
        cloud_sensitive,
        itinerary_translations
    ) VALUES (
        gen_random_uuid(),
        admin_user_id,
        '{
            "es": "Tour Valle del Arcoíris & Hierbas Buenas",
            "en": "Rainbow Valley & Hierbas Buenas Tour",
            "pt": "Tour Vale do Arco-Íris & Hierbas Buenas"
        }'::jsonb,
        '{
            "es": [
                {"type": "paragraph", "content": "Iniciamos el día con un desayuno entre montañas."}
            ]
        }'::jsonb,
        'REGULAR',
        22000,
        15,
        4,
        NULL,
        FALSE,
        NULL,
        'PUBLISHED',
        'tour-valle-del-arcoiris-y-hierbas-buenas',
        FALSE,
        FALSE,
        FALSE,
        '{
            "es": [
                {"time": "08:00", "description": "Salida desde San Pedro de Atacama."}
            ]
        }'::jsonb
    ) ON CONFLICT (slug) DO NOTHING;

    RAISE NOTICE 'Seeding de tours con contenido estructurado completado.';

END $$;
