package cn.susudad.enheng.client.http;

import cn.susudad.enheng.common.exception.ChannelCloseException;
import cn.susudad.enheng.common.protocol.EnhengPromise;
import cn.susudad.enheng.common.utils.ExecutorService;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.AttributeKey;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpHandler
 * @createTime 2022/8/17
 */
@Slf4j
public class ReqInfoQueue extends ChannelDuplexHandler {

  public final static AttributeKey<Queue<ReqInfo>> REQ_QUEUE = AttributeKey.valueOf("REQ_QUEUE");

  @Override
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    super.connect(ctx, remoteAddress, localAddress, promise);
    ctx.channel().attr(ReqInfoQueue.REQ_QUEUE).set(new ConcurrentLinkedQueue<>());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.debug("Http client closed");
    Queue<ReqInfo> queue = ctx.channel().attr(ReqInfoQueue.REQ_QUEUE).get();
    Iterator<ReqInfo> iterator = queue.iterator();
    while (iterator.hasNext()) {
      ReqInfo reqInfo = iterator.next();
      iterator.remove();
      EnhengPromise<FullHttpResponse> promise = reqInfo.getPromise();
      if (!promise.isDone()) {
        promise.tryFailure(new ChannelCloseException("连接已关闭。"));
      }
    }
    super.channelInactive(ctx);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof ReqInfo) {
      ExecutorService.getInstance().execute(() -> {
        ReqInfo reqInfo = (ReqInfo) msg;
        Queue<ReqInfo> queue = ctx.channel().attr(REQ_QUEUE).get();
        log.debug("REQ_QUEUE add ");
        queue.add(reqInfo);
        ctx.writeAndFlush(reqInfo.getRequest(), promise);
      });
    } else {
      super.write(ctx, msg, promise);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("异常。", cause);
    ctx.close();
    super.exceptionCaught(ctx, cause);
  }
}
