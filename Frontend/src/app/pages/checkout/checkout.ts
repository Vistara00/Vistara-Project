import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/environments/environment';
import { CheckinPopupComponent } from '../checkin-popup/checkin-popup';

// Shape returned by GET /admin/active-visitors (ActiveVisitorResponse on the backend)
interface ActiveVisitor {
  id: number;                    // VisitorSession id — used as sessionId for /admin/checkout
  bookingReference: string;
  userFullName: string;
  userEmail: string;
  userPhoneNumber: string;
  amount: number;
  checkInDate: string;
  checkOutDate: string;
  bookingStatus: string;
  vehicleRegistration: string;
  groupSize: number;
  checkInTime: string;
  sosTriggered: boolean;
  hasEmergency: boolean;
  notes: string;
}

// API Response wrapper interface
interface ApiResponse {
  success: boolean;
  message: string;
  data: ActiveVisitor[];
  timestamp: string;
  statusCode: number;
}

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.html',
  styleUrls: ['./checkout.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, CheckinPopupComponent]
})
export class CheckoutComponent implements OnInit {
  searchQuery = '';
  visitors: ActiveVisitor[] = [];
  loading = false;
  errorMessage = '';

  // Popup state (used only for the "view" action)
  selectedBooking: ActiveVisitor | null = null;
  showPopup = false;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchVisitors();
  }

  // Fetch active visitors from API — this endpoint already returns exactly
  // who's checked in and eligible for checkout, so no client-side status
  // filtering is needed.
  fetchVisitors(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.http.get<ApiResponse>(`${environment.apiUrl}/v1/admin/active-visitors`).subscribe({
      next: (res) => {
        this.loading = false;
        console.log('API Response:', res); // Debug log
        
        // Check if response has data
        if (res && res.data) {
          this.visitors = res.data;
          console.log('Visitors loaded:', this.visitors.length);
        } else {
          this.visitors = [];
          this.errorMessage = 'No active visitors found';
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('ACTIVE VISITORS ERROR:', err);
        this.errorMessage = err?.error?.message || 'Failed to load active visitors.';
        this.visitors = [];
      }
    });
  }

  // Filtered visitors for table (search only)
  get filteredBookings(): ActiveVisitor[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.visitors;
    
    return this.visitors.filter((v) =>
      v.userFullName.toLowerCase().includes(q) ||
      v.bookingReference.toLowerCase().includes(q)
    );
  }

  // Summary card metrics
  get totalCheckedIn(): number {
    return this.visitors.length;
  }

  get expectedCheckouts(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.visitors.filter((v) => v.checkOutDate === today).length;
  }

  get overdueCheckouts(): number {
    const today = new Date().toISOString().split('T')[0];
    return this.visitors.filter((v) => v.checkOutDate < today).length;
  }

  // View popup — active-visitors already returns everything the popup needs,
  // so no extra request required (and b.id is a session id, not a booking id,
  // so /v1/admin/bookings/{id} would have been the wrong lookup anyway).
  onView(v: ActiveVisitor) {
    this.selectedBooking = v;
    this.showPopup = true;
  }

  closePopup() {
    this.showPopup = false;
    this.selectedBooking = null;
  }

  // Check-out a specific visitor session (used by both the row button and the popup)
  onCheckout(v: ActiveVisitor) {
    if (!v) return;
    
    if (!confirm(`Checkout ${v.userFullName} (${v.bookingReference})?`)) {
      return;
    }
    
    const payload = {
      sessionId: v.id,
      notes: ''
    };
    
    this.loading = true;
    this.http.post(`${environment.apiUrl}/v1/admin/checkout`, payload).subscribe({
      next: () => {
        this.loading = false;
        alert('Checkout successful!');
        this.closePopup();
        this.fetchVisitors(); // refresh list (checked-out visitors will drop off automatically)
      },
      error: (err) => {
        this.loading = false;
        console.error('CHECKOUT ERROR:', err);
        alert('Failed to checkout. Please try again.');
      }
    });
  }

  // Popup action delegates to the shared method using the currently selected visitor
  onPopupCheckout() {
    if (this.selectedBooking) {
      this.onCheckout(this.selectedBooking);
    }
  }
}