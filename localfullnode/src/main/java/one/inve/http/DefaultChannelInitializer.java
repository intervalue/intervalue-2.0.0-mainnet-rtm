package one.inve.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * The class specifies inner netty's decoders and encoders which
 *               handle the input and output information
 * @author Francis.Deng
 * @date 2018年10月30日 下午2:58:13
 * @version V1.0
 */
public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	public void initChannel(SocketChannel ch) {
		// Create a default pipeline implementation.
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast("decoder", 		new HttpRequestDecoder());
		pipeline.addLast("encoder", 		new HttpResponseEncoder());
		pipeline.addLast("aggregator", 	new HttpObjectAggregator(512 * 1024));
//		pipeline.addLast("dispatcher", 	new DispatchHandler());
	}
}
