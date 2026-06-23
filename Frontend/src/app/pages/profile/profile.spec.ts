import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProfileComponent } from './profile';
import { environment } from '../../core/environments/environment';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileComponent, HttpClientTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch profile data successfully', () => {
    const mockResponse = {
      success: true,
      data: {
        fullName: 'Peter Gichuki',
        email: 'pegek001@gmail.com',
        phoneNumber: '254745407727',
        nationalId: '393943432',
        emergencyContactName: 'James Bwire',
        emergencyContactPhone: '2543232442232'
      }
    };

    component.fetchProfile();
    const req = httpMock.expectOne(`${environment.apiUrl}/v1/profile`);
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);

    expect(component.profileData.fullName).toBe('Peter Gichuki');
    expect(component.initials).toBe('PG');
  });

  it('should set isModified to true when a field changes', () => {
    component.isModified = false;
    component.onFieldChange();
    expect(component.isModified).toBeTrue();
  });

  it('should reset isModified after saving profile', () => {
    const mockResponse = { success: true };
    component.isModified = true;
    component.saveProfile();

    const req = httpMock.expectOne(`${environment.apiUrl}/v1/profile`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockResponse);

    expect(component.isModified).toBeFalse();
  });
});
