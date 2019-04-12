package one.inve.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import one.inve.node.GeneralNode;

/**
 * @author Francis.Deng
 * @date 2018年11月3日 下午3:29:38
 */
public class NettyHttpServer {
	private ChannelFuture channel;
	private final EventLoopGroup masterGroup;
	private final EventLoopGroup slaveGroup;

	public NettyHttpServer() {
		masterGroup = new NioEventLoopGroup();
		slaveGroup = new NioEventLoopGroup();
	}

	public void start(ChannelInitializer channelInitializer)
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

		try {
			final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup, slaveGroup)
					.channel(NioServerSocketChannel.class).childHandler(channelInitializer)
					.option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			channel = bootstrap.bind(((HttpChannelInitializer)channelInitializer).node.parameters.selfGossipAddress.httpPort).sync();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void shutdown()
	{
		slaveGroup.shutdownGracefully();
		masterGroup.shutdownGracefully();

		try {
			channel.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void boostrapHttpService(GeneralNode node) {
		Thread httpServerThread = new Thread(() -> {
			new NettyHttpServer().start(new HttpChannelInitializer(node));
		});
		httpServerThread.start();
	}

	public static void main(String[] args) {
		new NettyHttpServer().start(new DefaultChannelInitializer());
	}
}