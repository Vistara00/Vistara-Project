import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../core/environments/environment';

@Component({
  selector: 'app-broadcast',
  templateUrl: './broadcast.html',
  styleUrls: ['./broadcast.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class BroadcastComponent implements OnInit {
  broadcastTitle = '';
  broadcastMessage = '';
  broadcasts: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchBroadcastHistory();
  }

  sendBroadcast(): void {
    if (!this.broadcastTitle || !this.broadcastMessage) return;

    const payload = {
      title: this.broadcastTitle,
      message: this.broadcastMessage,
      timestamp: new Date().toISOString()
    };

    const url = `${environment.apiUrl}/v1/broadcast`;
    this.http.post<any>(url, payload).subscribe({
      next: (res) => {
        console.log('Broadcast sent:', res);
        this.broadcastTitle = '';
        this.broadcastMessage = '';
        this.fetchBroadcastHistory();
      },
      error: (err) => console.error('Broadcast error:', err)
    });
  }

 fetchBroadcastHistory(): void {
  const url = `${environment.apiUrl}/v1/admin/broadcasts`;
  this.http.get<any>(url).subscribe({
    next: (res) => {
      if (res?.success && res?.data) {
        // Map API response into broadcasts array
        this.broadcasts = res.data.map((item: any) => ({
          id: item.id,
          title: item.title,
          message: item.message,
          type: item.type,
          status: item.read ? 'Delivered' : 'Pending',
          timestamp: item.createdAt
        }));
      }
    },
    error: (err) => console.error('Broadcast history error:', err)
  });
}

}
