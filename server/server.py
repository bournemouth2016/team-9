import tornado.ioloop
import tornado.web
import tornado.websocket
from pymongo import MongoClient
import re
from gcm import GCM
import hashlib
import os
from bson.objectid import ObjectId

client = MongoClient('localhost',27017)
db = client['codeforgood']
gsm_api_key = "AIzaSyCNubwDzGzQ762X22dnh-rvn7btvjrrYBk"
gcm = GCM(gsm_api_key, debug=False)

websocket_clients = set()

def push_data(data,ids):
    response = gcm.plaintext_request(registration_id=ids, data=data)
    return response

def get_gps(gps):
    # print gps
    if len(gps) < 2:
        return []
    gps_split = gps.split(',')
    gps_split[0] = gps_split[0].replace('[','')
    gps_split[1] = gps_split[1].replace(']','')
    gps = [float(x) for x in gps_split]
    # print gps
    return gps

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        print "GET / request from", self.request.remote_ip
        # self.write("aada")
        self.render("index.html")

class CancelDangerHandler(tornado.web.RequestHandler):
    def post(self):
        print "POST /canceldanger request from", self.request.remote_ip
        incident_id = self.get_argument('incident_id','')
        db['incidents'].update({'_id':ObjectId(incident_id)},{'$set':{
            'status':'closed'
        }})
        self.write({'status':'ok'})

class DangerHandler(tornado.web.RequestHandler):
    def post(self):
        print "POST /danger request from", self.request.remote_ip
        gps = self.get_argument('gps',[])
        passengers = self.get_argument('max_passengers','')
        if passengers != '':
            passengers = int(passengers)
        else:
            passengers = None
        casualties = self.get_argument('casualties','')
        if casualties != '':
            casualties = int(casualties)
        else:
            casualties = None

        gps_json = { 'type': 'Point', 'coordinates': get_gps(gps) }

        incident = db['incidents'].insert_one({
            'phone': self.get_argument('phone',''),
            'gps' : gps_json,
            # 'picture': self.get_argument('picture',''),
            'details': self.get_argument('details',''),
            'passengers': passengers,
            'casualties': casualties,
            'status': 'danger',
            'rescuers': []
        })

        # res = db['boats'].find({'phone':self.get_argument('phone','')})
        res = db['boats'].find({'phone':{'$ne':self.get_argument('phone','')}})
        # res = db['boats'].find({
        #     'gps':
        #     {
        #         '$near': {
        #             '$geometry' :
        #             {
        #                 'type' : 'Point' ,
        #                 'coordinates' : [get_gps(gps)[0], get_gps(gps)[1]]
        #             }
        #         }
        #     }
        # }).limit(10)

        # print get_gps(gps)
        if res.count() > 0:
            people = [i['gcm_id'] for i in res]
            # print people
            push_data({
            'phone':self.get_argument('phone',''),
            'gps': str(get_gps(gps)),
            },people)

        boats = db['boats'].find({'phone':self.get_argument('phone','')})
        owner = boats[0]['owner_fname'] + ' ' + boats[0]['owner_lname']

        for ws_client in websocket_clients:
            ws_client.write_message({
                'coordinates':get_gps(gps),
                'phone':self.get_argument('phone',''),
                'owner':owner
            })
        # print (incident.inserted_id)

        self.write({
            'status':'ok',
            'incident_id':str(incident.inserted_id),
        })

class RegistrationHandler(tornado.web.RequestHandler):
    def post(self):
        print "POST /register request from", self.request.remote_ip
        passengers = self.get_argument('max_passengers','')
        if passengers != '':
            passengers = int(passengers)
        else:
            passengers = None
        response = db['boats'].insert_one({
            'location': self.get_argument('location',''),
            'vessel_type': self.get_argument('vessel_type',''),
            'max_passengers': passengers,
            'boat_name': self.get_argument('boat_name',''),
            'owner_fname': self.get_argument('owner_fname',''),
            'owner_lname': self.get_argument('owner_lname',''),
            'password' : self.get_argument('password',''),
            'phone': self.get_argument('phone',''),
            'gcm_id': self.get_argument('gcm_id',''),
        })
        print response
        self.write({'status':'ok'})

class RescueHandler(tornado.web.RequestHandler):
    def post(self):
        pass

class LoginHandler(tornado.web.RequestHandler):
    def post(self):
        print "POST /login request from", self.request.remote_ip
        phone = self.get_argument('phone','')
        password = self.get_argument('password','')
        res = db['boats'].find({
            'phone': phone,
            'password': password
        }).count()
        if res == 1:
            self.write({'status':'ok'})

class NrliReportHandler(tornado.web.RequestHandler):
    def get(self):
        print "GET /nrli-report request from", self.request.remote_ip
        total_incidents = db['incidents'].find().count()
        total_incidents_false = db['incidents'].find({'status':'false'}).count()
        total_responders_list = db['incidents'].find({'rescuers': { '$gt': 0}})
        total_responders = 0
        for res in total_responders_list:
            total_responders += len(res['rescuers'])

        self.write({
            'total_incidents': total_incidents,
            'total_incidents_false': total_incidents_false,
            'total_responders': total_responders
        })

class TripHandler(tornado.web.RequestHandler):
    def post(self):
        print "POST /trips request from", self.request.remote_ip
        db['trips'].insert_one({
            'phone': self.get_argument('phone',''),
            'status': self.get_argument('status',''),
            'time': self.get_argument('time',''),
        })
        self.write({'status':'ok'})


class GetDangerHandler(tornado.web.RequestHandler):
    def get(self):
        print "GET /indanger request from", self.request.remote_ip
        response = db['incidents'].find({'status':'danger'})
        results = []
        for item in response:
            results.append(item['gps']['coordinates'])
        self.write({'results': results})

class MapSocketHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        websocket_clients.add(self)
        response = db['incidents'].find({'status':'danger'})
        results = []
        for item in response:
            boats = db['boats'].find({'phone':item['phone']})
            owner = boats[0]['owner_fname'] + ' ' + boats[0]['owner_lname']
            results.append({
                'coordinates':item['gps']['coordinates'],
                'phone':item['phone'],
                'owner':owner
            })
        self.write_message({'results': results})
        print("WebSocket opened")

    def on_message(self, message):
        pass
        # self.write_message(u"You said: " + message)

    def on_close(self):
        websocket_clients.remove(self)
        print("WebSocket closed")

handlers = [
            (r"/", MainHandler),
            (r"/danger",DangerHandler),
            (r"/register",RegistrationHandler),
            (r"/login",LoginHandler),
            (r"/report-nrli",NrliReportHandler),
            (r"/map",MapSocketHandler),
            (r"/indanger",GetDangerHandler),
            (r"/trip",TripHandler),
            (r"/canceldanger",CancelDangerHandler),
        ]

settings = dict(
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            static_path=os.path.join(os.path.dirname(__file__), "static"),
        )

application = tornado.web.Application(handlers, **settings)

application.listen(7654, '0.0.0.0')

tornado.ioloop.IOLoop.instance().start()
