// src/app/pages/rangers/rangers.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../core/environments/environment';

// ── Types ─────────────────────────────────────────────────────────────────────

export interface Ranger {
  id: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  nationalId: string;
  active: boolean;
  verified: boolean;
  role: string;
}

export interface RangerDetail extends Ranger {
  createdAt: string;
  updatedAt: string;
  totalAssignedAlerts: number;
  activeAlerts: number;
  resolvedAlerts: number;
  recentAlerts: EmergencyAlert[];
}

export interface EmergencyAlert {
  id: number;
  alertStatus: string;
  alertType: string;
  visitorName: string;
  visitorPhone: string;
  latitude: number;
  longitude: number;
  message: string;
  createdAt: string;
  resolvedAt: string | null;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  statusCode: number;
}

// ── Ranger Service ──────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class RangerService {
  constructor(private http: HttpClient) { }

  getAllRangers(): Observable<ApiResponse<Ranger[]>> {
    return this.http.get<ApiResponse<Ranger[]>>(
      `${environment.apiUrl}/v1/admin/rangers`
    );
  }

  getAvailableRangers(): Observable<ApiResponse<Ranger[]>> {
    return this.http.get<ApiResponse<Ranger[]>>(
      `${environment.apiUrl}/v1/admin/rangers/available`
    );
  }

  getRangerById(id: number): Observable<ApiResponse<RangerDetail>> {
    return this.http.get<ApiResponse<RangerDetail>>(
      `${environment.apiUrl}/v1/admin/rangers/${id}`
    );
  }

  getRangerAlerts(id: number): Observable<ApiResponse<EmergencyAlert[]>> {
    return this.http.get<ApiResponse<EmergencyAlert[]>>(
      `${environment.apiUrl}/v1/admin/rangers/${id}/alerts`
    );
  }

  updateRangerStatus(id: number, active: boolean): Observable<ApiResponse<Ranger>> {
    return this.http.put<ApiResponse<Ranger>>(
      `${environment.apiUrl}/v1/admin/rangers/${id}/status`,
      { active }
    );
  }
}

// ── Component ──────────────────────────────────────────────────────────────

@Component({
  selector: 'app-rangers',
  templateUrl: './rangers.html',
  styleUrls: ['./rangers.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class RangersComponent implements OnInit {
  rangers: Ranger[] = [];
  filteredRangers: Ranger[] = [];
  selectedRanger: RangerDetail | null = null;
  selectedRangerAlerts: EmergencyAlert[] = [];
  
  isLoading = false;
  errorMessage = '';
  searchQuery = '';
  showDetailPopup = false;
  showAlertsPopup = false;
  isTogglingStatus = false;

  constructor(private rangerService: RangerService) { }

  ngOnInit(): void {
    this.loadRangers();
  }

  loadRangers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.rangerService.getAllRangers().subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.rangers = res.data || [];
          this.applyFilter();
        } else {
          this.errorMessage = res.message || 'Failed to load rangers.';
        }
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || 'Network error.';
      }
    });
  }

  applyFilter(): void {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) {
      this.filteredRangers = this.rangers;
    } else {
      this.filteredRangers = this.rangers.filter(r =>
        r.fullName.toLowerCase().includes(q) ||
        r.email.toLowerCase().includes(q) ||
        r.phoneNumber.includes(q)
      );
    }
  }

  viewRangerDetails(id: number): void {
    this.isLoading = true;
    this.rangerService.getRangerById(id).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.selectedRanger = res.data;
          this.showDetailPopup = true;
        } else {
          alert(res.message || 'Failed to load ranger details.');
        }
      },
      error: (err) => {
        this.isLoading = false;
        alert(err?.error?.message || 'Failed to load ranger details.');
      }
    });
  }

  viewRangerAlerts(id: number): void {
    this.isLoading = true;
    this.rangerService.getRangerAlerts(id).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.selectedRangerAlerts = res.data || [];
          this.showAlertsPopup = true;
        } else {
          alert(res.message || 'Failed to load ranger alerts.');
        }
      },
      error: (err) => {
        this.isLoading = false;
        alert(err?.error?.message || 'Failed to load ranger alerts.');
      }
    });
  }

  toggleRangerStatus(ranger: Ranger): void {
    const newStatus = !ranger.active;
    const action = newStatus ? 'activate' : 'deactivate';
    
    if (!confirm(`Are you sure you want to ${action} ${ranger.fullName}?`)) {
      return;
    }

    this.isTogglingStatus = true;
    this.rangerService.updateRangerStatus(ranger.id, newStatus).subscribe({
      next: (res) => {
        this.isTogglingStatus = false;
        if (res.success) {
          alert(`Ranger ${action}d successfully!`);
          this.loadRangers();
        } else {
          alert(res.message || `Failed to ${action} ranger.`);
        }
      },
      error: (err) => {
        this.isTogglingStatus = false;
        alert(err?.error?.message || `Failed to ${action} ranger.`);
      }
    });
  }

  closeDetailPopup(): void {
    this.showDetailPopup = false;
    this.selectedRanger = null;
  }

  closeAlertsPopup(): void {
    this.showAlertsPopup = false;
    this.selectedRangerAlerts = [];
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'PENDING': 'status-pending',
      'RESPONDING': 'status-responding',
      'RESOLVED': 'status-resolved',
      'FALSE_ALARM': 'status-false'
    };
    return map[status] || '';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      'PENDING': 'Pending',
      'RESPONDING': 'Responding',
      'RESOLVED': 'Resolved',
      'FALSE_ALARM': 'False Alarm'
    };
    return map[status] || status;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }
}