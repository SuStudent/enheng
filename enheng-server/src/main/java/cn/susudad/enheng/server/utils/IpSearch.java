package cn.susudad.enheng.server.utils;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * IpSearch description
 *
 * @author suyiyi@boke.com
 * @version 1.0.0
 * @date 2024-01-16 16:58:17
 */
@Slf4j
public class IpSearch {

	private static Searcher searcher = null;

	static {
		ClassPathResource resource = new ClassPathResource("ip2region.xdb");
		try {
			InputStream stream = resource.getInputStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int n;
			while (-1 != (n = stream.read(buffer))) {
				output.write(buffer, 0, n);
			}

			byte[] bytes = output.toByteArray();
			log.info("开始加载,字节数={}",bytes.length);
			searcher = Searcher.newWithBuffer(bytes);
		} catch (Exception e) {
			log.warn("加载异常", e);
		}
	}

	public static String getRegion(String ip) {
		try {
			return searcher.search(ip);
		} catch (Exception e) {
			log.warn("获取IP归属地异常，ip={}", ip, e);
		}
		return "";
	}
}
