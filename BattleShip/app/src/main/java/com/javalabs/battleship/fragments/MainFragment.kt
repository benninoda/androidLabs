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

        welcomeTextView.text = "Добро пожаловать, " + user?.displayName + "!"
        btnToProfile.setOnClickListener{
            Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_profileFragment)
        }

        btnCreateGame.setOnClickListener{

            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Game ID")

            val dialogLayout = inflater.inflate(layout.dialog_create_game, null)
            val editText  = dialogLayout.findViewById<TextView>(R.id.dialog_content_text_view)
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

            val game = hashMapOf(
                "firstPlayerId" to Firebase.auth.currentUser!!.uid)
            viewModel.setPlayer(Player.FIRST, Firebase.auth.currentUser!!.uid)
            Firebase.firestore.collection("games").add(game)
                .addOnSuccessListener { documentReference ->
                    run {
                        viewModel.shareId.postValue(documentReference.id)
                        viewModel.docRef.postValue(documentReference)
                    }
                }
                .addOnFailureListener{ e ->
                    Log.w("lll", "Error adding document", e)}


            viewModel.shareId.observe(viewLifecycleOwner, Observer {
                editText.text = viewModel.shareId.value
                val docRef = Firebase.firestore.collection("games")
                    .document(viewModel.shareId.value!!)

                docRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("listener", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Log.d("TAG", "Current data: ${snapshot.data}")
                    } else {
                        Log.e( "listener", "Current data: null")
                    }
                }
            })

            viewModel.docRef.observe(viewLifecycleOwner, Observer{

            })




            builder.setView(dialogLayout)
            Log.e("D", (editText == null).toString())
            builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
                Toast.makeText(context,
                    android.R.string.yes, Toast.LENGTH_SHORT).show()
                Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_createOnlineBattlefieldFragment)
            }

            builder.setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                Toast.makeText(context, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show()
                game_id = editText.text.toString()
            }

            builder.show()
        }

        btnPlayWithBot.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_mainFragment_to_createBattlefieldFragment)
        }

        btnJoinGame.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Game ID")
            val dialogLayout = inflater.inflate(layout.dialog_id, null)
            val editText  = dialogLayout.findViewById<EditText>(R.id.dialog_content)
            builder.setView(dialogLayout)
            Log.e("D", (editText == null).toString())
            builder.setPositiveButton(R.string.dialog_ok) { dialog, which ->
                viewModel.setPlayer(Player.SECOND, Firebase.auth.currentUser!!.uid)
                Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_createOnlineBattlefieldFragment)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, which ->
                Toast.makeText(context, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show()
                game_id = editText.text.toString()
            }
            builder.show()
        }

    }

}