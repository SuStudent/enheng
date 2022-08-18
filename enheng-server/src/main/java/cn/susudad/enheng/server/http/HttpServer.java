package cn.susudad.enheng.server.http;

import cn.susudad.enheng.common.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpServer
 * @createTime 2022/8/16
 */
@Slf4j
@Component
public class HttpServer extends Server {

  private ServerBootstrap bootstrap;

  private EventLoopGroup bossGroup;

  private EventLoopGroup workerGroup;

  @Autowired
  private HttpServerProperties properties;

  private final ExecutorService serverStarter = Executors.newSingleThreadExecutor();

  @Override
  public void start() {
    serverStarter.execute(() -> {
      try {
        init();
        ChannelFuture f = bootstrap.bind(properties.getPort()).sync();
        log.info("HttpServer started success, and listen on {}", f.channel().localAddress());
        f.channel().closeFuture().sync();
      } catch (InterruptedException e) {
        log.error("HttpServer start failed", e);
      } finally {
        stop();
      }
    });
  }

  @Override
  public void stop() {
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }
    serverStarter.shutdown();
    log.info("HttpServer server closed");
  }

  private void init() {
    bootstrap = new ServerBootstrap();
    bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new CustomizableThreadFactory("http-server-boss-"));
    workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new CustomizableThreadFactory("http-server-work-"));

    HttpProcessHandler httpProcessHandler = new HttpProcessHandler(properties.getCoreThreadSize(), properties.getMaxThreadSize());

    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new HttpServerInitializer(properties, httpProcessHandler));
  }
}
