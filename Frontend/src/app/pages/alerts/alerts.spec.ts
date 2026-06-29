// src/app/pages/alerts/alerts.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import {
  AlertsComponent,
  AlertService,
  EmergencyAlert,
  computeStats,
  getInitials,
  formatCoordinates,
  formatTime,
  humanizeAlertType,
  alertTypeIcon,
  alertTypeClass,
  statusLabel,
  avatarClass,
} from './alerts';

// ── Shared mock data ───────────────────────────────────────────────────────────

const MOCK_ALERTS: EmergencyAlert[] = [
  {
    id: 1,
    alertStatus: 'PENDING',
    alertType: 'MEDICAL',
    assignedRangerId: null,
    assignedRangerName: null,
    createdAt: new Date().toISOString(),
    emergencyContactName: 'Jane Doe',
    emergencyContactPhone: '0700000001',
    latitude: -1.2921,
    longitude: 36.8219,
    message: 'Chest pain reported',
    priority: 'HIGH',
    resolutionNotes: null,
    resolvedAt: null,
    respondedAt: null,
    responseTimeSeconds: null,
    sessionId: 101,
    visitorName: 'Elena Kamau',
    visitorPhone: '0711111111',
  },
  {
    id: 2,
    alertStatus: 'RESPONDING',
    alertType: 'WILDLIFE_ENCOUNTER',
    assignedRangerId: 5,
    assignedRangerName: 'Ranger Tom',
    createdAt: new Date().toISOString(),
    emergencyContactName: 'Bob Smith',
    emergencyContactPhone: '0700000002',
    latitude: -1.3000,
    longitude: 36.8300,
    message: 'Lion spotted near camp',
    priority: 'HIGH',
    resolutionNotes: null,
    resolvedAt: null,
    respondedAt: new Date().toISOString(),
    responseTimeSeconds: 120,
    sessionId: 102,
    visitorName: 'Brian Omondi',
    visitorPhone: '0722222222',
  },
  {
    id: 3,
    alertStatus: 'RESOLVED',
    alertType: 'LOST',
    assignedRangerId: 3,
    assignedRangerName: 'Ranger Sue',
    createdAt: new Date().toISOString(),
    emergencyContactName: 'Mary Njeri',
    emergencyContactPhone: '0700000003',
    latitude: -1.2800,
    longitude: 36.8100,
    message: 'Visitor found safe',
    priority: 'MEDIUM',
    resolutionNotes: 'Escorted back to camp',
    resolvedAt: new Date().toISOString(),
    respondedAt: new Date().toISOString(),
    responseTimeSeconds: 300,
    sessionId: 103,
    visitorName: 'Carol Wanjiku',
    visitorPhone: '0733333333',
  },
  {
    id: 4,
    alertStatus: 'FALSE_ALARM',
    alertType: 'VEHICLE_BREAKDOWN',
    assignedRangerId: null,
    assignedRangerName: null,
    createdAt: new Date().toISOString(),
    emergencyContactName: 'Peter Mwangi',
    emergencyContactPhone: '0700000004',
    latitude: -1.3100,
    longitude: 36.8400,
    message: 'False report — vehicle was fine',
    priority: 'LOW',
    resolutionNotes: 'No action needed',
    resolvedAt: new Date().toISOString(),
    respondedAt: null,
    responseTimeSeconds: null,
    sessionId: 104,
    visitorName: 'David Kariuki',
    visitorPhone: '0744444444',
  },
];

const MOCK_API_RESPONSE = {
  success: true,
  message: 'OK',
  data: MOCK_ALERTS,
  timestamp: new Date().toISOString(),
  statusCode: 200,
};

// ── Test setup helper ─────────────────────────────────────────────────────────

function createMockAlertService(overrides?: Partial<{ getAllAlerts: () => any }>) {
  return {
    getAllAlerts: jasmine.createSpy('getAllAlerts').and.returnValue(
      overrides?.getAllAlerts?.() ?? of(MOCK_API_RESPONSE)
    ),
  };
}

// ── Pure helper unit tests ─────────────────────────────────────────────────────

