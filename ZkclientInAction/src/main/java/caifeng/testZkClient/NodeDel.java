package caifeng.testZkClient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class NodeDel {
	/**
	 * @desc 删除节点
	 * @param args
	 */
	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		//删除节点
		boolean flag = zc.delete("/jike5");
		//节点存在子节点循环删除
		boolean flag1 = zc.deleteRecursive("/jike5");
		System.out.println(flag);
		System.out.println(flag1);
	}
}
