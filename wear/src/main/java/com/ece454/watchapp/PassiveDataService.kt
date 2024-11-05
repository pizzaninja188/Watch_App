package com.ece454.watchapp

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer

class PassiveDataService : PassiveListenerService() {
    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        // TODO: Do something with dataPoints
    }
}