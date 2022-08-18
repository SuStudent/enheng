package cn.susudad.enheng.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @Description RSAUtils
 * @createTime 2021/4/13
 */
public class RSAUtils {

  private static final String KEY_ALGORITHM_RSA = "RSA";

  private static final int KEY_SIZE = 1024;

  public static String[] getKeys() throws GeneralSecurityException {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    keyPairGen.initialize(KEY_SIZE);
    KeyPair keyPair = keyPairGen.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

    String publicKeyStr = getPublicKeyStr(publicKey);
    String privateKeyStr = getPrivateKeyStr(privateKey);

    System.out.println("公钥\r\n" + publicKeyStr);
    System.out.println("私钥\r\n" + privateKeyStr);
    String[] keys = new String[2];
    keys[0] = publicKeyStr;
    keys[1] = privateKeyStr;
    return keys;
  }

  public static String getPrivateKeyStr(PrivateKey privateKey) {
    return Base64.getEncoder().encodeToString(privateKey.getEncoded());
  }

  public static String getPublicKeyStr(PublicKey publicKey) {
    return Base64.getEncoder().encodeToString(publicKey.getEncoded());
  }

  private static PublicKey getPublicKey(String key) throws GeneralSecurityException {
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(key));
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
    return keyFactory.generatePublic(keySpec);
  }

  private static PrivateKey getPrivateKey(String key) throws GeneralSecurityException {
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
    return keyFactory.generatePrivate(keySpec);
  }

  /**
   * 私钥加密
   *
   * @param data 明文
   * @param key  私钥
   * @return 密文
   * @throws GeneralSecurityException IOException
   */
  public static String encryptByPrivateKey(String data, String key) throws GeneralSecurityException, IOException {
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey(key);
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    // 模长
    int keyLength = privateKey.getModulus().bitLength() / 8;
    // 加密最大明文长度
    int blockSize = keyLength - 11;
    byte[] read = read(data.getBytes(StandardCharsets.UTF_8), blockSize, cipher);
    return Base64.getEncoder().encodeToString(read);
  }


  /**
   * 公钥加密
   *
   * @param data 明文
   * @param key  公钥
   * @return 密文
   * @throws Exception
   */
  public static String encryptByPublicKey(String data, String key) throws GeneralSecurityException, IOException {
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    RSAPublicKey publicKey = (RSAPublicKey) getPublicKey(key);
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    // 模长
    int keyLength = publicKey.getModulus().bitLength() / 8;
    // 加密最大明文长度
    int blockSize = keyLength - 11;
    byte[] read = read(data.getBytes(StandardCharsets.UTF_8), blockSize, cipher);
    return Base64.getEncoder().encodeToString(read);
  }

  /**
   * 公钥解密
   *
   * @param data 密文
   * @param key  公钥
   * @return 明文
   * @throws GeneralSecurityException
   */
  public static String decryptByPublicKey(String data, String key) throws GeneralSecurityException, IOException {
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    RSAPublicKey publicKey = (RSAPublicKey) getPublicKey(key);
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    // 模长
    int keyLength = publicKey.getModulus().bitLength() / 8;
    return new String(read(Base64.getDecoder().decode(data), keyLength, cipher));
  }

  /**
   * 私钥解密
   *
   * @param data 密文
   * @param key  私钥
   * @return 明文
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static String decryptByPrivateKey(String data, String key) throws GeneralSecurityException, IOException {
    KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
    Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
    RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey(key);
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    // 模长
    int keyLength = privateKey.getModulus().bitLength() / 8;
    return new String(read(Base64.getDecoder().decode(data), keyLength, cipher));
  }


  private static byte[] read(byte[] bytes, int blockSize, Cipher cipher) throws IOException, GeneralSecurityException {
    int blockCount = (int) Math.ceil((double) bytes.length / (double) blockSize);
    try (ByteArrayOutputStream bout = new ByteArrayOutputStream(bytes.length)) {
      for (int i = 0; i < blockCount; i++) {
        int start = i * blockSize;
        int end = Math.min(bytes.length - start, blockSize);
        bout.write(cipher.doFinal(bytes, start, end));
      }
      return bout.toByteArray();
    }
  }

  public static void main(String[] args) throws GeneralSecurityException, IOException {
    String[] keys = getKeys();
    String publicKey = keys[0];
    String privateKey = keys[1];
    String asd = "222222asd23as2d2fa1dsasg123sfdfsda\n";

    String encodeStr = encryptByPublicKey(asd, publicKey);
    System.out.println("加密 :" + encodeStr);

    String s = decryptByPrivateKey(encodeStr, privateKey);
    System.out.println("解密 :" + s);
  }
}
