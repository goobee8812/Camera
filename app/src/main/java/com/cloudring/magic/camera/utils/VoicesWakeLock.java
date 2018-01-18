/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudring.magic.camera.utils;

import android.content.Context;
import android.os.PowerManager;

/**
 * Utility class to hold wake lock in app.
 */
public class VoicesWakeLock {

    private static PowerManager.WakeLock sCpuWakeLock;

    private static VoicesWakeLock instance;
    private  Context context;

    private VoicesWakeLock(Context context) {
        this.context=context;
        createPartialWakeLock(context);
    }

    public static VoicesWakeLock getInstance(Context context) {
        if (instance == null) {
            synchronized (VoicesWakeLock.class) {
                if (instance == null) {
                    instance = new VoicesWakeLock(context);
                }
            }
        }
        return instance;
    }

    public void createPartialWakeLock(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sCpuWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");

        sCpuWakeLock.setReferenceCounted(false);
    }

    public void acquire() {
        if (sCpuWakeLock != null) {
            System.out.println("acquire 电源锁");
            sCpuWakeLock.acquire();
            return;
        }
        createPartialWakeLock(context.getApplicationContext());
        System.out.println("acquire 电源锁 0");
        sCpuWakeLock.acquire();
    }


    public void releaseLock() {
        if (sCpuWakeLock != null) {
            System.out.println("释放电源锁");
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
