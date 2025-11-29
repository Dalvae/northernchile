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
  tourName?: string
  tourDate?: string
  tourTime?: string
  pickupLocation?: string
  equipmentList?: string
  emergencyContact?: string
}>(), {
  customerName: 'Diego Alvarez',
  tourName: 'Tour Astronómico Valle de la Luna',
  tourDate: '15 de Enero, 2025',
  tourTime: '19:00',
  pickupLocation: 'Hotel Explora, San Pedro de Atacama',
  equipmentList: 'Ropa abrigada, linterna, agua',
  emergencyContact: '+56 9 5765 5764'
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
            th:text="#{pickup.alert.message}"
          >
            ¡Tu tour comienza pronto!
          </Heading>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{pickup.greeting(${customerName})}"
          >
            ¡Hola <span>{{ props.customerName }}</span>!
          </Text>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{pickup.intro}"
          >
            Te recordamos que tu tour está por comenzar. Por favor, prepárate para el pickup.
          </Text>

          <!-- Tour Details -->
          <Section style="background-color: #f8fafc; border-left: 4px solid #fbaa62; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 16px; font-weight: 700;">
              <span th:text="${tourName}">{{ props.tourName }}</span>
            </Text>
            <Text style="margin: 0 0 8px 0; color: #1a2232;">
              <strong th:text="#{pickup.date}">Fecha:</strong> <span th:text="${tourDate}">{{ props.tourDate }}</span>
            </Text>
            <Text style="margin: 0; color: #1a2232;">
              <strong th:text="#{pickup.time}">Hora:</strong> <span th:text="${tourTime}">{{ props.tourTime }}</span>
            </Text>
          </Section>

          <!-- Pickup Location -->
          <Section style="background-color: #dbeafe; border-left: 4px solid #3b82f6; border-radius: 6px; padding: 16px 20px; margin: 24px 0;">
            <Text
              style="margin: 0 0 8px 0; color: #1e40af; font-weight: 700; font-size: 16px;"
              th:text="#{pickup.location.title}"
            >
              📍 Punto de recogida
            </Text>
            <Text
              style="margin: 0; color: #1e40af;"
              th:text="${pickupLocation}"
            >
              {{ props.pickupLocation }}
            </Text>
          </Section>

          <!-- What to Bring -->
          <Section style="border-radius: 6px; padding: 16px 20px; margin: 24px 0; background-color: #f0fdf4; border-left: 4px solid #10b981;">
            <Text
              style="margin: 0 0 8px 0; color: #065f46; font-weight: 700;"
              th:text="#{pickup.equipment.title}"
            >
              Qué llevar
            </Text>
            <Text
              style="margin: 0; color: #065f46; white-space: pre-wrap;"
              th:text="${equipmentList}"
            >
              {{ props.equipmentList }}
            </Text>
          </Section>

          <!-- Important Notice -->
          <Section style="background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 6px; padding: 16px; margin: 24px 0;">
            <Text
              style="margin: 0; color: #92400e; font-weight: 600;"
              th:text="#{pickup.ready}"
            >
              Por favor, esté listo en el punto de recogida 5 minutos antes de la hora indicada.
            </Text>
          </Section>

          <!-- Emergency Contact -->
          <Text
            style="margin: 24px 0 8px 0; color: #1a2232; font-weight: 600;"
            th:text="#{pickup.emergency}"
          >
            Contacto de emergencia
          </Text>
          <Text style="margin: 0; padding: 12px; background-color: #f8fafc; border-radius: 6px; color: #1a2232; border: 1px solid #e2e8f0;">
            <a
              th:href="'tel:' + ${emergencyContact}"
              :href="'tel:' + props.emergencyContact"
              style="color: #fbaa62; text-decoration: none; font-weight: 600;"
              th:text="${emergencyContact}"
            >
              {{ props.emergencyContact }}
            </a>
          </Text>

          <Text style="margin: 32px 0 0 0; color: #fbaa62; font-size: 18px; font-weight: 600; text-align: center;">
            ¡Nos vemos pronto! 🌟
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
