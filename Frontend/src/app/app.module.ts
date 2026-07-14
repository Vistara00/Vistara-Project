// src/app/app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

// Import LayoutModule for responsive design
import { LayoutModule } from '@angular/cdk/layout';

// ✅ Import your main App component (from app.ts or app.component.ts)
import { AppComponent } from './app.component'; // ✅ Now it exists

// Import your standalone components
import { LoginComponent } from './pages/login/login';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { DashboardHomeComponent } from './pages/dashboard/dashboard-home/dashboard-home';
import { TrackingComponent } from './pages/tracking/tracking';
import { CheckinComponent } from './pages/checkins/checkin'; // ✅ Fixed path
import { CheckoutComponent } from './pages/checkout/checkout';
import { BookingsComponent } from './pages/bookings/bookings';
import { AlertsComponent } from './pages/alerts/alerts';
import { RangersComponent } from './pages/rangers/rangers';
import { BroadcastComponent } from './pages/broadcast/broadcast';
import { SettingsComponent } from './pages/settings/settings';
import { ProfileComponent } from './pages/profile/profile';

// Import services
import { AuthService } from './core/auth.service';
import { BookingService } from './core/services/booking.service';
import { ProfileService } from './core/services/profile.service';
import { QRService } from './core/services/qr.service';
import { AlertService } from './pages/alerts/alerts';
import { RangerService } from './pages/rangers/rangers';

// Import guards
import { AuthGuard } from './core/auth-guard';

@NgModule({
  declarations: [
    AppComponent, // ✅ Use your main App component here
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    LayoutModule,
    
    // ✅ Import your standalone components
    LoginComponent,
    DashboardComponent,
    DashboardHomeComponent,
    TrackingComponent,
    CheckinComponent,
    CheckoutComponent,
    BookingsComponent,
    AlertsComponent,
    RangersComponent,
    BroadcastComponent,
    SettingsComponent,
    ProfileComponent,
  ],
  providers: [
    AuthService,
    BookingService,
    ProfileService,
    QRService,
    AlertService,
    RangerService,
    AuthGuard,
  ],
  bootstrap: [AppComponent] // ✅ Use AppComponent as bootstrap
})
export class AppModule { }