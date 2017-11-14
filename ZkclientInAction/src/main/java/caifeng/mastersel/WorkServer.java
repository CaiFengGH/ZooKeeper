package caifeng.mastersel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

/**
 * @author Ethan
 * @desc 工作服务器
 * master释放的三种情况：
 * 1、master主动释放
 * 2、master节点对应的服务器宕机
 * 3、网络抖动，优化策略是优先选取上次的master节点对应的服务器
 */
public class WorkServer {
	//服务器自身运行状态
	private volatile boolean running = false;
	//zkclient客户端
	private ZkClient zkClient;
	//master节点路径
	private static final String MASTER_PATH = "/master";
	//数据监听
	private IZkDataListener dataListener;
	//服务器自身数据
	private RunningData serverData;
	//master数据
	private RunningData masterData;
	
	//创建调度器，进行网络抖动的优化
	private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);
	private int delayTime = 5;

	public WorkServer(RunningData rd) {
		this.serverData = rd;
		this.dataListener = new IZkDataListener() {
			
			//为了避免上一个master不能成为新的master，使用调度器
			public void handleDataDeleted(String dataPath) throws Exception {
				// TODO Auto-generated method stub
				//takeMaster();
				//避免资源迁移带来的损耗，如果是则争抢权力，如果不是则延迟5秒争抢
				if (masterData!=null && masterData.getName().equals(serverData.getName())){
					takeMaster();
				}else{
					delayExector.schedule(new Runnable(){
						public void run(){
							takeMaster();
						}
					}, delayTime, TimeUnit.SECONDS);
				}
			}
			
			public void handleDataChange(String dataPath, Object data)
					throws Exception {
				// TODO Auto-generated method stub
			}
		};
	}
	/**
	 * @desc 获取zkclient客户端
	 */
	public ZkClient getZkClient() {
		return zkClient;
	}

	/**
	 * @desc 设置zkClient客户端
	 */
	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	/**
	 * @desc 开始服务
	 */
	public void start() throws Exception {
		if (running) {
			throw new Exception("server has startup...");
		} 
		running = true;
		//订阅master节点的删除事件
		zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
		//争抢master权力
		takeMaster();
	}

	/**
	 * @desc 关闭服务
	 */
	public void stop() throws Exception {
		if (!running) {
			throw new Exception("server has stoped");
		}
		running = false;
		
		delayExector.shutdown();
		//取消master节点订阅
		zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
		//释放master权力
		releaseMaster();
	}

	/**
	 * @desc 增强master
	 */
	private void takeMaster() {
		if (!running)
			return;

		try {
			//创建临时节点信息
			zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
			masterData = serverData;
			
			System.out.println(serverData.getName()+" is master");
			//演示每隔5秒中释放master
			delayExector.schedule(new Runnable() {			
				public void run() {
					// TODO Auto-generated method stub
					if (checkMaster()){
						releaseMaster();
					}
				}
			}, 5, TimeUnit.SECONDS);
		} catch (ZkNodeExistsException e) {
			//创建master失败，读取master数据
			RunningData runningData = zkClient.readData(MASTER_PATH, true);
			//读取master数据时，master被删除，重新争取master资源
			if (runningData == null) {
				takeMaster();
			} else {
				masterData = runningData;
			}
		} catch (Exception e) {
			// ignore;
		}
	}
	
	/**
	 * @desc 释放master节点
	 */
	private void releaseMaster() {
		if (checkMaster()) {
			//如果是master，直接删除资源
			zkClient.delete(MASTER_PATH);
		}
	}

	/**
	 * @desc 检测自己是否是master
	 */
	private boolean checkMaster() {
		try {
			RunningData eventData = zkClient.readData(MASTER_PATH);
			masterData = eventData;
			//master路径下面的数据和自身数据是否一致
			if (masterData.getName().equals(serverData.getName())) {
				return true;
			}
			return false;
		} catch (ZkNoNodeException e) {
			return false;
		} catch (ZkInterruptedException e) {
			//中断则继续进行重试
			return checkMaster();
		} catch (ZkException e) {
			return false;
		}
	}
}
