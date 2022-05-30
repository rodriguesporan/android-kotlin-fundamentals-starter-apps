/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var _tonight = MutableLiveData<SleepNight?>()
    private val _nights = database.getAllNights()
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    private var _showSnackBarEvent = MutableLiveData<Boolean>()

    val navigateToSleepQuality: LiveData<SleepNight> get() = _navigateToSleepQuality
    val showSnackBarEvent: LiveData<Boolean> get() = _showSnackBarEvent

    val nightsString = Transformations.map(_nights) { nights ->
        formatNights(nights, application.resources)
    }
    val clearButtonVisible = Transformations.map(_nights) { nights ->
        nights?.isNotEmpty()
    }
    val startButtonVisible = Transformations.map(_tonight) { tonight ->
        tonight == null
    }
    val stopButtonVisible = Transformations.map(_tonight) { tonight ->
        tonight != null
    }

    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        viewModelScope.launch {
            _tonight.value = getTonightFromDatabase()
        }
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()

            insert(newNight)
            _tonight.value = getTonightFromDatabase()
        }
    }

    /*fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = _tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)

            _navigateToSleepQuality.value = oldNight
        }
    }*/
    fun onStopTracking() {
        viewModelScope.launch {
            // In Kotlin, the return@label syntax is used for specifying which function among
            // several nested ones this statement returns from.
            // In this case, we are specifying to return from launch(),
            // not the lambda.
            val oldNight = _tonight.value ?: return@launch

            // Update the night in the database to add the end time.
            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight)

            // Set state to navigate to the SleepQualityFragment.
            _navigateToSleepQuality.value = oldNight
        }
    }

    fun onClear() {
        viewModelScope.launch {
            database.clear()
            _tonight.value = null
            _showSnackBarEvent.value = true
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if (night?.startTimeMilli != night?.endTimeMilli) {
            night = null
        }

        return night
    }

    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackBar() {
        _showSnackBarEvent.value = false
    }
}
