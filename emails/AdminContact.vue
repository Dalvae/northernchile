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
  name?: string
  email?: string
  phone?: string
  message?: string
  createdAt?: string
  messageId?: string
}>(), {
  name: 'Juan Pérez',
  email: 'cliente@email.com',
  phone: '+56 9 1234 5678',
  message: 'Hola, me gustaría saber más información sobre sus tours astronómicos...',
  createdAt: '2025-01-15 15:30',
  messageId: 'MSG-12345'
})
</script>

<template>
  <Html lang="es">
    <Head />
    <Body style="background-color: #f5f5f5; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">
      <Container style="background-color: #ffffff; margin: 40px auto; padding: 0; max-width: 600px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">
        <!-- Admin Header - Orange gradient -->
        <Section style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); padding: 40px 20px; text-align: center;">
          <Heading
            as="h1"
            style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 700;"
          >
            📬 Nuevo Mensaje de Contacto
          </Heading>
        </Section>

        <Section style="padding: 40px 30px;">
          <Text style="margin: 0 0 24px 0; color: #374151; font-size: 16px;">
            Se ha recibido un nuevo mensaje desde el formulario de contacto.
          </Text>

          <!-- Contact Info -->
          <Section style="background-color: #fffbeb; border-left: 4px solid #f59e0b; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 16px 0; color: #92400e; font-size: 18px;"
            >
              Información del Contacto
            </Heading>

            <Text style="margin: 0 0 10px 0; color: #1a2232;">
              <strong style="display: inline-block; min-width: 100px;">Nombre:</strong>
              <span th:text="${name}">{{ props.name }}</span>
            </Text>

            <Text style="margin: 0 0 10px 0; color: #1a2232;">
              <strong style="display: inline-block; min-width: 100px;">Email:</strong>
              <a
                th:href="'mailto:' + ${email}"
                :href="'mailto:' + props.email"
                style="color: #f59e0b; text-decoration: none;"
                th:text="${email}"
              >
                {{ props.email }}
              </a>
            </Text>

            <div th:if="${phone != null and !phone.isEmpty()}">
              <Text style="margin: 0; color: #1a2232;">
                <strong style="display: inline-block; min-width: 100px;">Teléfono:</strong>
                <a
                  th:href="'tel:' + ${phone}"
                  :href="'tel:' + props.phone"
                  style="color: #f59e0b; text-decoration: none;"
                  th:text="${phone}"
                >
                  {{ props.phone }}
                </a>
              </Text>
            </div>
          </Section>

          <!-- Message -->
          <Section style="background-color: #f3f4f6; border-left: 4px solid #6b7280; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Heading
              as="h3"
              style="margin: 0 0 12px 0; color: #374151; font-size: 18px;"
            >
              Mensaje
            </Heading>
            <Text
              style="margin: 0; color: #1f2937; white-space: pre-wrap;"
              th:text="${message}"
            >
              {{ props.message }}
            </Text>
          </Section>

          <!-- Metadata -->
          <Text style="margin: 24px 0 0 0; padding-top: 24px; border-top: 1px solid #e5e7eb; color: #6b7280; font-size: 12px;">
            <strong>Recibido:</strong> <span th:text="${createdAt}">{{ props.createdAt }}</span><br>
            <strong>ID:</strong> <span th:text="${messageId}">{{ props.messageId }}</span>
          </Text>

          <!-- Quick Actions -->
          <Section style="text-align: center; margin: 32px 0;">
            <a
              th:href="'mailto:' + ${email}"
              :href="'mailto:' + props.email"
              style="display: inline-block; background-color: #f59e0b; color: #ffffff; padding: 12px 28px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 14px; margin: 0 8px 8px 0;"
            >
              Responder
            </a>
            <a
              href="https://www.northernchile.com/admin/messages"
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
        </Section>
      </Container>
    </Body>
  </Html>
</template>
