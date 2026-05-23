import { Component, OnInit, signal, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-checkout',
  imports: [CommonModule],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css'
})
export class Checkout implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  eventId: string | null = null;
  dateId: string | null = null;

  // Estado del flujo
  isLoadingSeats = signal<boolean>(true);
  availableSeat = signal<any>(null);          // primer asiento disponible
  isProcessing = signal<boolean>(false);
  statusMessage = signal<string>('');          // mensaje de paso actual
  errorMessage = signal<string | null>(null);

  // Precio real del batch
  batchPrice = signal<number>(0);

  ngOnInit(): void {
    this.eventId = this.route.snapshot.paramMap.get('eventId');
    this.dateId = this.route.snapshot.paramMap.get('dateId');
    this.loadAvailableSeat();
  }

  /** Paso 0: obtener un asiento disponible para mostrar precio real */
  private loadAvailableSeat(): void {
    if (!this.eventId || !this.dateId) {
      this.isLoadingSeats.set(false);
      this.errorMessage.set('Faltan parámetros de evento o fecha.');
      return;
    }

    this.http.get<any[]>(
      `http://localhost:8080/v1/events/${this.eventId}/dates/${this.dateId}/seats`
    ).subscribe({
      next: (seats) => {
        const available = seats.find(s => s.status === 'AVAILABLE');
        if (available) {
          this.availableSeat.set(available);
        } else {
          this.errorMessage.set('No hay asientos disponibles para este evento.');
        }
        this.isLoadingSeats.set(false);
      },
      error: (err) => {
        console.error('Error loading seats', err);
        this.errorMessage.set('No se pudieron cargar los asientos disponibles. Intenta de nuevo.');
        this.isLoadingSeats.set(false);
      }
    });
  }

  processPayment(): void {
    const user = this.authService.currentUser();
    if (!user) {
      this.errorMessage.set('Sesión expirada. Por favor inicia sesión de nuevo.');
      return;
    }

    const seat = this.availableSeat();
    if (!seat) {
      this.errorMessage.set('No hay asientos disponibles.');
      return;
    }

    this.isProcessing.set(true);
    this.errorMessage.set(null);
    this.statusMessage.set('Reservando asiento...');

    // Paso 1: Crear reserva
    const reservationPayload = {
      seatId: seat.seatId,
      buyerId: user.sub,
      eventId: this.eventId,
      dateId: this.dateId,
      batchId: seat.batchId
    };

    this.http.post<any>('http://localhost:8080/v1/reservations', reservationPayload).subscribe({
      next: (reservation) => {
        this.statusMessage.set('Asiento reservado. Creando orden...');
        // Paso 2: esperar brevemente a que el snapshot de RabbitMQ llegue al orders-service
        setTimeout(() => this.createOrder(reservation.reservationId, user), 2500);
      },
      error: (err) => {
        console.error('Error creating reservation', err);
        this.isProcessing.set(false);
        this.statusMessage.set('');
        this.errorMessage.set(
          err.status === 409
            ? 'Este asiento acaba de ser tomado por otro comprador. Intenta de nuevo.'
            : 'Error al reservar el asiento. Por favor intenta de nuevo.'
        );
      }
    });
  }

  private createOrder(reservationId: string, user: any): void {
    const orderPayload = {
      buyerId: user.sub,
      eventId: this.eventId,
      dateId: this.dateId,
      reservationId: reservationId
    };

    this.http.post<any>('http://localhost:8080/v1/orders', orderPayload).subscribe({
      next: (order) => {
        this.statusMessage.set('Orden creada. Procesando pago...');
        this.processPaymentStep(order.orderId, user);
      },
      error: (err) => {
        console.error('Error creating order', err);
        this.isProcessing.set(false);
        this.statusMessage.set('');
        this.errorMessage.set('Error al crear la orden. La reserva puede haber expirado. Intenta de nuevo.');
      }
    });
  }

  private processPaymentStep(orderId: string, user: any): void {
    const paymentPayload = {
      orderId: orderId,
      buyerId: user.sub,
      method: 'CARD',
      paymentDetails: {
        gatewayToken: 'tok_visa_test_' + Math.random().toString(36).slice(2)
      }
    };

    this.http.post<any>('http://localhost:8080/v1/payments', paymentPayload).subscribe({
      next: (payment) => {
        this.isProcessing.set(false);
        this.statusMessage.set('');
        this.router.navigate(['/buyer/success', orderId]);
      },
      error: (err) => {
        console.error('Error processing payment', err);
        this.isProcessing.set(false);
        this.statusMessage.set('');
        this.errorMessage.set('Error al procesar el pago. La orden fue creada pero el pago falló. Contacta soporte.');
      }
    });
  }
}
