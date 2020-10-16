import requests as r
from json import dumps, loads
import SayiBulmaca.SayiBulmaca_3 as SayiBulmaca_3
import SayiBulmaca.SayiBulmaca_4 as SayiBulmaca_4
import Sudoku.Sudoku_6x6 as Sudoku_6x6
import Sudoku.Sudoku_9x9 as Sudoku_9x9
import Piramit.piramit_deneme as Piramit
urlSudoku6Kolay = "http://127.0.0.1:5000/"+"Sudoku_6_KOLAY"
urlSayiBulmaca3 = "http://127.0.0.1:5000/"+"SayiBulmaca_3"
urlSayiBulmaca4 = "http://127.0.0.1:5000/"+"SayiBulmaca_4"
urlPiramit_3 = "http://127.0.0.1:5000/"+"Piramit_6"
token = "fx!Ay:;<p6Q?C8N{"


def PostGame(num, url1, req="SORU", token1=None, game=None, level=None):
    data = []
    if level:
        for _ in range(num):
            data.append(game.main(level))
    else:
        for _ in range(num):
            data.append(game.main())
    post = {"Info": dumps({"Data": data, "Req": req}), "Token": token1}
    req = r.post(url1, data=post)
    print(req.status_code, req.json())


def GetGame(num=None, req="SORU", token1=None, url1=None, where=None):
    get = {"Info": num, "Where": where, "Token": token1, "Req": req}
    req = r.get(url1, data=get)
    return req.status_code, req.json()


def PrintSayiBulmaca(veri):
    for a in veri:
        for _ in a:
            for j in _:
                print(j)
            print("------------------")


def PrintSudoku(veri):
    if len(veri[0][0]) == 6:
        for a in veri:
            for i in a:
                row1, row2, row3, row4, row5, row6 = i
                print(row1[0:3], row1[3:6])
                print(row2[0:3], row2[3:6])
                print('')
                print(row3[0:3], row3[3:6])
                print(row4[0:3], row4[3:6])
                print('')
                print(row5[0:3], row5[3:6])
                print(row6[0:3], row6[3:6])
                print("----------------------")
            print("----------------------")
    elif len(veri[0][0]) == 9:
        for a in veri:
            for i in a:
                row1, row2, row3, row4, row5, row6, row7, row8, row9 = i
                print(row1[0:3], row1[3:6], row1[6:9])
                print(row2[0:3], row2[3:6], row2[6:9])
                print(row3[0:3], row3[3:6], row3[6:9])
                print('')
                print(row4[0:3], row4[3:6], row4[6:9])
                print(row5[0:3], row5[3:6], row5[6:9])
                print(row6[0:3], row6[3:6], row6[6:9])
                print('')
                print(row7[0:3], row7[3:6], row7[6:9])
                print(row8[0:3], row8[3:6], row8[6:9])
                print(row9[0:3], row9[3:6], row9[6:9])
                print("--------------------------------")
            print("--------------------------------")


PostGame(num=5, url1=urlPiramit_3, token1=token, game=Piramit, level=6)
info = GetGame(url1=urlPiramit_3, token1=token)
print(info[0], info[1])
# res = loads(info[1])["Info"]
# PrintSayiBulmaca(res)
# for i in res:
#     for j in i:
#         print(j)
#     print("------------------")
# print(info[0])

