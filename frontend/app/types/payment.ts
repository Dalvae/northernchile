import {
  PaymentSessionReqProviderEnum,
  PaymentSessionReqPaymentMethodEnum,
  PaymentStatusResStatusEnum,
  type PaymentSessionReq,
  type PaymentSessionRes,
  type PaymentRes,
  type PaymentStatusRes
} from 'api-client'

export const PaymentProvider = PaymentSessionReqProviderEnum
export type PaymentProvider = typeof PaymentSessionReqProviderEnum[keyof typeof PaymentSessionReqProviderEnum]

export const PaymentMethod = PaymentSessionReqPaymentMethodEnum
export type PaymentMethod = typeof PaymentSessionReqPaymentMethodEnum[keyof typeof PaymentSessionReqPaymentMethodEnum]

export const PaymentStatus = PaymentStatusResStatusEnum
export type PaymentStatus = typeof PaymentStatusResStatusEnum[keyof typeof PaymentStatusResStatusEnum]

// Re-export API types
export type { PaymentSessionReq, PaymentSessionRes, PaymentRes, PaymentStatusRes }

// Alias for backward compatibility (if needed) or just use PaymentRes
export type Payment = PaymentRes
