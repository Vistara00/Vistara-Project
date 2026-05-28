// src/app/pages/reports/reports.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.html',
  styleUrls: ['./reports.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ReportsComponent {
  searchQuery = '';
  startDate: string = '';
  endDate: string = '';
  statusFilter: string = '';
  vehicleFilter: string = '';

  reports = [
    { name: 'Marcus Thorne', vehicle: 'Toyota Tacoma', checkin: new Date('2026-05-25T08:14:00'), checkout: new Date('2026-05-25T12:30:00'), duration: '4h 16m', ranger: 'Ranger A', zone: 'North Gate', incident: 'None', status: 'Completed' },
    { name: 'Elena Rodriguez', vehicle: 'Jeep Wrangler', checkin: new Date('2026-05-25T08:45:00'), checkout: null, duration: '—', ranger: 'Ranger B', zone: 'East Trail', incident: 'None', status: 'Inside Park' },
    { name: 'Samuel Lee', vehicle: 'Hiking', checkin: new Date('2026-05-25T09:12:00'), checkout: new Date('2026-05-25T11:00:00'), duration: '1h 48m', ranger: 'Ranger C', zone: 'South Camp', incident: 'Minor SOS', status: 'Completed' }
  ];

  filteredReports = [...this.reports];

  filterReports(): void {
    const query = this.searchQuery.toLowerCase();
    this.filteredReports = this.reports.filter(r => {
      const matchesName = r.name.toLowerCase().includes(query);
      const matchesVehicle = !this.vehicleFilter || r.vehicle.toLowerCase().includes(this.vehicleFilter.toLowerCase());
      const matchesStatus = !this.statusFilter || r.status === this.statusFilter;
      const matchesDate =
        (!this.startDate || new Date(r.checkin) >= new Date(this.startDate)) &&
        (!this.endDate || new Date(r.checkout || r.checkin) <= new Date(this.endDate));
      return matchesName && matchesVehicle && matchesStatus && matchesDate;
    });
  }

  exportCSV(): void {
    console.log('Exporting detailed reports to CSV...');
  }

  exportPDF(): void {
    console.log('Exporting detailed reports to PDF...');
  }
}
