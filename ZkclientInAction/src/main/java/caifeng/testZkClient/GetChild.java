package caifeng.testZkClient;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class GetChild {
	/**
	 * @desc 获取子节点的信息
	 * @param args
	 */
	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		//获取节点的子节点信息
		List<String> cList = zc.getChildren("/jike5");
		
		System.out.println(cList.toString());
	}
}
