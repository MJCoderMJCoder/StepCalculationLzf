package com.lzf.stepcalculationlzf;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView text_step; //显示步数
    private TextView text_km;   //显示千米
    private SharedPreferences sharedPreferences;    //都在onCreate中初始化

    private long startTime; //库里面存储的今天的开始时间
    private long endTime; //库里面存储的今天的截止时间
    private int steps; //库里面存储的步数值
    private int stepsExtra; //库里面存储的因为重复累加产生的多余步数（应该减去）
    private int steps_today; //最终显示的今天的步数值

    private int stepsSensor = 0; //传感器的当前步数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text_step = (TextView) findViewById(R.id.main_text_step);
        text_km = (TextView) findViewById(R.id.text_km);
        // Context.MODE_PRIVATE：私有数据；只能够被本应用访问，写入的内容会覆盖源文件的内容
        sharedPreferences = this.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        startService(new Intent(this, SensorListener.class));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onResume() {
        super.onResume();
        // 如果有计步硬件支持，注册一个传感器监听器来实时更新UI
        SensorManager sm = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);  //Android4.4以上提供的计步传感器
        if (sensor == null) {
            new AlertDialog.Builder(this).setTitle("该设备不支持")
                    .setMessage("此应用程序需要专用的硬件计步传感器，当前的设备不具备，可能无法实现计步功能。")
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialogInterface) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        } else {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
            if (sharedPreferences.contains("startTime")) {
                startTime = sharedPreferences.getLong("startTime", 0);
                endTime = sharedPreferences.getLong("endTime", 0);
                steps = sharedPreferences.getInt("steps", 0);
                stepsExtra = sharedPreferences.getInt("stepsExtra", 0);
                if (System.currentTimeMillis() > endTime) {
                    saveSharedPreferences(sharedPreferences);
                }
            } else {
                saveSharedPreferences(sharedPreferences);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        //        try {
        //            SensorManager sm = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        //            sm.unregisterListener(this);
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values[0] > Integer.MAX_VALUE) {
            return;
        } else {
            stepsSensor = (int) sensorEvent.values[0];
            Log.v("stepsExtra ", stepsExtra + "");
            Log.v("steps ", steps + "");
            Log.v("stepsSensor ", stepsSensor + "");
        }
        updateShow();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // 该方法必须实现；但是暂时用不到
    }


    private void updateShow() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (steps < 0) {
            editor.putInt("stepsExtra", stepsSensor);
            stepsExtra = stepsSensor;
            steps++;
        }
        if (stepsSensor <= 0) {
            editor.putInt("stepsExtra", stepsSensor);
            stepsExtra = stepsSensor;
        }
        Log.v("stepsExtra ", stepsExtra + "");
        Log.v("steps ", steps + "");
        steps_today = steps + stepsSensor - stepsExtra;
        text_step.setText(steps_today + "步");
        editor.putInt("steps", steps_today);
        editor.putInt("stepsExtra", stepsSensor);
        editor.commit();

        // 显示距离
        float stepsize = 70; //cm (1km=100000cm)
        float distance_today = steps_today * stepsize;
        distance_today /= 100000;
        text_km.setText(distance_today + "km");
    }

    private void saveSharedPreferences(SharedPreferences sharedPreferences) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long startTimeTemp = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DATE, 1);
        long endTimeTemp = c.getTimeInMillis();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("startTime", startTimeTemp);
        editor.putLong("endTime", endTimeTemp);
        editor.putInt("steps", -1);
        editor.putInt("stepsExtra", 0);
        editor.commit();

        startTime = sharedPreferences.getLong("startTime", 0);
        endTime = sharedPreferences.getLong("endTime", 0);
        steps = sharedPreferences.getInt("steps", 0);
        stepsExtra = sharedPreferences.getInt("stepsExtra", 0);
        DeleteTestFile.sharedPrefsFile(getPackageName());
    }
}
