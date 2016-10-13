package com.ubudu.ilapp2.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ubudu.ilapp2.R;

/**
 * Created by mgasztold on 24/02/16.
 */
public final class FragmentUtils {

    private FragmentUtils() {}

    public static void changeFragment(FragmentActivity activity, Fragment nextFragment, boolean addToBackStack) {
            String name = nextFragment.getClass().getSimpleName();

            final FragmentManager fragmentManager = activity.getSupportFragmentManager();

            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_right_out);

            fragmentTransaction.replace(R.id.frame_content, nextFragment);

            if (addToBackStack) {
                fragmentTransaction.addToBackStack(name);
            }
            try {
                fragmentTransaction.commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            } catch (IllegalStateException e) {
            }

    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
