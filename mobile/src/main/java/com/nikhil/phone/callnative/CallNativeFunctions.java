package com.nikhil.phone.callnative;

import static com.nikhil.shared.Constants.LibNames.MY_NATIVE_LIB_NAME;

/**
 * Created by Nikhil on 28/7/17.
 */

public class CallNativeFunctions {

    static {
        System.loadLibrary(MY_NATIVE_LIB_NAME);
    }

    public static native String helloWorld();

    public static native double calculateMag(double x, double y, double z);

    public static native void calcRep(double ax, double ay, double az,
                                      double gx, double gy, double gz);

    public static native double getRepValue();

}
