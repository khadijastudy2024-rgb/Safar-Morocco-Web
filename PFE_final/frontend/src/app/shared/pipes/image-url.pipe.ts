import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'imageUrl',
    standalone: false
})
export class ImageUrlPipe implements PipeTransform {
    private baseUrl = '/uploads/';

    transform(value: string | any, fallback: string = 'assets/placeholder.jpg'): string {
        if (!value) return fallback;

        // If it's a Media object from the list, extract the URL
        let path = typeof value === 'string' ? value : value.url;

        if (!path) return fallback;

        // If it's already a full URL, return as is
        if (path.startsWith('http')) return path;

        // If it's a data URI, return as is
        if (path.startsWith('data:')) return path;

        // Remove leading slash if present to avoid double slashes when joining with baseUrl
        const cleanPath = path.startsWith('/') ? path.substring(1) : path;

        // If the path already includes 'uploads/', it's already a relative path we can use directly
        if (cleanPath.startsWith('uploads/')) {
            return '/' + cleanPath;
        }

        return this.baseUrl + cleanPath;
    }
}
