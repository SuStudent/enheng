package cn.susudad.enheng.server.http;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpProcessHandler
 * @createTime 2022/8/16
 */
@Slf4j
@Sharable
public class HttpProcessHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final ExecutorService executor;

  public HttpProcessHandler(int coreThreadSize, int maxThreadSize) {
    super();
    executor = new ThreadPoolExecutor(
        coreThreadSize,
        maxThreadSize,
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(),
        new CustomizableThreadFactory("http-request-worker-"),
        (r, executor) -> {
          try {
            log.warn("rejected task:{}", executor.getQueue().size());
            executor.getQueue().put(r);
          } catch (InterruptedException e) {
            log.error("rejected error", e);
          }
        }
    );
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
    ReferenceCountUtil.retain(request);
    executor.execute(new ProxyWork(ctx, request));
  }
}
