// src/app/pages/checkin/checkin.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/environments/environment';
import { CheckinPopupComponent } from '../checkin-popup/checkin-popup';

// Shape returned by GET /admin/bookings
interface Booking {
  id: number;
  bookingReference: string;
  userFullName: string;
  userEmail: string;
  userPhoneNumber: string;
  amount: number;
  checkInDate: string;
  checkOutDate: string;
  bookingStatus: string;
  paymentStatus: string;
  vehicleRegistration: string;
  groupSize: number;
  notes: string;
}

// API Response for list of bookings
interface BookingsListResponse {
  success: boolean;
  message: string;
  data: Booking[];  // Array of bookings
  timestamp: string;
  statusCode: number;
}

// API Response for single booking
interface SingleBookingResponse {
  success: boolean;
  message: string;
  data: Booking;  // Single booking object
  timestamp: string;
  statusCode: number;
}

@Component({
  selector: 'app-checkin',
  templateUrl: './checkin.html',
  styleUrls: ['./checkin.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, CheckinPopupComponent]
})
export class CheckinComponent implements OnInit {
  searchQuery = '';
  bookings: Booking[] = [];
  loading = false;
  errorMessage = '';

  // Popup state (used only for the "view" action)
  selectedBooking: Booking | null = null;
  showPopup = false;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchBookings();
  }

  // Fetch bookings from API
  fetchBookings(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.http.get<BookingsListResponse>(`${environment.apiUrl}/v1/admin/bookings`).subscribe({
      next: (res) => {
        this.loading = false;
        console.log('API Response:', res); // Debug log
        
        if (res && res.data) {
          // Check-in page: bookings that are paid and awaiting arrival
          this.bookings = res.data.filter(
            (b: Booking) => b.paymentStatus === 'PAID' && b.bookingStatus === 'CONFIRMED'
          );
          console.log('Bookings loaded:', this.bookings.length);
        } else {
          this.bookings = [];
          this.errorMessage = 'No bookings found';
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('CHECKIN LIST ERROR:', err);
        this.errorMessage = err?.error?.message || 'Failed to load bookings.';
        this.bookings = [];
      }
    });
  }

  // Filtered bookings for table (search only)
  get filteredBookings(): Booking[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.bookings;
    
    return this.bookings.filter((b) =>
      b.userFullName.toLowerCase().includes(q) ||
      b.bookingReference.toLowerCase().includes(q)
    );
  }

  // Summary card metrics
  get totalPending(): number {
    return this.bookings.length;
  }

  get scheduledToday(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.bookings.filter((b) => b.checkInDate === today).length;
  }

  get overdueCheckins(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.bookings.filter((b) => b.checkInDate < today).length;
  }

  // View popup
  onView(b: Booking): void {
    this.loading = true;
    this.http.get<SingleBookingResponse>(`${environment.apiUrl}/v1/admin/bookings/${b.id}`).subscribe({
      next: (res) => {
        this.loading = false;
        if (res && res.data) {
          this.selectedBooking = res.data;  // Now TypeScript knows this is a single Booking
          this.showPopup = true;
        } else {
          alert('Failed to load booking details.');
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('VIEW BOOKING ERROR:', err);
        alert('Failed to load booking details.');
      }
    });
  }

  closePopup(): void {
    this.showPopup = false;
    this.selectedBooking = null;
  }

  // Check-in a specific booking (used by both the row button and the popup)
  onCheckin(b: Booking): void {
    if (!b) return;
    
    if (!confirm(`Check-in ${b.userFullName} (${b.bookingReference})?`)) {
      return;
    }
    
    const payload = {
      bookingId: b.id,
      walkInUserId: 0,
      vehicleRegistrationOverride: b.vehicleRegistration || '',
      notes: ''
    };
    
    this.loading = true;
    this.http.post(`${environment.apiUrl}/v1/admin/checkin`, payload).subscribe({
      next: () => {
        this.loading = false;
        alert('Check‑in successful!');
        this.closePopup();
        this.fetchBookings(); // refresh list (checked-in bookings will drop off automatically)
      },
      error: (err) => {
        this.loading = false;
        console.error('CHECKIN ERROR:', err);
        alert('Failed to check‑in. Please try again.');
      }
    });
  }

  // Popup action delegates to the shared method using the currently selected booking
  onPopupCheckin(): void {
    if (this.selectedBooking) {
      this.onCheckin(this.selectedBooking);
    }
  }
}