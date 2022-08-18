package cn.susudad.enheng.common.protocol;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description MsgTypeEnum
 * @createTime 2022/8/11
 */
public enum MsgTypeEnum {
  ONE_WAY((byte) 0, "单向发送."),
  AUTH_REQ((byte) 1, "认证请求。"),
  AUTH_RESP((byte) 2, "认证响应。"),
  HEARTBEAT_REQ((byte) 3, "心跳请求。"),
  HEARTBEAT_RESP((byte) 4, "心跳响应。"),
  SERVICE_REQ((byte) 5, "业务请求。"),
  SERVICE_RESP((byte) 6, "业务响应。");

  private final byte type;
  private final String desc;

  MsgTypeEnum(byte type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public byte getType() {
    return this.type;
  }
}