describe('Pure helpers', () => {

  describe('getInitials()', () => {
    it('returns up to 2 uppercase initials', () => {
      expect(getInitials('Elena Kamau')).toBe('EK');
    });
    it('handles a single name', () => {
      expect(getInitials('Brian')).toBe('B');
    });
    it('handles empty string gracefully', () => {
      expect(getInitials('')).toBe('?');
    });
  });

  describe('formatCoordinates()', () => {
    it('formats positive lat/lng as N/E', () => {
      expect(formatCoordinates(1.2921, 36.8219)).toBe('1.2921° N, 36.8219° E');
    });
    it('formats negative lat/lng as S/W', () => {
      expect(formatCoordinates(-1.2921, -36.8219)).toBe('1.2921° S, 36.8219° W');
    });
  });

  describe('formatTime()', () => {
    it('returns "Just now" for a timestamp less than a minute ago', () => {
      const iso = new Date(Date.now() - 30_000).toISOString();
      expect(formatTime(iso).relative).toBe('Just now');
    });
    it('returns minutes ago for timestamps within the hour', () => {
      const iso = new Date(Date.now() - 5 * 60_000).toISOString();
      expect(formatTime(iso).relative).toBe('5m ago');
    });
    it('returns a valid HH:MM:SS time string', () => {
      const iso = new Date().toISOString();
      expect(formatTime(iso).time).toMatch(/^\d{2}:\d{2}:\d{2}$/);
    });
  });

  describe('humanizeAlertType()', () => {
    it('humanizes known types', () => {
      expect(humanizeAlertType('WILDLIFE_ENCOUNTER')).toBe('Wildlife Encounter');
      expect(humanizeAlertType('VEHICLE_BREAKDOWN')).toBe('Vehicle Breakdown');
      expect(humanizeAlertType('MEDICAL')).toBe('Medical');
    });
  });

  describe('alertTypeIcon()', () => {
    it('returns the medical icon for MEDICAL', () => {
      expect(alertTypeIcon('MEDICAL')).toBe('🏥');
    });
    it('returns a fallback for unknown types', () => {
      expect(alertTypeIcon('UNKNOWN' as any)).toBe('❓');
    });
  });

  describe('alertTypeClass()', () => {
    it('returns the correct CSS class for MEDICAL', () => {
      expect(alertTypeClass('MEDICAL')).toBe('alert-type-label--medical');
    });
    it('returns empty string for types with no special class', () => {
      expect(alertTypeClass('LOST')).toBe('');
    });
  });

  describe('statusLabel()', () => {
    it('maps all statuses to human-readable labels', () => {
      expect(statusLabel('PENDING')).toBe('Pending');
      expect(statusLabel('RESPONDING')).toBe('Responding');
      expect(statusLabel('RESOLVED')).toBe('Resolved');
      expect(statusLabel('FALSE_ALARM')).toBe('False Alarm');
    });
  });

  describe('avatarClass()', () => {
    it('cycles through 8 avatar classes', () => {
      expect(avatarClass(0)).toBe('avatar--0');
      expect(avatarClass(8)).toBe('avatar--0');
      expect(avatarClass(9)).toBe('avatar--1');
    });
  });

  describe('computeStats()', () => {
    it('counts active alerts (PENDING + RESPONDING)', () => {
      const stats = computeStats(MOCK_ALERTS);
      expect(stats.active).toBe(2);
    });
    it('counts only PENDING alerts', () => {
      const stats = computeStats(MOCK_ALERTS);
      expect(stats.pending).toBe(1);
    });
    it('counts alerts resolved today', () => {
      const stats = computeStats(MOCK_ALERTS);
      // RESOLVED + FALSE_ALARM both created today in mock data
      expect(stats.resolvedToday).toBe(2);
    });
    it('computes average response time from non-null responseTimeSeconds', () => {
      const stats = computeStats(MOCK_ALERTS);
      // Only alert 2 (120s) and alert 3 (300s) have responseTimeSeconds
      // avg = (120 + 300) / 2 = 210s → 3.5m
      expect(stats.avgResponseTime).toBe('3.5m');
    });
    it('returns — for avgResponseTime when no responses recorded', () => {
      const stats = computeStats([]);
      expect(stats.avgResponseTime).toBe('—');
    });
    it('computes success rate as percentage of closed alerts', () => {
      const stats = computeStats(MOCK_ALERTS);
      // 2 closed (RESOLVED + FALSE_ALARM) out of 4 total = 50%
      expect(stats.successRate).toBe(50);
    });
    it('returns 0 success rate for empty list', () => {
      expect(computeStats([]).successRate).toBe(0);
    });
  });

});

// ── Component tests ───────────────────────────────────────────────────────────

