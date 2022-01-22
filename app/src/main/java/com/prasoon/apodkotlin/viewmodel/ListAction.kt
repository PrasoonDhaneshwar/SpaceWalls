package com.prasoon.apodkotlin.viewmodel

import com.prasoon.apodkotlin.model.ApodModel

interface ListAction {
    fun onItemClickDetail(id: Int)
    fun onItemClickDeleted(apodModel: ApodModel)
}