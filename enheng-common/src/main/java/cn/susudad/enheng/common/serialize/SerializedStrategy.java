package cn.susudad.enheng.common.serialize;

import cn.susudad.enheng.common.protocol.SerTypeEnum;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description SerializedStrategy
 * @createTime 2022/8/12
 */
public interface SerializedStrategy {

  SerTypeEnum support();

  byte[] serialize(Object obj);

  <T> T deserialize(byte[] bytes, Class<T> cls);
}
