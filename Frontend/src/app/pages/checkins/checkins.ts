// src/app/pages/checkins/checkins.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-checkins',
  templateUrl: './checkins.html',
  styleUrls: ['./checkins.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class CheckinsComponent {
  searchQuery = '';
  visitors = [
    { name: 'Marcus Thorne', vehicle: 'Toyota Tacoma (B-7290)', checkin: new Date('2026-05-28T08:14:00'), checkout: null, status: 'Inside Park' },
    { name: 'Elena Rodriguez', vehicle: 'Jeep Wrangler (R-9921)', checkin: new Date('2026-05-28T08:45:00'), checkout: null, status: 'Inside Park' },
    { name: 'Samuel Lee', vehicle: 'No Vehicle (Hiking)', checkin: new Date('2026-05-28T09:12:00'), checkout: new Date('2026-05-28T11:00:00'), status: 'Checked Out' },
    { name: 'Linda Chen', vehicle: 'Subaru Outback (C-4402)', checkin: new Date('2026-05-28T09:30:00'), checkout: null, status: 'Inside Park' }
  ];

  filteredVisitors = [...this.visitors];

  get totalCheckins(): number {
    return this.visitors.length;
  }

  get totalCheckedOut(): number {
    return this.visitors.filter(v => v.status === 'Checked Out').length;
  }

  get currentlyInside(): number {
    return this.visitors.filter(v => v.status === 'Inside Park').length;
  }

  searchVisitor(): void {
    const query = this.searchQuery.toLowerCase();
    this.filteredVisitors = this.visitors.filter(v =>
      v.name.toLowerCase().includes(query) || v.vehicle.toLowerCase().includes(query)
    );
  }
}
