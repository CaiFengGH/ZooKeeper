package caifeng.queue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

public class DistributedSimpleQueue<T> {
	//客户端
	protected final ZkClient zkClient;
	//队列的根节点
	protected final String root;

	protected static final String Node_NAME = "n_";

	public DistributedSimpleQueue(ZkClient zkClient, String root) {
		this.zkClient = zkClient;
		this.root = root;
	}

	/**
	 * @desc 队列大小
	 */
	public int size() {
		return zkClient.getChildren(root).size();
	}

	/**
	 * @desc 队列是否为空
	 */
	public boolean isEmpty() {
		return zkClient.getChildren(root).size() == 0;
	}
	
    /**
     * @desc 创建节点 
     */
    public boolean offer(T element) throws Exception{
    	String nodeFullPath = root .concat( "/" ).concat( Node_NAME );
        try {
        	//创建持久的顺序节点
            zkClient.createPersistentSequential(nodeFullPath , element);
        }catch (ZkNoNodeException e) {
        	zkClient.createPersistent(root);
        	offer(element);
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
        return true;
    }

	@SuppressWarnings("unchecked")
	public T poll() throws Exception {
		try {
			List<String> list = zkClient.getChildren(root);
			if (list.size() == 0) {
				return null;
			}
			//根据节点编号进行排序
			Collections.sort(list, new Comparator<String>() {
				public int compare(String lhs, String rhs) {
					return getNodeNumber(lhs, Node_NAME).compareTo(getNodeNumber(rhs, Node_NAME));
				}
			});
			for ( String nodeName : list ){
				
				String nodeFullPath = root.concat("/").concat(nodeName);	
				try {
					T node = (T) zkClient.readData(nodeFullPath);
					zkClient.delete(nodeFullPath);
					return node;
				} catch (ZkNoNodeException e) {
					// ignore
				}
			}
			return null;
		} catch (Exception e) {
			throw ExceptionUtil.convertToRuntimeException(e);
		}
	}

	/**
	 * @desc 获取节点编号
	 */
	private String getNodeNumber(String str, String nodeName) {
		int index = str.lastIndexOf(nodeName);
		if (index >= 0) {
			index += Node_NAME.length();
			return index <= str.length() ? str.substring(index) : "";
		}
		return str;
	}
}
