package com.github.aakumykov.logger.di

import com.github.aakumykov.logger.gesture_logger.GestureLogReader
import com.github.aakumykov.logger.gesture_logger.GestureLogWriter
import com.github.aakumykov.logger.gesture_logger.RoomGestureLogger
import dagger.Module
import dagger.Provides

@Module
class LoggerModule {

    // TODO: scope?
    @Provides
    fun provideGestureLogReader(roomGestureLogger: RoomGestureLogger): GestureLogReader {
        return roomGestureLogger
    }

    @Provides
    fun provideGestureLogWriter(roomGestureLogger: RoomGestureLogger): GestureLogWriter {
        return roomGestureLogger
    }
}