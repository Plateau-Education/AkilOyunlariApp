from pymongo import MongoClient
from random import choices, randint
from flask import Flask, render_template, send_from_directory
from flask_restful import Api, Resource, reqparse
from json import dumps, loads
import os
import uuid
from b64uuid import B64UUID
from base64 import b85encode, b85decode
from flask_mail import Mail, Message

key = os.environ.get("MONGO_URI")
key2 = os.environ.get("MONGO_URI2")
token = os.environ.get("TOKEN")
app = Flask(__name__, template_folder="templates")
app.env = "development"
app.config["DEBUG"] = True
app.config['MAIL_SERVER'] = 'smtp.gmail.com'
app.config['MAIL_PORT'] = 587
app.config['MAIL_USERNAME'] = os.environ.get("email_user")
app.config['MAIL_PASSWORD'] = os.environ.get("email_password")
app.config['MAIL_USE_TLS'] = True
app.config['MAIL_USE_SSL'] = False
mail = Mail(app)
api = Api(app)
request = reqparse.RequestParser()
request.add_argument("Info", required=False)
request.add_argument("Token", required=False)
request.add_argument("Req", required=False)
request.add_argument("Query", required=False)
request.add_argument("Ids", required=False)
cluster = MongoClient(key)
cluster2 = MongoClient(key2)
db = cluster["AkilOyunlariDB"]
userdb = cluster2["AkilOyunlariUsers"]

def randomN(n):
    range_start = 10**(n-1)
    range_end = (10**n)-1
    return randint(range_start, range_end)


def send_Mail(receiver, name):
    kod = randomN(6)
    members = [[name, kod]]
    msg = Message('Mind Puzzles', sender=os.environ.get("email_user"), recipients=[receiver])
    msg.html = str(render_template("email.htm", members=members))
    mail.send(msg)
    return kod

def isTaken(info, query):
    col = userdb["User_ids"]
    qs = [i[query] for i in col.find()]
    for q in qs:
        if info == q:
            return True
    return False


def signIn(email, password):
    col = userdb["User_ids"]
    ids = [(i["email"], i["password"], i["_id"]) for i in col.find()]
    for idx in ids:
        if email == idx[0]:
            if password == idx[1]:
                return {"Message": idx[2]}, 200
    return False


def signUp(displayname, username, email, password):
    flag = True
    idx = None
    while flag:
        idx = B64UUID(uuid.uuid4()).string
        flag = isTaken(idx, "_id")                   
    userdb["Users"].insert_one({"_id": idx, "username": username, "solved": {
        "Sudoku": {"6": {"Easy": [], "Medium": [], "Hard": []}, "9": {"Easy": [],
                                                                        "Medium": [], "Hard": []}},
        "SozcukTuru": {"Easy": [], "Medium": [], "Hard": [], "Hardest": []},
        "SayiBulmaca": {"3": [], "4": [], "5": []}, "Patika": {"5": [], "7": [], "9": []},
        "HazineAvi": {"5": [],
                        "8": [], "10": []}, "Piramit": {"3": [], "4": [], "5": [], "6": []},
        "Pentomino": {"Easy": [],
                        "Medium": [], "Hard": []}, "Anagram": {"Easy": [], "Medium": [], "Hard": []}},
                                "puan": {"Sudoku": 0, "SayiBulmaca": 0, "Piramit": 0, "Patika": 0, "HazineAvi": 0, "Pentomino": 0,
                                         "SozcukTuru": 0, "Anagram":0,}})
    userdb["User_ids"].insert_one({"_id": idx, "displayname": displayname, "username": username, "email": email, "password": password})
    userdb["User_ids"].find_one_and_update({"_id": "userid"}, update={"$inc": {"seq": 1}}, new=True)
    return idx


def getSolved(user_id, query):
    query = query.split(".")
    query.insert(0, "solved")
    ids = userdb["Users"].find_one({"_id": user_id})
    for key in query:
        ids = ids[key]
    for n, i in enumerate(ids):
        ids[n] = int(i[0])
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
    rangelist = [i for i in range(2, collection.find_one(filter={"_id": "userid"})["seq"] + 1)]
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
        response = [[[i["soru"]]] for i in docs]
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
        if args["Token"] == token and args["Info"]:
            if isTaken(user, "_id"):
                try:
                    num = int(args["Info"])
                except ValueError:
                    return {"Message": "Unprocessable Entity"}, 422
                if num > 20:
                    return {"Message": "Method not allowed"}, 405
                response = Find(col=game, userid=user, cevap=args["Req"], amount=num, query=game)
                return dumps({"Info": response[0], "Ids": response[1]}), 200
            return {"Message": "Not Found"}, 404
        return {"Message": "Method not allowed"}, 405


