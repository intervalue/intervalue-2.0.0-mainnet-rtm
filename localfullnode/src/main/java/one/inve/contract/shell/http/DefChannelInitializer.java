package one.inve.contract.shell.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import one.inve.node.GeneralNode;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The class specifies inner netty's decoders and encoders which
 *               handle the input and output information
 * @author: Francis.Deng
 * @date: 2018年10月30日 下午2:58:13
 * @version: V1.0
 */
public class DefChannelInitializer extends ChannelInitializer<SocketChannel> {
	public GeneralNode node;

	public DefChannelInitializer(GeneralNode node) {
		this.node = node;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("encoder", new HttpResponseEncoder());

		pipeline.addLast("aggregator", new HttpObjectAggregator(512 * 1024));

		pipeline.addLast("dispatcher", new DispatchHandler(node));
	}
}
