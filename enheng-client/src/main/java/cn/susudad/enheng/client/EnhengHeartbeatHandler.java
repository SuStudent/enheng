package cn.susudad.enheng.client;

import cn.susudad.enheng.common.model.Heartbeat;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import cn.susudad.enheng.common.serialize.SerializedContext;
import cn.susudad.enheng.common.utils.MessageUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengHeartbeatHandler
 * @createTime 2022/8/16
 */
@Slf4j
public class EnhengHeartbeatHandler extends SimpleChannelInboundHandler<EnhengMessage> {

  private final int idlTimeout;
  private final ScheduledExecutorService executorService;
  private final EnhengClientProperties properties;

  private ScheduledFuture<?> scheduledFuture;

  public EnhengHeartbeatHandler(int idlTimeout, EnhengClientProperties properties) {
    this.idlTimeout = idlTimeout;
    this.properties = properties;
    this.executorService = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("Heartbeat-"));
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    scheduledFuture = executorService.scheduleAtFixedRate(() -> {
      try {
        heartbeatReq(ctx);
      } catch (Exception e) {
        log.error("Heartbeat error.", e);
      }
    }, 0, idlTimeout / 3, TimeUnit.SECONDS);
    super.handlerAdded(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (!scheduledFuture.isDone()) {
      scheduledFuture.cancel(true);
    }
    super.exceptionCaught(ctx, cause);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (!scheduledFuture.isDone()) {
      scheduledFuture.cancel(true);
    }
    super.channelInactive(ctx);
  }

  private void heartbeatReq(ChannelHandlerContext ctx) {
    if (ctx.channel().isActive()) {
      Heartbeat ping = Heartbeat.builder().appKey(properties.getAppKey()).time(new Date()).msg("ping").build();
      EnhengMessage message = MessageUtils.buildMessage(MsgTypeEnum.HEARTBEAT_REQ, ping);
      ctx.writeAndFlush(message);
    }
  }

  private void logHeartbeat(EnhengMessage msg) {
    Heartbeat heartbeat = SerializedContext.deserialize(msg, Heartbeat.class);
    log.debug(" <== {}", heartbeat.getMsg());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, EnhengMessage msg) throws Exception {
    if (msg.getHeader().getMsgType() == MsgTypeEnum.HEARTBEAT_RESP.getType()) {
      logHeartbeat(msg);
      return;
    }
    ctx.fireChannelRead(msg);
  }
}
