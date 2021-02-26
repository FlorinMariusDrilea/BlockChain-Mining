// imports
import java.security.MessageDigest;

public class BlockUtil {
    // Apply key (Sha256) to a string and returns the result
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashKey = digest.digest(input.getBytes("UTF-8"));
            // contains the hash in decimal format
            StringBuffer decimalString = new StringBuffer();
            for(int i = 0; i < hashKey.length; i++) {
                String hexDecimal = Integer.toHexString(0xff & hashKey[i]);
                if (hexDecimal.length() == 1) decimalString.append('0');
                decimalString.append(hexDecimal);
            }
            return decimalString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
