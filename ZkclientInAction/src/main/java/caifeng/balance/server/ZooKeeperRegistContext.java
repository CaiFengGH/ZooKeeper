package caifeng.balance.server;

import org.I0Itec.zkclient.ZkClient;

/**
 * @author Ethan
 * @desc 基于zookeeper的上下文环境
 */
public class ZooKeeperRegistContext {
	
	//zookeeper的路径
	private String path;
	//客户端
	private ZkClient zkClient;
	//数据
	private Object data;
	
	public ZooKeeperRegistContext(String path, ZkClient zkClient, Object data) {
		super();
		this.path = path;
		this.zkClient = zkClient;
		this.data = data;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public ZkClient getZkClient() {
		return zkClient;
	}
	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
}
