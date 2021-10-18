package com.example.securechit_chat.models

// chat model which will be stored on backend
class ChatMessage(val id: String , val text: String , val fromId: String , val toId : String , val timeStamp : Long){
    constructor() : this("" , "" , "" , "" , -1)
}
