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
  zoneFilter = 'All Zones';
  statusFilter = 'All Status';

  visitors = [
    { id: 'VT‑B821', name: 'Julianne Smith', zone: 'North Ridge', entry: '08:15 AM', exit: '04:30 PM', status: 'Checked In' },
    { id: 'VT‑B834', name: 'Marcus Beard', zone: 'West Lake', entry: '11:30 AM', exit: '—', status: 'Scheduled' },
    { id: 'VT‑B790', name: 'Knight', zone: 'South Forest', entry: '09:00 AM', exit: '—', status: 'Overstayed' },
    { id: 'VT‑B840', name: 'David Lopez', zone: 'East Plains', entry: '02:30 PM', exit: '—', status: 'Checked In' },
    { id: 'VT‑B845', name: 'Sarah Chen', zone: 'North Ridge', entry: '12:30 PM', exit: '—', status: 'Scheduled' }
  ];

  zones = [
    { name: 'North Ridge Sector', current: 142, capacity: 200 },
    { name: 'West Lake Recreational Area', current: 118, capacity: 150 },
    { name: 'South Forest Sanctuary', current: 67, capacity: 300 }
  ];

  get totalVisitors(): number {
    return 412;
  }

  get scheduledToday(): number {
    return 185;
  }

  get expectedCheckouts(): number {
    return 94;
  }

  get filteredVisitors() {
    return this.visitors.filter(v => {
      const matchesSearch =
        v.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        v.id.toLowerCase().includes(this.searchQuery.toLowerCase());
      const matchesZone = this.zoneFilter === 'All Zones' || v.zone === this.zoneFilter;
      const matchesStatus = this.statusFilter === 'All Status' || v.status === this.statusFilter;
      return matchesSearch && matchesZone && matchesStatus;
    });
  }
}
