package cn.susudad.enheng.common.serialize;

import cn.susudad.enheng.common.protocol.SerTypeEnum;
import cn.susudad.enheng.common.utils.JsonUtils;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description JSONSerialized
 * @createTime 2022/8/12
 */
@Component
public class JSONSerialized implements SerializedStrategy {

  @Override
  public SerTypeEnum support() {
    return SerTypeEnum.JSON;
  }

  @Override
  public byte[] serialize(Object obj) {
    if (obj == null) {
      return new byte[0];
    }
    if (obj instanceof String) {
      return ((String) obj).getBytes(StandardCharsets.UTF_8);
    }
    String json = JsonUtils.toJson(obj);
    return json.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public <T> T deserialize(byte[] bytes, Class<T> cls) {
    if (bytes == null) {
      return null;
    }
    String str = new String(bytes, StandardCharsets.UTF_8);
    if (cls == null) {
      return (T) str;
    }
    return JsonUtils.fromJson(str, cls);
  }
}
