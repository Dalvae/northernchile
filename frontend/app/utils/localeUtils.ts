/**
 * Locale utilities for mapping i18n locale codes to full locale codes.
 */

export const LOCALE_MAP = {
  es: 'es-CL',
  en: 'en-US',
  pt: 'pt-BR'
} as const

export type LocaleCode = keyof typeof LOCALE_MAP
export type FullLocaleCode = typeof LOCALE_MAP[LocaleCode]

/**
 * Maps i18n locale to full locale code.
 * @param locale - Current i18n locale (es, en, pt)
 * @returns Full locale code (es-CL, en-US, pt-BR)
 */
export function getLocaleCode(locale: string): FullLocaleCode {
  return LOCALE_MAP[locale as LocaleCode] || 'es-CL'
}
