import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CheckinService {
    constructor(private http: HttpClient) { }

    // Check-in request
    checkIn(bookingId: number, vehicleRegistration: string): Observable<any> {
        const payload = {
            bookingId,
            walkInUserId: 0, // empty
            vehicleRegistrationOverride: vehicleRegistration || '',
            notes: '' // empty
        };
        return this.http.post(`${environment.apiUrl}/v1/admin/checkin`, payload);
    }

    // View booking details
    viewBooking(bookingId: number): Observable<any> {
        return this.http.get(`${environment.apiUrl}/v1/admin/bookings/${bookingId}`);
    }

    // Checkout request
    checkout(sessionId: number): Observable<any> {
        const payload = {
            sessionId,
            notes: '' // empty
        };
        return this.http.post(`${environment.apiUrl}/v1/admin/checkout`, payload);
    }
}
