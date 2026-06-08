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
  statusFilter = 'All Status';

  alerts = [
    {
      name: 'Elena Brooks',
      location: 'North Trail – Sector 4',
      contact: '+1 555‑0101',
      coordinates: '85.85° AM',
      time: '04:48 AM',
      status: 'Active'
    },
    {
      name: 'James Miller',
      location: 'East Gorge – Sector 9',
      contact: '+1 555‑0102',
      coordinates: '34.8891° N, 118.2612° W',
      time: '10:41 AM',
      status: 'Pending'
    },
    {
      name: 'Sarah Lane',
      location: 'Central Lake – Sector 2',
      contact: '+1 555‑0103',
      coordinates: '34.1012° N, 118.1945° W',
      time: '10:12 AM',
      status: 'Resolved'
    }
  ];

  get activeAlerts(): number {
    return this.alerts.filter(a => a.status === 'Active').length;
  }

  get pendingAlerts(): number {
    return this.alerts.filter(a => a.status === 'Pending').length;
  }

  get resolvedAlerts(): number {
    return this.alerts.filter(a => a.status === 'Resolved').length;
  }

  get filteredAlerts() {
    return this.alerts.filter(a => {
      const matchesSearch =
        a.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        a.contact.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesStatus =
        this.statusFilter === 'All Status' || a.status === this.statusFilter;
      return matchesSearch && matchesStatus;
    });
  }
}
