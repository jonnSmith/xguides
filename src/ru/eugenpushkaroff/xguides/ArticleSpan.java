package ru.eugenpushkaroff.xguides;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class ArticleSpan extends ClickableSpan {

    final MainActivity activity;
    final String articleId;
    public TextPaint textpaint;
    
    public ArticleSpan(MainActivity activity, String articleId) {
        super();
        this.activity = activity;
        this.articleId = articleId;
    }
    
    @Override
    public void onClick(View arg0) {
        activity.setList(articleId);
    }
    
    @Override
    public void updateDrawState(TextPaint ds) {
    	textpaint = ds;    	
        textpaint.setColor(Color.BLUE);
        textpaint.setUnderlineText(true); 
    }
    

}