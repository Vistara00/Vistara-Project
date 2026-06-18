import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class BookingService {
  constructor(private http: HttpClient) {}

  // Fetch bookings
  getBookings() {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings`);
  }

  // Create booking
  createBooking(payload: any) {
    return this.http.post(`${environment.apiUrl}/v1/admin/bookings`, payload);
  }
}
