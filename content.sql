-- ====================================================================================
-- SCRIPT DE SEEDING DEFINITIVO - CONTENIDO COMPLETO Y TRADUCCIONES DETALLADAS
-- ====================================================================================
-- Propósito: Inserta los 3 tours principales con contenido completo y traducciones
--            fieles al contenido original, sin resúmenes.
-- ====================================================================================

DO $$
DECLARE
    admin_user_id UUID;
    admin_user_email VARCHAR := 'diego@example.com';
BEGIN
    -- --- PASO 1: ENCONTRAR EL ID DEL USUARIO ADMINISTRADOR ---
    RAISE NOTICE 'Buscando ID para el usuario administrador: %', admin_user_email;
    SELECT id INTO admin_user_id FROM users WHERE email = admin_user_email AND role = 'ROLE_SUPER_ADMIN' LIMIT 1;

    IF admin_user_id IS NULL THEN
        RAISE EXCEPTION 'Usuario SUPER_ADMIN con email ''%'' no encontrado. Por favor, crea el usuario primero o actualiza el email en este script.', admin_user_email;
    END IF;
    RAISE NOTICE 'Usuario administrador encontrado con ID: %', admin_user_id;

    -- ====================================================================================
    -- TOUR 1: ASTRONÓMICO - CONTENIDO COMPLETO
    -- ====================================================================================

    RAISE NOTICE 'Insertando Tour Astronómico con contenido completo...';
    INSERT INTO tours (
        id, owner_id, name_translations, description_blocks_translations, category, price,
        duration_hours, recurring, recurrence_rule, status, slug,
        moon_sensitive, wind_sensitive, cloud_sensitive,
        itinerary_translations, equipment_translations, additional_info_translations, 
        default_max_participants, default_start_time
    ) VALUES (
        gen_random_uuid(), admin_user_id,
        '{
            "es": "Tour Astronómico: Arqueoastronomía y Cosmovisión Andina",
            "en": "Astronomical Tour: Archaeoastronomy and Andean Cosmovision",
            "pt": "Tour Astronômico: Arqueoastronomia e Cosmovisão Andina"
        }'::jsonb,
        '{
            "es": [
                {"type": "paragraph", "content": "Una experiencia bajo los cielos más puros del planeta. En el corazón del desierto de Atacama, donde el silencio es tan profundo como el cielo, te invitamos a vivir una conexión real con el universo."},
                {"type": "paragraph", "content": "En Northern Chile Astronomy, combinamos ciencia, historia y emoción para ofrecerte una experiencia que trasciende la simple observación astronómica: aquí el cielo se entiende, se siente y se comparte."},
                {"type": "heading", "content": "🔭 Astronomía con alma:"},
                {"type": "paragraph", "content": "No solo mirarás estrellas: aprenderás a leerlas. Nuestros guías, expertos en astronomía y cosmovisión andina, revelan los secretos del cosmos y cómo los antiguos pueblos de los Andes lo interpretaron."},
                {"type": "heading", "content": "📸 Recuerdos del universo:"},
                {"type": "paragraph", "content": "Cada visitante recibe astrofotografías profesionales: retratos personales bajo las estrellas y capturas reales del espacio profundo. Imágenes únicas, tomadas con telescopios y cámaras de alta gama."},
                {"type": "heading", "content": "🥂 Cóctel bajo las estrellas:"},
                {"type": "paragraph", "content": "Entre una charla y una nebulosa, disfruta de vino, pisco o bebidas calientes según la temporada, acompañadas de snacks y buena compañía."},
                {"type": "heading", "content": "🚐 Comodidad y seguridad:"},
                {"type": "paragraph", "content": "Incluye traslado desde y hacia tu hotel en San Pedro (o punto de encuentro coordinado). Viajamos en vehículos seguros y cómodos, pensados para el entorno del desierto."},
                {"type": "heading", "content": "🌄 Un escenario natural incomparable"},
                {"type": "paragraph", "content": "Nuestro observatorio se encuentra sobre las dunas, lejos de las luces del pueblo, en un entorno puro y silencioso. Aquí, la oscuridad es tu aliada y el cielo tu espejo. Cada noche es distinta, cada visitante, único."},
                {"type": "heading", "content": "🌠 Una noche que no olvidarás"},
                {"type": "paragraph", "content": "Más que un tour, es una invitación a sentirte parte del universo. En Northern Chile Astronomy, el cielo no se observa… se vive."}
            ],
            "en": [
                {"type": "paragraph", "content": "An experience under the purest skies on the planet. In the heart of the Atacama Desert, where silence is as profound as the sky, we invite you to experience a real connection with the universe."},
                {"type": "paragraph", "content": "At Northern Chile Astronomy, we combine science, history, and emotion to offer you an experience that transcends simple astronomical observation: here the sky is understood, felt, and shared."},
                {"type": "heading", "content": "🔭 Astronomy with soul:"},
                {"type": "paragraph", "content": "You will not just look at stars: you will learn to read them. Our guides, experts in astronomy and Andean cosmovision, reveal the secrets of the cosmos and how ancient Andean peoples interpreted it."},
                {"type": "heading", "content": "📸 Memories of the universe:"},
                {"type": "paragraph", "content": "Each visitor receives professional astrophotography: personal portraits under the stars and real deep space captures. Unique images, taken with high-end telescopes and cameras."},
                {"type": "heading", "content": "🥂 Cocktail under the stars:"},
                {"type": "paragraph", "content": "Between one talk and a nebula, enjoy wine, pisco, or hot drinks depending on the season, accompanied by snacks and good company."},
                {"type": "heading", "content": "🚐 Comfort and safety:"},
                {"type": "paragraph", "content": "Includes transportation from and to your hotel in San Pedro (or coordinated meeting point). We travel in safe and comfortable vehicles, designed for the desert environment."},
                {"type": "heading", "content": "🌄 An incomparable natural setting"},
                {"type": "paragraph", "content": "Our observatory is located on the dunes, far from town lights, in a pure and silent environment. Here, darkness is your ally and the sky your mirror. Every night is different, every visitor unique."},
                {"type": "heading", "content": "🌠 A night you will never forget"},
                {"type": "paragraph", "content": "More than a tour, it is an invitation to feel part of the universe. At Northern Chile Astronomy, the sky is not observed... it is lived."}
            ],
            "pt": [
                {"type": "paragraph", "content": "Uma experiência sob os céus mais puros do planeta. No coração do Deserto do Atacama, onde o silêncio é tão profundo quanto o céu, convidamos você a viver uma conexão real com o universo."},
                {"type": "paragraph", "content": "Na Northern Chile Astronomy, combinamos ciência, história e emoção para oferecer uma experiência que transcende a simples observação astronômica: aqui o céu é compreendido, sentido e compartilhado."},
                {"type": "heading", "content": "🔭 Astronomia com alma:"},
                {"type": "paragraph", "content": "Você não apenas verá estrelas: aprenderá a lê-las. Nossos guias, especialistas em astronomia e cosmovisão andina, revelam os segredos do cosmos e como os antigos povos dos Andes o interpretaram."},
                {"type": "heading", "content": "📸 Memórias do universo:"},
                {"type": "paragraph", "content": "Cada visitante recebe astrofotografias profissionais: retratos pessoais sob as estrelas e capturas reais do espaço profundo. Imagens únicas, tiradas com telescópios e câmeras de alta gama."},
                {"type": "heading", "content": "🥂 Coquetel sob as estrelas:"},
                {"type": "paragraph", "content": "Entre uma conversa e uma nebulosa, aproveite vinho, pisco ou bebidas quentes conforme a temporada, acompanhados de snacks e boa companhia."},
                {"type": "heading", "content": "🚐 Conforto e segurança:"},
                {"type": "paragraph", "content": "Inclui traslado de ida e volta do seu hotel em San Pedro (ou ponto de encontro coordenado). Viajamos em veículos seguros e confortáveis, pensados para o ambiente desértico."},
                {"type": "heading", "content": "🌄 Um cenário natural incomparável"},
                {"type": "paragraph", "content": "Nosso observatório fica sobre as dunas, longe das luzes da cidade, em um ambiente puro e silencioso. Aqui, a escuridão é sua aliada e o céu seu espelho. Cada noite é diferente, cada visitante, único."},
                {"type": "heading", "content": "🌠 Uma noite inesquecível"},
                {"type": "paragraph", "content": "Mais que um tour, é um convite para se sentir parte do universo. Na Northern Chile Astronomy, o céu não se observa... se vive."}
            ]
        }'::jsonb,
        'ASTRONOMICAL', 55000.00, 3, TRUE, '0 21 * * *', 'PUBLISHED',
        'tour-astronomico-arqueoastronomia-y-cosmovision-andina',
        TRUE, TRUE, TRUE,
        '{
            "es": [
                {"time": "21:00 – 21:30", "description": "Recogida en tu alojamiento o punto de encuentro."},
                {"time": "21:30 – 21:45", "description": "Bienvenida, presentación y charla astronómica."},
                {"time": "21:45 – 23:30", "description": "Observación con telescopios, astrofotografía y cóctel."},
                {"time": "23:45", "description": "Retorno a los hoteles (horario flexible según grupo)."}
            ],
            "en": [
                {"time": "21:00 – 21:30", "description": "Pickup at your accommodation or meeting point."},
                {"time": "21:30 – 21:45", "description": "Welcome, presentation and astronomical talk."},
                {"time": "21:45 – 23:30", "description": "Observation with telescopes, astrophotography and cocktail."},
                {"time": "23:45", "description": "Return to hotels (flexible schedule depending on group)."}
            ],
            "pt": [
                {"time": "21:00 – 21:30", "description": "Busca em seu alojamento ou ponto de encontro."},
                {"time": "21:30 – 21:45", "description": "Boas-vindas, apresentação e palestra astronômica."},
                {"time": "21:45 – 23:30", "description": "Observação com telescópios, astrofotografia e coquetel."},
                {"time": "23:45", "description": "Retorno aos hotéis (horário flexível conforme o grupo)."}
            ]
        }'::jsonb,
        '{
            "es": [
                "Celestron NexStar 8SE (203 mm)",
                "Celestron 114 mm GoTo",
                "Sky-Watcher 130 EQ",
                "SWO SEESTAR S50 Smart Telescope",
                "Cámara Canon Mark III + lente gran angular"
            ],
            "en": [
                "Celestron NexStar 8SE (203 mm)",
                "Celestron 114 mm GoTo",
                "Sky-Watcher 130 EQ",
                "SWO SEESTAR S50 Smart Telescope",
                "Canon Mark III camera + wide angle lens"
            ],
            "pt": [
                "Celestron NexStar 8SE (203 mm)",
                "Celestron 114 mm GoTo",
                "Sky-Watcher 130 EQ",
                "SWO SEESTAR S50 Smart Telescope",
                "Câmera Canon Mark III + lente grande angular"
            ]
        }'::jsonb,
        '{
            "es": [
                "Lleva ropa abrigada: el desierto es frío por la noche.",
                "Los niños son bienvenidos, siempre con supervisión de adultos."
            ],
            "en": [
                "Bring warm clothes: the desert is cold at night.",
                "Children are welcome, always with adult supervision."
            ],
            "pt": [
                "Leve roupas quentes: o deserto é frio à noite.",
                "Crianças são bem-vindas, sempre com supervisão de adultos."
            ]
        }'::jsonb,
        15, '21:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    -- ====================================================================================
    -- TOUR 2: LAGUNAS ESCONDIDAS DE BALTINACHE & VALLECITO - CONTENIDO COMPLETO
    -- ====================================================================================

    RAISE NOTICE 'Insertando Tour Lagunas Escondidas de Baltinache & Vallecito...';
    INSERT INTO tours (
        id, owner_id, name_translations, description_blocks_translations, category, price,
        duration_hours, recurring, status, slug, moon_sensitive, wind_sensitive, cloud_sensitive,
        itinerary_translations, additional_info_translations, default_max_participants, default_start_time
    ) VALUES (
        gen_random_uuid(), admin_user_id,
        '{
            "es": "Tour Lagunas Escondidas de Baltinache & Vallecito",
            "en": "Hidden Lagoons of Baltinache & Vallecito Tour",
            "pt": "Tour Lagoas Escondidas de Baltinache & Vallecito"
        }'::jsonb,
        '{
            "es": [
                {"type": "paragraph", "content": "Observa espejos de sal, dunas doradas y geología viva en una ruta íntima al amanecer."},
                {"type": "heading", "content": "🌊 Lagunas Escondidas de Baltinache"},
                {"type": "paragraph", "content": "Este complejo de siete pozas de agua salada forma un oasis surreal en medio del desierto, ubicado en el Llano de la Paciencia, al pie de la Cordillera de la Sal y la Cordillera de Domeyko. Su origen geológico se remonta al Cretácico, cuando el surgimiento de los Andes atrapó brazos marinos que, mediante evaporación y millones de años de aridez, formaron este salar con una salinidad extrema de 220 gramos por litro—casi seis veces más que el mar Muerto. Las lagunas, con solo 5 metros de diámetro promedio, son espejos naturales donde el cielo desértico se refleja en aguas turquesas y cristalinas que crean un impactante contraste con los tonos terracota y rojizos del paisaje circundante. Solo dos de las siete lagunas están habilitadas para el baño, ofreciendo una experiencia de flotación única mientras las otras cinco permanecen protegidas, conservando su belleza virgen. En la última laguna, incluso se han detectado formaciones rojizas gelatinosas compatibles con bacterias extremófilas, evidencia de vida en condiciones límites que fascina a científicos y visitantes por igual."},
                {"type": "paragraph", "content": "Los visitantes experimentan una conexión profunda con la geología viva: al sumergirse en las aguas hiper salinas, sienten en su piel la historia de un océano prehistórico, mientras el silencio del desierto amplifica la percepción de este paisaje que combina la austera majestuosidad de la Cordillera de la Sal con la delicada transparencia de las lagunas. El acceso por un camino de tierra rojiza salpicado de cactus intensifica la sensación de llegar a un lugar secreto, casi intacto desde su formación hace millones de años."},
                {"type": "heading", "content": "Vallecito & Bus Mágico"},
                {"type": "paragraph", "content": "Ubicado precisamente en el Llano de la Paciencia, Vallecito representa la parte menos concurrida pero igualmente espectacular de la Cordillera de la Sal, donde el tiempo y la naturaleza han esculpido paisajes que parecen de otro planeta. La llamativa formación del Bus Mágico Escondido surge como un ícono contemporáneo en este escenario prehistórico, creando una escena surreal que contrasta la huella humana reciente con la inmensidad del desierto. Las formaciones rocosas impresionantes de Vallecito exhiben una paleta de colores cambiantes según la luz del día, desde ocres intensos hasta violetas etéreos, revelando capas sedimentarias que narran la historia tectónica de la región."},
                {"type": "paragraph", "content": "La experiencia geológica se intensifica al caminar por las altas dunas, donde la suave arena bajo los pies transmite la energía del viento, el principal arquitecto de este paisaje. Desde las cumbres, las vistas panorámicas se extienden hasta donde alcanza la vista, mostrando texturas y patrones erosivos que solo millones de años de viento y aridez pueden crear. Al explorar los antiguos canales utilizados por habitantes prehispánicos, los visitantes conectan con el ingenio humano adaptado a este entorno extremo, completando una experiencia que une geología, historia y la fuerza primigenia del desierto de Atacama."}
            ],
            "en": [
                {"type": "paragraph", "content": "Observe salt mirrors, golden dunes and living geology on an intimate sunrise route."},
                {"type": "heading", "content": "🌊 Hidden Lagoons of Baltinache"},
                {"type": "paragraph", "content": "This complex of seven saltwater pools forms a surreal oasis in the middle of the desert, located in the Llano de la Paciencia, at the foot of the Cordillera de la Sal and the Cordillera de Domeyko. Its geological origin dates back to the Cretaceous period, when the rise of the Andes trapped marine arms that, through evaporation and millions of years of aridity, formed this salt flat with extreme salinity of 220 grams per liter—almost six times that of the Dead Sea. The lagoons, with an average diameter of only 5 meters, are natural mirrors where the desert sky reflects in turquoise and crystalline waters that create a striking contrast with the terracotta and reddish tones of the surrounding landscape. Only two of the seven lagoons are enabled for bathing, offering a unique floating experience while the other five remain protected, preserving their virgin beauty. In the last lagoon, reddish gelatinous formations compatible with extremophile bacteria have even been detected, evidence of life in extreme conditions that fascinates scientists and visitors alike."},
                {"type": "paragraph", "content": "Visitors experience a deep connection with living geology: when immersing themselves in the hyper-saline waters, they feel on their skin the history of a prehistoric ocean, while the desert silence amplifies the perception of this landscape that combines the austere majesty of the Cordillera de la Sal with the delicate transparency of the lagoons. Access via a reddish dirt road dotted with cacti intensifies the feeling of arriving at a secret place, almost untouched since its formation millions of years ago."},
                {"type": "heading", "content": "Vallecito & Magic Bus"},
                {"type": "paragraph", "content": "Located precisely in the Llano de la Paciencia, Vallecito represents the less crowded but equally spectacular part of the Cordillera de la Sal, where time and nature have sculpted landscapes that seem from another planet. The striking formation of the Hidden Magic Bus emerges as a contemporary icon in this prehistoric setting, creating a surreal scene that contrasts recent human footprints with the immensity of the desert. The impressive rock formations of Vallecito display a palette of changing colors according to the daylight, from intense ochers to ethereal violets, revealing sedimentary layers that narrate the tectonic history of the region."},
                {"type": "paragraph", "content": "The geological experience intensifies when walking on the high dunes, where the soft sand underfoot transmits the energy of the wind, the main architect of this landscape. From the summits, panoramic views extend as far as the eye can see, showing textures and erosive patterns that only millions of years of wind and aridity can create. When exploring the ancient channels used by pre-Hispanic inhabitants, visitors connect with human ingenuity adapted to this extreme environment, completing an experience that unites geology, history and the primordial force of the Atacama Desert."}
            ],
            "pt": [
                {"type": "paragraph", "content": "Observe espelhos de sal, dunas douradas e geologia viva em uma rota íntima ao amanhecer."},
                {"type": "heading", "content": "🌊 Lagoas Escondidas de Baltinache"},
                {"type": "paragraph", "content": "Este complexo de sete poças de água salgada forma um oásis surreal no meio do deserto, localizado no Llano de la Paciencia, ao pé da Cordilheira do Sal e da Cordilheira de Domeyko. Sua origem geológica remonta ao Cretáceo, quando o surgimento dos Andes aprisionou braços marinhos que, mediante evaporação e milhões de anos de aridez, formaram este salar com uma salinidade extrema de 220 gramas por litro—quase seis vezes mais que o Mar Morto. As lagoas, com apenas 5 metros de diâmetro em média, são espelhos naturais onde o céu desértico se reflete em águas turquesa e cristalinas que criam um contraste impactante com os tons terracota e avermelhados da paisagem circundante. Apenas duas das sete lagoas estão habilitadas para banho, oferecendo uma experiência de flutuação única enquanto as outras cinco permanecem protegidas, conservando sua beleza virgem. Na última lagoa, inclusive, foram detectadas formações gelatinosas avermelhadas compatíveis com bactérias extremófilas, evidência de vida em condições limites que fascina cientistas e visitantes por igual."},
                {"type": "paragraph", "content": "Os visitantes experimentam uma conexão profunda com a geologia viva: ao se submergir nas águas hipersalinas, sentem na pele a história de um oceano pré-histórico, enquanto o silêncio do deserto amplifica a percepção desta paisagem que combina a austera majestade da Cordilheira do Sal com a delicada transparência das lagoas. O acesso por um caminho de terra avermelhada salpicado de cactos intensifica a sensação de chegar a um lugar secreto, quase intacto desde sua formação há milhões de anos."},
                {"type": "heading", "content": "Vallecito & Ônibus Mágico"},
                {"type": "paragraph", "content": "Localizado precisamente no Llano de la Paciencia, Vallecito representa a parte menos concorrida mas igualmente espetacular da Cordilheira do Sal, onde o tempo e a natureza esculpiram paisagens que parecem de outro planeta. A chamativa formação do Ônibus Mágico Escondido surge como um ícone contemporâneo neste cenário pré-histórico, criando uma cena surreal que contrasta a pegada humana recente com a imensidão do deserto. As impressionantes formações rochosas de Vallecito exibem uma paleta de cores cambiantes conforme a luz do dia, desde ocre intenso até violetas etéreos, revelando camadas sedimentares que narram a história tectônica da região."},
                {"type": "paragraph", "content": "A experiência geológica se intensifica ao caminhar pelas altas dunas, onde a suave areia sob os pés transmite a energia do vento, o principal arquiteto desta paisagem. Desde os cumes, as vistas panorâmicas se estendem até onde alcança a vista, mostrando texturas e padrões erosivos que apenas milhões de anos de vento e aridez podem criar. Ao explorar os antigos canais utilizados por habitantes pré-hispânicos, os visitantes conectam com o engenho humano adaptado a este ambiente extremo, completando uma experiência que une geologia, história e a força primigênia do deserto do Atacama."}
            ]
        }'::jsonb,
        'REGULAR', 45000.00, 5, FALSE, 'PUBLISHED',
        'tour-lagunas-escondidas-de-baltinache-y-vallecito',
        FALSE, FALSE, FALSE,
        '{
            "es": [
                {"time": "07:30 – 08:00", "description": "Recogida y salida desde San Pedro de Atacama."},
                {"time": "08:00 – 09:30", "description": "Lagunas Escondidas de Baltinache: flotación en aguas hipersalinas y explicación geológica."},
                {"time": "09:30 – 10:30", "description": "Traslado por el Llano de la Paciencia, miradores y lectura del paisaje."},
                {"time": "10:30 – 11:30", "description": "Vallecito y Bus Mágico: caminata por dunas y formaciones rocosas."},
                {"time": "11:30 – 12:30", "description": "Cóctel ligero, cierre del tour y retorno a San Pedro."}
            ],
            "en": [
                {"time": "07:30 – 08:00", "description": "Pickup and departure from San Pedro de Atacama."},
                {"time": "08:00 – 09:30", "description": "Hidden Lagoons of Baltinache: floating in hypersaline waters and geological explanation."},
                {"time": "09:30 – 10:30", "description": "Transfer through Llano de la Paciencia, viewpoints and landscape interpretation."},
                {"time": "10:30 – 11:30", "description": "Vallecito and Magic Bus: walk through dunes and rock formations."},
                {"time": "11:30 – 12:30", "description": "Light cocktail, tour closure and return to San Pedro."}
            ],
            "pt": [
                {"time": "07:30 – 08:00", "description": "Busca e saída de San Pedro de Atacama."},
                {"time": "08:00 – 09:30", "description": "Lagoas Escondidas de Baltinache: flutuação em águas hipersalinas e explicação geológica."},
                {"time": "09:30 – 10:30", "description": "Translado pelo Llano de la Paciencia, mirantes e leitura da paisagem."},
                {"time": "10:30 – 11:30", "description": "Vallecito e Ônibus Mágico: caminhada por dunas e formações rochosas."},
                {"time": "11:30 – 12:30", "description": "Coquetel leve, encerramento do tour e retorno a San Pedro."}
            ]
        }'::jsonb,
        '{
            "es": [
                "El Llano de la Paciencia no es solo un trayecto, sino una subcuenca geológica del Salar de Atacama donde la ausencia de vegetación y la presencia de sales minerales crean un escenario lunar perfecto para entender los procesos de evaporación y formación de salares.",
                "La Cordillera de la Sal, que enmarca gran parte de esta ruta, es parte del emblemático Valle de la Muerte, un afloramiento de rocas sedimentarias y evaporitas que evidencia la antigua presencia de lagos salinos prehistóricos."
            ],
            "en": [
                "The Llano de la Paciencia is not just a route, but a geological sub-basin of the Atacama Salt Flat where the absence of vegetation and the presence of mineral salts create a perfect lunar scenario to understand evaporation processes and salt flat formation.",
                "The Cordillera de la Sal, which frames much of this route, is part of the emblematic Valle de la Muerte, an outcrop of sedimentary rocks and evaporites that evidences the ancient presence of prehistoric salt lakes."
            ],
            "pt": [
                "O Llano de la Paciencia não é apenas um trajeto, mas uma sub-bacia geológica do Salar de Atacama onde a ausência de vegetação e a presença de sais minerais criam um cenário lunar perfeito para entender os processos de evaporação e formação de salares.",
                "A Cordilheira do Sal, que emoldura grande parte desta rota, é parte do emblemático Vale da Morte, um afloramento de rochas sedimentares e evaporitos que evidencia a antiga presença de lagos salinos pré-históricos."
            ]
        }'::jsonb,
        15, '07:30:00'
    ) ON CONFLICT (slug) DO NOTHING;

    -- ====================================================================================
    -- TOUR 3: VALLE DEL ARCOÍRIS & HIERBAS BUENAS - CONTENIDO COMPLETO
    -- ====================================================================================

    RAISE NOTICE 'Insertando Tour Valle del Arcoíris & Hierbas Buenas...';
    INSERT INTO tours (
        id, owner_id, name_translations, description_blocks_translations, category, price,
        duration_hours, recurring, status, slug, moon_sensitive, wind_sensitive, cloud_sensitive,
        itinerary_translations, default_max_participants, default_start_time
    ) VALUES (
        gen_random_uuid(), admin_user_id,
        '{
            "es": "Tour Valle del Arcoíris & Hierbas Buenas",
            "en": "Rainbow Valley & Hierbas Buenas Tour",
            "pt": "Tour Vale do Arco-Íris & Hierbas Buenas"
        }'::jsonb,
        '{
            "es": [
                {"type": "paragraph", "content": "Iniciamos el día con un desayuno entre montañas que despiertan con la primera luz del sol. El camino nos conduce hacia el Valle del Arcoíris, donde los minerales pintan la tierra con tonos intensos, formando uno de los paisajes más asombrosos de la Cordillera de Domeyko."},
                {"type": "heading", "content": "Valle del Arcoíris: Paleta Mineral de la Cordillera de Domeyko"},
                {"type": "paragraph", "content": "A 90 kilómetros de San Pedro de Atacama y a 3.500 metros de altitud, este valle geológico es un mausoleo de color y tiempo. Su espectro cromático no es mera decoración, sino la autobiografía de 90 millones de años de historia. Las formaciones rocosas pertenecen a la Formación Purilactis (Cretácico tardío) y Formación Tonel, compuestas por limolitas y areniscas finas que fueron depositadas en antiguos sistemas fluvio-lacustres. Los intensos tonos rojizos provienen de óxidos de hierro (hematita) formados en ambientes oxidantes; los verdes, de clorita y epidota generadas por alteración hidrotermal de intrusivos hipabisales; los amarillos y ocres, de sulfuros de hierro y sales evaporíticas; mientras que los blancos son yeso y halita, vestigios de antiguas lagunas salinas que brillan como nieve bajo el sol desértico."},
                {"type": "paragraph", "content": "La experiencia es una inmersión en una paleta viva: caminar entre estas formaciones erosionadas por el viento y el agua durante millones de años permite tocar capas sedimentarias que narran el alzamiento de los Andes. La mañana, con la luz baja, crea juegos de sombras que realzan cada estrato, ofreciendo momentos fotográficos únicos donde los minerales parecen encenderse desde adentro. Es un laboratorio natural donde la geología se hace arte y cada tono es un capítulo de la evolución tectónica de la Puna de Atacama."},
                {"type": "heading", "content": "Hierbas Buenas: Anfiteatro de Piedra y Memoria"},
                {"type": "paragraph", "content": "Este sitio arqueológico, ubicado a 65 km al norte de San Pedro en la confluencia de la Cordillera de Domeyko, la Cordillera de la Sal y los Andes (3.050 m.s.n.m.), es el mayor centro de arte rupestre de toda la zona arqueológica de San Pedro de Atacama. En su ''anfiteatro natural'' de rocas blandas volcánicas y procesos de plegamiento únicos, se conservan más de 1.000 petroglifos que representan cuatro tradiciones culturales principales: la naturalista de Taira, la tradición Angostura, el Estilo 2 de influencia Aguada y estilos Incásicos, con dataciones que abarcan desde el Formativo Temprano hasta el período Colonial."},
                {"type": "paragraph", "content": "Los visitantes se encuentran con un diálogo milenario esculpido en piedra: antropomorfos, camelidae, felinos andinos y sorprendentes primates amazónicos (evidencia de las extensas rutas de trueque que conectaban el Altiplano con el océano Pacífico). Los petroglifos no son simples dibujos, sino un lenguaje ritual que señalaba rutas, marcaba aguadas y transmitía cosmovisión. El sol de la mañana raspa las superficies oxidadas, resaltando las figuras con luz dorada mientras el silencio del desierto permite escuchar los pasos de las caravanas que hace mil años transitaban hacia el oasis. Es una conexión tangible con la cultura atacameña, donde cada grabado es un mensaje de supervivencia y espiritualidad en el desierto más árido del mundo."}
            ],
            "en": [
                {"type": "paragraph", "content": "We begin the day with breakfast among mountains that awaken with the first light of the sun. The road leads us towards Rainbow Valley, where minerals paint the earth with intense tones, forming one of the most astonishing landscapes of the Cordillera de Domeyko."},
                {"type": "heading", "content": "Rainbow Valley: Mineral Palette of the Cordillera de Domeyko"},
                {"type": "paragraph", "content": "Located 90 kilometers from San Pedro de Atacama at 3,500 meters above sea level, this geological valley is a mausoleum of color and time. Its chromatic spectrum is not mere decoration, but the autobiography of 90 million years of history. The rock formations belong to the Purilactis Formation (Late Cretaceous) and Tonel Formation, composed of siltstones and fine sandstones that were deposited in ancient fluvio-lacustrine systems. The intense reddish tones come from iron oxides (hematite) formed in oxidizing environments; the greens, from chlorite and epidote generated by hydrothermal alteration of hypabyssal intrusives; the yellows and ochers, from iron sulfides and evaporitic salts; while the whites are gypsum and halite, vestiges of ancient salt lagoons that shine like snow under the desert sun."},
                {"type": "paragraph", "content": "The experience is an immersion in a living palette: walking among these formations eroded by wind and water over millions of years allows touching sedimentary layers that narrate the rise of the Andes. The morning, with low light, creates shadow plays that enhance each stratum, offering unique photographic moments where minerals seem to ignite from within. It is a natural laboratory where geology becomes art and every tone is a chapter of the tectonic evolution of the Atacama Puna."},
                {"type": "heading", "content": "Hierbas Buenas: Amphitheater of Stone and Memory"},
                {"type": "paragraph", "content": "This archaeological site, located 65 km north of San Pedro at the confluence of the Cordillera de Domeyko, the Cordillera de la Sal and the Andes (3,050 meters above sea level), is the largest rock art center in the entire San Pedro de Atacama archaeological area. In its ''natural amphitheater'' of soft volcanic rocks and unique folding processes, over 1,000 petroglyphs are preserved representing four main cultural traditions: the naturalistic Taira, the Angostura tradition, Style 2 with Aguada influence, and Inca styles, with dates spanning from the Early Formative to the Colonial period."},
                {"type": "paragraph", "content": "Visitors encounter a millennial dialogue sculpted in stone: anthropomorphs, camelids, Andean felines and surprising Amazonian primates (evidence of the extensive trade routes that connected the Altiplano with the Pacific Ocean). The petroglyphs are not simple drawings, but a ritual language that marked routes, indicated water sources and transmitted cosmovision. The morning sun scrapes the oxidized surfaces, highlighting the figures with golden light while the desert silence allows hearing the footsteps of the caravans that a thousand years ago traveled towards the oasis. It is a tangible connection with the Atacameño culture, where every engraving is a message of survival and spirituality in the most arid desert in the world."}
            ],
            "pt": [
                {"type": "paragraph", "content": "Iniciamos o dia com café da manhã entre montanhas que despertam com a primeira luz do sol. O caminho nos conduz ao Vale do Arco-Íris, onde os minerais pintam a terra com tons intensos, formando uma das paisagens mais surpreendentes da Cordilheira de Domeyko."},
                {"type": "heading", "content": "Vale do Arco-Íris: Paleta Mineral da Cordilheira de Domeyko"},
                {"type": "paragraph", "content": "Localizado a 90 quilômetros de San Pedro de Atacama e a 3.500 metros de altitude, este vale geológico é um mausoléu de cor e tempo. Seu espectro cromático não é mera decoração, mas a autobiografia de 90 milhões de anos de história. As formações rochosas pertencem à Formação Purilactis (Cretáceo Superior) e Formação Tonel, compostas por siltitos e arenitos finos que foram depositados em antigos sistemas fluvio-lacustres. Os intensos tons avermelhados provêm de óxidos de ferro (hematita) formados em ambientes oxidantes; os verdes, de clorita e epidota geradas por alteração hidrotermal de intrusivos hipabissais; os amarelos e ocre, de sulfetos de ferro e sais evaporíticos; enquanto os brancos são gipsita e halita, vestígios de antigas lagoas salinas que brilham como neve sob o sol desértico."},
                {"type": "paragraph", "content": "A experiência é uma imersão em uma paleta viva: caminhar entre estas formações erodidas pelo vento e pela água durante milhões de anos permite tocar camadas sedimentares que narram o soerguimento dos Andes. A manhã, com a luz baixa, cria jogos de sombras que realçam cada estrato, oferecendo momentos fotográficos únicos onde os minerais parecem acender-se desde dentro. É um laboratório natural onde a geologia se faz arte e cada tom é um capítulo da evolução tectônica da Puna de Atacama."},
                {"type": "heading", "content": "Hierbas Buenas: Anfiteatro de Pedra e Memória"},
                {"type": "paragraph", "content": "Este sítio arqueológico, localizado a 65 km ao norte de San Pedro na confluência da Cordilheira de Domeyko, a Cordilheira do Sal e os Andes (3.050 m acima do nível do mar), é o maior centro de arte rupestre de toda a zona arqueológica de San Pedro de Atacama. Em seu ''anfiteatro natural'' de rochas vulcânicas macias e processos de dobramento únicos, conservam-se mais de 1.000 petróglifos que representam quatro tradições culturais principais: a naturalista de Taira, a tradição Angostura, o Estilo 2 de influência Aguada e estilos Incas, com datações que abrangem desde o Formativo Inicial até o período Colonial."},
                {"type": "paragraph", "content": "Os visitantes se deparam com um diálogo milenar esculpido em pedra: antropomorfos, camelídeos, felinos andinos e surpreendentes primatas amazônicos (evidência das extensas rotas de troca que conectavam o Altiplano com o oceano Pacífico). Os petróglifos não são simples desenhos, mas uma linguagem ritual que sinalizava rotas, marcava fontes de água e transmitia cosmovisão. O sol da manhã raspa as superfícies oxidadas, destacando as figuras com luz dourada enquanto o silêncio do deserto permite ouvir os passos das caravanas que há mil anos transitavam rumo ao oásis. É uma conexão tangível com a cultura atacamenha, onde cada gravura é uma mensagem de sobrevivência e espiritualidade no deserto mais árido do mundo."}
            ]
        }'::jsonb,
        'REGULAR', 40000.00, 4, FALSE, 'PUBLISHED',
        'tour-valle-del-arcoiris-y-hierbas-buenas',
        FALSE, FALSE, FALSE,
        '{
            "es": [
                {"time": "08:00 – 08:30", "description": "Salida desde San Pedro de Atacama y presentación del recorrido."},
                {"time": "08:30 – 10:00", "description": "Visita al Valle del Arcoíris: miradores, explicación geológica y fotografía."},
                {"time": "10:00 – 11:00", "description": "Traslado hacia Hierbas Buenas con lectura del paisaje e historia local."},
                {"time": "11:00 – 12:00", "description": "Recorrido guiado por Hierbas Buenas: arte rupestre, cosmovisión y cierre del tour."}
            ],
            "en": [
                {"time": "08:00 – 08:30", "description": "Departure from San Pedro de Atacama and presentation of the tour."},
                {"time": "08:30 – 10:00", "description": "Visit to Rainbow Valley: viewpoints, geological explanation and photography."},
                {"time": "10:00 – 11:00", "description": "Transfer to Hierbas Buenas with landscape interpretation and local history."},
                {"time": "11:00 – 12:00", "description": "Guided tour of Hierbas Buenas: rock art, cosmovision and tour closure."}
            ],
            "pt": [
                {"time": "08:00 – 08:30", "description": "Saída de San Pedro de Atacama e apresentação do roteiro."},
                {"time": "08:30 – 10:00", "description": "Visita ao Vale do Arco-Íris: mirantes, explicação geológica e fotografia."},
                {"time": "10:00 – 11:00", "description": "Translado para Hierbas Buenas com leitura da paisagem e história local."},
                {"time": "11:00 – 12:00", "description": "Percurso guiado por Hierbas Buenas: arte rupestre, cosmovisão e encerramento do tour."}
            ]
        }'::jsonb,
        15, '08:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    RAISE NOTICE 'Seeding de tours con contenido completo y traducciones detalladas finalizado.';

END $$;
