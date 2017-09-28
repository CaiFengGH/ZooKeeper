package caifeng.testZkClient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;

public class CreateNode {

	/**
	 * @desc 创建节点
	 * @param args
	 */
	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		User u = new User();
		u.setId(1);
		u.setName("test");
		//节点路径、节点对象和节点类型
		String path = zc.create("/jike5", u, CreateMode.PERSISTENT);
		//打印节点路径
		System.out.println("created path:"+path);
	}
}
