package present.engine;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;
import present.rpc.ClientException;

/**
 * UUID utilities
 *
 * @author Bob Lee
 */
public class Uuids {

  public static int LENGTH = 36; // chars

  public static String NULL = "00000000-0000-0000-0000-000000000000";

  private static final Pattern PATTERN = Pattern.compile(
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

  /** Verifies that the given string is formatted as a UUID. */
  public static void validate(String uuid) {
    if (!isValid(uuid)) throw new ClientException("Invalid UUID: " + uuid);
  }

  /** Verifies that the given string is formatted as a UUID. */
  public static boolean isValid(String uuid) {
    return PATTERN.matcher(uuid).matches();
  }

  public static String newUuid() {
    return UUID.randomUUID().toString();
  }

  private static HashFunction hmac;

  /**
   * Sets private key used to generate UUIDs from names.
   *
   * @param encodedKey Base64-encoded key
   */
  public static void setPrivateKey(String encodedKey) {
    byte[] privateKey = Base64.getDecoder().decode(encodedKey);
    hmac = Hashing.hmacSha256(privateKey);
  }

  /** Uses HMAC SHA-256 to generate a UUID from a name. */
  public static String fromName(String name) {
    if (hmac == null) throw new IllegalStateException("Call setPrivateKey() first.");
    ByteBuffer hash = ByteBuffer.wrap(hmac.hashString(name, Charsets.UTF_8).asBytes());
    return new UUID(hash.getLong(), hash.getLong()).toString();
  }

  /** Returns a UUID with the given character repeated. Useful for testing. */
  public static String repeat(char c) {
    char[] a = new char[LENGTH];
    Arrays.fill(a, c);
    a[8] = a[13] = a[18] = a[23] = '-';
    return new String(a);
  }
}
