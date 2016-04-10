package ru.eugenpushkaroff.xguides;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class ParameterizedSpan extends CharacterStyle {

    int color = 0;
    
    public ParameterizedSpan(String param) {
        try {
            color = Color.parseColor("#" + param);
        } catch(Exception ex) { }
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setColor(color);
    }

}