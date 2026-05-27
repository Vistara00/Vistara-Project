// src/app/pages/dashboard/dashboard.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: true,
  imports: [CommonModule]
})
export class DashboardComponent implements OnInit {
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

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Replace with real API calls later
  }

  trackAlert(id: string): void {
    // Navigate to tracking view or open a modal
    this.router.navigate(['/alerts', id]);
  }

  openVisitor(id: string): void {
    this.router.navigate(['/visitors', id]);
  }
}
