package caifeng.balance.server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Ethan
 * @desc 负责客户端和服务器端的通讯
 */
public class ServerHandler extends ChannelHandlerAdapter{

	private final BalanceUpdateProvider balanceUpdater;
	private static final Integer BALANCE_STEP = 1; 

    public ServerHandler(BalanceUpdateProvider balanceUpdater){
    	this.balanceUpdater = balanceUpdater;
    } 

    public BalanceUpdateProvider getBalanceUpdater() {
		return balanceUpdater;
	}	
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	System.out.println("one client connect...");
    	//连接建立时，进行负载因子自加
    	balanceUpdater.addBalance(BALANCE_STEP);
    }
	
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	//连接断开时，进行负载因子自减操作
    	balanceUpdater.reduceBalance(BALANCE_STEP);
    }

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
