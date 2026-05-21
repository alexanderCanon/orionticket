import { Component, computed, inject } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { OfflineSyncService } from '../../../../core/services/offline-sync.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-validator-layout',
  imports: [RouterModule, CommonModule],
  templateUrl: './validator-layout.html',
  styleUrl: './validator-layout.css'
})
export class ValidatorLayout {
  private authService = inject(AuthService);
  private syncService = inject(OfflineSyncService);
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

  isOnline = this.syncService.isOnline;
  pendingCount = this.syncService.pendingSyncCount;

  logout() {
    this.authService.logout();
  }
}
