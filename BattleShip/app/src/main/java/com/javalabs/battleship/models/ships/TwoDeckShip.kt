package com.javalabs.battleship.models.ships

import com.javalabs.battleship.TWO_DECK_SHIP_SIZE

class TwoDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return TWO_DECK_SHIP_SIZE
    }
}