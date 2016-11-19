import tornado.ioloop
import tornado.web
import tornado.websocket
from pymongo import MongoClient
import re
from gcm import GCM
import hashlib
import os

client = MongoClient('localhost',27017)
db = client['codeforgood']
gsm_api_key = "AIzaSyCNubwDzGzQ762X22dnh-rvn7btvjrrYBk"
# gsm_reg_ids = []
gcm = GCM(gsm_api_key, debug=False)

def push_data(data,ids):
    response = gcm.plaintext_request(registration_id=ids, data=data)
    return response

def get_gps(gps):
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

class DangerHandler(tornado.web.RequestHandler):
    def post(self):
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

        db['incidents'].insert_one({
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

        # print [get_gps(gps)[0], get_gps(gps)[1]]
        # print res
        # print res.next()
        # print res.count()

        print get_gps(gps)
        if res.count() > 0:
            people = [i['gcm_id'] for i in res]
            print people
            push_data({
            'phone':self.get_argument('phone',''),
            'gps': str(get_gps(gps)),
            },people)

        self.write({
            'status':'ok'
        })


class RegistrationHandler(tornado.web.RequestHandler):
    def post(self):

        passengers = self.get_argument('max_passengers','')
        if passengers != '':
            passengers = int(passengers)
        else:
            passengers = None

        db['boats'].insert_one({
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
        self.write({'status':'ok'})

class RescueHandler(tornado.web.RequestHandler):
    def post(self):
        pass
        # gps = self.get_argument('gps',[])
        # db['boats'].insert_one({
        #     'gps' : get_gps(gps),
        #     'rescuer_id' : self.get_argument('rescuer_id',''),
        # })

class LoginHandler(tornado.web.RequestHandler):
    def post(self):
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

class GetDangerHandler(tornado.web.RequestHandler):
    def get(self):
        pass

class MapSocketHandler(tornado.websocket.WebSocketHandler):
    def open(self):
        self.write_message({"key":'value'})
        print("WebSocket opened")

    def on_message(self, message):
        pass
        # self.write_message(u"You said: " + message)

    def on_close(self):
        print("WebSocket closed")

handlers = [
            (r"/", MainHandler),
            (r"/danger",DangerHandler),
            (r"/register",RegistrationHandler),
            (r"/login",LoginHandler),
            (r"/report-nrli",NrliReportHandler),
            (r"/map",MapSocketHandler),
            (r"/get-danger",GetDangerHandler),
        ]

settings = dict(
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            static_path=os.path.join(os.path.dirname(__file__), "static"),
        )

application = tornado.web.Application(handlers, **settings)

application.listen(7654, '0.0.0.0')

tornado.ioloop.IOLoop.instance().start()
