package com.example.securechit_chat.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val name:String , val profilePic : String , val Password: String , val email: String , val uid :String) : Parcelable{

    constructor() : this("","","","","")
}