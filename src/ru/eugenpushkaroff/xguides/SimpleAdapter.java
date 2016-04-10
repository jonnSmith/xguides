package ru.eugenpushkaroff.xguides;

import java.util.List;
import ru.eugenpushkaroff.xguides.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SimpleAdapter extends ArrayAdapter<Contact> {
	
	private List<Contact> itemList;
	private Context context;
	final MainActivity activity;
	private String imageURL;
	private ImageView icon;
	public ImageLoader imageLoader;
	
		
	public SimpleAdapter(MainActivity activity, List<Contact> itemList, Context ctx) {
		super(ctx, android.R.layout.simple_list_item_1, itemList);
		this.itemList = itemList;
		this.context = ctx;	
        this.activity = activity;
        imageLoader=new ImageLoader(activity.getApplicationContext());
	}
	
	public int getCount() {
		if (itemList != null)
			return itemList.size();
		return 0;
	}

	public Contact getItem(int position) {
		if (itemList != null)
			return itemList.get(position);
		return null;
	}

	public long getItemId(int position) {
		if (itemList != null)
			return itemList.get(position).hashCode();
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_item, null);
		}
		
		Contact c = itemList.get(position);
		
		String ID = c.getID();

		v.setClickable(true);
		v.setTag(ID);
		v.setOnClickListener(myClickListener);
		
		
		TextView text = (TextView) v.findViewById(R.id.name);
		text.setText(c.getName());
		
		TextView text2 = (TextView) v.findViewById(R.id.comment);
		text2.setText(c.getComment());
		
		icon = (ImageView) v.findViewById(R.id.icon);		
		imageURL = c.getImage();
		
		imageLoader.DisplayImage(imageURL, icon);
		
		
		
		return v;
		
	}
	
	
	public OnClickListener myClickListener = new OnClickListener() {
		public void onClick(View v) {			
			String Message =  (String)v.getTag();
			activity.setList(Message);       
		    }
		};

	public List<Contact> getItemList() {
		return itemList;
	}

	public void setItemList(List<Contact> itemList) {
		this.itemList = itemList;
	}
	
	
}
