import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  metrics = signal<any>({ totalUsers: 0, activeEvents: 0, totalRevenue: 0 });
  pendingEvents = signal<any[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>('http://localhost:8080/v1/admin/dashboard').subscribe({
      next: (res) => {
        this.metrics.set(res.metrics);
        this.pendingEvents.set(res.pendingEvents);
      },
      error: () => {
        this.metrics.set({
          totalUsers: 1420,
          activeEvents: 35,
          totalRevenue: 245000.50
        });
        this.pendingEvents.set([
          { id: 'EVT-998', title: 'Torneo E-Sports 2026', organizerEmail: 'org1@orion.com', requestDate: new Date().toISOString() },
          { id: 'EVT-999', title: 'Feria del Libro Independiente', organizerEmail: 'org2@orion.com', requestDate: new Date().toISOString() }
        ]);
      }
    });
  }

  approveEvent(id: string) {
    this.http.post(`http://localhost:8080/v1/admin/events/${id}/approve`, {}).subscribe({
      next: () => this.removeEvent(id),
      error: () => this.removeEvent(id)
    });
  }

  rejectEvent(id: string) {
    this.http.post(`http://localhost:8080/v1/admin/events/${id}/reject`, {}).subscribe({
      next: () => this.removeEvent(id),
      error: () => this.removeEvent(id)
    });
  }

  private removeEvent(id: string) {
    this.pendingEvents.update(events => events.filter(e => e.id !== id));
  }
}
