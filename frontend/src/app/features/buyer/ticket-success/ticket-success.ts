import { Component, OnInit, signal, inject } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { jsPDF } from 'jspdf';
import QRCode from 'qrcode';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-ticket-success',
  imports: [RouterModule],
  templateUrl: './ticket-success.html',
  styleUrl: './ticket-success.css'
})
export class TicketSuccess implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  orderId: string | null = null;
  isGeneratingPDF = signal<boolean>(false);

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('orderId');
  }

  async downloadPDF() {
    this.isGeneratingPDF.set(true);
    try {
      const pdf = new jsPDF();
      const user = this.authService.currentUser();
      const ticketData = JSON.stringify({
        orderId: this.orderId,
        buyer: user?.email,
        timestamp: new Date().toISOString()
      });
      const qrDataUrl = await QRCode.toDataURL(ticketData, { width: 150 });

      pdf.setFontSize(24);
      pdf.setTextColor('#4a4ae6');
      pdf.text('OrionTicket', 105, 30, { align: 'center' });
      pdf.setFontSize(16);
      pdf.setTextColor('#333333');
      pdf.text('ENTRADA OFICIAL', 105, 45, { align: 'center' });
      pdf.setFontSize(12);
      pdf.text(`Orden: ${this.orderId}`, 20, 70);
      pdf.text(`Titular: ${user?.email || 'Comprador'}`, 20, 80);
      pdf.text(`Fecha: ${new Date().toLocaleDateString()}`, 20, 90);
      pdf.addImage(qrDataUrl, 'PNG', 130, 60, 50, 50);
      pdf.setFontSize(10);
      pdf.text('Por favor, presenta este codigo QR en el evento.', 105, 130, { align: 'center' });
      pdf.save(`OrionTicket_${this.orderId}.pdf`);
    } catch (err) {
      console.error('Error al generar PDF:', err);
    } finally {
      this.isGeneratingPDF.set(false);
    }
  }
}
