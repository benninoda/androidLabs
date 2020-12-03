package com.javalabs.converter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.javalabs.test.R;

import java.util.Objects;

public class DataFragmentPremium extends DataFragment {

    TextView dataFrom, dataTo;
    Spinner spinner, spinnerTo, spinnerFrom;
    DataViewModel mViewModel;
    ImageButton btnSwapValue, btnCopyInput, btnCopyOutput;

    public static DataFragmentPremium newInstance() {
        return new DataFragmentPremium();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container,savedInstanceState);
        mViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(DataViewModel.class);
        return inflater.inflate(R.layout.data_fragment, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Log.e("D", "IN INVIEWCREATED-premium");

        spinnerFrom = (Spinner) view.findViewById(R.id.data_from_unit);
        spinnerTo = (Spinner) view.findViewById(R.id.data_to_unit);

        btnSwapValue = (ImageButton) view.findViewById(R.id.btn_swap);
        btnCopyInput = (ImageButton) view.findViewById(R.id.btn_copy_input);
        btnCopyOutput = (ImageButton) view.findViewById(R.id.btn_copy_output);
        btnSwapValue.setOnClickListener(v -> swapValues());
        btnCopyInput.setOnClickListener(v -> copyInput());
        btnCopyOutput.setOnClickListener(v -> copyOutput());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void swapValues(){
        mViewModel.swapValues();
        int temp_pos = mViewModel.getPositionTo().getValue();
        spinnerTo.setSelection(mViewModel.getPositionFrom().getValue());
        spinnerFrom.setSelection(temp_pos);
    }

    public void copyInput() {
        ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.input_value),
                                                    mViewModel.getSelectedDataFrom().getValue());
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.toast_copy, Toast.LENGTH_SHORT).show();
    }

    public void copyOutput() {
        ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.output_value), mViewModel.getSelectedDataTo().getValue());
        assert clipboard != null;
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.toast_copy, Toast.LENGTH_SHORT).show();
    }
}
