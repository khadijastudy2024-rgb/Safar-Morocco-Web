import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';

export interface Language {
    code: string;
    name: string;
    flag: string;
    rtl?: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class LanguageService {
    private readonly LANG_KEY = 'selected_language';
    private currentLangSubject = new BehaviorSubject<string>('en');
    public currentLang$ = this.currentLangSubject.asObservable();

    public languages: Language[] = [
        { code: 'en', name: 'English', flag: '🇬🇧' },
        { code: 'fr', name: 'French', flag: '🇫🇷' },
        { code: 'ar', name: 'Arabic', flag: '🇲🇦', rtl: true },
        { code: 'es', name: 'Spanish', flag: '🇪🇸' }
    ];

    constructor(
        private translate: TranslateService,
        private apiService: ApiService,
        private authService: AuthService
    ) {
        this.initLanguage();
    }

    private initLanguage() {
        this.translate.setDefaultLang('en');
        const savedLang = localStorage.getItem(this.LANG_KEY) || 'en';
        this.setLanguage(savedLang, false);

        // If logged in, fetch from backend and override
        this.authService.user$.subscribe(user => {
            if (user) {
                this.apiService.getPreferences().subscribe({
                    next: (prefs) => {
                        if (prefs && prefs.languagePreference) {
                            this.setLanguage(prefs.languagePreference.toLowerCase(), false);
                        }
                    },
                    error: (err: any) => console.error('Failed to fetch preferences', err)
                });
            }
        });
    }

    public setLanguage(langCode: string, saveToBackend: boolean = true) {
        this.translate.use(langCode);
        this.currentLangSubject.next(langCode);
        localStorage.setItem(this.LANG_KEY, langCode);
        this.applyRtl(langCode);

        if (saveToBackend && this.authService.isLoggedIn) {
            this.apiService.updatePreferences({ languagePreference: langCode.toUpperCase() }).subscribe({
                error: (err: any) => console.error('Failed to save language preference', err)
            });
        }
    }

    private applyRtl(langCode: string) {
        const isRtl = this.languages.find(l => l.code === langCode)?.rtl || false;
        const html = document.getElementsByTagName('html')[0];
        const body = document.getElementsByTagName('body')[0];

        if (isRtl) {
            html.setAttribute('dir', 'rtl');
            html.setAttribute('lang', langCode);
            body.classList.add('rtl-layout');
        } else {
            html.setAttribute('dir', 'ltr');
            html.setAttribute('lang', langCode);
            body.classList.remove('rtl-layout');
        }
    }

    public getCurrentLanguage(): Language | undefined {
        return this.languages.find(l => l.code === this.currentLangSubject.value);
    }
}
