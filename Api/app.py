from pymongo import MongoClient
from random import choices, randint
from flask import Flask, render_template, send_from_directory
from flask_restful import Api, Resource, reqparse
from json import dumps, loads
import os
import uuid
from b64uuid import B64UUID
from base64 import b85encode, b85decode
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail

key = os.environ.get("MONGO_URI")
key2 = os.environ.get("MONGO_URI2")
token = os.environ.get("TOKEN")
mail_key = os.environ.get('SENDGRID_API_KEY')
app = Flask(__name__, template_folder="templates")
app.env = "development"
email_user = os.environ.get("email_user")
email_password = os.environ.get("email_password")
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
db2 = cluster2["AkilOyunlariUsers"]
userdb = db2["Users"]
cl = db2["Class"]


def send_Mail(receiver, name):
    kod = randint(100000, 1000000)
    members = [[name, kod]]
    message = Mail(
    from_email='akiloyunlariapp@gmail.com',
    to_emails=f'{receiver}',
    subject='Welcome To Mind Puzzles',
    plain_text_content=f"Verification Code {kod}",
    html_content=f'{render_template("email.htm", members=members)}')
    try:
        sg = SendGridAPIClient(mail_key)
        response = sg.send(message)
        print(response.status_code)
        print(response.body)
        print(response.headers)
    except Exception as e:
        print(str(e))
    return kod


def isTaken(info, query):
    col = userdb
    qs = [i[query] for i in col.find()]
    for q in qs:
        if info == q:
            return True
    return False


def signIn(email, password):
    col = userdb
    ids = [(i["email"], i["password"], i["_id"], i["username"], i["displayname"], i["classid"]) for i in col.find()]
    for idx in ids:
        if email == idx[0]:
            if password == idx[1]:
                return {"Id": idx[2], "Username": b85decode(idx[3].encode()).decode(),
                        "Displayname": b85decode(idx[4].encode()).decode(), "ClassId": idx[5]}, 200
    return False


def signUp(displayname, username, email, password, identity):
    flag = True
    idx = None
    classid = "None"
    while flag:
        idx = B64UUID(uuid.uuid4()).string
        flag = isTaken(idx, "_id")
    if identity == "Instructor":
        flag = True
        while flag:
            flag = False
            classid = B64UUID(uuid.uuid4()).string[:6]
            for i in cl.find():
                if i["_id"] == classid:
                    flag = True
                    break
        cl.insert_one({"_id": classid, "Instructor": {"id": idx, "displayname": b85decode(displayname.encode()).decode(), 
                       "username": b85decode(username.encode()).decode()}, "Students": []})            
    userdb.insert_one(
        {"_id": idx, "type": identity,"displayname": displayname, "username": username, 
        "email": email, "password": password, "classid": classid, "class": [],
         "solved": {
             "Sudoku": {"6": {"Easy": [[], [[0, 0]], 0, 0, []], "Medium": [[], [[0, 0]], 0, 0, []], "Hard": [[], [[0, 0]], 0, 0, []]},
                        "9": {"Easy": [[], [[0, 0]], 0, 0, []], "Medium": [[], [[0, 0]], 0, 0, []], "Hard": [[], [[0, 0]], 0, 0, []]}},
             "SozcukTuru": {"Easy": [[], [[0, 0]], 0, 0, []], "Medium": [[], [[0, 0]], 0, 0, []], "Hard": [[], [[0, 0]], 0, 0, []],
                            "Hardest": [[], [[0, 0]], 0, 0, []]},
             "SayiBulmaca": {"3": [[], [[0, 0]], 0, 0, []], "4": [[], [[0, 0]], 0, 0, []], "5": [[], [[0, 0]], 0, 0, []]},
             "Patika": {"5": [[], [[0, 0]], 0, 0, []], "7": [[], [[0, 0]], 0, 0, []], "9": [[], [[0, 0]], 0, 0, []]},
             "HazineAvi": {"5": [[], [[0, 0]], 0, 0, []], "8": [[], [[0, 0]], 0, 0, []], "10": [[], [[0, 0]], 0, 0, []]},
             "Piramit": {"3": [[], [[0, 0]], 0, 0, []], "4": [[], [[0, 0]], 0, 0, []], "5": [[], [[0, 0]], 0, 0, []],
                         "6": [[], [[0, 0]], 0, 0, []]},
             "Pentomino": {"Easy": [[], [[0, 0]], 0, 0, []], "Medium": [[], [[0, 0]], 0, 0, []], "Hard": [[], [[0, 0]], 0, 0, []]},
             "Anagram": {"Easy": [[], [[0, 0]], 0, 0, []], "Medium": [[], [[0, 0]], 0, 0, []], "Hard": [[], [[0, 0]], 0, 0, []]}},
         "puan": {"Sudoku": 0, "SayiBulmaca": 0, "Piramit": 0, "Patika": 0,
                  "HazineAvi": 0, "Pentomino": 0, "SozcukTuru": 0, "Anagram": 0}})
    userdb.find_one_and_update({"_id": "userid"}, update={"$inc": {"seq": 1}}, new=True)    
    return {"Id": idx, "ClassId": classid}


