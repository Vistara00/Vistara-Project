import { HttpInterceptorFn } from '@angular/common/http';


export const TokenInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('authToken');


  if (token && token.trim().length > 0) {
    // Clone the request and attach the Authorization header
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next(cloned);
  }

  // If no token, just forward the original request
  return next(req);
};
