package com.javalabs.battleship.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.javalabs.battleship.models.Player
import com.javalabs.battleship.models.PlayerStats


object PlayerRepository {
    @Volatile
    var player: PlayerStats? = null
        private set

    fun createPlayer() {
        savePlayer(PlayerStats(FirebaseAuth.getInstance().currentUser!!.uid))
    }

    private fun savePlayer(player: PlayerStats) {
        Firebase.storage.reference.child("players")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

    }

    fun addEventListener() {
        val playerRefe =
            FirebaseStorage.getInstance().getReferenceFromUrl(Firebase.auth.currentUser!!.uid);

        val playerRef =
            FirebaseDatabase.getInstance().reference.child("players")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
        playerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                player = snapshot.getValue<PlayerStats>(PlayerStats::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                // nope
            }
        })
    }

    fun incWonGames() {
        if (player == null) return
        player!!.wonGames = player!!.wonGames + 1
        savePlayer(player!!)
    }

    fun incLostGames() {
        if (player == null) return
        player!!.lostGames = player!!.lostGames + 1
        savePlayer(player!!)
    }

    fun incUnfinishedGames() {
        if (player == null) return
        player!!.unfinishedGames = player!!.unfinishedGames + 1
        savePlayer(player!!)
    }

    fun resetStats() {
        if (player == null) return
        player!!.resetStats()
        savePlayer(player!!)
    }
}