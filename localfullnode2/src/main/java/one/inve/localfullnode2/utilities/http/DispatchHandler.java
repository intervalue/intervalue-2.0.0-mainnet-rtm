package one.inve.localfullnode2.utilities.http;

import static io.netty.buffer.Unpooled.copiedBuffer;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import one.inve.localfullnode2.utilities.ResponseUtils;
import one.inve.localfullnode2.utilities.http.annotation.RequestMapper;
import one.inve.localfullnode2.utilities.http.annotation.RequestMatchable;

/**
 * 
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * dispatch request to the service which uses {@link RequestMapper} as
 * instruction
 * 
 * @author Francis.Deng
 * @date 2018年11月2日 下午5:19:06
 * @version V1.0
 */
@Sharable
public class DispatchHandler extends ChannelInboundHandlerAdapter {
	private static final String HTTP_API_HANDLER_PACKAGE = "one.inve.localfullnode2.http";

	private HttpServiceImplsDependent dep;

	public DispatchHandler(HttpServiceImplsDependent dep) {
		this.dep = dep;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			final FullHttpRequest request = (FullHttpRequest) msg;
			final String responseMessage = doRequest(request);

			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					copiedBuffer(responseMessage.getBytes()));

			if (HttpHeaders.isKeepAlive(request)) {
				response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());

			// 允许跨域访问
			response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS, "*");// 允许headers自定义
			response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT,DELETE");
			response.headers().set(HttpHeaders.Names.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

			ctx.writeAndFlush(response);
		} else {
			super.channelRead(ctx, msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
				copiedBuffer(cause.getMessage().getBytes())));
	}

	private String doRequest(FullHttpRequest request) {
		HttpHandlers handlers = new HttpHandlers(HTTP_API_HANDLER_PACKAGE, RequestMapper.class);

		try {
			IFunction func = handlers.enrichFunc(dep,
					new SimpleHttpUriAndMethodMatcher(request.uri(), request.method().name()),
					new ParameterCheckFunction(), ParameterParser.parse(request));

			if (func != null) {
				Object obj = func.execute();

				String response = "";
				if (obj != null) {
					response = obj.toString();
				}
				return response;
			} else {
				return ResponseUtils
						.handleExceptionResponse("http request method is not supported or no such api service.");
			}
		} catch (Exception e) {
			return ResponseUtils.handleExceptionResponse(e.getMessage());
		}
	}

	/**
	 * simply compare uri and method name to check equality
	 */
	protected static class SimpleHttpUriAndMethodMatcher implements RequestMatchable {
		private String uri;
		private String method;

		SimpleHttpUriAndMethodMatcher(String uri, String method) {
			this.uri = uri;
			this.method = method;
		}

		@Override
		public boolean isMatched(RequestMapper mapper) {
			return uri.compareToIgnoreCase(mapper.value()) == 0
					&& method.compareToIgnoreCase(mapper.method().name()) == 0;
		}
	}
}
