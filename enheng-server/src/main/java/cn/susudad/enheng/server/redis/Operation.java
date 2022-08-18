package cn.susudad.enheng.server.redis;

import cn.susudad.enheng.common.utils.JsonUtils;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description Operation
 * @createTime 2022/8/15
 */
public abstract class Operation {

  public <Any> Any deserialize(String json, Class<Any> cls) {
    if (json == null) {
      return null;
    }
    return JsonUtils.fromJson(json, cls);
  }

  public String serialize(Object o) {
    return JsonUtils.toJson(o);
  }
}
