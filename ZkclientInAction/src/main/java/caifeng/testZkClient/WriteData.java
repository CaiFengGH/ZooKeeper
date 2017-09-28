package caifeng.testZkClient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class WriteData {
	/**
	 * @desc 对象的写入
	 * @param args
	 */
	public static void main(String[] args) {
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		User u = new User();
		u.setId(2);
		u.setName("test2");
		//序列化器将user对象转换为底层的字节数组，1为对象版本号
		zc.writeData("/jike5", u, 1);
	}
}
