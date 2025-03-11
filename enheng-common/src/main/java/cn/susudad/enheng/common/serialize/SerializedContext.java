package cn.susudad.enheng.common.serialize;

import cn.susudad.enheng.common.config.SpringContext;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.SerTypeEnum;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.susudad.enheng.common.utils.GzipUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description SerializedContext
 * @createTime 2022/8/12
 */
@Slf4j
public class SerializedContext {

  private static volatile ConcurrentHashMap<SerTypeEnum, SerializedStrategy> SERIALIZED_MAP;

  private static SerializedStrategy getSerialized(SerTypeEnum serType) {
    if (SERIALIZED_MAP == null) {
      synchronized (SerializedContext.class) {
        if (SERIALIZED_MAP == null) {
          ConcurrentHashMap<SerTypeEnum, SerializedStrategy> tmpMap = new ConcurrentHashMap<>();
          Map<String, SerializedStrategy> beans = SpringContext.getBeansOfType(SerializedStrategy.class);
          if (beans != null) {
            beans.forEach((name, obj) -> {
              tmpMap.put(obj.support(), obj);
            });
          }
          SERIALIZED_MAP = tmpMap;
        }
      }
    }
    SerializedStrategy strategy = SERIALIZED_MAP.get(serType);
    if (strategy != null) {
      return strategy;
    }
    return SERIALIZED_MAP.get(SerTypeEnum.JSON);
  }

  public static byte[] serialize(SerTypeEnum serType, Object obj) {
    SerializedStrategy serialized = getSerialized(serType);
    return GzipUtils.compress(serialized.serialize(obj));
  }

  private static <T> T deserialize(SerTypeEnum serType, byte[] bytes, Class<T> cls) {
    SerializedStrategy serialized = getSerialized(serType);
    return serialized.deserialize(GzipUtils.decompress(bytes), cls);
  }

  public static <T> T deserialize(EnhengMessage message, Class<T> cls) {
    return deserialize(SerTypeEnum.resolve(message.getHeader().getSerType()), message.getBody(), cls);
  }
}
