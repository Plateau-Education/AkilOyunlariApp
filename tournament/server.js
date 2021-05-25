const http = require('http');
const express = require('express');
const socketio = require('socket.io');
const axios = require("axios");

const app = express();
const server = http.createServer(app);
const io = socketio(server);

let rooms = []

const randint = (x, y) => {
  return Math.floor(Math.random() * (y - x) + x)
}

const generateCode = () => {
  while (1) {
    let code = randint(100000, 1000000)
    if (!rooms.find(room => room.room_id == code)){
      return code
    }
  }
}

const getGrids = async (gameTypes) => {
  const result = await axios.get("https://akiloyunlariapp.herokuapp.com/none/userid", {params: {Token: process.env.Token, Info: 1, Tournament: JSON.stringify(gameTypes)}})
  return await result.data
}

io.on("connection", socket => {
  let roomUserIn = null
  let start = true
  let code = null
    socket.on("createRoom", data => {
      console.log(data, "createRoom")
      console.log(data["code"])
      if (data["code"]) {
        code = data["code"]
      }
      else {
        code = generateCode()
      }      
      getGrids(data["gameTypes"]).then(dat => {
        console.log(dat)
        console.log(code)
        rooms.push({room_id: code, gridList: dat, organizator: {username: data["username"], socket_id: socket.id}, participants: []})
        io.to(socket.id).emit("roomCreated", code)
        socket.join(code)
        roomUserIn = code
      })
    })
    socket.on("joinToRoom", data => {
      if (start){
        console.log(data["room_id"])
        console.log(rooms)
        const room_index = rooms.findIndex(room => room.room_id == data["room_id"])
        console.log(room_index)
        if (room_index !== -1){
          rooms[room_index].participants.push({username: data["username"], socket_id: socket.id, score: 0})
          socket.join(data["room_id"])
          roomUserIn = data["room_id"]
          io.to(roomUserIn).emit("players", rooms[room_index])
        }
        else {
          io.to(socket.id).emit("roomNotFound")
          console.log("roomNotFound ")
        }
      }
      else {
        io.to(socket.id).emit("gameStarted")
      }   
    })
    socket.on("start", () => {
      console.log("start")
      console.log(roomUserIn)
      if (roomUserIn) {
        const room = rooms.find(room => room.room_id == roomUserIn)
        console.log(room)
        if (room) {
          io.to(roomUserIn).emit("start", room.gridList)
          start = false
        }
      }
    })
    socket.on("sendScore", data => {
      if (roomUserIn) {
        const room_index = rooms.findIndex(room => room.room_id == roomUserIn)
        if (room_index !== -1) {
          const user_index = rooms[room_index].participants.findIndex(user => user.socket_id == socket.id)
          if (user_index !== -1) {            
            rooms[room_index].participants[user_index].score += data
            rooms[room_index].participants.sort((a, b) => b.score - a.score)
          }
        }
      }
    })
    socket.on("endOfRound", () => {
      if (roomUserIn){
        const room_index = rooms.findIndex(room => room.room_id == roomUserIn)
        if (room_index !== -1) {
          console.log("endOfRound", socket.id)
          io.to(socket.id).emit("endOfRound", rooms[room_index].participants)
        }
      }  
    })
    socket.on("loadGame", data => {
      
    })
    socket.on("disconnect", () => {
      console.log("disconnect")
      const room_index = rooms.findIndex(room => room.room_id == roomUserIn)
      console.log("room_index", room_index)
      if (room_index !== -1) {
        console.log("currentRoom", rooms[room_index])
        rooms[room_index].participants = rooms[room_index].participants.filter(user => user.socket_id !== socket.id)
        if (rooms[room_index].organizator) {
          if (rooms[room_index].organizator.socket_id == socket.id) {
            rooms[room_index].organizator = null            
          }
        }
        console.log("participants' length", rooms[room_index].participants.length)
        if (rooms[room_index].participants.length == 0 && rooms[room_index].organizator == null) {
          rooms = rooms.filter(room => room.room_id != roomUserIn)
        }
        else {
          io.to(roomUserIn).emit("players", rooms[room_index])
        }
      }
    })
})

const PORT = process.env.PORT || 3000;

server.listen(PORT, () => console.log(`Server running on port ${PORT}`));