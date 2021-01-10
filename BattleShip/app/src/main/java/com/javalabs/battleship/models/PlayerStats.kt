package com.javalabs.battleship.models

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
class PlayerStats {
    var id: String? = null
    var wonGames = 0
    var lostGames = 0
    var unfinishedGames = 0

    constructor() {}
    constructor(user: FirebaseUser) {
        id = user.uid
        wonGames = 0
        lostGames = 0
        unfinishedGames = 0
    }

    constructor(id: String?) {
        this.id = id
        wonGames = 0
        lostGames = 0
        unfinishedGames = 0
    }

    constructor(id: String?, wonGames: Int, lostGames: Int, unfinishedGames: Int) {
        this.id = id
        this.wonGames = wonGames
        this.lostGames = lostGames
        this.unfinishedGames = unfinishedGames
    }

    fun resetStats() {
        wonGames = 0
        lostGames = 0
        unfinishedGames = 0
    }
}