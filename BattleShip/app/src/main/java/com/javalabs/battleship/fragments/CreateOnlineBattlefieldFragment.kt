package com.javalabs.battleship.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javalabs.battleship.R
import com.javalabs.battleship.databinding.FragmentCreateOnlineBattlefieldBinding
import com.javalabs.battleship.models.GameState
import com.javalabs.battleship.models.Player
import com.javalabs.battleship.viewmodels.GameViewModel
import com.javalabs.battleship.views.OpponentFieldView

class CreateOnlineBattlefieldFragment : Fragment() {

        lateinit var viewModel: GameViewModel
        private lateinit var binding: FragmentCreateOnlineBattlefieldBinding
        private val customOnTouchListener = View.OnTouchListener(implementCustomTouchListener())
        private lateinit var gameState : GameState
        private var isCurrentPlayerReady: Boolean = false
        private lateinit var gameId : String

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_create_online_battlefield, container, false
            )

            Log.e("n", "\n")
            Log.e("n", "\n")
            Log.e("n", "\n")
            Log.e("n", "\n")

            Log.e("ArgsL", arguments.toString())

            viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
            if (arguments != null) {
                viewModel.shareId.value = arguments?.getString("gameeId");

                var gameIdOrNull = arguments?.getString("gameeId")
                Log.e("Args222=", gameIdOrNull.toString())
                if (gameIdOrNull != null) {

                    gameId = gameIdOrNull
                    Log.e("Game Id log", " Set game id; gameId=" + gameId)
                }

                if(arguments?.getBoolean("playerNumber")!!)
                    viewModel.setPlayer(Player.FIRST, Firebase.auth.currentUser!!.uid)
                else
                    viewModel.setPlayer(Player.SECOND, Firebase.auth.currentUser!!.uid)
            }

            binding.gameViewModel = viewModel

            return binding.root
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            super.onViewCreated(view, savedInstanceState)
            Log.e("Listener", "here shared " + gameId)
            gameState = GameState.GENERATE_STEP

            val docRef = Firebase.firestore.collection("games")
                .document(gameId)
            docRef.set(hashMapOf(
                "GameIsStarted" to true
            ))

            docRef.addSnapshotListener { snapshot, e ->

                if (e != null) {
                }

                Log.e("Listener", "Snap text=" + snapshot.toString())

                if (snapshot != null) {

                    Log.e("GameState=", gameState.toString())

                    when (gameState) {

                        GameState.GENERATE_STEP -> {
                            if (snapshot["FirstPlayerReady"] != null && snapshot["SecondPlayerReady"] != null) {
                                gameState = GameState.TURN_FIRST
                                viewModel._startGameEvent.value = true
                                viewModel.activePlayer = Player.FIRST

                                viewModel.playAsPerson()

                                Log.e("Gave event", "Set start game event as true")
                            }
                        }
                        GameState.TURN_FIRST -> {
                            // get x, y and make shot; switch game state;
                        }
                        GameState.TURN_SECOND -> {
                            // get x, y and make show; switch gamestate;
                        }
                    }


                } else {
                    Log.e("listener", "Current data: null")
                }
            }

//        val userFieldView: UserFieldView = view.findViewById(R.id.viewPerson)
//        userFieldView.provideViewModel(viewModel)

            val opponentFieldView: OpponentFieldView = view.findViewById(R.id.viewComputer)
            opponentFieldView.provideViewModel(viewModel)

            viewModel.status.observe(viewLifecycleOwner) { newStatusId ->
                binding.tvStatus.text = resources.getText(newStatusId)
            }

            viewModel.selectedByPersonCoordinate.observe(viewLifecycleOwner) { point ->
                binding.viewComputer.getSelectedCoordinate(point)
                binding.viewFire.visibility = if (point == null) View.INVISIBLE else View.VISIBLE
            }

            viewModel.selectedByComputerCoordinate.observe(viewLifecycleOwner) {
                binding.progressBar.visibility = View.VISIBLE
            }

            viewModel.personShips.observe(viewLifecycleOwner) { coordinates ->
                binding.viewPerson.getShipsCoordinates(coordinates)
                if (coordinates.isNotEmpty()) {
                    binding.viewStart.visibility = View.VISIBLE
                }
            }

            viewModel.personSuccessfulShots.observe(viewLifecycleOwner) { coordinates ->
                binding.viewComputer.getCrossesCoordinates(coordinates)
            }

            viewModel.personFailedShots.observe(viewLifecycleOwner) { coordinates ->
                binding.viewComputer.getDotsCoordinates(coordinates)
            }

            viewModel.computerSuccessfulShots.observe(viewLifecycleOwner) { coordinates ->
                binding.viewPerson.getCrossesCoordinates(coordinates)
                binding.progressBar.visibility = View.INVISIBLE
            }

            viewModel.computerFailedShots.observe(viewLifecycleOwner) { coordinates ->
                binding.viewPerson.getDotsCoordinates(coordinates)
                binding.progressBar.visibility = View.INVISIBLE
            }

            viewModel.startGameEvent.observe(viewLifecycleOwner) { isStarted ->
                if (isStarted) binding.viewStart.visibility = View.GONE
                if (!isStarted) binding.viewNewGame.visibility = View.INVISIBLE
                binding.viewGenerate.visibility = if (isStarted) View.INVISIBLE else View.VISIBLE
            }

            viewModel.endGameEvent.observe(viewLifecycleOwner) { isEnded ->
                binding.viewNewGame.visibility = if (isEnded) View.VISIBLE else View.INVISIBLE
            }

            binding.viewGenerate.setOnTouchListener(customOnTouchListener)
            binding.viewFire.setOnTouchListener(customOnTouchListener)
            binding.viewStart.setOnTouchListener(customOnTouchListener)
            binding.viewNewGame.setOnTouchListener(customOnTouchListener)
        }

        private fun implementCustomTouchListener(): (View, MotionEvent) -> Boolean {
            return { v: View, event: MotionEvent ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.colorAccent
                            )
                        )
                        setTextColor(v, R.color.colorPrimary)
                    }
                    MotionEvent.ACTION_UP -> {
                        v.background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.square_background
                        )
                        setTextColor(v, R.color.colorPrimaryDark)
                        v.performClick()
                    }
                }
                true
            }
        }

        private fun setTextColor(v: View, color: Int) {
            if (v is TextView) v.setTextColor(
                ContextCompat.getColor(requireContext(), color)
            )
        }

}