import { HttpInterceptorFn } from '@angular/common/http';
import { log } from 'console';

export const TokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');

  // Always log when the interceptor runs
  console.log('Interceptor fired. Current token:', token);

  if (token && token.trim().length > 0) {
    // Clone the request and attach the Authorization header
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    console.log('Authorization header attached:', cloned.headers.get('Authorization'));
    return next(cloned);
  }

  // If no token, just forward the original request
  return next(req);
};
