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
let rooms = [rooms2P, rooms3P, rooms5P]
let waiting2P = {"Sudoku6": [], "Sudoku9": [], "Patika": [], "HazineAvi": [], 
                 "SayiBulmaca": [], "SozcukTuru": [], "Anagram": [], "Pentomino": []}
let waiting3P = {"Sudoku6": [], "Sudoku9": [], "Patika": [], "HazineAvi": [], 
                 "SayiBulmaca": [], "SozcukTuru": [], "Anagram": [], "Pentomino": []}
let waiting5P = {"Sudoku6": [], "Sudoku9": [], "Patika": [], "HazineAvi": [], 
                 "SayiBulmaca": [], "SozcukTuru": [], "Anagram": [], "Pentomino": []}
let waiting = [waiting2P, waiting3P, waiting5P]

const getGames = (game => {
  
})

io.on("connection", socket => {
  let roomId = null
  let game = null
  let clust = null
  let pType = null
  let playersInRoom = null
  let timeFlag = true
  socket.on("loadGame", data => {

  })
  socket.on("playerJoin", data => {
    game = data["game"]
    clust = data["cluster"]
    pType = data["pType"]    
    waiting[pType][game].push({id: socket.id, username: uname, cluster: clust, score: null})
  })    
  socket.on("findRoom", () => {      
    let compatiplePlayers = waiting[pType][game].filter(player => player.cluster === clust)
    roomId = socket.id
    let neededPlayers = 1 - compatiplePlayers.length
    if (neededPlayers <= 0){        
      playersInRoom = compatiplePlayers.slice(0, 1)        
    }
    else {
      playersInRoom = lodash.sampleSize(waiting[pType][game], neededPlayers)
    }
    playersInRoom.push({id: socket.id, cluster: clust, score: null})
    waiting[pType][game] = waiting[pType][game].filter(player => playersInRoom.findIndex(pl => pl.id === player.id) === -1)
    rooms[pType].push({room_id: roomId, players: playersInRoom, currentP: 0, currentS: 0, games: getGames(game)})
    for (const sock of playersInRoom) {
      io.to(sock.id).emit("roomFound", socket.id)
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
    const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
    if (activeRoom !== -1) {
      const playerIndex = rooms[pType][activeRoom].players.findIndex(pl => pl.id === socket.id)
      if (playerIndex !== -1) {
        rooms[pType][activeRoom].players[playerIndex].score = data
        rooms[pType][activeRoom].currentS++
      }
    }
  })
  socket.on("getScores", () => {
    const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
    if (activeRoom !== -1) {
      if (rooms[pType][activeRoom].currentS === rooms[pType][activeRoom].currentP) {
        rooms[pType][activeRoom].players.sort((a, b) => {a.score - b.score})
        rooms[pType][activeRoom].players.reverse()
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
    }
  })
})


const PORT = process.env.PORT || 3000;

server.listen(PORT, () => console.log(`Server running on port ${PORT}`));