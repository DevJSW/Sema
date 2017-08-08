package com.sema.sema.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by John on 29-May-17.
 */
public class CustonBoldFont extends TextView {


    public CustonBoldFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustonBoldFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustonBoldFont(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Aller_Bd.ttf");
            setTypeface(tf);
        }
    }

}

