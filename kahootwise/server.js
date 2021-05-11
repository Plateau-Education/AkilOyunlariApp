const http = require('http');
const express = require('express');
const socketio = require('socket.io');
const lodash = require("lodash")
const axios = require("axios");
const { result } = require('lodash');

const app = express();
const server = http.createServer(app);
const io = socketio(server);

let rooms = []

const randint = (x, y) => {
  return Math.floor(Math.random() * (max - min) + min)
}

const generateCode = () => {
  while (1) {
    let code = randint(100000, 1000000)
    if (!rooms.find(room => room.room_id === code)){
      return code
    }
  }
}

const getGrids = async (gameTypes) => {
  result = axios.get("https://akiloyunlariapp.herokuapp.com/none/userid", {params: {Token: process.env.Token, Info: 1, Tournament: 1, GameTypes: gameTypes}})
  return await result.data
}

const isValidCl = (rooms) => {
    let counter = 0;
    let tmp = "None"
    for (const i of rooms) {
      counter++;
      tmp = i
    }
    if (counter > 1) {
      return tmp
    }
    else {
      return -1
    }
}

const joinToRoom = (user_name, role, roomid, sid, gridx="empty-grid") => {
  if (role==="Organizator"){
    const index = rooms.findIndex(room => room.room_id === roomid)
    if (index!==-1){
      rooms[index].organizator = {username: user_name, socket_id: sid}
      return true
    }
    else {
    console.log(user_name, role, roomid, sid)
    rooms.push({room_id: roomid, gridList: gridx, organizator: {username: user_name, socket_id: sid}, participants: []})
    return true
    }
  }
  else if (role==="Participant"){
    const index = rooms.findIndex(room => room.room_id === roomid)
    if (index!==-1){
      console.log(user_name, role, roomid, sid)
      rooms[index].participants.push({username: user_name, socket_id: sid, score: null})
      return true
    }
    else{
      return -1
    }
  }
  else {
    return -1
  }
  
}

io.on("connection", socket => {
  let roomUserIn = null
  let start = true
  let endOfRound = false
    socket.on("createRoom", data => {
      console.log(data, "createRoom")
      const code = generateCode()
      getGrids(data["gameTypes"]).then(dat => {
        rooms.push({room_id: code, gridList: dat, organizator: {username: data, socket_id: socket.id}, participants: []})
        io.to(socket.id).emit("roomCreated")
        socket.join(code)
        roomUserIn = code
      })            
    })
    socket.on("joinToRoom", data => {
      if (start){
        const room_index = rooms.findIndex(room => room.code === data["room_id"])
        if (room_index !== -1){
          rooms[room_index].participants.push({username: data["username"], socket_id: socket.id, score: 0})
          socket.join(data["room_id"])
          roomUserIn = data["room_id"]
          io.to(roomUserIn).emit("players", rooms[room_index].participants)
        }
      }
      else {
        io.to(socket.id).emit("gameStarted")
      }   
    })
    socket.on("start", () => {
      if (roomUserIn) {
        const room = rooms.find(room => room.code === roomUserIn)
        if (room) {
          socket.to(roomUserIn).emit("start", room.gridList)
          start = false
        }
      }
    })
    socket.on("sendScore", data => {
      if (roomUserIn) {
        const room_index = rooms.findIndex(room => room.code === roomUserIn)
        if (room_index !== -1) {
          const user_index = rooms[room_index].participants.findIndex(user => user.socket_id === socket.id)
          if (user_index !== -1) {            
            rooms[room_index].participants[user_index].score += data
            rooms[room_index].participants.sort((a, b) => b.score - a.score)
            endOfRound = true
          }
        }
      }
    })
    socket.on("endOfRound", () => {
      if (endOfRound) {
        if (roomUserIn){
          const room_index = rooms.findIndex(room => room.code === roomUserIn)
          if (room_index !== -1) {
            io.to(roomUserIn).emit("endOfRound", rooms[room_index].participants)
            endOfRound = false
          }
        }  
      }          
    })
    socket.on("loadGame", data => {
      
    })
    socket.on("disconnect", () => {
      console.log("disconnect")
      const room_index = rooms.findIndex(room => room.code === roomUserIn)
      console.log("room_index", room_index)
      if (room_index !== -1) {
        console.log("currentRoom", rooms[room_index])
        if (rooms[room_index].organizator.socket_id === socket.id) {
          rooms[room_index].organizator = null
        }
        else {
          rooms[room_index].participants = rooms[room_index].participants.filter(user => user.socket_id !== socket.id)
        }
        if (rooms[room_index].participants.length === 0 && rooms[room_index].organizator === null) {
          rooms = rooms.filter(room => room.code !== roomUserIn)
        }
      }
    })
})