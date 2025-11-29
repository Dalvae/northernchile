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
  userName?: string
  resetUrl?: string
}>(), {
  userName: 'Diego Alvarez',
  resetUrl: 'https://www.northernchile.com/auth/reset-password?token=abc123'
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
            th:text="#{email.password.reset.title}"
          >
            Restablece tu contraseña
          </Heading>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.password.reset.greeting(${userName})}"
          >
            Hola <span>{{ props.userName }}</span>,
          </Text>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.password.reset.body}"
          >
            Recibimos una solicitud para restablecer tu contraseña. Haz clic en el botón de abajo para elegir una nueva contraseña:
          </Text>

          <Section style="text-align: center; margin: 32px 0;">
            <a
              th:href="${resetUrl}"
              :href="props.resetUrl"
              style="display: inline-block; background-color: #fbaa62; color: #1a2232; padding: 14px 32px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 16px;"
              th:text="#{email.password.reset.button}"
            >
              Restablecer Contraseña
            </a>
          </Section>

          <Text
            style="margin: 24px 0 8px 0; color: #64748b; font-size: 14px;"
            th:text="#{email.password.reset.alternative}"
          >
            O copia y pega este enlace en tu navegador:
          </Text>

          <Text style="margin: 0; padding: 12px; background-color: #f8fafc; border-radius: 6px; word-break: break-all; font-size: 14px; color: #1a2232; border: 1px solid #e2e8f0;">
            <a
              th:href="${resetUrl}"
              :href="props.resetUrl"
              style="color: #fbaa62; text-decoration: none;"
            >
              {{ props.resetUrl }}
            </a>
          </Text>

          <Section style="background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 6px; padding: 16px; margin: 24px 0;">
            <Text
              style="margin: 0; color: #92400e; font-size: 14px;"
              th:text="#{email.password.reset.expiration}"
            >
              Este enlace expirará en 2 horas.
            </Text>
          </Section>

          <Text
            style="margin: 24px 0 0 0; color: #64748b; font-size: 14px;"
            th:text="#{email.password.reset.ignore}"
          >
            Si no solicitaste restablecer tu contraseña, por favor ignora este correo o contáctanos si tienes dudas.
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
