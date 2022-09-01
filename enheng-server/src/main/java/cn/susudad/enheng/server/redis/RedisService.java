package cn.susudad.enheng.server.redis;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description RedisService
 * @createTime 2022/8/15
 */
public class RedisService {

  public final HashOp HASH;
  public final ListOp LIST;
  public final StringOp STRING;
  public final SetOp SET;
  public final SortedSetOp ZSET;
  private final StringRedisTemplate redisTemplate;

  public RedisService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.HASH = new HashOp(redisTemplate);
    this.LIST = new ListOp(redisTemplate);
    this.STRING = new StringOp(redisTemplate);
    this.SET = new SetOp(redisTemplate);
    this.ZSET = new SortedSetOp(redisTemplate);
  }

  public Boolean del(String key) {
    return redisTemplate.delete(key);
  }

  public Long del(Collection<String> keys) {
    return redisTemplate.delete(keys);
  }

  public Boolean exists(String key) {
    return redisTemplate.hasKey(key);
  }

  public Boolean expire(String key, long timeout, TimeUnit unit) {
    return redisTemplate.expire(key, timeout, unit);
  }

  public Boolean expire(String key, Duration timeout) {
    return redisTemplate.expire(key, timeout);
  }

  public Boolean expireAt(String key, Date date) {
    return redisTemplate.expireAt(key, date);
  }

  public Boolean persist(String key) {
    return redisTemplate.persist(key);
  }

  public Long pTTL(String key) {
    return redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
  }

  public Long ttl(String key) {
    return redisTemplate.getExpire(key);
  }

  public String randomKey() {
    return redisTemplate.randomKey();
  }

  public void rename(String oldKey, String newKey) {
    redisTemplate.rename(oldKey, newKey);
  }

  public void renameNX(String oldKey, String newKey) {
    redisTemplate.renameIfAbsent(oldKey, newKey);
  }

  public DataType type(String key) {
    return redisTemplate.type(key);
  }

  public Cursor<String> scan(ScanOptions options) {
    return redisTemplate.scan(options);
  }

  public <T> T eval(byte[] script, ReturnType returnType, int numKeys, byte[]... keysAndArgs){
    return redisTemplate.execute((RedisCallback<T>) connection -> connection.eval(script, returnType, numKeys, keysAndArgs));
  }
}
