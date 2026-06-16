import { TestBed } from '@angular/core/testing';
import { HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { of } from 'rxjs';
import { TokenInterceptor } from './token-interceptor';

describe('TokenInterceptor', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('should attach Authorization header when token exists', () => {
    const fakeToken = 'fake-jwt-token';
    localStorage.setItem('authToken', fakeToken);

    const req = new HttpRequest('GET', '/api/test');
    const next: HttpHandlerFn = (r) => {
      expect(r.headers.get('Authorization')).toBe(`Bearer ${fakeToken}`);
      return of({} as any);
    };

    TokenInterceptor(req, next);
  });

  it('should not attach Authorization header when token is missing', () => {
    const req = new HttpRequest('GET', '/api/test');
    const next: HttpHandlerFn = (r) => {
      expect(r.headers.has('Authorization')).toBe(false);
      return of({} as any);
    };

    TokenInterceptor(req, next);
  });

  it('should not attach Authorization header when token is empty string', () => {
    localStorage.setItem('authToken', '   '); // whitespace only
    
    const req = new HttpRequest('GET', '/api/test');
    const next: HttpHandlerFn = (r) => {
      expect(r.headers.has('Authorization')).toBe(false);
      return of({} as any);
    };

    TokenInterceptor(req, next);
  });
});