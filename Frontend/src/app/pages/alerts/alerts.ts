// src/app/pages/alerts/alerts.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../core/environments/environment';

// ── Types ─────────────────────────────────────────────────────────────────────

export type AlertStatus = 'PENDING' | 'RESPONDING' | 'RESOLVED' | 'FALSE_ALARM';
export type AlertType =
  | 'MEDICAL'
  | 'WILDLIFE_ENCOUNTER'
  | 'VEHICLE_BREAKDOWN'
  | 'GENERAL_DISTRESS'
  | 'ACCIDENT'
  | 'LOST';
export type Priority = 'HIGH' | 'MEDIUM' | 'LOW';

export interface EmergencyAlert {
  id: number;
  alertStatus: AlertStatus;
  alertType: AlertType;
  assignedRangerId: number | null;
  assignedRangerName: string | null;
  createdAt: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  latitude: number;
  longitude: number;
  message: string;
  priority: Priority;
  resolutionNotes: string | null;
  resolvedAt: string | null;
  respondedAt: string | null;
  responseTimeSeconds: number | null;
  sessionId: number;
  visitorName: string;
  visitorPhone: string;
}

export interface Ranger {
  id: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  role: string;
  active: boolean;
  verified: boolean;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  statusCode: number;
}

export interface AlertStats {
  active: number;
  pending: number;
  resolvedToday: number;
  avgResponseTime: string;
  successRate: number;
}

// ── Alert Service ─────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class AlertService {
  constructor(private http: HttpClient) { }

  getAllAlerts(): Observable<ApiResponse<EmergencyAlert[]>> {
    return this.http.get<ApiResponse<EmergencyAlert[]>>(
      `${environment.apiUrl}/v1/admin/alerts`
    );
  }

  getActiveAlerts(): Observable<ApiResponse<EmergencyAlert[]>> {
    return this.http.get<ApiResponse<EmergencyAlert[]>>(
      `${environment.apiUrl}/v1/admin/alerts/active`
    );
  }

  getAvailableRangers(): Observable<ApiResponse<Ranger[]>> {
    return this.http.get<ApiResponse<Ranger[]>>(
      `${environment.apiUrl}/v1/admin/rangers/available`
    );
  }

  assignRanger(alertId: number, rangerId: number): Observable<ApiResponse<EmergencyAlert>> {
    return this.http.put<ApiResponse<EmergencyAlert>>(
      `${environment.apiUrl}/v1/admin/assign-ranger/${alertId}/${rangerId}`,
      {}
    );
  }

  resolveAlert(alertId: number, notes: string): Observable<ApiResponse<EmergencyAlert>> {
    return this.http.put<ApiResponse<EmergencyAlert>>(
      `${environment.apiUrl}/v1/admin/resolve-alert/${alertId}`,
      { notes }
    );
  }

  getAlertById(alertId: number): Observable<ApiResponse<EmergencyAlert>> {
    return this.http.get<ApiResponse<EmergencyAlert>>(
      `${environment.apiUrl}/v1/admin/alerts/${alertId}`
    );
  }
}

// ── Pure Helpers ──────────────────────────────────────────────────────────────

