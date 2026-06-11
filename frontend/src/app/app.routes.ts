import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { PublicLayout } from './features/public/layout/public-layout/public-layout';
import { BuyerLayout } from './features/buyer/layout/buyer-layout/buyer-layout';
import { OrganizerLayout } from './features/organizer/layout/organizer-layout/organizer-layout';
import { AdminLayout } from './features/admin/layout/admin-layout/admin-layout';
import { ValidatorLayout } from './features/validator/layout/validator-layout/validator-layout';

// Components
import { Catalog } from './features/public/catalog/catalog';
import { Login } from './features/public/auth/login/login';
import { Register } from './features/public/auth/register/register';
import { EventDetail } from './features/public/event-detail/event-detail';
import { SeatPicker } from './features/buyer/seat-picker/seat-picker';
import { Payment } from './features/buyer/payment/payment';
import { TicketSuccess } from './features/buyer/ticket-success/ticket-success';
import { Dashboard as OrganizerDashboard } from './features/organizer/dashboard/dashboard';
import { EventCreate } from './features/organizer/event-create/event-create';
import { Scanner } from './features/validator/scanner/scanner';
import { History } from './features/buyer/history/history';

export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      { path: '', component: Catalog },
      { path: 'login', component: Login },
      { path: 'register', component: Register },
      { path: 'event/:id', component: EventDetail }
    ]
  },
  {
    path: 'buyer',
    component: BuyerLayout,
    canActivate: [authGuard],
    children: [
      { path: '', loadComponent: () => import('./features/buyer/dashboard/dashboard').then(m => m.Dashboard) },
      { path: 'seats/:eventId/:dateId', component: SeatPicker },
      { path: 'payment/:orderId', component: Payment },
      { path: 'success/:orderId', component: TicketSuccess },
      { path: 'history', component: History }
    ]
  },
  {
    path: 'organizer',
    component: OrganizerLayout,
    canActivate: [authGuard],
    children: [
      { path: '', component: OrganizerDashboard },
      { path: 'create-event', component: EventCreate }
    ]
  },
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard],
    children: [
      { path: '', loadComponent: () => import('./features/admin/dashboard/dashboard').then(m => m.Dashboard) }
    ]
  },
  {
    path: 'validator',
    component: ValidatorLayout,
    canActivate: [authGuard],
    children: [
      { path: '', component: Scanner }
    ]
  },
  { path: '**', redirectTo: '' }
];
