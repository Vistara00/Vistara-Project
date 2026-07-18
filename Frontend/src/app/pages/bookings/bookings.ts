import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { NewBooking } from '../new-booking/new-booking';
import { BookingService } from '../../core/services/booking.service';
import { QRService } from '../../core/services/qr.service';  // ✅ Import QRService
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { QRModalComponent } from '../../components/qr-modal/qr-modal';

@Component({
  selector: 'app-bookings',
  templateUrl: './bookings.html',
  styleUrls: ['./bookings.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, MatIconModule, MatTooltipModule, MatButtonModule, NewBooking, QRModalComponent]
})
export class BookingsComponent implements OnInit {
  bookings: any[] = [];
  filteredBookings: any[] = [];
  loading = false;
  errorMessage = '';
  showModal = false;

  // QR Code modal
  showQRModal = false;
  selectedBookingForQR: any = null;
  qrImageUrl: SafeUrl | null = null;
  qrLoading = false;

  // Search and Filters
  searchQuery = '';
  selectedStatus = '';
  selectedPaymentMethod = '';
  startDate = '';
  endDate = '';
  sortBy = 'createdAt';
  sortDirection: 'asc' | 'desc' = 'desc';

  // Payment status options
  paymentStatuses = ['PENDING', 'PAID', 'REFUNDED', 'FAILED'];
  paymentMethods = ['CASH', 'MPESA', 'ECITIZEN'];

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;

  // ✅ Expose Math to template
  Math = Math;

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }

  get hasPrev(): boolean {
    return this.currentPage > 0;
  }

  get hasNext(): boolean {
    return this.currentPage < this.totalPages - 1;
  }

  constructor(
    private bookingService: BookingService,
    private qrService: QRService,  // ✅ Inject QRService
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    this.fetchBookings();
  }

  fetchBookings(): void {
    this.loading = true;
    this.errorMessage = '';

    const params: any = {
      page: this.currentPage,
      size: this.pageSize,
      search: this.searchQuery,
      status: this.selectedStatus,
      paymentMethod: this.selectedPaymentMethod,
      startDate: this.startDate,
      endDate: this.endDate,
      sortBy: this.sortBy,
      sortDirection: this.sortDirection
    };

    Object.keys(params).forEach(key => {
      if (!params[key]) {
        delete params[key];
      }
    });

    this.bookingService.getBookings(params).subscribe({
      next: (res: any) => {
        this.loading = false;
        if (res?.success) {
          this.totalItems = res.data?.total ?? res.total ?? res.data?.length ?? 0;
          const items = res.data?.items || res.data || [];
          
          this.bookings = items.map((b: any) => ({
            id: b.id,
            bookingReference: b.bookingReference,
            visitor_name: b.userFullName || 'Unknown Visitor',
            userEmail: b.userEmail || '—',
            userPhone: b.userPhoneNumber || '—',
            payment_method: b.paymentMethod || '—',
            checkin_date: b.checkInDate,
            checkout_date: b.checkOutDate,
            paymentStatus: b.paymentStatus,
            bookingStatus: b.bookingStatus,
            amount: b.amount,
            vehicleRegistration: b.vehicleRegistration || '—',
            groupSize: b.groupSize || 1,
            createdAt: b.createdAt,
            checkinStatus: b.checkinStatus || false
          }));
          
          this.applyFilters();
        } else {
          this.errorMessage = res?.message || 'Failed to load bookings.';
        }
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Failed to load bookings.';
        console.error('Bookings error:', err);
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.bookings];

    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(b =>
        b.visitor_name.toLowerCase().includes(query) ||
        b.bookingReference?.toLowerCase().includes(query) ||
        b.userEmail?.toLowerCase().includes(query) ||
        b.userPhone?.includes(query)
      );
    }

    if (this.selectedStatus) {
      filtered = filtered.filter(b => b.paymentStatus === this.selectedStatus);
    }

    if (this.selectedPaymentMethod) {
      filtered = filtered.filter(b => b.payment_method === this.selectedPaymentMethod);
    }

    if (this.startDate) {
      filtered = filtered.filter(b => b.checkin_date >= this.startDate);
    }
    if (this.endDate) {
      filtered = filtered.filter(b => b.checkin_date <= this.endDate);
    }

    filtered.sort((a, b) => {
      let valA = a[this.sortBy] || '';
      let valB = b[this.sortBy] || '';
      
      if (typeof valA === 'string') {
        valA = valA.toLowerCase();
        valB = valB.toLowerCase();
      }
      
      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });

    this.filteredBookings = filtered;
    this.totalItems = filtered.length;
  }

  onSearch(): void {
    this.currentPage = 0;
    this.applyFilters();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedPaymentMethod = '';
    this.startDate = '';
    this.endDate = '';
    this.sortBy = 'createdAt';
    this.sortDirection = 'desc';
    this.currentPage = 0;
    this.applyFilters();
  }

  refreshData(): void {
    this.fetchBookings();
  }

  toggleSort(column: string): void {
    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'desc';
    }
    this.applyFilters();
  }

  getSortIcon(column: string): string {
    if (this.sortBy !== column) return 'fa-sort';
    return this.sortDirection === 'asc' ? 'fa-sort-up' : 'fa-sort-down';
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.fetchBookings();
  }

  nextPage(): void { this.goToPage(this.currentPage + 1); }
  prevPage(): void { this.goToPage(this.currentPage - 1); }

  openBookingModal(): void {
    this.showModal = true;
  }

  closeBookingModal(): void {
    this.showModal = false;
    this.fetchBookings();
  }

  // ✅ Open QR Code Modal using QRService
  openQRModal(booking: any): void {
    this.selectedBookingForQR = booking;
    this.showQRModal = true;
    this.qrLoading = true;
    this.qrImageUrl = null;

    // ✅ Use QRService instead of BookingService
    this.qrService.getBookingQR(booking.id).subscribe({
      next: (res: any) => {
        this.qrLoading = false;
        if (res?.success && res?.data?.qrCodeBase64) {
          const base64 = res.data.qrCodeBase64;
          this.qrImageUrl = this.sanitizer.bypassSecurityTrustUrl(`data:image/png;base64,${base64}`);
        } else {
          this.qrImageUrl = null;
          console.warn('No QR code data received:', res);
        }
      },
      error: (err: any) => {
        this.qrLoading = false;
        console.error('QR Code error:', err);
        alert('Failed to load QR code. Please try again.');
      }
    });
  }

  // ✅ Close QR Modal
  closeQRModal(): void {
    this.showQRModal = false;
    this.selectedBookingForQR = null;
    this.qrImageUrl = null;
    this.qrLoading = false;
  }

  // ✅ Download QR Code
  downloadQR(): void {
    if (!this.qrImageUrl) return;
    const link = document.createElement('a');
    link.download = `QR-${this.selectedBookingForQR?.bookingReference}.png`;
    link.href = this.qrImageUrl.toString();
    link.click();
  }

  // ✅ Print Booking Details
  printBooking(): void {
    const printWindow = window.open('', '_blank');
    if (!printWindow) return;
    
    printWindow.document.write(`
      <html>
        <head>
          <title>Booking - ${this.selectedBookingForQR?.bookingReference}</title>
          <style>
            body { font-family: Arial, sans-serif; padding: 20px; }
            .header { text-align: center; margin-bottom: 20px; }
            .header h2 { color: #006400; }
            .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
            .section { margin-bottom: 15px; }
            .section h4 { color: #374151; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; }
            .row { display: flex; justify-content: space-between; padding: 3px 0; font-size: 12px; }
            .label { font-weight: 600; color: #6b7280; }
            .value { color: #111827; }
            .qr { text-align: center; margin-top: 20px; }
            .qr img { max-width: 120px; }
            .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 11px; border-top: 1px solid #e5e7eb; padding-top: 15px; }
            .status-badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 10px; font-weight: 600; }
            .status-paid { background: #d1fae5; color: #059669; }
            .status-pending { background: #fef3c7; color: #d97706; }
          </style>
        </head>
        <body>
          <div class="header">
            <h2>Vistara Booking Confirmation</h2>
            <p><strong>Reference:</strong> ${this.selectedBookingForQR?.bookingReference}</p>
          </div>
          <div class="grid">
            <div>
              <div class="section">
                <h4>Visitor Information</h4>
                <div class="row"><span class="label">Name:</span><span class="value">${this.selectedBookingForQR?.visitor_name}</span></div>
                <div class="row"><span class="label">Email:</span><span class="value">${this.selectedBookingForQR?.userEmail}</span></div>
                <div class="row"><span class="label">Phone:</span><span class="value">${this.selectedBookingForQR?.userPhone}</span></div>
              </div>
              <div class="section">
                <h4>Stay Details</h4>
                <div class="row"><span class="label">Check-in:</span><span class="value">${this.formatDate(this.selectedBookingForQR?.checkin_date)}</span></div>
                <div class="row"><span class="label">Check-out:</span><span class="value">${this.formatDate(this.selectedBookingForQR?.checkout_date)}</span></div>
                <div class="row"><span class="label">Group Size:</span><span class="value">${this.selectedBookingForQR?.groupSize || 1}</span></div>
                <div class="row"><span class="label">Vehicle:</span><span class="value">${this.selectedBookingForQR?.vehicleRegistration || '—'}</span></div>
              </div>
              <div class="section">
                <h4>Payment Details</h4>
                <div class="row"><span class="label">Method:</span><span class="value">${this.selectedBookingForQR?.payment_method}</span></div>
                <div class="row"><span class="label">Amount:</span><span class="value">${this.formatCurrency(this.selectedBookingForQR?.amount)}</span></div>
                <div class="row"><span class="label">Status:</span><span class="value"><span class="status-badge ${this.getStatusClass(this.selectedBookingForQR?.paymentStatus)}">${this.getStatusLabel(this.selectedBookingForQR?.paymentStatus)}</span></span></div>
              </div>
            </div>
            <div>
              <div class="section">
                <h4>QR Code</h4>
                <div class="qr">
                  <img src="${this.qrImageUrl}" alt="QR Code"/>
                  <p>Scan for check-in verification</p>
                </div>
              </div>
            </div>
          </div>
          <div class="footer">
            <p>Generated on ${new Date().toLocaleString()}</p>
            <p>Vistara Tourist Tracking System © 2026</p>
          </div>
          <script>
            window.onload = function() { window.print(); window.close(); }
          </script>
        </body>
      </html>
    `);
    printWindow.document.close();
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'PAID': 'status-paid',
      'PENDING': 'status-pending',
      'REFUNDED': 'status-refunded',
      'FAILED': 'status-failed',
      'CONFIRMED': 'status-confirmed',
      'CANCELLED': 'status-cancelled'
    };
    return map[status] || '';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      'PAID': 'Paid',
      'PENDING': 'Pending',
      'REFUNDED': 'Refunded',
      'FAILED': 'Failed',
      'CONFIRMED': 'Confirmed',
      'CANCELLED': 'Cancelled'
    };
    return map[status] || status;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  formatCurrency(amount: number): string {
    if (amount == null) return '—';
    return `KES ${amount.toFixed(2)}`;
  }

  getInitials(name: string): string {
    if (!name) return '?';
    const parts = name.split(' ');
    return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase();
  }
}