package com.sema.sema;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by John on 29-May-17.
 */
public class CustonFont extends TextView {


    public CustonFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustonFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustonFont(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Aller_Rg.ttf");
            setTypeface(tf);
        }
    }

}

