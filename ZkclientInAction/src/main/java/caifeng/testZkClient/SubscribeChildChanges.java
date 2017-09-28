package caifeng.testZkClient;

import java.util.List;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

public class SubscribeChildChanges {
	
	private static class ZkChildListener implements IZkChildListener{
		public void handleChildChange(String parentPath,
				List<String> currentChilds) throws Exception {
			// TODO Auto-generated method stub
			System.out.println(parentPath);
			System.out.println(currentChilds.toString());
		}
	}

	/**
	 * @desc 子节点列表事件变化
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		ZkClient zc = new ZkClient("192.168.1.105:2181",10000,10000,new SerializableSerializer());
		System.out.println("conneted ok!");
		//将节点子节点列表事件进行添加，即使/
		zc.subscribeChildChanges("/jike20", new ZkChildListener());
		Thread.sleep(Integer.MAX_VALUE);
	}
}
