package com.javalabs.battleship.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.javalabs.battleship.R
import com.javalabs.battleship.battle_field.BattleField
import com.javalabs.battleship.battle_field.Coordinate
import com.javalabs.battleship.models.Player
import com.javalabs.battleship.models.ships.Ship
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
//import org.junit.runner.Request.method
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class GameViewModel : ViewModel(), IGetViewModel {

    private lateinit var playerId: String
    lateinit var activePlayer: Player
    private lateinit var client: CollectionReference

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

//    private lateinit var gameQueue: Queue<Any>
//    private lateinit var shotManager: ShotManager

    //==================================
    private lateinit var currentPlayer: Player;
    private lateinit var currentPlayerId: String;


    init {
        initValues()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    private fun setPlayer(player: Player, uid: String) {
        Log.d("GameView", "Player: ${player}")
        Log.d("GameView", "Player UID: ${uid}")
        activePlayer = player;
        currentPlayer = Player.NONE;
        currentPlayerId = uid; //
    }

    private fun initValues() {
//        playerId = Firebase.auth.currentUser!!.uid
        activePlayer = Player.NONE
        //        shotManager = ShotManager()
        personBattleField = BattleField()
        opponentBattleField = BattleField()
        _status.value = R.string.status_welcome_text
        _startGameEvent.value = false
        _endGameEvent.value = false
        _selectedByPersonCoordinate.value = null
    }


    /**
     * Create listener for game events from firebase
     */
    fun initFirebaseConsumer(gameId: String) {
        val gameDocumentReference = Firebase.firestore.collection("gameEvents")
            .document(gameId);
        gameDocumentReference.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("listener", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("TAG", "Current data: ${snapshot.data}")
                val currentData = snapshot.data;
                when (currentData?.get("CurrentEvent")) {
                    "PlayerTurn" -> {
                        processPlayerTurn(currentData["CurrentPlayer"] as String)
                    }
                    "Hit" -> {
                        processHit(
                            currentData["CurrentPlayer"] as String,
                            currentData["X"].toString().toInt(),
                            currentData["Y"].toString().toInt()
                        )
                    }
                    "Miss" -> {
                        processMiss(
                            currentData["CurrentPlayer"] as String,
                            currentData["X"].toString().toInt(),
                            currentData["Y"].toString().toInt()
                        )

                    }
                    "SwitchPlayers" -> {
                        processSwitchPlayers(currentData["CurrentPlayer"] as String)
                    }
                    "EndGame" -> {
                        processEndGame(currentData["Winner"] as String)
                    }
                }
            } else {
                Log.e("listener", "Current data: null")
            }
        }
    }

    private fun playerConnected() {

    }

    fun firstPlayerConnect(player: Player, uid: String) {
        setPlayer(player, uid);
//        currentPlayer = Player.FIRST;
        // todo: put event
        playerConnected()
    }

    fun secondPlayerConnect(player: Player, uid: String) {
        setPlayer(player, uid);
//        currentPlayer = Player.FIRST ;
        // todo: override params
        playerConnected()
    }

    fun processPlayerTurn(id: String) {
        if (id == activePlayer.toString()) {
            _startGameEvent.value = true;
            currentPlayer = activePlayer;
            playAsPerson();
        }
        else {
            currentPlayer = Player.NONE;
        }
    }

    fun shot() {
        // todo: do we need it ?
    }

    fun processSwitchPlayers(previousPlayerId: String) {
        //
        if (previousPlayerId == currentPlayerId) {
            // Make Battlefield inactive
            // todo:
        }
    }

    fun processStartGame() {
        // 1. get game info drom DB
        // todo:
        // playerTurn(1)
    }

    fun processEndGame(winnerId: String) {
        // update stats for both players
        // todo:
    }

    fun processFirstPlayerConnect() {
        // todo:
    }

    fun processSecondPlayerConnect() {
        if (activePlayer == Player.FIRST) {
            // put startGameEvent to game state -- only server starts game !
            // todo:
        }
    }

    fun startGame() {
        val collection = Firebase.firestore.collection("games")
            .document(shareId.value!!)
        val eventsCollection = Firebase.firestore.collection("gameEvents")
            .document(shareId.value!!)
        if (activePlayer == Player.FIRST) {
            Log.e("Log", "Is first player; gameId=" + shareId.value)
            collection.set(
                hashMapOf(
                    "FirstPlayerReady" to true
                ), SetOptions.merge()
            )
            collection.set(
                hashMapOf(
                    "FirstPlayerField" to personBattleField.getShipsCoordinates()
                ), SetOptions.merge()
            )
            eventsCollection.set(
                hashMapOf(
                    "CurrentEvent" to "PlayerTurn",
                    "CurrentPlayer" to "FIRST",
                    "Params" to hashMapOf(
                        "currentPlayer" to "First"
                    )
                )
            )
        }
        if (activePlayer == Player.SECOND) {
            Log.e("Log", "Is Second player; gameId=" + shareId.value)
            collection.set(
                hashMapOf(
                    "SecondPlayerReady" to true
                ), SetOptions.merge()
            )
            collection.set(
                hashMapOf(
                    "SecondPlayerField" to personBattleField.getShipsCoordinates()
                ), SetOptions.merge()
            )
        }
        val listen = collection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("listener", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d("TAG", "Current data: ${snapshot.data}")
//                personBattleField.ships = snapshot.data?.get("FirstPlayerBattlefield") as List<Ship>
//                opponentBattleField.ships = snapshot.data?.get("SecondPlayerBattlefield") as List<Ship>
            } else {
                Log.e("listener", "Current data: null")
            }
        }
        Thread.sleep(2000L);
    }

    fun generateShips() {
        personBattleField.initBattleShip()
        personBattleField.randomizeShips()
        _personShips.value = personBattleField.getShipsCoordinates()

        opponentBattleField.initBattleShip()
        opponentBattleField.randomizeShips()
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
        if (activePlayer == currentPlayer) {
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

    fun processHit(playerId: String, x: Int, y: Int) {
        val eventsCollection = Firebase.firestore.collection("gameEvents")
            .document(shareId.value!!)
        if (playerId == activePlayer.toString()) {
            _status.value = R.string.status_shot_ship_again_text
            _personSuccessfulShots.value = opponentBattleField.getCrossesCoordinates()
            if (opponentBattleField.isGameOver()) {
                // todo: put in db endGameEvent(winner)
                endGame(true)
            } else {
                // another hit
                if (playerId == Player.FIRST.toString()) {
                    eventsCollection.set(
                        hashMapOf(
                            "CurrentPlayer" to playerId,
                            "CurrentEvent" to "PlayerTurn"
                        )
                    )
                }
            }
        } else {
            val coordinate:Coordinate = Coordinate(x, y);
            _selectedByComputerCoordinate.value = coordinate
            val isShipHit = personBattleField.handleShot(coordinate)
            _computerSuccessfulShots.value = personBattleField.getCrossesCoordinates()
            if (playerId == Player.FIRST.toString()) {
                eventsCollection.set(
                    hashMapOf(
                        "CurrentPlayer" to playerId,
                        "CurrentEvent" to "SwitchPlayers"
                    )
                )
            }
        }
    }

    fun processMiss(playerId: String, x: Int, y: Int) {
        val eventsCollection = Firebase.firestore.collection("gameEvents")
            .document(shareId.value!!)
        if (playerId == activePlayer.toString()) {
            val coordinate:Coordinate = Coordinate(x, y);
            _selectedByComputerCoordinate.value = coordinate
            val isShipHit = personBattleField.handleShot(coordinate)
            _computerSuccessfulShots.value = personBattleField.getCrossesCoordinates()
            if (playerId == Player.FIRST.toString()) {
                eventsCollection.set(
                    hashMapOf(
                        "CurrentPlayer" to playerId,
                        "CurrentEvent" to "SwitchPlayers"
                    )
                )
            }
        } else {
            _status.value = R.string.status_shot_ship_again_text
            _personSuccessfulShots.value = opponentBattleField.getCrossesCoordinates()
            if (opponentBattleField.isGameOver()) {
                // todo: put in db endGameEvent(winner)
                endGame(true)
            } else {
                // another hit
                if (playerId == Player.FIRST.toString()) {
                    eventsCollection.set(
                        hashMapOf(
                            "CurrentPlayer" to playerId,
                            "CurrentEvent" to "PlayerTurn"
                        )
                    )
                }
            }
        }
    }

    fun makeFireAsPerson() {
        val eventsCollection = Firebase.firestore.collection("gameEvents")
            .document(shareId.value!!)
        if (activePlayer == Player.FIRST) {
            val isShipHit = opponentBattleField.handleShot(_selectedByPersonCoordinate.value)
            if (isShipHit) {
                eventsCollection.set(
                    hashMapOf(
                        "CurrentEvent" to "Hit",
                        "CurrentPlayer" to activePlayer,
                        "X" to _selectedByPersonCoordinate.value!!.x,
                        "Y" to _selectedByPersonCoordinate.value!!.y
                    )
                )
                _personFailShots.value = opponentBattleField.getDotsCoordinates()
            } else {
                eventsCollection.set(
                    hashMapOf(
                        "CurrentEvent" to "Miss",
                        "CurrentPlayer" to activePlayer,
                        "X" to _selectedByPersonCoordinate.value!!.x,
                        "Y" to _selectedByPersonCoordinate.value!!.y
                    )
                )
                _personFailShots.value = opponentBattleField.getDotsCoordinates()
                _status.value = R.string.status_opponent_shot_text
            }
            _selectedByPersonCoordinate.value = null
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