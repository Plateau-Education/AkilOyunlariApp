from flask import Flask
from flask_restful import Api, Resource, reqparse
from json import loads, dumps
import sqlite3

app = Flask(__name__)
api = Api(app)
request = reqparse.RequestParser()
request.add_argument("Info", help="Enter Info", required=False)
request.add_argument("Token", required=False)
request.add_argument("Where", required=False)
request.add_argument("Req", required=False)
db = sqlite3.connect("C:/Users/Proper/Documents/GitHub/AkilOyunlariApp/RestfulAPI/sorular.db", check_same_thread=False)
cursor = db.cursor()


def CloseDB():
    db.close()


def CreateTable(table_name):
    cursor.execute(f"CREATE TABLE IF NOT EXISTS {table_name} (ID INTEGER PRIMARY KEY AUTOINCREMENT,SORU VARCHAR(500))")
    db.commit()
    return


def InsertInto(table_name, columns="SORU", values=None):
    if columns == "SORU":
        cursor.execute(f"INSERT INTO {table_name} ({columns}) VALUES (?)", (dumps(values),))
        db.commit()
        return
    cursor.execute(f"INSERT INTO {table_name} ({columns}) VALUES (?,?)", (dumps(values[0]), dumps(values[1])))
    db.commit()
    return


def SelectFrom(table_name, columns, amount=None, where="ID NOT NULL"):
    if amount:
        cursor.execute(f"""SELECT {columns} FROM {table_name} WHERE ID IN (SELECT ID FROM {table_name} 
                        WHERE {where} ORDER BY RANDOM() LIMIT {amount}) ORDER BY RANDOM()""")
        db.commit()
    else:
        cursor.execute(f"SELECT {columns} FROM {table_name} WHERE {where} ORDER BY RANDOM()")
        db.commit()
    sorular = []
    for i in cursor.fetchall().copy():
        row = []
        for j in i:
            row.append(loads(j))
        sorular.append(row)
    return sorular


# def DeleteFrom(table_name, where=None):
#     if where:
#         cursor.execute(f"DELETE FROM ({table_name}) WHERE ({where})")
#         db.commit()
#     else:
#         cursor.execute(f"DELETE FROM ({table_name})")
#         db.commit()
#     db.close()
#     return


class Sayi(Resource):
    def __init__(self):
        self.token = "fx!Ay:;<p6Q?C8N{"

    def post(self, game):
        args = request.parse_args()
        if args["Token"] == self.token:
            if args["Info"]:
                data_set = SelectFrom(table_name=game, columns="SORU")
                for s in data_set:
                    data_set[data_set.index(s)] = str(s[0])
                data = loads(args["Info"])["Data"]
                for i in data:
                    if type(i) == list:
                        if str(i) not in data_set:
                            InsertInto(table_name=game, columns=loads(args["Info"])["Req"], values=i)
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
                if args["Where"]:
                    return dumps({"Info": SelectFrom(table_name=game, columns=args["Req"], amount=num, where=args["Where"])})
                return dumps({"Info": SelectFrom(table_name=game, columns=args["Req"], amount=num)})
            else:
                if args["Where"]:
                    return dumps({"Info": SelectFrom(table_name=game, columns=args["Req"], where=args["Where"])})
                return dumps({"Info": SelectFrom(table_name=game, columns=args["Req"])})
        else:
            return {"Message": "Unauthorized"}, 401

    # def delete(self, game, level):
    #     num = request.parse_args()["info"]
    #     if storage:
    #         if num:
    #             num = int(num)
    #             for i in range(num):
    #                 storage[game][level].remove(storage[game][level][0])
    #             return {"Data": "Deleted"}, 201
    #         else:
    #             DeleteFrom(game, level)
    #             return {"Data": "Deleted"}, 201
    #     else:
    #         return {"Data": "Ulaşılamadı"}

    # def patch(self, game, level):
    #     pass


api.add_resource(Sayi, "/<string:game>")
if __name__ == '__main__':
    app.run(debug=True)
