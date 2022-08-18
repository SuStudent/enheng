package cn.susudad.enheng.common.protocol;

import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengMessage
 * @createTime 2022/8/11
 */
@Data
public class EnhengMessage {

  private MessageHeader header;

  private byte[] body;
}
