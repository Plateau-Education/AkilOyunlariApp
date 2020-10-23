from pymongo import MongoClient
from random import choice, shuffle
from flask import Flask
from flask_restful import Api, Resource, reqparse
from json import loads, dumps

app = Flask(__name__)
app.env = "development"
api = Api(app)
request = reqparse.RequestParser()
request.add_argument("Info", help="Enter Info", required=False)
request.add_argument("Token", required=False)
request.add_argument("Req", required=False)
cluster = MongoClient("mongodb+srv://Efdal:app1oyunakil@akiloyunlariapp.fzr3l.mongodb.net/AkilOyunlariDB?retryWrites=true&w=majority")
db = cluster["AkilOyunlariDB"]


def getNextSeqValue(name, collection):
    chose = collection.find_one_and_update(filter={"_id": name}, update={"$inc": {"seq": 1}}, new=True)
    return chose["seq"]


def CountDocs(col):
    collection = db[col]
    return collection.count_documents(filter={})


def Insert(col, value, cevap=None):
    collection = db[col]
    if cevap:
        num = getNextSeqValue("userid", collection)
        collection.insert_one({"_id": num, "soru": value[0], "cevap": value[1]})
    else:
        num = getNextSeqValue("userid", collection)
        collection.insert_one({"_id": num, "soru": value})


def Find(col, amount=None, cevap=None):
    collection = db[col]
    response = []
    if amount:
        rangelist = [i for i in range(2, collection.count_documents(filter={}))]
        if cevap:
            for i in range(amount):
                randchoice = choice(rangelist)
                rangelist.remove(randchoice)
                doc = collection.find_one(filter={"_id": randchoice})
                response.append([doc["soru"], doc["cevap"]])
            return response
        else:
            for i in range(amount):
                randchoice = choice(rangelist)
                rangelist.remove(randchoice)
                doc = collection.find_one(filter={"_id": randchoice})
                response.append([[doc["soru"]]])
            return response
    else:
        if cevap:
            doc = collection.find(filter={"_id": {"$type": 16}})
            for i in doc:
                response.append([i["soru"], i["cevap"]])
            shuffle(response)
            return response
        else:
            doc = collection.find(filter={"_id": {"$type": 16}})
            for i in doc:
                response.append([i["soru"]])
            shuffle(response)
            return response


class Sayi(Resource):
    def __init__(self):
        self.token = "fx!Ay:;<p6Q?C8N{"

    def post(self, game):
        args = request.parse_args()
        if args["Token"] == self.token:
            if args["Info"]:
                if CountDocs(game) > 1:
                    data_set = Find(col=game)
                    for s in data_set:
                        data_set[data_set.index(s)] = str(s[0])
                    data = loads(args["Info"])["Data"]
                    for i in data:
                        if type(i) == list:
                            if str(i) not in data_set:
                                Insert(col=game, cevap=args["Req"], value=i)
                else:
                    data = loads(args["Info"])["Data"]
                    for i in data:
                        if type(i) == list:
                            Insert(col=game, cevap=args["Req"], value=i)
                return {"Data": "Created"}, 201
            else:
                return {"Message": "Unprocessable Entity"}, 422
        else:
            return {"Message": "Unauthorized"}, 401

    def get(self, game):
        args = request.parse_args()
        if args["Token"] == self.token:
            if args["Info"]:
                try:
                    num = int(args["Info"])
                except ValueError:
                    return {"Message": "Unprocessable Entity"}, 422
                return dumps({"Info": Find(col=game, cevap=args["Req"], amount=num)})
            else:
                return dumps({"Info": Find(col=game, cevap=args["Req"])})
        else:
            return {"Message": "Unauthorized"}, 401


class Init(Resource):
    def get(self):
        collist = db.list_collection_names()
        collist.sort()
        json = {}
        for i in collist:
            json[i] = db[i].find_one(filter={"_id": "userid"})["seq"]
        return json


api.add_resource(Init, "/")
api.add_resource(Sayi, "/<string:game>")
if __name__ == '__main__':
    app.run(debug=True)
