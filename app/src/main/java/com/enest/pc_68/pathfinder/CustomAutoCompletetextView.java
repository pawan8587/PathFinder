package com.enest.pc_68.pathfinder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.HashMap;

/**
 * Created by PC-68 on 5/2/2017.
 */

public class CustomAutoCompletetextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {
    public CustomAutoCompletetextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected CharSequence convertSelectionToString(Object selectedItem) {
        HashMap<String, String> hm = (HashMap<String, String>) selectedItem;
        return hm.get("description");
    }
}
