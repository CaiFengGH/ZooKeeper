package caifeng.mastersel;

import java.io.Serializable;

/**
 * @author Ethan
 * @desc 描述workerServer的信息
 */
public class RunningData implements Serializable {

	//可序列化
	private static final long serialVersionUID = 4260577459043203630L;
	//基本信息
	private Long cid;
	private String name;

	public Long getCid() {
		return cid;
	}
	public void setCid(Long cid) {
		this.cid = cid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
