import { Component, OnInit, OnDestroy } from '@angular/core';
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
export class BroadcastComponent implements OnInit, OnDestroy {
  broadcastTitle = '';
  broadcastMessage = '';
  selectedTarget: 'active' | 'all' | 'specific' = 'active';
  specificVisitorId: number | null = null;

  // UI states
  loading = false;
  success = false;
  failed = false;
  animKey = 0;

  get successSrc(): string {
    return `assets/images/checkmark.svg?v=${this.animKey}`;
  }

  get failSrc(): string {
    return `assets/images/failed.svg?v=${this.animKey}`;
  }

  broadcasts: any[] = [];
  totalBroadcasts = 0;
  currentPage = 1;
  pageSize = 4;

  get totalPages(): number {
    return Math.ceil(this.totalBroadcasts / this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  private timer: any;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchBroadcastHistory();
  }

  sendBroadcast(): void {
    if (!this.broadcastTitle || !this.broadcastMessage) return;

    const url = `${environment.apiUrl}/v1/admin/broadcast`;

    let payload: any = {
      title: this.broadcastTitle,
      message: this.broadcastMessage
    };

    if (this.selectedTarget === 'active') {
      payload.broadcastType = 'ACTIVE_VISITORS';
    } else if (this.selectedTarget === 'all') {
      payload.broadcastType = 'ALL_USERS';
    } else if (this.selectedTarget === 'specific') {
      if (!this.specificVisitorId) {
        alert('Please enter a Visitor ID for specific user broadcast.');
        return;
      }
      payload.visitorId = this.specificVisitorId;
    }

    this.loading = true;
    this.success = false;
    this.failed = false;

    this.http.post<any>(url, payload).subscribe({
      next: () => this.showSuccess(),
      error: () => this.showError()
    });
  }

  private showSuccess(): void {
    this.loading = false;
    this.animKey++;
    this.success = true;
    // Refresh history immediately
    this.fetchBroadcastHistory();
    // Reset form
    this.broadcastTitle = '';
    this.broadcastMessage = '';
    this.specificVisitorId = null;
    this.selectedTarget = 'active';
    // Auto-dismiss animation after 5s
    this.timer = setTimeout(() => {
      this.success = false;
    }, 5000);
  }

  private showError(): void {
    this.loading = false;
    this.animKey++;
    this.failed = true;
    this.timer = setTimeout(() => {
      this.failed = false;
    }, 5000);
  }

  fetchBroadcastHistory(): void {
    const url = `${environment.apiUrl}/v1/admin/broadcasts/all`;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        if (res?.success && res?.data) {
          const all = res.data.map((item: any) => ({
            id: item.id,
            title: item.title,
            message: item.message,
            type: item.type,
            status: item.read ? 'Delivered' : 'Pending',
            timestamp: item.createdAt,
            recipientCount: item.recipientCount ?? 0
          }));
          this.totalBroadcasts = all.length;
          const start = (this.currentPage - 1) * this.pageSize;
          this.broadcasts = all.slice(start, start + this.pageSize);
        }
      },
      error: (err) => console.error('Broadcast history error:', err)
    });
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.fetchBroadcastHistory();
  }

  nextPage(): void { this.goToPage(this.currentPage + 1); }
  prevPage(): void { this.goToPage(this.currentPage - 1); }

  ngOnDestroy(): void {
    clearTimeout(this.timer);
  }
}