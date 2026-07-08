// src/app/pages/tracking/tracking.ts
import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { environment } from '../../core/environments/environment';

// Import Leaflet
import * as L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet icon issue
const iconRetinaUrl = 'assets/marker-icon-2x.png';
const iconUrl = 'assets/marker-icon.png';
const shadowUrl = 'assets/marker-shadow.png';
const iconDefault = L.icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

// ── Types ─────────────────────────────────────────────────────────────────────

export interface ActiveVisitorData {
  sessionId: number;
  visitorName: string;
  visitorEmail: string;
  visitorPhone: string;
  checkInTime: string;
  groupSize: number;
  vehicleRegistration: string;
  sosTriggered: boolean;
  hasEmergency: boolean;
  lastLatitude?: number;
  lastLongitude?: number;
  lastLocationTime?: string;
  lastAccuracy?: number;
  lastBatteryLevel?: number;
  lastWithinGeofence?: boolean;
  bookingReference?: string;
  bookingStatus?: string;
}

export interface VisitorTrackingDetails {
  sessionId: number;
  checkInTime: string;
  checkOutTime: string | null;
  isActive: boolean;
  groupSize: number;
  vehicleRegistration: string;
  hasEmergency: boolean;
  sosTriggered: boolean;
  notes: string;
  visitorId: number;
  visitorName: string;
  visitorEmail: string;
  visitorPhone: string;
  bookingId: number;
  bookingReference: string;
  paymentStatus: string;
  bookingStatus: string;
  lastLatitude: number;
  lastLongitude: number;
  lastLocationTime: string;
  locationHistory: LocationHistoryPoint[];
  totalLocations: number;
}

export interface LocationHistoryPoint {
  id: number;
  latitude: number;
  longitude: number;
  accuracy: number;
  batteryLevel: number;
  timestamp: string;
  withinGeofence: boolean;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  statusCode: number;
}

// ── Component ─────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-tracking',
  templateUrl: './tracking.html',
  styleUrls: ['./tracking.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, RouterModule]
})
export class TrackingComponent implements OnInit, AfterViewInit, OnDestroy {
  searchQuery = '';
  visitors: ActiveVisitorData[] = [];
  filteredVisitors: ActiveVisitorData[] = [];
  selectedVisitor: VisitorTrackingDetails | null = null;
  
  isLoading = false;
  errorMessage = '';
  isMapInitialized = false;
  showVisitorPopup = false;
  lastRefreshTime: Date | null = null;
  
  // Map
  private map: L.Map | null = null;
  private markers: L.Marker[] = [];
  private userMarkers: { [key: number]: L.Marker } = {};
  private refreshInterval: any;

  constructor(private http: HttpClient) {
    // Expose method to window for popup button clicks
    (window as any).selectVisitor = this._selectVisitor.bind(this);
  }

