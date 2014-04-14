<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreService" %>
<%@ page import="com.google.appengine.api.datastore.DatastoreServiceFactory" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.FetchOptions" %>
<%@ page import="com.google.appengine.api.datastore.Key" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.google.appengine.api.datastore.Query" %>
<%@ page import="java.util.List" %>
<html>
  <head>
    <title>TweetMap</title>
    <link rel="stylesheet" type="text/css" href="/bootstrap/css/bootstrap.css" />
    <link href="/bootstrap/css/bootstrap-switch.css" rel="stylesheet">
		
	<!-- These need to go together to prevent FOUC. In a production app, less would be compiled statically. -->
	<link rel="stylesheet/less" type="text/css" href="/css/motomapia.less" />
	<script type="text/javascript" charset="utf8" src="/js/less-1.3.0.min.js"></script>
	<script type="text/javascript" src="/js/markerclusterer.js"></script>
	<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?libraries=visualization&sensor=false"></script>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no">
    <meta charset="utf-8">
     <style> 
       #map-canvas { 
       	width: 100%;
        height: 100%;  
		top: 40px;	
		bottom: 0;
       } 
     </style>
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
    	  'stylers': [{'visibility': 'on'}, {'hue': '#5f94ff'}, {'lightness': 30}]
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
	
	function initialize() {
	  var mapOptions = {
	    zoom: 2,
	    minZoom: 2, 
	    maxZoom: 18,
	    center: new google.maps.LatLng(0, 0),
	  	disableDefaultUI: true,
	  	styles: mapStyle
	  };
	  
	  map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
	  
	  var script = document.createElement('script');
	  script.setAttribute('src', '/query?callback=tweet_callback');
	  document.getElementsByTagName('head')[0].appendChild(script);
	}
	
	google.maps.event.addDomListener(window, 'load', initialize);
	
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
	  //photoOff();
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

    </script>
  </head>
  <body>
  	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
			        <span class="sr-only">Toggle navigation</span>
			        <span class="icon-bar"></span>
			        <span class="icon-bar"></span>
			   		<span class="icon-bar"></span>
			    </button>
				<a class="navbar-brand" href="#"><span class="moto">Tweet</span><span class="mapia">map</span></a>
			</div>
			<div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
				<ul class="nav navbar-nav navbar-left">
					<li class="divider-vertical"></li>
					<li><a data-toggle="modal" href="#instructions">Instructions</a></li>
					<li><a href="https://github.com/Walliee/TweetMap" target="_blank">Code</a></li>
				</ul>
				<form class="navbar-form navbar-right">
					<input id="heatmap" type="checkbox" name="Heat" class="btn btn-default"/>
					<input id="pics" type="checkbox" name="Pictures" class="btn btn-default"/>
				</form>
			</div>					
		</div>
	</div>
    <div id="map-canvas"></div>
    <script type="text/javascript" charset="utf8" src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
    <script type="text/javascript" charset="utf8" src="/js/bootstrap-switch.js"></script>
    <script type="text/javascript" charset="utf8" src="/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript">
		$('#heatmap').bootstrapSwitch('onText','Heat');
		$('#heatmap').bootstrapSwitch('offText','Scatter');
		$('#heatmap').bootstrapSwitch('offColor','primary');
		$('#heatmap').bootstrapSwitch('size','small');
	</script>
	<script type="text/javascript">
		$('#pics').bootstrapSwitch('onText','Pics');
		$('#pics').bootstrapSwitch('offText','Dots');
		$('#pics').bootstrapSwitch('offColor','primary');
		$('#pics').bootstrapSwitch('size','small');
	</script>
    <script type="text/javascript">
    	$('#pics').on('switchChange.bootstrapSwitch', function (e) { 
    														if(markers[0].getIcon()==null) {
    															photoOn();
    														} else {
    															photoOff();
    														} 
    													});
    </script>
    <script type="text/javascript">
    	$('#heatmap').on('switchChange.bootstrapSwitch', function (e) { 
    															if (heatmap.getMap() == null) {
    																heatmapOn(); 
    															} else {
    																heatmapOff();
    															}
    														});
    </script>
  </body>
</html>