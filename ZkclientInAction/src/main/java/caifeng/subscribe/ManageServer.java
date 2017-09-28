package caifeng.subscribe;

import java.util.List;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import com.alibaba.fastjson.JSON;

public class ManageServer {
	//服务器注册路径
	private String serversPath;
	//command路径，模拟controlServer的command
	private String commandPath;
	//config的属性
	private String configPath;
	//zkClient的客户端
	private ZkClient zkClient;
	//config的基本配置
	private ServerConfig config;
	//注册服务器的变化
	private IZkChildListener childListener;
	//监听command数据变化
	private IZkDataListener dataListener;
	//自身的服务器列表
	private List<String> workServerList;

	public ManageServer(String serversPath, String commandPath,
			String configPath, ZkClient zkClient, ServerConfig config) {
		this.serversPath = serversPath;
		this.commandPath = commandPath;
		this.zkClient = zkClient;
		this.config = config;
		this.configPath = configPath;
		this.childListener = new IZkChildListener() {

			public void handleChildChange(String parentPath,
					List<String> currentChilds) throws Exception {
				// TODO Auto-generated method stub
				workServerList = currentChilds;
				System.out.println("work server list changed, new list is ");
				//服务器列表变化
				execList();
			}
		};
		this.dataListener = new IZkDataListener() {

			public void handleDataDeleted(String dataPath) throws Exception {
				// TODO Auto-generated method stub
				// ignore;
			}

			public void handleDataChange(String dataPath, Object data)
					throws Exception {
				// TODO Auto-generated method stub
				String cmd = new String((byte[]) data);
				System.out.println("cmd:"+cmd);
				//执行最新的指令
				exeCmd(cmd);
			}
		};
	}

	/**
	 * @desc 初始化
	 */
	private void initRunning() {
		zkClient.subscribeDataChanges(commandPath, dataListener);
		zkClient.subscribeChildChanges(serversPath, childListener);
	}

	/*
	 * 1: list 列出工作服务器 
	 * 2: create 创建config节点
	 * 3: modify 修改服务器类型
	 */
	private void exeCmd(String cmdType) {
		if ("list".equals(cmdType)) {
			execList();
		} else if ("create".equals(cmdType)) {
			execCreate();
		} else if ("modify".equals(cmdType)) {
			execModify();
		} else {
			System.out.println("error command!" + cmdType);
		}
	}

	/**
	 * @desc 查看已经工作的服务器列表
	 */
	private void execList() {
		System.out.println(workServerList.toString());
	}

	/**
	 * @desc 
	 */
	private void execCreate() {
		if (!zkClient.exists(configPath)) {
			try {
				//config持久节点
				zkClient.createPersistent(configPath, JSON.toJSONString(config)
						.getBytes());
			} catch (ZkNodeExistsException e) {
				//节点已经存在，直接写入内容
				zkClient.writeData(configPath, JSON.toJSONString(config)
						.getBytes());
			} catch (ZkNoNodeException e) {
				//父节点未被创建
				String parentDir = configPath.substring(0,
						configPath.lastIndexOf('/'));
				zkClient.createPersistent(parentDir, true);
				execCreate();
			}
		}
	}

	/**
	 * @desc 修改操作
	 */
	private void execModify() {
		//
		config.setDbUser(config.getDbUser() + "_modify");
		try {
			zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
		} catch (ZkNoNodeException e) {
			//写入时，config节点不存在
			execCreate();
		}
	}

	/**
	 * @desc 开启服务
	 */
	public void start() {
		initRunning();
	}

	/**
	 * @desc 关闭服务
	 */
	public void stop() {
		zkClient.unsubscribeChildChanges(serversPath, childListener);
		zkClient.unsubscribeDataChanges(commandPath, dataListener);
	}
}
