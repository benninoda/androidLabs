package com.javalabs.battleship.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javalabs.battleship.R
import com.javalabs.battleship.R.layout
import com.javalabs.battleship.models.Player
import com.javalabs.battleship.viewmodels.GameViewModel


class MainFragment : Fragment() {

    private lateinit var btnCreateGame: TextView
    private lateinit var btnJoinGame: TextView
    private lateinit var btnToProfile: TextView
    private lateinit var btnPlayWithBot: TextView
    private lateinit var game_id: String
    private lateinit var welcomeTextView: TextView
    private lateinit var viewModel: GameViewModel
    private var isNewInstance: Boolean = false
    private lateinit var subscriber: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        return inflater.inflate(layout.fragment_main, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = Firebase.auth.currentUser
        btnCreateGame = view.findViewById(R.id.create_game_button);
        btnJoinGame = view.findViewById(R.id.join_game_button);
        btnToProfile = view.findViewById(R.id.to_profile_button);
        btnPlayWithBot = view.findViewById(R.id.bot_game_button)
        welcomeTextView = view.findViewById(R.id.welcome)
        isNewInstance = true

        if (viewModel.shareId.value != null && isNewInstance == true) {
            viewModel.shareId.value = null;
            isNewInstance = false
        }

        Log.e("D", "ON VIEW CREATED!!!")
        Log.e("D", "Current value=" + viewModel.shareId.value.toString())

        welcomeTextView.text = "Добро пожаловать, " + user?.displayName + "!"

        btnToProfile.setOnClickListener{
            Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_profileFragment)
        }

        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        builder.setTitle("Game ID")


        val dialogLayout = inflater.inflate(layout.dialog_create_game, null)
        (dialogLayout.parent as? ViewGroup)?.removeView(dialogLayout)
        val editText  = dialogLayout.findViewById<TextView>(R.id.dialog_content_text_view)

        editText.text = null;

        builder.setView(dialogLayout)
        Log.e("D", (editText == null).toString())
        val copyId = dialogLayout.findViewById<ImageButton>(R.id.copy_in_dialog_create_game)
        copyId.setOnClickListener{
            val clipboard: ClipboardManager =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "gameId",
                editText.text
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, "Скопировано", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(R.string.dialog_cancel) { dialog, which ->
            Toast.makeText(context, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show()
            game_id = editText.text.toString()
            subscriber.remove()
        }

        val alert = builder.create()

        btnCreateGame.setOnClickListener{

            val game = hashMapOf(
                "firstPlayerId" to Firebase.auth.currentUser!!.uid)
            Firebase.firestore.collection("games").add(game)
                .addOnSuccessListener { documentReference ->
                    run {
//                        viewModel.shareId.postValue("2") 2 for debug
                        viewModel.shareId.postValue(documentReference.id)
                        viewModel.docRef.postValue(documentReference)
                    }
                }
                .addOnFailureListener{ e ->
                    Log.e("lll", "Error adding document", e)}


            (dialogLayout.parent as? ViewGroup)?.removeView(dialogLayout)

            alert.show()
        }

        viewModel.shareId.observe(viewLifecycleOwner, Observer {
            if (editText != null)
                editText.text = viewModel.shareId.value

            if (viewModel.shareId.value != null) {

                val docRef = Firebase.firestore.collection("games")
                    .document(viewModel.shareId.value!!)

                Log.e("listener", "Add new event listener on onCreated")
                 subscriber = docRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                    }

                    Log.e("Listener", "Snap text=" + snapshot.toString())
                    Log.e("Listener", "View model id value=" + viewModel.shareId.value)

                    if (snapshot != null && snapshot["isConnected"] != null ) {
                        Log.e("listener", "vm " + viewModel.shareId.value!!)
                        Log.e("listener", "snap " + snapshot["gameId"].toString())
                        alert.dismiss()
                        val bundle = Bundle()
                        bundle.putCharSequence("gameeId", viewModel.shareId.value)
                        bundle.putBoolean("playerNumber", true)

                        Navigation.findNavController(view).navigate(
                            R.id.action_mainFragment_to_createOnlineBattlefieldFragment,
                            bundle)
                    } else {
                        Log.e("listener", "Current data: null")
                    }
                }


            }
        })


        btnPlayWithBot.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_createBattlefieldFragment)
        }

        btnJoinGame.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Game ID")
            val dialogLayout = inflater.inflate(layout.dialog_id, null, false)
            val editText1  = dialogLayout.findViewById<EditText>(R.id.dialog_content)
            (dialogLayout.parent as? ViewGroup)?.removeView(dialogLayout)
            builder.setView(dialogLayout)
            Log.e("D", (editText1 == null).toString())

            builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->

                val TAG: String = "lol"
                val docRef = Firebase.firestore.collection("games")
                    .document(editText1.text.toString())
                Log.e(TAG, "edit tetx: ${editText1.text.toString()}")

                docRef.get().addOnSuccessListener { document ->
                        if (document.data != null) {
                            Log.e(TAG, "DocumentSnapshot data: ${document.data}")
                            docRef.set(hashMapOf(
                                "isConnected" to true,
                                "gameId" to editText1.text.toString()
                            ))
                            Log.e("kek", "BEFORE NAVIGATE; GAME_ID=" + viewModel.shareId.value.toString())
                            val bundle = Bundle()
                            bundle.putCharSequence("gameeId", editText1.text.toString())
                            bundle.putBoolean("playerNumber", false)
                            Navigation.findNavController(view).navigate(
                                R.id.action_mainFragment_to_createOnlineBattlefieldFragment,
                                bundle
                            )

                        } else {
                            Log.e(TAG, "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "get failed with ", exception)
                    }

                (dialogLayout.parent as? ViewGroup)?.removeView(dialogLayout)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                Toast.makeText(context, "EditText is " + editText1.text.toString(), Toast.LENGTH_SHORT).show()
                game_id = editText1.text.toString()
            }
            builder.show()
        }

    }

}