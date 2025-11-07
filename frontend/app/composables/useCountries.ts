/**
 * Converts a country code (ISO 3166-1 alpha-2) to a flag emoji
 * Uses Unicode Regional Indicator Symbols
 */
function countryCodeToFlag(code: string): string {
  if (!code || code.length !== 2) return ''

  const codePoints = code
    .toUpperCase()
    .split('')
    .map(char => 127397 + char.charCodeAt(0))

  return String.fromCodePoint(...codePoints)
}

export function useCountries() {
  const countries = [
    { value: 'CL', label: 'Chile' },
    { value: 'AR', label: 'Argentina' },
    { value: 'BR', label: 'Brasil' },
    { value: 'PE', label: 'Perú' },
    { value: 'BO', label: 'Bolivia' },
    { value: 'UY', label: 'Uruguay' },
    { value: 'PY', label: 'Paraguay' },
    { value: 'CO', label: 'Colombia' },
    { value: 'EC', label: 'Ecuador' },
    { value: 'VE', label: 'Venezuela' },
    { value: 'MX', label: 'México' },
    { value: 'US', label: 'Estados Unidos' },
    { value: 'CA', label: 'Canadá' },
    { value: 'ES', label: 'España' },
    { value: 'FR', label: 'Francia' },
    { value: 'DE', label: 'Alemania' },
    { value: 'IT', label: 'Italia' },
    { value: 'GB', label: 'Reino Unido' },
    { value: 'PT', label: 'Portugal' },
    { value: 'NL', label: 'Países Bajos' },
    { value: 'BE', label: 'Bélgica' },
    { value: 'CH', label: 'Suiza' },
    { value: 'AT', label: 'Austria' },
    { value: 'SE', label: 'Suecia' },
    { value: 'NO', label: 'Noruega' },
    { value: 'DK', label: 'Dinamarca' },
    { value: 'FI', label: 'Finlandia' },
    { value: 'PL', label: 'Polonia' },
    { value: 'CZ', label: 'República Checa' },
    { value: 'AU', label: 'Australia' },
    { value: 'NZ', label: 'Nueva Zelanda' },
    { value: 'JP', label: 'Japón' },
    { value: 'CN', label: 'China' },
    { value: 'KR', label: 'Corea del Sur' },
    { value: 'IN', label: 'India' },
    { value: 'RU', label: 'Rusia' },
    { value: 'ZA', label: 'Sudáfrica' },
    { value: 'IL', label: 'Israel' },
    { value: 'TR', label: 'Turquía' },
    { value: 'IQ', label: 'Irak' },
    { value: 'IE', label: 'Irlanda' },
    { value: 'GR', label: 'Grecia' }
  ].sort((a, b) => a.label.localeCompare(b.label))

  /**
   * Get country label from code or name
   * Handles both ISO codes (CL, AR) and full names (Chile, Argentina)
   */
  const getCountryLabel = (code: string | null | undefined): string => {
    if (!code) return ''

    // Si tiene más de 2 caracteres, probablemente es el nombre completo
    if (code.length > 2) {
      return code
    }

    // Buscar por código ISO
    const country = countries.find(c => c.value.toUpperCase() === code.toUpperCase())
    return country?.label || code
  }

  /**
   * Get country flag emoji from code or name
   * Handles both ISO codes (CL, AR) and full names (Chile, Argentina)
   */
  const getCountryFlag = (code: string | null | undefined): string => {
    if (!code) return ''

    // Si tiene más de 2 caracteres, buscar el código por nombre
    if (code.length > 2) {
      const country = countries.find(c => c.label.toLowerCase() === code.toLowerCase())
      if (country) {
        return countryCodeToFlag(country.value)
      }
      return ''
    }

    // Es un código ISO de 2 letras
    return countryCodeToFlag(code)
  }

  return {
    countries,
    getCountryLabel,
    getCountryFlag
  }
}
