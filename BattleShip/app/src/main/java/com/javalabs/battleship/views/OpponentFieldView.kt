package com.javalabs.battleship.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.javalabs.battleship.R
import com.javalabs.battleship.SQUARES_COUNT
import com.javalabs.battleship.battle_field.Coordinate

class OpponentFieldView : FieldView {

    private lateinit var dotsCoordinates: ArrayList<Coordinate>
    private lateinit var crossesCoordinates: ArrayList<Coordinate>
    private lateinit var paintSelected: Paint
    private var selectedSquare: Coordinate? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        dotsCoordinates = arrayListOf()
        crossesCoordinates = arrayListOf()
        paintSelected = Paint()
        paintSelected.color = ContextCompat.getColor(context, R.color.greySelected)
        setOnTouchListener(OnTouchListener(getCustomOnTouchListener()))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (dotsCoordinates.isNotEmpty()) {
            for (coordinate in dotsCoordinates) {
                canvas?.drawDot(coordinate)
            }
        }
        if (crossesCoordinates.isNotEmpty()) {
            for (cross in crossesCoordinates) {
                canvas?.drawCross(cross.y, cross.x)
            }
        }
        if (selectedSquare != null) {
            canvas?.drawSquare(selectedSquare!!.x, selectedSquare!!.y, paintSelected)
        }
    }

    private fun getCustomOnTouchListener(): (View, MotionEvent) -> Boolean {
        return { _: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    viewModel.handleOpponentAreaClick(convertUICoordinates(event.x, event.y))
                }
            }
            true
        }
    }

    private fun convertUICoordinates(x: Float, y: Float): Coordinate {
        var i = y.toInt() / squareWidth.toInt()
        var j = x.toInt() / squareWidth.toInt()
        if (i == SQUARES_COUNT) i--
        if (j == SQUARES_COUNT) j--
        return Coordinate(i, j)
    }

    fun getSelectedCoordinate(selectedSquare: Coordinate?) {
        if (selectedSquare == null) {
            this.selectedSquare = null
        } else {
            this.selectedSquare = Coordinate(selectedSquare.y, selectedSquare.x)
        }
        invalidate()
    }

    fun getDotsCoordinates(coordinates: ArrayList<Coordinate>) {
        this.dotsCoordinates = coordinates
        invalidate()
    }

    fun getCrossesCoordinates(coordinates: ArrayList<Coordinate>) {
        this.crossesCoordinates = coordinates
        invalidate()
    }
}