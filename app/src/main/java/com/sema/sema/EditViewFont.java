package com.sema.sema;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by John on 29-May-17.
 */
public class EditViewFont extends EditText {


    public EditViewFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public EditViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditViewFont(Context context) {
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

