package cn.susudad.enheng.common.config;

import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description SpringContext
 * @createTime 2022/8/11
 */
@Component
public class SpringContext implements ApplicationContextAware {

  private static ApplicationContext AC;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    SpringContext.AC = applicationContext;
  }

  public static <T> T getBean(Class<T> cls) {
    try {
      return AC.getBean(cls);
    } catch (Exception e) {
      return null;
    }
  }

  public static Object getBean(String name) {
    try {
      return AC.getBean(name);
    } catch (Exception e) {
      return null;
    }
  }

  public static <T> T getBean(String name, Class<T> cls) {

    try {
      return AC.getBean(name, cls);
    } catch (Exception e) {
      return null;
    }
  }

  public static <T> Map<String, T> getBeansOfType(@Nullable Class<T> cls) throws BeansException {
    try {
      return AC.getBeansOfType(cls);
    } catch (Exception e) {
      return null;
    }
  }


  public static <T> T getProperty(String property, Class<T> cls, T def) {
    try {
      return AC.getEnvironment().getProperty(property, cls, def);
    } catch (Exception e) {
      return def;
    }
  }
}
