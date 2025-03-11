package cn.susudad.enheng.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GzipUtils description
 *
 * @author suyiyi@boke.com
 * @version 1.0.0
 * @date 2025-03-11 16:15
 */
public class GzipUtils {

	public static byte[] compress(byte[] data) {
		try (
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(out)
		) {
			gzip.write(data);
			gzip.close();
			out.close();
			return out.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}
	}

	public static byte[] decompress(byte[] compressedData) {
		try (
				ByteArrayInputStream byteStream = new ByteArrayInputStream(compressedData);
				GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
		) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = gzipStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			gzipStream.close();
			output.close();
			byteStream.close();
			return output.toByteArray();
		} catch (Exception e) {
			return new byte[0];
		}
	}
}
