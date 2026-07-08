// src/app/core/token.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const TokenInterceptor: HttpInterceptorFn = (req, next) => {
  // ✅ Use 'token' key (matches AuthService)
  const token = localStorage.getItem('token');
  
  // Check if token exists and is not empty
  if (token && token.trim().length > 0) {
    // Clone the request and attach the Authorization header
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    
    return next(cloned);
  }

  // If no token, just forward the original request
  return next(req);
};