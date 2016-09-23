package cat.irec.photonbuster;

public class Device {

	private int id;
	private String name;
	private String ip;
	private int port;
	private int type;
	private int enable;

	private static final String[] STRINGS = { "Luminaria", "Espectrometro" };

	public Device() {
	}

	public Device(String name, String ip, int port, int type) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.type = type;
		this.enable = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEnable() {
		return enable;
	}

	public void setEnable(int enable) {
		this.enable = enable;
	}

	@Override
	public String toString() {
		return "Device [id=" + this.id + ", name=" + this.name + ", ip="
				+ this.ip + ", port=" + this.port + ", type="
				+ STRINGS[this.type] + "]";
	}
}
