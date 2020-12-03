from pymongo import MongoClient
from random import choices
from flask import Flask, render_template, send_from_directory
from flask_restful import Api, Resource, reqparse
from json import dumps
import os
import uuid
from b64uuid import B64UUID
from base64 import b85encode, b85decode

key = os.environ.get("MONGO_URI")
key2 = os.environ.get("MONGO_URI2")
token = os.environ.get("TOKEN")
app = Flask(__name__, template_folder="templates")
app.env = "development"
api = Api(app)
request = reqparse.RequestParser()
request.add_argument("Info", required=False)
request.add_argument("Token", required=False)
request.add_argument("Req", required=False)
request.add_argument("Query", required=False)
request.add_argument("SignIn", required=False)
request.add_argument("SignUp", required=False)
request.add_argument("Google", required=False)
request.add_argument("Nick", required=False)
request.add_argument("Ids", required=False)
cluster = MongoClient(key)
cluster2 = MongoClient(key2)
db = cluster["AkilOyunlariDB"]
userdb = cluster2["AkilOyunlariUsers"]


def findUser(user_id):
    return userdb["Users"].find_one(filter={"_id": user_id})


def isIdTaken(user_id):
    col = userdb["User_ids"]
    ids = [i["_id"] for i in col.find()]
    for idx in ids:
        if user_id == idx:
            return False
    return True


def isNameTaken(username):
    col = userdb["User_ids"]
    names = [i["username"] for i in col.find()]
    for name in names:
        if username == name:
            return False
    return True


def isNickTaken(nick):
    col = userdb["Users"]
    nicks = [i["nick"] for i in col.find()]
    for nickx in nicks:
        if nick == nickx:
            return False
    return True

def signIn(user_id, password):
    col = userdb["User_ids"]
    ids = [(i["_id"], i["password"], i["_id"]) for i in col.find()]
    for idx in ids:
        if user_id == idx[0]:
            if password == idx[1]:
                return {"Message": idx[2]}, 200
    return False


def signUp(user_id, password):
    flag = True
    idx = None
    while flag:
        idx = B64UUID(uuid.uuid4()).string
        flag = not isIdTaken(idx)                   
    userdb["Users"].insert_one({"_id": idx, "solved": {
        "Sudoku": {"6": {"Easy": [], "Medium": [], "Hard": []}, "9": {"Easy": [],
                                                                        "Medium": [], "Hard": []}},
        "SozcukTuru": {"Easy": [], "Medium": [], "Hard": [], "Hardest": []},
        "SayiBulmaca": {"3": [], "4": [], "5": []}, "Patika": {"5": [], "7": [], "9": []},
        "HazineAvi": {"5": [],
                        "8": [], "10": []}, "Piramit": {"3": [], "4": [], "5": [], "6": []},
        "Pentomino": {"Easy": [],
                        "Medium": [], "Hard": []}, "Anagram": {"Easy": [], "Medium": [], "Hard": []}},
                                "puan": 0})
    userdb["User_ids"].insert_one({"_id": idx, "password": password, "username": user_id})
    userdb["User_ids"].find_one_and_update({"_id": "userid"}, update={"$inc": {"seq": 1}}, new=True)
    return idx


def getSolved(user_id, query):
    query = query.split(".")
    query.insert(0, "solved.")
    ids = userdb["Users"].find_one({"_id": user_id})
    for key in query:
        ids = ids[key]
    return ids


def getNextSeqValue(name, collection):
    chose = collection.find_one_and_update(filter={"_id": name}, update={"$inc": {"seq": 1}}, new=True)
    return chose["seq"]


def Insert(col, value, cevap=None):
    collection = db[col]
    if cevap:
        num = getNextSeqValue("userid", collection)
        collection.insert_one({"_id": num, "soru": value[0], "cevap": value[1]})
    else:
        num = getNextSeqValue("userid", collection)
        collection.insert_one({"_id": num, "soru": value})


def Find(col, userid, amount=1, cevap=None, query=None):
    collection = db[col]
    rangelist = [i for i in range(2, collection.find_one(filter={"_id": "userid"}["seq"] + 1))]
    if cevap:
        solved = getSolved(userid, query)
        for n in solved:
            rangelist.remove(int(n))
        randchoice = choices(rangelist, k=amount)
        docs = collection.find(filter={"_id": {"$in": randchoice}})
        response = [[i["soru"], i["cevap"]] for i in docs]
        return response, randchoice
    else:
        solved = getSolved(userid, query)
        for n in solved:
            rangelist.remove(int(n))
        randchoice = choices(rangelist, k=amount)
        docs = collection.find(filter={"_id": {"$in": randchoice}})
        response = [[i["soru"]] for i in docs]
        return response, randchoice


