package cn.susudad.enheng.common.protocol;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.WriteTimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description AbstractHandler
 * @createTime 2022/8/12
 */
@Slf4j
public abstract class AbstractHandler<T> extends SimpleChannelInboundHandler<T> {

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    IdleStateEvent state = (IdleStateEvent) evt;
    switch (state.state()) {
      case READER_IDLE:
        log.info("读取空闲超时");
        break;
      case WRITER_IDLE:
        log.info("写入空闲超时");
        break;
      case ALL_IDLE:
        log.info("读取和写入空闲耗时");
        break;
    }
    close(ctx);
    super.userEventTriggered(ctx, evt);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    if (cause instanceof WriteTimeoutException) {
      log.info("IO写入超时", cause);
    } else {
      log.error("", cause);
    }
    close(ctx);
  }

  private void close(ChannelHandlerContext ctx) {
    if (ctx.channel().isActive()) {
      ChannelFuture channelFuture = ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER);
      channelFuture.addListener(ChannelFutureListener.CLOSE);
    }
  }
}
