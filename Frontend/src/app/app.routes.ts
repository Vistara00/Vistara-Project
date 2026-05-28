import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';

export const routes: Routes = [
  // Login route
  { path: 'login', component: LoginComponent },

  // Default redirect
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // Dashboard layout with nested child pages
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard').then(m => m.DashboardComponent),
    children: [
      {
         path: '',
  loadComponent: () =>
    import('./pages/dashboard/dashboard-home/dashboard-home').then(m => m.DashboardHomeComponent)
      },
      {
        path: 'tracking',
        loadComponent: () =>
          import('./pages/tracking/tracking').then(m => m.TrackingComponent)
      },
      {
        path: 'checkins',
        loadComponent: () =>
          import('./pages/checkins/checkins').then(m => m.CheckinsComponent)
      },
      {
        path: 'alerts',
        loadComponent: () =>
          import('./pages/alerts/alerts').then(m => m.AlertsComponent)
      },
      {
        path: 'records',
        loadComponent: () =>
          import('./pages/records/records').then(m => m.RecordsComponent)
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./pages/reports/reports').then(m => m.ReportsComponent)
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./pages/settings/settings').then(m => m.SettingsComponent)
      }
    ]
  }
];
