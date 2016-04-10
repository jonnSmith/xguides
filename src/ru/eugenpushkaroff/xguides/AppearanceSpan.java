package ru.eugenpushkaroff.xguides;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class AppearanceSpan extends CharacterStyle {
    
    public static final int NONE = -1;
    
    final int color, bgColor, textSize;
    final boolean boldText, italicText, strikeThruText, underlineText;
    
    public AppearanceSpan(int color, int bgColor, int textSize, boolean boldText, boolean italicText, 
            boolean strikeThruText, boolean underlineText) {
        this.color = color;
        this.bgColor = bgColor;
        this.textSize = textSize;
        this.boldText = boldText;
        this.italicText = italicText;
        this.strikeThruText = strikeThruText;
        this.underlineText = underlineText;
    }
    
    @Override
    public void updateDrawState(TextPaint tp) {
        if (color != NONE) tp.setColor(color);
        if (bgColor != NONE) tp.bgColor = bgColor;
        tp.setFakeBoldText(boldText);
        tp.setStrikeThruText(strikeThruText);
      //  if (textSize != NONE) tp.setTextSize(textSize);
        tp.setUnderlineText(underlineText);
        tp.setTextSkewX(italicText ? -0.25f : 0);
    }

}