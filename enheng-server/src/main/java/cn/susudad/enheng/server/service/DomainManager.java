package cn.susudad.enheng.server.service;

import cn.susudad.enheng.server.server.DomainConnection;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description DomainManager
 * @createTime 2022/8/16
 */
@Slf4j
public class DomainManager {

  private static final ConcurrentHashMap<String, DomainConnection> DOMAIN_MAP = new ConcurrentHashMap<>();

  public static boolean add(DomainConnection connection) {
    return DOMAIN_MAP.putIfAbsent(connection.getSubdomain(), connection) == null;
  }

  public static void remove(DomainConnection connection) {
    remove(connection.getSubdomain());
  }

  public static void remove(String subdomain) {
    DOMAIN_MAP.remove(subdomain);
  }

  public static DomainConnection get(String subdomain) {
    return DOMAIN_MAP.get(subdomain);
  }
}
