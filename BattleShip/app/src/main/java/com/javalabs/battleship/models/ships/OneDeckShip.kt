package com.javalabs.battleship.models.ships

import com.javalabs.battleship.ONE_DECK_SHIP_SIZE

class OneDeckShip : Ship() {

    init {
        initCells()
    }

    override fun getLength(): Int {
        return ONE_DECK_SHIP_SIZE
    }
}