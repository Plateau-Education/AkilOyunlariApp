import requests as r
from json import dumps, loads
import SayiBulmaca_3
import SayiBulmaca_4
import sudoku6x6
import sudoku9x9

urlSudoku6Kolay = "https://akiloyunlariapp.herokuapp.com/"+"Sudoku_6_Kolay"
urlSayiBulmaca3 = "https://akiloyunlariapp.herokuapp.com/SayiBulmaca_3"
urlSayiBulmaca4 = "https://akiloyunlariapp.herokuapp.com/SayiBulmaca_4"
sample = "http://127.0.0.1:5000/"+"Sudoku_6_Kolay"
token = "fx!Ay:;<p6Q?C8N{"


def PostGame(num, url1, req=None, token1=None, game=None, level=None):
    data = set()
    if level:
        for _ in range(num):
            data.add(game.main(level))
    else:
        for _ in range(num):
            data.add(game.main())
    post = {"Info": dumps({"Data": list(data)}), "Req": req, "Token": token1}
    req = r.post(url1, data=post)
    print(req.status_code, req.json())


def GetGame(num=None, req=None, token1=None, url1=None):
    get = {"Info": num, "Token": token1, "Req": req}
    req = r.get(url1, data=get)
    return req.status_code, req.json()


def PrintSayiBulmaca(veri):
    for i in range(len(veri)):
        if len(veri[i]) == 1:
            veri[i] = veri[i][0]
    for a in veri:
        for e in a:
            print(e)
        print("------------------")


def PrintSudoku(veri):
    for i in range(len(veri)):
        if len(veri[i]) == 1:
            veri[i] = veri[i][0]
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


PostGame(num=10, url1=urlSudoku6Kolay, token1=token, game=sudoku6x6, req=True, level="Easy")
# info = GetGame(url1=urlSayiBulmaca3, token1=token)
# print(info[0])
# res = loads(info[1])["Info"]
# PrintSayiBulmaca(res)
# res = loads(r.get(urlSayiBulmaca3, data={"Info": 1, "Token": token, "Req": False}).json())["Info"]
# print(res, type(res))
# PostGame(985, urlSayiBulmaca3, token1=token, game=SayiBulmaca_3)
# for i in res:
#     for j in i:
#         print(j)
#     print("------------------")
# print(info[0])

