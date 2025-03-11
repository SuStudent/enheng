package cn.susudad.enheng.common.serialize;

import cn.susudad.enheng.common.protocol.SerTypeEnum;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * HessianSerialized description
 *
 * @author suyiyi@boke.com
 * @version 1.0.0
 * @date 2025-03-11 15:39
 */
@Component
public class HessianSerialized implements SerializedStrategy {

	@Override
	public SerTypeEnum support() {
		return SerTypeEnum.HESSIAN;
	}

	@Override
	public byte[] serialize(Object obj) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			Hessian2Output hessian2Output = new Hessian2Output(bos);
			hessian2Output.setCloseStreamOnClose(true);
			hessian2Output.writeObject(obj);
			hessian2Output.flush();
			hessian2Output.close();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T deserialize(byte[] bytes, Class<T> cls) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
			Hessian2Input hessian2Input = new Hessian2Input(bis);
			T result = (T) hessian2Input.readObject(cls);
			hessian2Input.close();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
