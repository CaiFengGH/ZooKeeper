package caifeng.lock;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SimpleDistributedLockMutex extends BaseDistributedLock implements
		DistributedLock {
	
	//锁名称前缀，成功创建的顺序节点名称
	private static final String LOCK_NAME = "lock-";
	//locker节点的路径
	private final String basePath;
	//获取锁后创建的顺序节点的路径
	private String ourLockPath;
    
    /**
     * @desc 判断是否正确获取锁
     * @param time 时间
     * @param unit 时间单位
     */
    private boolean internalLock(long time, TimeUnit unit) throws Exception
    {
    	ourLockPath = attemptLock(time, unit);
        return ourLockPath != null;
    }
    
    public SimpleDistributedLockMutex(ZkClientExt client, String basePath){
    	super(client,basePath,LOCK_NAME);
    	this.basePath = basePath;
    }

	public void acquire() throws Exception {
		//-1代表永不释放锁
        if ( !internalLock(-1, null) )
        {
            throw new IOException("连接丢失!在路径:'"+basePath+"'下不能获取锁!");
        }
	}

	public boolean acquire(long time, TimeUnit unit) throws Exception {
		return internalLock(time, unit);
	}

	public void release() throws Exception {
		releaseLock(ourLockPath);
	}
}
