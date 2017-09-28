package caifeng.testZkClient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.data.Stat;

public class GetData {

	/**
	 * @desc 获取节点的状态信息
	 * @param args
	 */
	public static void main(String[] args) {
		
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		
		//为获得节点的状态信息
		Stat stat = new Stat();
		//序列器将底层数组转换为对象
		User u = zc.readData("/jike5",stat);
		System.out.println(u.toString());
		System.out.println(stat);
	}
}
