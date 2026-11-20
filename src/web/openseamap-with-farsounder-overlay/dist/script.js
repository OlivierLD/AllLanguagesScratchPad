// Initialize the map
var map = L.map('map').setView([41.5, -71.4], 11); // Coordinates are set to a sample location

if (true) { // Open Street Map
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);
}

if (true) {  // Depth
    // Marine Profile Layer
    var marineProfileLayer = L.tileLayer.wms('https://geoserver.openseamap.org/geoserver/gwc/service/wms', {
            layers: 'gebco2021:gebco_2021',
            format: 'image/png',
            transparent: true,
            version: '1.1.1',
            attribution: 'Marine Profile Data &copy; OpenSeaMap'
        }).addTo(map);
}

if (false) { // Boat tracks (around Newport RI)
    // Add hillshade PNG as an image overlay
    var imageUrlHillshade = 'https://farsounder.github.io/webmap/all_the_grids_rasterized_3000x5000hillshade_2.png';
    var imageBoundsHillshade = [[41.671457779, -71.457633569], [41.383995775, -71.288330215]];
    L.imageOverlay(imageUrlHillshade, imageBoundsHillshade).addTo(map);
}

if (false) {
    var imageUrlColor = 'https://farsounder.github.io/webmap/all_the_grids_rasterized_3000x5000color_3.png';
    var imageBoundsColor = [[41.671457779, -71.457633569], [41.383995775, -71.288330215]];
    L.imageOverlay(imageUrlColor, imageBoundsColor).addTo(map);
}

if (true) {  // Seamarks
    L.tileLayer('https://tiles.openseamap.org/seamark/{z}/{x}/{y}.png', {
            attribution: 'Map data: &copy; <a href="https://www.openseamap.org">OpenSeaMap</a> contributors'
        }).addTo(map);
}

if (false) { // Marine Traffic ???
    var marineTrafficLayer = L.tileLayer('https://tiles.marinetraffic.com/ais_helpers/shiptilesingle.aspx?output=png&sat=1&grouping=shiptype&tile_size=256&legends=1&zoom={z}&X={x}&Y={y}', {
        attribution: 'Marine Traffic Data &copy; MarineTraffic'
    }).addTo(map);
}