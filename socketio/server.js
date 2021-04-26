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
      return false
    }
}

const indexbyParam = (arr, param, info) => {
  let iter = 0
  for (const item of arr) {
    if (item[param]===info){
      return iter
    }
    iter++
  }
  return false
}

const joinToRoom = (user_name, role, roomid, sid) => {
  if (role==="Instructor"){
    const index = indexbyParam(rooms, "room_id", roomid)
    if (index!==false){
      rooms[index].instructor = {username: user_name, socket_id: sid}
      return true
    }
    else {
    console.log(user_name, role, roomid, sid)
    rooms.push({room_id: roomid, instructor: {username: user_name, socket_id: sid}, students: []})
    return true
    }
  }
  else if (role==="Student"){
    const index = indexbyParam(rooms, "room_id", roomid)
    if (index!==false){
      rooms[index].students.push({username: user_name, socket_id: sid})
      return true
    }
    else{
      return false
    }
  }
  else {
    return false
  }
  
}

io.on("connection", socket => {
  console.log("Made socket connection");
  socket.on("joinToRoom", data => {
    const room = isValidCl(socket.rooms)
    if (room===false){
      if (joinToRoom(data["user_name"], data["role"], data["room_id"], socket.id)){
        socket.join(data["room_id"])
        io.to(data["room_id"]).emit("participants", rooms[indexbyParam(rooms, "room_id", data["room_id"])])        
      }
      else{
        console.log("joinToRoom false")
      }
    }
    else{
    console.log("joinToRoom false")
    }
  })
  socket.on("sendGrid", data => {
    const room = isValidCl(socket.rooms)
    if (room!==false){
        socket.to(room).emit("sendGrid", data)
        console.log(data)
    }
    else {
    console.log("sendGrid false")
    }
  })
  socket.on("voiceChat", data => {
    const room = isValidCl(socket.rooms)
    if (room!==false){
      socket.to(room).emit("voiceChat", data)
    }    
  })
  socket.on("getPermission", username => {
    const room = isValidCl(socket.rooms)
    if (room!==false){
      const index = indexbyParam(rooms, "room_id", room)
      if (index!==false){
        const instructor_sid = rooms[index].instructor.socket_id
        socket.to(instructor_sid).emit("getPermission", username)
      }
    }
  })
  socket.on("givePermission", username => {
    const room = isValidCl(socket.rooms)
    console.log(`givePer - username : ${username}`)
    console.log(`givePer - room : ${room}`)
    if (room!==false){
      const index = indexbyParam(rooms, "room_id", room)
      console.log(`givePer - index : ${index}`)
      if (index!==false){
        const student_index = indexbyParam(rooms[index].students, "username", username)
        if (student_index!==false){
          console.log(`givePer - stindex : ${student_index}`)
          socket.to(rooms[index].students[student_index].socket_id).emit("permissionGranted")
        }
      }
    }
  })
  socket.on("removePermission", username => {
    const room = isValidCl(socket.rooms)
    if (room!==false){
      const index = indexbyParam(rooms, "room_id", room)
      if (index!==false){
        const student_index = indexbyParam(rooms[index].students, "username", username)
        if (student_index!==false){
          socket.to(rooms[index].students[student_index].socket_id).emit("permissionRemoved")
        }
      }
    }
  })
});
io.on("disconnect", socket => {
  const room = isValidCl(socket.rooms)
  if (room !== false) {
    const index = indexbyParam(rooms, "room_id", room)
    if (index !== false) {
      if (rooms[index].instructor.socket_id === socket.id){
        rooms[index].instructor = null
      }
      else {
        rooms[index].students = rooms[index].students.filter(student => student.socket_id !== socket.id)
      }  
      if (rooms[index].instructor === null && rooms[index].students.length === 0){
        rooms = rooms.filter(room => room.room_id !== room)
      }
    }
  }
})

const PORT = process.env.PORT || 3000;

server.listen(PORT, () => console.log(`Server running on port ${PORT}`));