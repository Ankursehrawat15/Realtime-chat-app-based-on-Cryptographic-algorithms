package com.example.securechit_chat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.securechit_chat.R
import com.example.securechit_chat.databinding.ActivityMessagesListBinding
import com.example.securechit_chat.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class MessagesList : AppCompatActivity() {

    // view binding
    private lateinit var binding : ActivityMessagesListBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // object of auth
        auth = FirebaseAuth.getInstance()





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

    private fun ShowAlertDialog() : AlertDialog.Builder {
        val logoutDialog = AlertDialog.Builder(this).setTitle("Logout").setMessage("Do you want to logout ?")
            .setIcon(R.drawable.ic_securechitchat)
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                val intent : Intent = Intent(this, SignIn::class.java)
                startActivity(intent)
                finish()

            }.setNegativeButton("No") { dialogInterface , i ->

                dialogInterface.cancel()
            }

        return logoutDialog
    }


}