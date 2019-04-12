package one.inve.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import one.inve.node.GeneralNode;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    public GeneralNode node;

    public HttpChannelInitializer(GeneralNode node) {
        this.node = node;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", 		new HttpRequestDecoder());
        pipeline.addLast("encoder", 		new HttpResponseEncoder());
        pipeline.addLast("aggregator", 	new HttpObjectAggregator(512 * 1024));
        pipeline.addLast("dispatcher", 	new DispatchHandler(node));
    }
}
