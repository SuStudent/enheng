package cn.susudad.enheng.server.server;

import cn.susudad.enheng.common.config.SpringContext;
import cn.susudad.enheng.common.exception.ChannelCloseException;
import cn.susudad.enheng.common.model.Heartbeat;
import cn.susudad.enheng.common.model.HttpResp;
import cn.susudad.enheng.common.model.LoginReq;
import cn.susudad.enheng.common.model.LoginResp;
import cn.susudad.enheng.common.protocol.AbstractHandler;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.EnhengPromise;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import cn.susudad.enheng.common.serialize.SerializedContext;
import cn.susudad.enheng.common.utils.MessageUtils;
import cn.susudad.enheng.server.service.AuthService;
import cn.susudad.enheng.server.service.DomainManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengServerHandler
 * @createTime 2022/8/11
 */
@Slf4j
public class EnhengServerHandler extends AbstractHandler<EnhengMessage> {

  private ChannelPromise loginPromise;

  private static final ExecutorService executor = new ThreadPoolExecutor(
      4,
      12,
      60L,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue<>(),
      new CustomizableThreadFactory("enehng-proxy-worker-"),
      (r, executor) -> {
        try {
          log.warn("rejected task:{}", executor.getQueue().size());
          executor.getQueue().put(r);
        } catch (InterruptedException e) {
          log.error("rejected error", e);
        }
      }
  );

  public static final AttributeKey<String> DOMAIN_KEY = AttributeKey.valueOf(String.class, "SUBDOMAIN");

  public static final AttributeKey<ConcurrentHashMap<Long, EnhengPromise<HttpResp>>> SERVICE_MAP = AttributeKey.valueOf("SERVICE_MAP");


  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("new channel has join. {}", ctx.channel().remoteAddress());
    loginPromise = ctx.newPromise();
    ctx.channel().eventLoop().schedule(() -> {
      if (!loginPromise.isSuccess()) {
        log.info("未在指定时间内完成登录。关闭连接。");
        ctx.close();
      }
    }, 10, TimeUnit.SECONDS);
    super.channelActive(ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    String subdomain = ctx.channel().attr(DOMAIN_KEY).get();
    if (StringUtils.isNotBlank(subdomain)) {
      log.info("channel quit。{} register with {}", ctx.channel().remoteAddress(), subdomain);
      DomainManager.remove(subdomain);
    }

    ConcurrentHashMap<Long, EnhengPromise<HttpResp>> promiseMap = ctx.channel().attr(SERVICE_MAP).get();
    if (promiseMap != null) {
      Iterator<Entry<Long, EnhengPromise<HttpResp>>> iterator = promiseMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<Long, EnhengPromise<HttpResp>> promiseEntry = iterator.next();
        iterator.remove();
        promiseEntry.getValue().tryFailure(new ChannelCloseException("连接已关闭。"));
      }
    }
    super.channelInactive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, EnhengMessage enhengMessage) throws Exception {
    log.debug("{}", enhengMessage);
    byte msgType = enhengMessage.getHeader().getMsgType();
    if (msgType == MsgTypeEnum.AUTH_REQ.getType()) {
      login(ctx, enhengMessage);
      return;
    } else if (msgType == MsgTypeEnum.HEARTBEAT_REQ.getType()) {
      heartbeat(ctx, enhengMessage);
      return;
    } else if (msgType == MsgTypeEnum.SERVICE_RESP.getType()) {
      handleService(ctx, enhengMessage);
      return;
    }
    ctx.fireChannelRead(enhengMessage);
  }

  private void handleService(ChannelHandlerContext ctx, EnhengMessage enhengMessage) {
    executor.submit(() -> {
      ConcurrentHashMap<Long, EnhengPromise<HttpResp>> promiseConcurrentHashMap = ctx.channel().attr(SERVICE_MAP).get();
      EnhengPromise<HttpResp> promise = promiseConcurrentHashMap.remove(enhengMessage.getHeader().getMsgSeq());
      if (promise != null) {
        HttpResp httpResp = SerializedContext.deserialize(enhengMessage, HttpResp.class);
        promise.trySuccess(httpResp);
      }
    });
  }

  private void heartbeat(ChannelHandlerContext ctx, EnhengMessage message) {
    Heartbeat heartbeatReq = SerializedContext.deserialize(message, Heartbeat.class);
    log.info("{}", heartbeatReq);
    EnhengMessage heartbeatResp = MessageUtils.buildRespMessage(message, MsgTypeEnum.HEARTBEAT_RESP,
        Heartbeat.builder().appKey(heartbeatReq.getAppKey()).time(new Date()).msg("pong").build());
    ctx.writeAndFlush(heartbeatResp);
  }

  private void login(ChannelHandlerContext ctx, EnhengMessage enhengMessage) {
    LoginReq loginReq = SerializedContext.deserialize(enhengMessage, LoginReq.class);
    LoginResp loginResp = SpringContext.getBean(AuthService.class).login(loginReq);
    if (loginResp.isSuccess()) {
      if (!DomainManager.add(new DomainConnection(ctx, loginReq.getAppKey(), loginReq.getSubdomain()))) {
        log.info("subdomain 重复。");
        loginResp.setSuccess(false);
        loginResp.setDesc("subdomain 已存在，请更换。");
      } else {
        if (loginPromise.trySuccess()) {
          ctx.channel().attr(DOMAIN_KEY).set(loginReq.getSubdomain());
          ctx.channel().attr(SERVICE_MAP).set(new ConcurrentHashMap<>());
        }
      }
    }
    EnhengMessage message = MessageUtils.buildRespMessage(enhengMessage, MsgTypeEnum.AUTH_RESP, loginResp);
    ctx.writeAndFlush(message);
  }
}
