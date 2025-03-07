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

package com.example.android.trackmysleepquality

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

@RunWith(AndroidJUnit4::class)
class SleepDatabaseTest {

    private lateinit var sleepDao: SleepDatabaseDao
    private lateinit var db: SleepDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
                // Allowing main thread queries, just for testing.
                .allowMainThreadQueries()
                .build()
        sleepDao = db.sleepDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        sleepDao.clear()
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGet() {
        val night = SleepNight(sleepQuality = 2)
        sleepDao.insert(night)

        val tonight = sleepDao.get(1L)
        assertEquals(2, tonight?.sleepQuality)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetTonight() {
        val night = SleepNight()
        sleepDao.insert(night)

        val tonight = sleepDao.getTonight()
        assertEquals(-1, tonight?.sleepQuality)
    }

    @Test
    @Throws(Exception::class)
    fun updateAndGetTonight() {
        val night = SleepNight()
        sleepDao.insert(night)

        sleepDao.update(
            night.copy(nightId = 1, sleepQuality = 3)
        )

        val tonight = sleepDao.getTonight()
        Assert.assertNotNull(tonight)

        assertEquals(night.startTimeMilli, tonight?.startTimeMilli)
        assertEquals(night.endTimeMilli, tonight?.endTimeMilli)
        assertEquals(3, tonight?.sleepQuality)
    }
}