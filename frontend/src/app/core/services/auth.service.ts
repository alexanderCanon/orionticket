import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, UserTokenPayload } from '../../shared/models/auth.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly API_URL = 'https://api.orionticket.com/v1/auth';
  private readonly TOKEN_KEY = 'orionticket_token';

  // Signals for reactive state management
  public currentUser = signal<UserTokenPayload | null>(this.getDecodedToken());
  public isAuthenticated = signal<boolean>(this.hasValidToken());

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {}

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap((response) => {
        this.setToken(response.accessToken);
      }),
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.API_URL}/register`, userData);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this.currentUser.set(this.getDecodedToken());
    this.isAuthenticated.set(true);
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const decoded = this.decodeToken(token);
    if (!decoded) return false;

    // Check expiration (exp is in seconds)
    return decoded.exp * 1000 > Date.now();
  }

  private getDecodedToken(): UserTokenPayload | null {
    const token = this.getToken();
    if (!token) return null;

    if (!this.hasValidToken()) {
      localStorage.removeItem(this.TOKEN_KEY);
      return null;
    }

    return this.decodeToken(token);
  }

  private decodeToken(token: string): UserTokenPayload | null {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      return null;
    }
  }
}
