
const fs = require('fs');
const path = require('path');

const files = [
    path.join(__dirname, 'node_modules', 'leaflet-routing-machine', 'dist', 'leaflet-routing-machine.js'),
    path.join(__dirname, 'node_modules', 'leaflet-routing-machine', 'dist', 'leaflet-routing-machine.min.js'),
    path.join(__dirname, 'patches', 'leaflet-routing-machine+3.2.12.patch')
];

files.forEach(file => {
    if (!fs.existsSync(file)) {
        console.log(`[NOT FOUND] ${file}`);
        return;
    }
    console.log(`\n--- Analyzing: ${path.basename(file)} ---`);
    const content = fs.readFileSync(file, 'utf8');

    // Count "Formatter:"
    const formatterCount = (content.match(/Formatter:/g) || []).length;
    console.log(`Total "Formatter:" occurrences: ${formatterCount}`);

    // Find clusters of "Formatter:"
    // Search for "Formatter: Formatter, Formatter: Formatter" or variations
    const regex = /Formatter:[^,}]*,[^,}]*Formatter:/g;
    const clusters = content.match(regex);
    if (clusters) {
        console.log(`Found ${clusters.length} clusters:`);
        clusters.forEach(c => console.log(`  - "${c.replace(/\s+/g, ' ')}"`));
    } else {
        console.log("No clusters found.");
    }

    if (file.endsWith('.min.js')) {
        // Specifically for minified, look for direct duplication
        if (content.indexOf('Formatter:Formatter,Formatter:Formatter') !== -1) {
            console.log("!!! FOUND EXACT DUPLICATE IN MINIFIED !!!");
        }
    }
});
