import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
    name: 'categoryName',
    standalone: false,
    pure: false
})
export class CategoryNamePipe implements PipeTransform {
    private categoryKeyMap: { [key: string]: string } = {
        'ALL_EVENTS': 'CATEGORIES.ALL',
        'CULTURAL': 'CATEGORIES.CULTURAL',
        'NATURE': 'CATEGORIES.NATURE',
        'HISTORICAL': 'CATEGORIES.HISTORICAL',
        'RELIGIOUS': 'CATEGORIES.RELIGIOUS',
        'URBAN': 'CATEGORIES.URBAN',
        'FOOD': 'CATEGORIES.FOOD',
        'ADVENTURE': 'CATEGORIES.ADVENTURE',
        'BEACH': 'CATEGORIES.BEACH',
        'CRAFT': 'CATEGORIES.CRAFT'
    };

    constructor(private translate: TranslateService) {}

    transform(value: string): string {
        if (!value) return this.translate.instant('CATEGORIES.ALL');

        const normalized = value.toUpperCase()
            .replace('CATEGORIES.', '')
            .replace(' ', '_');

        const key = this.categoryKeyMap[normalized];
        if (key) {
            const translated = this.translate.instant(key);
            return translated !== key ? translated : value;
        }
        return value;
    }
}
