package caifeng.subscribe;

/**
 * @author Ethan
 * @desc 服务器配置
 */
public class ServerConfig {
	//数据库地址
	private String dbUrl;
	//数据库密码
	private String dbPwd;
	//数据库用户
	private String dbUser;
	
	public String getDbUrl() {
		return dbUrl;
	}
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	public String getDbPwd() {
		return dbPwd;
	}
	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}
	public String getDbUser() {
		return dbUser;
	}
	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}
	
	@Override
	public String toString() {
		return "ServerConfig [dbUrl=" + dbUrl + ", dbPwd=" + dbPwd
				+ ", dbUser=" + dbUser + "]";
	}
}
