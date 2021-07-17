const http = require('http')
const express = require('express')
const socketio = require('socket.io')
const lodash = require("lodash")
const axios = require("axios")

const app = express()
const server = http.createServer(app)
const io = socketio(server)

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

let classrooms = []
let contestrooms = []

const getGames = async (game) => {
  const res = await axios.get(`https://akiloyunlariapp.herokuapp.com/${game}/userid`, {params: {Token: "fx!Ay:;<p6Q?C8N{", Info: 1, "Multiplayer": 1}})
  return await res.data;
};

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

const digiEdu_joinToRoom = (user_name, role, roomid, sid, gridx="empty-grid", gametype="game-type") => {
  if (role==="Instructor"){
    const index = classrooms.findIndex(room => room.room_id === roomid)
    if (index!==-1){
      classrooms[index].participants.instructor = {username: user_name, socket_id: sid}
      return true
    }
    else {
    console.log(user_name, role, roomid, sid)
    classrooms.push({room_id: roomid, gameType: gametype, grid: gridx, participants: {instructor: {username: user_name, socket_id: sid}, students: []}})
    return true
    }
  }
  else if (role==="Student"){
    const index = classrooms.findIndex(room => room.room_id === roomid)
    if (index!==-1){
      console.log(user_name, role, roomid, sid)
      classrooms[index].participants.students.push({username: user_name, socket_id: sid})
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

const randint = (x, y) => {
    return Math.floor(Math.random() * (y - x) + x)
  }
  
  const generateCode = () => {
    while (1) {
      let code = randint(100000, 1000000)
      if (!contestrooms.find(room => room.room_id == code)){
        return code
      }
    }
  }
  
  const getGrids = async (gameTypes) => {
    const result = await axios.get("https://akiloyunlariapp.herokuapp.com/none/userid", {params: {Token: process.env.Token, Info: 1, Tournament: JSON.stringify(gameTypes)}})
    return await result.data
  }
  

io.on("connection", socket => {
    
  let usageType = null

  let roomId = null
  let game = null
  let clust = null
  let pType = null
  let playersInRoom = null
  let timeFlag = true
  let usern = null

  let contestroomUserIn = null
  let start = true
  let code = null

  let classroomUserIn = null
  // multiplayer-side
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
    console.log("current PLayer", currentPlayer, usern)
    waiting[pType][game] = waiting[pType][game].filter(player => player.id !== socket.id)
    let compatiplePlayers = waiting[pType][game].filter(player => player.cluster === clust)
    roomId = socket.id
    let needNum = pType - compatiplePlayers.length - 1
    let neededPlayers = needNum > 0 ? needNum : 0
    console.log("compatiple", compatiplePlayers, usern)
    console.log("needed", neededPlayers, usern)
    if (waiting[pType][game].length < neededPlayers) {
      if (currentPlayer !== undefined){
      waiting[pType][game].push(currentPlayer)
      }
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
        console.log("room Found", usern, sock.id)
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
  socket.on("multiplayer_sendScore", data => {
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
          if (rooms[pType][activeRoom].currentS == pType) {
            io.to(roomId).emit("scores", rooms[pType][activeRoom].players)
          }
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
  // tournament-side
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
      contestrooms.push({room_id: code, gridList: dat, organizator: {username: data["username"], socket_id: socket.id}, participants: []})
      io.to(socket.id).emit("roomCreated", code)
      socket.join(code)
      contestroomUserIn = code
    })
  })
  socket.on("tournament_joinToRoom", data => {
    console.log("tournament_joinToRoom")
    if (start){
      console.log(data["room_id"])
      console.log(contestrooms)
      const room_index = contestrooms.findIndex(room => room.room_id == data["room_id"])
      console.log(room_index)
      if (room_index !== -1){
        contestrooms[room_index].participants.push({username: data["username"], socket_id: socket.id, score: 0})
        socket.join(data["room_id"])
        contestroomUserIn = data["room_id"]
        io.to(contestroomUserIn).emit("players", contestrooms[room_index])
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
    console.log(contestroomUserIn)
    if (contestroomUserIn) {
      const room = contestrooms.find(room => room.room_id == contestroomUserIn)
      console.log(room)
      if (room) {
        io.to(contestroomUserIn).emit("start", room.gridList)
        start = false
      }
    }
  })
  socket.on("tournament_sendScore", data => {
    console.log("tournament_sendScore")
    if (contestroomUserIn) {
      const room_index = contestrooms.findIndex(room => room.room_id == contestroomUserIn)
      if (room_index !== -1) {
        const user_index = contestrooms[room_index].participants.findIndex(user => user.socket_id == socket.id)
        if (user_index !== -1) {            
          contestrooms[room_index].participants[user_index].score += data
          contestrooms[room_index].participants.sort((a, b) => b.score - a.score)
        }
      }
    }
  })
  socket.on("endOfRound", () => {
    console.log("endofround", contestroomUserIn)
    if (contestroomUserIn){
      const room_index = contestrooms.findIndex(room => room.room_id == contestroomUserIn)
      if (room_index !== -1) {
        console.log("endOfRound", socket.id)
        io.to(socket.id).emit("endOfRound", contestrooms[room_index].participants)
      }
    }  
  })
  // digiEdu-side
  socket.on("digiEdu_joinToRoom", data => {
    console.log("digiEdu_joinToRoom")
    const room = isValidCl(socket.rooms)
    if (room===-1){
      if (digiEdu_joinToRoom(data["user_name"], data["role"], data["room_id"], socket.id, data["grid"], data["gameType"]) !== -1){
        socket.join(data["room_id"])
        console.log(data["room_id"])
        console.log(classrooms)
        classroomUserIn = classrooms.find(room => room.room_id === data["room_id"])
        console.log(classroomUserIn)
        if (classroomUserIn) {
          io.to(data["room_id"]).emit("participants", classroomUserIn)          
          io.to(socket.id).emit("gameType", classroomUserIn.gameType)
          io.to(socket.id).emit("sendGrid", classroomUserIn.grid)
        }
        else {
          io.to(socket.id).emit("digiEdu_joinToRoom", "failed")
          console.log("digiEdu_joinToRoom -1")
        }
           
      }
      else{
        io.to(socket.id).emit("digiEdu_joinToRoom", "failed")
        console.log("digiEdu_joinToRoom -1")
      }
    }
    else{
      io.to(socket.id).emit("digiEdu_joinToRoom", "failed")
      console.log("digiEdu_joinToRoom -1")
    }
  })
  socket.on("sendGrid", data => {
    console.log("sendGrid")
    const roomid = isValidCl(socket.rooms)
    if (roomid!==-1){
      const index = classrooms.findIndex(room => room.room_id === roomid)
      //console.log(index)
      if (index !== -1){
        classrooms[index].grid = data
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
    console.log("gameType")
    const roomid = isValidCl(socket.rooms)
    if (roomid!==-1){
      const index = classrooms.findIndex(room => room.room_id === roomid)
      //console.log(index)
      if (index !== -1){
        classrooms[index].gameType = data
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
    console.log("voiceChat")
    const roomid = isValidCl(socket.rooms)
    if (roomid!==-1){
      const index = classrooms.findIndex(room => room.room_id === roomid)
      //console.log(index)
      if (index !== -1){
        classrooms[index].grid = data
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
      const index = classrooms.findIndex(room => room.room_id === roomid)
      console.log(`givePer - index : ${index}`)
      if (index!==-1){
        const student_index = classrooms[index].participants.students.findIndex(st => st.username === username)
        console.log(`givePer - stindex : ${student_index}`)
        if (student_index!==-1){          
          console.log(`st_sid : ${classrooms[index].participants.students[student_index].socket_id}`)
          socket.to(classrooms[index].participants.students[student_index].socket_id).emit("permissionGranted")
        }
      }
    }
  })
  socket.on("removePermission", username => {
    const roomid = isValidCl(socket.rooms)
    console.log(`giveRem - username : ${username}`)
    console.log(`giveRem - room : ${roomid}`)
    if (roomid!==-1){
      const index = classrooms.findIndex(room => room.room_id === roomid)
      console.log(`giveRem - index : ${index}`)
      if (index!==-1){
        const student_index = classrooms[index].participants.students.findIndex(st => st.username === username)
        console.log(`giveRem - stindex : ${student_index}`)
        if (student_index!==-1) {       
          console.log(`st_sid : ${classrooms[index].participants.students[student_index].socket_id}`)
          socket.to(classrooms[index].participants.students[student_index].socket_id).emit("permissionRemoved")
        }
      }
    }
  })
  // all-in
  socket.on("usageType", data => {
      usageType = data
  })
  socket.on("disconnect", () => {
    if (usageType === "multiplayer"){
        console.log("multiplayer_disconnect", usern)
        const activeRoom = rooms[pType].findIndex(room => room.id === roomId)
        console.log("activeRoom", activeRoom)
        if (activeRoom !== -1) {
        rooms[pType][activeRoom].currentP--
        console.log("currentP", rooms[pType][activeRoom].currentP)
        if (rooms[pType][activeRoom].currentP === 0) {
            rooms[pType] = rooms[pType].filter(room => room.id !== roomId)
      }
        }
    }
    else if (usageType === "tournament"){
        console.log("tournament_disconnect")
        const room_index = contestrooms.findIndex(room => room.room_id == contestroomUserIn)
        console.log("room_index", room_index)
        if (room_index !== -1) {
            console.log("currentRoom", contestrooms[room_index])
            contestrooms[room_index].participants = contestrooms[room_index].participants.filter(user => user.socket_id !== socket.id)
            if (contestrooms[room_index].organizator) {
            if (contestrooms[room_index].organizator.socket_id == socket.id) {
                contestrooms[room_index].organizator = null            
            }
            }
            console.log("participants' length", contestrooms[room_index].participants.length)
            if (contestrooms[room_index].participants.length == 0 && contestrooms[room_index].organizator == null) {
            contestrooms = contestrooms.filter(room => room.room_id != contestroomUserIn)
            }
            else {
            io.to(contestroomUserIn).emit("players", contestrooms[room_index])
            }
        }
    }
    else if (usageType === "digiEdu"){
        console.log("digiEdu_disconnect")
        console.log(classrooms)
        console.log(classroomUserIn)
        if (classroomUserIn) {
        const index = classrooms.findIndex(room => room.room_id === classroomUserIn.room_id)
        console.log(index)
        if (index !== -1) {
            socket.leave(classroomUserIn.room_id)
            if (classrooms[index].participants.instructor) {
            if (classrooms[index].participants.instructor.socket_id === socket.id){
                classrooms[index].participants.instructor = null
                console.log("ins")
            }
            else {
                classrooms[index].participants.students = classrooms[index].participants.students.filter(student => student.socket_id !== socket.id)
                console.log("st")
            }
            }
            else {
            classrooms[index].participants.students = classrooms[index].participants.students.filter(student => student.socket_id !== socket.id)
            console.log("st")
            }  
            if (classrooms[index].participants.instructor === null && classrooms[index].participants.students.length === 0){
            classrooms = classrooms.filter(room => room.room_id !== classroomUserIn.room_id)
            }
            else {
            io.to(classroomUserIn.room_id).emit("participants", classrooms[index])
            }
        }
        }
    }
    else {
        console.log("usageType Error - disconnect")
    }    
  })
  socket.on("loadGame", data => {
    if (usageType === "multiplayer"){
        roomId = data["roomId"]
        game = data["game"]
        clust = data["cluster"]
        pType = data["pType"]
    }    
    else if (usageType === "tournament"){

    }
    else {
        console.log("usageType Error - loadGame")
    }
  })
})


const PORT = process.env.PORT || 3000;

server.listen(PORT, () => console.log(`Server running on port ${PORT}`));