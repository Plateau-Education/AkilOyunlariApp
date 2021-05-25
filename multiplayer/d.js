const axios = require("axios")

const res = axios.get(`https://akiloyunlariapp.herokuapp.com/HazineAvi/userid`, {params: {Token: "fx!Ay:;<p6Q?C8N{", Info: 1, "Multiplayer": 1}})
console.log(res)