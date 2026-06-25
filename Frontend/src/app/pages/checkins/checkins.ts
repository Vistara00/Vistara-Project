// src/app/pages/checkins/checkins.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/environments/environment';
import { CheckinPopupComponent } from '../checkin-popup/checkin-popup';

@Component({
  selector: 'app-checkins',
  templateUrl: './checkins.html',
  styleUrls: ['./checkins.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, CheckinPopupComponent]
})
export class CheckinsComponent implements OnInit {
  searchQuery = '';
  statusFilter = 'All Status';
  bookings: any[] = [];
  loading = false;
  errorMessage = '';

  // Popup state
  selectedBooking: any = null;
  showPopup = false;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchBookings();
  }

  // Fetch bookings from API
  fetchBookings(): void {
    this.loading = true;
    this.http.get<any>(`${environment.apiUrl}/v1/admin/bookings`).subscribe({
      next: (res) => {
        this.loading = false;
        if (res?.success && res?.data) {
          // Only show paid bookings
          this.bookings = res.data.filter((b: any) => b.paymentStatus === 'PAID');
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('CHECKINS ERROR:', err);
        this.errorMessage = err?.error?.message || 'Failed to load bookings.';
      }
    });
  }

  // Filtered bookings for table
  get filteredBookings() {
    return this.bookings.filter((b) => {
      const matchesSearch =
        b.userFullName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        b.bookingReference.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesStatus =
        this.statusFilter === 'All Status' || b.bookingStatus === this.statusFilter.toUpperCase();
      return matchesSearch && matchesStatus;
    });
  }

  // Summary card metrics
  get totalVisitors(): number {
    return this.bookings.length;
  }

  get scheduledToday(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.bookings.filter((b) => b.checkInDate === today).length;
  }

  get expectedCheckouts(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.bookings.filter((b) => b.checkOutDate === today).length;
  }

  // Popup controls
  onView(b: any) {
    // Fetch booking details from API before showing popup
    this.http.get<any>(`${environment.apiUrl}/v1/admin/bookings/${b.id}`).subscribe({
      next: (res) => {
        if (res?.success && res?.data) {
          this.selectedBooking = res.data;
          this.showPopup = true;
        }
      },
      error: (err) => {
        console.error('VIEW BOOKING ERROR:', err);
        alert('Failed to load booking details.');
      }
    });
  }

  closePopup() {
    this.showPopup = false;
    this.selectedBooking = null;
  }

  // Popup actions
  onPopupCheckin() {
    if (!this.selectedBooking) return;
    const payload = {
      bookingId: this.selectedBooking.id,
      walkInUserId: 0,
      vehicleRegistrationOverride: this.selectedBooking.vehicleRegistration || '',
      notes: ''
    };
    this.http.post(`${environment.apiUrl}/v1/admin/checkin`, payload).subscribe({
      next: () => {
        alert('Check‑in successful!');
        this.closePopup();
        this.fetchBookings(); // refresh list
      },
      error: (err) => {
        console.error('CHECKIN ERROR:', err);
        alert('Failed to check‑in.');
      }
    });
  }

  onPopupCheckout() {
    if (!this.selectedBooking) return;
    const payload = {
      sessionId: this.selectedBooking.id, // using booking id as sessionId
      notes: ''
    };
    this.http.post(`${environment.apiUrl}/v1/admin/checkout`, payload).subscribe({
      next: () => {
        alert('Checkout successful!');
        this.closePopup();
        this.fetchBookings(); // refresh list
      },
      error: (err) => {
        console.error('CHECKOUT ERROR:', err);
        alert('Failed to checkout.');
      }
    });
  }
}
