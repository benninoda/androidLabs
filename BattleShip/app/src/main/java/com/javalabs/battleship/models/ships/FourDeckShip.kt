package com.javalabs.battleship.models.ships

import com.javalabs.battleship.FOUR_DECK_SHIP_SIZE

class FourDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return FOUR_DECK_SHIP_SIZE
    }
}