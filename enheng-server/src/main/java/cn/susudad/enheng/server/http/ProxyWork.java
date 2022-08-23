package cn.susudad.enheng.server.http;

import cn.susudad.enheng.common.exception.ProxyException;
import cn.susudad.enheng.common.model.HttpReq;
import cn.susudad.enheng.common.model.HttpResp;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.EnhengPromise;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import cn.susudad.enheng.common.utils.MessageUtils;
import cn.susudad.enheng.server.server.DomainConnection;
import cn.susudad.enheng.server.service.DomainManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ProxyWork
 * @createTime 2022/8/17
 */
@Slf4j
public class ProxyWork implements Runnable {

  private final ChannelHandlerContext ctx;

  private final FullHttpRequest request;

  public ProxyWork(ChannelHandlerContext ctx, FullHttpRequest request) {
    this.ctx = ctx;
    this.request = request;
  }

  @Override
  public void run() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start(request.uri());
    HttpReq httpReq = null;
    try {
      httpReq = HttpReq.convert(request);
    } finally {
      ReferenceCountUtil.release(request);
    }
    String subdomain = getSubdomain(request);
    if (StringUtils.isBlank(subdomain)) {
      notFound(ctx, request);
      return;
    }
    DomainConnection connection = DomainManager.get(subdomain);
    if (connection == null) {
      log.info("not register，subdomain={}, remote={}", subdomain, ctx.channel().remoteAddress().toString());
      notFound(ctx, request);
      return;
    }
    EnhengMessage message = MessageUtils.buildMessage(MsgTypeEnum.SERVICE_REQ, httpReq);
    EnhengPromise<HttpResp> promise = new EnhengPromise<>(10);
    if (!connection.send(message, promise)) {
      error(new ProxyException("发送失败。"));
      return;
    }
    HttpReq finalHttpReq = httpReq;
    promise.onComplete((resp, e) -> {
      if (e != null) {
        error(e);
        return;
      }
      stopWatch.stop();
      log.info("proxy：remote={}, appKey={}, subdomain={}, uri={}, status={}, {}ms", ctx.channel().remoteAddress().toString(), connection.getAppKey(),
          connection.getSubdomain(), finalHttpReq.getUri(),
          resp.getStatus(), stopWatch.getTotalTimeMillis());
      success(resp);
    });
  }

  private void success(HttpResp httpResp) {
    FullHttpResponse sourceResponse = httpResp.getSourceResponse();
    ctx.channel().writeAndFlush(sourceResponse);
  }

  private void error(Throwable e) {
    log.error("代理异常。", e);
    DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.BAD_GATEWAY);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
    ctx.channel().writeAndFlush(response);
  }

  private String getSubdomain(FullHttpRequest request) {
    String host = request.headers().get(HttpHeaderNames.HOST);
    if (StringUtils.isBlank(host)) {
      return null;
    }
    return StringUtils.substringBefore(host, ".");
  }

  private void notFound(ChannelHandlerContext ctx, FullHttpRequest request) {
    DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.NOT_FOUND);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
    ChannelFuture future = ctx.writeAndFlush(response);
    if (!HttpUtil.isKeepAlive(request)) {
      future.addListener(ChannelFutureListener.CLOSE);
    }
  }
}