def getSolved(user_id, query):
    query = query.split(".")
    query.insert(0, "solved")
    ids = userdb.find_one({"_id": user_id})
    idx = []
    for k in query:
        ids = ids[k]
    for i in ids[0]:
        idx.append(int(i[0]))
    for i in ids[4]:
        idx.append(int(i[0]))
    return idx


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
    json = [("Kullanıcı Sayısı", userdb.find_one(filter={"_id": "userid"})["seq"])]
    for i in collist:
        json.append((str(i), str(db[i].find_one(filter={"_id": "userid"})["seq"])))
    return render_template("index.html", members=json)


@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'), 'favicon.ico',
                               mimetype='image/vnd.microsoft.icon')


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
        if args["Token"] == token and args["Info"]:
            cols = db.list_collection_names()
            if userinfo == "userGet":
                stat_dict = {}
                usera = userdb.find_one({"_id": args["Info"]})["solved"]
                for col in cols:
                    userb = usera
                    col2 = col.split(".")
                    for i in col2:
                        userb = userb[i]
                    stat_dict[col] = userb[1]
                stat_dict["puan"] = userdb.find_one({"_id": args["Info"]})["puan"]
                return dumps(stat_dict), 200
            elif userinfo == "userBest":
                best_dict = {}
                usera = userdb.find_one({"_id": args["Info"]})["solved"]
                for col in cols:
                    userb = usera
                    col2 = col.split(".")
                    for i in col2:
                        userb = userb[i]
                    best_dict[col] = [userb[2], userb[3]]
                return dumps(best_dict), 200
            elif userinfo == "classGet":
                info = loads(args["Info"].replace("'", '"'))
                classid = info["ClassId"]
                if classid == "None":
                    return {"Message": "Not Found!"}, 404
                return dumps(cl.find_one({"_id": classid})), 200
        return {"Message": "Method not allowed"}, 405

    def post(self, userinfo):
        args = request.parse_args()
        if args["Token"] == token and args["Info"]:
            if userinfo == "userGoogle":
                google = b85encode(args["Info"].encode()).decode()
                if isTaken(google, "email"):
                    idx = userdb.find_one({"email": google})
                    return {"Id": idx["_id"], "Username": b85decode(idx["username"].encode()).decode(),
                            "Displayname": b85decode(idx["displayname"].encode()).decode(), "classid": idx["classid"]}, 200
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
                identity = json["type"]
                if not isTaken(email, "email"):
                    idx = signUp(displayname=displayname, username=username, email=email, password=password, identity=identity)                    
                    return idx, 200
                return {"Message": "User Already Exists!"}, 200
            elif userinfo == "userSend":
                json = loads(args["Info"].replace("'", '"'))
                email = b85encode(json["email"].encode()).decode()
                username = b85encode(json["username"].encode()).decode()
                if isTaken(username, "username"):
                    return {"Message": "Username Already Exists!"}, 422
                if not isTaken(email, "email"):
                    kod = send_Mail(json["email"], json["username"])
                    return {"Message": kod}, 200
                return {"Message": "User Already Exists!"}, 200
            return {"Message": "Method not allowed"}, 405
        return {"Message": "Method not allowed"}, 405

    def put(self, userinfo):
        puan_dict = {"Sudoku.6.Easy": 0.1, "Sudoku.6.Medium": 0.4, "Sudoku.6.Hard": 0.6, "Sudoku.9.Easy": 0.3,
                     "Sudoku.9.Medium": 0.8,
                     "Sudoku.9.Hard": 1.3, "SayiBulmaca.3": 0.1, "SayiBulmaca.4": 0.3, "SayiBulmaca.5": 0.7,
                     "Piramit.3": 0.1,
                     "Piramit.4": 0.25, "Piramit.5": 0.6, "Piramit.6": 1.2, "Patika.5": 0.1, "Patika.7": 0.3,
                     "Patika.9": 0.8,
                     "HazineAvi.5": 0.1, "HazineAvi.8": 0.2, "HazineAvi.10": 0.7, "Pentomino.Easy": 0.1,
                     "Pentomino.Medium": 0.3,
                     "Pentomino.Hard": 1, "SozcukTuru.Easy": 0.1, "SozcukTuru.Medium": 0.25, "SozcukTuru.Hard": 0.5,
                     "SozcukTuru.Hardest": 1, "Anagram.Easy": 0.1, "Anagram.Medium": 0.3, "Anagram.Hard": 0.8}
        args = request.parse_args()
        if args["Token"] == token and args["Info"]:
            if userinfo == "userUpdate":
                info = loads(args["Info"].replace("'", '"'))
                idx = info["Id"]
                game_ids = info["Ids"]
                unsolved = []
                total = 0
                if isTaken(idx, "_id"):
                    time = 0
                    new_time = 0
                    valid_ids = []
                    best = []
                    for n, i in enumerate(game_ids):
                        game_ids[n] = i.split("-")
                        game_ids[n][1] = int(game_ids[n][1])
                        if game_ids[n][1] != 0:
                            time += 1
                            new_time += game_ids[n][1]
                            best.append(game_ids[n][1])
                            total += puan_dict[info["Query"]] * 10
                            valid_ids.append(game_ids[n][1])
                        else:
                            unsolved.append(game_ids[n])
                            game_ids.remove(game_ids[n])                
                    q = info['Query'].split(".")
                    q.append(3)
                    best_bef = userdb.find_one({"_id": idx})["solved"]
                    for i in q:
                        best_bef = best_bef[i]
                    q.remove(3)
                    if best == []:
                        best = best_bef
                    else:
                        best = min(best)
                    if best_bef != 0 and best_bef < best:
                        best = best_bef
                    game = info["Query"].split(".")[0]
                    userdb.find_one_and_update({"_id": idx},
                                            update={"$inc": {f"solved.{info['Query']}.2": total, f"puan.{game}": total}, 
                                                    "$set": {f"solved.{info['Query']}.3": best}})
                    ar = userdb.find_one({"_id": idx})["solved"]
                    for i in q:
                        ar = ar[i]
                    constant = ((len(ar[0]) + len(game_ids)) // 10) + 1                
                    cb = ar[1][0][1]                
                    new_stats = []
                    if cb != constant:
                        ids = ar[0].copy()
                        ids.extend(game_ids)
                        avg_t = 0
                        for n, i in enumerate(ids, 1):
                            avg_t += int(i[1])
                            if n % constant == 0:
                                new_stats.append([avg_t / n, n])
                        if new_stats[-1][1] != len(ids):
                            new_stats.append([avg_t / len(ids), len(ids)])
                        userdb.find_one_and_update({"_id": idx},
                                            update={"$set": {f"solved.{info['Query']}.1": new_stats}})  
                    else:
                        ar = ar[1]
                        count = ar[-1][1]
                        avg_t = ar[-1][0] * count
                        new_stats = [ar[-1]]                
                        flag = True
                        if ar[-1][1] % constant == 0:
                            flag = False
                        for stat in valid_ids:
                            count += 1
                            avg_t += stat
                            if count % constant == 0:
                                new_stats.append([avg_t / count, count])
                        if flag:
                            userdb.find_one_and_update({"_id": idx}, update={"$pull": {f"solved.{info['Query']}.1": ar[-1]}})
                        if new_stats[-1][1] != count:
                            new_stats.append([avg_t / count, count])
                        new_stats.remove(ar[-1])
                        userdb.find_one_and_update({"_id": idx},
                                            update={"$push": {f"solved.{info['Query']}.1": {"$each": new_stats}}})                
                    userdb.find_one_and_update({"_id": idx},
                                            update={"$addToSet": {f"solved.{info['Query']}.0": {"$each": game_ids},
                                                                    f"solved.{info['Query']}.4": {"$each": unsolved}}})
                    return {"Message": "Ok"}, 200
                return {"Message": "Not Found"}, 200
            elif userinfo == "classUpdate":
                info = loads(args["Info"].replace("'", '"'))
                classid = info["ClassId"]
                userid = info["Id"]
                displayna = info["Displayname"]
                userna = info["Username"]
                tc = cl.find_one({"_id": classid})
                if tc:
                    cl.find_one_and_update({"_id": classid}, update={"$push": {"Students": {"id": userid, "displayname": displayna, "username": userna}}})
                    userdb.find_one_and_update({"_id": userid}, update={"$set": {"classid": classid}})
                    return dumps(cl.find_one({"_id": classid})), 200
                return {"Message": "Not Found!"}, 404
            return {"Message": "Method not allowed"}, 405

    def delete(self, userinfo):
        args = request.parse_args()
        if args["Token"] == token and args["Info"] and userinfo == "userDelete":
            idx = loads(args["Info"].replace("'", '"'))["Id"]
            if isTaken(idx, "_id"):
                users = userdb
                users.find_one_and_delete({"_id": idx})
                users.find_one_and_update({"_id": "userid"}, update={"$inc": {"seq": -1}})
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

        users = userdb
        first = users.find()
        games = ["Sudoku", "SayiBulmaca", "Piramit", "Patika", "HazineAvi", "Pentomino", "SozcukTuru", "Anagram"]
        for game in games:
            self.leader_list_dict[game] = []
        p = 0
        for i in first:
            if p == 0:
                p = 1
                continue
            for game in games:
                self.leader_list_dict[game].append([b85decode(i["username"].encode()).decode(), i["puan"][game]])
        for game in self.leader_list_dict.values():
            game.sort(key=sort, reverse=True)


api.add_resource(Games, "/<string:game>/<string:user>")
api.add_resource(User, "/<string:userinfo>")
api.add_resource(LeaderBoard, "/leaderboard/board")