@app.route("/")
def get1():
    collist = db.list_collection_names()
    collist.sort()
    json = []
    json.append(("Kullanıcı Sayısı", userdb["User_ids"].find_one(filter={"_id": "userid"})["seq"]))
    for i in collist:
        json.append((str(i), str(db[i].find_one(filter={"_id": "userid"})["seq"])))
    return render_template("index.html", members=json)

@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'), 'favicon.ico', mimetype='image/vnd.microsoft.icon')


class Games(Resource):
    def get(self, game, user):
        args = request.parse_args()
        if args["Token"] == token:
            if not isIdTaken(user):
                if args["Info"]:
                    try:
                        num = int(args["Info"])
                    except ValueError:
                        return {"Message": "Unprocessable Entity"}, 422
                    if num > 20:
                        return {"Message": "Method not allowed"}, 405
                    response = Find(col=game, userid=user, cevap=args["Req"], amount=num, query=game)
                    return dumps({"Info": response[0], "Ids": response[1]}), 200
                return {"Message": "Method not allowed"}, 405
            else:
                return {"Message": "Not Found"}, 404
        else:
            return {"Message": "Method not allowed"}, 405


class User(Resource):
    def get(self, username):
        args = request.parse_args()
        if args["Token"] == token:
            user = userdb["Users"].find_one(filter={"_id": username})
            return dumps(user), 200
        else:
            return {"Message": "Method not allowed"}, 405

    def post(self, username):
        args = request.parse_args()
        if args["Token"] == token:
            if args["Nick"]:
                nickname = b85encode(args["Nick"].encode()).decode()
                if isNickTaken(nickname):
                    userdb["Users"].find_one_and_update(filter={"_id": username}, update={"$set": {"nick": nickname}})
                    return {"Message": "Ok"}, 200
                return {"Message": "This Nick Already Exists"}, 422
            if args["Google"]:
                password = b85encode(args["Google"].encode()).decode()
                google = b85encode(username.encode()).decode()
                if not isNameTaken(google):
                    return {"Message": userdb["User_ids"].find_one({"username": google})["_id"]}, 200
                idx = signUp(google, password)
                return {"Message": idx}, 201
            if args["SignIn"]:
                usernamez = b85encode(username.encode()).decode()
                password = b85encode(args["SignIn"].encode()).decode()
                return signIn(usernamez, password)
            if args["SignUp"]:
                usernamez = b85encode(username.encode()).decode()
                password = b85encode(args["SignUp"].encode()).decode()      
                if isNameTaken(usernamez):
                    idx = signUp(usernamez, password)
                    return {"Message": idx}, 201
                else:
                    return {"Message": "Username Already Exists!"}, 422
            return {"Message": "Method not allowed"}, 405
        else:
            return {"Message": "Method not allowed"}, 405

    def put(self, username):
        args = request.parse_args()
        if args["Token"] == token:
            ids = args["Ids"]
            if not isIdTaken(username):
                userdb["Users"].find_one_and_update({"_id": username},
                                                    update={"$addToSet": {"solved." + args["Query"]: {"$each": ids}}})
                return {"Message": "Ok"}, 200
            else:
                return {"Message": "Not Found"}, 404
        else:
            return {"Message": "Method not allowed"}, 405

    def delete(self, username):
        args = request.parse_args()
        if args["Token"] == token:
            if not isIdTaken(username):
                ids = userdb["User_ids"]
                users = userdb["Users"]
                ids.find_one_and_delete({"_id": username})
                users.find_one_and_delete({"_id": username})
                ids.find_one_and_update({"_id": "userid"}, update={"$inc": {"seq": -1}})
                return {"message": "deleted"}, 200
            else:
                return {"Message": "Not Found"}, 404
        else:
            return {"Message": "Method not allowed"}, 405


class LeaderBoard(Resource):
    def __init__(self):
        self.leader_list = []

    def get(self):
        args = request.parse_args()
        if args["Token"] == token:
            self.makeList()
            return dumps(self.leader_list), 200
        else:
            return {"Message": "Method not allowed"}, 405

    def makeList(self):
        def sort(orn):
            return orn[1]
        users = userdb["Users"]
        first = users.find()
        for i in first:
            self.leader_list.append([b85decode(i["nickame"].encode()).decode(), i["puan"]])
        self.leader_list.sort(key=sort, reverse=True)


api.add_resource(Games, "/<string:game>/<string:user>")
api.add_resource(User, "/<string:username>")
api.add_resource(LeaderBoard, "/leaderboard/board")
