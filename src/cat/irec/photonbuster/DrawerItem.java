package cat.irec.photonbuster;

public class DrawerItem {

	String ItemName;
	int imgResID;

	int img;
	String title;

	public DrawerItem(String itemName, int imgResID) {
		ItemName = itemName;
		this.imgResID = imgResID;
		this.title = null;
		this.img = 0;
	}

	public DrawerItem(String title) {
		this(null, 0);
		this.title = title;
	}

	public DrawerItem(int img) {
		this(null, 0);
		this.img = img;
	}

	public String getItemName() {
		return ItemName;
	}

	public void setItemName(String itemName) {
		ItemName = itemName;
	}

	public int getImgResID() {
		return imgResID;
	}

	public void setImgResID(int imgResID) {
		this.imgResID = imgResID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getImg() {
		return img;
	}

	public void setImg(int img) {
		this.img = img;
	}
}
