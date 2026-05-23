import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryUtils } from '../../../shared/utils/category.utils';

interface Seat {
  seatId: string;
  batchId: string;
  zone: string | null;
  section: string | null;
  row: string | null;
  seatNumber: string | null;
  type: string;
  status: string;
}

interface SeatDisplay extends Seat {
  displayNumber: number;
  x: number;
  y: number;
}

interface EventDate {
  dateId: string;
  scheduledAt: string;
  venueName: string;
  availableSeats: number;
}

@Component({
  selector: 'app-seat-picker',
  imports: [CommonModule],
  templateUrl: './seat-picker.html',
  styleUrl: './seat-picker.css'
})
export class SeatPicker implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  eventId: string | null = null;
  eventName = signal<string>('');
  eventCategory = signal<string>('');
  dates = signal<EventDate[]>([]);
  selectedDateId = signal<string | null>(null);
  seats = signal<SeatDisplay[]>([]);
  selectedSeatIds = signal<Set<string>>(new Set());

  isLoadingEvent = signal<boolean>(true);
  isLoadingSeats = signal<boolean>(false);
  isReserving = signal<boolean>(false);
  errorMessage = signal<string | null>(null);
  statusMessage = signal<string>('');

  readonly MAX_SEATS = 1;

  isSports = computed(() => this.eventCategory().toLowerCase().includes('deporte'));

  selectedSeats = computed(() => {
    const ids = this.selectedSeatIds();
    return this.seats().filter(s => ids.has(s.seatId));
  });

  selectedCount = computed(() => this.selectedSeatIds().size);

  ngOnInit(): void {
    this.eventId = this.route.snapshot.paramMap.get('eventId');
    const dateId = this.route.snapshot.paramMap.get('dateId');
    this.loadEvent(dateId);
  }

  private loadEvent(initialDateId: string | null): void {
    if (!this.eventId) {
      this.isLoadingEvent.set(false);
      this.errorMessage.set('Evento no encontrado.');
      return;
    }

    this.http.get<any>(`http://localhost:8080/v1/catalog/events/${this.eventId}`).subscribe({
      next: (event) => {
        this.eventName.set(event.name || 'Evento');
        this.eventCategory.set(event.category || 'Otros');
        this.dates.set(event.dates || []);
        const dateToSelect = initialDateId || (event.dates?.[0]?.dateId ?? null);
        if (dateToSelect) {
          this.selectDate(dateToSelect);
        }
        this.isLoadingEvent.set(false);
      },
      error: () => {
        this.isLoadingEvent.set(false);
        this.errorMessage.set('No se pudo cargar el evento.');
      }
    });
  }

  selectDate(dateId: string): void {
    if (this.selectedDateId() === dateId) return;
    this.selectedDateId.set(dateId);
    this.selectedSeatIds.set(new Set());
    this.seats.set([]);
    this.errorMessage.set(null);
    this.loadSeats(dateId);
  }

  private loadSeats(dateId: string): void {
    this.isLoadingSeats.set(true);
    this.http.get<Seat[]>(
      `http://localhost:8080/v1/events/${this.eventId}/dates/${dateId}/seats`
    ).subscribe({
      next: (seats) => {
        const processedSeats = this.calculateSeatLayout(seats);
        this.seats.set(processedSeats);
        this.isLoadingSeats.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar los asientos.');
        this.isLoadingSeats.set(false);
      }
    });
  }

  private calculateSeatLayout(rawSeats: Seat[]): SeatDisplay[] {
    const isSport = this.isSports();
    const result: SeatDisplay[] = [];
    
    // Sort seats by seatId or just use index to assign display numbers
    const seats = [...rawSeats];

    if (isSport) {
      // 360-degree circular layout
      const rings = [20, 30, 50]; // Seats per ring
      const radii = [140, 200, 260];
      const cx = 350; // Center X of container
      const cy = 350; // Center Y of container

      let seatIndex = 0;
      for (let r = 0; r < rings.length; r++) {
        const ringSeats = rings[r];
        const radius = radii[r];
        for (let i = 0; i < ringSeats; i++) {
          if (seatIndex >= seats.length) break;
          const angle = (i / ringSeats) * 2 * Math.PI;
          const x = cx + radius * Math.cos(angle);
          const y = cy + radius * Math.sin(angle);
          
          result.push({
            ...seats[seatIndex],
            displayNumber: seatIndex + 1,
            x: x - 15, // Offset by half seat width/height
            y: y - 15
          });
          seatIndex++;
        }
      }
      
      // Handle remaining seats if > 100
      while (seatIndex < seats.length) {
         result.push({
            ...seats[seatIndex],
            displayNumber: seatIndex + 1,
            x: cx + 320 * Math.cos((seatIndex) * 0.2),
            y: cy + 320 * Math.sin((seatIndex) * 0.2)
          });
          seatIndex++;
      }
      
    } else {
      // 180-degree semi-circular (amphitheater) layout
      const rings = [15, 20, 25, 40]; 
      const radii = [120, 180, 240, 300];
      const cx = 350; // Center X
      const cy = 400; // Center Y (at the bottom of the amphitheater)

      let seatIndex = 0;
      for (let r = 0; r < rings.length; r++) {
        const ringSeats = rings[r];
        const radius = radii[r];
        for (let i = 0; i < ringSeats; i++) {
          if (seatIndex >= seats.length) break;
          // Math.PI to 2*Math.PI goes from left (-x) to right (+x) over the top (-y)
          const angle = Math.PI + (i / (ringSeats - 1)) * Math.PI;
          const x = cx + radius * Math.cos(angle);
          const y = cy + radius * Math.sin(angle);
          
          result.push({
            ...seats[seatIndex],
            displayNumber: seatIndex + 1,
            x: x - 15,
            y: y - 15
          });
          seatIndex++;
        }
      }

      // Handle remaining seats if > 100
      while (seatIndex < seats.length) {
         const angle = Math.PI + ((seatIndex % 50) / 49) * Math.PI;
         result.push({
            ...seats[seatIndex],
            displayNumber: seatIndex + 1,
            x: cx + 360 * Math.cos(angle) - 15,
            y: cy + 360 * Math.sin(angle) - 15
          });
          seatIndex++;
      }
    }
    
    return result;
  }

  toggleSeat(seat: SeatDisplay): void {
    if (seat.status !== 'AVAILABLE') return;
    const current = new Set(this.selectedSeatIds());
    if (current.has(seat.seatId)) {
      current.delete(seat.seatId);
    } else {
      if (this.MAX_SEATS === 1) {
        current.clear();
        current.add(seat.seatId);
      } else {
        if (current.size >= this.MAX_SEATS) {
          this.errorMessage.set(`Máximo ${this.MAX_SEATS} asientos por orden.`);
          return;
        }
        current.add(seat.seatId);
      }
    }
    this.errorMessage.set(null);
    this.selectedSeatIds.set(current);
  }

  isSeatSelected(seatId: string): boolean {
    return this.selectedSeatIds().has(seatId);
  }

  getSeatLabel(seat: SeatDisplay): string {
    return seat.displayNumber ? `Asiento ${seat.displayNumber}` : seat.seatId.substring(0, 4).toUpperCase();
  }

  getCategoryClass(): string {
    return CategoryUtils.getCategoryClass(this.eventCategory());
  }

  getCategoryIcon(): string {
    return CategoryUtils.getCategoryIcon(this.eventCategory());
  }

  getSeatTooltip(seat: SeatDisplay): string {
    if (seat.status !== 'AVAILABLE') return 'No disponible';
    const parts: string[] = [];
    parts.push(`Asiento: ${seat.displayNumber}`);
    if (seat.zone) parts.push(`Zona: ${seat.zone}`);
    if (seat.section) parts.push(`Sección: ${seat.section}`);
    parts.push(`Tipo: ${seat.type === 'GENERAL_ADMISSION' ? 'Admisión General' : seat.type}`);
    return parts.join(' | ');
  }

  reserveAndProceed(): void {
    const user = this.authService.currentUser();
    if (!user) {
      this.router.navigate(['/login']);
      return;
    }

    const dateId = this.selectedDateId();
    const seats = this.selectedSeats();

    if (!dateId || seats.length === 0) {
      this.errorMessage.set('Selecciona al menos un asiento.');
      return;
    }

    this.isReserving.set(true);
    this.errorMessage.set(null);
    this.statusMessage.set(`Reservando ${seats.length} asiento(s)...`);

    // Parallel reservations for each selected seat
    const reservationRequests = seats.map(seat =>
      this.http.post<any>('http://localhost:8080/v1/reservations', {
        seatId: seat.seatId,
        buyerId: user.sub,
        eventId: this.eventId,
        dateId: dateId,
        batchId: seat.batchId
      })
    );

    forkJoin(reservationRequests).subscribe({
      next: (reservations) => {
        this.statusMessage.set('Asientos reservados. Creando orden...');
        const reservationIds = reservations.map((r: any) => r.reservationId);

        // Wait briefly for RabbitMQ snapshot to propagate to orders-service
        setTimeout(() => this.createOrder(reservationIds, user, dateId), 2500);
      },
      error: (err) => {
        this.isReserving.set(false);
        this.statusMessage.set('');
        if (err.status === 409) {
          this.errorMessage.set('Uno o más asientos ya no están disponibles. Por favor selecciona otros.');
          // Reload seats to reflect current state
          this.loadSeats(dateId);
          this.selectedSeatIds.set(new Set());
        } else {
          this.errorMessage.set('Error al reservar los asientos. Por favor intenta de nuevo.');
        }
      }
    });
  }

  private createOrder(reservationIds: string[], user: any, dateId: string): void {
    this.http.post<any>('http://localhost:8080/v1/orders', {
      reservationId: reservationIds[0],
      buyerId: user.sub,
      eventId: this.eventId,
      dateId: dateId
    }).subscribe({
      next: (order) => {
        this.isReserving.set(false);
        this.statusMessage.set('');
        this.router.navigate(['/buyer/payment', order.orderId]);
      },
      error: () => {
        this.isReserving.set(false);
        this.statusMessage.set('');
        this.errorMessage.set('Los asientos fueron reservados pero no se pudo crear la orden. Por favor ve a "Mis Entradas" para pagar.');
      }
    });
  }
}
