package cn.susudad.enheng.common.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpMsg
 * @createTime 2022/8/18
 */
@Data
public class HttpMsg implements Serializable {

	private static final long serialVersionUID = 7183478146829995583L;
	private String httpVersion;
	private List<HeaderEntry> headers;
	private byte[] content;
}
