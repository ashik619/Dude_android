package com.ashik619.dude.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.ashik619.dude.R;


/**
 * Created by ashik619 on 27/01/17.
 */

public class CustomEditText extends EditText {

    public static final int BOLD_EXTRA = 0;
    public static final int BOLD_EXTRA_ITALIC = 1;
    public static final int BOLD = 2;
    public static final int BOLD_ITALIC = 3;
    public static final int SEMI_BOLD = 4;
    public static final int SEMI_BOLD_ITALIC = 5;
    public static final int REGULAR = 6;
    public static final int REGULAR_ITALIC = 7;
    public static final int LIGHT = 8;
    public static final int LIGHT_ITALIC = 9;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText);
            int type = typedArray.getInt(R.styleable.CustomEditText_custom_font_et, REGULAR);
            typedArray.recycle();

            setCustomTypeface(context, type);
        }

    }

    private void setCustomTypeface(Context context, int type) {
        switch (type) {
            case BOLD_EXTRA: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/bold_extra.ttf"));
                break;
            }
            case BOLD_EXTRA_ITALIC: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/bold_extra_italic.ttf"));
                break;
            }
            case BOLD: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/bold.ttf"));
                break;
            }
            case BOLD_ITALIC: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/bold_italic.ttf"));
                break;
            }
            case SEMI_BOLD: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/semi_bold.ttf"));
                break;
            }
            case SEMI_BOLD_ITALIC: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/semi_bold_italic.ttf"));
                break;
            }
            case REGULAR: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Fonty.ttf"));
                break;
            }
            case REGULAR_ITALIC: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/regular_italic.ttf"));
                break;
            }
            case LIGHT: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/light.ttf"));
                break;
            }
            case LIGHT_ITALIC: {
                setTypeface(Typeface.createFromAsset(context.getAssets(), "open_sans/light_italic.ttf"));
                break;
            }
        }

    }


}
