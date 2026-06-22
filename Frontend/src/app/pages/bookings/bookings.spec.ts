import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BookingsComponent } from './bookings';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BookingService } from '../../core/services/booking.service';
import { of } from 'rxjs';

describe('BookingsComponent', () => {
  let component: BookingsComponent;
  let fixture: ComponentFixture<BookingsComponent>;
  let bookingServiceSpy: jasmine.SpyObj<BookingService>;

  beforeEach(async () => {
    bookingServiceSpy = jasmine.createSpyObj('BookingService', ['getBookings']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, BookingsComponent], // standalone component goes here
      providers: [{ provide: BookingService, useValue: bookingServiceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(BookingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch bookings on init', () => {
    const mockBookings = { data: [{ bookingReference: 'ABC123', paymentMethod: 'Cash' }] };
    bookingServiceSpy.getBookings.and.returnValue(of(mockBookings));

    component.ngOnInit();

    expect(bookingServiceSpy.getBookings).toHaveBeenCalled();
    expect(component.bookings.length).toBeGreaterThan(0);
  });
});
