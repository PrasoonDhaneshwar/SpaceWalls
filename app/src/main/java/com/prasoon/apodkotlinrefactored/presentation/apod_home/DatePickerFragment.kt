package com.prasoon.apodkotlinrefactored.presentation.apod_home

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants.CURRENT_DATE_FOR_API
import com.prasoon.apodkotlinrefactored.core.common.Constants.SELECTED_SIMPLE_DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment: DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val TAG = "DatePicker"

    val cal = Calendar.getInstance()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), R.style.DatePickerDialogTheme,this, year, month, day)

        // Account for Date Picker to show till today's date.
        val halfDayBefore = 1 * 12 * 60 * 60 * 1000L
        val minimumRange = Calendar.getInstance()
        minimumRange.set(Calendar.YEAR, 1995)
        minimumRange.set(Calendar.MONTH, 5)
        minimumRange.set(Calendar.DAY_OF_MONTH, 16)

        datePickerDialog.datePicker.setMinDate(minimumRange.getTimeInMillis());
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - halfDayBefore

        return datePickerDialog
    }

    override fun onDateSet(view: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int) {
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth)
        cal.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
        // Default format ex: YEAR: 2021 MONTH: 4 DAY: 19
        Log.d(TAG, "calendar default format: YEAR: $selectedYear MONTH: $selectedDayOfMonth DAY: $selectedDayOfMonth")

        // val myFormat = "dd.MM.yyyy" // mention the format you need
        // val myFormat2 = "dd LLL yyyy HH:mm:ss aaa z" // mention the format you need
        val format = "LLL d, yyyy" // mention the format you need
        val selectedDate = SimpleDateFormat(format, Locale.US).format(cal.time)
        Log.d(TAG, "calendar simpleDateFormat: $selectedDate")    // Jul 1, 2022

        val monthOfYearString =
            if (selectedMonth + 1 < 10) "0" + (selectedMonth + 1) else (selectedMonth + 1).toString()
        val dayOfMonthString =
            if (selectedDayOfMonth < 10) "0$selectedDayOfMonth" else selectedDayOfMonth.toString()

        val currentDateForApi = "$selectedYear-$monthOfYearString-$dayOfMonthString"

        val selectedDateBundle = Bundle()
        selectedDateBundle.putString(SELECTED_SIMPLE_DATE_FORMAT, selectedDate)
        selectedDateBundle.putString(CURRENT_DATE_FOR_API, currentDateForApi)

        setFragmentResult("REQUEST_KEY", selectedDateBundle)
    }
}