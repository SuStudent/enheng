package cn.susudad.enheng.server.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpServerInitializer
 * @createTime 2022/8/16
 */
public class HttpServerInitializer extends ChannelInitializer<Channel> {

  private HttpProcessHandler httpProcessHandler;

  public HttpServerInitializer(HttpProcessHandler httpProcessHandler) {
    this.httpProcessHandler = httpProcessHandler;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
//    ch.pipeline().addLast(LoggingHandler.class.getSimpleName(), new LoggingHandler(HttpServer.class, LogLevel.INFO));
    ch.pipeline().addLast(HttpServerCodec.class.getSimpleName(), new HttpServerCodec());
    ch.pipeline().addLast(HttpObjectAggregator.class.getSimpleName(), new HttpObjectAggregator(64 * 1024 * 1024));
    ch.pipeline().addLast(httpProcessHandler.getClass().getSimpleName(), httpProcessHandler);
  }
}
