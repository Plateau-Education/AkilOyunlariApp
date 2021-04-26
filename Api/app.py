from pymongo import MongoClient
from random import choices,randint
from flask import Flask,render_template, send_from_directory
from flask_restful import Api,Resource,reqparse
from json import dumps,loads
import os
import uuid
from b64uuid import B64UUID
from base64 import b85encode,b85decode
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import mail_settings
import socket
import threading

key = os.environ.get("MONGO_URI")
key2 = os.environ.get("MONGO_URI2")
cluster = MongoClient(key)
cluster2 = MongoClient(key2)
token = os.environ.get("TOKEN")#API şifresi sisteme alınır.
mail_key = os.environ.get('SENDGRID_API_KEY')#Sendgrid şifresi sisteme alınır.
app = Flask(__name__, template_folder="templates")
app.env = "development"
api = Api(app)
email_user = os.environ.get("email_user")
email_password = os.environ.get("email_password")
#Sengrid için gönderici mail adresi ve şifresi sisteme alınır.
request = reqparse.RequestParser()
request.add_argument("Info", required=False)
request.add_argument("Token", required=False)
request.add_argument("Req", required=False)
request.add_argument("Query", required=False)
request.add_argument("Ids", required=False)
db = cluster["AkilOyunlariDB"]
db2 = cluster2["AkilOyunlariUsers"]
userdb = db2["Users"]
cl = db2["Class"]
sender = "akiloyunlariapp@gmail.com"
subject = "Welcome To Mind Puzzles"