export function getInitials(name: string): string {
  return (name || '?')
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

export function formatCoordinates(lat: number, lng: number): string {
  const latDir = lat >= 0 ? 'N' : 'S';
  const lngDir = lng >= 0 ? 'E' : 'W';
  return `${Math.abs(lat).toFixed(4)}° ${latDir}, ${Math.abs(lng).toFixed(4)}° ${lngDir}`;
}

export function formatTime(isoString: string): { time: string; relative: string } {
  const date = new Date(isoString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHrs = Math.floor(diffMins / 60);
  const time = date.toTimeString().slice(0, 8);

  let relative: string;
  if (diffMins < 1) relative = 'Just now';
  else if (diffMins < 60) relative = `${diffMins}m ago`;
  else if (diffHrs < 24) relative = `${diffHrs}h ${diffMins % 60}m ago`;
  else relative = `${Math.floor(diffHrs / 24)}d ago`;

  return { time, relative };
}

export function humanizeAlertType(type: AlertType): string {
  const map: Record<AlertType, string> = {
    MEDICAL: 'Medical',
    WILDLIFE_ENCOUNTER: 'Wildlife Encounter',
    VEHICLE_BREAKDOWN: 'Vehicle Breakdown',
    GENERAL_DISTRESS: 'General Distress',
    ACCIDENT: 'Accident',
    LOST: 'Lost',
  };
  return map[type] ?? type;
}

export function alertTypeClass(type: AlertType): string {
  const map: Partial<Record<AlertType, string>> = {
    MEDICAL: 'alert-type-label--medical',
    WILDLIFE_ENCOUNTER: 'alert-type-label--wildlife',
    ACCIDENT: 'alert-type-label--accident',
  };
  return map[type] ?? '';
}

export function statusLabel(status: AlertStatus): string {
  const map: Record<AlertStatus, string> = {
    PENDING: 'Pending',
    RESPONDING: 'Responding',
    RESOLVED: 'Resolved',
    FALSE_ALARM: 'False Alarm',
  };
  return map[status] ?? status;
}

export function avatarClass(index: number): string {
  return `avatar--${index % 8}`;
}

export function computeStats(alerts: EmergencyAlert[]): AlertStats {
  const today = new Date().toDateString();

  const active = alerts.filter(
    (a) => a.alertStatus === 'PENDING' || a.alertStatus === 'RESPONDING'
  ).length;

  const pending = alerts.filter((a) => a.alertStatus === 'PENDING').length;

  const resolvedToday = alerts.filter(
    (a) =>
      (a.alertStatus === 'RESOLVED' || a.alertStatus === 'FALSE_ALARM') &&
      new Date(a.createdAt).toDateString() === today
  ).length;

  const responded = alerts.filter((a) => a.responseTimeSeconds !== null);
  const avgSecs =
    responded.length > 0
      ? responded.reduce((sum, a) => sum + (a.responseTimeSeconds ?? 0), 0) /
      responded.length
      : 0;
  const avgResponseTime =
    avgSecs > 0
      ? avgSecs < 60
        ? `${Math.round(avgSecs)}s`
        : `${(avgSecs / 60).toFixed(1)}m`
      : '—';

  const total = alerts.length;
  const closed = alerts.filter(
    (a) => a.alertStatus === 'RESOLVED' || a.alertStatus === 'FALSE_ALARM'
  ).length;
  const successRate = total > 0 ? Math.round((closed / total) * 100) : 0;

  return { active, pending, resolvedToday, avgResponseTime, successRate };
}

// ── Angular Component ──────────────────────────────────────────────────────────

@Component({
  selector: 'app-alerts',
  templateUrl: './alerts.html',
  styleUrls: ['./alerts.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ],
})
export class AlertsComponent implements OnInit, OnDestroy {

  // ── State ──────────────────────────────────────────────────────────────────
  alerts: EmergencyAlert[] = [];
  stats: AlertStats = {
    active: 0,
    pending: 0,
    resolvedToday: 0,
    avgResponseTime: '—',
    successRate: 0,
  };

  isLoading = false;
  errorMessage = '';

  // ── Filters ────────────────────────────────────────────────────────────────
  searchQuery = '';
  typeFilter: AlertType | '' = '';
  statusFilter: AlertStatus | '' = '';

  // ── Popup State ────────────────────────────────────────────────────────────
  selectedAlert: EmergencyAlert | null = null;
  showPopup = false;
  popupMode: 'view' | 'assign' | 'resolve' = 'view';

  // ── Assignment State ──────────────────────────────────────────────────────
  availableRangers: Ranger[] = [];
  selectedRangerId: number | null = null;
  isAssigning = false;

  // ── Resolve State ──────────────────────────────────────────────────────────
  resolveNotes = '';
  isResolving = false;

  // ── Auto-refresh ───────────────────────────────────────────────────────────
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  // ── Expose helpers to the template ────────────────────────────────────────
  readonly getInitials = getInitials;
  readonly formatCoordinates = formatCoordinates;
  readonly formatTime = formatTime;
  readonly humanizeAlertType = humanizeAlertType;
  readonly alertTypeClass = alertTypeClass;
  readonly statusLabel = statusLabel;
  readonly avatarClass = avatarClass;

  constructor(private alertService: AlertService) { }

  // ── Lifecycle ──────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.loadAlerts();
    this.refreshInterval = setInterval(() => this.loadAlerts(), 30_000);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  // ── Data Fetching ──────────────────────────────────────────────────────────
  loadAlerts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.alertService.getAllAlerts().subscribe({
      next: (res) => {
        if (!res.success) {
          this.errorMessage = res.message || 'Failed to load alerts.';
        } else {
          this.alerts = res.data ?? [];
          this.stats = computeStats(this.alerts);
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage =
          err?.error?.message ?? 'Network error — could not reach the server.';
        this.isLoading = false;
      },
    });
  }

  // ── Computed: filtered list ────────────────────────────────────────────────
  get filteredAlerts(): EmergencyAlert[] {
    return this.alerts.filter((a) => {
      const q = this.searchQuery.toLowerCase();
      const matchesSearch =
        !q ||
        a.visitorName.toLowerCase().includes(q) ||
        a.visitorPhone.toLowerCase().includes(q) ||
        a.emergencyContactName.toLowerCase().includes(q) ||
        a.id.toString().includes(q);

      const matchesType = !this.typeFilter || a.alertType === this.typeFilter;
      const matchesStatus = !this.statusFilter || a.alertStatus === this.statusFilter;

      return matchesSearch && matchesType && matchesStatus;
    });
  }

  // ── Stat shorthands ────────────────────────────────────────────────────────
  get activeCount(): number { return this.stats.active; }
  get pendingCount(): number { return this.stats.pending; }
  get resolvedCount(): number { return this.stats.resolvedToday; }

  // ── View Alert Detail ──────────────────────────────────────────────────────
  viewAlert(alert: EmergencyAlert): void {
    this.selectedAlert = alert;
    this.popupMode = 'view';
    this.showPopup = true;
    this.resolveNotes = '';
    this.selectedRangerId = null;
  }

  // ── Assign Ranger ──────────────────────────────────────────────────────────
  openAssignPopup(alert: EmergencyAlert): void {
    this.selectedAlert = alert;
    this.popupMode = 'assign';
    this.showPopup = true;
    this.selectedRangerId = null;
    this.isAssigning = false;
    this.loadAvailableRangers();
  }

  loadAvailableRangers(): void {
    this.alertService.getAvailableRangers().subscribe({
      next: (res) => {
        if (res.success) {
          this.availableRangers = res.data ?? [];
        } else {
          this.availableRangers = [];
        }
      },
      error: () => {
        this.availableRangers = [];
      }
    });
  }

  assignRanger(): void {
    if (!this.selectedAlert || !this.selectedRangerId) {
      alert('Please select a ranger to assign.');
      return;
    }

    this.isAssigning = true;
    this.alertService.assignRanger(this.selectedAlert.id, this.selectedRangerId).subscribe({
      next: (res) => {
        this.isAssigning = false;
        if (res.success) {
          alert('Ranger assigned successfully!');
          this.closePopup();
          this.loadAlerts();
        } else {
          alert(res.message || 'Failed to assign ranger.');
        }
      },
      error: (err) => {
        this.isAssigning = false;
        alert(err?.error?.message || 'Failed to assign ranger.');
      }
    });
  }

  // ── Resolve Alert ──────────────────────────────────────────────────────────
  openResolvePopup(alert: EmergencyAlert): void {
    this.selectedAlert = alert;
    this.popupMode = 'resolve';
    this.showPopup = true;
    this.resolveNotes = '';
    this.isResolving = false;
  }

  resolveAlert(): void {
    if (!this.selectedAlert) return;

    if (!this.resolveNotes.trim()) {
      alert('Please provide resolution notes.');
      return;
    }

    this.isResolving = true;
    this.alertService.resolveAlert(this.selectedAlert.id, this.resolveNotes).subscribe({
      next: (res) => {
        this.isResolving = false;
        if (res.success) {
          alert('Alert resolved successfully!');
          this.closePopup();
          this.loadAlerts();
        } else {
          alert(res.message || 'Failed to resolve alert.');
        }
      },
      error: (err) => {
        this.isResolving = false;
        alert(err?.error?.message || 'Failed to resolve alert.');
      }
    });
  }

  // ── Popup Controls ─────────────────────────────────────────────────────────
  closePopup(): void {
    this.showPopup = false;
    this.selectedAlert = null;
    this.selectedRangerId = null;
    this.resolveNotes = '';
    this.popupMode = 'view';
  }

  // ── Export CSV ─────────────────────────────────────────────────────────────
  exportLog(): void {
    const headers = [
      'ID', 'Visitor', 'Phone', 'Type', 'Status', 'Priority',
      'Latitude', 'Longitude', 'Created At', 'Message',
    ];
    const rows = this.alerts.map((a) => [
      a.id, a.visitorName, a.visitorPhone, a.alertType,
      a.alertStatus, a.priority, a.latitude, a.longitude,
      a.createdAt, `"${(a.message || '').replace(/"/g, '""')}"`,
    ]);
    const csv = [headers, ...rows].map((r) => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `dispatch-log-${new Date().toISOString().slice(0, 10)}.csv`;
    anchor.click();
    URL.revokeObjectURL(url);
  }
}