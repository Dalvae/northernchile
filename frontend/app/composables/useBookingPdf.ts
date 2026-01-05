import { jsPDF } from 'jspdf'
import { parseDateOnly, CHILE_TIMEZONE } from '~/utils/dateUtils'
import { getLocaleCode } from '~/utils/localeUtils'
import type { BookingRes } from 'api-client'

export const useBookingPdf = () => {
  const { t, locale } = useI18n()
  const { formatPrice } = useCurrency()
  const { getCountryLabel } = useCountries()

  const formatDateTime = (dateString?: string, timeString?: string): string => {
    if (!dateString) return '-'

    // Parse date-only strings correctly (avoid UTC interpretation)
    const date = parseDateOnly(dateString)
    if (Number.isNaN(date.getTime())) return '-'

    const dateFormatted = new Intl.DateTimeFormat(getLocaleCode(locale.value), {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      timeZone: CHILE_TIMEZONE
    }).format(date)

    if (timeString) {
      return `${dateFormatted} - ${timeString}`
    }
    return dateFormatted
  }

  const getStatusLabel = (status?: string): string => {
    if (!status) return '-'
    const statusLabels: Record<string, Record<string, string>> = {
      CONFIRMED: { es: 'Confirmada', en: 'Confirmed', pt: 'Confirmada' },
      PENDING: { es: 'Pendiente', en: 'Pending', pt: 'Pendente' },
      CANCELLED: { es: 'Cancelada', en: 'Cancelled', pt: 'Cancelada' },
      COMPLETED: { es: 'Completada', en: 'Completed', pt: 'Concluida' }
    }
    return statusLabels[status]?.[locale.value] || status
  }

  const generateBookingPdf = async (booking: BookingRes): Promise<void> => {
    const doc = new jsPDF()
    const pageWidth = doc.internal.pageSize.getWidth()
    let yPosition = 20

    // Header
    doc.setFontSize(24)
    doc.setFont('helvetica', 'bold')
    doc.text('Northern Chile Tours', pageWidth / 2, yPosition, { align: 'center' })

    yPosition += 12
    doc.setFontSize(14)
    doc.setFont('helvetica', 'normal')
    doc.text(t('profile.booking_confirmation'), pageWidth / 2, yPosition, { align: 'center' })

    // Booking reference
    yPosition += 15
    doc.setFontSize(10)
    doc.setTextColor(100, 100, 100)
    const bookingId = booking.id || 'UNKNOWN'
    doc.text(`${t('profile.booking_reference')}: #${bookingId.substring(0, 8).toUpperCase()}`, pageWidth / 2, yPosition, { align: 'center' })
    doc.setTextColor(0, 0, 0)

    // Divider
    yPosition += 10
    doc.setDrawColor(200, 200, 200)
    doc.line(20, yPosition, pageWidth - 20, yPosition)

    // Tour information section
    yPosition += 15
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text(t('booking.tour_info'), 20, yPosition)

    yPosition += 10
    doc.setFontSize(11)
    doc.setFont('helvetica', 'normal')

    // Tour name
    doc.setFont('helvetica', 'bold')
    doc.text(booking.tourName || '-', 20, yPosition)
    doc.setFont('helvetica', 'normal')

    // Date and time
    yPosition += 8
    // tourStartTime is LocalTime (string) in API
    doc.text(`${t('booking.date')}: ${formatDateTime(booking.tourDate, booking.tourStartTime as string)}`, 20, yPosition)

    // Status
    yPosition += 8
    doc.text(`${t('booking.status_label')}: ${getStatusLabel(booking.status)}`, 20, yPosition)

    // Participants section
    if (booking.participants && booking.participants.length > 0) {
      yPosition += 15
      doc.setDrawColor(200, 200, 200)
      doc.line(20, yPosition, pageWidth - 20, yPosition)

      yPosition += 10
      doc.setFontSize(14)
      doc.setFont('helvetica', 'bold')
      doc.text(`${t('booking.participant_details')} (${booking.participants.length})`, 20, yPosition)

      yPosition += 8
      doc.setFontSize(10)
      doc.setFont('helvetica', 'normal')

      booking.participants.forEach((participant, index) => {
        // Check if we need a new page
        if (yPosition > 250) {
          doc.addPage()
          yPosition = 20
        }

        yPosition += 8
        doc.setFont('helvetica', 'bold')
        doc.text(`${index + 1}. ${participant.fullName || '-'}`, 25, yPosition)
        doc.setFont('helvetica', 'normal')

        yPosition += 6
        doc.text(`   ${t('booking.document')}: ${participant.documentId}`, 25, yPosition)

        if (participant.nationality) {
          yPosition += 6
          doc.text(`   ${t('booking.nationality')}: ${getCountryLabel(participant.nationality)}`, 25, yPosition)
        }

        if (participant.pickupAddress) {
          yPosition += 6
          const pickupLines = doc.splitTextToSize(`   ${t('booking.pickup_address')}: ${participant.pickupAddress}`, pageWidth - 50)
          doc.text(pickupLines, 25, yPosition)
          yPosition += (pickupLines.length - 1) * 5
        }

        if (participant.specialRequirements) {
          yPosition += 6
          const reqLines = doc.splitTextToSize(`   ${t('booking.special_requirements')}: ${participant.specialRequirements}`, pageWidth - 50)
          doc.text(reqLines, 25, yPosition)
          yPosition += (reqLines.length - 1) * 5
        }
      })
    }

    // Special requests for the whole booking
    if (booking.specialRequests) {
      yPosition += 12
      doc.setFont('helvetica', 'bold')
      doc.text(`${t('booking.special_requests')}:`, 20, yPosition)
      doc.setFont('helvetica', 'normal')
      yPosition += 6
      const specialLines = doc.splitTextToSize(booking.specialRequests, pageWidth - 45)
      doc.text(specialLines, 25, yPosition)
      yPosition += (specialLines.length - 1) * 5
    }

    // Payment section
    yPosition += 15
    doc.setDrawColor(200, 200, 200)
    doc.line(20, yPosition, pageWidth - 20, yPosition)

    yPosition += 10
    doc.setFontSize(14)
    doc.setFont('helvetica', 'bold')
    doc.text(t('booking.payment_summary'), 20, yPosition)

    yPosition += 10
    doc.setFontSize(11)
    doc.setFont('helvetica', 'normal')

    // Subtotal
    doc.text(t('common.subtotal'), 20, yPosition)
    doc.text(formatPrice(booking.subtotal), pageWidth - 20, yPosition, { align: 'right' })

    // Tax
    yPosition += 7
    doc.text(`${t('common.tax')} (19%)`, 20, yPosition)
    doc.text(formatPrice(booking.taxAmount), pageWidth - 20, yPosition, { align: 'right' })

    // Total
    yPosition += 10
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(12)
    doc.text(t('common.total'), 20, yPosition)
    doc.text(formatPrice(booking.totalAmount), pageWidth - 20, yPosition, { align: 'right' })

    // Footer
    yPosition += 20
    doc.setDrawColor(200, 200, 200)
    doc.line(20, yPosition, pageWidth - 20, yPosition)

    yPosition += 10
    doc.setFontSize(9)
    doc.setFont('helvetica', 'normal')
    doc.setTextColor(100, 100, 100)

    const footerText1 = t('profile.pdf_footer_contact')
    const footerText2 = t('profile.pdf_footer_thanks')

    doc.text(footerText1, pageWidth / 2, yPosition, { align: 'center' })
    yPosition += 6
    doc.text(footerText2, pageWidth / 2, yPosition, { align: 'center' })

    // Website
    yPosition += 10
    doc.setTextColor(59, 130, 246)
    doc.text('www.northernchiletours.com', pageWidth / 2, yPosition, { align: 'center' })

    // Download the PDF
    const fileName = `booking-${(booking.id || 'unknown').substring(0, 8)}.pdf`
    doc.save(fileName)
  }

  return {
    generateBookingPdf
  }
}
