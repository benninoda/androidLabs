package com.javalabs.battleship.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.javalabs.battleship.R
import com.javalabs.battleship.R.layout
import com.javalabs.battleship.viewmodels.GameViewModel
import com.javalabs.battleship.viewmodels.UserViewModel


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
            Log.e("D", "btn to profile click")
            Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_profileFragment)
        }

        btnCreateGame.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Game ID")
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            val dialogLayout = inflater.inflate(layout.dialog_create_game, null)
            val editText  = dialogLayout.findViewById<TextView>(R.id.dialog_content_text_view)
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
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            val dialogLayout = inflater.inflate(layout.dialog_id, null)
            val editText  = dialogLayout.findViewById<EditText>(R.id.dialog_content)
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

    }

    companion object {

        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {

            }
    }

}