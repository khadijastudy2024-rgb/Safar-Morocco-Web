
const fs = require('fs');
const path = require('path');

const filesToFix = [
    path.join(__dirname, 'node_modules', 'leaflet-routing-machine', 'dist', 'leaflet-routing-machine.js'),
    path.join(__dirname, 'node_modules', 'leaflet-routing-machine', 'dist', 'leaflet-routing-machine.min.js')
];

filesToFix.forEach(filePath => {
    if (fs.existsSync(filePath)) {
        let content = fs.readFileSync(filePath, 'utf8');
        let initialContent = content;

        // Fix for unminified or partially minified (with spaces)
        // Original: Formatter: Formatter, Formatter: Formatter,
        // Target: Formatter: Formatter,
        content = content.replace(/Formatter:\s*Formatter,\s*Formatter:\s*Formatter,/g, 'Formatter: Formatter,');

        // Fix for minified (no spaces)
        // Original: Formatter:Formatter,Formatter:Formatter,
        // Target: Formatter:Formatter,
        content = content.replace(/Formatter:Formatter,Formatter:Formatter,/g, 'Formatter:Formatter,');

        if (content !== initialContent) {
            fs.writeFileSync(filePath, content, 'utf8');
            console.log(`Fixed duplicate Formatter key in: ${path.basename(filePath)}`);
        } else {
            console.log(`No duplicate Formatter key found or already fixed in: ${path.basename(filePath)}`);
        }
    } else {
        console.log(`File not found: ${filePath}`);
    }
});
