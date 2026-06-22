import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportsComponent } from './reports';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

// Stub Chart.js globally
class MockChart {
  constructor(ctx: any, config: any) {}
  destroy() {}
}

// Replace Chart with mock before tests run
(globalThis as any).Chart = MockChart;

describe('ReportsComponent', () => {
  let component: ReportsComponent;
  let fixture: ComponentFixture<ReportsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FormsModule, ReportsComponent] // standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(ReportsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter reports by search query', () => {
    component.searchQuery = 'Elena';
    component.filterReports();
    expect(component.filteredReports.length).toBe(1);
    expect(component.filteredReports[0].name).toBe('Elena Rodriguez');
  });

  it('should reset filters', () => {
    component.searchQuery = 'Marcus';
    component.filterReports();
    expect(component.filteredReports.length).toBe(1);

    component.resetFilters();
    expect(component.filteredReports.length).toBe(component.reports.length);
    expect(component.searchQuery).toBe('');
  });

  it('should export CSV without throwing', () => {
    expect(() => component.exportCSV()).not.toThrow();
  });

  it('should export PDF without throwing', () => {
    expect(() => component.exportPDF()).not.toThrow();
  });

  it('should print view without throwing', () => {
    expect(() => component.printView()).not.toThrow();
  });
});
