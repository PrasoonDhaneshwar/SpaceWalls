package com.prasoon.apodkotlinrefactored.presentation.apod_list

import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

interface ListAction {
    fun onItemClickDetail(date: String)
    fun onItemClickDeleted(apodArchive: ApodArchive, position: Int) : Boolean
}