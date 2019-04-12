package one.inve.contract.shell.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import one.inve.contract.shell.http.annotation.RequestMapper;
import one.inve.contract.shell.http.annotation.RequestMatchable;
import one.inve.node.GeneralNode;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: dispatch request to the handler which uses
 *               {@link RequestMapper} as instruction
 * @author: Francis.Deng
 * @date: 2018年11月2日 下午5:19:06
 * @version: V1.0
 */
@Sharable
public class DispatchHandler extends ChannelInboundHandlerAdapter {
	GeneralNode node;

	public DispatchHandler(GeneralNode node) {
		this.node = node;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			final FullHttpRequest request = (FullHttpRequest) msg;

//			System.out.println(request.getUri());
//			System.out.println(request.getMethod().name());

			ByteBuf buf = request.content();
			byte[] byteArray = new byte[buf.capacity()];
			buf.readBytes(byteArray);
			String req = new String(byteArray);
//			System.out.println(req);

			// final String responseMessage = "Hello from Netty!";
			final String responseMessage = doRequest(req, request.getUri(), request.getMethod().name());

			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					copiedBuffer(responseMessage.getBytes()));

			if (HttpHeaders.isKeepAlive(request)) {
				response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());

			ctx.writeAndFlush(response);
		} else {
			super.channelRead(ctx, msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
				copiedBuffer(cause.getMessage().getBytes())));
	}

	// where we introduce key class {@link ContractHandlers}
	protected String doRequest(String request, String uri, String methodName) {
		String response = "failed in function execution";
		ContractHandlers contractHandlersHandler = new ContractHandlers("one.inve.contract.shell.http.handler",
				RequestMapper.class);

		IFunc func = contractHandlersHandler.enrichFunc(
				node,
				new SimpleHttpUriAndMethodMatcher(uri, methodName),
				new ParameterTypesEqualityFunction(),
				request);

		if (func != null) {
			Object obj = func.execute();

			if (obj != null) {
				response = obj.toString();
			}
		}

		return response;
	}

	/**
	 * 
	 * simply compare uri and method name to check equality
	 */
	protected static class SimpleHttpUriAndMethodMatcher implements RequestMatchable {
		private String uri;
		private String method;

		public SimpleHttpUriAndMethodMatcher(String uri, String method) {
			this.uri = uri;
			this.method = method;
		}

		@Override
		public boolean isMatched(RequestMapper mapper) {
			if (uri.compareToIgnoreCase(mapper.value()) == 0
					&& method.compareToIgnoreCase(mapper.method().name()) == 0) {
				return true;
			}

			return false;
		}

	}
}
