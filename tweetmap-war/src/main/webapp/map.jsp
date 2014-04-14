<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <script type="text/javascript" src="/js/markerclusterer.js"></script>
    <style type="text/css">
      html, body, #map-canvas {
        height: 100%;
        margin: 0px;
        padding: 0px
      }
    </style>
    <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization"></script>
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
		var markers = [];
		var heatmapData = [];
		var heatmap;
		var markerCluster;
		var picArray = [];
		var iconImage = {
		          path: google.maps.SymbolPath.CIRCLE,
		          scale: 2,
		          strokeColor: '#00F',
		          strokeWeight: 0.5,
		          fillColor: '#00F',
		          fillOpacity: 0.5
		        };
		
		google.maps.event.addDomListener(window, 'load', function() {
		  map = new google.maps.Map(document.getElementById('map-canvas'), {
			zoom: 2,
		    minZoom: 2, 
		    maxZoom: 18,
		    center: new google.maps.LatLng(0, 0),
		  	disableDefaultUI: true,
		  	styles: mapStyle
		  });
		
		  map.data.setStyle(styleFeature);
		  var script = document.createElement('script');
		  script.setAttribute('src', '/query?callback=tweet_callback');
		  document.getElementsByTagName('head')[0].appendChild(script);  
		});
		
		// Defines the callback function referenced in the jsonp file.
		function tweet_callback(data) {
		  //map.data.addGeoJson(data);
		  for (var i = 0; i < data.features.length; i++) {
			  var dataFeatures = data.features[i];
			  var contentString = '<p><b>' + dataFeatures.properties.user + '</b> : ' + dataFeatures.properties.text + '</p>';
			  picArray.push(dataFeatures.properties.userImage);
			  var infowindow = new google.maps.InfoWindow({
			      content: contentString
			  });
	          var latLng = new google.maps.LatLng(dataFeatures.geometry.coordinates[1],
	        		  dataFeatures.geometry.coordinates[0]);
	          heatmapData.push(latLng);
	          var marker = new google.maps.Marker({
	            position: latLng,
	            html: contentString
	          });
	          google.maps.event.addListener(marker, 'click', function() {
	        	  infowindow.setContent(this.html);
	        	  infowindow.open(map, this);
	        	  });
	          markers.push(marker);
		  }
		  markerCluster = new MarkerClusterer(map, markers);
		  markerCluster.setMaxZoom(4);
		  heatmap = new google.maps.visualization.HeatmapLayer({
			    data: heatmapData,
			    dissipating: true,
			    maxIntensity: 8,
			    map: null
			  });
		  photoOff();
		}
		
		function heatmapOn() {
			for(var i=0;i<markers.length;i++) {
				  markers[i].setVisible(false);
			  }
			  markerCluster.clearMarkers(); 
			  markerCluster.redraw();
			  heatmap.setMap(map);
		}
		
		function heatmapOff() {
			heatmap.setMap(null);
			for(var i=0;i<markers.length;i++) {
				  markers[i].setVisible(true);
			  }
			  markerCluster.addMarkers(markers); 
			  markerCluster.redraw();
		}
		
		function photoOn() {
			for(var i=0;i<markers.length;i++) {
				  markers[i].setIcon(picArray[i]);
			  }
		}
		
		function photoOff() {
			for(var i=0;i<markers.length;i++) {
				  markers[i].setIcon(null);
			  }
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