import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { QRService } from '../../core/services/qr.service';

@Component({
  selector: 'app-qr-modal',
  templateUrl: './qr-modal.html',
  styleUrls: ['./qr-modal.css'],
  standalone: true,
  imports: [CommonModule]
})
export class QRModalComponent implements OnInit {
  @Input() bookingId!: number;
  @Input() bookingReference: string = '';
  @Input() visitorName: string = '';
  @Input() checkInDate: string = '';
  @Input() checkOutDate: string = '';
  @Input() paymentStatus: string = '';
  @Input() bookingStatus: string = '';
  @Input() isTouristMode: boolean = false;
  
  @Output() close = new EventEmitter<void>();
  
  qrImageUrl: SafeUrl | null = null;
  qrLoading = false;
  errorMessage = '';

  constructor(
    private qrService: QRService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadQRCode();
  }

  loadQRCode(): void {
    if (!this.bookingId && !this.isTouristMode) {
      this.errorMessage = 'No booking ID provided.';
      return;
    }

    this.qrLoading = true;
    this.errorMessage = '';

    const request = this.isTouristMode 
      ? this.qrService.getMyBookingQR()
      : this.qrService.getBookingQR(this.bookingId);

    request.subscribe({
      next: (res: any) => {
        this.qrLoading = false;
        if (res?.success && res?.data?.qrCodeBase64) {
          const base64 = res.data.qrCodeBase64;
          this.qrImageUrl = this.sanitizer.bypassSecurityTrustUrl(
            `data:image/png;base64,${base64}`
          );
          // Update booking info from response if available
          if (res.data.bookingReference) {
            this.bookingReference = res.data.bookingReference;
          }
          if (res.data.visitorName) {
            this.visitorName = res.data.visitorName;
          }
        } else {
          this.errorMessage = res?.message || 'Failed to load QR code.';
        }
      },
      error: (err: any) => {
        this.qrLoading = false;
        this.errorMessage = err?.error?.message || 'Failed to load QR code.';
        console.error('QR Code error:', err);
      }
    });
  }

  downloadQR(): void {
    if (!this.qrImageUrl) return;
    const link = document.createElement('a');
    link.download = `QR-${this.bookingReference || 'booking'}.png`;
    link.href = this.qrImageUrl.toString();
    link.click();
  }

  closeModal(): void {
    this.close.emit();
  }
}