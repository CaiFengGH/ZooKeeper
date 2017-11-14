package caifeng.subscribe;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import com.alibaba.fastjson.JSON;

/**
 * @author Ethan
 * @desc 工作服务器
 */
public class WorkServer {
	//客户端
	private ZkClient zkClient;
	//config的路径
	private String configPath;
	//服务器的注册路径
	private String serversPath;
	//服务器的数据
	private ServerData serverData;
	//服务器的初始配置
	private ServerConfig serverConfig;
	//时间监听器
	private IZkDataListener dataListener;

	public WorkServer(String configPath, String serversPath,
			ServerData serverData, ZkClient zkClient, ServerConfig initConfig) {
		this.zkClient = zkClient;
		this.serversPath = serversPath;
		this.configPath = configPath;
		this.serverConfig = initConfig;
		this.serverData = serverData;

		this.dataListener = new IZkDataListener() {

			public void handleDataDeleted(String dataPath) throws Exception {
				// TODO Auto-generated method stub
			}

			public void handleDataChange(String dataPath, Object data)
					throws Exception {
				// TODO Auto-generated method stub
				String retJson = new String((byte[])data);
				//反序列化对象
				ServerConfig serverConfigLocal = (ServerConfig)JSON.parseObject(retJson,ServerConfig.class);
				//更新本地的配置
				updateConfig(serverConfigLocal);
				System.out.println("new Work server config is:"+serverConfig.toString());
			}
		};
	}

	/**
	 * @desc 开启服务
	 */
	public void start() {
		System.out.println("work server start...");
		initRunning();
	}

	/**
	 * @desc 取消服务
	 */
	public void stop() {
		System.out.println("work server stop...");
		zkClient.unsubscribeDataChanges(configPath, dataListener);
	}

	/**
	 * @desc 初始化配置
	 */
	private void initRunning() {
		//注册信息
		registMe();
		zkClient.subscribeDataChanges(configPath, dataListener);
	}

	/**
	 * @desc 注册
	 */
	private void registMe() {
		String mePath = serversPath.concat("/").concat(serverData.getAddress());
		try {
			//创建临时节点，将服务器数据进行序列化
			zkClient.createEphemeral(mePath, JSON.toJSONString(serverData)
					.getBytes());
		} catch (ZkNoNodeException e) {
			//创建serverPath，然后再次进行注册
			zkClient.createPersistent(serversPath, true);
			registMe();
		}
	}

	/**
	 * @desc 更新本地的服务器配置
	 */
	private void updateConfig(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
}
