### **Contexto del Proyecto: Plataforma "Northern Chile"**

**Propósito:** Este documento recopila los apuntes, ideas, requisitos de negocio y la visión estratégica que sirven como base para el desarrollo de la plataforma "Northern Chile". Su objetivo es centralizar la información clave del cliente para que guíe el diseño, el contenido y la funcionalidad del proyecto.

#### **1. Visión del Negocio y Propuesta de Valor**

- **El Producto Principal:** Lo que más se debe destacar es la experiencia astronómica.
- **Filosofía de Venta:** No se vende un tour, se vende una experiencia de conocimiento, un "show". Se crea un **"Auditorio a Cielo Abierto"** donde la gente se recuesta a los pies del desierto para conectar el cielo con el suelo.
- **Público Objetivo Principal:** Turista **brasilero** y **chileno**. Específicamente, gente "que tiene un poquito más de cultura".
- **Identidad de Marca:** Debe ser algo **serio y llamativo**, con un fuerte **enfoque local** en San Pedro de Atacama.
- **La Fotografía es Clave:** Se debe dar máxima relevancia a la calidad de las fotos en la plataforma.

#### **2. Oferta de Tours y Estructura Comercial**

- **Estructura de la Oferta:** Se definen 3 segmentos de tours:
  1.  **Tours Astronómicos** (El producto estrella).
  2.  **Tours Regulares** (Geiser del Tatio, Valle de la Luna, Lagunas Altiplánicas, Piedras Rojas, Valle del Arcoíris, Laguna Cejar, Lagunas Escondidas).
  3.  **Tours Especiales** (Privados, compartidos, giras estudiantiles).
- **Comportamiento de Compra:** Un insight clave es que "nadie compra el tour astronómico solo, le sale conveniente comprar paquetes". La plataforma debe facilitar esta compra combinada.

#### **3. Requisitos Funcionales y Técnicos**

- **Dominios Considerados:** `astronorthernchile.com` y `Northernchile.com`.
- **Pasarelas de Pago:** Es crucial revisar e integrar **Transbank, PIX** (para el mercado brasileño) y **Mercado Pago**.
- **Calendario Inteligente:**
  - Debe ser capaz de integrar **condiciones meteorológicas** (se necesita una API gratuita).
  - Debe mostrar las **fases lunares**, ya que son críticas para el tour astronómico.
- **Comunicaciones y Marketing:**
  - Implementar un sistema de **mailing** para el proceso de booking.
  - Integrar las reseñas y puntuaciones de **TripAdvisor**.
- **Visión a Futuro:** Se contempla añadir un **Chat** (posiblemente con IA) en una fase posterior, Integrar **WhatsApp** (a futuro).

#### **4. Reglas de Negocio y Operaciones**

- **Conflicto de Horarios:** Hay que implementar una regla que impida reservar un tour astronómico si el cliente ya reservó el tour a los Géisers del Tatio para el mismo día.
- **Condicionantes Climáticas:** Hay aproximadamente **40 días nublados al año** que pueden complicar la realización del tour astronómico. La plataforma debe gestionar esta expectativa y para otros tours condiciones como tormentas de arenas cerrar el booking con 25 kt de viento.
- **Estacionalidad:** Los cielos de Atacama son **totalmente diferentes cada 6 meses**. Esto puede ser un punto de marketing para incentivar visitas en distintas épocas del año.
- **Desafío del Mercado Local:** En San Pedro es difícil salirse de lo tradicional porque se arriesga a no llenar los cupos.

#### **5. Detalles de la Experiencia y Credenciales (Contenido Clave)**

- **La Experiencia Astronómica:**
  - Es un tour natural que implica **caminar por las dunas**.
  - **Equipo Técnico:** Se cuenta con 4 telescopios (Catadioptrico Celestron 8se, un Skywatcher con montura ecuatorial de 6 pulgadas, Celestron 114 lsm de 4 pulgadas, Seestar s50zvw) y cámaras profesionales.
- **Credenciales de Alex (Fundador):**
  - **15 años de trayectoria** en turismo astronómico en Bolivia y San Pedro.
  - Trabajaba con el **Museo del Meteorito**.
  - **Ganaron el premio al mejor tour de Atacama** el año pasado.
  - Participó como la **voz en off** del tour astronómico en la feria VIVA en Antofagasta.
  - Su agencia fue seleccionada entre las **9 mejores de San Pedro** por emisarios turísticos.

#### **6. Puntos Abiertos a Definir por el Cliente**

- **La Colaboración con David:**
  - **El problema:** ¿Cómo se integra a David en la plataforma? Él solo puede los fines de semana y no quiere pagar impuestos directamente.
  - **Posible Solución Propuesta:** Que los tours de David se vendan a través de la plataforma, Alex facture por ellos (agregando los impuestos correspondientes) y luego le pague su parte a David. Esto requiere la validación del cliente.

#### **7. Información del Ecosistema Local y Contactos**

- **Empresas en la zona:** Smart tour, Hoteles Pueblos de Tierra, Casa Voyage, Backpacker, Hostal Jara.
