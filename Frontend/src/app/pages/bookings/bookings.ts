import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { NewBooking } from '../new-booking/new-booking';
import { BookingService } from '../../core/services/booking.service';

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.html',
  styleUrls: ['./bookings.css'],
  standalone: true,
  imports: [CommonModule, MatIconModule, MatTooltipModule, MatButtonModule, NewBooking]
})
export class BookingsComponent implements OnInit {
  bookings: any[] = [];
  loading = false;
  errorMessage = '';
  showModal = false;

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    this.fetchBookings();
  }

  fetchBookings(): void {
    this.loading = true;
    this.bookingService.getBookings().subscribe({
      next: (res: any) => {
        this.loading = false;
        console.log('BOOKINGS RESPONSE:', res);

        this.bookings = (res.data || []).map((b: any) => ({
          id: b.bookingReference,
          visitor_name: b.vehicleRegistration || 'Unknown Visitor',
          email: '—',
          payment_method: b.paymentMethod,
          checkin_date: b.checkInDate,
          checkout_date: b.checkOutDate,
          status: b.paymentStatus,
          amount: b.amount
        }));
      },
      error: (err) => {
        this.loading = false;
        console.error('BOOKINGS ERROR:', err);
        this.errorMessage = err?.error?.message || 'Failed to load bookings.';
      }
    });
  }

  openBookingModal(): void {
    this.showModal = true;
  }

  closeBookingModal(): void {
    this.showModal = false;
  }
}
