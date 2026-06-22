import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RecordsComponent } from './records';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

describe('RecordsComponent', () => {
  let component: RecordsComponent;
  let fixture: ComponentFixture<RecordsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FormsModule, RecordsComponent] // standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(RecordsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate total visitors correctly', () => {
    expect(component.totalVisitors).toBe(component.records.length);
  });

  it('should calculate currently inside correctly', () => {
    const insideCount = component.records.filter(r => r.status === 'Inside Park').length;
    expect(component.currentlyInside).toBe(insideCount);
  });

  it('should filter records by search query', () => {
    component.searchQuery = 'Elena';
    component.searchRecords();
    expect(component.filteredRecords.length).toBe(1);
    expect(component.filteredRecords[0].name).toBe('Elena Rodriguez');
  });

  it('should export CSV without throwing', () => {
    expect(() => component.exportCSV()).not.toThrow();
  });

  it('should export PDF without throwing', () => {
    expect(() => component.exportPDF()).not.toThrow();
  });
});
