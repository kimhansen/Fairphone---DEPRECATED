package com.kwamecorp.fairphonepeaceofmind.components;

public class ListItem
{
	private boolean checked = false;
	private String label = "";

	public ListItem() {
	}

	public ListItem(String label, boolean checked) {

		this.checked = checked;
		this.label = label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isChecked()
	{
		return checked;
	}

	public void toggleChecked()
	{
		checked = !checked;
	}
}
