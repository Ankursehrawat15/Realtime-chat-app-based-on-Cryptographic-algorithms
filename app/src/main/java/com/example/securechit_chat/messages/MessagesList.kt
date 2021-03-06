package com.example.securechit_chat.messages

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.securechit_chat.Aes
import com.example.securechit_chat.R
import com.example.securechit_chat.registerLogin.SignIn
import com.example.securechit_chat.databinding.ActivityMessagesListBinding
import com.example.securechit_chat.models.ChatMessage
import com.example.securechit_chat.models.User
import com.example.securechit_chat.registerLogin.SignUp
import com.example.securechit_chat.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder


// To do
// users image loading using sharedprefrences
// currentUser name
class MessagesList : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
    }

    private val adapter = GroupAdapter<ViewHolder>()

    // view binding
    private lateinit var binding : ActivityMessagesListBinding
    private lateinit var auth : FirebaseAuth






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesListBinding.inflate(layoutInflater)
        setContentView(binding.root)



        auth = FirebaseAuth.getInstance()
        loadCurrentUserData()
        binding.currentChatRV.adapter = adapter
        binding.currentChatRV.addItemDecoration(DividerItemDecoration(this , DividerItemDecoration.VERTICAL))


        // set item click listner on your adapter
        adapter.setOnItemClickListener { item, view ->

            val intent = Intent(this , ChatLogActivity::class.java)

            val row = item as LatestMessageRow
            intent.putExtra(NewUsersChat.USER_KEY,row.chatPartnerUser )
            startActivity(intent)
        }




        ListenForLatestMessages()



        // for fetching list with whom chat history is there or currently chatting
               fetchCurrentUser()


        // signout from app function
        binding.signOut.setOnClickListener {
            ShowAlertDialog().show()
        }

        // start chat with new User
        binding.newUserMessage.setOnClickListener {
            val intent = Intent(this , NewUsersChat::class.java)
            startActivity(intent)

        }




    }

    private fun loadCurrentUserData() {
        val decrypter : Aes = Aes()
        val sharedPrefrences : SharedPreferences = getSharedPreferences("Meta-Deta" , Context.MODE_PRIVATE )
        var userName : String? = sharedPrefrences.getString("userName:${auth.currentUser?.uid}" , null)
        var userImageUrl : String? = sharedPrefrences.getString("imageUrl:${auth.currentUser?.uid}" , null)
        if(userImageUrl != null) {
            userImageUrl = decrypter.decrypt(userImageUrl, SignUp.key)
        }


        if(userName != null){
           userName =  decrypter.decrypt(userName , SignUp.key)
            binding.textView.text = userName
        }


        Picasso.get().load(userImageUrl).into(binding.profilePic)
    }

    val latestMessagesMap = HashMap<String , ChatMessage>()

    private fun ListenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }




    }







    // fetching the list user with whom chat has been done or going on
    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun ShowAlertDialog() : AlertDialog.Builder {
        val logoutDialog = AlertDialog.Builder(this).setTitle("Logout").setMessage("Do you want to logout ?")
            .setIcon(R.drawable.ic_securechitchat)
            .setPositiveButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()

            }.setNegativeButton("Yes") {  _, i ->
                auth.signOut()
                val intent : Intent = Intent(this, SignIn::class.java)
                startActivity(intent)
                finish()

            }

        return logoutDialog
    }


}