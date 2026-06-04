import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.html',
  styleUrls: ['./bookings.css'],
  standalone: true,
  imports: [CommonModule, HttpClientModule]
})
export class BookingsComponent implements OnInit {
  bookings: any[] = [];
  loading = false;
  errorMessage = '';

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
          // Adjust based on your API response structure
          this.bookings = res.data || res;
        },
        error: (err) => {
          this.loading = false;
          console.error('BOOKINGS ERROR:', err);
          this.errorMessage = err?.error?.message || 'Failed to load bookings.';
        }
      });
  }
}
