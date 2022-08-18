package cn.susudad.enheng.client;

import cn.susudad.enheng.client.config.CommandConfig;
import cn.susudad.enheng.common.config.SpringContext;
import cn.susudad.enheng.common.model.LoginReq;
import cn.susudad.enheng.common.model.LoginResp;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import cn.susudad.enheng.common.serialize.SerializedContext;
import cn.susudad.enheng.common.utils.MessageUtils;
import cn.susudad.enheng.common.utils.RSAUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengLoginHandler
 * @createTime 2022/8/12
 */
@Slf4j
public class EnhengLoginHandler extends SimpleChannelInboundHandler<EnhengMessage> {

  private CommandConfig commandConfig;
  private EnhengClientProperties properties;

  public EnhengLoginHandler(EnhengClientProperties properties) {
    this.properties = properties;
    this.commandConfig = SpringContext.getBean(CommandConfig.class);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("登录 ==>");
    login(ctx);
    super.channelActive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, EnhengMessage msg) throws Exception {
    if (msg.getHeader().getMsgType() == MsgTypeEnum.AUTH_RESP.getType()) {
      logAfter(ctx, msg);
    }
  }

  private void login(ChannelHandlerContext ctx) {
    String sourceStr = properties.getAppKey() + ":" + System.currentTimeMillis();
    try {
      String appSig = RSAUtils.encryptByPublicKey(sourceStr, properties.getAppSecret());
      LoginReq loginReq = LoginReq.builder().appKey(properties.getAppKey()).appSig(appSig).subdomain(commandConfig.getSubDomain()).build();
      EnhengMessage message = MessageUtils.buildMessage(MsgTypeEnum.AUTH_REQ, loginReq);
      ctx.writeAndFlush(message);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("登录失败。", e);
    }
  }

  private void logAfter(ChannelHandlerContext ctx, EnhengMessage enhengMessage) {
    LoginResp loginResp = SerializedContext.deserialize(enhengMessage, LoginResp.class);
    log.info("{}", loginResp);
    if (!loginResp.isSuccess()) {
      ctx.close();
      return;
    }
    ctx.pipeline().remove(this);
    ctx.pipeline().addLast(EnhengHeartbeatHandler.class.getSimpleName(), new EnhengHeartbeatHandler(loginResp.getIdlTimeout(), properties));
    ctx.pipeline().addLast(EnhengClientHandler.class.getSimpleName(), new EnhengClientHandler(properties));

  }
}
