package cn.susudad.enheng.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * HeaderEntry description
 *
 * @author suyiyi@boke.com
 * @version 1.0.0
 * @date 2025-03-11 16:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderEntry implements Serializable {
	private static final long serialVersionUID = -8203276681638356535L;
	private String key;
	private String value;
}
