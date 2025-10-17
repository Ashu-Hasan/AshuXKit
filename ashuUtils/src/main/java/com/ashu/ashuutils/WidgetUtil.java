package com.ashu.ashuutils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public interface WidgetUtil {

    // Call this from your activity to hide status bar
    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint({"WrongConstant", "ObsoleteSdkInt"})
    public static void hideStatusBar(Activity activity) {
        Window window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 and above
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // Below Android 11
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            controller.hide(android.view.WindowInsets.Type.statusBars());
        }
    }


    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressLint("ResourceType")
    public static void changeActionBarAndStatusBarColor(Activity activity, int actionBarColor, int statusBarColor) {
        // Change ActionBar color, title, and icon color if available
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
            ActionBar actionBar = appCompatActivity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setBackgroundDrawable(new ColorDrawable(actionBarColor));
                actionBar.setTitle(Html.fromHtml("<font color='#000000'>" + actionBar.getTitle() + "</font>")); // Set title color to black
            }
        }

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(statusBarColor);
        }

        // Change Status Bar Icons to Dark (Black) when background is light (Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // For Android 11+ (API 30), use WindowInsetsController
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = activity.getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        }

       /* // Set Toolbar text and icon color to black
        Toolbar toolbar = activity.findViewById(R.id.toolbar); // Ensure your activity has a Toolbar with this ID
        if (toolbar != null) {
            toolbar.setTitleTextColor(Color.BLACK);

            // Change navigation icon color (for back button, hamburger menu, etc.)
            Drawable navIcon = toolbar.getNavigationIcon();
            if (navIcon != null) {
                navIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN); // Apply black tint
                toolbar.setNavigationIcon(navIcon);
            }
        }*/
    }


    public static Fragment switchFragment(FragmentManager fm,
                                          Fragment targetFragment, Fragment activeFragment,
                                          int containerId) {
        final String TAG = "FragmentSwitcher";

        if (fm == null || targetFragment == null) {
            Log.e(TAG, "Invalid params: fm=" + fm + " target=" + targetFragment + " active=" + activeFragment);
            return activeFragment; // Return current if params are invalid
        }

        try {
            FragmentTransaction transaction = fm.beginTransaction();

            // Case 1: No active fragment yet (first load)
            if (activeFragment == null) {
                Log.d(TAG, "No active fragment. Adding: " + targetFragment.getClass().getSimpleName());
                transaction.add(containerId, targetFragment).commitAllowingStateLoss();
                return targetFragment;
            }

            // Case 2: Switching between fragments
            if (activeFragment == targetFragment) {
                Log.d(TAG, "Target fragment is already active: " + targetFragment.getClass().getSimpleName());
                return targetFragment; // Already showing
            }

            if (!targetFragment.isAdded()) {
                Log.d(TAG, "Adding fragment: " + targetFragment.getClass().getSimpleName() +
                        " | Hiding: " + activeFragment.getClass().getSimpleName());
                transaction.add(containerId, targetFragment).hide(activeFragment).commitAllowingStateLoss();
            } else {
                Log.d(TAG, "Showing fragment: " + targetFragment.getClass().getSimpleName() +
                        " | Hiding: " + activeFragment.getClass().getSimpleName());
                transaction.hide(activeFragment).show(targetFragment).commitAllowingStateLoss();
            }

            Log.i(TAG, "Switched to fragment: " + targetFragment.getClass().getSimpleName());
            return targetFragment; // New active fragment
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException while switching fragments", e);
            return activeFragment; // Fallback to current fragment
        } catch (Exception e) {
            Log.e(TAG, "Exception while switching fragments", e);
            return activeFragment; // Fallback to current fragment
        }
    }

    public static void setupEditTextField(EditText editText, String fieldType) {

        // Clear any existing filters before applying new ones
        editText.setFilters(new InputFilter[]{}); // Removes previous max length filters

        switch (fieldType.toLowerCase()) {

            case "email":
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;

            case "number":
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);

                // ✅ Set max length to 10 digits
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                break;

            case "password":
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                // Default: password hidden
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());

                /*editText.ivEye.setOnClickListener(v -> {
                    if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // Show password
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    } else {
                        // Hide password
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    // Move cursor to end
                    editText.setSelection(editText.getText().length());
                });*/
                break;

            default:
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setScrolling(EditText editText, ScrollView scrollView, View scrollWidget) {
        editText.setOnTouchListener((v, event) -> {
            scrollView.postDelayed(() -> {
                scrollView.smoothScrollTo(0, (int) scrollWidget.getY());
            }, 200);
            return false; // return false so default behavior (keyboard showing) still happens
        });
    }
}
