package cn.susudad.enheng.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ServerApplication
 * @createTime 2022/8/11
 */
@SpringBootApplication(scanBasePackages = {"cn.susudad.enheng"})
public class ServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ServerApplication.class, args);
  }

}
