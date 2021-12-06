package com.example.securechit_chat.views

import android.widget.ImageView
import android.widget.TextView
import com.example.securechit_chat.Aes
import com.example.securechit_chat.R
import com.example.securechit_chat.messages.MessagesList
import com.example.securechit_chat.models.ChatMessage
import com.example.securechit_chat.models.User
import com.example.securechit_chat.registerLogin.SignUp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){


    var chatPartnerUser: User? = null
    private val decrypter: Aes = Aes()

    override fun getLayout(): Int {
        return R.layout.chat_layout_home
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.findViewById<TextView>(R.id.latestMessage).text = chatMessage.text

        val chatPartnerId : String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }else{
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.userNamerecent).text = decrypter.decrypt(chatPartnerUser?.name , SignUp.key)

                Picasso.get().load(decrypter.decrypt(chatPartnerUser?.profilePic , SignUp.key)).into(viewHolder.itemView.findViewById<ImageView>(
                    R.id.profilePicIVRecent))
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })





    }
}