package caifeng.subscribe;

/**
 * @author Ethan
 * @desc 服务器属性
 */
public class ServerData {
	
	private Integer id;
	private String address;
	private String name;
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "ServerData [address=" + address + ", id=" + id + ", name="
				+ name + "]";
	}
}
