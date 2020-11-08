from pymongo import MongoClient
from random import shuffle, choice
from Sudoku import Sudoku_6x6, Sudoku_9x9
from SayiBulmaca import SayiBulmaca_3, SayiBulmaca_4, SayiBulmaca_5
from Piramit import piramit
from HazineAvi import HazineAvi
from Patika import Patika
from SozcukTuru import sozcukturu


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


def post(game, liste, cevap):
    if CountDocs(game) > 1:
        data_set = Find(col=game)
        for n, s in enumerate(data_set):
            data_set[n] = str(s[0])
        for i in liste:
            if type(i) == list:
                if str(i) not in data_set:
                    Insert(col=game, cevap=cevap, value=i)
    else:
        for i in liste:
            if type(i) == list:
                Insert(col=game, cevap=cevap, value=i)


def main(game, size, count="1", level=None):
    try:
        count = int(count)
    except ValueError:
        return False
    if game == "Sudoku":
        if size == "6":
            games = Sudoku_6x6.main(level, count)
        elif size == "9":
            games = Sudoku_9x9.main(level, count)
        else:
            return False
        post(game + size + level, games, True)
        return True
    elif game == "SayiBulmaca":
        if size == "3":
            games = SayiBulmaca_3.main(count)
        elif size == "4":
            games = SayiBulmaca_4.main(count)
        elif size == "5":
            games = SayiBulmaca_5.main(count)
        else:
            return False
        post(game + size, games, False)
        return True
    elif game == "Piramit":
        if 3 <= size <= 6:
            games = piramit.main(size, count)
            post(game + size, games, False)
            return True
    elif game == "Patika":
        if size == 5 or 7 or 9:
            games = Patika.main(size, count)
            post(game + size, games, False)
        else:
            return False
    elif game == "HazineAvi":
        if size == 5 or 8 or 10:
            games = HazineAvi.main(size, count)
            post(game + size, games, False)
        else:
            return False
    elif game == "SozcukTuru":
        if level == "Easy" or "Medium" or "Hard" or "VeryHard":
            games = sozcukturu.main(level, count)
            post(game + level, games, False)
        else:
            return False
    else:
        return False
    return True


data = input().split(" ")
reqs = [i.split("-") for i in data]
for i in reqs:
    if len(i) == 3:
        main(i[0], i[1], i[2])
    elif len(i) == 4:
        main(i[0], i[1], i[2], i[3])
