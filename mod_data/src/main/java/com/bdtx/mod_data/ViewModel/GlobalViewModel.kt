package com.bdtx.mod_data.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GlobalViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        @Volatile
        private var instance: GlobalViewModel? = null

        fun getInstance(application: Application): GlobalViewModel {
            return instance ?: synchronized(this) {
                instance ?: GlobalViewModel(application).also { instance = it }
            }
        }
    }
}

class GlobalViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlobalViewModel.getInstance(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}