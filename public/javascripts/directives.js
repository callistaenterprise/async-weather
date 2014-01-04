app.directive('map', function () {
  return {
    link: function (scope, elem, attrs) {

      function initialize() {
        var mapOptions = {
          center: new google.maps.LatLng(57.71,11.94),
          zoom: 2,
          mapTypeId: google.maps.MapTypeId.ROADMAP,
          streetViewControl: false,
          zoomControl: true,
          zoomControlOptions: {
            style: google.maps.ZoomControlStyle.LARGE,
            position: google.maps.ControlPosition.LEFT_CENTER
          },
          panControl: true,
          panControlOptions: {
            position: google.maps.ControlPosition.TOP_RIGHT
          }
        };
        var bounds = new google.maps.LatLngBounds();
        var map = new google.maps.Map(elem[0], mapOptions);
        console.log(map);

        angular.forEach(scope.positions, function(p){
          var position = new google.maps.LatLng(p.location.lat, p.location.lng)
          bounds.extend(position);

          var marker = new google.maps.Marker({
              position: position,
              map: map,
              title: "\"<h3>" + p.temperatures['smhi'].temp + "</h3>\""
          });

          var content = '';

          for (var key in p.temperatures) {
            content = content + key + ': ' + p.temperatures[key].temp + ' grader<br/>';
          }

          var infoWindow = new google.maps.InfoWindow({
              content: content
          });

          // Allow each marker to have an info window
          google.maps.event.addListener(marker, 'mouseover', function() {
            infoWindow.open(map, marker);
          });

          google.maps.event.addListener(marker, 'mouseout', function() {
            infoWindow.close();
          });
          map.fitBounds(bounds);

        });

        var boundsListener = google.maps.event.addListener((map), 'bounds_changed', function(event) {

          if(this.getZoom() > 20){
            this.setZoom(this.getZoom()-5);
          }

          google.maps.event.removeListener(boundsListener);
        });

        scope.$on('update', function() {
          initialize();
        })

      }
      google.maps.event.addDomListener(window, 'load', initialize);
    }
  };
});