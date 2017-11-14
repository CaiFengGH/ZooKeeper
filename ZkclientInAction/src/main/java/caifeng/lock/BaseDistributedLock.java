package caifeng.lock;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

/**
 * @author Ethan
 * @desc zookeeper实现分布式锁的细节
 * 实现过程：
 * 1、在lock节点下创建临时顺序节点
 * 2、获取lock节点的顺序子节点
 * 3、判断当前节点是不是顺序子节点中最小的节点
 * 4、如果是，则直接获取锁，不是则订阅次小节点的节点删除事件
 * 5、次小节点删除后，重新从第2步开始执行
 */
public class BaseDistributedLock {
	
    private final ZkClientExt client;
    //创建的锁节点的路径
    private final String  path;
    //lock节点的路径
    private final String  basePath;
    //锁前缀
    private final String  lockName;
    private static final Integer  MAX_RETRY_COUNT = 10;
    	
	public BaseDistributedLock(ZkClientExt client, String path, String lockName){

        this.client = client;
        this.basePath = path;
        //lockName是锁前缀
        this.path = path.concat("/").concat(lockName);		
		this.lockName = lockName;
	}
	
	/**
	 * @desc 删除占有锁时的顺序节点的路径
	 */
	private void deleteOurPath(String ourPath) throws Exception{
		client.delete(ourPath);
	}
	
	private String createLockNode(ZkClient client,  String path) throws Exception{
		//客户端创建顺序节点
		return client.createEphemeralSequential(path, null);
	}
	
	private boolean waitToLock(long startMillis, Long millisToWait, String ourPath) throws Exception{
        
		boolean  haveTheLock = false;
        boolean  doDelete = false;
        
        try
        {
 
            while ( !haveTheLock )
            {
            	//获取已经排序的顺序节点列表
                List<String> children = getSortedChildren();
                //获取当前顺序节点的编号
                String sequenceNodeName = ourPath.substring(basePath.length()+1);

                int  ourIndex = children.indexOf(sequenceNodeName);
                if ( ourIndex<0 ){
                	throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);
                }
                //获取期待监视的锁
                boolean isGetTheLock = ourIndex == 0;
                String  pathToWatch = isGetTheLock ? null : children.get(ourIndex - 1);
                
                if ( isGetTheLock ){
                	
                    haveTheLock = true;
                    
                }else{
                	//监听小于自己的次节点
                    String  previousSequencePath = basePath .concat( "/" ) .concat( pathToWatch );
                    //在latch上面进行等待
                    final CountDownLatch    latch = new CountDownLatch(1);
                    final IZkDataListener previousListener = new IZkDataListener() {
                		
                		public void handleDataDeleted(String dataPath) throws Exception {
                			latch.countDown();			
                		}
                		
                		public void handleDataChange(String dataPath, Object data) throws Exception {
                			// ignore									
                		}
                	};

                    try 
                    {                  
						//如果节点不存在会出现异常
                    	client.subscribeDataChanges(previousSequencePath, previousListener);
                    	
                        if ( millisToWait != null )
                        {
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            startMillis = System.currentTimeMillis();
                            if ( millisToWait <= 0 )
                            {
                                doDelete = true;    // timed out - delete our node
                                break;
                            }

                            latch.await(millisToWait, TimeUnit.MICROSECONDS);
                        }
                        else
                        {
                        	latch.await();
                        }
                    }
                    catch ( ZkNoNodeException e ) 
                    {
                        //ignore
                    }finally{
                    	client.unsubscribeDataChanges(previousSequencePath, previousListener);
                    }
                }
            }
        }
        catch ( Exception e )
        {
            //发生异常需要删除节点
            doDelete = true;
            throw e;
        }
        finally
        {
            //如果需要删除节点
            if ( doDelete )
            {
                deleteOurPath(ourPath);
            }
        }
        return haveTheLock;
	}
	
    private String getLockNodeNumber(String str, String lockName)
    {
        int index = str.lastIndexOf(lockName);
        if ( index >= 0 )
        {
            index += lockName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }
	
    /**
     * @desc 获取排序好的顺序节点
     */
    List<String> getSortedChildren() throws Exception
    {
    	try{
	        List<String> children = client.getChildren(basePath);
	        Collections.sort
	        (
	        	children,
	            new Comparator<String>()
	            {
	                public int compare(String lhs, String rhs)
	                {
	                    return getLockNodeNumber(lhs, lockName).compareTo(getLockNodeNumber(rhs, lockName));
	                }
	            }
	        );
	        return children;
    	}catch(ZkNoNodeException e){
    		client.createPersistent(basePath, true);
    		return getSortedChildren();
    	}
    }
	
	/**
	 * @desc 释放锁
	 */
	protected void releaseLock(String lockPath) throws Exception{
		deleteOurPath(lockPath);	
	}
	
	/**
	 * @desc 获取锁
	 */
	protected String attemptLock(long time, TimeUnit unit) throws Exception{
		
        final long      startMillis = System.currentTimeMillis();
        final Long      millisToWait = (unit != null) ? unit.toMillis(time) : null;

        String          ourPath = null;
        boolean         hasTheLock = false;
        boolean         isDone = false;
        int             retryCount = 0;
        
        //网络闪断需要重试一试
        while ( !isDone )
        {
            isDone = true;
            try
            {
            	//创建临时顺序子节点
                ourPath = createLockNode(client, path);
                //判断自己是否获取锁，没有获得则等待直到获得
                hasTheLock = waitToLock(startMillis, millisToWait, ourPath);
            }
            catch ( ZkNoNodeException e )
            {
            	//重试指定的次数
                if ( retryCount++ < MAX_RETRY_COUNT )
                {
                    isDone = false;
                }
                else
                {
                    throw e;
                }
            }
        }
        //成功获取锁，返回锁路径
        if ( hasTheLock )
        {
            return ourPath;
        }
        return null;
	}
}
