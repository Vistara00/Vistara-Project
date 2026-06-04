import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-reports',
  templateUrl: './reports.html',
  styleUrls: ['./reports.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ReportsComponent implements OnInit {
  searchQuery = '';
  startDate = '';
  endDate = '';
  statusFilter = '';
  vehicleFilter = '';
  rangerFilter = '';
  zoneFilter = '';

  reports = [
    { name: 'Marcus Thorne', vehicle: 'Toyota Tacoma', checkin: new Date('2026-05-25T08:14:00'), checkout: new Date('2026-05-25T12:30:00'), duration: '4h 16m', ranger: 'Ranger A', zone: 'North Gate', incident: 'None', contact: '555-1234', status: 'Completed' },
    { name: 'Elena Rodriguez', vehicle: 'Jeep Wrangler', checkin: new Date('2026-05-25T08:45:00'), checkout: null, duration: '—', ranger: 'Ranger B', zone: 'East Trail', incident: 'None', contact: '555-5678', status: 'Inside Park' },
    { name: 'Samuel Lee', vehicle: 'Hiking', checkin: new Date('2026-05-25T09:12:00'), checkout: new Date('2026-05-25T11:00:00'), duration: '1h 48m', ranger: 'Ranger C', zone: 'South Camp', incident: 'Minor SOS', contact: '555-9012', status: 'Completed' }
  ];

  filteredReports = [...this.reports];

  ngOnInit(): void {
    this.renderCharts();
  }

  filterReports(): void {
    const query = this.searchQuery.toLowerCase();
    this.filteredReports = this.reports.filter(r => {
      const matchesName = r.name.toLowerCase().includes(query);
      const matchesVehicle = !this.vehicleFilter || r.vehicle.toLowerCase().includes(this.vehicleFilter.toLowerCase());
      const matchesRanger = !this.rangerFilter || r.ranger.toLowerCase().includes(this.rangerFilter.toLowerCase());
      const matchesZone = !this.zoneFilter || r.zone.toLowerCase().includes(this.zoneFilter.toLowerCase());
      const matchesStatus = !this.statusFilter || r.status === this.statusFilter;
      const matchesDate =
        (!this.startDate || new Date(r.checkin) >= new Date(this.startDate)) &&
        (!this.endDate || new Date(r.checkout || r.checkin) <= new Date(this.endDate));
      return matchesName && matchesVehicle && matchesRanger && matchesZone && matchesStatus && matchesDate;
    });
    this.renderCharts();
  }

  resetFilters(): void {
    this.searchQuery = '';
    this.startDate = '';
    this.endDate = '';
    this.statusFilter = '';
    this.vehicleFilter = '';
    this.rangerFilter = '';
    this.zoneFilter = '';
    this.filteredReports = [...this.reports];
    this.renderCharts();
  }

  exportCSV(): void {
    console.log('Exporting detailed reports to CSV...');
  }

  exportPDF(): void {
    console.log('Exporting detailed reports to PDF...');
  }

  printView(): void {
    window.print();
  }

  renderCharts(): void {
    const ctx1 = document.getElementById('visitsChart') as HTMLCanvasElement;
    const ctx2 = document.getElementById('statusChart') as HTMLCanvasElement;

    new Chart(ctx1, {
      type: 'bar',
      data: {
        labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'],
        datasets: [{ label: 'Visits per Day', data: [5, 8, 6, 9, 7], backgroundColor: '#2e7d32' }]
      }
    });

    new Chart(ctx2, {
      type: 'pie',
      data: {
        labels: ['Completed', 'Inside Park', 'Incident'],
        datasets: [{ data: [60, 30, 10], backgroundColor: ['#4caf50', '#ffb300', '#e53935'] }]
      }
    });
  }
}