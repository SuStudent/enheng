package cn.susudad.enheng.server.redis;

import cn.susudad.enheng.common.utils.ReflectUtils;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisZSetCommands.Aggregate;
import org.springframework.data.redis.connection.RedisZSetCommands.Limit;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.connection.RedisZSetCommands.Tuple;
import org.springframework.data.redis.connection.RedisZSetCommands.Weights;
import org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description SortedSetOP
 * @createTime 2022/8/29
 */
public class SortedSetOp extends Operation {

  private final ZSetOperations<String, String> ZSET;
  private final StringRedisTemplate redisTemplate;

  public SortedSetOp(StringRedisTemplate redisTemplate) {
    this.ZSET = redisTemplate.opsForZSet();
    this.redisTemplate = redisTemplate;
  }

  private byte[] rawKey(String key) {
    byte[] rawKey = null;
    try {
      rawKey = (byte[]) ReflectUtils.invoke(ZSET, "rawKey", key);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
      rawKey = key.getBytes(StandardCharsets.UTF_8);
    }
    return rawKey;
  }


  private <T> Set<T> deserializeSet(Set<String> set, Class<T> clz) {
    if (set == null) {
      return null;
    }
    return set.stream().map(v -> deserialize(v, clz)).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private <T> TypedTuple<T> deserializeTuple(TypedTuple<String> tuple, Class<T> clz) {
    if (tuple == null) {
      return null;
    }
    return new DefaultTypedTuple<>(deserialize(tuple.getValue(), clz), tuple.getScore());
  }

  private <T> Set<TypedTuple<T>> deserializeTuples(Set<TypedTuple<String>> tuples, Class<T> clz) {
    if (tuples == null) {
      return null;
    }
    return tuples.stream().map(tup -> new DefaultTypedTuple<>(deserialize(tup.getValue(), clz), tup.getScore()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }


  public <T> TypedTuple<T> bZPopMax(String key, long timeout, TimeUnit unit, Class<T> clz) {
    return deserializeTuple(ZSET.popMax(key, timeout, unit), clz);
  }

  public <T> TypedTuple<T> bZPopMin(String key, long timeout, TimeUnit unit, Class<T> clz) {
    return deserializeTuple(ZSET.popMin(key, timeout, unit), clz);
  }

  public Boolean zAdd(String key, Object value, double score) {
    return ZSET.add(key, serialize(value), score);
  }

  public Boolean zAdd(String key, Object value, double score, ZAddArgs args) {
    return redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.zAdd(rawKey(key), score, rawKey(serialize(value)), args));
  }

  public Boolean zAddIfAbsent(String key, Object value, double score) {
    return zAdd(key, value, score, ZAddArgs.ifNotExists());
  }

  public Long zAdd(String key, Set<TypedTuple<Object>> tuples) {
    Set<TypedTuple<String>> tupleSet = tuples.stream().map(t -> new DefaultTypedTuple<>(serialize(t.getValue()), t.getScore()))
        .collect(Collectors.toSet());
    return ZSET.add(key, tupleSet);
  }

  public Long zAdd(String key, Set<TypedTuple<Object>> tuples, ZAddArgs args) {
    Set<Tuple> tupleSet = tuples.stream().map(t -> new DefaultTuple(rawKey(serialize(t.getValue())), t.getScore())).collect(Collectors.toSet());
    return redisTemplate.execute((RedisCallback<Long>) connection -> connection.zAdd(rawKey(key), tupleSet, args));
  }

  public Long zAddIfAbsent(String key, Set<TypedTuple<Object>> tuples) {
    return zAdd(key, tuples, ZAddArgs.ifNotExists());
  }

  public Long zCard(String key) {
    return ZSET.zCard(key);
  }

  public Long zCount(String key, double min, double max) {
    return ZSET.count(key, min, max);
  }

  public <T> Set<T> zDiff(String key, Collection<String> otherKeys, Class<T> clz) {
    return deserializeSet(ZSET.difference(key, otherKeys), clz);
  }

  public <T> Set<TypedTuple<T>> zDiffWithScores(String key, Collection<String> otherKeys, Class<T> clz) {
    return deserializeTuples(ZSET.differenceWithScores(key, otherKeys), clz);
  }

  public Long zDiffStore(String key, Collection<String> otherKeys, String destKey) {
    return ZSET.differenceAndStore(key, otherKeys, destKey);
  }

  public Double zIncrBy(String key, Object value, double delta) {
    return ZSET.incrementScore(key, serialize(value), delta);
  }

  public <T> Set<T> zInter(String key, Collection<String> otherKeys, Class<T> clz) {
    return deserializeSet(ZSET.intersect(key, otherKeys), clz);
  }

  public <T> Set<TypedTuple<T>> zInterWithScores(String key, Collection<String> otherKeys, Class<T> clz) {
    return deserializeTuples(ZSET.intersectWithScores(key, otherKeys), clz);
  }

  public <T> Set<TypedTuple<T>> zInterWithScores(String key, Collection<String> otherKeys, Aggregate aggregate, Weights weights, Class<T> clz) {
    return deserializeTuples(ZSET.intersectWithScores(key, otherKeys, aggregate, weights), clz);
  }

  public Long zInterStore(String key, Collection<String> otherKeys, String destKey) {
    return ZSET.intersectAndStore(key, otherKeys, destKey);
  }

  public Long zInterStore(String key, Collection<String> otherKeys, String destKey, Aggregate aggregate, Weights weights) {
    return ZSET.intersectAndStore(key, otherKeys, destKey, aggregate, weights);
  }

  public Long zLexCount(String key, Range range) {
    return ZSET.lexCount(key, range);
  }

  public List<Double> zMScore(String key, Object... o) {
    String[] array = Arrays.stream(o).map(this::serialize).toArray(String[]::new);
    return ZSET.score(key, array);
  }

  public <T> TypedTuple<T> zPopMax(String key, Class<T> clz) {
    return deserializeTuple(ZSET.popMax(key), clz);
  }

  public <T> Set<TypedTuple<T>> zPopMax(String key, long count, Class<T> clz) {
    return deserializeTuples(ZSET.popMax(key, count), clz);
  }

  public <T> TypedTuple<T> zPopMin(String key, Class<T> clz) {
    return deserializeTuple(ZSET.popMin(key), clz);
  }

  public <T> Set<TypedTuple<T>> zPopMin(String key, long count, Class<T> clz) {
    return deserializeTuples(ZSET.popMin(key, count), clz);
  }

  public <T> T zRandMember(String key, Class<T> clz) {
    return deserialize(ZSET.randomMember(key), clz);
  }

  public <T> TypedTuple<T> zRandMemberWithScore(String key, Class<T> clz) {
    return deserializeTuple(ZSET.randomMemberWithScore(key), clz);
  }

  public <T> List<T> zRandMember(String key, long count, Class<T> clz) {
    List<String> members = null;
    if (count > 0) {
      Set<String> set = ZSET.distinctRandomMembers(key, count);
      if (set != null) {
        members = new ArrayList<>(set);
      }
    } else {
      members = ZSET.randomMembers(key, count);
    }
    if (members == null) {
      return null;
    }
    return members.stream().map(v -> deserialize(v, clz)).collect(Collectors.toList());
  }

  public <T> List<TypedTuple<T>> zRandMemberWithScore(String key, long count, Class<T> clz) {
    List<TypedTuple<String>> members = null;
    if (count > 0) {
      Set<TypedTuple<String>> set = ZSET.distinctRandomMembersWithScore(key, count);
      if (set != null) {
        members = new ArrayList<>(set);
      }
    } else {
      members = ZSET.randomMembersWithScore(key, count);
    }
    if (members == null) {
      return null;
    }
    return members.stream().map(v -> new DefaultTypedTuple<>(deserialize(v.getValue(), clz), v.getScore())).collect(Collectors.toList());
  }

  public <T> Set<T> zRange(String key, long start, long end, Class<T> clz) {
    return deserializeSet(ZSET.range(key, start, end), clz);
  }

  public <T> Set<TypedTuple<T>> zRangeWithScores(String key, long start, long end, Class<T> clz) {
    return deserializeTuples(ZSET.rangeWithScores(key, start, end), clz);
  }

  public <T> Set<T> zRangeByLex(String key, Range range, Limit limit, Class<T> clz) {
    return deserializeSet(ZSET.rangeByLex(key, range, limit), clz);
  }

  public <T> Set<T> zRangeByScore(String key, double min, double max, Class<T> clz) {
    return deserializeSet(ZSET.rangeByScore(key, min, max), clz);
  }

  public <T> Set<T> zRangeByScore(String key, double min, double max, long offset, long count, Class<T> clz) {
    return deserializeSet(ZSET.rangeByScore(key, min, max, offset, count), clz);
  }

  public <T> Set<TypedTuple<T>> zRangeByScoreWithScores(String key, double min, double max, Class<T> clz) {
    return deserializeTuples(ZSET.rangeByScoreWithScores(key, min, max), clz);
  }

  public <T> Set<TypedTuple<T>> zRangeByScoreWithScores(String key, double min, double max, long offset, long count, Class<T> clz) {
    return deserializeTuples(ZSET.rangeByScoreWithScores(key, min, max, offset, count), clz);
  }

  public Long zRank(String key, Object o) {
    return ZSET.rank(key, serialize(o));
  }

  public Long zRem(String key, Object... values) {
    return ZSET.remove(key, Arrays.stream(values).map(this::serialize).toArray(String[]::new));
  }

  public Long zRemRangeByLex(String key, Range range) {
    return ZSET.removeRangeByLex(key, range);
  }

  public Long zRemRangeByScore(String key, double min, double max) {
    return ZSET.removeRangeByScore(key, min, max);
  }

  public <T> Set<T> zRevRange(String key, long start, long end, Class<T> clz) {
    return deserializeSet(ZSET.reverseRange(key, start, end), clz);
  }

  public <T> Set<TypedTuple<T>> zRevRangeWithScores(String key, long start, long end, Class<T> clz) {
    return deserializeTuples(ZSET.reverseRangeWithScores(key, start, end), clz);
  }

  public <T> Set<T> zRevRangeByLex(String key, Range range, Limit limit, Class<T> clz) {
    return deserializeSet(ZSET.reverseRangeByLex(key, range, limit), clz);
  }

  public <T> Set<T> zRevRangeByScore(String key, double min, double max, Class<T> clz) {
    return deserializeSet(ZSET.reverseRangeByScore(key, min, max), clz);
  }

  public <T> Set<T> zRevRangeByScore(String key, double min, double max, long offset, long count, Class<T> clz) {
    return deserializeSet(ZSET.reverseRangeByScore(key, min, max, offset, count), clz);
  }

  public <T> Set<TypedTuple<T>> zRevRangeByScoreWithScores(String key, double min, double max, Class<T> clz) {
    return deserializeTuples(ZSET.reverseRangeByScoreWithScores(key, min, max), clz);
  }

  public <T> Set<TypedTuple<T>> zRevRangeByScoreWithScores(String key, double min, double max, long offset, long count, Class<T> clz) {
    return deserializeTuples(ZSET.reverseRangeByScoreWithScores(key, min, max, offset, count), clz);
  }

  public Long reverseRank(String key, Object o) {
    return ZSET.reverseRank(key, serialize(o));
  }

  public <T> Cursor<TypedTuple<T>> zScan(String key, ScanOptions options, Class<T> clz) {
    Cursor<TypedTuple<String>> scan = ZSET.scan(key, options);
    return new ConvertingCursor<>(scan, (tuple) -> new DefaultTypedTuple<>(deserialize(tuple.getValue(), clz), tuple.getScore()));
  }

  public Double zScore(String key, Object o) {
    return ZSET.score(key, serialize(0));
  }

  public <T> Set<T> zUnion(String key, Collection<String> otherKeys, Class<T> clz) {
    return deserializeSet(ZSET.union(key, otherKeys), clz);
  }

  public <T> Set<TypedTuple<T>> zUnionWithScores(String key, Collection<String> otherKeys, Class<T> clz) {
    return deserializeTuples(ZSET.unionWithScores(key, otherKeys), clz);
  }

  public <T> Set<TypedTuple<T>> zUnionWithScores(String key, Collection<String> otherKeys, Aggregate aggregate, Weights weights, Class<T> clz) {
    return deserializeTuples(ZSET.unionWithScores(key, otherKeys, aggregate, weights), clz);
  }

  public Long zUnionStore(String key, Collection<String> otherKeys, String destKey) {
    return ZSET.unionAndStore(key, otherKeys, destKey);
  }


  public Long zUnionStore(String key, Collection<String> otherKeys, String destKey, Aggregate aggregate, Weights weights) {
    return ZSET.unionAndStore(key, otherKeys, destKey, aggregate, weights);
  }

}
