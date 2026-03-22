import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
    name: 'translateValue',
    standalone: false,
    pure: false
})
export class TranslateValuePipe implements PipeTransform {
    private translations: { [lang: string]: { [key: string]: string } } = {
        'ar': {
            // Months
            'janvier': 'يناير', 'février': 'فبراير', 'mars': 'مارس',
            'avril': 'أبريل', 'mai': 'مايو', 'juin': 'يونيو',
            'juillet': 'يوليوز', 'août': 'غشت', 'septembre': 'شتنبر',
            'octobre': 'أكتوبر', 'novembre': 'نونبر', 'décembre': 'دجنبر',
            'oct': 'أكتوبر', 'nov': 'نونبر', 'dec': 'دجنبر',
            'jan': 'يناير', 'feb': 'فبراير', 'mar': 'مارس',
            'apr': 'أبريل', 'may': 'مايو', 'jun': 'يونيو',
            'jul': 'يوليوز', 'aug': 'غشت', 'sep': 'شتنبر',
            'january': 'يناير', 'february': 'فبراير', 'march': 'مارس',
            'april': 'أبريل', 'june': 'يونيو',
            'july': 'يوليوز', 'august': 'غشت', 'september': 'شتنبر',
            'october': 'أكتوبر', 'november': 'نونبر', 'december': 'دجنبر',
            // Languages
            'arabe': 'العربية', 'français': 'الفرنسية', 'francais': 'الفرنسية',
            'tashelhit': 'تشلحيت', 'amazigh': 'الأمازيغية',
            'anglais': 'الإنجليزية', 'espagnol': 'الإسبانية',
            'arabic': 'العربية', 'french': 'الفرنسية',
            'english': 'الإنجليزية', 'spanish': 'الإسبانية',
            'tamazight': 'الأمازيغية', 'darija': 'الدارجة',
            'berbère': 'الأمازيغية', 'berbere': 'الأمازيغية',
            
            // Destinations & Cities (Morocco)
            'jemaa el-fnaa': 'ساحة جامع الفناء',
            'hassan ii mosque': 'مسجد الحسن الثاني',
            'ait ben haddou': 'آيت بن حدو',
            'merzouga desert': 'صحراء مرزوكة',
            'ouzoud waterfalls': 'شلالات أوزود',
            'fes el bali': 'فاس البالي',
            'todgha gorges': 'مضايق تودغى',
            'dades valley': 'وادي دادس',
            'essaouira medina': 'مدينة الصويرة القديمة',
            'rabat city center': 'مركز مدينة الرباط',
            'agadir beach': 'شاطئ أكادير',
            'sefrou city': 'مدينة صفرو',
            'guelmim gate': 'باب كلميم',
            'laayoune city': 'مدينة العيون',
            'imilchil village': 'قرية إملشيل',
            'dakhla lagoon': 'بحيرة الداخلة',
            'al hoceima bay': 'خليج الحسيمة',
            'bin el ouidane lake': 'بحيرة بين الويدان',
            'oualidia lagoon': 'بحيرة الوليدية',
            'azrou cedar forest': 'غابة أرز أزرو',
            'hercules caves': 'مغارة هرقل',
            'oukaimeden resort': 'منتجع أوكايمدن',
            'saidia beach': 'شاطئ السعيدية',
            'asilah old town': 'مدينة أصيلة القديمة',
            
            'marrakech': 'مراكش',
            'casablanca': 'الدار البيضاء',
            'ouarzazate': 'ورزازات',
            'errachidia': 'الرشيدية',
            'azilal': 'أزيلال',
            'fes': 'فاس',
            'tinghir': 'تنغير',
            'boumalne dades': 'بومالن دادس',
            'essaouira': 'الصويرة',
            'rabat': 'الرباط',
            'agadir': 'أكادير',
            'kelaat mgouna': 'قلعة مكونة',
            'sefrou': 'صفرو',
            'guelmim': 'كلميم',
            'laayoune': 'العيون',
            'imilchil': 'إملشيل',
            'dakhla': 'الداخلة',
            'al hoceima': 'الحسيمة',
            'oualidia': 'الوليدية',
            'azrou': 'أزرو',
            'tangier': 'طنجة',
            'berkane': 'بركان',
            'asilah': 'أصيلة',
            'tan-tan': 'طانطان',
            'ifrane': 'إفران',
            'meknes': 'مكناس',
            'cultural': 'ثقافي',
            'nature': 'طبيعة',
            'historical': 'تاريخي',
            'religious': 'ديني'
        }
    };

    constructor(private translate: TranslateService) {}

    transform(value: string | undefined | null, fallbackKey?: string): string {
        if (!value && fallbackKey) {
            return this.translate.instant(fallbackKey);
        }
        if (!value) return '';

        const lang = this.translate.currentLang || this.translate.defaultLang || 'en';
        const map = this.translations[lang];
        if (!map) return value;

        // Check if the entire string (lowercased) exists in the map
        const fullMatch = map[value.toLowerCase()];
        if (fullMatch) {
            return fullMatch;
        }

        // Split by common separators, translate each part, rejoin
        return value.replace(/[\wÀ-ÿ-]+/gi, (word) => {
            const lower = word.toLowerCase();
            return map[lower] || word;
        });
    }
}
