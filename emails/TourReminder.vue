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
  bookingId?: string
  tourName?: string
  tourDate?: string
  tourTime?: string
}>(), {
  customerName: 'Diego Alvarez',
  bookingId: 'NCH-12345',
  tourName: 'Tour Astronómico Valle de la Luna',
  tourDate: '15 de Enero, 2025',
  tourTime: '19:00'
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
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

        <Section style="padding: 40px 30px;">
          <Heading
            as="h2"
            style="color: #1a2232; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;"
            th:text="#{email.reminder.title}"
          >
            ¡Tu tour es mañana!
          </Heading>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.reminder.greeting(${customerName})}"
          >
            Hola <span>{{ props.customerName }}</span>,
          </Text>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.reminder.body}"
          >
            Este es un recordatorio amistoso de que tu tour con Northern Chile Tours está programado para mañana.
          </Text>

          <Section style="background-color: #f8fafc; border-left: 4px solid #fbaa62; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 16px; font-weight: 700;">
              <span th:text="#{email.booking.tour}">Tour:</span> <span th:text="${tourName}">{{ props.tourName }}</span>
            </Text>
            <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 16px; font-weight: 700;">
              <span th:text="#{email.booking.date}">Fecha:</span> <span th:text="${tourDate}">{{ props.tourDate }}</span>
            </Text>
            <Text style="margin: 0; color: #1a2232; font-size: 16px; font-weight: 700;">
              <span th:text="#{email.booking.time}">Hora:</span> <span th:text="${tourTime}">{{ props.tourTime }}</span>
            </Text>
          </Section>

          <Section style="background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 6px; padding: 16px; margin: 24px 0;">
            <Text
              style="margin: 0 0 12px 0; color: #92400e; font-weight: 700;"
              th:text="#{email.reminder.important}"
            >
              ⚠️ Importante:
            </Text>
            <Text
              style="margin: 0 0 8px 0; color: #92400e; font-size: 14px;"
              th:text="#{email.reminder.point1}"
            >
              • Por favor llega 10 minutos antes de la hora programada
            </Text>
            <Text
              style="margin: 0 0 8px 0; color: #92400e; font-size: 14px;"
              th:text="#{email.reminder.point2}"
            >
              • Trae ropa abrigada - las noches del desierto pueden ser frías
            </Text>
            <Text
              style="margin: 0; color: #92400e; font-size: 14px;"
              th:text="#{email.reminder.point3(${bookingId})}"
            >
              • No olvides tu ID de reserva: {{ props.bookingId }}
            </Text>
          </Section>

          <Text
            style="margin: 24px 0 8px 0; color: #1a2232; font-weight: 600;"
            th:text="#{email.reminder.questions}"
          >
            ¿Necesitas hacer cambios?
          </Text>

          <Text
            style="margin: 0 0 24px 0; color: #64748b; font-size: 14px;"
            th:text="#{email.reminder.contact}"
          >
            Contáctanos en contacto@northernchile.com o +56 9 5765 5764
          </Text>

          <Text
            style="margin: 24px 0 0 0; color: #fbaa62; font-size: 16px; font-weight: 600; text-align: center;"
            th:text="#{email.reminder.excited}"
          >
            ¡Estamos ansiosos por compartir esta increíble experiencia contigo!
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
