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
  bookingId?: string
  status?: string
  tourName?: string
  tourDate?: string
  customerName?: string
  customerEmail?: string
  participantCount?: string
  subtotal?: string
  taxAmount?: string
  totalAmount?: string
  specialRequests?: string
  createdAt?: string
}>(), {
  bookingId: 'NCH-12345',
  status: 'PENDING',
  tourName: 'Tour Astronómico Valle de la Luna',
  tourDate: '15/01/2025 20:00',
  customerName: 'Juan Pérez',
  customerEmail: 'cliente@email.com',
  participantCount: '2',
  subtotal: '$100.000 CLP',
  taxAmount: '$19.000 CLP',
  totalAmount: '$119.000 CLP',
  specialRequests: '',
  createdAt: '2025-01-10 15:30'
})

const statusBadgeStyle = {
  PENDING: 'background-color: #fef3c7; color: #92400e;',
  CONFIRMED: 'background-color: #d1fae5; color: #065f46;',
  PAID: 'background-color: #dbeafe; color: #1e40af;'
}
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <!-- Admin Header - Green gradient -->
        <Section style="background: linear-gradient(135deg, #059669 0%, #047857 100%); padding: 40px 20px; text-align: center;">
          <Heading
            as="h1"
            style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 700;"
          >
            Nueva Reserva Recibida
          </Heading>
        </Section>

        <Section style="padding: 40px 30px;">
          <Text style="margin: 0 0 24px 0; color: #374151; font-size: 16px;">
            Se ha recibido una nueva reserva en el sistema.
          </Text>

          <!-- Booking Details -->
          <Section style="background-color: #f0fdf4; border-left: 4px solid #059669; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 16px 0; color: #065f46; font-size: 18px;"
            >
              Detalles de la Reserva
            </Heading>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  ID Reserva:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${bookingId}"
                >
                  <strong>{{ props.bookingId }}</strong>
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Estado:
                </td>
                <td style="padding: 6px 0;">
                  <span
                    th:text="${status}"
                    :style="statusBadgeStyle[props.status] || statusBadgeStyle.PENDING"
                    style="display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; text-transform: uppercase;"
                  >
                    {{ props.status }}
                  </span>
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Tour:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${tourName}"
                >
                  {{ props.tourName }}
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Fecha del Tour:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${tourDate}"
                >
                  {{ props.tourDate }}
                </td>
              </tr>
            </table>
          </Section>

          <!-- Customer Info -->
          <Section style="background-color: #f0fdf4; border-left: 4px solid #059669; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 16px 0; color: #065f46; font-size: 18px;"
            >
              Información del Cliente
            </Heading>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Cliente:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${customerName}"
                >
                  {{ props.customerName }}
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Email:
                </td>
                <td style="padding: 6px 0;">
                  <a
                    th:href="'mailto:' + ${customerEmail}"
                    :href="'mailto:' + props.customerEmail"
                    style="color: #059669; text-decoration: none;"
                    th:text="${customerEmail}"
                  >
                    {{ props.customerEmail }}
                  </a>
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Participantes:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${participantCount}"
                >
                  {{ props.participantCount }}
                </td>
              </tr>
            </table>
          </Section>

          <!-- Payment Info -->
          <Section style="background-color: #f0fdf4; border-left: 4px solid #059669; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 16px 0; color: #065f46; font-size: 18px;"
            >
              Información del Pago
            </Heading>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  Subtotal:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${subtotal}"
                >
                  {{ props.subtotal }}
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin: 12px 0;"
            >
              <tr>
                <td style="font-weight: 600; color: #1a2232; width: 45%; padding: 6px 0;">
                  IVA (19%):
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${taxAmount}"
                >
                  {{ props.taxAmount }}
                </td>
              </tr>
            </table>

            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin-top: 16px; padding-top: 16px; border-top: 2px solid #d1fae5;"
            >
              <tr>
                <td style="font-size: 18px; font-weight: 700; color: #059669; width: 45%; padding: 6px 0;">
                  Total:
                </td>
                <td
                  style="font-size: 18px; font-weight: 700; color: #059669; padding: 6px 0;"
                  th:text="${totalAmount}"
                >
                  {{ props.totalAmount }}
                </td>
              </tr>
            </table>
          </Section>

          <!-- Special Requests -->
          <div th:if="${specialRequests != null and !specialRequests.isEmpty()}">
            <Section style="background-color: #fef9c3; border-left: 4px solid #eab308; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
              <Heading
                as="h3"
                style="margin: 0 0 12px 0; color: #92400e; font-size: 18px;"
              >
                Solicitudes Especiales
              </Heading>
              <Text
                style="margin: 0; color: #92400e; white-space: pre-wrap;"
                th:text="${specialRequests}"
              >
                {{ props.specialRequests }}
              </Text>
            </Section>
          </div>

          <!-- Metadata -->
          <Text style="margin: 24px 0 0 0; padding-top: 24px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 12px;">
            <strong>Reserva creada:</strong> <span th:text="${createdAt}">{{ props.createdAt }}</span>
          </Text>

          <!-- Quick Actions -->
          <Section style="text-align: center; margin: 32px 0;">
            <a
              href="https://www.northernchile.com/admin/bookings"
              style="display: inline-block; background-color: #059669; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 14px;"
            >
              Ver en Admin
            </a>
          </Section>
        </Section>

        <Section style="background-color: #f8fafc; padding: 20px; text-align: center; border-top: 1px solid #e2e8f0;">
          <Text style="margin: 0; color: #64748b; font-size: 12px;">
            Este es un email automático del sistema de Northern Chile Tours
          </Text>
          <Text style="margin: 8px 0 0 0; color: #64748b; font-size: 12px;">
            Revisa y procesa esta reserva en el panel de administración.
          </Text>
        </Section>
      </Container>
    </Body>
  </Html>
</template>
