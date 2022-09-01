package cn.susudad.enheng.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * json 序列化工具 base with jackson
 */
@Slf4j
public final class JsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    // 日期转换
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

    MAPPER
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(javaTimeModule)
        .setSerializationInclusion(Include.NON_NULL);
  }


  /**
   * To json string.
   *
   * @param object the object
   * @return the string
   */
  public static String toJson(final Object object) {
    try {
      return MAPPER.writeValueAsString(object);
    } catch (IOException e) {
      log.warn("write to json string error: {}, {}", object, e);
      return "{}";
    }
  }

  /**
   * From json t.
   *
   * @param <T>    the type parameter
   * @param json   the json
   * @param tClass the t class
   * @return the t
   */
  public static <T> T fromJson(final String json, final Class<T> tClass) {
    try {
      return MAPPER.readValue(json, tClass);
    } catch (JsonProcessingException e) {
      log.warn("write to json string error: {}, {}", json, e);
      return null;
    }
  }

  /**
   * fromList
   *
   * @param json
   * @param clazz
   * @param <T>
   * @return
   */
  public static <T> List<T> fromList(final String json, final Class<T> clazz) {
    try {
      JavaType javaType = getTypeFactory().constructParametricType(List.class, clazz);
      return MAPPER.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      log.warn("write to json string error: {}, {}", json, e);
      return new ArrayList<>(1);
    }
  }

  /**
   * fromMap
   *
   * @param json
   * @param clazz
   * @return hashMap
   */
  public static <T> Map<String, T> fromMap(final String json, final Class<T> clazz) {
    try {
      JavaType javaType = getTypeFactory().constructParametricType(Map.class, String.class, clazz);
      return MAPPER.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      log.warn("write to json string error: {}, {}", json, e);
      return new HashMap<>();
    }
  }

  /**
   * @param json
   * @return HashMap
   */
  public static Map<String, Object> fromMap(final String json) {
    return fromMap(json, Object.class);
  }


  /**
   * fromStringMap.
   *
   * @param json json
   * @return hashMap map
   */
  public static Map<String, String> fromStringMap(final String json) {
    return fromMap(json, String.class);
  }

  /**
   * @param json the json
   * @return the map
   */
  public static Map<String, Object> fromObjectMap(final String json) {
    return fromMap(json, Object.class);
  }

  /**
   * @param json json
   * @return hashMap list
   */
  public static List<Map<String, Object>> fromListMap(final String json) {
    try {
      return MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {
      });
    } catch (JsonProcessingException e) {
      log.warn("write to json string error: {}, {}", json, e);
      return new ArrayList<>(1);
    }
  }

  public static <T> T fromAny(String json, TypeReference<T> valueTypeRef) {
    try {
      return MAPPER.readValue(json, valueTypeRef);
    } catch (JsonProcessingException e) {
      log.warn("write to json string error: {}, {}", json, e);
      return null;
    }
  }

  public static TypeFactory getTypeFactory() {
    return MAPPER.getTypeFactory();
  }

  public static <T> T fromAny(String json, JavaType javaType) {
    try {
      return MAPPER.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      log.warn("write to json string error: {}, {}", json, e);
      return null;
    }
  }
}
