package com.javalabs.battleship.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.javalabs.battleship.R
import com.javalabs.battleship.viewmodels.MainViewModel
import com.javalabs.battleship.viewmodels.UserViewModel
import com.squareup.picasso.Picasso
import java.io.InputStream
import java.time.LocalDateTime

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nicknameTextView: TextView
    private lateinit var btnChangeNickname: ImageButton
    private lateinit var btnSave: ImageView
    private lateinit var imageUri: Uri
    lateinit var viewModel: UserViewModel

    private val PICK_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("ShowToast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = Firebase.auth.currentUser
        btnChangeNickname = view.findViewById(R.id.change_nickname)
        profileImageView = view.findViewById(R.id.imgProfilePic)
        nicknameTextView = view.findViewById(R.id.nickname_text_view)
        btnSave = view.findViewById(R.id.imgFloating)

        nicknameTextView.text = user?.displayName
        viewModel.nickname.value = user?.displayName
        viewModel.imageUri.value = user?.photoUrl

        Log.e("D", viewModel.imageUri.value.toString())
        Log.e("D", viewModel.nickname.value.toString())
        if (user?.photoUrl != null) {
            Picasso.with(context)
                .load(user.photoUrl)
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.ic_launcher)
        }

        viewModel.imageUri.observe(viewLifecycleOwner, Observer {
            val profileUpdates = userProfileChangeRequest {
                displayName = nicknameTextView.text.toString()
                photoUri = viewModel.imageUri.value
            }
            Log.e("D", "in observe")

            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "All right",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
        })

        btnChangeNickname.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater

            builder.setTitle("Измените никнейм")
            val dialogLayout = inflater.inflate(R.layout.dialog_change_nickname, null)
            val editText = dialogLayout.findViewById<TextView>(R.id.dialog_change_nickname_content)
            val filter =
                InputFilter { source, start, end, dest, dstart, dend ->
                    for (i in start until end) {
                        if (!Character.isLetterOrDigit(source[i])) {
                            return@InputFilter ""
                        }
                    }
                    null
                }
            editText.filters = arrayOf(filter)
            builder.setView(dialogLayout)
            Log.e("D", (editText == null).toString())
            builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
                if (editText.text.isNotEmpty()) {
                    nicknameTextView.text = editText.text
                } else {
                    Toast.makeText(
                        context,
                        "Никнейм не может быть пустой строкой",
                        Toast.LENGTH_SHORT
                    )
                }
            }

            builder.setNegativeButton(R.string.dialog_cancel) { dialog, which ->
            }

            builder.show()
        }

        btnSave.setOnClickListener {

            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(imageUri)
            val avatarRef =
                Firebase.storage.reference.child("avatars/${java.util.Calendar.getInstance().time}")
            val uploadTask = avatarRef.putStream(inputStream!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                avatarRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModel.imageUri.postValue(task.result)
                }
            }
        }

        profileImageView.setOnClickListener {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"

            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            pickIntent.type = "image/*"

            val chooserIntent = Intent.createChooser(getIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            startActivityForResult(getIntent, PICK_IMAGE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            if (data != null) {
                profileImageView.setImageURI(data.data!!)
                imageUri = data.data!!
            }
        }


    }
}