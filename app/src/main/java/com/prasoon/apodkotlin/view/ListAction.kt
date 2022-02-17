package com.prasoon.apodkotlin.view

import com.prasoon.apodkotlin.model.ApodModel

interface ListAction {
    fun onItemClickDetail(id: Int)
    fun onItemClickDeleted(apodModel: ApodModel, position: Int) : Boolean
}