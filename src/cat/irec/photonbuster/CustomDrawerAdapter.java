package cat.irec.photonbuster;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cat.irec.photonbuster.R;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {

	Context context;
	List<DrawerItem> drawerItemList;
	int layoutResID;

	public CustomDrawerAdapter(Context context, int layoutResourceID,
			List<DrawerItem> listItems) {
		super(context, layoutResourceID, listItems);
		this.context = context;
		this.drawerItemList = listItems;
		this.layoutResID = layoutResourceID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		DrawerItemHolder drawerHolder;
		View view = convertView;

		if (view == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			drawerHolder = new DrawerItemHolder();

			view = inflater.inflate(layoutResID, parent, false);
			drawerHolder.ItemName = (TextView) view
					.findViewById(R.id.drawer_itemName); // texto opcion
			drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon); // icono
																					// opcion
			drawerHolder.menuIcon = (ImageView) view
					.findViewById(R.id.drawer_menu_icon); // icono principal
															// menu
			drawerHolder.title = (TextView) view.findViewById(R.id.drawerTitle); // titulo

			drawerHolder.headerLayout = (LinearLayout) view
					.findViewById(R.id.headerLayout); // Layout de icono de menu
			drawerHolder.titleLayout = (LinearLayout) view
					.findViewById(R.id.titleLayout); // Layout de titulo
			drawerHolder.itemLayout = (LinearLayout) view
					.findViewById(R.id.itemLayout); // Layout del texto de
													// opcion

			view.setTag(drawerHolder);

		} else {
			drawerHolder = (DrawerItemHolder) view.getTag();

		}

		DrawerItem dItem = (DrawerItem) this.drawerItemList.get(position);

		if (dItem.getTitle() != null) {
			drawerHolder.titleLayout.setVisibility(LinearLayout.VISIBLE);
			drawerHolder.itemLayout.setVisibility(LinearLayout.INVISIBLE);
			drawerHolder.headerLayout.setVisibility(LinearLayout.INVISIBLE);

			drawerHolder.title.setText(dItem.getTitle());
		} else {
			if (dItem.getItemName() == null) {
				drawerHolder.titleLayout.setVisibility(LinearLayout.INVISIBLE);
				drawerHolder.itemLayout.setVisibility(LinearLayout.INVISIBLE);
				drawerHolder.headerLayout.setVisibility(LinearLayout.VISIBLE);

				drawerHolder.menuIcon.setImageDrawable(view.getResources()
						.getDrawable(dItem.getImg()));
			} else {
				drawerHolder.titleLayout.setVisibility(LinearLayout.INVISIBLE);
				drawerHolder.itemLayout.setVisibility(LinearLayout.VISIBLE);
				drawerHolder.headerLayout.setVisibility(LinearLayout.INVISIBLE);

				drawerHolder.icon.setImageDrawable(view.getResources()
						.getDrawable(dItem.getImgResID()));
				drawerHolder.ItemName.setText(dItem.getItemName());
			}
		}
		return view;
	}

	private static class DrawerItemHolder {
		TextView ItemName, title;
		ImageView icon, menuIcon;
		LinearLayout headerLayout, titleLayout, itemLayout;
	}
}