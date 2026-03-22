import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { LanguageService, Language } from '../../core/services/language.service';

@Component({
    standalone: false,
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent {
    currentUser: any = null;
    isUserLoggedIn = false;
    isUserAdmin = false;
    isScrolled = false;
    menuOpen = false;

    languages: Array<Language> = [];
    currentLanguage?: Language;

    constructor(
        public authService: AuthService,
        public languageService: LanguageService
    ) {
        this.languages = this.languageService.languages;
        this.languageService.currentLang$.subscribe(() => {
            this.currentLanguage = this.languageService.getCurrentLanguage();
        });
        this.authService.user$.subscribe(user => {
            this.currentUser = user;
            this.isUserLoggedIn = !!user;
            this.isUserAdmin = user?.role === 'ADMIN';
        });

        window.addEventListener('scroll', () => {
            this.isScrolled = window.scrollY > 20;
        });
    }

    logout() {
        this.authService.logout();
    }

    changeLanguage(langCode: string) {
        this.languageService.setLanguage(langCode);
    }

    openAssistant() {
        const chatTrigger = document.querySelector('.chatbot-trigger') as HTMLElement;
        if (chatTrigger) {
            chatTrigger.click();
        }
    }
    // Mobile dropdown toggle
    toggleDropdown(event: Event, dropdownClass: string) {
        if (window.innerWidth <= 991) {
            event.stopPropagation();
            const dropdown = (event.currentTarget as HTMLElement).closest(`.${dropdownClass}`);
            if (dropdown) {
                dropdown.classList.toggle('show');
            }
        }
    }

    toggleMenu() {
        this.menuOpen = !this.menuOpen;
    }
}
