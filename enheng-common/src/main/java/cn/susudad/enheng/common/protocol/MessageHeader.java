package cn.susudad.enheng.common.protocol;

import java.nio.charset.StandardCharsets;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description MessageHeader
 * @createTime 2022/8/11
 */
@Data
public class MessageHeader {

  /**
   * 魔术
   */
  private short magic = 0xfef;

  /**
   * 消息版本
   */
  private byte version;

  /**
   * 正文序列化类型
   */
  private byte serType;

  /**
   * 消息类型
   */
  private byte msgType;

  /**
   * 消息总长度
   */
  private int length;

  /**
   * 消息ID
   */
  private long msgSeq;

  /**
   * 扩展数据
   */
  private String attach;

  public int getAttachLength() {
    if (StringUtils.isBlank(attach)) {
      return 0;
    }
    return attach.getBytes(StandardCharsets.UTF_8).length;
  }
}
