// src/app/pages/dashboard/dashboard.ts
import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { ProfileService } from '../../core/services/profile.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class DashboardComponent implements OnInit {
  initials = '';
  fullName = '';
  showMenu = false;

  constructor(
    private profileService: ProfileService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.fetchProfile();
  }

  // Fetch profile and compute initials
  private fetchProfile(): void {
    this.profileService.getProfile().subscribe({
      next: (res: any) => {
        const name = res?.data?.fullName || '';
        this.fullName = name;
        const parts = name.split(' ');
        this.initials =
          (parts[0]?.[0] || '') + (parts[1]?.[0] || '');
        this.initials = this.initials.toUpperCase();
      },
      error: (err) => console.error('Profile fetch error:', err)
    });
  }

  toggleMenu(): void {
    this.showMenu = !this.showMenu;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.profile-icon') && !target.closest('.dropdown')) {
      this.showMenu = false;
    }
  }

  navigateTo(route: string): void {
    this.router.navigate([`/dashboard/${route}`]);
    this.showMenu = false;
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}