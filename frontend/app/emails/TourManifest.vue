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

interface Participant {
  number: number
  fullName: string
  phoneNumber?: string
  pickupAddress?: string
  nationality?: string
  documentId?: string
  specialRequirements?: string
  bookingSpecialRequests?: string
}

const props = withDefaults(defineProps<{
  tourName?: string
  tourDate?: string
  tourTime?: string
  guideName?: string
  totalParticipants?: string
  participants?: Participant[]
}>(), {
  tourName: 'Tour Astronómico Premium',
  tourDate: '27/11/2025',
  tourTime: '20:00',
  guideName: 'Carlos Rodríguez',
  totalParticipants: '8',
  participants: () => [
    {
      number: 1,
      fullName: 'Juan Pérez',
      phoneNumber: '+56 9 1234 5678',
      pickupAddress: 'Hotel Explora, hab 204',
      nationality: 'Chileno',
      documentId: '12.345.678-9',
      specialRequirements: 'Vegetariano',
      bookingSpecialRequests: ''
    },
    {
      number: 2,
      fullName: 'María García',
      phoneNumber: '+56 9 8765 4321',
      pickupAddress: 'Hostal Desert',
      nationality: 'Argentina',
      documentId: 'DNI 30123456',
      specialRequirements: '',
      bookingSpecialRequests: 'Cumpleaños de María'
    }
  ]
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 650px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <!-- Header -->
        <Section style="background: linear-gradient(135deg, #1a2232 0%, #243447 100%); padding: 30px 20px; text-align: center;">
          <Text style="margin: 0 0 5px 0; color: #fbaa62; font-size: 14px; text-transform: uppercase; letter-spacing: 2px;">
            Manifiesto de Tour
          </Text>
          <Heading
            as="h1"
            style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 600;"
          >
            Northern Chile Tours
          </Heading>
        </Section>

        <!-- Tour Info -->
        <Section style="background-color: #f8fafc; padding: 20px 24px; border-bottom: 3px solid #fbaa62;">
          <Heading
            as="h2"
            style="margin: 0 0 16px 0; color: #1a2232; font-size: 20px;"
            th:text="${tourName}"
          >
            {{ props.tourName }}
          </Heading>

          <table
            role="presentation"
            cellspacing="0"
            cellpadding="0"
            border="0"
            width="100%"
          >
            <tr>
              <td style="padding: 4px 24px 4px 0; color: #64748b; font-size: 14px;">
                <strong style="color: #1a2232;">Fecha:</strong> <span th:text="${tourDate}">{{ props.tourDate }}</span>
              </td>
              <td style="padding: 4px 24px 4px 0; color: #64748b; font-size: 14px;">
                <strong style="color: #1a2232;">Hora:</strong> <span th:text="${tourTime}">{{ props.tourTime }}</span>
              </td>
              <td style="padding: 4px 0; color: #64748b; font-size: 14px;">
                <strong style="color: #1a2232;">Guía:</strong> <span th:text="${guideName}">{{ props.guideName }}</span>
              </td>
            </tr>
          </table>
        </Section>

        <!-- Summary Box -->
        <Section style="background-color: #fbaa62; padding: 20px; text-align: center;">
          <Text
            style="margin: 0; font-size: 36px; font-weight: 700; color: #1a2232;"
            th:text="${totalParticipants}"
          >
            {{ props.totalParticipants }}
          </Text>
          <Text style="margin: 4px 0 0 0; font-size: 14px; color: #1a2232; opacity: 0.9;">
            Participantes Confirmados
          </Text>
        </Section>

        <!-- Participant List -->
        <Section style="padding: 24px;">
          <Heading
            as="h3"
            style="color: #1a2232; font-size: 16px; margin: 0 0 16px 0; padding-bottom: 12px; border-bottom: 1px solid #e2e8f0;"
          >
            Lista de Participantes
          </Heading>

          <!-- Participant Cards -->
          <div th:each="p : ${participants}">
            <Section style="background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-bottom: 12px;">
              <!-- Participant Header -->
              <table
                role="presentation"
                cellspacing="0"
                cellpadding="0"
                border="0"
                width="100%"
                style="margin-bottom: 12px;"
              >
                <tr>
                  <td style="vertical-align: middle;">
                    <span
                      style="display: inline-block; background-color: #1a2232; color: #ffffff; width: 28px; height: 28px; border-radius: 50%; text-align: center; line-height: 28px; font-weight: 700; font-size: 14px;"
                      th:text="${p.number}"
                    >1</span>
                    <span
                      style="font-weight: 600; font-size: 16px; color: #1a2232; margin-left: 10px;"
                      th:text="${p.fullName}"
                    >Juan Pérez</span>
                  </td>
                </tr>
              </table>

              <!-- Participant Details -->
              <div style="font-size: 14px; color: #64748b; line-height: 1.8;">
                <div
                  th:if="${p.phoneNumber != null and !p.phoneNumber.isEmpty()}"
                  style="margin-bottom: 4px;"
                >
                  <span style="display: inline-block; width: 20px; text-align: center;">📱</span>
                  <span th:text="${p.phoneNumber}">+56 9 1234 5678</span>
                </div>
                <div
                  th:if="${p.pickupAddress != null and !p.pickupAddress.isEmpty()}"
                  style="margin-bottom: 4px;"
                >
                  <span style="display: inline-block; width: 20px; text-align: center;">📍</span>
                  <span th:text="${p.pickupAddress}">Hotel Explora, hab 204</span>
                </div>
                <div
                  th:if="${p.nationality != null and !p.nationality.isEmpty()}"
                  style="margin-bottom: 4px;"
                >
                  <span style="display: inline-block; width: 20px; text-align: center;">🌍</span>
                  <span th:text="${p.nationality}">Chileno</span>
                  <span th:if="${p.documentId != null and !p.documentId.isEmpty()}"> | Doc: <span th:text="${p.documentId}">12.345.678-9</span></span>
                </div>
              </div>

              <!-- Special Alert -->
              <div
                th:if="${(p.specialRequirements != null and !p.specialRequirements.isEmpty()) or (p.bookingSpecialRequests != null and !p.bookingSpecialRequests.isEmpty())}"
                style="background-color: #fef3c7; border: 1px solid #fbbf24; border-radius: 4px; padding: 10px 14px; margin-top: 12px; font-size: 13px; color: #92400e;"
              >
                <span style="margin-right: 6px;">⚠️</span>
                <span
                  th:if="${p.specialRequirements != null and !p.specialRequirements.isEmpty()}"
                  th:text="${p.specialRequirements}"
                >Vegetariano</span>
                <span th:if="${p.specialRequirements != null and !p.specialRequirements.isEmpty() and p.bookingSpecialRequests != null and !p.bookingSpecialRequests.isEmpty()}"> | </span>
                <span
                  th:if="${p.bookingSpecialRequests != null and !p.bookingSpecialRequests.isEmpty()}"
                  th:text="${p.bookingSpecialRequests}"
                >Cumpleaños</span>
              </div>
            </Section>
          </div>

          <!-- Vue preview fallback -->
          <Section
            v-for="p in props.participants"
            :key="p.number"
            style="background-color: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; margin-bottom: 12px;"
          >
            <table
              role="presentation"
              cellspacing="0"
              cellpadding="0"
              border="0"
              width="100%"
              style="margin-bottom: 12px;"
            >
              <tr>
                <td style="vertical-align: middle;">
                  <span style="display: inline-block; background-color: #1a2232; color: #ffffff; width: 28px; height: 28px; border-radius: 50%; text-align: center; line-height: 28px; font-weight: 700; font-size: 14px;">{{ p.number }}</span>
                  <span style="font-weight: 600; font-size: 16px; color: #1a2232; margin-left: 10px;">{{ p.fullName }}</span>
                </td>
              </tr>
            </table>

            <div style="font-size: 14px; color: #64748b; line-height: 1.8;">
              <div
                v-if="p.phoneNumber"
                style="margin-bottom: 4px;"
              >
                <span style="display: inline-block; width: 20px; text-align: center;">📱</span>
                <span>{{ p.phoneNumber }}</span>
              </div>
              <div
                v-if="p.pickupAddress"
                style="margin-bottom: 4px;"
              >
                <span style="display: inline-block; width: 20px; text-align: center;">📍</span>
                <span>{{ p.pickupAddress }}</span>
              </div>
              <div
                v-if="p.nationality"
                style="margin-bottom: 4px;"
              >
                <span style="display: inline-block; width: 20px; text-align: center;">🌍</span>
                <span>{{ p.nationality }}</span>
                <span v-if="p.documentId"> | Doc: {{ p.documentId }}</span>
              </div>
            </div>

            <div
              v-if="p.specialRequirements || p.bookingSpecialRequests"
              style="background-color: #fef3c7; border: 1px solid #fbbf24; border-radius: 4px; padding: 10px 14px; margin-top: 12px; font-size: 13px; color: #92400e;"
            >
              <span style="margin-right: 6px;">⚠️</span>
              <span v-if="p.specialRequirements">{{ p.specialRequirements }}</span>
              <span v-if="p.specialRequirements && p.bookingSpecialRequests"> | </span>
              <span v-if="p.bookingSpecialRequests">{{ p.bookingSpecialRequests }}</span>
            </div>
          </Section>
        </Section>

        <!-- Footer -->
        <Section style="background-color: #1a2232; padding: 20px; text-align: center;">
          <Text style="margin: 0 0 8px 0; color: #94a3b8; font-size: 12px;">
            Este manifiesto fue generado automáticamente 2 horas antes del inicio del tour.
          </Text>
          <Text style="margin: 0; color: #64748b; font-size: 12px;">
            Northern Chile Tours | San Pedro de Atacama
          </Text>
        </Section>
      </Container>
    </Body>
  </Html>
</template>
