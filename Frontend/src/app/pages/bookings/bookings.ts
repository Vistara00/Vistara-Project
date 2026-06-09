import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

//  Angular Material imports
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { NewBooking } from '../new-booking/new-booking';

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.html',
  styleUrls: ['./bookings.css'],
  standalone: true,
  imports: [
    CommonModule,
    HttpClientModule,
    MatIconModule,
    MatTooltipModule,
    MatButtonModule,
    NewBooking
  ]
})
export class BookingsComponent implements OnInit {
  bookings: any[] = [];
  loading = false;
  errorMessage = '';

  // Modal state
  showModal = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchBookings();
  }

  fetchBookings(): void {
    this.loading = true;
    this.http.get('https://undrafted-erasable-crevice.ngrok-free.dev/api/v1/bookings')
      .subscribe({
        next: (res: any) => {
          this.loading = false;
          this.bookings = res.data || res;
        },
        error: (err) => {
          this.loading = false;
          console.error('BOOKINGS ERROR:', err);
          this.errorMessage = err?.error?.message || 'Failed to load bookings.';
        }
      });
  }

  // Modal control methods
  openBookingModal(): void {
    this.showModal = true;
  }

  closeBookingModal(): void {
    this.showModal = false;
  }
}
