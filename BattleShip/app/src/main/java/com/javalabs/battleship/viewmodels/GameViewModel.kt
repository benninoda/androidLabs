package com.javalabs.battleship.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.javalabs.battleship.R
import com.javalabs.battleship.battle_field.BattleField
import com.javalabs.battleship.battle_field.Coordinate
import com.javalabs.battleship.logic.ShotManager
import com.javalabs.battleship.models.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
//import org.junit.runner.Request.method
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap



class GameViewModel : ViewModel() , IGetViewModel{
    private val TAG: String = "GameViewModel"

    private lateinit var playerId: String
    private lateinit var currentPlayer: Player
    lateinit var activePlayer: Player
    private  lateinit var client: CollectionReference
    private var isCurrentUserReady: Boolean = false


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
    var docRef = MutableLiveData<DocumentReference>()
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
    var _startGameEvent = MutableLiveData<Boolean>()
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
    private lateinit var gameQueue: Queue<Any>
    private lateinit var shotManager: ShotManager
//    private lateinit var shotManager: ShotManager

    init {
        initValues()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    fun setPlayer(player: Player, uid: String){
        Log.e("e", "IN SET PLAYER; PLAYER=" + player)
        currentPlayer = player
        playerId = uid
    }

    private fun initValues() {
//        playerId = Firebase.auth.currentUser!!.uid
        activePlayer = Player.NONE
        currentPlayer = Player.NONE

        shotManager = ShotManager()

        personBattleField = BattleField()
        opponentBattleField = BattleField()
        _status.value = R.string.status_welcome_text
        _startGameEvent.value = false
        _endGameEvent.value = false
        _selectedByPersonCoordinate.value = null
    }


//    fun produce(key: String, value: Any) {
//        this.client.document(shareId.value!!).set();
//
//         val event = hashMapOf<String, Any>(
//           "type" to playerConnected(),
//           "params" to '1'
//         );
//    }
    
//    fun initConsumer(){
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val post = dataSnapshot.value
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
//            }
//        }.add
//    }

//    fun updateState() {
//        while (true) {
//            val currentEvent = gameQueue.remove()
//            val method = jsonToMethod(currentEvent)
//
//        }
//    }

    fun startGame() {

        if (isCurrentUserReady)
            return

        Log.e(TAG,  "in start game in game view model " + shareId.value)
        val docRef = Firebase.firestore.collection("games")
            .document(shareId.value!!)


        if (currentPlayer == Player.FIRST){
            Log.e("Log", "Is first player; gameId=" + shareId.value)
            docRef.set(hashMapOf(
                "FirstPlayerReady" to true
            ), SetOptions.merge())
        }
        else if (currentPlayer == Player.SECOND){
            Log.e("Log", "Is Second player; gameId=" + shareId.value)
            docRef.set(hashMapOf(
                "SecondPlayerReady" to true
            ), SetOptions.merge())
        }
        else {
            Log.e("Player log", " HUETA")
        }

        isCurrentUserReady = true
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

    fun playerConnected(){

    }

    override fun handleOpponentAreaClick(coordinate: Coordinate) {
        if (activePlayer == Player.FIRST) {
            Log.e("D", "in opponent area click 2 players")
            if (opponentBattleField.isCellFreeToBeSelected(coordinate)) {
                _selectedByPersonCoordinate.value = coordinate
            }
        }
    }


    public fun playAsPerson() {
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
                activePlayer = Player.COMPUTER
//                Firebase.firestore.
//                playAsComputer()
            }
        }
    }

    private fun playAsComputer() {
        val coordinate: Coordinate = shotManager.getCoordinateToShot()
        _selectedByComputerCoordinate.value = coordinate
        val isShipHit = personBattleField.handleShot(coordinate)
        shotManager.handleShot(isShipHit)
        if (isShipHit) {
            uiScope.launch {
//                delay(SECOND_IN_MILLIS)
                _computerSuccessfulShots.value = personBattleField.getCrossesCoordinates()
                if (personBattleField.isGameOver()) {
                    endGame(false)
                } else {
                    _status.value = R.string.status_opponent_shot_again_text
                    checkCurrentPlayer()
                }
            }
        } else {
            uiScope.launch {
//                delay(SECOND_IN_MILLIS + SECOND_IN_MILLIS / 2)
                _computerFailShots.value = personBattleField.getDotsCoordinates()
                activePlayer = Player.PERSON
                checkCurrentPlayer()
                _status.value = R.string.status_select_to_fire_text
            }
        }
    }

    private fun checkCurrentPlayer() {
        if (activePlayer == Player.PERSON) {
            playAsPerson()
        } else {
            playAsComputer()
        }
    }

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