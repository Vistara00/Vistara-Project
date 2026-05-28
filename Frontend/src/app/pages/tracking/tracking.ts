// src/app/pages/tracking/tracking.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tracking',
  templateUrl: './tracking.html',
  styleUrls: ['./tracking.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class TrackingComponent implements OnInit {
  searchQuery = '';
  visitors = [
    { name: 'Marcus Thorne', vehicle: 'Toyota Tacoma (B-7290)', lastUpdate: new Date(), status: 'Inside Park' },
    { name: 'Elena Rodriguez', vehicle: 'Jeep Wrangler (R-9921)', lastUpdate: new Date(), status: 'Inside Park' },
    { name: 'Samuel Lee', vehicle: 'Hiking (No Vehicle)', lastUpdate: new Date(), status: 'Inside Park' },
    { name: 'Linda Chen', vehicle: 'Subaru Outback (C-4402)', lastUpdate: new Date(), status: 'Inside Park' }
  ];

  filteredVisitors = [...this.visitors];

  ngOnInit(): void {
    // Later: initialize Leaflet or Mapbox map here
  }

  searchVisitor(): void {
    const query = this.searchQuery.toLowerCase();
    this.filteredVisitors = this.visitors.filter(v =>
      v.name.toLowerCase().includes(query)
    );
  }
}
