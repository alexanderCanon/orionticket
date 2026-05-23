import { Component, OnInit, signal, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-payment',
  imports: [CommonModule, FormsModule],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})
export class Payment implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  orderId: string | null = null;
  order = signal<any>(null);
  
  isLoading = signal<boolean>(true);
  isProcessing = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  paymentData = {
    cardName: '',
    cardNumber: '',
    expiry: '',
    cvv: ''
  };

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('orderId');
    this.loadOrder();
  }

  private loadOrder(): void {
    if (!this.orderId) {
      this.isLoading.set(false);
      this.errorMessage.set('ID de orden no proporcionado.');
      return;
    }

    this.http.get<any>(`http://localhost:8080/v1/orders/${this.orderId}`).subscribe({
      next: (order) => {
        this.order.set(order);
        this.isLoading.set(false);
        if (order.status !== 'CREATED') {
          this.errorMessage.set(`Esta orden ya no puede ser pagada (Estado: ${order.status}).`);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('No se pudo cargar la información de la orden.');
      }
    });
  }

  processPayment(): void {
    const user = this.authService.currentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    if (!this.orderId || this.order()?.status !== 'CREATED') return;

    this.isProcessing.set(true);
    this.errorMessage.set(null);

    const paymentPayload = {
      orderId: this.orderId,
      buyerId: user.sub,
      method: 'CARD',
      paymentDetails: {
        // En un escenario real aquí se usaría Stripe.js o similar para tokenizar la tarjeta.
        // Aquí enviamos un token simulado para el MVP.
        gatewayToken: 'tok_visa_test_' + Math.random().toString(36).slice(2)
      }
    };

    this.http.post<any>('http://localhost:8080/v1/payments', paymentPayload).subscribe({
      next: () => {
        this.isProcessing.set(false);
        this.router.navigate(['/buyer/success', this.orderId]);
      },
      error: (err) => {
        console.error('Error processing payment', err);
        this.isProcessing.set(false);
        this.errorMessage.set('Error al procesar el pago. Por favor verifica tus datos o intenta con otra tarjeta.');
      }
    });
  }
}
