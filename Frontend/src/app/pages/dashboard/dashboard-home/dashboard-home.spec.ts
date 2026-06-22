// src/app/pages/dashboard/dashboard-home.ts
import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Chart, registerables } from 'chart.js';
import { environment } from '../../../core/environments/environment';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard-home',
  templateUrl: './dashboard-home.html',
  styleUrls: ['./dashboard-home.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class DashboardHomeComponent implements OnInit, AfterViewInit {
  @ViewChild('visitorTrendChart', { static: false }) visitorTrendChart!: ElementRef<HTMLCanvasElement>;

  // Summary metrics
  totalVisitorsToday = 0;
  activeAlerts = 0;
  parkOccupancy = 0;
  dailyBookingValue = 0;
  alertPriority = '';
  alertStatus = '';

  // Weather widget
  weather = {
    temperature: 0,
    windSpeed: 0,
    precipitation: 0,
    condition: 'Loading...'
  };

  // Daily visitor trend data
  dailyVisitors: { day: number; count: number }[] = [];
  startDate: string = '';
  endDate: string = '';
  private visitorChartInstance: Chart | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchDashboardStats();
    this.fetchWeather();
  }

  ngAfterViewInit(): void {
    // Render placeholder chart immediately
    this.renderVisitorTrendChart();
  }

  fetchDashboardStats(): void {
    const url = `${environment.apiUrl}/dashboard`;

    this.http.get<any>(url).subscribe({
      next: (res) => {
        if (res?.success && res?.data) {
          const data = res.data;
          this.totalVisitorsToday = data.activeVisitors || 0;
          this.activeAlerts = data.emergencyAlertBreakdown?.[0]?.count || 0;
          this.alertPriority = data.emergencyAlertBreakdown?.[0]?.priority || '';
          this.alertStatus = data.emergencyAlertBreakdown?.[0]?.status || '';
          this.parkOccupancy = Math.round((data.activeVisitors / 5000) * 100);
          this.dailyBookingValue = data.dailyAverageRevenue || 0;

          // Default daily trend for current month
          const today = new Date();
          this.dailyVisitors = this.generateDailyTrend(today.getMonth(), today.getFullYear());
          this.renderVisitorTrendChart();
        }
      },
      error: (err) => console.error('Dashboard API error:', err)
    });
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

  private generateDailyTrend(month: number, year: number): { day: number; count: number }[] {
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    return Array.from({ length: daysInMonth }, (_, i) => ({
      day: i + 1,
      count: 0 // placeholder until backend data arrives
    }));
  }

  private renderVisitorTrendChart(): void {
    if (!this.visitorTrendChart) return;
    const ctx = this.visitorTrendChart.nativeElement.getContext('2d');
    if (!ctx) return;

    if (this.visitorChartInstance) {
      this.visitorChartInstance.destroy();
    }

    const labels = this.dailyVisitors.map((d) => d.day);
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
            pointRadius: 4,
            pointHoverRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          x: {
            title: { display: true, text: 'Day of Month' },
            ticks: { stepSize: 2 }, // interval of 2 days
            grid: { color: '#e0e0e0' }
          },
          y: {
            title: { display: true, text: 'Visitors' },
            beginAtZero: true,
            ticks: { stepSize: 100, callback: (v) => Math.floor(v) },
            grid: { color: '#e0e0e0' }
          }
        }
      }
    });
  }

  updateVisitorTrend(): void {
    if (!this.startDate || !this.endDate) return;

    const url = `${environment.apiUrl}/dashboard/visitors?start=${this.startDate}&end=${this.endDate}`;
    this.http.get<any>(url).subscribe({
      next: (res) => {
        if (res?.success && res?.data) {
          this.dailyVisitors = res.data.map((d: any) => ({
            day: new Date(d.date).getDate(),
            count: d.count
          }));
          this.renderVisitorTrendChart();
        }
      },
      error: (err) => console.error('Visitor trend API error:', err)
    });
  }

  // Existing demo data
  recentActivity = [
    { icon: 'fa-ticket-alt', text: 'Ticket #1023 Verified' },
    { icon: 'fa-satellite-dish', text: 'Ranger Uplink Active' },
    { icon: 'fa-users', text: 'New Group Booking #TB-03' },
    { icon: 'fa-broadcast-tower', text: 'SOS Broadcast Sent at 09:30 AM' },
    { icon: 'fa-ticket-alt', text: 'Ticket #1024 Verified' }
  ];

  quickActions = [
    { label: 'Broadcast SOS', icon: 'fa-bullhorn', color: 'red' },
    { label: 'New Booking', icon: 'fa-calendar-plus', color: 'green' },
    { label: 'Export Report', icon: 'fa-file-export', color: 'gray' }
  ];
}
