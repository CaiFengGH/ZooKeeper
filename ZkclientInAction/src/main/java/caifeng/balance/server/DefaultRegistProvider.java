package caifeng.balance.server;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

public class DefaultRegistProvider implements RegistProvider {

	public void regist(Object context) throws Exception {
		//将下面内容封装在上下文环境中
		// 1:path
		// 2:zkClient
		// 3:serverData
		ZooKeeperRegistContext registContext = (ZooKeeperRegistContext) context;
		String path = registContext.getPath();
		ZkClient zc = registContext.getZkClient();

		try {
			//创建临时节点
			zc.createEphemeral(path, registContext.getData());
		} catch (ZkNoNodeException e) {
			//父节点不存在，先创建父节点的数据
			String parentDir = path.substring(0, path.lastIndexOf('/'));
			zc.createPersistent(parentDir, true);
			regist(registContext);
		}
	}

	public void unRegist(Object context) throws Exception {
		// TODO Auto-generated method stub
		return;
	}
}
