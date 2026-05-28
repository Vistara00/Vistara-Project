// src/app/pages/records/records.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-records',
  templateUrl: './records.html',
  styleUrls: ['./records.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class RecordsComponent {
  searchQuery = '';
  records = [
    { name: 'Marcus Thorne', vehicle: 'Toyota Tacoma (B-7290)', checkin: new Date('2026-05-25T08:14:00'), checkout: new Date('2026-05-25T12:30:00'), status: 'Completed' },
    { name: 'Elena Rodriguez', vehicle: 'Jeep Wrangler (R-9921)', checkin: new Date('2026-05-25T08:45:00'), checkout: null, status: 'Inside Park' },
    { name: 'Samuel Lee', vehicle: 'Hiking (No Vehicle)', checkin: new Date('2026-05-25T09:12:00'), checkout: new Date('2026-05-25T11:00:00'), status: 'Completed' },
    { name: 'Linda Chen', vehicle: 'Subaru Outback (C-4402)', checkin: new Date('2026-05-25T09:30:00'), checkout: null, status: 'Inside Park' }
  ];

  filteredRecords = [...this.records];

  get totalVisitors(): number {
    return this.records.length;
  }

  get currentlyInside(): number {
    return this.records.filter(r => r.status === 'Inside Park').length;
  }

  get visitsThisMonth(): number {
    const now = new Date();
    return this.records.filter(r => r.checkin.getMonth() === now.getMonth()).length;
  }

  searchRecords(): void {
    const query = this.searchQuery.toLowerCase();
    this.filteredRecords = this.records.filter(r =>
      r.name.toLowerCase().includes(query) || r.vehicle.toLowerCase().includes(query)
    );
  }

  exportCSV(): void {
    console.log('Exporting records to CSV...');
  }

  exportPDF(): void {
    console.log('Exporting records to PDF...');
  }
}
