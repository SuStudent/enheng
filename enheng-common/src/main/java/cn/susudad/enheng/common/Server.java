package cn.susudad.enheng.common;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description Server
 * @createTime 2022/8/16
 */
public abstract class Server implements ApplicationListener<ApplicationContextEvent> {

  public abstract void start();

  public abstract void stop();

  @Override
  public void onApplicationEvent(ApplicationContextEvent event) {
    if (event instanceof ContextRefreshedEvent) {
      start();
    }
    if (event instanceof ContextStoppedEvent) {
      stop();
    }
  }
}
