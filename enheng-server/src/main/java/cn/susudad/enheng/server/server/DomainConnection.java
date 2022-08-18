package cn.susudad.enheng.server.server;

import cn.susudad.enheng.common.model.HttpResp;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.EnhengPromise;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description DomainConnection
 * @createTime 2022/8/16
 */
public class DomainConnection implements Closeable {

  private final ChannelHandlerContext ctx;

  private final String appKey;

  private final String subdomain;

  private final AtomicBoolean lock = new AtomicBoolean();

  public DomainConnection(ChannelHandlerContext ctx, String appKey, String subdomain) {
    this.ctx = ctx;
    this.appKey = appKey;
    this.subdomain = subdomain;
  }

  public String getAppKey() {
    return appKey;
  }

  public String getSubdomain() {
    return subdomain;
  }

  public boolean send(EnhengMessage enhengMessage, EnhengPromise<HttpResp> promise) {
    if (enhengMessage.getHeader().getMsgType() == MsgTypeEnum.SERVICE_REQ.getType()) {
      ConcurrentHashMap<Long, EnhengPromise<HttpResp>> promiseMap = ctx.channel().attr(EnhengServerHandler.SERVICE_MAP).get();
      while (true) {
        if (lock.compareAndSet(false, true)) {
          try {
            if (promiseMap.putIfAbsent(enhengMessage.getHeader().getMsgSeq(), promise) == null) {
              ctx.channel().writeAndFlush(enhengMessage);
              return true;
            }
            return false;
          } finally {
            lock.set(false);
          }
        }
      }
    } else {
      ctx.channel().writeAndFlush(enhengMessage);
    }
    return true;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DomainConnection that = (DomainConnection) o;
    return ctx.equals(that.ctx);
  }

  @Override
  public int hashCode() {
    return ctx.hashCode();
  }

  @Override
  public void close() {
    if (ctx.channel().isActive()) {
      ChannelFuture channelFuture = ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER);
      channelFuture.addListener(ChannelFutureListener.CLOSE);
    } else {
      ctx.channel().close();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (ctx.channel().isActive()) {
      ChannelFuture channelFuture = ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER);
      channelFuture.addListener(ChannelFutureListener.CLOSE);
    } else {
      ctx.channel().close();
    }
    super.finalize();
  }
}
