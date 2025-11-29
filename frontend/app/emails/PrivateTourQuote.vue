<script setup lang="ts">
import {
  Body,
  Container,
  Head,
  Heading,
  Html,
  Section,
  Text
} from '@vue-email/components'

const props = withDefaults(defineProps<{
  customerName?: string
  tourType?: string
  price?: string
  requests?: string
  notes?: string
}>(), {
  customerName: 'Diego Alvarez',
  tourType: 'Tour Astronómico Privado - Observación de Estrellas',
  price: '$450.000 CLP',
  requests: 'Grupo de 4 personas, interesados en fotografía astronómica',
  notes: 'Incluye telescopios profesionales y guía experto en astrofotografía'
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <Section style="background: linear-gradient(135deg, #1a2232 0%, #243447 100%); padding: 40px 20px; text-align: center;">
          <div style="margin-bottom: 16px;">
            <svg
              width="64"
              height="64"
              viewBox="0 0 32 32"
              xmlns="http://www.w3.org/2000/svg"
              style="display: inline-block;"
            >
              <rect
                x="0"
                y="0"
                width="32"
                height="32"
                rx="8"
                ry="8"
                fill="#fbaa62"
              />
              <g transform="translate(4, 4)">
                <path
                  d="m10.065 12.493-6.18 1.318a.934.934 0 0 1-1.108-.702l-.537-2.15a1.07 1.07 0 0 1 .691-1.265l13.504-4.44"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="m13.56 11.747 4.332-.924"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="m16 21-3.105-6.21"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M16.485 5.94a2 2 0 0 1 1.455-2.425l1.09-.272a1 1 0 0 1 1.212.727l1.515 6.06a1 1 0 0 1-.727 1.213l-1.09.272a2 2 0 0 1-2.425-1.455z"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="m6.158 8.633 1.114 4.456"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="m8 21 3.105-6.21"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle
                  cx="12"
                  cy="13"
                  r="2"
                  fill="none"
                  stroke="#1a2232"
                  stroke-width="2"
                />
              </g>
            </svg>
          </div>
          <Heading
            as="h1"
            style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;"
          >
            Northern Chile Tours
          </Heading>
        </Section>

        <Section style="padding: 40px 30px;">
          <Heading
            as="h2"
            style="color: #1a2232; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;"
            th:text="#{email.private.quote.title}"
          >
            Tu Cotización de Tour Privado
          </Heading>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.private.quote.greeting(${customerName})}"
          >
            Hola <span>{{ props.customerName }}</span>,
          </Text>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.private.quote.intro}"
          >
            Gracias por tu interés en un tour privado. Hemos preparado una cotización personalizada para ti.
          </Text>

          <!-- Quote Details -->
          <Section style="background-color: #f8fafc; border-left: 4px solid #fbaa62; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Text
              style="margin: 0 0 16px 0; color: #1a2232; font-size: 18px; font-weight: 700;"
              th:text="#{email.private.quote.details}"
            >
              Detalles del Tour
            </Text>

            <Text style="margin: 0 0 12px 0; color: #1a2232;">
              <strong th:text="#{email.private.quote.type}">Tipo de Tour:</strong><br>
              <span
                th:text="${tourType}"
                style="color: #64748b;"
              >{{ props.tourType }}</span>
            </Text>

            <Text style="margin: 20px 0 0 0; color: #1a2232; font-size: 24px; font-weight: 700;">
              <span th:text="#{email.private.quote.price}">Precio Total:</span><br>
              <span
                style="color: #fbaa62;"
                th:text="${price}"
              >{{ props.price }}</span>
            </Text>
          </Section>

          <!-- Validity Notice -->
          <Section style="background-color: #dbeafe; border-left: 4px solid #3b82f6; border-radius: 6px; padding: 16px; margin: 24px 0;">
            <Text
              style="margin: 0; color: #1e40af; font-size: 14px;"
              th:text="#{email.private.quote.validity}"
            >
              Esta cotización es válida por 7 días.
            </Text>
          </Section>

          <!-- Special Requests -->
          <div th:if="${requests != null and !requests.isEmpty()}">
            <Text
              style="margin: 24px 0 8px 0; color: #1a2232; font-weight: 700;"
              th:text="#{email.private.quote.requests}"
            >
              Tus Solicitudes Especiales:
            </Text>
            <Section style="background-color: #f0fdf4; border-left: 4px solid #10b981; border-radius: 6px; padding: 16px; margin: 8px 0 24px 0;">
              <Text
                style="margin: 0; color: #065f46; white-space: pre-wrap;"
                th:text="${requests}"
              >
                {{ props.requests }}
              </Text>
            </Section>
          </div>

          <!-- Notes -->
          <div th:if="${notes != null and !notes.isEmpty()}">
            <Text
              style="margin: 24px 0 8px 0; color: #1a2232; font-weight: 700;"
              th:text="#{email.private.quote.notes}"
            >
              Notas:
            </Text>
            <Text
              style="margin: 0 0 24px 0; padding: 16px; background-color: #fef3c7; border-radius: 6px; color: #92400e; border: 1px solid #fcd34d; white-space: pre-wrap;"
              th:text="${notes}"
            >
              {{ props.notes }}
            </Text>
          </div>

          <!-- Next Steps -->
          <Section style="background-color: #f8fafc; border-radius: 8px; padding: 20px; margin: 32px 0;">
            <Text
              style="margin: 0; color: #374151; line-height: 1.6;"
              th:text="#{email.private.quote.next}"
            >
              Para confirmar tu reserva, responde a este correo o contáctanos. Coordinaremos los detalles del pago y finalizaremos tu reservación.
            </Text>
          </Section>

          <Section style="text-align: center; margin: 32px 0;">
            <a
              href="mailto:contacto@northernchile.com"
              style="display: inline-block; background-color: #fbaa62; color: #1a2232; padding: 14px 32px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 16px;"
            >
              Confirmar Reserva
            </a>
          </Section>

          <Text style="margin: 24px 0 0 0; color: #64748b; font-size: 14px; text-align: center;">
            ¡Esperamos crear una experiencia inolvidable para ti!
          </Text>
        </Section>

        <Section style="background-color: #f8fafc; padding: 30px; text-align: center; border-top: 1px solid #e2e8f0;">
          <Text style="margin: 8px 0; font-weight: 600; color: #475569; font-size: 14px;">
            Northern Chile Tours
          </Text>
          <Text style="margin: 8px 0; color: #64748b; font-size: 14px;">
            San Pedro de Atacama, Chile
          </Text>
          <Text style="margin: 8px 0; font-size: 14px;">
            <a
              href="https://www.northernchile.com"
              style="color: #fbaa62; text-decoration: none; font-weight: 600;"
            >
              www.northernchile.com
            </a>
          </Text>
        </Section>
      </Container>
    </Body>
  </Html>
</template>
