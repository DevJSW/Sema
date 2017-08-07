package com.sema.sema.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by John on 29-May-17.
 */
public class ButtonFont extends Button {


    public ButtonFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ButtonFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonFont(Context context) {
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