  ngOnInit(): void {
    // Check if token exists
    const token = localStorage.getItem('token');
    console.log('🔑 Token exists:', !!token);
    if (token) {
      console.log('🔑 Token preview:', token.substring(0, 20) + '...');
    } else {
      console.warn('⚠️ No token found. Please login first.');
      this.errorMessage = 'Please login first to access tracking data.';
    }
    
    // ✅ Load visitors only once on init (no auto-refresh)
    this.loadVisitors();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    }, 500);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
    if (this.map) {
      this.map.remove();
    }
  }

  /**
   * Get headers with authentication token
   */
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    let headers = new HttpHeaders();
    
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
      console.log('🔑 Authorization header set');
    } else {
      console.warn('⚠️ No token found when getting headers');
    }
    
    headers = headers.set('Content-Type', 'application/json');
    headers = headers.set('Accept', 'application/json');
    
    return headers;
  }

  /**
   * Load active visitors with location data
   * ✅ Only called on init and when refresh button is clicked
   */
  loadVisitors(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const url = `${environment.apiUrl}/v1/admin/active-sessions-with-location`;
    const headers = this.getHeaders();

    console.log('🔍 Loading active visitors from:', url);
    console.log('📋 Headers:', headers);

    this.http.get<ApiResponse<ActiveVisitorData[]>>(url, { headers }).subscribe({
      next: (res) => {
        this.isLoading = false;
        console.log('✅ Response received:', res);
        
        if (res.success) {
          this.visitors = res.data || [];
          this.filteredVisitors = [...this.visitors];
          this.lastRefreshTime = new Date();
          console.log(`📊 Loaded ${this.visitors.length} active visitors`);
          
          if (this.map && this.isMapInitialized) {
            this.updateMapMarkers();
          }
        } else {
          this.errorMessage = res.message || 'Failed to load visitors.';
        }
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        console.error('❌ Error loading visitors:', err);
        
        if (err.status === 0) {
          this.errorMessage = 'Cannot connect to server. Please check your connection.';
        } else if (err.status === 401) {
          this.errorMessage = 'Session expired. Please login again.';
          // Redirect to login after 2 seconds
          setTimeout(() => {
            window.location.href = '/login';
          }, 2000);
        } else if (err.status === 403) {
          this.errorMessage = 'Access forbidden. Please login again.';
          // Redirect to login after 2 seconds
          setTimeout(() => {
            window.location.href = '/login';
          }, 2000);
        } else {
          this.errorMessage = err?.error?.message || 'Failed to load visitors.';
        }
        
        console.error('❌ Error details:', {
          status: err.status,
          statusText: err.statusText,
          message: err.message
        });
      }
    });
  }

  /**
   * Load visitor tracking details
   */
  loadVisitorDetails(sessionId: number): void {
    this.isLoading = true;
    
    const url = `${environment.apiUrl}/v1/admin/visitor-tracking-details/${sessionId}`;
    const headers = this.getHeaders();
    
    this.http.get<ApiResponse<VisitorTrackingDetails>>(url, { headers }).subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success) {
          this.selectedVisitor = res.data;
          this.showVisitorPopup = true;
          
          // Focus map on selected visitor
          if (this.selectedVisitor?.lastLatitude && this.selectedVisitor?.lastLongitude) {
            this.focusMapOnLocation(
              this.selectedVisitor.lastLatitude,
              this.selectedVisitor.lastLongitude
            );
          }
        } else {
          alert(res.message || 'Failed to load visitor details.');
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Error loading visitor details:', err);
        alert('Failed to load visitor details.');
      }
    });
  }

  /**
   * Search visitors by name
   */
  searchVisitor(): void {
    const query = this.searchQuery.toLowerCase().trim();
    if (!query) {
      this.filteredVisitors = [...this.visitors];
      return;
    }
    this.filteredVisitors = this.visitors.filter(v =>
      v.visitorName.toLowerCase().includes(query)
    );
  }

  /**
   * Initialize Leaflet map
   */
  initMap(): void {
    if (this.isMapInitialized) return;
    
    try {
      // Default center (Nairobi coordinates - adjust as needed)
      const centerLat = -1.2921;
      const centerLng = 36.8219;
      const zoom = 13;

      this.map = L.map('map', {
        center: [centerLat, centerLng],
        zoom: zoom,
        zoomControl: true
      });

      // Add OpenStreetMap tile layer
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);

      // Add zoom control
      L.control.zoom({
        position: 'topright'
      }).addTo(this.map);

      this.isMapInitialized = true;
      console.log('🗺️ Map initialized successfully');
      
      if (this.visitors.length > 0) {
        this.updateMapMarkers();
      }
    } catch (error) {
      console.error('Failed to initialize map:', error);
      this.errorMessage = 'Failed to load map. Please refresh the page.';
    }
  }

  /**
   * Update map markers for all visitors
   */
  updateMapMarkers(): void {
    if (!this.map || !this.isMapInitialized) {
      console.warn('⚠️ Map not initialized, skipping marker update');
      return;
    }

    // Clear existing markers
    this.markers.forEach(marker => this.map!.removeLayer(marker));
    this.markers = [];
    this.userMarkers = {};

    let markersAdded = 0;
    this.visitors.forEach(visitor => {
      // Only show visitors with location data
      if (visitor.lastLatitude && visitor.lastLongitude) {
        const isEmergency = visitor.sosTriggered || visitor.hasEmergency;
        
        const popupContent = `
          <div class="map-popup">
            <strong>${visitor.visitorName}</strong><br/>
            ${isEmergency ? '🚨 <span style="color:red;font-weight:bold;">SOS EMERGENCY</span><br/>' : ''}
            Vehicle: ${visitor.vehicleRegistration || 'N/A'}<br/>
            Group: ${visitor.groupSize} people<br/>
            Status: ${isEmergency ? '🚨 EMERGENCY' : '✅ Active'}<br/>
            Booking: ${visitor.bookingReference || 'N/A'}<br/>
            Last update: ${visitor.lastLocationTime ? new Date(visitor.lastLocationTime).toLocaleTimeString() : 'N/A'}
            ${visitor.lastWithinGeofence ? '<br/>📍 Inside Park' : ''}
            <br/><br/>
            <button onclick="window.selectVisitor(${visitor.sessionId})" 
                    style="background:#4b81f4;color:#fff;border:none;padding:4px 12px;border-radius:4px;cursor:pointer;">
              View Details
            </button>
          </div>
        `;

        // Different marker colors for emergency vs normal
        const markerIcon = isEmergency
          ? L.icon({
              iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
              shadowUrl: 'assets/marker-shadow.png',
              iconSize: [25, 41],
              iconAnchor: [12, 41],
              popupAnchor: [1, -34],
              shadowSize: [41, 41]
            })
          : L.icon({
              iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
              shadowUrl: 'assets/marker-shadow.png',
              iconSize: [25, 41],
              iconAnchor: [12, 41],
              popupAnchor: [1, -34],
              shadowSize: [41, 41]
            });

        const marker = L.marker([visitor.lastLatitude, visitor.lastLongitude], { icon: markerIcon })
          .bindPopup(popupContent)
          .addTo(this.map!);

        // Click handler
        marker.on('click', () => {
          this.loadVisitorDetails(visitor.sessionId);
        });

        this.markers.push(marker);
        this.userMarkers[visitor.sessionId] = marker;
        markersAdded++;
      }
    });

    console.log(`📍 Added ${markersAdded} markers to map`);

    // Fit bounds to show all markers
    if (this.markers.length > 0) {
      try {
        const group = L.featureGroup(this.markers);
        this.map.fitBounds(group.getBounds().pad(0.1));
      } catch (error) {
        console.warn('Could not fit bounds:', error);
      }
    }
  }

  /**
   * Focus map on a specific location
   */
  focusMapOnLocation(lat: number, lng: number): void {
    if (!this.map) return;
    this.map.setView([lat, lng], 16);
  }

  /**
   * Refresh data - called only when refresh button is clicked
   */
  refreshData(): void {
    this.loadVisitors();
  }

  /**
   * Get status class for styling
   */
  getStatusClass(visitor: ActiveVisitorData): string {
    return (visitor.sosTriggered || visitor.hasEmergency) ? 'status-emergency' : 'status-active';
  }

  /**
   * Get status text
   */
  getStatusText(visitor: ActiveVisitorData): string {
    if (visitor.sosTriggered || visitor.hasEmergency) {
      return '🚨 EMERGENCY';
    }
    if (visitor.lastWithinGeofence) {
      return '📍 Inside Park';
    }
    return '✅ Active';
  }

  /**
   * Get initials for avatar
   */
  getInitials(name: string): string {
    return (name || '?')
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  /**
   * Format time
   */
  formatTime(isoString: string | undefined): string {
    if (!isoString) return 'N/A';
    const date = new Date(isoString);
    return date.toLocaleTimeString();
  }

  /**
   * Get visitor count
   */
  getVisitorCount(): number {
    return this.visitors.length;
  }

  /**
   * Get SOS count
   */
  getSosCount(): number {
    return this.visitors.filter(v => v.sosTriggered || v.hasEmergency).length;
  }

  /**
   * Get last refresh time as string
   */
  getLastRefreshTime(): string {
    if (!this.lastRefreshTime) return 'Never';
    return this.lastRefreshTime.toLocaleTimeString();
  }

  // ── Popup Controls ──────────────────────────────────────────────────────────

  closePopup(): void {
    this.showVisitorPopup = false;
    this.selectedVisitor = null;
  }

  // ── Expose to window for inline onclick handlers ───────────────────────────

  // Make selectVisitor available globally for popup buttons
  private _selectVisitor = (sessionId: number) => {
    this.loadVisitorDetails(sessionId);
  };
}