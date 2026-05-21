import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);

  myEvents = signal<any[]>([]);

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.http.get<any>(`http://localhost:8080/v1/events?organizerId=${user.sub}`).subscribe({
        next: (res) => {
          this.myEvents.set(res.content || res);
        },
        error: () => {
          this.myEvents.set([
            { id: '1', title: 'Concierto Sinfonico VIP', date: new Date().toISOString(), ticketsSold: 120, totalCapacity: 500 },
            { id: '2', title: 'Festival de Tecnologia 2026', date: new Date().toISOString(), ticketsSold: 450, totalCapacity: 500 }
          ]);
        }
      });
    }
  }
}
