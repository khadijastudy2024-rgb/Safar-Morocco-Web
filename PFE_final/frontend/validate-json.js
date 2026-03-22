const fs = require('fs');
const path = require('path');

const files = [
    'src/assets/i18n/en.json',
    'src/assets/i18n/fr.json',
    'src/assets/i18n/ar.json'
];

files.forEach(file => {
    try {
        const content = fs.readFileSync(file, 'utf8');
        JSON.parse(content);
        console.log(`✅ ${file} is valid JSON`);
    } catch (e) {
        console.error(`❌ ${file} is INVALID JSON:`, e.message);
    }
});
