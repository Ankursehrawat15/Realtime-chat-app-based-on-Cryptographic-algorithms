package com.example.securechit_chat.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.securechit_chat.R
import com.example.securechit_chat.models.User
import com.example.securechit_chat.databinding.ActivityNewUsersChatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class NewUsersChat : AppCompatActivity() {

    private lateinit var binding : ActivityNewUsersChatBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUsersChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // loading bar
       val loading  = binding.loadingNewUserPB


           // fetch users list
          fetchUsers(loading)

        // back button (to move to previous activity)
        binding.imageBack.setOnClickListener {
            val intent =  Intent(this , MessagesList::class.java)
            startActivity(intent)
            finish()
        }

    }

    companion object{
        val USER_KEY  = "USER_KEY"
    }
        // gets the list of users for starting the new messages
    private fun fetchUsers(load : ProgressBar) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                // need to decrypt in here first
                 val adapter = GroupAdapter<ViewHolder>()

                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if(user != null){
                        adapter.add(UserList(user))
                    }

                }


                // adapter listner gives us view and item
                adapter.setOnItemClickListener { item, view ->

                      val userItem = item as UserList
                        val intent = Intent(view.context , ChatLogActivity::class.java)
                         intent.putExtra(USER_KEY , userItem.user)
                        startActivity(intent)
                        finish()

                }

                 load.visibility = View.GONE
                binding.newUserRV.adapter = adapter  // setting up the adapter
                binding.newUserRV.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }
}
    // this class binds with the item loads image from web using picasso
class UserList(val user: User): Item<ViewHolder>(){

    // will be called in our list for each user object later on...
    override fun bind(viewHolder: ViewHolder, position: Int) {
         viewHolder.itemView.findViewById<TextView>(R.id.userNameTV).text = user.name
        viewHolder.itemView.findViewById<TextView>(R.id.usersEmail).text = user.email

        Picasso.get().load(user.profilePic).into(viewHolder.itemView.findViewById<ImageView>(R.id.profilePicIV))
    }

    override fun getLayout(): Int {
        return R.layout.user_list_item

    }
}