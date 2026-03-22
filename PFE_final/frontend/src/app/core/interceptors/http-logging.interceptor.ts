import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class HttpLoggingInterceptor implements HttpInterceptor {
    
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const startTime = Date.now();
        console.log('🌐 HTTP Request:', {
            method: req.method,
            url: req.url,
            headers: req.headers.keys(),
            body: req.body
        });

        return next.handle(req).pipe(
            tap(
                event => {
                    if (event instanceof HttpResponse) {
                        const endTime = Date.now();
                        const duration = endTime - startTime;
                        console.log('🌐 HTTP Response:', {
                            url: req.url,
                            status: event.status,
                            statusText: event.statusText,
                            duration: `${duration}ms`,
                            body: event.body
                        });
                    }
                },
                error => {
                    console.error('🌐 HTTP Error:', {
                        url: req.url,
                        error: error,
                        status: error.status,
                        statusText: error.statusText
                    });
                }
            )
        );
    }
}
