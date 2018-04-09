package com.applimatix.wrapperlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;


/**
 * Created by mike on 28/07/15.
 */
public class WrapperLayout extends FrameLayout {

    private static final String TAG = "WrapperLayout";

    private int layoutWrapper;
    private int parentId;

    public WrapperLayout(Context context) {
        super(context);
        init(null);
    }

    public WrapperLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WrapperLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(21)
    public WrapperLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        //read the custom attributes

        if (attrs == null) {
            Log.wtf(TAG, "attrs not defined");
            return;
        }

        final TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.WrapperLayout);

        if (styledAttributes == null) {
            Log.wtf(TAG, "styledAttributes not defined");
            return;
        }

        layoutWrapper = styledAttributes.getResourceId(R.styleable.WrapperLayout_layoutWrapper, 0);
        parentId = styledAttributes.getResourceId(R.styleable.WrapperLayout_parentId, 0);

        styledAttributes.recycle();

        if (layoutWrapper == 0 || parentId == 0) {
            Log.wtf(TAG, "layoutWrapper or parentId not defined");
            return;
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                wrapLayout();
            }
        });
    }

    private void wrapLayout() {
        final FrameLayout content = getRootView().findViewById(android.R.id.content);
        final ViewGroup wrapper = (ViewGroup) inflate(getContext(), layoutWrapper, content);

        if (wrapper == null) {
            Log.wtf(TAG, "Could not inflate layoutWrapper");
            return;
        }

        final ViewGroup parent = wrapper.findViewById(parentId);

        if (parent == null) {
            Log.wtf(TAG, "Could not find parentId in layoutWrapper");
            return;
        }

        content.removeView(this);

        View wrapperChild;

        if (getChildCount() == 1) {
            wrapperChild = getChildAt(0);
            if (wrapperChild instanceof ViewGroup && parent.getParent() instanceof ViewGroup) {
                // wrapperChild is a ViewGroup so lets replace the parent ViewGroup with it
                ViewGroup grandParent = (ViewGroup) parent.getParent();
                ViewGroup.LayoutParams params = parent.getLayoutParams();
                wrapperChild.setLayoutParams(params);
                removeView(wrapperChild);
                int idx = grandParent.indexOfChild(parent);
                grandParent.removeView(parent);
                grandParent.addView(wrapperChild, idx);
            }
            else {
                // wrapperChild is not a ViewGroup
                removeView(wrapperChild);
                parent.addView(wrapperChild);
            }
        }
        else {
            for (int i = 0; i < getChildCount(); i++) {
                wrapperChild = getChildAt(i);
                removeView(wrapperChild);
                parent.addView(wrapperChild);
            }
        }
    }
}
