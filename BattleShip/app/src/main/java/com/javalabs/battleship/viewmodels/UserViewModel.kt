package com.javalabs.battleship.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {

    var imageUri = MutableLiveData<Uri>()
    var nickname = MutableLiveData<String>()

    init {
        initValues()
    }

    private fun initValues(){
        imageUri.value = null
        nickname.value = null
    }
}