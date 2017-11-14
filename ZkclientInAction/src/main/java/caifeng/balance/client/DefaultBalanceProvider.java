package caifeng.balance.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import caifeng.balance.server.ServerData;

/**
 * @author Ethan
 * @desc 此处的负载均衡算法，只是简单的获得最小的负载因子的实现
 */
public class DefaultBalanceProvider extends AbstractBalanceProvider<ServerData> {
	
	private final String zkServer;
	private final String serversPath;
	private final ZkClient zc;
	
	private static final Integer SESSION_TIME_OUT = 10000;
	private static final Integer CONNECT_TIME_OUT = 10000;

	public DefaultBalanceProvider(String zkServer, String serversPath) {
		this.serversPath = serversPath;
		this.zkServer = zkServer;
		//创建zkClient
		this.zc = new ZkClient(this.zkServer, SESSION_TIME_OUT, CONNECT_TIME_OUT,
				new SerializableSerializer());
	}

	@Override
	protected ServerData balanceAlgorithm(List<ServerData> items) {
		// TODO Auto-generated method stub
		if (items.size()>0){
			Collections.sort(items);
			return items.get(0);
		}else{
			return null;
		}
	}

	@Override
	protected List<ServerData> getBalanceItems() {
		// TODO Auto-generated method stub
		List<ServerData> sdList = new ArrayList<ServerData>();
		List<String> children = zc.getChildren(this.serversPath);
		for(int i=0; i<children.size();i++){
			ServerData sd = zc.readData(serversPath+"/"+children.get(i));
			sdList.add(sd);
		}		
		return sdList;
	}
}
