import tornado.ioloop
import tornado.web
from pymongo import MongoClient
import re
from gcm import GCM
import hashlib

client = MongoClient('localhost',27017)
db = client['codeforgood']
gsm_api_key = "AIzaSyCNubwDzGzQ762X22dnh-rvn7btvjrrYBk"
gsm_reg_ids = []
gcm = GCM(gsm_api_key, debug=False)

def gcm(data):
    response = gcm.plaintext_request(registration_id=gsm_reg_ids, data=data)
    return response

def get_gps(gps):
    if len(gps) < 2:
        return []
    gps = self.get_argument('gps',[])
    gps_split = gps.split(',')
    gps_split[0] = gps_split[0].replace('[','')
    gps_split[1] = gps_split[1].replace(']','')
    gps = [int(x) for x in gps_split]
    return gps

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        print "GET / request from", self.request.remote_ip
        self.write("aada")
        # self.render("home.html")

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

        db['incidents'].insert_one({
            'gps' : get_gps(gps),
            # 'picture': self.get_argument('picture',''),
            'details': self.get_argument('details',''),
            'gps' : get_gps(gps),
            'passengers': passengers,
            'casualties': casualties,
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
        gps = self.get_argument('gps',[])
        db['boats'].insert_one({
            'gps' : get_gps(gps),
            'rescuer_id' : self.get_argument('rescuer_id',''),
        })

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
        pass


handlers = [
            (r"/", MainHandler),
            (r"/danger",DangerHandler),
            (r"/register",RegistrationHandler),
            (r"/login",LoginHandler),
        ]



application = tornado.web.Application(handlers)

application.listen(7654, '0.0.0.0')

tornado.ioloop.IOLoop.instance().start()
