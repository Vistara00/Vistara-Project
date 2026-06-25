// src/app/pages/checkins/checkins.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/environments/environment';

@Component({
  selector: 'app-checkins',
  templateUrl: './checkins.html',
  styleUrls: ['./checkins.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class CheckinsComponent implements OnInit {
  searchQuery = '';
  statusFilter = 'All Status';
  bookings: any[] = [];
  loading = false;
  errorMessage = '';

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchBookings();
  }

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
}
