package com.wongxd.absolutedomain

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DownloadService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
