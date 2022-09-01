package cn.susudad.enheng.server.redis;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description SetOp
 * @createTime 2022/8/27
 */
public class SetOp extends Operation {

  private final SetOperations<String, String> set;

  public SetOp(StringRedisTemplate redisTemplate) {
    this.set = redisTemplate.opsForSet();
  }

  public Long sAdd(String key, Object... values) {
    String[] array = Arrays.stream(values).map(this::serialize).toArray(String[]::new);
    return set.add(key, array);
  }

  public Long sCard(String key) {
    return set.size(key);
  }

  public <T> Set<T> sDiff(String key, Collection<String> otherKeys, Class<T> clz) {
    Set<String> difference = set.difference(key, otherKeys);
    if (difference == null) {
      return null;
    }
    return difference.stream().map(v -> this.deserialize(v, clz)).collect(Collectors.toSet());
  }

  public Long sDiffStore(String key, Collection<String> otherKeys, String destKey) {
    return set.differenceAndStore(key, otherKeys, destKey);
  }

  public <T> Set<T> sInter(String key, Collection<String> otherKeys, Class<T> clz) {
    Set<String> intersect = set.intersect(key, otherKeys);
    if (intersect == null) {
      return null;
    }
    return intersect.stream().map(v -> this.deserialize(v, clz)).collect(Collectors.toSet());
  }

  public Long sInterStore(String key, Collection<String> otherKeys, String destKey) {
    return set.intersectAndStore(key, otherKeys, destKey);
  }

  public Boolean sIsMember(String key, Object o) {
    return set.isMember(key, serialize(o));
  }

  public <T> Set<T> sMembers(String key, Class<T> clz) {
    Set<String> members = set.members(key);
    if (members == null) {
      return null;
    }
    return members.stream().map(v -> this.deserialize(v, clz)).collect(Collectors.toSet());
  }

  public Map<Object, Boolean> sMIsMember(String key, Object... objects) {
    String[] array = Arrays.stream(objects).map(this::serialize).toArray(String[]::new);
    Map<Object, Boolean> member = set.isMember(key, array);
    if (member == null) {
      return null;
    }
    Map<Object, Boolean> result = new HashMap<>();
    for (int i = 0; i < array.length; i++) {
      result.put(objects[i], member.get(array[i]));
    }
    return result;
  }

  public Boolean sMove(String key, Object value, String destKey) {
    return set.move(key, serialize(value), destKey);
  }

  public <T> T sPop(String key, Class<T> clz) {
    return deserialize(set.pop(key), clz);
  }

  public <T> List<T> sPop(String key, long count, Class<T> clz) {
    List<String> pop = set.pop(key, count);
    if (pop == null) {
      return null;
    }
    return pop.stream().map(v -> this.deserialize(v, clz)).collect(Collectors.toList());
  }

  public <T> List<T> sRandMember(String key, long count, Class<T> clz) {
    List<String> members = set.randomMembers(key, count);
    if (members == null) {
      return null;
    }
    return members.stream().map(v -> this.deserialize(v, clz)).collect(Collectors.toList());
  }

  public Long remove(String key, Object... values) {
    String[] array = Arrays.stream(values).map(this::serialize).toArray(String[]::new);
    return set.remove(key, array);
  }

  public <T> Cursor<T> scan(String key, ScanOptions options, Class<T> clz) {
    return new ConvertingCursor<>(set.scan(key, options), (s -> this.deserialize(s, clz)));
  }

  public <T> Set<T> sUnion(String key, Collection<String> otherKeys, Class<T> clz) {
    Set<String> union = set.union(key, otherKeys);
    if (union == null) {
      return null;
    }
    return union.stream().map(s -> this.deserialize(s, clz)).collect(Collectors.toSet());
  }

  public Long sUnionStore(String key, Collection<String> otherKeys, String destKey) {
    return set.unionAndStore(key, otherKeys, destKey);
  }

}
