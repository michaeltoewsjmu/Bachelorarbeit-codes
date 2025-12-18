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
var months = ee.List.sequence(1, 12);

// Lade die gesamte Gap-Filled Kollektion
var annualCollection = ee.ImageCollection(MODIS_COLLECTION)
                  .filterDate(yearStart, yearEnd); 
                  
print('Anzahl der Bilder in der MOD16A2GF Kollektion für 2003:', annualCollection.size()); 

// 3. Funktion zur Berechnung der monatlichen Summe
var calculateMonthlySum = function(month) {
  var monthStart = ee.Date.fromYMD(year, month, 1);
  var monthEnd = monthStart.advance(1, 'month');
  
  var monthlyCollection = annualCollection
                            .filterDate(monthStart, monthEnd)
                            .select('ET');

  // Summe der 8-tägigen Werte
  var monthlySumET = monthlyCollection.sum().multiply(SCALE_FACTOR);
  
  // Metadaten hinzufügen
  return monthlySumET
           .clip(rhonGeometry)
           .set('year', year)
           .set('month', month)
           .set('system:time_start', monthStart.millis());
};

var monthlyETCollection = ee.ImageCollection(months.map(calculateMonthlySum));


// 5. Visualisierung (Zur Kontrolle nur den JULI)
var monthlyVis = {
  min: 0,
  max: 150,
  palette: [
    'fee0d2', 'fc9272', 'de2d26', 'a50f15', '67000d'
  ],
};
Map.centerObject(rhonGeometry, 9); 
Map.addLayer(rhonGeometry, {color: '0000FF'}, 'Untersuchungsgebiet Rhön');

// Zeige den JULI (Monat 7) an.
var julyET = monthlyETCollection.filter(ee.Filter.eq('month', 7)).first();
Map.addLayer(
    julyET, 
    monthlyVis, 
    'ET Summe Rhön 2003 (JULI) (mm/Monat)'
);


// 6. Export Block
var monthlyImagesList = monthlyETCollection.toList(monthlyETCollection.size());
var listSize = monthlyImagesList.size().getInfo();

for (var i = 0; i < listSize; i++) {
  var image = ee.Image(monthlyImagesList.get(i));
  
  var month = ee.Number(image.get('month')).format('%02d').getInfo();
  var yearString = ee.Number(image.get('year')).format('%d').getInfo();
  
  Export.image.toDrive({
      image: image, 
      description: 'MOD16GF_Monthly_ET_Rhoen_' + yearString + '_' + month,
      folder: 'GEE_Bachelorarbeit_Monatssummen_2003', 
      fileNamePrefix: 'ET_Rhoen_' + yearString + '_' + month,
      scale: RESOLUTION, 
      region: studyRegion,
      fileFormat: 'GeoTIFF'
  });
}
