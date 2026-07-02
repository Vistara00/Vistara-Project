// src/app/pages/dashboard/dashboard-home.ts
import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { environment } from '../../../core/environments/environment';

Chart.register(...registerables);

// Types for dashboard data
interface EmergencyAlertBreakdown {
  status: string;
  priority: string;
  count: number;
}

interface DailyData {
  date: string;
  count: number;
}

interface WeeklyData {
  weekNumber: number;
  count: number;
}

interface DashboardStats {
  emergencyAlertBreakdown: EmergencyAlertBreakdown[];
  activeVisitors: number;
  totalUsers: number;
  todayCheckins: number;
  todayCheckouts: number;
  dailyAverageRevenue: number;
  attendanceDaily: DailyData[];
  revenueDaily: DailyData[];
  attendanceWeekly: WeeklyData[];
}

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
  styleUrls: ['./dashboard-home.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class DashboardHomeComponent implements OnInit, AfterViewInit {
  @ViewChild('visitorTrendChart', { static: false }) visitorTrendChart!: ElementRef<HTMLCanvasElement>;
  @ViewChild('revenueChart', { static: false }) revenueChart!: ElementRef<HTMLCanvasElement>;

  // Summary metrics
  totalVisitorsToday = 0;
  activeAlerts = 0;
  pendingAlerts = 0;
  resolvedAlerts = 0;
  parkOccupancy = 0;
  dailyBookingValue = 0;
  totalUsers = 0;
  todayCheckins = 0;
  todayCheckouts = 0;

  // Alert details
  alertStatuses: { status: string; count: number }[] = [];

  // Weather widget
  weather = {
    temperature: 0,
    windSpeed: 0,
    precipitation: 0,
    condition: 'Loading...'
  };

  // Daily visitor trend
  dailyVisitors: { day: number; count: number }[] = [];
  dailyRevenue: { day: number; amount: number }[] = [];
  weeklyAttendance: { week: number; count: number }[] = [];
  
  startDate: string = '';
  endDate: string = '';
  
  private visitorChartInstance: Chart | null = null;
  private revenueChartInstance: Chart | null = null;

  // Expose Math to template
  Math = Math;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.fetchDashboardStats();
    this.fetchWeather();
  }

  ngAfterViewInit(): void {
    // Charts will be rendered after data is loaded
  }

  // ===== Helper Methods for Template =====
  
  getMaxWeeklyCount(): number {
    if (this.weeklyAttendance.length === 0) return 1;
    return Math.max(...this.weeklyAttendance.map(w => w.count));
  }

  getMaxAlertCount(): number {
    if (this.alertStatuses.length === 0) return 1;
    return Math.max(...this.alertStatuses.map(a => a.count));
  }

  // ===== Data Fetching =====

  fetchDashboardStats(): void {
    const url = `${environment.apiUrl}/v1/admin/dashboard/stats`;

    this.http.get<any>(url).subscribe({
      next: (res) => {
        if (res?.success && res?.data) {
          const data: DashboardStats = res.data;
          
          // Map summary metrics
          this.totalVisitorsToday = data.activeVisitors || 0;
          this.totalUsers = data.totalUsers || 0;
          this.todayCheckins = data.todayCheckins || 0;
          this.todayCheckouts = data.todayCheckouts || 0;
          this.dailyBookingValue = data.dailyAverageRevenue || 0;
          
          // Map alert breakdown
          if (data.emergencyAlertBreakdown) {
            this.alertStatuses = data.emergencyAlertBreakdown.map(item => ({
              status: item.status,
              count: item.count
            }));
            
            // Calculate active alerts (PENDING + RESPONDING)
            this.activeAlerts = data.emergencyAlertBreakdown
              .filter(a => a.status === 'PENDING' || a.status === 'RESPONDING')
              .reduce((sum, a) => sum + a.count, 0);
            
            this.pendingAlerts = data.emergencyAlertBreakdown
              .filter(a => a.status === 'PENDING')
              .reduce((sum, a) => sum + a.count, 0);
            
            this.resolvedAlerts = data.emergencyAlertBreakdown
              .filter(a => a.status === 'RESOLVED' || a.status === 'FALSE_ALARM')
              .reduce((sum, a) => sum + a.count, 0);
          }
          
          // Calculate park occupancy (assuming capacity of 5000)
          this.parkOccupancy = data.activeVisitors ? Math.round((data.activeVisitors / 5000) * 100) : 0;

          // Map daily attendance
          if (data.attendanceDaily && data.attendanceDaily.length > 0) {
            const today = new Date();
            const currentMonth = today.getMonth();
            const currentYear = today.getFullYear();
            
            this.dailyVisitors = this.generateDailyTrendWithData(
              currentMonth, 
              currentYear, 
              data.attendanceDaily
            );
          } else {
            const today = new Date();
            this.dailyVisitors = this.generateDailyTrend(today.getMonth(), today.getFullYear());
          }

          // Map revenue data
          if (data.revenueDaily && data.revenueDaily.length > 0) {
            this.dailyRevenue = data.revenueDaily.map(item => ({
              day: new Date(item.date).getDate(),
              amount: item.count
            }));
          }

          // Map weekly attendance
          if (data.attendanceWeekly && data.attendanceWeekly.length > 0) {
            this.weeklyAttendance = data.attendanceWeekly.map(item => ({
              week: item.weekNumber,
              count: item.count
            }));
          }

          // Render charts after data is loaded
          setTimeout(() => {
            this.renderVisitorTrendChart();
            this.renderRevenueChart();
          }, 100);
        }
      },
      error: (err) => {
        console.error('Dashboard API error:', err);
        const today = new Date();
        this.dailyVisitors = this.generateDailyTrend(today.getMonth(), today.getFullYear());
        this.renderVisitorTrendChart();
      }
    });
  }

  private generateDailyTrendWithData(
    month: number, 
    year: number, 
    attendanceData: DailyData[]
  ): { day: number; count: number }[] {
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    
    const attendanceMap = new Map<string, number>();
    attendanceData.forEach(item => {
      attendanceMap.set(item.date, item.count);
    });

    return Array.from({ length: daysInMonth }, (_, i) => {
      const day = i + 1;
      const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      return {
        day: day,
        count: attendanceMap.get(dateStr) || 0
      };
    });
  }

  private generateDailyTrend(month: number, year: number): { day: number; count: number }[] {
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    return Array.from({ length: daysInMonth }, (_, i) => ({
      day: i + 1,
      count: Math.floor(Math.random() * 100) + 20
    }));
  }

  fetchWeather(): void {
    const lat = -1.3733;
    const lon = 36.8580;
    const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,precipitation,wind_speed_10m,weathercode`;

    this.http.get<any>(url).subscribe({
      next: (res) => {
        const current = res?.current;
        if (current) {
          this.weather.temperature = Math.round(current.temperature_2m);
          this.weather.windSpeed = Math.round(current.wind_speed_10m);
          this.weather.precipitation = current.precipitation ?? 0;
          this.weather.condition = this.mapWeatherCode(current.weathercode);
        }
      },
      error: (err) => console.error('Weather API error:', err)
    });
  }

  private mapWeatherCode(code: number): string {
    const map: Record<number, string> = {
      0: 'Clear sky',
      1: 'Mainly clear',
      2: 'Partly cloudy',
      3: 'Overcast',
      45: 'Fog',
      48: 'Depositing rime fog',
      51: 'Light drizzle',
      61: 'Light rain',
      63: 'Moderate rain',
      65: 'Heavy rain',
      71: 'Light snow',
      80: 'Rain showers',
      95: 'Thunderstorm'
    };
    return map[code] || 'Unknown';
  }

  private renderVisitorTrendChart(): void {
    if (!this.visitorTrendChart) return;
    const ctx = this.visitorTrendChart.nativeElement.getContext('2d');
    if (!ctx) return;

    if (this.visitorChartInstance) {
      this.visitorChartInstance.destroy();
    }

    const labels = this.dailyVisitors.map((d) => `Day ${d.day}`);
    const dataPoints = this.dailyVisitors.map((d) => d.count);

    this.visitorChartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Daily Visitors',
            data: dataPoints,
            borderColor: '#2e7d32',
            backgroundColor: 'rgba(46, 125, 50, 0.2)',
            tension: 0.3,
            fill: true,
            pointRadius: 3,
            pointHoverRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { 
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => {
                const value = context.parsed.y;
                return value !== null && value !== undefined ? `${value} visitors` : '0 visitors';
              }
            }
          }
        },
        scales: {
          x: {
            title: { display: true, text: 'Day of Month' },
            ticks: { maxTicksLimit: 15 },
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          y: {
            title: { display: true, text: 'Visitors' },
            beginAtZero: true,
            grid: { color: 'rgba(0,0,0,0.05)' }
          }
        }
      }
    });
  }

  private renderRevenueChart(): void {
    if (!this.revenueChart) return;
    const ctx = this.revenueChart.nativeElement.getContext('2d');
    if (!ctx) return;

    if (this.revenueChartInstance) {
      this.revenueChartInstance.destroy();
    }

    const labels = this.dailyRevenue.map((d) => `Day ${d.day}`);
    const dataPoints = this.dailyRevenue.map((d) => d.amount);

    this.revenueChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: 'Daily Revenue (KES)',
            data: dataPoints,
            backgroundColor: 'rgba(46, 125, 50, 0.6)',
            borderColor: '#2e7d32',
            borderWidth: 1,
            borderRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { 
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => {
                const value = context.parsed.y;
                return value !== null && value !== undefined ? `KES ${value.toFixed(2)}` : 'KES 0.00';
              }
            }
          }
        },
        scales: {
          x: {
            title: { display: true, text: 'Day of Month' },
            ticks: { maxTicksLimit: 15 },
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          y: {
            title: { display: true, text: 'Revenue (KES)' },
            beginAtZero: true,
            grid: { color: 'rgba(0,0,0,0.05)' }
          }
        }
      }
    });
  }

  updateVisitorTrend(): void {
    if (!this.startDate || !this.endDate) return;
    this.fetchDashboardStats();
  }

  // Quick actions
  quickActions = [
    { label: 'Broadcast SOS', icon: 'fa-bullhorn', color: 'red', action: 'broadcast' },
    { label: 'New Booking', icon: 'fa-calendar-plus', color: 'green', action: 'booking' },
    { label: 'Export Report', icon: 'fa-file-export', color: 'gray', action: 'export' }
  ];

  // Recent activity
  get recentActivity(): { icon: string; text: string }[] {
    return [
      { icon: 'fa-ticket-alt', text: `New check-in: ${this.todayCheckins} visitors today` },
      { icon: 'fa-sign-out-alt', text: `${this.todayCheckouts} check-out${this.todayCheckouts !== 1 ? 's' : ''} completed` },
      { icon: 'fa-exclamation-triangle', text: `${this.pendingAlerts} pending alert${this.pendingAlerts !== 1 ? 's' : ''}` },
      { icon: 'fa-dollar-sign', text: `Daily revenue: KES ${this.dailyBookingValue.toFixed(2)}` }
    ];
  }

  onQuickAction(action: string): void {
    console.log('Quick action:', action);
    // Implement navigation or actions here
    switch(action) {
      case 'broadcast':
        // Navigate to broadcast
        break;
      case 'booking':
        // Navigate to bookings
        break;
      case 'export':
        // Export logic
        break;
    }
  }
}