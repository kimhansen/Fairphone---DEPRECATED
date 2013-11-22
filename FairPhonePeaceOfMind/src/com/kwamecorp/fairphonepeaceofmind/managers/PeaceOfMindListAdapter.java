package com.kwamecorp.fairphonepeaceofmind.managers;

import java.util.ArrayList;

import com.kwamecorp.fairphonepeaceofmind.R;
import com.kwamecorp.fairphonepeaceofmind.R.id;
import com.kwamecorp.fairphonepeaceofmind.R.layout;
import com.kwamecorp.fairphonepeaceofmind.components.ListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class PeaceOfMindListAdapter extends BaseAdapter
{
	private final Context context;
	private ArrayList<ListItem> itemsList;

	public PeaceOfMindListAdapter(Context context, ArrayList<ListItem> itemsList) {
		super();

		this.context = context;
		this.itemsList = itemsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View listItem = inflater.inflate(R.layout.list_item_layout, parent, false);

		TextView label = (TextView) listItem.findViewById(R.id.label);
		CheckBox checkBox = (CheckBox) listItem.findViewById(R.id.checkbox);

		label.setText(itemsList.get(position).getLabel());
		checkBox.setChecked(itemsList.get(position).isChecked());

		return listItem;
	}

	@Override
	public int getCount()
	{
		return itemsList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return itemsList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}
}
