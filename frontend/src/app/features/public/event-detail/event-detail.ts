import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { CategoryUtils } from '../../../shared/utils/category.utils';

@Component({
  selector: 'app-event-detail',
  imports: [CommonModule, FormsModule],
  templateUrl: './event-detail.html',
  styleUrl: './event-detail.css'
})
export class EventDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  event = signal<any>(null);
  isLoading = signal<boolean>(true);
  selectedDateId = signal<string | null>(null);
  eventId: string | null = null;

  selectedDate = computed(() => {
    const e = this.event();
    const id = this.selectedDateId();
    if (!e || !e.dates || !id) return null;
    return e.dates.find((d: any) => d.dateId === id) || null;
  });

  ngOnInit(): void {
    this.eventId = this.route.snapshot.paramMap.get('id');
    if (this.eventId) {
      this.http.get<any>(`http://localhost:8080/v1/catalog/events/${this.eventId}`).subscribe({
        next: (res) => {
          this.event.set(res);
          // Pre-seleccionar la primera fecha disponible
          if (res.dates && res.dates.length > 0) {
            this.selectedDateId.set(res.dates[0].dateId);
          }
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        }
      });
    }
  }

  selectDate(dateId: string): void {
    this.selectedDateId.set(dateId);
  }

  goToCheckout(): void {
    const dateId = this.selectedDateId();
    if (!dateId) return;

    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/buyer/seats', this.eventId, dateId]);
    } else {
      this.router.navigate(['/login']);
    }
  }

  getCategoryClass(category: string): string {
    return CategoryUtils.getCategoryClass(category);
  }

  getCategoryIcon(category: string): string {
    return CategoryUtils.getCategoryIcon(category);
  }
}
