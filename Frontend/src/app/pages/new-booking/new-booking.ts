import { Component, EventEmitter, Output } from '@angular/core';
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
export class NewBooking {
  @Output() close = new EventEmitter<void>();

  loading = false;
  success = false;

  constructor(private http: HttpClient) {}

  // Called when form is submitted
confirmBooking(form?: any): void {
  if (!form?.value) return;

  const bookingData = form.value;
  this.loading = true;

  if (bookingData.payment === 'Cash') {
    // Add notes for cash bookings
    const payload = {
      ...bookingData,
      notes: bookingData.notes || "Paid cash at gate"
    };

    this.http.post('/api/v1/admin/bookings/cash-booking', payload).subscribe({
      next: () => this.showSuccess(),
      error: () => this.loading = false
    });

  } else if (bookingData.payment === 'Mpesa') {
    // Add notes for Mpesa bookings
    const payload = {
      ...bookingData,
      notes: bookingData.notes || "Awaiting Mpesa payment"
    };

    this.http.post('/api/v1/admin/bookings/mpesa-booking', payload).subscribe({
      next: () => this.showSuccess(),
      error: () => this.loading = false
    });

  } else if (bookingData.payment === 'Ecitizen') {
    alert('E‑Citizen service not available.');
    this.loading = false;
  }
}


  // Show success animation
  private showSuccess(): void {
    this.loading = false;
    this.success = true;
    setTimeout(() => {
      this.success = false;
      this.close.emit(); 
    }, 2000);
  }

  // Close modal manually
  closeModal(): void {
    this.close.emit();
  }
}
