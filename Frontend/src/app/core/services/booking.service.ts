import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class BookingService {
  constructor(private http: HttpClient) {}

  // Fetch bookings
getBookings(page: number = 0, size: number = 20) {
  return this.http.get(`${environment.apiUrl}/v1/admin/bookings`, {
    params: { page, size }
  });
}

  // Create booking
  createBooking(payload: any) {
    return this.http.post(`${environment.apiUrl}/v1/admin/bookings`, payload);
  }
}