describe('AlertsComponent', () => {
  let component: AlertsComponent;
  let fixture: ComponentFixture<AlertsComponent>;
  let mockAlertService: ReturnType<typeof createMockAlertService>;

  beforeEach(async () => {
    mockAlertService = createMockAlertService();

    await TestBed.configureTestingModule({
      imports: [AlertsComponent],
      providers: [
        { provide: AlertService, useValue: mockAlertService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  // ── Creation ───────────────────────────────────────────────────────────────

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // ── Data loading ───────────────────────────────────────────────────────────

  it('calls AlertService.getAllAlerts on init', () => {
    expect(mockAlertService.getAllAlerts).toHaveBeenCalled();
  });

  it('populates alerts from the API response', () => {
    expect(component.alerts.length).toBe(MOCK_ALERTS.length);
  });

  it('sets isLoading to false after a successful load', () => {
    expect(component.isLoading).toBeFalse();
  });

  it('sets errorMessage on a failed API call', async () => {
    mockAlertService.getAllAlerts.and.returnValue(
      throwError(() => ({ error: { message: 'Unauthorized' } }))
    );
    component.loadAlerts();
    await fixture.whenStable();
    expect(component.errorMessage).toBe('Unauthorized');
  });

  it('falls back to a generic error message when server body is missing', async () => {
    mockAlertService.getAllAlerts.and.returnValue(
      throwError(() => ({}))
    );
    component.loadAlerts();
    await fixture.whenStable();
    expect(component.errorMessage).toBe('Network error — could not reach the server.');
  });

  it('sets errorMessage when success is false', async () => {
    mockAlertService.getAllAlerts.and.returnValue(
      of({ ...MOCK_API_RESPONSE, success: false, message: 'Access denied' })
    );
    component.loadAlerts();
    await fixture.whenStable();
    expect(component.errorMessage).toBe('Access denied');
  });

  // ── Stats shorthands ───────────────────────────────────────────────────────

  it('activeCount reflects PENDING + RESPONDING alerts', () => {
    expect(component.activeCount).toBe(2);
  });

  it('pendingCount reflects only PENDING alerts', () => {
    expect(component.pendingCount).toBe(1);
  });

  it('resolvedCount reflects alerts closed today', () => {
    expect(component.resolvedCount).toBe(2);
  });

  // ── Filtering ──────────────────────────────────────────────────────────────

  it('returns all alerts when no filters are set', () => {
    expect(component.filteredAlerts.length).toBe(MOCK_ALERTS.length);
  });

  it('filters by visitorName search query', () => {
    component.searchQuery = 'elena';
    const filtered = component.filteredAlerts;
    expect(filtered.length).toBe(1);
    expect(filtered[0].visitorName).toBe('Elena Kamau');
  });

  it('filters by visitorPhone search query', () => {
    component.searchQuery = '0722222222';
    const filtered = component.filteredAlerts;
    expect(filtered.length).toBe(1);
    expect(filtered[0].visitorPhone).toBe('0722222222');
  });

  it('filters by emergencyContactName search query', () => {
    component.searchQuery = 'mary';
    const filtered = component.filteredAlerts;
    expect(filtered.length).toBe(1);
    expect(filtered[0].emergencyContactName).toBe('Mary Njeri');
  });

  it('filters by alertStatus', () => {
    component.statusFilter = 'RESOLVED';
    const filtered = component.filteredAlerts;
    expect(filtered.every((a) => a.alertStatus === 'RESOLVED')).toBeTrue();
  });

  it('filters by alertType', () => {
    component.typeFilter = 'MEDICAL';
    const filtered = component.filteredAlerts;
    expect(filtered.every((a) => a.alertType === 'MEDICAL')).toBeTrue();
  });

  it('combines search and status filters correctly', () => {
    component.searchQuery = 'brian';
    component.statusFilter = 'RESPONDING';
    const filtered = component.filteredAlerts;
    expect(filtered.length).toBe(1);
    expect(filtered[0].visitorName).toBe('Brian Omondi');
  });

  it('returns empty array when no alerts match the filters', () => {
    component.searchQuery = 'zzznomatch';
    expect(component.filteredAlerts.length).toBe(0);
  });

  // ── Auto-refresh cleanup ───────────────────────────────────────────────────

  it('clears the refresh interval on destroy', () => {
    spyOn(window, 'clearInterval');
    component.ngOnDestroy();
    expect(clearInterval).toHaveBeenCalled();
  });

});