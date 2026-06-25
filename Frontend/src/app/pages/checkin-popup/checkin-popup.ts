import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-checkin-popup',
  templateUrl: './checkin-popup.html',
  styleUrls: ['./checkin-popup.css'],
  standalone: true,
  imports: [CommonModule]
})
export class CheckinPopupComponent {
  @Input() booking: any;       // booking details passed from parent
  @Input() show = false;       // controls visibility
  @Output() close = new EventEmitter<void>();
  @Output() checkin = new EventEmitter<void>();
  @Output() checkout = new EventEmitter<void>();

  onClose() {
    this.close.emit();
  }

  onCheckin() {
    this.checkin.emit();
  }

  onCheckout() {
    this.checkout.emit();
  }
}