def send_Mail(receiver, name):
    kod = randint(100000, 1000000)#rastgele doğrulama kodu belirlenir.
    members = [[name, kod]]#alıcı bilgileri
    message = Mail(
    from_email=f'{sender}',
    to_emails=f'{receiver}',
    subject=f'{subject}',
    plain_text_content=f"Verification Code: {kod}",
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
    ids = [(i["email"], i["password"], i["_id"], i["username"], i["displayname"], i["classid"], i["type"]) for i in col.find()]
    for idx in ids:
        if email == idx[0]:
            if password == idx[1]:
                return {"Id": idx[2], "Username": b85decode(idx[3].encode()).decode(),
                        "Displayname": b85decode(idx[4].encode()).decode(), "ClassId": idx[5], "Type": idx[6]}, 200
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
             "HazineAvi": {"5": [[], [[0, 0]], 0, 0, [], 1], "8": [[], [[0, 0]], 0, 0, [], 1], "10": [[], [[0, 0]], 0, 0, [], 2]},
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


def Find(col, userid, amount=1, cevap=None, query=None, AR=None):
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
        if AR and col.split(".")[0] == "HazineAvi":
            flag = True
            fis = -1
            k = 0
            usar = collection.find_one({"_id": "userid"})["difs"]
            ak = list(usar.keys())
            for k5, ke in enumerate(ak):
                ak[k5] = int(ke)
            solved = set(getSolved(userid, query))
            sorular = []
            while flag:
                if AR + k < min(ak):
                    fis = 1
                    k = 1
                    continue
                if AR + k > max(ak):
                    return "Message:", False
                ar1 = list(set(usar[str(AR + k)]) - solved)
                k += fis
                if len(ar1) >= amount:
                    randchoice = choices(ar1, k=amount)
                    docs = collection.find(filter={"_id": {"$in": randchoice}})
                    response = [[[i["soru"]]] for i in docs]
                    return response, randchoice
                else:
                    sorular.extend(ar1)
                    amount -= len(ar1)
            return "Message:", False
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
                if game.split(".")[0] == "HazineAvi":
                    ar = int(userdb.find_one({"_id": user})["solved"]["HazineAvi"][game.split(".")[1]][5])
                    response = Find(col=game, userid=user, cevap=args["Req"], amount=num, query=game, AR=ar)
                    return dumps({"Info": response[0], "Ids": response[1]}), 200
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
                return cl.find_one({"_id": classid}), 200
        return {"Message": "Method not allowed"}, 405

    def post(self, userinfo):
        args = request.parse_args()
        if args["Token"] == token and args["Info"]:
            if userinfo == "userGoogle":
                google = b85encode(args["Info"].encode()).decode()
                if isTaken(google, "email"):
                    idx = userdb.find_one({"email": google})
                    user = {"Id": idx["_id"], "Username": b85decode(idx["username"].encode()).decode(),
                            "Displayname": b85decode(idx["displayname"].encode()).decode(), "ClassId": idx["classid"], "Type": idx["type"]}, 200
                    return user
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
            elif userinfo == "taskSend":
                json = loads(args["Info"].replace("'", '"'))
                to = json["to"]
                game = json["game"]
                goal = int(json["goal"])
                cla = json["class"]
                std2 = cl.find_one({"_id": cla})["Students"]
                if to == "all":
                    for n in range(len(std2)):
                        cl.find_one_and_update({"_id": cla}, {"$push": {f"Students.{n}.tasks": [game, goal, 0, "active"]}})
                    return {"Message": "Done!"}, 200
                for n in range(len(std2)):
                    if std2[n]["id"] == to:
                        cl.find_one_and_update({"_id": cla}, {"$push": {f"Students.{n}.tasks": [game, goal, 0, "active"]}})
                        return {"Message": "Done!"}, 200
                return {"Message": "User Not Found!"}, 200
            elif userinfo == "taskDelete":
                json = loads(args["Info"].replace("'", '"'))
                to = json["to"]
                game = json["game"]
                goal = int(json["goal"])
                clId = json["class"]
                if clId != "None":
                    sts = cl.find_one({"_id": clId})["Students"]
                    for k, s in enumerate(sts):
                        if s["id"] == to:
                            cl.find_one_and_update({"_id": clId}, update={"$pull": {f"Students.{k}.tasks": [game, goal, goal, "passive"]}})
                            return {"Message": "Done!"}, 200
                return {"Message": "Method not allowed"}, 405
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
        dif_dict = {"5": [1, 7, 30, 50], "8": [1, 13, 70, 100], "10": [2, 12, 280, 330]}
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
                    for i in unsolved:
                        game_ids.remove(i)
                    game = info['Query']
                    q = info['Query'].split(".")
                    sty = userdb.find_one({"_id": idx})
                    best_bef = sty["solved"]
                    clId = sty["classid"]
                    if clId != "None":
                        sts = cl.find_one({"_id": clId})["Students"]
                        for k, s in enumerate(sts):
                            if s["id"] == idx:
                                for l, task in enumerate(s["tasks"]):
                                    if task[0] == game:
                                        if task[3] == "active":
                                            goal = task[1]
                                            pres = task[2] + len(game_ids)
                                            if pres >= goal:
                                                cl.find_one_and_update({"_id": clId}, update={"$set" : {f"Students.{k}.tasks.{l}.2": goal, f"Students.{k}.tasks.{l}.3": "passive"}})
                                            else:
                                                cl.find_one_and_update({"_id": clId}, update={"$set" : {f"Students.{k}.tasks.{l}.2": pres}})
                                            break
                                break
                    for i in q:
                        best_bef = best_bef[i]                    
                    if q[0] == "HazineAvi":
                        dif = best_bef[5]
                        dif -= len(unsolved) * 0.1
                        d1 = dif_dict[q[1]]
                        for n in valid_ids:
                            k1 = (d1[3] - n)/d1[3]/10 if n > d1[3] else 0.1 if d1[3] > n > d1[2] else 0.1 + (d1[2] - n)/d1[2]/10
                            dif += k1
                        if dif < dif_dict[q[1]][0]:
                            dif = dif_dict[q[1]][0]
                        elif dif > dif_dict[q[1]][1]:
                            dif = dif_dict[q[1]][1]
                        userdb.find_one_and_update({"_id": idx}, update={"$set": {f"solved.{info['Query']}.5": dif}})
                    best_bef = best_bef[3]
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
                    constant = ((len(ar[0]) + len(game_ids)) // 8) + 1                
                    # cb = ar[1][0][1]                
                    new_stats = []
                    # if cb != constant:
                    ids = ar[0].copy()
                    ids.extend(game_ids)
                    avg_t = 0
                    ax = 0
                    tur = 0
                    for i in ids:
                        avg_t += int(i[1])
                        ax += 1
                        tur += 1
                        if ax % constant == 0:
                            new_stats.append([avg_t / ax, tur])
                            avg_t = 0
                            ax = 0
                    if ax != 0:
                        new_stats.append([avg_t / ax, tur])
                    userdb.find_one_and_update({"_id": idx},
                                        update={"$set": {f"solved.{info['Query']}.1": new_stats}})  
                    #else:
                     #   ar = ar[1]
                      #  count = ar[-1][1]
                      #  tur = count
                       # avg_t = ar[-1][0] * count
                       # new_stats = [ar[-1]]                
                        #flag = True
                        #if ar[-1][1] % constant == 0:
                         #   flag = False
                       # for stat in valid_ids:
                        #    count += 1
                         #   avg_t += stat
                         #   tur += 1
                          #  if count % constant == 0:
                           #     new_stats.append([avg_t / count, tur])
                            #    avg_t = 0
                             #   count = 0
                    #    if flag:
                     #       userdb.find_one_and_update({"_id": idx}, update={"$pull": {f"solved.{info['Query']}.1": ar[-1]}})
                      #  if count != 0:
                       #     new_stats.append([avg_t / count, tur])
                     #   new_stats.remove(ar[-1])
                      #  userdb.find_one_and_update({"_id": idx},
                       #                     update={"$push": {f"solved.{info['Query']}.1": {"$each": new_stats}}})                
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
                    cl.find_one_and_update({"_id": classid}, update={"$push": {"Students": {"id": userid, "displayname": displayna, "username": userna, "tasks":[]}}})
                    userdb.find_one_and_update({"_id": userid}, update={"$set": {"classid": classid}})
                    return cl.find_one({"_id": classid}), 200
                return {"Message": "Not Found!"}, 404
            elif userinfo == "leaveClass":
                info = loads(args["Info"].replace("'", '"'))
                classid = info["ClassId"]
                userid = info["Id"]
                tc = cl.find_one({"_id": classid})
                if tc:
                    cl.find_one_and_update({"_id": classid}, update={"$pull": {"Students": {"id": userid}}})
                    userdb.find_one_and_update({"_id": userid}, update={"$set": {"classid": "None"}})
                    return {"Message": "Ok!"}, 200
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
            
class Room:
    def __init__(self, room_id):
        self.id = room_id
        self.clients = []

class Server:
    def __init__(self):
        self.ip = "0.0.0.0"
        print("denemeeeee", end="  ------------   ")
        print(os.environ.get('PORT'))
        self.port = int(os.environ.get('PORT', 17995))

        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s.bind((self.ip, self.port))

        self.rooms = dict()
        for i in cl.find():
            self.rooms[i["_id"]] = Room(i["_id"])
        self.accept_connections()

    def accept_connections(self):
        self.s.listen(5)

        print('Running on IP: ' + self.ip)
        print('Running on port: ' + str(self.port))

        while True:
            c, addr = self.s.accept()
            self.addToRoom(c)

    def broadcast(self, sock, data, room_id):
        for client in self.rooms[room_id].clients:
            if client != sock:
                try:
                    client.send(data)
                except:
                    pass

    def handle_client(self, c, room_id):
        while 1:
            try:
                data = c.recv(1024)
                self.broadcast(c, data, room_id)

            except socket.error:
                c.close()

    def addToRoom(self, c):
        room_id = c.recv(16).decode()
        self.rooms[room_id].clients.append(c)
        threading.Thread(target=self.handle_client, args=(c, room_id,)).start()


threading.Thread(target=Server).start()
api.add_resource(Games, "/<string:game>/<string:user>")
api.add_resource(User, "/<string:userinfo>")
api.add_resource(LeaderBoard, "/leaderboard/board")
