import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-checkin-popup',
  templateUrl: './checkin-popup.html',
  styleUrls: ['./checkin-popup.css'],
  standalone: true,
  imports: [CommonModule],
  providers: [DatePipe, CurrencyPipe]
})
export class CheckinPopupComponent {
  @Input() booking: any;
  @Input() show = false;

  @Output() close = new EventEmitter<void>();
  @Output() checkin = new EventEmitter<void>();
  @Output() checkout = new EventEmitter<void>();

  /** Tracks whether the copy toast is visible */
  copySuccess = false;
  private copyTimer: any;

  onClose() { this.close.emit(); }
  onCheckin() { this.checkin.emit(); }
  onCheckout() { this.checkout.emit(); }

  /**
   * Copies the booking reference to the clipboard and shows a brief
   * "Copied!" confirmation that auto-dismisses after 2 seconds.
   */
  copyReference(ref: string | undefined): void {
    if (!ref) return;

    navigator.clipboard.writeText(ref).then(() => {
      this.copySuccess = true;
      clearTimeout(this.copyTimer);
      this.copyTimer = setTimeout(() => (this.copySuccess = false), 2000);
    }).catch(() => {
      // Fallback for older browsers / non-HTTPS environments
      const ta = document.createElement('textarea');
      ta.value = ref;
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);

      this.copySuccess = true;
      clearTimeout(this.copyTimer);
      this.copyTimer = setTimeout(() => (this.copySuccess = false), 2000);
    });
  }

  /**
   * Formats an ISO date string into "June 26, 2026" style.
   * Returns the raw value if it can't be parsed.
   */
  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }

  /**
   * Returns the day name ("Friday") for an ISO date string.
   */
  formatDay(dateStr: string | undefined): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleDateString('en-US', { weekday: 'long' });
  }

  /**
   * Formats an amount number into a currency string ("$1,200.00").
   */
  formatCurrency(amount: number | undefined): string {
    if (amount == null) return '—';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
  }
}