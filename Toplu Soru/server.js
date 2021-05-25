const http = require('http');
const express = require('express');
const socketio = require('socket.io');


const app = express();
const server = http.createServer(app);
const io = socketio(server);

let rooms = [];

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

const joinToRoom = (user_name, role, roomid, sid, gridx="empty-grid", gametype="game-type") => {
  if (role==="Instructor"){
    const index = rooms.findIndex(room => room.room_id === roomid)
    if (index!==-1){
      rooms[index].participants.instructor = {username: user_name, socket_id: sid}
      return true
    }
    else {
    console.log(user_name, role, roomid, sid)
    rooms.push({room_id: roomid, gameType: gametype, grid: gridx, participants: {instructor: {username: user_name, socket_id: sid}, students: []}})
    return true
    }
  }
  else if (role==="Student"){
    const index = rooms.findIndex(room => room.room_id === roomid)
    if (index!==-1){
      console.log(user_name, role, roomid, sid)
      rooms[index].participants.students.push({username: user_name, socket_id: sid})
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
  console.log("Made socket connection");
  socket.on("joinToRoom", data => {
    const room = isValidCl(socket.rooms)
    if (room===-1){
      if (joinToRoom(data["user_name"], data["role"], data["room_id"], socket.id, data["grid"], data["gameType"]) !== -1){
        socket.join(data["room_id"])
        console.log(data["room_id"])
        console.log(rooms)
        roomUserIn = rooms.find(room => room.room_id === data["room_id"])
        console.log(roomUserIn)
        if (roomUserIn) {
          io.to(data["room_id"]).emit("participants", roomUserIn)          
          io.to(socket.id).emit("gameType", roomUserIn.gameType)
          io.to(socket.id).emit("sendGrid", roomUserIn.grid)
        }
        else {
          io.to(socket.id).emit("joinToRoom", "failed")
          console.log("joinToRoom -1")
        }
           
      }
      else{
        io.to(socket.id).emit("joinToRoom", "failed")
        console.log("joinToRoom -1")
      }
    }
    else{
      io.to(socket.id).emit("joinToRoom", "failed")
      console.log("joinToRoom -1")
    }
  })
  socket.on("sendGrid", data => {
    const roomid = isValidCl(socket.rooms)
    if (roomid!==-1){
      const index = rooms.findIndex(room => room.room_id === roomid)
      //console.log(index)
      if (index !== -1){
        rooms[index].grid = data
        socket.to(roomid).emit("sendGrid", data)
        //console.log(data)
      }
      else {
        io.to(socket.id).emit("sendGrid", "failed")
        //console.log("sendGrid -1")
      }
    }
    else {
      io.to(socket.id).emit("sendGrid", "failed")
      //console.log("sendGrid -1")
    }
  })
  socket.on("gameType", data => {
    const roomid = isValidCl(socket.rooms)
    if (roomid!==-1){
      const index = rooms.findIndex(room => room.room_id === roomid)
      //console.log(index)
      if (index !== -1){
        rooms[index].gameType = data
        socket.to(roomid).emit("gameType", data)
        //console.log(data)
      }
      else {
        io.to(socket.id).emit("gameType", "failed")
        //console.log("gameType -1")
      }
    }
    else {
      io.to(socket.id).emit("gameType", "failed")
      //console.log("gameType -1")
    }
  })
  socket.on("voiceChat", data => {
    const roomid = isValidCl(socket.rooms)
    if (roomid!==-1){
      const index = rooms.findIndex(room => room.room_id === roomid)
      //console.log(index)
      if (index !== -1){
        rooms[index].grid = data
        socket.to(roomid).emit("voiceChat", data)
        console.log(data)
      }
      else {
        io.to(socket.id).emit("voiceChat", "failed")
        //console.log("sendGrid -1")
      }
    }
    else {
      io.to(socket.id).emit("voiceChat", "failed")
      //console.log("sendGrid -1")
    }  
  })
  socket.on("givePermission", username => {
    const roomid = isValidCl(socket.rooms)
    console.log(`givePer - username : ${username}`)
    console.log(`givePer - room : ${roomid}`)
    if (roomid!==-1){
      const index = rooms.findIndex(room => room.room_id === roomid)
      console.log(`givePer - index : ${index}`)
      if (index!==-1){
        const student_index = rooms[index].participants.students.findIndex(st => st.username === username)
        console.log(`givePer - stindex : ${student_index}`)
        if (student_index!==-1){          
          console.log(`st_sid : ${rooms[index].participants.students[student_index].socket_id}`)
          socket.to(rooms[index].participants.students[student_index].socket_id).emit("permissionGranted")
        }
      }
    }
  })
  socket.on("removePermission", username => {
    const roomid = isValidCl(socket.rooms)
    console.log(`giveRem - username : ${username}`)
    console.log(`giveRem - room : ${roomid}`)
    if (roomid!==-1){
      const index = rooms.findIndex(room => room.room_id === roomid)
      console.log(`giveRem - index : ${index}`)
      if (index!==-1){
        const student_index = rooms[index].participants.students.findIndex(st => st.username === username)
        console.log(`giveRem - stindex : ${student_index}`)
        if (student_index!==-1) {       
          console.log(`st_sid : ${rooms[index].participants.students[student_index].socket_id}`)
          socket.to(rooms[index].participants.students[student_index].socket_id).emit("permissionRemoved")
        }
      }
    }
  })
  socket.on("disconnect", () => {
    console.log("disconnect")
    console.log(rooms)
    console.log(roomUserIn)
    if (roomUserIn) {
      const index = rooms.findIndex(room => room.room_id === roomUserIn.room_id)
      console.log(index)
      if (index !== -1) {
        socket.leave(roomUserIn.room_id)
        if (rooms[index].participants.instructor) {
          if (rooms[index].participants.instructor.socket_id === socket.id){
            rooms[index].participants.instructor = null
            console.log("ins")
          }
          else {
            rooms[index].participants.students = rooms[index].participants.students.filter(student => student.socket_id !== socket.id)
            console.log("st")
          }
        }
        else {
          rooms[index].participants.students = rooms[index].participants.students.filter(student => student.socket_id !== socket.id)
          console.log("st")
        }  
        if (rooms[index].participants.instructor === null && rooms[index].participants.students.length === 0){
          rooms = rooms.filter(room => room.room_id !== roomUserIn.room_id)
        }
        else {
          io.to(roomUserIn.room_id).emit("participants", rooms[index])
        }
      }
    }
  })
})

const PORT = process.env.PORT || 3000;

server.listen(PORT, () => console.log(`Server running on port ${PORT}`));