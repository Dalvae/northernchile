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
  requestId?: string
  status?: string
  tourType?: string
  requestedDate?: string
  participantCount?: string
  customerName?: string
  customerEmail?: string
  customerPhone?: string
  specialRequests?: string
  createdAt?: string
}>(), {
  requestId: 'PTR-12345',
  status: 'PENDING',
  tourType: 'Tour Astronómico Privado',
  requestedDate: '15/01/2025 20:00',
  participantCount: '6',
  customerName: 'María García',
  customerEmail: 'cliente@email.com',
  customerPhone: '+56 9 1234 5678',
  specialRequests: 'Queremos celebrar un cumpleaños durante el tour.',
  createdAt: '2025-01-10 15:30'
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <!-- Admin Header - Purple gradient -->
        <Section style="background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%); padding: 40px 20px; text-align: center;">
          <Heading
            as="h1"
            style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 700;"
          >
            Nueva Solicitud de Tour Privado
          </Heading>
        </Section>

        <Section style="padding: 40px 30px;">
          <!-- Action Needed Alert -->
          <Section style="background-color: #fef2f2; border: 2px solid #ef4444; border-radius: 8px; padding: 16px 20px; margin: 0 0 24px 0; text-align: center;">
            <Text style="margin: 0; color: #dc2626; font-weight: 600; font-size: 16px;">
              Se requiere cotización para esta solicitud
            </Text>
          </Section>

          <!-- Request Details -->
          <Section style="background-color: #f5f3ff; border-left: 4px solid #7c3aed; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 16px 0; color: #5b21b6; font-size: 18px;"
            >
              Detalles de la Solicitud
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
                  ID Solicitud:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${requestId}"
                >
                  <strong>{{ props.requestId }}</strong>
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
                    style="display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; text-transform: uppercase; background-color: #ddd6fe; color: #5b21b6;"
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
                  Tipo de Tour:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${tourType}"
                >
                  {{ props.tourType }}
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
                  Fecha Solicitada:
                </td>
                <td
                  style="color: #1a2232; padding: 6px 0;"
                  th:text="${requestedDate}"
                >
                  {{ props.requestedDate }}
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

          <!-- Customer Info -->
          <Section style="background-color: #f5f3ff; border-left: 4px solid #7c3aed; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 16px 0; color: #5b21b6; font-size: 18px;"
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
                  Nombre:
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
                    style="color: #7c3aed; text-decoration: none;"
                    th:text="${customerEmail}"
                  >
                    {{ props.customerEmail }}
                  </a>
                </td>
              </tr>
            </table>

            <div th:if="${customerPhone != null and !customerPhone.isEmpty()}">
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
                    Teléfono:
                  </td>
                  <td style="padding: 6px 0;">
                    <a
                      th:href="'tel:' + ${customerPhone}"
                      :href="'tel:' + props.customerPhone"
                      style="color: #7c3aed; text-decoration: none;"
                      th:text="${customerPhone}"
                    >
                      {{ props.customerPhone }}
                    </a>
                  </td>
                </tr>
              </table>
            </div>
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
            <strong>Solicitud creada:</strong> <span th:text="${createdAt}">{{ props.createdAt }}</span>
          </Text>

          <!-- Quick Actions -->
          <Section style="text-align: center; margin: 32px 0;">
            <a
              th:href="'mailto:' + ${customerEmail}"
              :href="'mailto:' + props.customerEmail"
              style="display: inline-block; background-color: #7c3aed; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 14px; margin: 0 8px 8px 0;"
            >
              Contactar Cliente
            </a>
            <a
              href="https://www.northernchile.com/admin/private-tours"
              style="display: inline-block; background-color: #6b7280; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 14px; margin: 0 0 8px 0;"
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
            Revisa esta solicitud y contacta al cliente para proporcionar una cotización.
          </Text>
        </Section>
      </Container>
    </Body>
  </Html>
</template>
