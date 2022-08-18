package cn.susudad.enheng.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengClient
 * @createTime 2022/8/11
 */
@Slf4j
@Component
public class EnhengClient implements ApplicationListener<ContextRefreshedEvent> {

  private Bootstrap bootstrap;
  private EventLoopGroup workerGroup;

  private Channel channel;
  @Autowired
  private EnhengClientProperties enhengClientProperties;

  private ApplicationContext applicationContext;

  private final ExecutorService serverStarter = Executors.newSingleThreadExecutor();

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    this.applicationContext = event.getApplicationContext();
    init();
    serverStarter.execute(() -> {
      try {
        connect();
      } catch (ConnectException ignored) {
      }
    });
  }

  private void init() {
    bootstrap = new Bootstrap();
    workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new CustomizableThreadFactory("client-work"));
    bootstrap.group(workerGroup)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.TCP_NODELAY, true)
        .handler(new EnhengClientInitializer(enhengClientProperties));
  }

  private void connect() throws ConnectException {
    ChannelFuture future = null;
    try {
      future = bootstrap.connect(enhengClientProperties.getRemoteHost(), enhengClientProperties.getRemotePort()).sync();
      channel = future.channel();
    } catch (Exception e) {
      log.error("socket 链接异常，", e);
      throw new ConnectException("创建socket失败。");
    } finally {
      try {
        if (channel != null) {
          channel.closeFuture().sync();
        }
      } catch (InterruptedException ignored) {
      }
      exit();
    }
  }

  private void exit() {
    log.info("exit now.");
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }
    System.exit(SpringApplication.exit(applicationContext));
  }
}
