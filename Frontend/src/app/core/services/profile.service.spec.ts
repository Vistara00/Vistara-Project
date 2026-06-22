import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProfileService } from './profile.service';
import { environment } from '../environments/environment';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProfileService]
    });
    service = TestBed.inject(ProfileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should call the correct API URL', () => {
    service.getProfile().subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/v1/profile`);
    expect(req.request.method).toBe('GET');

    // Respond with mock data
    req.flush({ data: { fullName: 'John Doe' } });
  });
});
