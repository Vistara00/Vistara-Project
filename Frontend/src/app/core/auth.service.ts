import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    //  check expiry
    const expiry = localStorage.getItem('tokenExpiry');
    if (expiry && Date.now() > +expiry) {
      this.logout(); // clear expired token
      return false;
    }
    return true;
  }

  saveToken(token: string, expiresIn: number = 3600): void {
    localStorage.setItem('authToken', token);
    // store expiry timestamp (default 1 hour)
    const expiryTime = Date.now() + expiresIn * 1000;
    localStorage.setItem('tokenExpiry', expiryTime.toString());
  }

  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('tokenExpiry');
  }
}
