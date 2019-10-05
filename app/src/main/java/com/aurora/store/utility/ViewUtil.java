

package com.aurora.store.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.store.R;

import java.util.ArrayList;
import java.util.List;

public class ViewUtil {

    private static final List<int[]> gradientColors = new ArrayList<>();
    private static final List<Integer> solidColors = new ArrayList<>();
    private static int ANIMATION_DURATION_SHORT = 250;

    static {
        gradientColors.add(new int[]{0xFFFEB692, 0xFFEA5455});
        gradientColors.add(new int[]{0xFFC9CFFC, 0xFF7367F0});
        gradientColors.add(new int[]{0xFFFCE38A, 0xFFF38181});
        gradientColors.add(new int[]{0xFF90F7EC, 0xFF32CCBC});
        gradientColors.add(new int[]{0xFF81FBB8, 0xFF28C76F});
        gradientColors.add(new int[]{0xFFFDEB71, 0xFFFF6C00});
    }

    static {
        solidColors.add(0xFFFFB900);
        solidColors.add(0xFF28C76F);
        solidColors.add(0xFFEE3440);
        solidColors.add(0xFF7367F0);
        solidColors.add(0xFF00AEFF);
        solidColors.add(0xFF32CCBC);
    }

    @ColorInt
    public static int getSolidColors(int colorIndex) {
        return solidColors.get(colorIndex % solidColors.size());
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void setCustomColors(Context mContext, SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setColorSchemeColors(mContext
                .getResources()
                .getIntArray(R.array.colorShades));
    }

    @NonNull
    public static Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static int getStyledAttribute(Context context, int styleID) {
        TypedArray arr = context.obtainStyledAttributes(new TypedValue().data, new int[]{styleID});
        int styledColor = arr.getColor(0, Color.WHITE);
        arr.recycle();
        return styledColor;
    }

    public static void setText(TextView textView, String text) {
        if (null != textView)
            textView.setText(text);
    }

    public static void setText(View v, TextView textView, int stringId, Object... text) {
        setText(textView, v.getResources().getString(stringId, text));
    }

    public static void setText(View v, int viewId, String text) {
        TextView textView = v.findViewById(viewId);
        if (null != textView)
            textView.setText(text);
    }

    public static void setText(View v, int viewId, int stringId, Object... text) {
        setText(v, viewId, v.getResources().getString(stringId, text));
    }

    public static void showWithAnimation(View view) {
        final int mShortAnimationDuration = view.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

    }

    public static void hideWithAnimation(View view) {
        final int mShortAnimationDuration = view.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        view.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    public static void rotateView(View view, boolean reverse) {
        final RotateAnimation animation = new RotateAnimation(
                reverse ? 180 : 0,
                reverse ? 0 : 180,
                (float) view.getWidth() / 2,
                (float) view.getHeight() / 2);
        animation.setDuration(300);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    public static void expandView(final View v, int targetHeight) {
        int prevHeight = v.getHeight();
        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(ANIMATION_DURATION_SHORT);
        valueAnimator.start();
    }

    public static void collapseView(final View v, int targetHeight) {
        int prevHeight = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            v.getLayoutParams().height = (int) animation.getAnimatedValue();
            v.requestLayout();
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(ANIMATION_DURATION_SHORT);
        valueAnimator.start();
    }

    public static void setVisibility(View view, boolean visibility) {
        if (visibility)
            showWithAnimation(view);
        else
            hideWithAnimation(view);
    }

    public static void setVisibility(View view, boolean visibility, boolean noAnim) {
        if (noAnim)
            view.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
        else
            setVisibility(view, visibility);
    }
}
