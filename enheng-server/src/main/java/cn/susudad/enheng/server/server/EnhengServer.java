package cn.susudad.enheng.server.server;

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
 * @description EnhengServer
 * @createTime 2022/8/11
 */
@Component
@Slf4j
public class EnhengServer extends Server {

  private ServerBootstrap bootstrap;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  @Autowired
  private EnhengServerProperties enhengServerProperties;
  private final ExecutorService serverStarter = Executors.newSingleThreadExecutor();

  @Override
  public void start() {
    serverStarter.execute(() -> {
      try {
        init();
        ChannelFuture f = bootstrap.bind(enhengServerProperties.getPort()).sync();
        log.info("EnhengServer started success, and listen on {}", f.channel().localAddress());
        f.channel().closeFuture().sync();
      } catch (InterruptedException e) {
        log.error("EnhengServer start failed", e);
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
    log.info("EnhengServer server closed");
  }

  private void init() {
    bootstrap = new ServerBootstrap();
    bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new CustomizableThreadFactory("enheng-server-boss-"));
    workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new CustomizableThreadFactory("enheng-server-work-"));
    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 1024)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new EnhengServerInitializer(enhengServerProperties));
  }
}
