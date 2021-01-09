package com.javalabs.battleship.viewmodels

import com.javalabs.battleship.battle_field.Coordinate

interface IGetViewModel {
    fun handleOpponentAreaClick(coordinate: Coordinate)
}