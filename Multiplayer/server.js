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

const getWords = async (game) => {
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
    waiting[pType][game].push({id: socket.id, username: usern, cluster: clust, score: null})
  })    
  socket.on("findRoom", () => {      
    let compatiplePlayers = waiting[pType][game].filter(player => player.cluster === clust)
    roomId = socket.id
    let needNum = pType - compatiplePlayers.length - 1
    let neededPlayers = needNum > 0 ? needNum : 0
    if (waiting[pType][game].length < neededPlayers) {
      io.to(socket.id).emit("wait")
    }
    else {
      playersInRoom = lodash.sampleSize(waiting[pType][game], neededPlayers)
      playersInRoom.concat( compatiplePlayers.slice(0, pType - neededPlayers - 1))
      playersInRoom.push({id: socket.id, cluster: clust, score: null})
      waiting[pType][game] = waiting[pType][game].filter(player => playersInRoom.findIndex(pl => pl.id === player.id) === -1)
      getGames(game).then(dat => {
        rooms[pType].push({id: roomId, players: playersInRoom, currentP: 0, currentS: 0, games: dat})
      })    
      for (const sock of playersInRoom) {
        io.to(sock.id).emit("roomFound", socket.id)
      }
    }
  })
  socket.on("getInRoom", data => {
    let choosenRoom = rooms[pType].findIndex(room => room.id === data) 
    if (choosenRoom !== -1){
      roomId = data
      socket.join(data)
      rooms[pType][choosenRoom].currentP++
      if (rooms[pType][choosenRoom].currentP === pType) {
        io.to(data).emit("start", rooms[pType][choosenRoom])
      }        
    }
  })
  socket.on("sendScore", data => {
    if (timeFlag) {
      const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
      if (activeRoom !== -1) {
        const playerIndex = rooms[pType][activeRoom].players.findIndex(pl => pl.id === socket.id)
        if (playerIndex !== -1) {
          rooms[pType][activeRoom].players[playerIndex].score = data
          rooms[pType][activeRoom].currentS++
        }
      }
    }
    else {
      io.to(socket.id).emit("timeisup")
    }
  })
  socket.on("getScores", () => {
    const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
    if (activeRoom !== -1) {
      timeFlag = false
      if (rooms[pType][activeRoom].currentS >= rooms[pType][activeRoom].currentP) {
        rooms[pType][activeRoom].players.sort((a, b) => b.score - a.score)
        //rooms[pType][activeRoom].players.reverse()
        io.to(socket.id).emit("scores", rooms[pType][activeRoom].players)
      }
      else {
        io.to(socket.id).emit("wait")
      }
    }
  })
  socket.on("disconnect", () => {
    const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
    if (activeRoom !== -1) {
      rooms[pType][activeRoom].currentP--
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