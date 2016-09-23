package cat.irec.photonbuster;

import java.util.ArrayList;
import java.util.List;

import cat.irec.photonbuster.MySQLiteHelper;
import cat.irec.photonbuster.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentThree extends Fragment {

	ImageView ivIcon;
	TextView tvItemName;

	private Spinner spinner1;
	private Button btnSubmit;
	private EditText ipaddress, port, name;

	View view;
/*
	public static final String IMAGE_RESOURCE_ID = "iconResourceID";
	public static final String ITEM_NAME = "itemName";
*/
	public FragmentThree() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_layout_three, container, false);
/*
		ivIcon = (ImageView) view.findViewById(R.id.frag3_icon);
		tvItemName = (TextView) view.findViewById(R.id.frag3_text);

		tvItemName.setText(getArguments().getString(ITEM_NAME));
		ivIcon.setImageDrawable(view.getResources().getDrawable(getArguments().getInt(IMAGE_RESOURCE_ID)));
*/
		name = (EditText) view.findViewById(R.id.editText1);
		port = (EditText) view.findViewById(R.id.editText2);
		ipaddress = (EditText) view.findViewById(R.id.editText3);

		spinner1 = (Spinner) view.findViewById(R.id.spinner1);
		List<String> list = new ArrayList<String>();
		list.add("LUMINARIA");
		list.add("PHOTOSPECTROMETER");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner1.setAdapter(dataAdapter);

		btnSubmit = (Button) view.findViewById(R.id.button1);

		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				MySQLiteHelper db = new MySQLiteHelper(view.getContext());

				db.addDevice(new Device(name.getText().toString(), ipaddress.getText().toString(), Integer.parseInt(port.getText().toString()), spinner1.getSelectedItemPosition()));

				Toast.makeText(	view.getContext(), "On Button Click : " + "\n"
						        + String.valueOf(spinner1.getSelectedItem())
								+ "\n" + ipaddress.getText().toString() + "\n"
								+ port.getText().toString() + "\n"
								+ name.getText().toString(), Toast.LENGTH_LONG).show();
			}
		});

		return view;
	}
}
