(function(){
  var location = [];

var ws = new WebSocket("ws://188.166.104.20:7654/map");
ws.onopen = function(){
    // ws.send("Sent message ok");
};



  var mymap = L.map('mapid').setView([51.505, -0.09], 13);
  L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
      maxZoom: 18,
      id: 'your.mapbox.project.id',
      accessToken: 'your.mapbox.public.access.token'
  }).addTo(mymap);

  ws.onmessage = function(event) {
    data_json = JSON.parse(event.data)
    if(data_json.hasOwnProperty('results')){
        for (i in data_json['results']){

              // console.log(data_json['results'][i]);
              var marker = L.marker(data_json['results'][i]).addTo(mymap);
              marker.bindPopup("<strong>Name: </strong>"  + " <br><strong>Telephone:</strong>").openPopup();
        }
    }
    else{
        var new_marker = L.marker(data_json['coordinates']).addTo(mymap);
    }
  // var value = JSON.parse(event.data);
  // console.log(value)
  
};
  // marker.on("click",function(event){

  // });
})();
