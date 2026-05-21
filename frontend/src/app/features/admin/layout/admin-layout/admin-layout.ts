import { Component, computed, inject } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterModule, CommonModule],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css'
})
export class AdminLayout {
  private authService = inject(AuthService);
  private router = inject(Router);

  isAuthenticated = this.authService.isAuthenticated;
  currentUser = this.authService.currentUser;

  portalLink = computed(() => {
    const user = this.currentUser();
    if (!user) return '/login';
    
    switch (user.roleId) {
      case '00000000-0000-0000-0000-000000000003': return '/admin';
      case '00000000-0000-0000-0000-000000000002': return '/organizer';
      case '00000000-0000-0000-0000-000000000005': return '/validator';
      case '00000000-0000-0000-0000-000000000001': return '/buyer';
      default: return '/';
    }
  });

  logout() {
    this.authService.logout();
  }
}
