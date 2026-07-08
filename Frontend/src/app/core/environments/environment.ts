// src/environments/environment.ts
export const environment = {
  production: false,
  // ✅ With proxy
  apiUrl: '/api',
  // WebSocket URL
  wsUrl: '/ws',
  features: {
    enableTracking: true,
    enableSOS: true,
    enableGeofence: true,
    enableNotifications: true,
    enableMpesa: false
  },
  map: {
    defaultLatitude: -1.2921,
    defaultLongitude: 36.8219,
    defaultZoom: 13,
    maxZoom: 19
  },
  refreshIntervals: {
    visitorTracking: 10000,
    activeSessions: 15000,
    notifications: 30000
  },
  pagination: {
    pageSize: 20,
    maxSize: 10
  }
};