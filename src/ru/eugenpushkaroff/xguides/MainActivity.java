package ru.eugenpushkaroff.xguides;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import ru.eugenpushkaroff.xguides.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.gms.ads.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class MainActivity extends Activity {

    TextView tvContent;
    RelativeLayout mainStage;
    public String serverURI = "http://osg.net.ua/helper/getdate.php";
    public int backpress = 1;
    public LinkedList<String> parentIDS = new LinkedList<String>();
    public Map<String, Integer> parentPosition = new HashMap<String, Integer>();
    private SimpleAdapter adpt;
    public Boolean CurrentView = false; 
    public Boolean CurrentSearch = false;
    public Boolean isSearch = false;
    public Boolean isListing = false;
    public int countList = 0;
    public String appTitle;
    public String postID = "1";
    public AdView adView;
    public Boolean adSense = true;
    public int padding_in_px = 0;
    
    static final float minTextSize = 12;
    static final float regularTextSize = 14;
    static final float maxTextSize = 24; 
    static final float changeTextSize = (float) 0.5;
    public float currentTextSize = 0;
    
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;    
    int mode = NONE;
    
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    
    int clickCount = 0;
    long startTime;
    long duration;
    static final int MAX_DURATION = 500;
    
    private PowerManager.WakeLock wl;
    
    float x1,x2;
    float y1,y2;
    float sWidth;

    
   
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
          }
                
        tvContent = (TextView)findViewById(R.id.tvContent);
        tvContent.setLinksClickable(true);
        tvContent.setMovementMethod(new LinkMovementMethod());        
        
        mainStage = (RelativeLayout) findViewById(R.id.mainstage);
        
        adpt  = new SimpleAdapter(MainActivity.this, new ArrayList<Contact>(), this);
        ListView lView = (ListView) findViewById(R.id.listview);
        lView.setAdapter(adpt);  
        
        adView = (AdView)this.findViewById(R.id.adView); 
                
        if(adSense) { 
        	
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                final int lAdHeight = adView.getHeight(); 
                ListView lView = (ListView) findViewById(R.id.listview);
                lView.setPadding(0, 5, 0, lAdHeight);
                tvContent.setPadding(10, 10, 10, lAdHeight); 
            	
            }
        });
        
        
        }        
        
        setList("article_1");        
        parentIDS.addLast("article_1");
        
        EasyTracker.getInstance(this).activityStart(this); 
        
    	DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);        
        sWidth = displaymetrics.widthPixels/2;
        
        tvContent.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                
            	float scale;
            	
            	switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                	start.set(event.getX(), event.getY());
                    mode = DRAG; 
                    x1 = event.getX();
                    y1 = event.getY();
                    break;         
                case MotionEvent.ACTION_UP: 
                	
                	clickCount++;

                    if (clickCount==1){
                        startTime = System.currentTimeMillis();
                    }

                    else if(clickCount == 2)
                    {
                        long duration =  System.currentTimeMillis() - startTime;
                        if(duration <= MAX_DURATION)
                        { 
                        	tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, regularTextSize);
                        	currentTextSize = 0;
                            clickCount = 0;
                            duration = 0;
                        }else{
                            clickCount = 1;
                            startTime = System.currentTimeMillis();
                        }
                        break;             
                    }                    
                    break;
                case MotionEvent.ACTION_POINTER_UP:          
                    mode = NONE;
                    break;         
                case MotionEvent.ACTION_POINTER_DOWN: 
                	oldDist = spacing(event);
                    mode = ZOOM;                        
                    break;  
                case MotionEvent.ACTION_MOVE:         
                    if (mode == DRAG) {
                        x2 = event.getX();
                        y2 = event.getY();                     

                        if ((x1 - sWidth) > x2)
                        {   
                        setPrevPost(postID);
                        x1 = x2;
                        }
                        else if ((x1 + sWidth) < x2)
                        {  
                        setNextPost(postID);
                        x1 = x2;
                        }

                    	} else if (mode == ZOOM) { 
                    	
                    	float newDist = spacing(event);
                    	scale = newDist / oldDist;
                    	
                    	if(scale > 1) {
                    	
                    	if(currentTextSize == 0){
                    		currentTextSize = regularTextSize + changeTextSize;
                    		tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
                    	} else if (currentTextSize >= maxTextSize){                    	
                    		currentTextSize = maxTextSize;	
                		    tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
                    	} else {
                    		currentTextSize = currentTextSize + changeTextSize;
                    		tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);                    		
                    	} 
                    	
                    	} else if(scale < 1) {
                    		
                        	if(currentTextSize == 0){
                        		currentTextSize = regularTextSize - changeTextSize;
                        		tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
                        	} else if (currentTextSize <= minTextSize){                    	
                        		currentTextSize = minTextSize;	
                    		    tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
                        	} else {
                        		currentTextSize = currentTextSize - changeTextSize;
                        		tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);                    		
                        	}   
                        	
                    	} 
                     }                 
                    break;
                } 
            	return true;
            }
        });
        
        
        
        
        appTitle = getStringResourceByName("title_activity_main");
        setTitle(appTitle);
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");
 
    }
		
	
   public void setNextPost(String cPostID) {
    
   	if (AppStatus.getInstance(this).isOnline(this)) {    	   	   		
    	
   	    setParentPosition();   	           	
   	   	postID = cPostID;   	
   	   	String serverURL = serverURI + "?next=true&p=" + postID;    	
   	    isSearch = false;
   	   	isListing = true;   
	    (new AsyncListViewLoader()).execute(serverURL); 	
	
	} else { setOutDialog(); }       
   }
    
    
   public void setPrevPost(String cPostID) {
       
   if (AppStatus.getInstance(this).isOnline(this)) {    	   	   		
       	
    setParentPosition();           	
   	postID = cPostID;   	
   	String serverURL = serverURI + "?back=true&p=" + postID;    	
   	isSearch = false;
   	isListing = true;   	
   	(new AsyncListViewLoader()).execute(serverURL); 	
   	
   	} else { setOutDialog(); } 
   }
       
    @Override
    protected void onPause() {
    super.onPause();
    wl.release();
    }

    @Override
    protected void onResume() {
    super.onResume();
    wl.acquire();
    }   
    
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) java.lang.Math.sqrt(x * x + y * y);
    }

    public boolean onCreateOptionsMenu(Menu menu) {    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu); 
        return super.onCreateOptionsMenu(menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        switch (item.getItemId()) {
           case R.id.action_back:
        	    setBackArticle();
                return true;
           case R.id.action_search: 
        	   LinearLayout searchView = (LinearLayout) findViewById(R.id.searchView);
        	   if(!CurrentSearch) {        	   
        	   searchView.setVisibility(View.VISIBLE);
        	   CurrentSearch = true;
        	   
               Button searchSubmit = (Button) findViewById(R.id.searchSubmit);
               final EditText searchText = (EditText) findViewById(R.id.searchText);
               
               searchSubmit.setOnClickListener(new View.OnClickListener() {
            	      public void onClick(View view) {
            	    	LinearLayout searchView = (LinearLayout) findViewById(R.id.searchView);
            	        String data = searchText.getText().toString();
            	        if(data != null && !data.isEmpty()) {
            	        if(!isSearch) { setParentPosition(); }
            	        String serverURL = serverURI + "?search=true&q=" + data;  
            	        setParentPosition();
            	        isSearch = true;
            	        isListing = false;
            	    	(new AsyncListViewLoader()).execute(serverURL);
            	    	backpress = 0;
            	    	searchView.setVisibility(View.GONE);
             		    CurrentSearch = false;
            	        }
            	      }
            	    });
        	   
        	   } else {
        		   searchView.setVisibility(View.GONE);
        		   CurrentSearch = false;
        	    }
            default:
                return super.onOptionsItemSelected(item);
        }
    }    
   

	public void onBackPressed()
    {    	    	
    	if (backpress == 1) {
            Toast toast = Toast.makeText(getApplicationContext(), 
            getStringResourceByName("before_exit_message"), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show(); 	
            backpress = 2;
    	} else if (backpress > 1) {      		
    	setOutToast();
        } else {   	
    	setBackArticle();
        }
    }
   
    public boolean dispatchKeyEvent(KeyEvent event) {  
        if (event.getAction() == KeyEvent.ACTION_DOWN) {  
            switch (event.getKeyCode()) {  
            case KeyEvent.KEYCODE_VOLUME_UP:  
                scrollToPrevious();  
                return true;  
            case KeyEvent.KEYCODE_VOLUME_DOWN:  
                scrollToNext();  
                return true;  
            }  
        }  
        if (event.getAction() == KeyEvent.ACTION_UP   
            && (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP   
                || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {  
            return true;  
        }  
        return super.dispatchKeyEvent(event);  
    }  
    
    private void scrollToNext() { 
    	if(CurrentView) { 
    		
    	ScrollView mainScrollView = (ScrollView) findViewById(R.id.sv);    	
    	DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        
    	int screenHeight = displaymetrics.heightPixels;    	
    	int newPosition = mainScrollView.getScrollY() + screenHeight - padding_in_px*2 - 50; 
    	
        mainScrollView.scrollTo(0, newPosition);        
        mainScrollView.clearFocus();  } else {  
        	
        ListView lView = (ListView) findViewById(R.id.listview);
        int nextPosition = lView.getLastVisiblePosition();
        lView.setSelection(nextPosition); 
        
        }
    }  
          
    private void scrollToPrevious() {  
    	if(CurrentView) { 
    	ScrollView mainScrollView = (ScrollView) findViewById(R.id.sv);    	
    	DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        
    	int screenHeight = displaymetrics.heightPixels;
    	int newPosition = mainScrollView.getScrollY()- screenHeight + padding_in_px*2 + 50;
    	
        mainScrollView.scrollTo(0, newPosition);
        mainScrollView.clearFocus();  
        
        } else {      
        	
            ListView lView = (ListView) findViewById(R.id.listview);
            
            int lastPosition = lView.getLastVisiblePosition();
            int firstPosition = lView.getFirstVisiblePosition();            
            int previousPosition = firstPosition - (lastPosition - firstPosition);
            
            if(previousPosition > 0){
            lView.setSelection(previousPosition);
            } else {            
            lView.setSelection(0);	
            }
        }
    }  
        
    void setBackArticle() {     	
    	if (AppStatus.getInstance(this).isOnline(this)) {
    	int marker =  parentIDS.size();    	
    	String Parent = "article_1";
    	
    	if (marker == 1){  
    	Parent = parentIDS.getFirst(); 
    	} else if (marker > 1){    	
   	    parentIDS.removeLast();
        Parent = parentIDS.getLast();       
    	} 
    	
    	setParentPosition();
    	isSearch = false;
    	
    	postID = Parent.replaceFirst("article_", "");
    	String serverURL = serverURI + "?list=true&p=" + postID;  
    	
    	isListing = false;
    	
    	(new AsyncListViewLoader()).execute(serverURL);
    	if(Parent != "article_1") { backpress = 0;} else { backpress = 1; }    	
    	if(adSense) {  }	
    	
    	} else {
    		setOutDialog();	
    	}}

    void setList(String strListResId) { 
    	if (AppStatus.getInstance(this).isOnline(this)) {    	   	   		
    	
    		int marker =  parentIDS.size();  
        	String Child = "child";
        	        	
        	if (marker > 1) { int childindex = marker - 2; Child = parentIDS.get(childindex); } 
        	
        	if(Child != strListResId) { parentIDS.addLast(strListResId); }
        	else { parentIDS.removeLast(); }  
        	
       
        	if( strListResId == "article_1" ) { backpress = 1; } else { backpress = 0; }
        	
        	setParentPosition();
        	isSearch = false;
        	
        	EasyTracker easyTracker = EasyTracker.getInstance(this);
        	
        	easyTracker.send(MapBuilder
        		      .createEvent("ui_action",
        		                   "set_list",
        		                   strListResId,
        		                   null)
        		      .build()
        		  );

        	
    	postID = strListResId.replaceFirst("article_", "");
    	String serverURL = serverURI + "?list=true&p=" + postID;  
    	
    	isListing = false;
    	
    	(new AsyncListViewLoader()).execute(serverURL); 
    	
    	 if(adSense) {  }
    	
    	} else {
    		setOutDialog();	
    	}} 
    
    public void setParentPosition(){    	
        int currentPosition = 0;    	
        if(CurrentView) { 
      	 ScrollView mainScrollView = (ScrollView) findViewById(R.id.sv);
      	 currentPosition = mainScrollView.getScrollY();
      	 } else {     	
      	 ListView lView = (ListView) findViewById(R.id.listview);    	 
      	 currentPosition = lView.getFirstVisiblePosition(); }      	
         
        if(!isSearch) { parentPosition.put(postID, currentPosition); }
        
    }
        
   DialogInterface.OnClickListener dioclExit = new DialogInterface.OnClickListener() {        
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            setOutToast();           
        }
    };
    
    DialogInterface.OnClickListener dioclOK = new DialogInterface.OnClickListener() {        
        public void onClick(DialogInterface dialog, int which) {
        	startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));       
        }
    };
    
    
    public void setOutDialog() {
    	 new AlertDialog.Builder(this)
       	.setTitle(getStringResourceByName("error_connection_title"))
       	.setMessage(getStringResourceByName("error_connection_text"))
       	.setNegativeButton(getStringResourceByName("error_connection_exit_button"), dioclExit)
       	.setPositiveButton(getStringResourceByName("error_connection_settings_button"), dioclOK)
       	.setCancelable(false)
       	.create().show();
    }
    
    public void setOutToast() {    	
        adpt.imageLoader.clearCache();
        EasyTracker.getInstance(this).activityStop(this);
        MainActivity.super.onBackPressed();
        System.exit(0);
    }
        
    
    final Spannable revertSpanned(Spanned stext) {
        Object[] spans = stext.getSpans(0, stext.length(), Object.class);
        Spannable ret = Spannable.Factory.getInstance().newSpannable(stext.toString());
        if (spans != null && spans.length > 0) {
            for(int i = spans.length - 1; i >= 0; --i) {
                ret.setSpan(spans[i], stext.getSpanStart(spans[i]), stext.getSpanEnd(spans[i]), stext.getSpanFlags(spans[i]));
            }
        }
        return ret;
    }
    

    Html.TagHandler htmlTagHandler = new Html.TagHandler() {
        public void handleTag(boolean opening, String tag, Editable output,	XMLReader xmlReader) {
            Object span = null;
            if (tag.startsWith("article_")) span = new ArticleSpan(MainActivity.this, tag);
            else if ("title".equalsIgnoreCase(tag)) span = new AppearanceSpan(0xff000000, AppearanceSpan.NONE, 20, true, false, false, false);
            else if (tag.startsWith("color_")) span = new ParameterizedSpan(tag.substring(6));
            if (span != null) processSpan(opening, output, span);
        }
    };
    
    void switchView(boolean viewID){
    	ListView lView = (ListView) findViewById(R.id.listview);	
        ScrollView mainScrollView = (ScrollView) findViewById(R.id.sv);  
        tvContent = (TextView)findViewById(R.id.tvContent);
        
    	if(viewID) {     
    	tvContent.setText("");
        lView.setVisibility(View.GONE);
        mainScrollView.setVisibility(View.VISIBLE);
        CurrentView = true;
    	} else {               
        lView.setVisibility(View.VISIBLE);
        mainScrollView.setVisibility(View.GONE);
        CurrentView = false;
    	}
    }
    
    void processSpan(boolean opening, Editable output, Object span) {
        int len = output.length();
        if (opening) {
            output.setSpan(span, len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object[] objs = output.getSpans(0, len, span.getClass());
            int where = len;
            if (objs.length > 0) {
                for(int i = objs.length - 1; i >= 0; --i) {
                    if (output.getSpanFlags(objs[i]) == Spannable.SPAN_MARK_MARK) {
                        where = output.getSpanStart(objs[i]);
                        output.removeSpan(objs[i]);
                        break;
                    }
                }
            }
            
            if (where != len) {
                output.setSpan(span, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    

  
    
    
    private class AsyncListViewLoader extends AsyncTask<String, Void, List<Contact>> {
    	private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
    	private String Content;
    	private String textContent = "empty";
    	private final HttpClient Client = new DefaultHttpClient();
        TextView uiUpdate = (TextView) findViewById(R.id.tvContent);        
        private boolean emptyError = false;
        
		@Override
		protected void onPostExecute(List<Contact> result) {	
			
			if(emptyError || Content.length() == 0) { 
				dialog.dismiss();
				setErrorText();				
			} else {
				
			int newPos = 0;
			
			if(!isSearch) { for( Entry<String, Integer> entry : parentPosition.entrySet() )
				if( postID.equals( entry.getKey() ) )
					newPos = entry.getValue();	}
			
			if(textContent.equals("empty")) {
			super.onPostExecute(result);			
						
			adpt.setItemList(result);
			adpt.notifyDataSetChanged();
			dialog.dismiss();			
			switchView(false);
			ListView lView = (ListView) findViewById(R.id.listview);
			lView.setSelection(newPos);
			
			} else {
				dialog.dismiss();				
				switchView(true);
				
		        Spanned spannedText = Html.fromHtml(textContent,new MyImageGetter(), htmlTagHandler);
				Spannable reversedText = revertSpanned(spannedText);
		        uiUpdate.setText(reversedText);
		        ScrollView mainScrollView = (ScrollView) findViewById(R.id.sv);
                mainScrollView.smoothScrollTo(0, newPos);
                mainScrollView.clearFocus();
                uiUpdate.setTextSize(TypedValue.COMPLEX_UNIT_SP, regularTextSize);
            	currentTextSize = 0;
				
			}
			
			setTitle(appTitle);
			}

		}
		
		private void setErrorText() {
	    	switchView(true);
	    	Spanned spannedText = Html.fromHtml(getStringResourceByName("error_connection_article"),new MyImageGetter(), htmlTagHandler);
			Spannable reversedText = revertSpanned(spannedText);
	        uiUpdate.setText(reversedText);
	    }

		@Override
		protected void onPreExecute() {		
			super.onPreExecute();
			dialog.setMessage(getStringResourceByName("download_message"));
			dialog.show();			
		}

		@Override
		protected List<Contact> doInBackground(String... params) {
			List<Contact> result = new ArrayList<Contact>();
			
			try {
				String u = new String(params[0]);
				
				HttpGet httpget = new HttpGet(u);
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				Content = Client.execute(httpget, responseHandler);									
				Content = Content.substring(1);								
								
				JSONObject getJSON = new JSONObject(Content);
				String isParent = getJSON.getString("parent");				
				
				if(isListing) { 
				String getID = getJSON.getString("pid");	
				postID = getID; 				
				}
				
				appTitle = getJSON.getString("title");
								
				if(isParent.equals("parent")){
				JSONArray arr = getJSON.getJSONArray("childs");
				countList = arr.length();
				for (int i=0; i < countList; i++) {
					JSONObject JSONObject = arr.getJSONObject(i);
					Contact Contact = convertContact(JSONObject);
					result.add(Contact);
				 }					
				 return result;
				} else if (isParent.equals("child")) {
					textContent = getJSON.getString("content");						
				} else {
					emptyError = true;
				}
				
			}
			catch(Throwable t) {
				t.printStackTrace();
				emptyError = true;
			}
			return null;
		}
		
		private Contact convertContact(JSONObject obj) throws JSONException {
			String name = obj.getString("name");
			String Comment = obj.getString("Comment");
			String Image = obj.getString("Image");
			String ID = obj.getString("ID");
			
			return new Contact(name, Comment, Image, ID);
		}
		
		private class MyImageGetter implements Html.ImageGetter{

            @Override
            public Drawable getDrawable(String source) {               
            	
                try {
                	URL url = new URL(source);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BitmapDrawable dr = new BitmapDrawable(getResources(), BitmapFactory.decodeStream(is));
                                        
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int screenWidth = displaymetrics.widthPixels - (int)20;
                    int pictureWidth =  dr.getIntrinsicWidth();
                    int pictureHeight = dr.getIntrinsicHeight();
                    int newHeight = (int) ((float)pictureHeight*((float)screenWidth/(float)pictureWidth));     
                                                      
                    dr.setBounds(0, 0, screenWidth, newHeight);
                    
                    
                    return dr;
                    
                } catch (IOException e) {
                	
                    e.printStackTrace();
                                        
                    int resId = getResources().getIdentifier("connection_error", "drawable", getPackageName());
                    Drawable dr = MainActivity.this.getResources().getDrawable(resId);
                    
                    DisplayMetrics displaymetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                    int screenWidth = displaymetrics.widthPixels - (int)20;
                    int pictureWidth =  dr.getIntrinsicWidth();
                    int pictureHeight = dr.getIntrinsicHeight();
                    int newHeight = (int) ((float)pictureHeight*((float)screenWidth/(float)pictureWidth));
                    
                    dr.setBounds(0, 0, screenWidth, newHeight); 
                    return dr;
                    
                }   
            }   
        }
    	
    }
       
    public String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
      }   
    
} 

