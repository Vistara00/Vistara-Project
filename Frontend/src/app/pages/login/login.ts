// src/app/pages/login/login.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service'; 
import { environment } from '../../core/environments/environment';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule ]
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  showPassword = false;
  remember = false;

  // ✅ Correct URL: environment.apiUrl is '/api', so this becomes '/api/auth/login'
  // Proxy will rewrite '/api' to '/api/v1'
  private readonly API_URL = `${environment.apiUrl}/v1/auth/login`;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private authService: AuthService 
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const saved = localStorage.getItem('vistara_email');
    if (saved) {
      this.loginForm.patchValue({ email: saved });
      this.remember = true;
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onRemember(e: Event): void {
    this.remember = (e.target as HTMLInputElement).checked;
    if (!this.remember) {
      localStorage.removeItem('vistara_email');
    }
  }

  onLogin(): void {
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      return;
    }

    this.loading = true;
    const payload = this.loginForm.value;

    console.log('🔍 Login URL:', this.API_URL);
    console.log('📤 Login payload:', { email: payload.email, password: '******' });

    this.http.post(this.API_URL, payload).subscribe({
      next: (res: any) => {
        console.log('✅ Login response:', res);
        this.loading = false;

        const token = res?.data?.token;

        if (token) {
          // ✅ Save token with key 'token'
          localStorage.setItem('token', token);
          // Also save with authService
          this.authService.saveToken(token, 3600);

          if (this.remember) {
            localStorage.setItem('vistara_email', payload.email);
          } else {
            localStorage.removeItem('vistara_email');
          }

          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = 'Login successful but token missing in response.';
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('❌ LOGIN ERROR:', err);
        
        if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please check your internet connection.';
        } else if (err.status === 401) {
          this.errorMessage = 'Invalid email or password. Please try again.';
        } else if (err.status === 403) {
          this.errorMessage = 'Access forbidden. Please check your credentials.';
        } else if (err.status === 404) {
          this.errorMessage = 'Login endpoint not found. Please check the URL.';
        } else {
          this.errorMessage = err?.error?.message || 'Login failed. Please try again.';
        }
      }
    });
  }
}