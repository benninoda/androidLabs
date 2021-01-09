package com.javalabs.battleship.models.ships

import com.javalabs.battleship.FOUR_DECK_SHIP_SIZE
import com.javalabs.battleship.battle_field.Cell
import com.javalabs.battleship.battle_field.Coordinate
import java.util.*
import kotlin.collections.ArrayList
import com.javalabs.battleship.battle_field.CellState.*
import com.javalabs.battleship.models.Direction

abstract class Ship {

    private var cells: ArrayList<Cell> = ArrayList(FOUR_DECK_SHIP_SIZE)
    private lateinit var row: IntRange
    private lateinit var column: IntRange
    private var orientation: Direction = when (Random().nextBoolean()) {
        true -> Direction.VERTICAL
        false -> Direction.HORIZONTAL
    }

    protected fun initCells() {
        for (index in 0 until getLength()) {
            cells.add(Cell(SHIP))
        }
        cells.trimToSize()
    }

    fun getShipCells(): ArrayList<Cell> {
        return cells
    }

    fun getShipOrientation(): Direction {
        return orientation
    }

    fun getRowCoordinates(): IntRange {
        return row
    }

    fun getColumnCoordinates(): IntRange {
        return column
    }

    fun setCellsCoordinates(i: Int, j: Int) {
        var iPos = i
        var jPos = j
        if (orientation == Direction.VERTICAL) {
            row = IntRange(i, i + getLength() - 1)
            column = IntRange(j, j)
            for (index in 0 until getLength()) {
                cells[index].setCoordinates(iPos, jPos)
                iPos++
            }
        } else {
            row = IntRange(i, i)
            column = IntRange(j, j + getLength() - 1)
            for (index in 0 until getLength()) {
                cells[index].setCoordinates(iPos, jPos)
                jPos++
            }
        }
    }

    fun setShotSuccessState(coordinate: Coordinate) {
        for (cell in cells) {
            if (cell.getX() == coordinate.x && cell.getY() == coordinate.y) {
                cell.setCellState(SHOT_SUCCESS)
                break
            }
        }
    }

    fun isDead(): Boolean {
        var isDead = true
        for (cell in cells) {
            if (cell.getCellState() == SHIP) {
                isDead = false
                break
            }
        }
        return isDead
    }

    abstract fun getLength(): Int

    override fun toString(): String {
        return getLength().toString() + " " + orientation + " " + cells.toString()
    }
}