package one.inve.cfg.core;

import java.util.List;

public interface IFullnodeConf {
	String getPubIP();

	String getGossipPort();

	String getRpcPort();

	String getHttpPort();

	String getPrefix();

	String getShardSize();

	String getShardNodeSize();

	String getStatic();

	List<String> getWhitelist();

}
