package com.javalabs.battleship.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.javalabs.battleship.R
import com.javalabs.battleship.databinding.FragmentCreateBattlefieldBinding
import com.javalabs.battleship.viewmodels.MainViewModel
import com.javalabs.battleship.views.OpponentFieldView
import com.javalabs.battleship.views.UserFieldView


class CreateBattlefieldFragment : Fragment() {

    lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentCreateBattlefieldBinding
    private val customOnTouchListener = View.OnTouchListener(implementCustomTouchListener())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_create_battlefield, container, false
        )
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.mainViewModel = viewModel

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val userFieldView: UserFieldView = view.findViewById(R.id.viewPerson)
//        userFieldView.provideViewModel(viewModel)

        val opponentFieldView: OpponentFieldView = view.findViewById(R.id.viewComputer)
        opponentFieldView.provideViewModel(viewModel)

        viewModel.status.observe( viewLifecycleOwner) { newStatusId ->
            binding.tvStatus.text = resources.getText(newStatusId)
        }

        viewModel.selectedByPersonCoordinate.observe(viewLifecycleOwner) { point ->
            binding.viewComputer.getSelectedCoordinate(point)
            binding.viewFire.visibility = if (point == null) View.INVISIBLE else View.VISIBLE
        }

        viewModel.selectedByComputerCoordinate.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.VISIBLE
        }

        viewModel.personShips.observe(viewLifecycleOwner) { coordinates ->
            binding.viewPerson.getShipsCoordinates(coordinates)
            if (coordinates.isNotEmpty()) {
                binding.viewStart.visibility = View.VISIBLE
            }
        }

        viewModel.personSuccessfulShots.observe(viewLifecycleOwner) { coordinates ->
            binding.viewComputer.getCrossesCoordinates(coordinates)
        }

        viewModel.personFailedShots.observe(viewLifecycleOwner) { coordinates ->
            binding.viewComputer.getDotsCoordinates(coordinates)
        }

        viewModel.computerSuccessfulShots.observe(viewLifecycleOwner) { coordinates ->
            binding.viewPerson.getCrossesCoordinates(coordinates)
            binding.progressBar.visibility = View.INVISIBLE
        }

        viewModel.computerFailedShots.observe(viewLifecycleOwner) { coordinates ->
            binding.viewPerson.getDotsCoordinates(coordinates)
            binding.progressBar.visibility = View.INVISIBLE
        }

        viewModel.startGameEvent.observe(viewLifecycleOwner) { isStarted ->
            if (isStarted) binding.viewStart.visibility = View.GONE
            if (!isStarted) binding.viewNewGame.visibility = View.INVISIBLE
            binding.viewGenerate.visibility = if (isStarted) View.INVISIBLE else View.VISIBLE
        }

        viewModel.endGameEvent.observe(viewLifecycleOwner) { isEnded ->
            binding.viewNewGame.visibility = if (isEnded) View.VISIBLE else View.INVISIBLE
        }

        binding.viewGenerate.setOnTouchListener(customOnTouchListener)
        binding.viewFire.setOnTouchListener(customOnTouchListener)
        binding.viewStart.setOnTouchListener(customOnTouchListener)
        binding.viewNewGame.setOnTouchListener(customOnTouchListener)
    }

    private fun implementCustomTouchListener(): (View, MotionEvent) -> Boolean {
        return { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundColor(ContextCompat.getColor( requireContext(), R.color.colorAccent))
                    setTextColor(v, R.color.colorPrimary)
                }
                MotionEvent.ACTION_UP -> {
                    v.background = ContextCompat.getDrawable( requireContext(), R.drawable.square_background)
                    setTextColor(v, R.color.colorPrimaryDark)
                    v.performClick()
                }
            }
            true
        }
    }

    private fun setTextColor(v: View, color: Int) {
        if (v is TextView) v.setTextColor(
            ContextCompat.getColor( requireContext(), color)
        )
    }


}