package cn.susudad.enheng.common.utils;

import cn.susudad.enheng.common.config.SpringContext;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.MessageHeader;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import cn.susudad.enheng.common.protocol.SerTypeEnum;
import cn.susudad.enheng.common.serialize.SerializedContext;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description MessageUtils
 * @createTime 2022/8/12
 */
public class MessageUtils {

  private static SnowFlake snowFlake;

  private final static SerTypeEnum DEFAULT_SER_TYPE = SerTypeEnum.JSON;

  private final static byte DEFAULT_VERSION = (byte) 1;

  private static SnowFlake getSnowFlake() {
    if (snowFlake == null) {
      snowFlake = SpringContext.getBean(SnowFlake.class);
    }
    return snowFlake;
  }

  public static EnhengMessage buildMessage(MsgTypeEnum msgType, SerTypeEnum serType, byte version, String attach, Object body) {
    EnhengMessage message = new EnhengMessage();
    MessageHeader header = new MessageHeader();
    header.setVersion(version);
    header.setSerType(serType.getType());
    header.setMsgType(msgType.getType());
    header.setMsgSeq(getSnowFlake().nextId());
    header.setAttach(attach);

    byte[] bytes = SerializedContext.serialize(serType, body);
    message.setHeader(header);
    message.setBody(bytes);

    return message;
  }

  public static EnhengMessage buildRespMessage(EnhengMessage reqMessage, MsgTypeEnum msgType, Object body) {
    EnhengMessage message = new EnhengMessage();
    MessageHeader header = new MessageHeader();
    header.setVersion(reqMessage.getHeader().getVersion());
    header.setSerType(reqMessage.getHeader().getSerType());
    header.setMsgType(msgType.getType());
    header.setMsgSeq(reqMessage.getHeader().getMsgSeq());
    header.setAttach(reqMessage.getHeader().getAttach());

    byte[] bytes = SerializedContext.serialize(SerTypeEnum.resolve(reqMessage.getHeader().getSerType()), body);
    message.setHeader(header);
    message.setBody(bytes);
    return message;
  }

  public static EnhengMessage buildMessage(MsgTypeEnum msgType, String attach, Object body) {
    return buildMessage(msgType, DEFAULT_SER_TYPE, DEFAULT_VERSION, attach, body);
  }

  public static EnhengMessage buildMessage(MsgTypeEnum msgType, Object body) {
    return buildMessage(msgType, null, body);
  }
}
