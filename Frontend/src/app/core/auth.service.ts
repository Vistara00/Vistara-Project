// src/app/core/auth.service.ts
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'token';
  private readonly EXPIRY_KEY = 'tokenExpiry';

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const expiry = localStorage.getItem(this.EXPIRY_KEY);
    if (expiry && Date.now() > +expiry) {
      this.logout();
      return false;
    }
    return true;
  }

  saveToken(token: string, expiresIn: number = 3600): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    const expiryTime = Date.now() + expiresIn * 1000;
    localStorage.setItem(this.EXPIRY_KEY, expiryTime.toString());
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.EXPIRY_KEY);
    localStorage.removeItem('vistara_email');
  }
}