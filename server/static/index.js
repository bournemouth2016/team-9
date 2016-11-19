(function(){
  var location = [];

  var ws = new WebSocket("ws://188.166.104.20:7654/map");
  ws.onopen = function(){
    ws.send("Sent message ok");
  };
//  socket.onmessage = function(event) {
//    if(typeOf event.data === String ){
//       //create a JSON object
//       var jsonObject = JSON.parse(event.data);
//       var username = jsonObject.name;
//       var message = jsonObject.message;
    
//       console.log(“Received data string”);
//    }
// }

  var mymap = L.map('mapid').setView([51.505, -0.09], 13);
  L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
      maxZoom: 18,
      id: 'your.mapbox.project.id',
      accessToken: 'your.mapbox.public.access.token'
  }).addTo(mymap);

  var marker = L.marker([51.5, -0.09]).addTo(mymap);
  marker.bindPopup("<strong>Name:</strong><br><strong>Telephone:</strong>").openPopup();
  // marker.on("click",function(event){
     
  // });
})();