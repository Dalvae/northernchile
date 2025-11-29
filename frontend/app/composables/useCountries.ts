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
    { value: 'CL', label: 'Chile', phoneCode: '+56' },
    { value: 'AR', label: 'Argentina', phoneCode: '+54' },
    { value: 'BR', label: 'Brasil', phoneCode: '+55' },
    { value: 'PE', label: 'Perú', phoneCode: '+51' },
    { value: 'BO', label: 'Bolivia', phoneCode: '+591' },
    { value: 'UY', label: 'Uruguay', phoneCode: '+598' },
    { value: 'PY', label: 'Paraguay', phoneCode: '+595' },
    { value: 'CO', label: 'Colombia', phoneCode: '+57' },
    { value: 'EC', label: 'Ecuador', phoneCode: '+593' },
    { value: 'VE', label: 'Venezuela', phoneCode: '+58' },
    { value: 'MX', label: 'México', phoneCode: '+52' },
    { value: 'US', label: 'Estados Unidos', phoneCode: '+1' },
    { value: 'CA', label: 'Canadá', phoneCode: '+1' },
    { value: 'ES', label: 'España', phoneCode: '+34' },
    { value: 'FR', label: 'Francia', phoneCode: '+33' },
    { value: 'DE', label: 'Alemania', phoneCode: '+49' },
    { value: 'IT', label: 'Italia', phoneCode: '+39' },
    { value: 'GB', label: 'Reino Unido', phoneCode: '+44' },
    { value: 'PT', label: 'Portugal', phoneCode: '+351' },
    { value: 'NL', label: 'Países Bajos', phoneCode: '+31' },
    { value: 'BE', label: 'Bélgica', phoneCode: '+32' },
    { value: 'CH', label: 'Suiza', phoneCode: '+41' },
    { value: 'AT', label: 'Austria', phoneCode: '+43' },
    { value: 'SE', label: 'Suecia', phoneCode: '+46' },
    { value: 'NO', label: 'Noruega', phoneCode: '+47' },
    { value: 'DK', label: 'Dinamarca', phoneCode: '+45' },
    { value: 'FI', label: 'Finlandia', phoneCode: '+358' },
    { value: 'PL', label: 'Polonia', phoneCode: '+48' },
    { value: 'CZ', label: 'República Checa', phoneCode: '+420' },
    { value: 'AU', label: 'Australia', phoneCode: '+61' },
    { value: 'NZ', label: 'Nueva Zelanda', phoneCode: '+64' },
    { value: 'JP', label: 'Japón', phoneCode: '+81' },
    { value: 'CN', label: 'China', phoneCode: '+86' },
    { value: 'KR', label: 'Corea del Sur', phoneCode: '+82' },
    { value: 'IN', label: 'India', phoneCode: '+91' },
    { value: 'RU', label: 'Rusia', phoneCode: '+7' },
    { value: 'ZA', label: 'Sudáfrica', phoneCode: '+27' },
    { value: 'IL', label: 'Israel', phoneCode: '+972' },
    { value: 'TR', label: 'Turquía', phoneCode: '+90' },
    { value: 'IQ', label: 'Irak', phoneCode: '+964' },
    { value: 'IE', label: 'Irlanda', phoneCode: '+353' },
    { value: 'GR', label: 'Grecia', phoneCode: '+30' },
    { value: 'HU', label: 'Hungría', phoneCode: '+36' },
    { value: 'RO', label: 'Rumania', phoneCode: '+40' },
    { value: 'UA', label: 'Ucrania', phoneCode: '+380' },
    { value: 'TH', label: 'Tailandia', phoneCode: '+66' },
    { value: 'SG', label: 'Singapur', phoneCode: '+65' },
    { value: 'MY', label: 'Malasia', phoneCode: '+60' },
    { value: 'ID', label: 'Indonesia', phoneCode: '+62' },
    { value: 'PH', label: 'Filipinas', phoneCode: '+63' },
    { value: 'VN', label: 'Vietnam', phoneCode: '+84' },
    { value: 'AE', label: 'Emiratos Árabes', phoneCode: '+971' },
    { value: 'SA', label: 'Arabia Saudita', phoneCode: '+966' },
    { value: 'EG', label: 'Egipto', phoneCode: '+20' },
    { value: 'MA', label: 'Marruecos', phoneCode: '+212' },
    { value: 'NG', label: 'Nigeria', phoneCode: '+234' },
    { value: 'KE', label: 'Kenia', phoneCode: '+254' },
    { value: 'CR', label: 'Costa Rica', phoneCode: '+506' },
    { value: 'PA', label: 'Panamá', phoneCode: '+507' },
    { value: 'GT', label: 'Guatemala', phoneCode: '+502' },
    { value: 'HN', label: 'Honduras', phoneCode: '+504' },
    { value: 'SV', label: 'El Salvador', phoneCode: '+503' },
    { value: 'NI', label: 'Nicaragua', phoneCode: '+505' },
    { value: 'DO', label: 'República Dominicana', phoneCode: '+1' },
    { value: 'CU', label: 'Cuba', phoneCode: '+53' },
    { value: 'PR', label: 'Puerto Rico', phoneCode: '+1' },
    { value: 'JM', label: 'Jamaica', phoneCode: '+1' },
    { value: 'TT', label: 'Trinidad y Tobago', phoneCode: '+1' },
    { value: 'IS', label: 'Islandia', phoneCode: '+354' },
    { value: 'LU', label: 'Luxemburgo', phoneCode: '+352' },
    { value: 'MT', label: 'Malta', phoneCode: '+356' },
    { value: 'CY', label: 'Chipre', phoneCode: '+357' },
    { value: 'HR', label: 'Croacia', phoneCode: '+385' },
    { value: 'SI', label: 'Eslovenia', phoneCode: '+386' },
    { value: 'SK', label: 'Eslovaquia', phoneCode: '+421' },
    { value: 'BG', label: 'Bulgaria', phoneCode: '+359' },
    { value: 'RS', label: 'Serbia', phoneCode: '+381' },
    { value: 'BA', label: 'Bosnia', phoneCode: '+387' },
    { value: 'AL', label: 'Albania', phoneCode: '+355' },
    { value: 'MK', label: 'Macedonia del Norte', phoneCode: '+389' },
    { value: 'EE', label: 'Estonia', phoneCode: '+372' },
    { value: 'LV', label: 'Letonia', phoneCode: '+371' },
    { value: 'LT', label: 'Lituania', phoneCode: '+370' },
    { value: 'TW', label: 'Taiwán', phoneCode: '+886' },
    { value: 'HK', label: 'Hong Kong', phoneCode: '+852' }
  ].sort((a, b) => a.label.localeCompare(b.label))

  // Phone codes sorted by most common for tourism (Chile first, then Latin America, then others)
  const phoneCodes = [
    { code: '+56', country: 'CL', label: 'Chile' },
    { code: '+54', country: 'AR', label: 'Argentina' },
    { code: '+55', country: 'BR', label: 'Brasil' },
    { code: '+51', country: 'PE', label: 'Perú' },
    { code: '+57', country: 'CO', label: 'Colombia' },
    { code: '+52', country: 'MX', label: 'México' },
    { code: '+1', country: 'US', label: 'USA/Canadá' },
    { code: '+44', country: 'GB', label: 'Reino Unido' },
    { code: '+34', country: 'ES', label: 'España' },
    { code: '+33', country: 'FR', label: 'Francia' },
    { code: '+49', country: 'DE', label: 'Alemania' },
    { code: '+39', country: 'IT', label: 'Italia' },
    { code: '+351', country: 'PT', label: 'Portugal' },
    { code: '+31', country: 'NL', label: 'Países Bajos' },
    { code: '+32', country: 'BE', label: 'Bélgica' },
    { code: '+41', country: 'CH', label: 'Suiza' },
    { code: '+43', country: 'AT', label: 'Austria' },
    { code: '+46', country: 'SE', label: 'Suecia' },
    { code: '+47', country: 'NO', label: 'Noruega' },
    { code: '+45', country: 'DK', label: 'Dinamarca' },
    { code: '+358', country: 'FI', label: 'Finlandia' },
    { code: '+354', country: 'IS', label: 'Islandia' },
    { code: '+48', country: 'PL', label: 'Polonia' },
    { code: '+420', country: 'CZ', label: 'Rep. Checa' },
    { code: '+36', country: 'HU', label: 'Hungría' },
    { code: '+40', country: 'RO', label: 'Rumania' },
    { code: '+30', country: 'GR', label: 'Grecia' },
    { code: '+353', country: 'IE', label: 'Irlanda' },
    { code: '+61', country: 'AU', label: 'Australia' },
    { code: '+64', country: 'NZ', label: 'Nueva Zelanda' },
    { code: '+81', country: 'JP', label: 'Japón' },
    { code: '+86', country: 'CN', label: 'China' },
    { code: '+82', country: 'KR', label: 'Corea del Sur' },
    { code: '+886', country: 'TW', label: 'Taiwán' },
    { code: '+852', country: 'HK', label: 'Hong Kong' },
    { code: '+65', country: 'SG', label: 'Singapur' },
    { code: '+60', country: 'MY', label: 'Malasia' },
    { code: '+66', country: 'TH', label: 'Tailandia' },
    { code: '+62', country: 'ID', label: 'Indonesia' },
    { code: '+63', country: 'PH', label: 'Filipinas' },
    { code: '+84', country: 'VN', label: 'Vietnam' },
    { code: '+91', country: 'IN', label: 'India' },
    { code: '+972', country: 'IL', label: 'Israel' },
    { code: '+971', country: 'AE', label: 'Emiratos' },
    { code: '+966', country: 'SA', label: 'Arabia Saudita' },
    { code: '+90', country: 'TR', label: 'Turquía' },
    { code: '+7', country: 'RU', label: 'Rusia' },
    { code: '+380', country: 'UA', label: 'Ucrania' },
    { code: '+27', country: 'ZA', label: 'Sudáfrica' },
    { code: '+20', country: 'EG', label: 'Egipto' },
    { code: '+212', country: 'MA', label: 'Marruecos' },
    { code: '+591', country: 'BO', label: 'Bolivia' },
    { code: '+593', country: 'EC', label: 'Ecuador' },
    { code: '+595', country: 'PY', label: 'Paraguay' },
    { code: '+598', country: 'UY', label: 'Uruguay' },
    { code: '+58', country: 'VE', label: 'Venezuela' },
    { code: '+506', country: 'CR', label: 'Costa Rica' },
    { code: '+507', country: 'PA', label: 'Panamá' },
    { code: '+502', country: 'GT', label: 'Guatemala' },
    { code: '+503', country: 'SV', label: 'El Salvador' },
    { code: '+504', country: 'HN', label: 'Honduras' },
    { code: '+505', country: 'NI', label: 'Nicaragua' },
    { code: '+53', country: 'CU', label: 'Cuba' },
    { code: '+352', country: 'LU', label: 'Luxemburgo' },
    { code: '+356', country: 'MT', label: 'Malta' },
    { code: '+357', country: 'CY', label: 'Chipre' },
    { code: '+385', country: 'HR', label: 'Croacia' },
    { code: '+386', country: 'SI', label: 'Eslovenia' },
    { code: '+421', country: 'SK', label: 'Eslovaquia' },
    { code: '+359', country: 'BG', label: 'Bulgaria' },
    { code: '+381', country: 'RS', label: 'Serbia' },
    { code: '+372', country: 'EE', label: 'Estonia' },
    { code: '+371', country: 'LV', label: 'Letonia' },
    { code: '+370', country: 'LT', label: 'Lituania' }
  ]

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

  /**
   * Get phone code from country ISO code (e.g., 'CL' -> '+56')
   */
  const getPhoneCodeByCountry = (countryCode: string | null | undefined): string => {
    if (!countryCode) return '+56'
    const country = countries.find(c => c.value.toUpperCase() === countryCode.toUpperCase())
    return country?.phoneCode || '+56'
  }

  /**
   * Get country ISO code from phone code (e.g., '+56' -> 'CL')
   * Returns first match for codes shared by multiple countries (e.g., +1)
   */
  const getCountryByPhoneCode = (phoneCode: string | null | undefined): string | null => {
    if (!phoneCode) return null
    const entry = phoneCodes.find(p => p.code === phoneCode)
    return entry?.country || null
  }

  return {
    countries,
    phoneCodes,
    getCountryLabel,
    getCountryFlag,
    getPhoneCodeByCountry,
    getCountryByPhoneCode
  }
}
