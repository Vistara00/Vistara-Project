// src/app/core/services/qr.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class QRService {
  constructor(private http: HttpClient) {}

  /**
   * Get QR Code for a specific booking (Admin/Ranger)
   */
  getBookingQR(bookingId: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/qr/booking/${bookingId}`);
  }

  /**
   * Get QR Code for the current user's active booking (Tourist)
   */
  getMyBookingQR(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/qr/my-booking`);
  }
}