import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { environment } from '../../core/environments/environment';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ProfileComponent implements OnInit {
  profileData = {
    fullName: '',
    email: '',
    phoneNumber: '',
    nationalId: '',
    emergencyContactName: '',
    emergencyContactPhone: ''
  };

  initials = '';
  isModified = false; // track if form has been changed

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchProfile();
  }

  fetchProfile(): void {
    const url = `${environment.apiUrl}/v1/profile`;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        if (res?.success && res?.data) {
          const data = res.data;
          this.profileData = {
            fullName: data.fullName || '',
            email: data.email || '',
            phoneNumber: data.phoneNumber || '',
            nationalId: data.nationalId || '',
            emergencyContactName: data.emergencyContactName || '',
            emergencyContactPhone: data.emergencyContactPhone || ''
          };

          this.initials = this.getInitials(data.fullName);
          this.isModified = false; // reset after loading profile
        }
      },
      error: (err) => console.error('Profile fetch error:', err)
    });
  }

  // Helper to generate initials
  private getInitials(name: string): string {
    const parts = name.split(' ');
    return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase();
  }

  // Called whenever a field changes
  onFieldChange(): void {
    this.isModified = true;
  }

  saveProfile(): void {
    const url = `${environment.apiUrl}/v1/profile`;
    this.http.put<any>(url, this.profileData).subscribe({
      next: (res) => {
        console.log('Profile updated successfully:', res);
        this.isModified = false; // disable button again after save
      },
      error: (err) => console.error('Profile update error:', err)
    });
  }
}
