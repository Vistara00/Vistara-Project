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

  currentPage = 0;
  pageSize = 20;
  totalItems = 0;

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }

  get hasPrev(): boolean {
    return this.currentPage > 0;
  }

  get hasNext(): boolean {
    return this.currentPage < this.totalPages - 1;
  }

  constructor(private bookingService: BookingService) { }

  ngOnInit(): void {
    this.fetchBookings();
  }

  fetchBookings(): void {
    this.loading = true;
    this.errorMessage = '';

    this.bookingService.getBookings(this.currentPage, this.pageSize).subscribe({
      next: (res: any) => {
        this.loading = false;
        console.log('BOOKINGS RESPONSE:', res);

        // Adjust these two lines to match your actual response shape
        this.totalItems = res.data?.total ?? res.total ?? res.data?.length ?? 0;
        const items = res.data?.items || res.data || [];

        this.bookings = items.map((b: any) => ({
          id: b.bookingReference,
          visitor_name: b.userFullName || 'Unknown Visitor',
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

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.fetchBookings();
  }

  nextPage(): void {
    this.goToPage(this.currentPage + 1);
  }

  prevPage(): void {
    this.goToPage(this.currentPage - 1);
  }

  openBookingModal(): void {
    this.showModal = true;
  }

  closeBookingModal(): void {
    this.showModal = false;
  }
}