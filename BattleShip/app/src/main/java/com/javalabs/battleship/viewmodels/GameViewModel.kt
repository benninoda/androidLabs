package com.javalabs.battleship.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javalabs.battleship.R
import com.javalabs.battleship.SECOND_IN_MILLIS
import com.javalabs.battleship.battle_field.BattleField
import com.javalabs.battleship.battle_field.Coordinate
import com.javalabs.battleship.logic.ShotManager
import com.javalabs.battleship.models.Player
import kotlinx.coroutines.*

class GameViewModel : ViewModel() , IGetViewModel{

    private lateinit var activePlayer: Player
    private var _selectedByPersonCoordinate = MutableLiveData<Coordinate>()
    val selectedByPersonCoordinate: LiveData<Coordinate>
        get() = _selectedByPersonCoordinate
    private var _selectedByComputerCoordinate = MutableLiveData<Coordinate>()
    val selectedByComputerCoordinate: LiveData<Coordinate>
        get() = _selectedByComputerCoordinate
    private var _status = MutableLiveData<Int>()
    val status: LiveData<Int>
        get() = _status
    var shareId = MutableLiveData<String>()
    private var _personShips = MutableLiveData<ArrayList<Coordinate>>()
    val personShips: LiveData<ArrayList<Coordinate>>
        get() = _personShips
    private var _personFailShots = MutableLiveData<ArrayList<Coordinate>>()
    val personFailedShots: LiveData<ArrayList<Coordinate>>
        get() = _personFailShots
    private var _personSuccessfulShots = MutableLiveData<ArrayList<Coordinate>>()
    val personSuccessfulShots: LiveData<ArrayList<Coordinate>>
        get() = _personSuccessfulShots
    private var _computerFailShots = MutableLiveData<ArrayList<Coordinate>>()
    val computerFailedShots: LiveData<ArrayList<Coordinate>>
        get() = _computerFailShots
    private var _computerSuccessfulShots = MutableLiveData<ArrayList<Coordinate>>()
    val computerSuccessfulShots: LiveData<ArrayList<Coordinate>>
        get() = _computerSuccessfulShots
    private var _startGameEvent = MutableLiveData<Boolean>()
    val startGameEvent: LiveData<Boolean>
        get() = _startGameEvent
    private var _endGameEvent = MutableLiveData<Boolean>()
    val endGameEvent: LiveData<Boolean>
        get() = _endGameEvent
    private var viewModelJob = Job()
    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var personBattleField: BattleField
    private lateinit var opponentBattleField: BattleField
    private lateinit var game: HashMap<String, Any>
//    private lateinit var shotManager: ShotManager

    init {
        initValues()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun createGame(){

    }

    private fun initValues() {
        activePlayer = Player.FIRST
        game = hashMapOf(
            "firstPlayerId" to Firebase.auth.currentUser!!.uid,
            "startGameEvent" to false,
            "endGameEvent" to false)
        Firebase.firestore.collection("games").add(game)
            .addOnSuccessListener { documentReference ->
                run {
                    shareId.postValue(documentReference.id)
                }
            }
            .addOnFailureListener { e ->
                Log.w("lll", "Error adding document", e)}

//        shotManager = ShotManager()
        personBattleField = BattleField()
        opponentBattleField = BattleField()
        _status.value = R.string.status_welcome_text
        _startGameEvent.value = false
        _endGameEvent.value = false
        _selectedByPersonCoordinate.value = null
    }

    fun startGame() {
        _startGameEvent.value = true
        playAsPerson()
    }

    fun generateShips() {
        personBattleField.initBattleShip()
        personBattleField.randomizeShips()
        _personShips.value = personBattleField.getShipsCoordinates()
        _status.value = R.string.status_generate_or_start_text
    }

    fun startNewGame() {
        initValues()
        _personShips.value = ArrayList()
        _personFailShots.value = ArrayList()
        _personSuccessfulShots.value = ArrayList()
        _computerFailShots.value = ArrayList()
        _computerSuccessfulShots.value = ArrayList()
    }

    override fun handleOpponentAreaClick(coordinate: Coordinate) {
        if (activePlayer == Player.SECOND) {
            Log.e("D", "in opponent area click 2 players")
            if (opponentBattleField.isCellFreeToBeSelected(coordinate)) {
                _selectedByPersonCoordinate.value = coordinate
            }
        }
    }

    private fun playAsPerson() {
        activePlayer = Player.FIRST
        if (_status.value != R.string.status_shot_ship_again_text) {
            _status.value = R.string.status_select_to_fire_text
        }
    }

    fun makeFireAsPerson() {
        if (activePlayer == Player.FIRST) {
            val isShipHit = opponentBattleField.handleShot(_selectedByPersonCoordinate.value)
            _selectedByPersonCoordinate.value = null
            if (isShipHit) {
                _status.value = R.string.status_shot_ship_again_text
                _personSuccessfulShots.value = opponentBattleField.getCrossesCoordinates()
                if (opponentBattleField.isGameOver()) {
                    endGame(true)
                } else {
                    playAsPerson()
                }
            } else {
                _personFailShots.value = opponentBattleField.getDotsCoordinates()
                _status.value = R.string.status_opponent_shot_text
                activePlayer = Player.SECOND
//                Firebase.firestore.
//                playAsComputer()
            }
        }
    }

//    private fun playAsComputer() {
//        val coordinate: Coordinate = shotManager.getCoordinateToShot()
//        _selectedByComputerCoordinate.value = coordinate
//        val isShipHit = personBattleField.handleShot(coordinate)
//        shotManager.handleShot(isShipHit)
//        if (isShipHit) {
//            uiScope.launch {
//                delay(SECOND_IN_MILLIS)
//                _computerSuccessfulShots.value = personBattleField.getCrossesCoordinates()
//                if (personBattleField.isGameOver()) {
//                    endGame(false)
//                } else {
//                    _status.value = R.string.status_opponent_shot_again_text
//                    checkCurrentPlayer()
//                }
//            }
//        } else {
//            uiScope.launch {
//                delay(SECOND_IN_MILLIS + SECOND_IN_MILLIS / 2)
//                _computerFailShots.value = personBattleField.getDotsCoordinates()
//                activePlayer = Player.PERSON
//                checkCurrentPlayer()
//                _status.value = R.string.status_select_to_fire_text
//            }
//        }
//    }

//    private fun checkCurrentPlayer() {
//        if (activePlayer == Player.PERSON) {
//            playAsPerson()
//        } else {
//            playAsComputer()
//        }
//    }

    private fun endGame(isFirstPersonWon: Boolean) {
        activePlayer = Player.NONE
        _endGameEvent.value = true
        if (isFirstPersonWon) {
            _status.value = R.string.status_game_over_you_win_text
        } else {
            _status.value = R.string.status_game_over_you_lose_text
        }
    }
}