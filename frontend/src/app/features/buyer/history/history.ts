import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { TicketPdfService } from '../../../core/services/ticket-pdf';

@Component({
  selector: 'app-history',
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './history.html',
  styleUrl: './history.css'
})
export class History implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private ticketPdf = inject(TicketPdfService);
  private router = inject(Router);

  myOrders = signal<any[]>([]);
  isLoading = signal<boolean>(true);
  loadError = signal<string | null>(null);
  downloadingId = signal<string | null>(null);

  // Filters
  selectedStatus = signal<string>('CONFIRMED');
  selectedEvent = signal<string>('ALL');

  // Derived unique events for the filter dropdown
  availableEvents = computed(() => {
    const orders = this.myOrders();
    const events = orders.map(o => o.eventName);
    return [...new Set(events)];
  });

  // Derived filtered and sorted orders
  filteredOrders = computed(() => {
    let orders = this.myOrders();
    
    // Filter by status
    if (this.selectedStatus() !== 'ALL') {
      orders = orders.filter(o => o.status === this.selectedStatus());
    }
    
    // Filter by event
    if (this.selectedEvent() !== 'ALL') {
      orders = orders.filter(o => o.eventName === this.selectedEvent());
    }
    
    // Sort by date (newest first / closest to present)
    return orders.sort((a, b) => {
      const dateA = new Date(a.purchaseDate).getTime();
      const dateB = new Date(b.purchaseDate).getTime();
      return dateB - dateA;
    });
  });

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.http.get<any>(`http://localhost:8080/v1/buyers/${user.sub}/orders`).subscribe({
        next: (res) => {
          const orders = res.orders || [];
          const mapped = orders.map((o: any) => ({
            id: o.orderId,
            eventId: o.eventId,
            dateId: o.dateId,
            seatId: o.seatId,
            eventName: 'Evento #' + (o.eventId?.substring(0, 8) || '?'),
            seatLabel: o.seatId ? 'Asiento #' + o.seatId.substring(0, 8) : '',
            purchaseDate: o.createdAt,
            status: o.status,
            total: o.total
          }));
          this.myOrders.set(mapped);
          this.isLoading.set(false);

          // Fetch Event Names
          const uniqueEvents = [...new Set(mapped.map((o: any) => o.eventId))];
          uniqueEvents.forEach(eventId => {
            if (!eventId) return;
            this.http.get<any>(`http://localhost:8080/v1/catalog/events/${eventId}`).subscribe({
              next: (event) => {
                this.myOrders.update(current => current.map(o => 
                  o.eventId === eventId ? { ...o, eventName: event.name } : o
                ));
              },
              error: (err) => console.error('Error fetching event details', err)
            });
          });

          // Fetch Seat Labels
          const uniqueEventDates = [...new Set(mapped.filter((o: any) => o.eventId && o.dateId).map((o: any) => `${o.eventId}|${o.dateId}`))];
          uniqueEventDates.forEach(key => {
            const [eventId, dateId] = (key as string).split('|');
            this.http.get<any[]>(`http://localhost:8080/v1/events/${eventId}/dates/${dateId}/seats`).subscribe({
              next: (seats) => {
                this.myOrders.update(current => current.map(o => {
                  if (o.eventId === eventId && o.dateId === dateId) {
                    const seat = seats.find((s: any) => s.seatId === o.seatId);
                    if (seat) {
                      const label = seat.row && seat.seatNumber ? `${seat.row}-${seat.seatNumber}` : seat.seatNumber || seat.seatId.substring(0, 8);
                      return { ...o, seatLabel: 'Asiento ' + label };
                    }
                  }
                  return o;
                }));
              },
              error: (err) => console.error('Error fetching seats', err)
            });
          });
        },
        error: (err) => {
          console.error('Error loading orders', err);
          this.loadError.set('No se pudieron cargar tus compras. Por favor intenta de nuevo.');
          this.isLoading.set(false);
        }
      });
    } else {
      this.isLoading.set(false);
    }
  }

  async downloadTicket(order: any) {
    this.downloadingId.set(order.id);
    const email = this.authService.currentUser()?.email || 'Comprador';
    await this.ticketPdf.generateTicket(order, email);
    this.downloadingId.set(null);
  }

  payOrder(orderId: string) {
    this.router.navigate(['/buyer/payment', orderId]);
  }
}
