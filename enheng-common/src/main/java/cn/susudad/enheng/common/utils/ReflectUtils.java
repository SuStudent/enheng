package cn.susudad.enheng.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ReflectUtils
 * @createTime 2022/8/29
 */
@Slf4j
public class ReflectUtils {

  public static Method getMethod(Class<?> clz, String name) {
    assert clz != null;
    assert name != null;
    Class<?> objClass = clz;
    while (objClass != null) {
      Method[] declaredMethods = objClass.getDeclaredMethods();
      for (Method declaredMethod : declaredMethods) {
        if (declaredMethod.getName().equals(name)) {
          return declaredMethod;
        }
      }
      objClass = objClass.getSuperclass();
    }
    log.error("{} not find method {}", clz.getName(), name);
    return null;
}

  public static Object invoke(Object obj, String name, Object... args)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = getMethod(obj.getClass(), name);
    if (method == null) {
      throw new NoSuchMethodException(obj.getClass() + " not find method " + name);
    }
    method.setAccessible(true);
    return method.invoke(obj,args);
  }

}
