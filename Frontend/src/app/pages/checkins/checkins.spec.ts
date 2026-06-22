import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckinsComponent } from './checkins';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('CheckinsComponent', () => {
  let component: CheckinsComponent;
  let fixture: ComponentFixture<CheckinsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, CheckinsComponent] // standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(CheckinsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter visitors by search query', () => {
    component.searchQuery = 'Julianne';
    const filtered = component.filteredVisitors;
    expect(filtered.length).toBe(1);
    expect(filtered[0].name).toBe('Julianne Smith');
  });

  it('should filter visitors by zone', () => {
    component.zoneFilter = 'West Lake';
    const filtered = component.filteredVisitors;
    expect(filtered.every(v => v.zone === 'West Lake')).toBe(true);
  });

  it('should filter visitors by status', () => {
    component.statusFilter = 'Scheduled';
    const filtered = component.filteredVisitors;
    expect(filtered.every(v => v.status === 'Scheduled')).toBe(true);
  });
});
