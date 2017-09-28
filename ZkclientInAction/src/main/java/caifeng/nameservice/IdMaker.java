package caifeng.nameservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

public class IdMaker {
	
	private ZkClient client = null;
	//服务器地址
	private final String server;
	//顺序节点的根
	private final String root;
	//节点名称
	private final String nodeName;
	//服务器是否正在运行
	private volatile boolean running = false;
	//
	private ExecutorService cleanExector = null;
	//删除方式 不删除 立即删除 延时删除
	public enum RemoveMethod{
		NONE,IMMEDIATELY,DELAY
	}
	
	public IdMaker(String zkServer,String root,String nodeName){
		this.root = root;
		this.server = zkServer;
		this.nodeName = nodeName;
	}
	
	/**
	 * @desc 启动服务
	 */
	public void start() throws Exception {
		if (running)
			throw new Exception("server has stated...");
		running = true;
		init();
	}
	
	/**
	 * @desc 停止服务
	 */
	public void stop() throws Exception {
		if (!running)
			throw new Exception("server has stopped...");
		running = false;
		freeResource();
	}
	/**
	 * @desc 初始化
	 */
	private void init(){
		client = new ZkClient(server,5000,5000,new BytesPushThroughSerializer());
		cleanExector = Executors.newFixedThreadPool(10);
		try{
			client.createPersistent(root,true);
		}catch (ZkNodeExistsException e){
			//ignore;
		}
	}
	
	/**
	 * @desc 释放服务器资源
	 */
	private void freeResource(){
		//关闭删除的线程池
		cleanExector.shutdown();
		try{
			//
			cleanExector.awaitTermination(2, TimeUnit.SECONDS);
		}catch(InterruptedException e){
			e.printStackTrace();
		}finally{
			cleanExector = null;
		}
	
		if (client!=null){
			client.close();
			client=null;
		}
	}
	
	/**
	 * @desc 检测是否正在运行 
	 */
	private void checkRunning() throws Exception {
		if (!running)
			throw new Exception("请先调用start");
	}
	
	private String ExtractId(String str){
		int index = str.lastIndexOf(nodeName);
		if (index >= 0){
			index+=nodeName.length();
			return index <= str.length()?str.substring(index):"";
		}
		return str;
	}
	
	/**
	 * @desc 生成ID
	 */
	public String generateId(RemoveMethod removeMethod) throws Exception{
		//在服务的运行台下面调用
		checkRunning();
		//构造顺序节点的完整路径
		final String fullNodePath = root.concat("/").concat(nodeName);
		//创建持久的顺序节点
		final String ourPath = client.createPersistentSequential(fullNodePath, null);
		
		if (removeMethod.equals(RemoveMethod.IMMEDIATELY)){
			client.delete(ourPath);
		}else if (removeMethod.equals(RemoveMethod.DELAY)){
			cleanExector.execute(new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
					client.delete(ourPath);
				}
			});
		}
		//node-0000000000, node-0000000001
		return ExtractId(ourPath);
	}
}
