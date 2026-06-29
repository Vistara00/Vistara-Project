import { Component, EventEmitter, Output, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-new-booking',
  templateUrl: './new-booking.html',
  styleUrls: ['./new-booking.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class NewBooking implements OnDestroy {
  @Output() close = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
  @Output() bookingSuccess = new EventEmitter<void>();

  loading = false;
  success = false;
  failed = false;
  animKey = 0;

  get successSrc(): string {
    return `assets/images/checkmark.svg?v=${this.animKey}`;
  }

  get failSrc(): string {
    return `assets/images/failed.svg?v=${this.animKey}`;
  }

  private timer: any;

  constructor(private http: HttpClient) { }

  confirmBooking(form?: any): void {
    if (!form?.value) return;

    const bookingData = form.value;
    this.loading = true;
    this.success = false;
    this.failed = false;

    if (bookingData.payment === 'Cash') {
      const payload = { ...bookingData, notes: bookingData.notes || 'Paid cash at gate' };
      this.http.post('/api/v1/admin/bookings/cash-booking', payload).subscribe({
        next: () => this.showSuccess(),
        error: () => this.showError()
      });

    } else if (bookingData.payment === 'Mpesa') {
      const payload = { ...bookingData, notes: bookingData.notes || 'Awaiting Mpesa payment' };
      this.http.post('/api/v1/admin/bookings/mpesa-booking', payload).subscribe({
        next: () => this.showSuccess(),
        error: () => this.showError()
      });

    } else if (bookingData.payment === 'Ecitizen') {
      alert('E‑Citizen service not available.');
      this.loading = false;
    }
  }
  private showSuccess(): void {
    this.loading = false;
    this.animKey++;
    this.success = true;
    this.bookingSuccess.emit(); // ← notify parent to refresh table
    this.timer = setTimeout(() => {
      this.success = false; // animation disappears after 5s
    }, 5000);
  }

  private showError(): void {
    this.loading = false;
    this.animKey++;
    this.failed = true;
    this.timer = setTimeout(() => {
      this.failed = false;
    }, 5000);
  }

  closeModal(): void {
    this.cancel.emit();
  }

  ngOnDestroy(): void {
    clearTimeout(this.timer);
  }
}