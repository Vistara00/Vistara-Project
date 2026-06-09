import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HttpClientModule]
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';
  showPassword = false;
  remember = false;

  private readonly API_URL = 'http://localhost:8087/api/v1/auth/login';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
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

    this.http.post(this.API_URL, payload)
      .subscribe({
        next: (res: any) => {
          console.log('LOGIN RESPONSE:', res);

          this.loading = false;

          const token = res?.data?.token;

          if (token) {
            localStorage.setItem('token', token);

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

          console.error('LOGIN ERROR:', err);

          this.errorMessage =
            err?.error?.message ||
            'Login failed. Please check your credentials or server connection.';
        }
      });
  }
}