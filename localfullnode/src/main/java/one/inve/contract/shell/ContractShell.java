package one.inve.contract.shell;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import one.inve.contract.Contract;
import one.inve.contract.conf.ContractConfigurable;
import one.inve.contract.shell.event.EventsLoop;
import one.inve.contract.shell.http.DefChannelInitializer;
import one.inve.http.HttpChannelInitializer;
import one.inve.node.GeneralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The entry class to have a couple of useful objects(within
 *               Shell) you can customize.
 * @author: Francis.Deng
 * @date: 2018年10月30日 下午2:24:50
 * @version: V1.0
 */
public class ContractShell {
	private static final Logger logger = LoggerFactory.getLogger(ContractShell.class);
	private ContractConfigurable configurator;
	private EventsLoop eventsLoop;
	private Thread httpServerThread;


	public ContractShell(Contract c) {
		this.configurator = c.getConfigurator();
		this.eventsLoop = new EventsLoop();
	}

	public EventsLoop getEventsLoop() {
		return eventsLoop;
	}

	public void boostrapHttpService(GeneralNode node) {

		httpServerThread = new Thread(() -> {
			// load it from configuration in the future.
			new NettyHttpServer().start(new DefChannelInitializer(node));
		});
		httpServerThread.start();
	}

	private static class NettyHttpServer {
		private ChannelFuture channel;
		private final EventLoopGroup masterGroup;
		private final EventLoopGroup slaveGroup;
		public NettyHttpServer() {
			masterGroup = new NioEventLoopGroup();
			slaveGroup = new NioEventLoopGroup();
		}

		public void start(ChannelInitializer channelInitializer) {
			Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

			try {

				final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup, slaveGroup)
						.channel(NioServerSocketChannel.class).childHandler(channelInitializer)
						.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

				int selfId = (((DefChannelInitializer)channelInitializer).node.getShardId()+1)*100
						+ (int)((DefChannelInitializer)channelInitializer).node.getCreatorId();
				channel = bootstrap.bind(8888+selfId).sync();
			} catch (final InterruptedException e) {
				e.printStackTrace();
				logger.error("error: {}", e);
			}
		}

		public void shutdown() {
			slaveGroup.shutdownGracefully();
			masterGroup.shutdownGracefully();

			try {
				channel.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("error: {}", e);
			}
		}

	}
}
