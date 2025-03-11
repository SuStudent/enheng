package cn.susudad.enheng.common.protocol;


import lombok.Getter;

import java.util.Arrays;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description SerTypeEnum
 * @createTime 2022/8/11
 */
@Getter
public enum SerTypeEnum {
  JSON((byte) 1),
  PROTOBUF((byte) 2),
  HESSIAN((byte) 3);

  private final byte type;

  SerTypeEnum(byte b) {
    this.type = b;
  }

  public static SerTypeEnum resolve(byte b) {
    return Arrays.stream(values()).filter(s -> s.type == b).findFirst().orElse(JSON);
  }
}
