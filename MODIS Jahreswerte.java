// 1. Definition des Untersuchungsgebiets (Rhön)
var rhonGeometry = ee.FeatureCollection("projects/ee-michaeltoews0/assets/biosphaerenreservat_rhoen");
var studyRegion = rhonGeometry.geometry().bounds(); 

// 2. Definition des Zieljahres und des Datensatzes
var year = 2003; // Das Zieljahr wurde für andere Jahre geändert
var MODIS_COLLECTION = 'MODIS/061/MOD16A2GF';
var SCALE_FACTOR = 0.1; // Skalierungsfaktor
var RESOLUTION = 500;

var yearStart = ee.Date.fromYMD(year, 1, 1);
var yearEnd = yearStart.advance(1, 'year');

var annualCollection = ee.ImageCollection(MODIS_COLLECTION)
                  .filterDate(yearStart, yearEnd); 
                  
print('Anzahl der 8-Tage-Bilder in der Kollektion für ' + year + ':', annualCollection.size()); 

// 3. Berechnung der JÄHRLICHEN GESAMTSUMME
var evapotranspiration = annualCollection.select('ET');

var annualSumET = evapotranspiration.sum().multiply(SCALE_FACTOR);

// 4. Visualisierungsparameter definieren
var annualSumVis = {
  min: 400,
  max: 1000, 
  palette: [
    'ffffff', 'fcd163', '99b718', '66a000', '3e8601', '207401', '056201',
    '004c00', '011301'
  ],
};

// 5. Karte zentrieren und Layer hinzufügen
Map.centerObject(rhonGeometry, 9); 
Map.addLayer(rhonGeometry, {color: '0000FF'}, 'Untersuchungsgebiet Rhön');

// Fügt den berechneten Layer hinzu und schneidet ihn präzise auf die Geometrie zu
Map.addLayer(
    annualSumET.clip(rhonGeometry), 
    annualSumVis, 
    'ET Jahressumme Rhön ' + year + ' (mm/Jahr)'
);

// 6. Export-Funktion
var yearString = ee.Number(year).format('%d').getInfo();

Export.image.toDrive({
  image: annualSumET.clip(rhonGeometry), 
  description: 'MOD16GF_Annual_SUM_ET_Rhoen_' + yearString,
  folder: 'GEE_Bachelorarbeit_Jahressummen', 
  fileNamePrefix: 'ET_Rhoen_' + yearString + '_Summe',
  scale: RESOLUTION, 
  region: studyRegion,
  fileFormat: 'GeoTIFF'
});
