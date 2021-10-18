package com.example.securechit_chat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import com.example.securechit_chat.R
import com.example.securechit_chat.databinding.ActivityChatLogBinding
import com.example.securechit_chat.models.ChatMessage
import com.example.securechit_chat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatLogActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding : ActivityChatLogBinding

    // adapter for recyclerView
     val adapter = GroupAdapter<ViewHolder>()
     var toUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.chatRecyclerView.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewUsersChat.USER_KEY)
        // setting userName with whom chat is going on
        if(toUser != null) {
            binding.textPerson.text = toUser!!.name

        }

             // updates and get the new messages at realTime
          listenForMessages()

        // send button for sending messages
         binding.sendBtn.setOnClickListener {

             performSendMessage()
         }


        // for back button
        binding.imageBack.setOnClickListener {
            val intent  =  Intent(this , MessagesList::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")


        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

              val chatMessage =   snapshot.getValue(ChatMessage::class.java)

                if(chatMessage != null){

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){

                        adapter.add(ChatToItem(chatMessage.text))

                    }else{
                        adapter.add(ChatFromItem(chatMessage.text , toUser!!))
                    }


                }

                     binding.chatRecyclerView.scrollToPosition(adapter.itemCount-1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    // storing messages to fireBase
    private fun performSendMessage(){
        val text = binding.inputMessage.text.toString() // text entered by user

         if(TextUtils.isEmpty(text)){
             return
         }

          // id of user sending message
         val fromId = FirebaseAuth.getInstance().uid
         // user object based from user selected to message
         val user = intent.getParcelableExtra<User>(NewUsersChat.USER_KEY)
         val toId = user?.uid

         if(fromId == null) return
         if(toId == null) return
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val to_Ref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

         val data_to_Store = ChatMessage(ref.key!! ,text , fromId , toId , System.currentTimeMillis() / 1000 )

         ref.setValue(data_to_Store)
             .addOnCompleteListener {
                 binding.inputMessage.text.clear()
                 binding.chatRecyclerView.scrollToPosition(adapter.itemCount - 1)
             }


        to_Ref.setValue(data_to_Store)

           val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        latestMessageRef.setValue(data_to_Store)

        val latestMessageRefToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        latestMessageRefToRef.setValue(data_to_Store)
    }

}




// for recieving messages
class ChatFromItem(val text: String , val user : User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
         viewHolder.itemView.findViewById<TextView>(R.id.textMessageRecieved).text = text
        val uri = user.profilePic
        val imageView = viewHolder.itemView.findViewById<ImageView>(R.id.profilePicReciver)
        Picasso.get().load(uri).into(imageView)
    }

    override fun getLayout(): Int {
       return R.layout.item_container_recieved_message
    }
}

// for sending messages
class ChatToItem(val text : String ): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textMessage).text = text

    }

    override fun getLayout(): Int {
       return R.layout.item_container_sent_message
    }
}