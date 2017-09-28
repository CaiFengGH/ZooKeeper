package caifeng.balance.client;

import java.util.List;

public abstract class AbstractBalanceProvider<T> implements BalanceProvider<T> {
	//从负载因子列表中找到最小的实现
	protected abstract T balanceAlgorithm(List<T> items);
	//获取所有的工作服务器的负载因子
	protected abstract List<T> getBalanceItems();
	
	public T getBalanceItem(){
		return balanceAlgorithm(getBalanceItems());
	}
}
