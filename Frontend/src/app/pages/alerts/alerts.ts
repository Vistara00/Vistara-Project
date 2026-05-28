// src/app/pages/alerts/alerts.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-alerts',
  templateUrl: './alerts.html',
  styleUrls: ['./alerts.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class AlertsComponent {
  searchQuery = '';
  alerts = [
    { id: 'sos-1', name: 'John Doe', coords: '34.05, -118.24', time: new Date(), status: 'PENDING' },
    { id: 'sos-2', name: 'Jane Smith', coords: '34.01, -118.49', time: new Date(Date.now() - 2 * 60 * 60 * 1000), status: 'RESOLVED' },
    { id: 'sos-3', name: 'Samuel Lee', coords: '34.07, -118.30', time: new Date(Date.now() - 30 * 60 * 1000), status: 'ACTIVE' }
  ];

  filteredAlerts = [...this.alerts];

  get activeCount(): number {
    return this.alerts.filter(a => a.status === 'ACTIVE').length;
  }

  get pendingCount(): number {
    return this.alerts.filter(a => a.status === 'PENDING').length;
  }

  get resolvedCount(): number {
    return this.alerts.filter(a => a.status === 'RESOLVED').length;
  }

  searchAlert(): void {
    const query = this.searchQuery.toLowerCase();
    this.filteredAlerts = this.alerts.filter(a =>
      a.name.toLowerCase().includes(query) || a.status.toLowerCase().includes(query)
    );
  }

  viewAlert(id: string): void {
    console.log('View alert details:', id);
  }

  assignRanger(id: string): void {
    console.log('Assign ranger to alert:', id);
  }
}
