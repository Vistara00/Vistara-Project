// src/app/core/services/booking.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../core/environments/environment';

@Injectable({ providedIn: 'root' })
export class BookingService {
  constructor(private http: HttpClient) {}

  /**
   * Fetch bookings with filters and pagination
   */
  getBookings(params?: any): Observable<any> {
    let httpParams = new HttpParams();
    
    // Default pagination
    const page = params?.page ?? 0;
    const size = params?.size ?? 20;
    
    httpParams = httpParams.set('page', page.toString());
    httpParams = httpParams.set('size', size.toString());
    
    // Add optional filters
    if (params?.search) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params?.status) {
      httpParams = httpParams.set('status', params.status);
    }
    if (params?.paymentMethod) {
      httpParams = httpParams.set('paymentMethod', params.paymentMethod);
    }
    if (params?.startDate) {
      httpParams = httpParams.set('startDate', params.startDate);
    }
    if (params?.endDate) {
      httpParams = httpParams.set('endDate', params.endDate);
    }
    if (params?.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params?.sortDirection) {
      httpParams = httpParams.set('sortDirection', params.sortDirection);
    }
    
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings`, { 
      params: httpParams 
    });
  }

  /**
   * Get booking by ID
   */
  getBookingById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings/${id}`);
  }

  /**
   * ✅ Get QR Code for a booking
   * GET /api/v1/bookings/{bookingId}/qr
   */
  getBookingQR(bookingId: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/bookings/${bookingId}/qr`);
  }

  /**
   * Create cash booking
   */
  createCashBooking(payload: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/v1/admin/bookings/cash-booking`, payload);
  }

  /**
   * Create M-Pesa booking
   */
  createMpesaBooking(payload: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/v1/admin/bookings/mpesa-booking`, payload);
  }

  /**
   * Confirm payment for a booking
   */
  confirmPayment(bookingId: number, paymentReference: string): Observable<any> {
    return this.http.post(
      `${environment.apiUrl}/v1/admin/bookings/${bookingId}/confirm-payment`, 
      null,
      { params: { paymentReference } }
    );
  }

  /**
   * Cancel a booking
   */
  cancelBooking(bookingId: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/v1/admin/bookings/${bookingId}/cancel`, {});
  }

  /**
   * Delete a booking (admin only)
   */
  deleteBooking(bookingId: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/v1/admin/bookings/${bookingId}`);
  }

  /**
   * Get all bookings (admin only)
   */
  getAllBookings(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings`);
  }

  /**
   * Get bookings by status (admin only)
   */
  getBookingsByStatus(status: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings`, {
      params: { status }
    });
  }

  /**
   * ✅ Get bookings by user ID
   */
  getBookingsByUser(userId: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings/user/${userId}`);
  }

  /**
   * ✅ Update booking status
   */
  updateBookingStatus(bookingId: number, status: string): Observable<any> {
    return this.http.patch(
      `${environment.apiUrl}/v1/admin/bookings/${bookingId}/status`,
      { status }
    );
  }

  /**
   * ✅ Check if booking is eligible for check-in
   */
  checkBookingEligibility(bookingId: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings/${bookingId}/check-eligibility`);
  }

  /**
   * ✅ Get booking statistics
   */
  getBookingStats(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/v1/admin/bookings/stats`);
  }
}