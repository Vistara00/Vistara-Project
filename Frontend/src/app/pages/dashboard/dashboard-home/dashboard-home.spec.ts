// src/app/pages/dashboard/dashboard-home.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardHomeComponent } from './dashboard-home';

describe('DashboardHomeComponent', () => {
  let component: DashboardHomeComponent;
  let fixture: ComponentFixture<DashboardHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardHomeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardHomeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate peak visitor time correctly', () => {
    const expectedPeak = '12 PM'; // highest count in visitorFlow
    expect(component.peakTime).toBe(expectedPeak);
  });

  it('should have summary metrics defined', () => {
    expect(component.totalVisitorsToday).toBeGreaterThan(0);
    expect(component.activeAlerts).toBeGreaterThanOrEqual(0);
    expect(component.parkOccupancy).toBeGreaterThanOrEqual(0);
    expect(component.dailyBookingValue).toBeGreaterThan(0);
  });

  it('should provide visitor flow data', () => {
    expect(component.visitorFlow.length).toBeGreaterThan(0);
    expect(component.visitorFlow[0]).toHaveProperty('time');
    expect(component.visitorFlow[0]).toHaveProperty('count');
  });

  it('should provide recent activity items', () => {
    expect(component.recentActivity.length).toBeGreaterThan(0);
    expect(component.recentActivity[0]).toHaveProperty('icon');
    expect(component.recentActivity[0]).toHaveProperty('text');
  });

  it('should provide quick actions', () => {
    expect(component.quickActions.length).toBeGreaterThan(0);
    expect(component.quickActions[0]).toHaveProperty('label');
    expect(component.quickActions[0]).toHaveProperty('icon');
    expect(component.quickActions[0]).toHaveProperty('color');
  });
});
