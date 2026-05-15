export const CONTACT_EMAIL = 'contacto@northernchile.com'
export const CONTACT_PHONE_DISPLAY = '+56 9 2201 4902'
export const CONTACT_PHONE_E164 = '56922014902'
export const CONTACT_PHONE_TEL = `+${CONTACT_PHONE_E164}`

export const INSTAGRAM_URL = 'https://www.instagram.com/northernchilespa/'
export const FACEBOOK_URL = 'https://www.facebook.com/northernchilespa'
export const TRIPADVISOR_URL = 'https://www.tripadvisor.com.br/Attraction_Review-g303681-d34296499-Reviews-Northern_Chile-San_Pedro_de_Atacama_Antofagasta_Region.html'
export const WHATSAPP_URL = `https://wa.me/${CONTACT_PHONE_E164}`

export interface SocialLink {
  label: string
  icon: string
  url: string
}

export const PUBLIC_SOCIAL_LINKS: SocialLink[] = [
  {
    label: 'Instagram',
    icon: 'i-simple-icons-instagram',
    url: INSTAGRAM_URL
  },
  {
    label: 'Facebook',
    icon: 'i-simple-icons-facebook',
    url: FACEBOOK_URL
  },
  {
    label: 'TripAdvisor',
    icon: 'i-simple-icons-tripadvisor',
    url: TRIPADVISOR_URL
  }
]

export const CONTACT_SOCIAL_LINKS: SocialLink[] = [
  ...PUBLIC_SOCIAL_LINKS,
  {
    label: 'WhatsApp',
    icon: 'i-simple-icons-whatsapp',
    url: WHATSAPP_URL
  }
]
