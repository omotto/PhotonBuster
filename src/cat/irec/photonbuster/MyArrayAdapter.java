package cat.irec.photonbuster;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import cat.irec.photonbuster.R;

public class MyArrayAdapter extends ArrayAdapter<Device> {

	private final int resource;
	private final List<Device> values;

	private final Activity context;

	public MyArrayAdapter(Activity context, int resource, List<Device> values) {
		super(context, resource, values);
		this.resource = resource;
		this.context = context;
		this.values = values;
	}

	static class ViewHolder {
		ImageView icon;
		CheckBox checkbox;
		TextView name;
		TextView ip;
		TextView port;
	}

	public Device getItem(int i) {
		return values.get(i);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(resource, null);
			final ViewHolder viewHolder = new ViewHolder();
			// --
			viewHolder.name = (TextView) view.findViewById(R.id.name);
			viewHolder.ip = (TextView) view.findViewById(R.id.ip);
			viewHolder.port = (TextView) view.findViewById(R.id.port);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
			viewHolder.icon = (ImageView) view.findViewById(R.id.logo);
			// --
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Device device = (Device) viewHolder.checkbox.getTag();
					if (buttonView.isChecked())
						device.setEnable(1);
					else
						device.setEnable(0);
					MySQLiteHelper db = new MySQLiteHelper(getContext());
					db.updateDevice(device);
				}
			});
			// --
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(values.get(position));
		} else {
			view = convertView;
			((ViewHolder) view.getTag()).checkbox.setTag(values.get(position));
		}

		ViewHolder holder = (ViewHolder) view.getTag();
		holder.name.setText(values.get(position).getName());
		holder.ip.setText(values.get(position).getIp());
		holder.port.setText(String.valueOf(values.get(position).getPort()));
		holder.icon.setImageResource(R.drawable.ic_action_labels);
		if (values.get(position).getEnable() == 1)
			holder.checkbox.setChecked(true);
		else
			holder.checkbox.setChecked(false);
		/*
		 * nameTextView.setText(values.get(position).getName());
		 * ipTextView.setText(values.get(position).getIp());
		 * portTextView.setText(String.valueOf(values.get(position).getPort()));
		 * // -- // Change the icon
		 * 
		 * 
		 * checkbox = (CheckBox) rowView.findViewById(R.id.check);
		 * 
		 * int enable = values.get(position).getEnable();
		 * 
		 * if (enable == 0) {
		 * imageView.setImageResource(R.drawable.ic_action_labels);
		 * checkbox.setChecked(false); } else {
		 * imageView.setImageResource(R.drawable.ic_action_help);
		 * checkbox.setChecked(true); }
		 */
		return view;
	}
}