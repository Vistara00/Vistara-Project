// src/app/pages/dashboard/dashboard-home.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
  styleUrls: ['./dashboard-home.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class DashboardHomeComponent {
  // Summary metrics
  totalVisitorsToday = 1284;
  activeAlerts = 3;
  parkOccupancy = 26;
  dailyBookingValue = 14520;

  // Visitor flow data (for chart)
  visitorFlow = [
    { time: '08 AM', count: 180 },
    { time: '10 AM', count: 320 },
    { time: '12 PM', count: 480 },
    { time: '02 PM', count: 410 },
    { time: '04 PM', count: 300 },
    { time: '06 PM', count: 220 }
  ];

  // Weather widget
  weather = {
    temperature: 24,
    windSpeed: 12,
    precipitation: 5,
    condition: 'Sunny'
  };

  // Recent activity
  recentActivity = [
    { icon: 'fa-ticket-alt', text: 'Ticket #1023 Verified' },
    { icon: 'fa-satellite-dish', text: 'Ranger Uplink Active' },
    { icon: 'fa-users', text: 'New Group Booking #TB-03' },
    { icon: 'fa-broadcast-tower', text: 'SOS Broadcast Sent at 09:30 AM' },
    { icon: 'fa-ticket-alt', text: 'Ticket #1024 Verified' }
  ];

  // Quick actions
  quickActions = [
    { label: 'Broadcast SOS', icon: 'fa-bullhorn', color: 'red' },
    { label: 'New Booking', icon: 'fa-calendar-plus', color: 'green' },
    { label: 'Export Report', icon: 'fa-file-export', color: 'gray' }
  ];

  // Helper to get peak time
  get peakTime(): string {
    const max = Math.max(...this.visitorFlow.map(v => v.count));
    const peak = this.visitorFlow.find(v => v.count === max);
    return peak ? peak.time : '';
  }
}
