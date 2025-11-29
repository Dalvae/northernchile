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

// Props para preview/testing - en producción vendrán de Thymeleaf
const props = withDefaults(defineProps<{
  customerName?: string
  bookingId?: string
  tourName?: string
  tourDate?: string
  tourTime?: string
  participantCount?: string
  totalAmount?: string
}>(), {
  customerName: 'Diego Alvarez',
  bookingId: 'NCH-12345',
  tourName: 'Tour Astronómico Valle de la Luna',
  tourDate: '2025-01-15',
  tourTime: '19:00',
  participantCount: '2',
  totalAmount: '$150.00'
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <!-- Header -->
        <Section style="background: linear-gradient(135deg, #1a2232 0%, #243447 100%); padding: 40px 20px; text-align: center;">
          <img
            src="https://www.northernchile.com/images/email-logo.png"
            alt="Northern Chile Tours"
            width="64"
            height="64"
            style="display: block; margin: 0 auto 16px auto; border-radius: 8px;"
          >
          <Heading
            as="h1"
            style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;"
          >
            Northern Chile Tours
          </Heading>
        </Section>

        <!-- Body -->
        <Section style="padding: 40px 30px;">
          <Heading
            as="h2"
            style="color: #1a2232; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;"
            th:text="#{email.booking.confirmation.title}"
          >
            ¡Reserva Confirmada!
          </Heading>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.booking.confirmation.greeting(${customerName})}"
          >
            Hola <span>{{ props.customerName }}</span>,
          </Text>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.booking.confirmation.body}"
          >
            ¡Gracias por reservar con Northern Chile Tours! Tu aventura está confirmada.
          </Text>

          <!-- Info Box -->
          <Section style="background-color: #f8fafc; border-left: 4px solid #fbaa62; border-radius: 8px; padding: 20px 24px; margin: 24px 0; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);">
            <Text
              style="margin: 0 0 16px 0; color: #1a2232; font-size: 18px; font-weight: 700;"
              th:text="#{email.booking.details}"
            >
              Detalles de la Reserva
            </Text>

            <!-- Booking ID -->
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td
                  style="font-weight: 600; color: #475569; width: 40%;"
                  th:text="#{email.booking.id}"
                >
                  ID de Reserva:
                </td>
                <td
                  style="color: #1a2232; text-align: right;"
                  th:text="${bookingId}"
                >
                  {{ props.bookingId }}
                </td>
              </tr>
            </table>

            <!-- Tour Name -->
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td
                  style="font-weight: 600; color: #475569; width: 40%;"
                  th:text="#{email.booking.tour}"
                >
                  Tour:
                </td>
                <td
                  style="color: #1a2232; text-align: right;"
                  th:text="${tourName}"
                >
                  {{ props.tourName }}
                </td>
              </tr>
            </table>

            <!-- Date -->
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td
                  style="font-weight: 600; color: #475569; width: 40%;"
                  th:text="#{email.booking.date}"
                >
                  Fecha:
                </td>
                <td
                  style="color: #1a2232; text-align: right;"
                  th:text="${tourDate}"
                >
                  {{ props.tourDate }}
                </td>
              </tr>
            </table>

            <!-- Time -->
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td
                  style="font-weight: 600; color: #475569; width: 40%;"
                  th:text="#{email.booking.time}"
                >
                  Hora:
                </td>
                <td
                  style="color: #1a2232; text-align: right;"
                  th:text="${tourTime}"
                >
                  {{ props.tourTime }}
                </td>
              </tr>
            </table>

            <!-- Participants -->
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td
                  style="font-weight: 600; color: #475569; width: 40%;"
                  th:text="#{email.booking.participants}"
                >
                  Participantes:
                </td>
                <td
                  style="color: #1a2232; text-align: right;"
                  th:text="${participantCount}"
                >
                  {{ props.participantCount }}
                </td>
              </tr>
            </table>

            <!-- Total -->
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin-top: 20px; padding-top: 20px; border-top: 2px solid #e2e8f0;"
            >
              <tr>
                <td
                  style="font-size: 20px; font-weight: 700; color: #fbaa62;"
                  th:text="#{email.booking.total}"
                >
                  Total:
                </td>
                <td
                  style="font-size: 20px; font-weight: 700; color: #fbaa62; text-align: right;"
                  th:text="${totalAmount}"
                >
                  {{ props.totalAmount }}
                </td>
              </tr>
            </table>
          </Section>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.booking.confirmation.next}"
          >
            Te enviaremos un recordatorio 24 horas antes de tu tour. Por favor llega 10 minutos antes al punto de encuentro.
          </Text>

          <Text style="margin: 30px 0 0 0; color: #374151;">
            <strong th:text="#{email.booking.confirmation.questions}">¿Preguntas?</strong><br>
            <span th:text="#{email.booking.confirmation.contact}">Contáctanos en contacto@northernchile.com o +56 9 5765 5764</span>
          </Text>
        </Section>

        <!-- Footer -->
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
