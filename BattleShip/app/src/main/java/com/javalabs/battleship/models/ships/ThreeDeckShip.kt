package com.javalabs.battleship.models.ships

import com.javalabs.battleship.THREE_DECK_SHIP_SIZE

class ThreeDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return THREE_DECK_SHIP_SIZE
    }
}