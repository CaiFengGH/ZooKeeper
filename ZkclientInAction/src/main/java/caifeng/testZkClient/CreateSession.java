package caifeng.testZkClient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class CreateSession {

	/**
	 * @desc 测试ZkClient的创建会话功能 
	 * @param args
	 */
	public static void main(String[] args) {
		//第四个参数需要序列化器，使用自带的序列化器		
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
	}
}
