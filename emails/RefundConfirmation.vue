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
  refundAmount?: string
  paymentMethod?: string
}>(), {
  customerName: 'Diego Alvarez',
  refundAmount: '$150.000 CLP',
  paymentMethod: 'Tarjeta Visa ****1234'
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
            style="color: #10b981; font-size: 24px; font-weight: 700; margin: 0 0 20px 0;"
            th:text="#{email.refund.title}"
          >
            Reembolso Confirmado
          </Heading>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.refund.greeting(${customerName})}"
          >
            Hola <span>{{ props.customerName }}</span>,
          </Text>

          <Text
            style="margin: 16px 0; color: #374151;"
            th:text="#{email.refund.body}"
          >
            Tu reembolso ha sido procesado exitosamente.
          </Text>

          <Section style="background-color: #f8fafc; border-left: 4px solid #10b981; border-radius: 8px; padding: 20px 24px; margin: 24px 0;">
            <Text
              style="margin: 0 0 16px 0; color: #1a2232; font-size: 18px; font-weight: 700;"
              th:text="#{email.refund.details}"
            >
              Detalles del Reembolso
            </Text>

            <Text style="margin: 0 0 12px 0; color: #1a2232; font-size: 16px;">
              <strong th:text="#{email.refund.amount}">Monto reembolsado:</strong> <span
                style="color: #10b981; font-weight: 700;"
                th:text="${refundAmount}"
              >{{ props.refundAmount }}</span>
            </Text>

            <Text style="margin: 0; color: #1a2232; font-size: 16px;">
              <strong th:text="#{email.refund.method}">Método de pago:</strong> <span th:text="${paymentMethod}">{{ props.paymentMethod }}</span>
            </Text>
          </Section>

          <Section style="background-color: #dbeafe; border-left: 4px solid #3b82f6; border-radius: 6px; padding: 16px; margin: 24px 0;">
            <Text
              style="margin: 0; color: #1e40af; font-size: 14px;"
              th:text="#{email.refund.timing}"
            >
              Por favor espera de 5 a 10 días hábiles para que el reembolso aparezca en tu cuenta.
            </Text>
          </Section>

          <Text
            style="margin: 24px 0 0 0; color: #fbaa62; font-size: 16px; font-weight: 600; text-align: center;"
            th:text="#{email.refund.future}"
          >
            ¡Esperamos verte en una futura aventura!
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
