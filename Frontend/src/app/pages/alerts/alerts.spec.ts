// src/app/pages/alerts/alerts.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlertsComponent } from './alerts';

describe('AlertsComponent', () => {
  let component: AlertsComponent;
  let fixture: ComponentFixture<AlertsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlertsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AlertsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate active alerts correctly', () => {
    expect(component.activeAlerts).toBe(
      component.alerts.filter(a => a.status === 'Active').length
    );
  });

  it('should calculate pending alerts correctly', () => {
    expect(component.pendingAlerts).toBe(
      component.alerts.filter(a => a.status === 'Pending').length
    );
  });

  it('should calculate resolved alerts correctly', () => {
    expect(component.resolvedAlerts).toBe(
      component.alerts.filter(a => a.status === 'Resolved').length
    );
  });

  it('should filter alerts by search query', () => {
    component.searchQuery = 'Elena';
    const filtered = component.filteredAlerts;
    expect(filtered.every(a => a.name.includes('Elena'))).toBeTrue();
  });

  it('should filter alerts by status', () => {
    component.statusFilter = 'Resolved';
    const filtered = component.filteredAlerts;
    expect(filtered.every(a => a.status === 'Resolved')).toBeTrue();
  });
});
