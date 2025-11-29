<script setup lang="ts">
import {
  Body,
  Container,
  Head,
  Heading,
  Html,
  Section,
  Text,
} from '@vue-email/components'

const props = withDefaults(defineProps<{
  customerName?: string
  bookingId?: string
  tourName?: string
  tourDate?: string
  reason?: string
  refundAmount?: string
}>(), {
  customerName: 'Diego Alvarez',
  bookingId: 'NCH-12345',
  tourName: 'Tour Astronómico Valle de la Luna',
  tourDate: '15 de Enero, 2025',
  reason: 'Condiciones climáticas adversas',
  refundAmount: '$150.000 CLP',
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <Section style="background: linear-gradient(135deg, #1a2232 0%, #243447 100%); padding: 40px 20px; text-align: center;">
          <div style="margin-bottom: 16px;">
            <svg width="64" height="64" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg" style="display: inline-block;">
              <rect x="0" y="0" width="32" height="32" rx="8" ry="8" fill="#fbaa62" />
              <g transform="translate(4, 4)">
                <path d="m10.065 12.493-6.18 1.318a.934.934 0 0 1-1.108-.702l-.537-2.15a1.07 1.07 0 0 1 .691-1.265l13.504-4.44" fill="none" stroke="#1a2232" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <path d="m13.56 11.747 4.332-.924" fill="none" stroke="#1a2232" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <path d="m16 21-3.105-6.21" fill="none" stroke="#1a2232" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <path d="M16.485 5.94a2 2 0 0 1 1.455-2.425l1.09-.272a1 1 0 0 1 1.212.727l1.515 6.06a1 1 0 0 1-.727 1.213l-1.09.272a2 2 0 0 1-2.425-1.455z" fill="none" stroke="#1a2232" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <path d="m6.158 8.633 1.114 4.456" fill="none" stroke="#1a2232" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <path d="m8 21 3.105-6.21" fill="none" stroke="#1a2232" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <circle cx="12" cy="13" r="2" fill="none" stroke="#1a2232" stroke-width="2" />
              </g>
            </svg>
          </div>
          <Heading as="h1" style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;">
            Northern Chile Tours
          </Heading>
        </Section>

        <Section style="padding: 40px 30px;">
          <Heading as="h2" style="color: #ef4444; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;" th:text="#{email.cancelled.title}">
            Reserva Cancelada
          </Heading>

          <Text style="margin: 16px 0; color: #374151;" th:text="#{email.cancelled.greeting(${customerName})}">
            Hola <span>{{ props.customerName }}</span>,
          </Text>

          <Section style="background-color: #fee2e2; border-left: 4px solid #ef4444; border-radius: 6px; padding: 16px; margin: 24px 0;">
            <Text style="margin: 0; color: #991b1b; font-weight: 600;" th:text="#{email.cancelled.notice}">
              Lamentamos informarte que tu reserva ha sido cancelada.
            </Text>
          </Section>

          <Section style="background-color: #f8fafc; border-left: 4px solid #fbaa62; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 16px;">
              <strong th:text="#{email.booking.id}">ID de Reserva:</strong> <span th:text="${bookingId}">{{ props.bookingId }}</span>
            </Text>
            <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 16px;">
              <strong th:text="#{email.booking.tour}">Tour:</strong> <span th:text="${tourName}">{{ props.tourName }}</span>
            </Text>
            <Text style="margin: 0; color: #1a2232; font-size: 16px;">
              <strong th:text="#{email.booking.date}">Fecha:</strong> <span th:text="${tourDate}">{{ props.tourDate }}</span>
            </Text>
          </Section>

          <Text style="margin: 24px 0 8px 0; color: #1a2232; font-weight: 600;" th:text="#{email.cancelled.reason}">
            Motivo:
          </Text>
          <Text style="margin: 0 0 24px 0; color: #64748b;" th:text="${reason}">
            {{ props.reason }}
          </Text>

          <Heading as="h3" style="color: #1a2232; font-size: 18px; font-weight: 700; margin: 32px 0 16px 0;" th:text="#{email.cancelled.refund.title}">
            Información del Reembolso
          </Heading>

          <Text style="margin: 0 0 12px 0; color: #374151;" th:text="#{email.cancelled.refund.body}">
            Tu pago será reembolsado al método de pago original.
          </Text>

          <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 18px; font-weight: 700;">
            <span th:text="#{email.cancelled.refund.amount}">Monto a reembolsar:</span> <span style="color: #fbaa62;" th:text="${refundAmount}">{{ props.refundAmount }}</span>
          </Text>

          <Text style="margin: 0 0 24px 0; color: #64748b; font-size: 14px;" th:text="#{email.cancelled.refund.timing}">
            Por favor espera de 5 a 10 días hábiles para que el reembolso aparezca en tu cuenta.
          </Text>

          <Text style="margin: 24px 0; color: #374151;" th:text="#{email.cancelled.reschedule}">
            Si deseas reprogramar para otra fecha, visita nuestro sitio web o contáctanos.
          </Text>

          <Text style="margin: 24px 0 0 0; color: #64748b; font-style: italic;" th:text="#{email.cancelled.apology}">
            Pedimos disculpas por cualquier inconveniente y esperamos verte en un futuro tour.
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
            <a href="https://www.northernchile.cl" style="color: #fbaa62; text-decoration: none; font-weight: 600;">
              www.northernchile.cl
            </a>
          </Text>
        </Section>
      </Container>
    </Body>
  </Html>
</template>
