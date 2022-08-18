package cn.susudad.enheng.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ClientApplication
 * @createTime 2022/8/11
 */
@SpringBootApplication(scanBasePackages = {"cn.susudad.enheng"})
public class ClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClientApplication.class, args);
  }
}
