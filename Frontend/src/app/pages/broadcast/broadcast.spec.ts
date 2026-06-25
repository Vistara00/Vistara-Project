import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BroadcastComponent } from './broadcast';
import { environment } from '../../core/environments/environment';

describe('BroadcastComponent', () => {
  let component: BroadcastComponent;
  let fixture: ComponentFixture<BroadcastComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, BroadcastComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(BroadcastComponent);
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

  it('should send a broadcast', () => {
    component.broadcastTitle = 'Test Alert';
    component.broadcastMessage = 'This is a test broadcast';
    component.sendBroadcast();

    const req = httpMock.expectOne(`${environment.apiUrl}/v1/broadcast`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true });

    expect(component.broadcastTitle).toBe('');
    expect(component.broadcastMessage).toBe('');
  });

  it('should fetch broadcast history', () => {
    const mockHistory = {
      success: true,
      data: [
        { title: 'Test Alert', message: 'Hello', status: 'Delivered', timestamp: new Date().toISOString() }
      ]
    };

    component.fetchBroadcastHistory();
    const req = httpMock.expectOne(`${environment.apiUrl}/v1/broadcast/history`);
    expect(req.request.method).toBe('GET');
    req.flush(mockHistory);

    expect(component.broadcasts.length).toBe(1);
    expect(component.broadcasts[0].title).toBe('Test Alert');
  });
});
