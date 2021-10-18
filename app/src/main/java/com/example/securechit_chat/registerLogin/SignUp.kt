package com.example.securechit_chat.registerLogin

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import com.example.securechit_chat.R
import com.example.securechit_chat.databinding.ActivitySignUpBinding
import com.example.securechit_chat.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class SignUp : AppCompatActivity() {

    // viewBinding class
    private lateinit var binding: ActivitySignUpBinding
    private  val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var progressDialog : Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // switching to signIn
        binding.existingAccount.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }
         // loading animation
        progressDialog = Dialog(this)
        progressDialog.setContentView(R.layout.custom_progress_dialog)


        // sign up button onClickListner
        binding.buttonSignUp.setOnClickListener {

            val email = binding.inputEmailSp.text.toString()
            val password = binding.inputPasswordSP.text.toString()
            val confirmPassword = binding.inputConfirmPasswordSp.text.toString()
            val userName = binding.inputNameSp.text.toString()

            // checking whether the field are empty or not
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(userName)
            ) {
                showSnackBar("Fields cannot be empty")
                return@setOnClickListener
            }

            // checking the length of password
            if (password.length < 8) {
                showSnackBar("Password should have atLeast 7 characters")
                return@setOnClickListener
            }

            // matching the password and confirming the same
            if (password != confirmPassword) {
                showSnackBar("oops password did not match")
                return@setOnClickListener
            }


            // creating account
            register(email, password)


        }

        // geting the image from device
        binding.AddImage.setOnClickListener {
              val intent = Intent(Intent.ACTION_PICK)
               intent.type = "image/*"

            startActivityForResult(intent,0)

        }




    }

    // for image selection from device
     private var selectedPhoto: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            selectedPhoto = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhoto)
            binding.profilePic.setImageBitmap(bitmap)
            binding.AddImage.alpha = 0f
        }
    }

    // for sign up
    private fun register(Email: String, Password: String) {
        auth.createUserWithEmailAndPassword(Email, Password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.show()
                    uploadImageToFirebase()

                } else {
                    progressDialog.dismiss()
                    val msg: String? = task.exception?.message
                    if (msg != null) {
                        showSnackBar(msg)
                    } else {
                        showSnackBar("Some problem occurred while registering user. Try Again")
                    }
                }
            }
    }
      // uploading the image to storage on firebase
    private fun uploadImageToFirebase() {
       val filename = UUID.randomUUID().toString()
      val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhoto!!)
            .addOnSuccessListener {
               // for downloading users profile location which can be stored on realtime database
                ref.downloadUrl.addOnSuccessListener {

                   // saving the users meta deta at backend
                    // passed image url
                   saveUserInfoFireBase(it.toString())


               }



            }
            .addOnFailureListener{
                progressDialog.dismiss()
                showSnackBar( "Some problem occurred while saving image")
            }

    }


    // saving the users meta deta at backend
    private fun saveUserInfoFireBase(profileImageUrl : String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
      val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
           val name = binding.inputNameSp.text.toString()
           val email = binding.inputEmailSp.text.toString()

           val user = User(name, profileImageUrl , binding.inputPasswordSP.text.toString(), email , uid)
           ref.setValue(user)
               .addOnSuccessListener {
                   progressDialog.dismiss()
                   emailVerification()

               }
               .addOnFailureListener{
                   progressDialog.dismiss()
                   showSnackBar( "Some problem occurred while saving users information")
               }



    }

    private fun emailVerification() {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this , "Verification Link is Sent to Email", Toast.LENGTH_LONG).show()
                val intent = Intent(this ,SignIn::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this , "Not found any Email Address" + it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // function for snackBar
    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(binding.signUpLayout, message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction("okay") {}
        snackBar.show()
    }


}


