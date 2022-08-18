package cn.susudad.enheng.common.model;

import java.util.List;
import java.util.Map.Entry;
import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpMsg
 * @createTime 2022/8/18
 */
@Data
public class HttpMsg {

  private String httpVersion;
  private List<Entry<String, String>> headers;
  private byte[] content;

}
