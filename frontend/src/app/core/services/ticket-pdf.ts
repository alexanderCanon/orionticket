import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import QRCode from 'qrcode';

@Injectable({
  providedIn: 'root'
})
export class TicketPdfService {
  async generateTicket(order: any, email: string): Promise<void> {
    try {
      const pdf = new jsPDF();
      
      const ticketData = JSON.stringify({
        orderId: order.id,
        buyer: email,
        timestamp: new Date().toISOString()
      });

      const qrDataUrl = await QRCode.toDataURL(ticketData, { width: 150 });

      pdf.setFontSize(24);
      pdf.setTextColor('#4a4ae6'); // Primary color
      pdf.text('OrionTicket', 105, 30, { align: 'center' });

      pdf.setFontSize(16);
      pdf.setTextColor('#333333');
      pdf.text('ENTRADA OFICIAL', 105, 45, { align: 'center' });

      pdf.setFontSize(12);
      pdf.text(`Evento: ${order.eventName}`, 20, 70);
      pdf.text(`Ubicación: ${order.seatLabel}`, 20, 80);
      pdf.text(`Orden: ${order.id}`, 20, 90);
      pdf.text(`Titular: ${email}`, 20, 100);
      pdf.text(`Fecha Compra: ${new Date(order.purchaseDate).toLocaleDateString()}`, 20, 110);

      pdf.addImage(qrDataUrl, 'PNG', 130, 70, 50, 50);

      pdf.setFontSize(10);
      pdf.text('Por favor, presenta este código QR en la entrada del evento.', 105, 140, { align: 'center' });

      pdf.save(`OrionTicket_${order.id}.pdf`);

    } catch (err) {
      console.error('Error al generar PDF:', err);
    }
  }
}
