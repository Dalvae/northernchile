// Payment types based on backend DTOs

export enum PaymentProvider {
  TRANSBANK = 'TRANSBANK',
  MERCADOPAGO = 'MERCADOPAGO',
  STRIPE = 'STRIPE'
}

export enum PaymentMethod {
  WEBPAY = 'WEBPAY',
  PIX = 'PIX',
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  BANK_TRANSFER = 'BANK_TRANSFER',
  WALLET = 'WALLET'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
  EXPIRED = 'EXPIRED'
}

export interface PaymentInitReq {
  bookingId: string
  provider: PaymentProvider
  paymentMethod: PaymentMethod
  amount: number
  currency?: string
  returnUrl?: string
  cancelUrl?: string
  userEmail?: string
  description?: string
  expirationMinutes?: number
}

export interface PaymentInitRes {
  paymentId: string
  status: PaymentStatus
  paymentUrl?: string
  detailsUrl?: string
  qrCode?: string
  pixCode?: string
  token?: string
  expiresAt?: string
  message?: string
}

export interface PaymentStatusRes {
  paymentId: string
  externalPaymentId?: string
  status: PaymentStatus
  amount: number
  currency: string
  message?: string
  updatedAt: string
}

export interface Payment {
  id: string
  bookingId: string
  provider: PaymentProvider
  paymentMethod: PaymentMethod
  externalPaymentId?: string
  status: PaymentStatus
  amount: number
  currency: string
  paymentUrl?: string
  detailsUrl?: string
  qrCode?: string
  pixCode?: string
  token?: string
  expiresAt?: string
  errorMessage?: string
  createdAt: string
  updatedAt: string
}
