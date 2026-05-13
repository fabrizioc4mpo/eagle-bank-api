import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Attaches Authorization: Bearer <token> to outgoing HTTP requests
 * when a token is present in localStorage under key 'auth_token'.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  try {
    const token = localStorage.getItem('auth_token');
    if (token) {
      // Do not overwrite an existing Authorization header
      const hasAuth = req.headers.has('Authorization');
      const authReq = hasAuth
        ? req
        : req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
      return next(authReq);
    }
  } catch {
    // Access to localStorage might fail in some environments; ignore and proceed
  }
  return next(req);
};
