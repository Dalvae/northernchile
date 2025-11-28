/**
 * Composable for handling currency formatting across the application
 *
 * Uses Intl.NumberFormat for proper internationalization and currency display
 * Never use parseFloat() for calculations - always delegate to backend
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/NumberFormat
 */
export const useCurrency = () => {
  const { locale } = useI18n()

  /**
   * Formats a price string or number for display in Chilean Pesos (CLP)
   *
   * @param priceValue - Price as string (from API) or number
   * @param currency - Currency code (default: 'CLP')
   * @returns Formatted price string (e.g., "$50.000" for CLP)
   *
   * @example
   * formatPrice("50000") // "$50.000"
   * formatPrice(50000.0000) // "$50.000"
   */
  const formatPrice = (
    priceValue: string | number | null | undefined,
    currency: string = 'CLP'
  ): string => {
    if (priceValue === null || priceValue === undefined) {
      return formatPrice(0, currency)
    }

    // Convert string to number safely (only for display, never for calculations!)
    const numericValue = typeof priceValue === 'string'
      ? parseFloat(priceValue)
      : priceValue

    // Handle invalid numbers
    if (isNaN(numericValue)) {
      console.warn(`Invalid price value: ${priceValue}`)
      return formatPrice(0, currency)
    }

    // Currency-specific formatting rules
    // Always show currency code (CLP) for clarity with international tourists
    const formatOptions: Intl.NumberFormatOptions = {
      style: 'currency',
      currency,
      currencyDisplay: 'code',
      // CLP doesn't use decimal places, but other currencies might
      minimumFractionDigits: currency === 'CLP' ? 0 : 2,
      maximumFractionDigits: currency === 'CLP' ? 0 : 2
    }

    // Use locale-specific formatting
    const localeCode = getLocaleCode(locale.value)

    try {
      return new Intl.NumberFormat(localeCode, formatOptions).format(numericValue)
    } catch (error) {
      console.error('Error formatting price:', error)
      // Fallback to basic formatting
      return `$${numericValue.toLocaleString(localeCode)}`
    }
  }

  /**
   * Maps i18n locale to Intl.NumberFormat locale code
   *
   * @param locale - Current i18n locale (es, en, pt)
   * @returns Locale code for NumberFormat (es-CL, en-US, pt-BR)
   */
  const getLocaleCode = (locale: string): string => {
    const localeMap: Record<string, string> = {
      es: 'es-CL',
      en: 'en-US',
      pt: 'pt-BR'
    }
    return localeMap[locale] || 'es-CL'
  }

  /**
   * Formats a price range for display
   *
   * @param minPrice - Minimum price
   * @param maxPrice - Maximum price
   * @param currency - Currency code
   * @returns Formatted range string
   *
   * @example
   * formatPriceRange("30000", "50000") // "$30.000 - $50.000"
   */
  const formatPriceRange = (
    minPrice: string | number,
    maxPrice: string | number,
    currency: string = 'CLP'
  ): string => {
    return `${formatPrice(minPrice, currency)} - ${formatPrice(maxPrice, currency)}`
  }

  /**
   * Formats a price with a "From" prefix for base prices
   *
   * @param price - Base price
   * @param currency - Currency code
   * @returns Formatted string with prefix
   *
   * @example
   * formatFromPrice("50000") // "Desde $50.000"
   */
  const formatFromPrice = (
    price: string | number,
    currency: string = 'CLP'
  ): string => {
    const { t } = useI18n()
    return `${t('tours.price_from')} ${formatPrice(price, currency)}`
  }

  return {
    formatPrice,
    formatPriceRange,
    formatFromPrice
  }
}
