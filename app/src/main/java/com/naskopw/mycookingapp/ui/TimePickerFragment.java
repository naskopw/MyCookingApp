package com.naskopw.mycookingapp.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private String alarmName;
    private int cookingTime;

    public TimePickerFragment(String recipeName, int cookingTime) {
        alarmName = recipeName;
        this.cookingTime = cookingTime;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, cookingTime);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_MESSAGE, alarmName);
        i.putExtra(AlarmClock.EXTRA_HOUR, hourOfDay);
        i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        startActivity(i);
    }
}

