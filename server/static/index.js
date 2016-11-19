(function(){
  var location = [];

var ws = new WebSocket("ws://188.166.104.20:7654/map");
ws.onopen = function(){
    // ws.send("Sent message ok");
};



  var mymap = L.map('mapid').setView([50.74299711383331, -1.830132394788736], 13);
  L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
      maxZoom: 18,
      id: 'your.mapbox.project.id',
      accessToken: 'your.mapbox.public.access.token'
  }).addTo(mymap);

  ws.onmessage = function(event) {
    data_json = JSON.parse(event.data)
    //console.log(data_json)
    if(data_json.hasOwnProperty('results')){
        //console.log(data_json['results'].length);
        for (i=0; i< data_json['results'].length; i++){
              // console.log(data_json['results'][i]);
              var marker = L.marker(data_json['results'][i]['coordinates']).addTo(mymap);
              marker.bindPopup("<strong>Name: </strong>"  + data_json['results'][i]['owner'] + " <br><strong>Telephone:</strong> "+data_json['results'][i]['phone']).openPopup();
        }
    }
    else{
        var new_marker = L.marker(data_json['coordinates']).addTo(mymap);
        console.log(data_json);     
        new_marker.bindPopup("<strong>Name: </strong>"  + data_json['owner'] + " <br><strong>Telephone:</strong> "+data_json['phone']).openPopup();
    }
  // var value = JSON.parse(event.data);
  //console.log(value)
  
};
  // marker.on("click",function(event){

  // });
})();