class User(Resource):
    def get(self, userinfo):
        args = request.parse_args()
        if args["Token"] == token and args["Info"] and userinfo == "userGet":
            json = args["Info"]
            if json["Id"]:
                user = userdb["Users"].find_one(filter={"_id": json["Id"]})
                return dumps(user), 200
            return {"Message": "Method not allowed"}, 405
        return {"Message": "Method not allowed"}, 405

    def post(self, userinfo):
        args = request.parse_args()
        if args["Token"] == token and args["Info"]:
            if userinfo == "userGoogle":
                google = b85encode(args["Info"].encode()).decode()
                if isTaken(google, "email"):
                    return {"Message": userdb["User_ids"].find_one({"email": google})["_id"]}, 200
                return {"Message": "Not Found"}, 200
            elif userinfo == "userSignIn":
                json = loads(args["Info"].replace("'", '"'))
                password = b85encode(json["password"].encode()).decode()  
                email = b85encode(json["email"].encode()).decode()     
                return signIn(email, password)
            elif userinfo == "userSignUp":
                json = loads(args["Info"].replace("'", '"'))
                username = b85encode(json["username"].encode()).decode() 
                displayname = b85encode(json["displayname"].encode()).decode()  
                password = b85encode(json["password"].encode()).decode()  
                email = b85encode(json["email"].encode()).decode()
                if not isTaken(email, "email"):
                    idx = signUp(displayname=displayname, username=username, email=email, password=password)
                    return {"Message": idx}, 200
                return {"Message": "User Already Exists!"}, 200
            elif userinfo == "userSend":
                json = loads(args["Info"].replace("'", '"'))                
                email = b85encode(json["email"].encode()).decode()
                if not isTaken(email, "email"):
                    kod = send_Mail(json["email"], json["displayname"])
                    return {"Message": kod}, 200
                return {"Message": "User Already Exists!"}, 200
            return {"Message": "Method not allowed"}, 405
        return {"Message": "Method not allowed"}, 405

    def put(self, userinfo):
        puan_dict =  {"Sudoku.6.Easy": 0.1, "Sudoku.6.Medium": 0.4, "Sudoku.6.Hard": 0.6, "Sudoku.9.Easy": 0.3, "Sudoku.9.Medium": 0.8,
                    "Sudoku.9.Hard": 1.3, "SayiBulmaca.3": 0.1, "SayiBulmaca.4": 0.3, "SayiBulmaca.5": 0.7, "Piramit.3": 0.1,
                    "Piramit.4": 0.25, "Piramit.5": 0.6, "Piramit.6": 1.2, "Patika.5": 0.1, "Patika.7": 0.3, "Patika.9": 0.8,
                    "HazineAvi.5": 0.1, "HazineAvi.8": 0.2, "HazineAvi.10": 0.7, "Pentomino.Easy": 0.1, "Pentomino.Medium": 0.3,
                    "Pentomino.Hard": 1, "SozcukTuru.Easy": 0.1, "SozcukTuru.Medium": 0.25, "SozcukTuru.Hard": 0.5,
                    "SozcukTuru.Hardest": 1, "Anagram.Easy": 0.1, "Anagram.Medium": 0.3, "Anagram.Hard": 0.8}
        args = request.parse_args()
        if args["Token"] == token and args["Info"] and userinfo == "userUpdate":
            info = loads(args["Info"].replace("'", '"'))
            idx = info["Id"]
            game_ideas = info["Ids"]
            total = 0
            if isTaken(idx, "_id"):
                for n, i in enumerate(game_ideas):
                    game_ideas[n] = i.split("-")
                    if game_ideas[n][1]:
                        total += puan_dict[info["Query"]] * 1000 / game_ideas[n][1]
                game = info["Query"].split("-")[0]
                userdb["Users"].find_one_and_update({"_id": idx},
                                                    update={"$addToSet": {"solved." + info["Query"]: {"$each": info["Ids"]}}, "$inc": {f"puan.{game}": total}})
                return {"Message": "Ok"}, 200
            return {"Message": "Not Found"}, 200
        return {"Message": "Method not allowed"}, 405

    def delete(self, userinfo):
        args = request.parse_args()
        if args["Token"] == token and args["Info"] and userinfo == "userDelete":
            idx = loads(args["Info"].replace("'", '"'))["Id"]
            if isTaken(idx, "_id"):
                ids = userdb["User_ids"]
                users = userdb["Users"]
                ids.find_one_and_delete({"_id": idx})
                users.find_one_and_delete({"_id": idx})
                ids.find_one_and_update({"_id": "userid"}, update={"$inc": {"seq": -1}})
                return {"message": "User Deleted"}, 200
            return {"Message": "Not Found"}, 200
        return {"Message": "Method not allowed"}, 405


class LeaderBoard(Resource):
    def __init__(self):
        self.leader_list_dict = {}

    def get(self):
        args = request.parse_args()
        if args["Token"] == token:
            self.makeList()
            return dumps(self.leader_list_dict), 200
        else:
            return {"Message": "Method not allowed"}, 405

    def makeList(self):
        def sort(orn):
            return orn[1]
        users = userdb["Users"]
        first = users.find()
        games = ["Sudoku", "SayiBulmaca", "Piramit", "Patika", "HazineAvi", "Pentomino", "SozcukTuru", "Anagram"]
        for game in games:
            self.leader_list_dict[game] = [] 
        for i in first:
            for game in games:
                self.leader_list_dict[game].append([b85decode(i["username"].encode()).decode(), i[f"puan.{game}"]])
        for game in self.leader_list_dict.values():
            game.sort(key=sort, reverse=True)


api.add_resource(Games, "/<string:game>/<string:user>")
api.add_resource(User, "/<string:userinfo>")
api.add_resource(LeaderBoard, "/leaderboard/board")
