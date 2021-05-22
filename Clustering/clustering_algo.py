# -*- coding: utf-8 -*-
"""
Created on Sun May  2 23:35:30 2021

@author: yAquila04
"""


#%%

import pandas as pd
from pymongo import MongoClient
import ssl
from sklearn.cluster import AgglomerativeClustering
from sklearn.cluster import KMeans

#%%
cluster = MongoClient("mongodb+srv://Efdal:app1oyunakil@userbase.1bjhc.mongodb.net/AkilOyunlariUsers?retryWrites=true&w=majority",ssl_cert_reqs=ssl.CERT_NONE)
db = cluster["AkilOyunlariUsers"]
userdb = db["Users"]


games = ["Sudoku.6.Easy", "Sudoku.6.Medium", "Sudoku.6.Hard", "Sudoku.9.Easy",
         "Sudoku.9.Medium", "Sudoku.9.Hard", "SozcukTuru.Easy", "SozcukTuru.Medium",
         "SozcukTuru.Hard", "SozcukTuru.Hardest", "SayiBulmaca.3", "SayiBulmaca.4",
         "SayiBulmaca.5", "Patika.5", "Patika.7", "Patika.9", "HazineAvi.5",
         "HazineAvi.8", "HazineAvi.10", "Piramit.3", "Piramit.4", "Piramit.5", "Piramit.6", "Pentomino.Easy",
         "Pentomino.Medium", "Pentomino.Hard", "Anagram.Easy", "Anagram.Medium", "Anagram.Hard"]
users = []
flag = 1
for user in userdb.find():
    if flag == 1:
        flag += 1
        continue
    u = {"userid": user["_id"], "stats": {}}
    for game in games:
        info = user["solved"]
        for i in game.split("."):
            info = info[i]
        u["stats"][game] = info[1][-1]
    users.append(u)

gameSplit = [["userid","Sudoku.6.Easy_t", "Sudoku.6.Medium_t", "Sudoku.6.Hard_t", "Sudoku.9.Easy_t", "Sudoku.9.Medium_t", "Sudoku.9.Hard_t", 
         "Sudoku.6.Easy_a", "Sudoku.6.Medium_a", "Sudoku.6.Hard_a", "Sudoku.9.Easy_a", "Sudoku.9.Medium_a", "Sudoku.9.Hard_a"], 
        ["userid","SozcukTuru.Easy_t", "SozcukTuru.Medium_t", "SozcukTuru.Hard_t", "SozcukTuru.Hardest_t", 
         "SozcukTuru.Easy_a", "SozcukTuru.Medium_a", "SozcukTuru.Hard_a", "SozcukTuru.Hardest_a"], 
        ["userid","SayiBulmaca.3_t", "SayiBulmaca.4_t", "SayiBulmaca.5_t", "SayiBulmaca.3_a", "SayiBulmaca.4_a", "SayiBulmaca.5_a"], 
        ["userid","Patika.5_t", "Patika.7_t", "Patika.9_t", "Patika.5_a", "Patika.7_a", "Patika.9_a"], 
        ["userid","HazineAvi.5_t", "HazineAvi.8_t", "HazineAvi.10_t", "HazineAvi.5_a", "HazineAvi.8_a", "HazineAvi.10_a"], 
        ["userid","Piramit.3_t", "Piramit.4_t", "Piramit.5_t", "Piramit.6_t", "Piramit.3_a", "Piramit.4_a", "Piramit.5_a", "Piramit.6_a"],
        ["userid","Pentomino.Easy_t", "Pentomino.Medium_t", "Pentomino.Hard_t", "Pentomino.Easy_a", "Pentomino.Medium_a", "Pentomino.Hard_a"],
        ["userid","Anagram.Easy_t", "Anagram.Medium_t", "Anagram.Hard_t", "Anagram.Easy_a", "Anagram.Medium_a", "Anagram.Hard_a"]]


#%%

dfDictList = dict()
for gameNames in gameSplit:
    gameDict = {key:[] for key in gameNames}
    dfDictList[gameNames[1].split(".")[0]] = gameDict

useridList = []

for user in users:
    for i in ["Sudoku","SozcukTuru","SayiBulmaca","Patika","HazineAvi","Piramit","Pentomino","Anagram"]:
        dfDictList[i]["userid"].append(user["userid"])
        useridList.append(user["userid"])
    for game in games:
        if user["stats"][game][0] == 0: 
            dfDictList[game.split(".")[0]][game+"_t"].append(0)
        else:
            dfDictList[game.split(".")[0]][game+"_t"].append(1/user["stats"][game][0])
        
        dfDictList[game.split(".")[0]][game+"_a"].append(user["stats"][game][1])
    
    
dataFrameList = []
for i in ["Sudoku","SozcukTuru","SayiBulmaca","Patika","HazineAvi","Piramit","Pentomino","Anagram"]:
    dataFrameList.append(pd.DataFrame.from_dict(data=dfDictList[i]))

XList = []
for df in dataFrameList:
    XList.append(df.iloc[:,1:].values)
    

predList = []
for X in XList:
#    agc= AgglomerativeClustering(n_clusters=4,linkage='ward',affinity='euclidean')
    km= KMeans(n_clusters=5,init='k-means++')
    predList.append(km.fit_predict(X))

for i in range(len(predList)):
    gameNames = ["Sudoku","SozcukTuru","SayiBulmaca","Patika","HazineAvi","Piramit","Pentomino","Anagram"]
    for p in range(len(predList[i])):
        userdb.find_one_and_update({"_id": str(useridList[p])}, update={"$set": {"cluster."+gameNames[i]: int(predList[i][p])}})


