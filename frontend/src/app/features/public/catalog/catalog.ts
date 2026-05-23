import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

import { CategoryUtils } from '../../../shared/utils/category.utils';

interface EventCatalogDto {
  eventId: string;
  name: string;
  category: string;
  organizerName: string;
  dates: { dateId: string; scheduledAt: string; availableSeats: number; venueName: string; }[];
}

@Component({
  selector: 'app-catalog',
  imports: [RouterModule],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css'
})
export class Catalog implements OnInit {
  events = signal<EventCatalogDto[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>('http://localhost:8080/v1/catalog/events')
      .subscribe({
        next: (res) => {
          this.events.set(res.events || []);
        },
        error: (err) => {
          console.error('Error fetching catalog, using mocks as fallback', err);
          this.events.set([
            { eventId: '1', name: 'Concierto Sinfónico VIP', category: 'Música', organizerName: 'Org', dates: [] },
            { eventId: '2', name: 'Festival de Tecnología 2026', category: 'Conferencia', organizerName: 'Org', dates: [] }
          ]);
        }
      });
  }

  getClosestDate(event: EventCatalogDto): string {
    if (!event.dates || event.dates.length === 0) return 'Fecha por confirmar';
    const now = new Date().getTime();
    
    // Filtramos fechas futuras
    const futureDates = event.dates
      .map(d => new Date(d.scheduledAt))
      .filter(d => d.getTime() > now);
      
    if (futureDates.length === 0) return 'Fechas pasadas';
    
    futureDates.sort((a, b) => a.getTime() - b.getTime());
    
    // Formatear la fecha
    return futureDates[0].toLocaleString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'UTC'
    });
  }

  getCategoryClass(category: string): string {
    return CategoryUtils.getCategoryClass(category);
  }

  getCategoryIcon(category: string): string {
    return CategoryUtils.getCategoryIcon(category);
  }
}
