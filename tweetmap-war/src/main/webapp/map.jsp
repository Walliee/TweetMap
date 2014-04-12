<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <style type="text/css">
      html, body, #map-canvas {
        height: 100%;
        margin: 0px;
        padding: 0px
      }
    </style>
    <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
    <script>
var mapStyle = [{
  'featureType': 'all',
  'elementType': 'all',
  'stylers': [{'visibility': 'off'}]
}, {
  'featureType': 'landscape',
  'elementType': 'geometry',
  'stylers': [{'visibility': 'on'}, {'color': '#fcfcfc'}]
}, {
  'featureType': 'water',
  'elementType': 'labels',
  'stylers': [{'visibility': 'off'}]
}, {
  'featureType': 'water',
  'elementType': 'geometry',
  'stylers': [{'visibility': 'on'}, {'hue': '#5f94ff'}, {'lightness': 60}]
}];

var map;

google.maps.event.addDomListener(window, 'load', function() {
  map = new google.maps.Map(document.getElementById('map-canvas'), {
    center: { lat: 20, lng: -160 },
    zoom: 3,
    styles: mapStyle
  });

  map.data.setStyle(styleFeature);

  // Get the earthquake data (JSONP format)
  // This feed is a copy from the USGS feed, you can find the originals here:
  //   http://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
  var script = document.createElement('script');
  script.setAttribute('src',
    'https://storage.googleapis.com/maps-devrel/quakes.geo.json');
  document.getElementsByTagName('head')[0].appendChild(script);
});

// Defines the callback function referenced in the jsonp file.
function eqfeed_callback(data) {
  map.data.addGeoJson(data);
}

function styleFeature(feature) {
//   var low = [151, 83, 34];   // color of mag 1.0
//   var high = [5, 69, 54];  // color of mag 6.0 and above
//   var minMag = 1.0;
//   var maxMag = 6.0;

//   // fraction represents where the value sits between the min and max
//   var fraction = (Math.min(feature.getProperty('mag'), maxMag) - minMag) /
//       (maxMag - minMag);

//   var color = interpolateHsl(low, high, fraction);

  return {
    icon: {
      path: google.maps.SymbolPath.CIRCLE,
      strokeWeight: 0.5,
      strokeColor: '#00f',
      fillColor: '#00f',
      fillOpacity: 0.5,
      // while an exponent would technically be correct, quadratic looks nicer
      scale: 1 //Math.pow(feature.getProperty('mag'), 2)
    },
    //zIndex: Math.floor(feature.getProperty('mag'))
  };
}

// function interpolateHsl(lowHsl, highHsl, fraction) {
//   var color = [];
//   for (var i = 0; i < 3; i++) {
//     // Calculate color based on the fraction.
//     color[i] = (highHsl[i] - lowHsl[i]) * fraction + lowHsl[i];
//   }

//   return 'hsl(' + color[0] + ',' + color[1] + '%,' + color[2] + '%)';
// }

    </script>
  </head>
  <body id="map-container">
    <div id="map-canvas"></div>
  </body>
</html>