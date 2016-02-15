package com.ubudu.sampleapp.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ubudu.sampleapp.R;

import java.util.ArrayList;

public class LogListAdapter extends ArrayAdapter<String> {

	private ArrayList<String> msgs = new ArrayList<String>();

	@Override
	public void add(String object) {
		msgs.add(object);
		super.add(object);
	}

	public void putLogs(ArrayList<String> m){
		msgs.clear();
		msgs.addAll(m);
	}
	
	public LogListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	
	public int getCount() {
		return this.msgs.size();
	}

	public String getItem(int index) {
		return this.msgs.get(index);
	}

	public void recoverMsgs(ArrayList<String> c){
		msgs = c;
	}
	
	public ArrayList<String> getElements(){
		return msgs;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.loglistitem, parent, false);
		}

	    TextView logmsg = (TextView) view.findViewById(R.id.text);
	    String msgbx = getItem(position);
	    logmsg.setText(msgbx);

	    return view;
	}

}