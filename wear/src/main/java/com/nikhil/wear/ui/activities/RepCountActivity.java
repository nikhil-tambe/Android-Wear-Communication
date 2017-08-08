package com.nikhil.wear.ui.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.nikhil.wear.R;
import com.nikhil.wear.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The main activity for the Jumping Jack application. This activity registers itself to receive
 * sensor values. Since on wearable devices a full screen activity is very short-lived, we set the
 * FLAG_KEEP_SCREEN_ON to give user adequate time for taking actions but since we don't want to
 * keep screen on for an extended period of time, there is a SCREEN_ON_TIMEOUT_MS that is enforced
 * if no interaction is discovered.
 * <p>
 * This activity includes a {@link android.support.v4.view.ViewPager} with two pages, one that
 * shows the current count and one that allows user to reset the counter. the current value of the
 * counter is persisted so that upon re-launch, the counter picks up from the last value. At any
 * stage, user can set this counter to 0.
 */

/**
 * Created by Nikhil on 8/8/17.
 */

public class RepCountActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "RepCountActivity";

    /**
     * How long to keep the screen on when no activity is happening
     **/
    private static final long SCREEN_ON_TIMEOUT_MS = 20000; // in milliseconds

    /**
     * an up-down movement that takes more than this will not be registered as such
     **/
    private static final long TIME_THRESHOLD_NS = 4000000000L; // in nanoseconds (= 4sec)

    /**
     * Earth gravity is around 9.8 m/s^2 but user may not completely direct his/her hand vertical
     * during the exercise so we leave some room. Basically if the x-component of gravity, as
     * measured by the Gravity sensor, changes with a variation (delta) > GRAVITY_THRESHOLD,
     * we consider that a successful count.
     */
    private static final float GRAVITY_THRESHOLD = 6.0f;

    @BindView(R.id.counter_TextView)
    TextView counter_TextView;

    private SensorManager sensorManager;
    private Sensor sensor;
    private long lastTime = 0;
    private boolean isUp = false;
    private int jumpCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reps_count);

        ButterKnife.bind(this);
        setAmbientEnabled();

        jumpCounter = Utils.getCounterFromPreference(this);
        counter_TextView.setText("" + jumpCounter);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Successfully registered for the sensor updates");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Unregistered for sensor events");
        }
    }

    @OnClick(R.id.resetCounter_Button)
    public void resetCounterButton_Clicked() {
        setCounter(0);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(TAG, "onSensorChanged: " + event.timestamp + " : " + event.values[0]);
        detectRep(event.values[0], event.timestamp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * A simple algorithm to detect a successful up-down movement of hand(s). The algorithm is
     * based on the assumption that when a person is wearing the watch, the x-component of gravity
     * as measured by the Gravity Sensor is +9.8 when the hand is downward and -9.8 when the hand
     * is upward (signs are reversed if the watch is worn on the right hand). Since the upward or
     * downward may not be completely accurate, we leave some room and instead of 9.8, we use
     * GRAVITY_THRESHOLD. We also consider the up <-> down movement successful if it takes less than
     * TIME_THRESHOLD_NS.
     */
    private void detectRep(float xValue, long timestamp) {
        if ((Math.abs(xValue) > GRAVITY_THRESHOLD)) {
            if (timestamp - lastTime < TIME_THRESHOLD_NS
                    && isUp != (xValue > 0)) {
                onRepDetected(!isUp);
            }
            isUp = xValue > 0;
            lastTime = timestamp;
        }
    }

    /**
     * Called on detection of a successful down -> up or up -> down movement of hand.
     */
    private void onRepDetected(boolean up) {
        // we only count a pair of up and down as one successful movement
        if (up) {
            return;
        }
        jumpCounter++;
        setCounter(jumpCounter);
    }

    /**
     * Updates the counter on UI, saves it to preferences and vibrates the watch when counter
     * reaches a multiple of 10.
     */
    private void setCounter(int i) {
        displayCounter(i);
        Utils.saveCounterToPreference(this, i);
        if (i > 0 && i % 10 == 0) {
            Utils.vibrate(this, 0);
        }
    }

    public void displayCounter(int i) {
        counter_TextView.setText(i < 0 ? "0" : String.valueOf(i));
    }

}
