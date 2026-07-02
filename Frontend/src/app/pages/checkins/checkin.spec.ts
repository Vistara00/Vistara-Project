// src/app/pages/checkin/checkin.spec.ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckinComponent } from './checkin';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('CheckinComponent', () => {
  let component: CheckinComponent;
  let fixture: ComponentFixture<CheckinComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, CheckinComponent] // standalone component
    }).compileComponents();

    fixture = TestBed.createComponent(CheckinComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // Mock data for testing
  const mockBookings = [
    {
      id: 1,
      bookingReference: 'VST26070211590719',
      userFullName: 'Juma',
      userEmail: 'juma@example.com',
      userPhoneNumber: '254712541948',
      amount: 100.00,
      checkInDate: '2026-07-02',
      checkOutDate: '2026-07-03',
      bookingStatus: 'CONFIRMED',
      paymentStatus: 'PAID',
      vehicleRegistration: 'KCS 122G',
      groupSize: 1,
      notes: ''
    },
    {
      id: 2,
      bookingReference: 'VST26070214078316',
      userFullName: 'Denzel',
      userEmail: 'denzel@example.com',
      userPhoneNumber: '254112552334',
      amount: 5.00,
      checkInDate: '2026-07-02',
      checkOutDate: '2026-07-03',
      bookingStatus: 'CONFIRMED',
      paymentStatus: 'PAID',
      vehicleRegistration: 'KCZ 123A',
      groupSize: 2,
      notes: 'Checked in at gate 1'
    }
  ];

  it('should filter bookings by search query', () => {
    // Set mock data
    component.bookings = mockBookings;
    
    // Test filtering by name
    component.searchQuery = 'Juma';
    const filtered = component.filteredBookings;
    expect(filtered.length).toBe(1);
    expect(filtered[0].userFullName).toBe('Juma');
    
    // Test filtering by booking reference
    component.searchQuery = 'VST26070214078316';
    const filtered2 = component.filteredBookings;
    expect(filtered2.length).toBe(1);
    expect(filtered2[0].userFullName).toBe('Denzel');
  });

  it('should return all bookings when search query is empty', () => {
    component.bookings = mockBookings;
    component.searchQuery = '';
    const filtered = component.filteredBookings;
    expect(filtered.length).toBe(2);
  });

  it('should calculate total pending correctly', () => {
    component.bookings = mockBookings;
    expect(component.totalPending).toBe(2);
  });

  it('should calculate scheduled today correctly', () => {
    component.bookings = mockBookings;
    // Both bookings have checkInDate of '2026-07-02'
    const today = new Date().toISOString().split('T')[0];
    // Since we're using mock data, we need to account for the actual date
    // This test might need to be adjusted based on the current date
    const expectedCount = mockBookings.filter(b => b.checkInDate === today).length;
    expect(component.scheduledToday).toBe(expectedCount);
  });

  it('should calculate overdue checkins correctly', () => {
    component.bookings = mockBookings;
    const today = new Date().toISOString().split('T')[0];
    const expectedCount = mockBookings.filter(b => b.checkInDate < today).length;
    expect(component.overdueCheckins).toBe(expectedCount);
  });
});