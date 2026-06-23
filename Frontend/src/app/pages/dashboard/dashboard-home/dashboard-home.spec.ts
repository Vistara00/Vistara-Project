// src/app/pages/dashboard/dashboard-home.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardHomeComponent } from './dashboard-home';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DashboardHomeComponent', () => {
  let component: DashboardHomeComponent;
  let fixture: ComponentFixture<DashboardHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, DashboardHomeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have summary metrics defined (initial defaults)', () => {
    expect(component.totalVisitorsToday).toBeDefined();
    expect(component.activeAlerts).toBeDefined();
    expect(component.parkOccupancy).toBeDefined();
    expect(component.dailyBookingValue).toBeDefined();
    expect(component.alertPriority).toBeDefined();
    expect(component.alertStatus).toBeDefined();
  });

  it('should generate daily trend correctly', () => {
    const result = (component as any).generateDailyTrend(0, 2026); // January 2026
    expect(result.length).toBeGreaterThan(0);
    expect(result[0].day).toBe(1);
    expect(result[0].count).toBeGreaterThanOrEqual(50);
  });

  it('should render visitor trend chart even with no data', () => {
    component.dailyVisitors = [];

    // Mock the ViewChild manually
    Object.defineProperty(component, 'visitorTrendChart', {
      get: () => ({
        nativeElement: document.createElement('canvas'),
      }),
    });

    expect(() => (component as any).renderVisitorTrendChart()).not.toThrow();
  });

  it('should have weather object defined', () => {
    expect(component.weather).toBeDefined();
    expect(component.weather.temperature).toBeDefined();
    expect(component.weather.windSpeed).toBeDefined();
    expect(component.weather.precipitation).toBeDefined();
    expect(component.weather.condition).toBeDefined();
  });

  it('should provide recent activity items', () => {
    expect(component.recentActivity.length).toBeGreaterThan(0);
    expect(component.recentActivity[0].icon).toBeDefined();
    expect(component.recentActivity[0].text).toBeDefined();
  });

  it('should provide quick actions', () => {
    expect(component.quickActions.length).toBeGreaterThan(0);
    expect(component.quickActions[0].label).toBeDefined();
    expect(component.quickActions[0].icon).toBeDefined();
    expect(component.quickActions[0].color).toBeDefined();
  });
});
