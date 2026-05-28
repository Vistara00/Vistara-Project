// src/app/pages/dashboard/dashboard-home.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
  styleUrls: ['./dashboard-home.css'],
  standalone: true,
  imports: [CommonModule]
})
export class DashboardHomeComponent implements OnInit {
  stats = {
    activeVisitors: 124,
    checkedInToday: 87,
    activeSOS: 2,
    rescuesCompleted: 5
  };

  sosAlerts = [
    { id: 'sos-1', name: 'John Doe', coords: '34.05, -118.24', time: new Date(), status: 'PENDING' },
    { id: 'sos-2', name: 'Jane Smith', coords: '34.01, -118.49', time: new Date(Date.now() - 2 * 60 * 60 * 1000), status: 'RESOLVED' }
  ];

  visitors = [
    { id: 'v1', name: 'Marcus Thorne', checkin: new Date('2026-05-25T08:14:00'), vehicle: 'Toyota Tacoma (B-7290)', status: 'Inside Park' },
    { id: 'v2', name: 'Elena Rodriguez', checkin: new Date('2026-05-25T08:45:00'), vehicle: 'Jeep Wrangler (R-9921)', status: 'Inside Park' },
    { id: 'v3', name: 'Samuel Lee', checkin: new Date('2026-05-25T09:12:00'), vehicle: 'No Vehicle (Hiking)', status: 'Inside Park' },
    { id: 'v4', name: 'Linda Chen', checkin: new Date('2026-05-25T09:30:00'), vehicle: 'Subaru Outback (C-4402)', status: 'Inside Park' }
  ];

  constructor() {}

  ngOnInit(): void {
    // Replace with real API calls later
  }

  trackAlert(id: string): void {
    // For now just log, later you can navigate or open modal
    console.log('Track alert:', id);
  }

  openVisitor(id: string): void {
    console.log('Open visitor details:', id);
  }
}
