package com.prasoon.apodkotlinrefactored.presentation.apod_list

import com.prasoon.apodkotlinrefactored.domain.model.Apod

interface ListAction {
    fun onItemClickDetail(date: String)
    fun onItemClickDeleted(apodModel: Apod, position: Int) : Boolean
}