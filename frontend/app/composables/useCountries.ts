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
    { value: 'TR', label: 'Turquía' }
  ].sort((a, b) => a.label.localeCompare(b.label));

  return {
    countries
  };
}
