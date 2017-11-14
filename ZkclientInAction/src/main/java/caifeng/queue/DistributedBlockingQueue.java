package caifeng.queue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * @author Ethan
 * @desc 阻塞式的分布式队列
 */
public class DistributedBlockingQueue<T> extends DistributedSimpleQueue<T>{      
    
    public DistributedBlockingQueue(ZkClient zkClient, String root) {
    	super(zkClient, root);
	}

    @Override
	public T poll() throws Exception {

		while (true){
			
			final CountDownLatch    latch = new CountDownLatch(1);
			final IZkChildListener childListener = new IZkChildListener() {
				
				public void handleChildChange(String parentPath, List<String> currentChilds)
						throws Exception {
					latch.countDown();
				}
			}; 
			zkClient.subscribeChildChanges(root, childListener);
			try{
				T node = super.poll();
	            if ( node != null ){
	                return node;
	            }else{
	            	//直到可以消费到数据，才返回
	            	latch.await();
	            }
			}finally{
				zkClient.unsubscribeChildChanges(root, childListener);
			}
		}
	}
}
