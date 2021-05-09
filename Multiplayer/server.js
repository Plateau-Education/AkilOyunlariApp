const http = require('http');
const express = require('express');
const socketio = require('socket.io');
const lodash = require("lodash")
const axios = require("axios")

const app = express();
const server = http.createServer(app);
const io = socketio(server);

let rooms2P = []
let rooms3P = []
let rooms5P = []
let rooms = {2: rooms2P, 3: rooms3P, 5: rooms5P}
let waiting2P = {"Sudoku6": [], "Sudoku9": [], "Patika": [], "HazineAvi": [], "Piramit": [], 
                 "SayiBulmaca": [], "SozcukTuru": [], "Anagram": [], "Pentomino": []}
let waiting3P = {"Sudoku6": [], "Sudoku9": [], "Patika": [], "HazineAvi": [], "Piramit": [],
                 "SayiBulmaca": [], "SozcukTuru": [], "Anagram": [], "Pentomino": []}
let waiting5P = {"Sudoku6": [], "Sudoku9": [], "Patika": [], "HazineAvi": [], "Piramit": [],
                 "SayiBulmaca": [], "SozcukTuru": [], "Anagram": [], "Pentomino": []}
let waiting = {2: waiting2P, 3: waiting3P, 5: waiting5P}

const getGames = async (game) => {
  const res = await axios.get(`https://akiloyunlariapp.herokuapp.com/${game}/userid`, {params: {Token: "fx!Ay:;<p6Q?C8N{", Info: 1, "Multiplayer": 1}})
  return await res.data;
};

io.on("connection", socket => {
  let roomId = null
  let game = null
  let clust = null
  let pType = null
  let playersInRoom = null
  let timeFlag = true
  let usern = null
  socket.on("playerJoin", data => {
    game = data["game"]
    clust = data["cluster"]
    pType = data["pType"]
    usern = data["username"]
    console.log("infos")
    console.log(game, clust, pType, usern)
    waiting[pType][game].push({id: socket.id, username: usern, cluster: clust, score: null})
    console.log("waiting")
    console.log(waiting[pType][game])
  })    
  socket.on("findRoom", () => {
    console.log("findRoom - user name", usern)
    let currentPlayer = waiting[pType][game].filter(player => player.id === socket.id)[0]
    waiting[pType][game] = waiting[pType][game].filter(player => player.id !== socket.id)
    let compatiplePlayers = waiting[pType][game].filter(player => player.cluster === clust)
    roomId = socket.id
    let needNum = pType - compatiplePlayers.length - 1
    let neededPlayers = needNum > 0 ? needNum : 0
    console.log("compatiple", compatiplePlayers, usern)
    console.log("needed", neededPlayers, usern)
    if (waiting[pType][game].length < neededPlayers) {
      waiting[pType][game].push(currentPlayer)
      io.to(socket.id).emit("wait")      
    }
    else {
      playersInRoom = lodash.sampleSize(waiting[pType][game], neededPlayers)
      playersInRoom = playersInRoom.concat(compatiplePlayers.slice(0, pType - neededPlayers - 1))
      console.log("list for extend", compatiplePlayers.slice(0, pType - neededPlayers - 1))
      console.log("Early - playerlist", playersInRoom, usern)
      playersInRoom.push({id: socket.id, username: usern, cluster: clust, score: null})
      waiting[pType][game] = waiting[pType][game].filter(player => playersInRoom.findIndex(pl => pl.id === player.id) === -1)
      rooms[pType].push({id: roomId, players: playersInRoom, currentP: 0, currentS: 0, games: null})
      console.log("waiting", waiting[pType][game])
      console.log("PlayersInRoom", playersInRoom, usern)
      console.log("room", rooms[pType])
      for (const sock of playersInRoom) {
        io.to(sock.id).emit("roomFound", socket.id)
      }
    }
  })
  socket.on("getInRoom", data => {
    console.log("getInRoom - username", usern)
    console.log("data", data)
    let choosenRoom = rooms[pType].findIndex(room => room.id === data)
    console.log("choosenIndex", choosenRoom)
    console.log("choosenRoom", rooms[pType][choosenRoom])
    if (choosenRoom !== -1){
      roomId = data
      socket.join(data)
      rooms[pType][choosenRoom].currentP++
      console.log("currentP", rooms[pType][choosenRoom].currentP, typeof rooms[pType][choosenRoom].currentP)
      console.log("pType", pType, usern, typeof pType)
      if (rooms[pType][choosenRoom].currentP == pType) {
        console.log("passed")
        getGames(game).then(dat => {
          console.log("getGames", usern)
          rooms[pType][choosenRoom].games = dat
          console.log("data", dat)
          console.log(rooms[pType][choosenRoom])
          io.to(data).emit("start", rooms[pType][choosenRoom])
        })        
      }        
    }
  })
  socket.on("sendScore", data => {
    console.log("flag", timeFlag, usern)
    if (timeFlag) {
      const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
      console.log("activeROom", activeRoom)
      if (activeRoom !== -1) {
        const playerIndex = rooms[pType][activeRoom].players.findIndex(pl => pl.id === socket.id)
        console.log("playerIndex", playerIndex)
        if (playerIndex !== -1) {
          console.log("data", data)
          rooms[pType][activeRoom].players[playerIndex].score = data
          rooms[pType][activeRoom].currentS++
        }
      }
    }
    else {
      io.to(socket.id).emit("timeis up", usern)
    }
  })
  socket.on("getScores", () => {
    console.log("getScore - username", usern)
    const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
    console.log("activeRoom", activeRoom)
    if (activeRoom !== -1) {      
      console.log("currentS", rooms[pType][activeRoom].currentS)
      console.log("currentP", rooms[pType][activeRoom].currentP)
      if (!timeFlag) {
        rooms[pType][activeRoom].players.sort((a, b) => b.score - a.score)
        //rooms[pType][activeRoom].players.reverse(),
        console.log("playerList", rooms[pType][activeRoom].players)
        io.to(socket.id).emit("scores", rooms[pType][activeRoom].players)
      }
      else {
        timeFlag = false
        io.to(socket.id).emit("wait")
      }
    }
  })
  socket.on("disconnect", () => {
    console.log("disconnect", usern)
    const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
        console.log("activeRoom", activeRoom)
    if (activeRoom !== -1) {
      rooms[pType][activeRoom].currentP--
      console.log("currentP", rooms[pType][activeRoom].currentP)
      if (rooms[pType][activeRoom].currentP === 0) {
        rooms[pType] = rooms[pType].filter(room => room.id !== roomId)
      }
    }
  })
  socket.on("loadGame", data => {
    roomId = data["roomId"]
    game = data["game"]
    clust = data["cluster"]
    pType = data["pType"]    
  })
})


const PORT = process.env.PORT || 3000;

server.listen(PORT, () => console.log(`Server running on port ${PORT}`));