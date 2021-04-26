import socketio

client = socketio.Client()
client.connect("http://localhost:3000/")
# https://server4groups.herokuapp.com/
# http://localhost:3000/
client.emit("joinToRoom", {"user_name": "zedOTP", "role": "Student", "room_id": "deneme"})
# input()
client.emit("sendGrid", "denemegrid")
client.emit("getPermission", "zedOTP")

@client.event
def sendGrid(data):
    print(data)

@client.event
def participants(data):
    print(type(data))
    print(data)

@client.event
def voiceChat(data):
    print(data)

@client.event
def permissionGranted():
    print("Permission Granted")

@client.event
def permissionRemoved():
    print("Permission Removed")


client.wait()
