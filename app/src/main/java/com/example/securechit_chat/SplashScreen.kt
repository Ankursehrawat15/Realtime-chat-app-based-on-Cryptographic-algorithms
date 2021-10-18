package com.example.securechit_chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.securechit_chat.messages.MessagesList
import com.example.securechit_chat.registerLogin.SignIn
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        // for checking is user logged in or not
        verifyUserLogedIn()


    }

    // for checking is user logged in or not
    private fun verifyUserLogedIn() {
        if (auth.currentUser?.uid == null){
            val intent = Intent(this , SignIn::class.java)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this , MessagesList::class.java)
            startActivity(intent)
            finish()
        }
    }
}