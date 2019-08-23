package com.lzf.stepcalculationlzf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;


public class SensorListener extends Service implements SensorEventListener {
    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // 该方法必须实现；但是暂时用不到
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        // 每隔一段时间重新启动服务，以激活计步传感器
        ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC,
                        System.currentTimeMillis(), PendingIntent
                                .getService(getApplicationContext(), 2,
                                        new Intent(this, SensorListener.class),
                                        PendingIntent.FLAG_UPDATE_CURRENT));

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        reRegisterSensor();
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.v("onTaskRemoved", "sensor service task removed");
        // 隔500毫秒重新启动服务
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, new Intent(this, SensorListener.class), 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("onDestroy", "SensorListener onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void reRegisterSensor() {
        Log.v("reRegisterSensor", "re-register sensor listener");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1)
            return; // 模拟器
        Log.v("sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName()", sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName());
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL, 0);  // 延迟0(最多)
    }
}
