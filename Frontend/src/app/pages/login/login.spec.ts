import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from '../../core/auth.service';
import { of } from 'rxjs';

class MockAuthService {
  saveToken(token: string, expiry: number) {}
  logout() {}
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: MockAuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, ReactiveFormsModule, RouterTestingModule, LoginComponent],
      providers: [{ provide: AuthService, useClass: MockAuthService }]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with email and password controls', () => {
    expect(component.loginForm.contains('email')).toBeTrue();
    expect(component.loginForm.contains('password')).toBeTrue();
  });

  it('should toggle password visibility', () => {
    expect(component.showPassword).toBeFalse();
    component.togglePassword();
    expect(component.showPassword).toBeTrue();
  });

  it('should show error if form invalid on login', () => {
    component.loginForm.setValue({ email: '', password: '' });
    component.onLogin();
    expect(component.errorMessage).toBe('Please fill in all required fields correctly.');
  });

  it('should call AuthService.saveToken when token exists', () => {
    spyOn(authService, 'saveToken');
    const mockResponse = { data: { token: 'abc123' } };

    // simulate login success
    (component as any).http.post = () => of(mockResponse);

    component.loginForm.setValue({ email: 'test@example.com', password: '123456' });
    component.onLogin();

    expect(authService.saveToken).toHaveBeenCalledWith('abc123', 3600);
  });
});
