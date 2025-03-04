package cn.susudad.enheng.client.http;

import cn.susudad.enheng.common.protocol.EnhengPromise;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * 1、请求完成后不会关闭连接，是否keep-alive 交由服务端控制。
 * 2、在同一个链接中响应是按请求顺序到达，所以存在前一次请求耗时而影响下一次的请求。(Http 管线化 决定。)
 * 3、若想并发请求，可构建多个HttpClient实例来提升性能。HttpClient实例的个数由本机可用的端口数决定。
 *
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpClient base for netty
 * @createTime 2022/8/17
 */
@Slf4j
public class HttpClient {

  private static Bootstrap bootstrap = new Bootstrap();
  private static EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(),
      new CustomizableThreadFactory("client-work"));
  ;

  static {
    bootstrap.group(workerGroup)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            ch.pipeline().addLast(new HttpClientCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(64 * 1024 * 1024));
            ch.pipeline().addLast(new HttpDecode());
            ch.pipeline().addLast(new ReqInfoQueue());
          }
        });
  }

  private Channel channel;

  private final String host;

  private final int port;

  private HttpClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public static HttpClient createClient(String host, int port) throws InterruptedException {
    HttpClient httpClient = new HttpClient(host, port);
    try {
      httpClient.connect();
    } catch (InterruptedException e) {
      log.error("", e);
      throw e;
    }
    return httpClient;
  }

  private void connect() throws InterruptedException {
    ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
    try {
      channelFuture.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      channelFuture.cancel(true);
      throw e;
    }

    this.channel = channelFuture.channel();
  }

  private void requiredHeader(FullHttpRequest request) {
    HttpHeaders headers = request.headers();
    if (!headers.contains(HttpHeaderNames.HOST)) {
      request.headers().set(HttpHeaderNames.HOST, channel.remoteAddress().toString().substring(1));
    }
    if (!headers.contains(HttpHeaderNames.USER_AGENT)) {
      request.headers().set(HttpHeaderNames.USER_AGENT, "Enheng Netty Client");
    }
    if (!headers.contains(HttpHeaderNames.ACCEPT)) {
      request.headers().set(HttpHeaderNames.ACCEPT, "*/*");
    }
    if (!headers.contains(HttpHeaderNames.ACCEPT_ENCODING)) {
      request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, "gzip, deflate, br");
    }
    if (!headers.contains(HttpHeaderNames.CONNECTION)) {
      request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
    }
  }

  public EnhengPromise<FullHttpResponse> request(FullHttpRequest request) {
    EnhengPromise<FullHttpResponse> promise = new EnhengPromise<>(10);
    requiredHeader(request);
    ReqInfo reqInfo = new ReqInfo(request, promise);
    channel.writeAndFlush(reqInfo);
    return promise;
  }

  public void close() throws IOException {
    if (channel != null && channel.isActive()) {
      Queue<ReqInfo> infos = channel.attr(ReqInfoQueue.REQ_QUEUE).get();
      if (infos.size() > 0) {
        log.warn("http client close, REQ_QUEUE is not empty. size: {}", infos.size());
      }
    }
    if (channel != null) {
      log.debug("{} 关闭连接。", channel);
      channel.close();
    }
  }

  public boolean isActive() {
    return channel != null && channel.isOpen() && channel.isActive() && channel.isWritable();
  }
}
