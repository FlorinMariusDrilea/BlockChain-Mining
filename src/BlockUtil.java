// imports
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;

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

    // apply ECDSA signature and return result -> bytes
    public static byte[] applyECDSASignature(PrivateKey privateKey, String input) {
        Signature data;
        byte[] output = new byte[0];
        try {
            data = Signature.getInstance("ECDSA", "BC");
            data.initSign(privateKey);
            byte[] stringByte = input.getBytes();
            data.update(stringByte);
            byte[] realSignature = data.sign();
            output = realSignature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    // verifies signature
    public static boolean verifyECDSASignature(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    //Tacks in array of transactions and returns a merkle root.
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();
        ArrayList<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        ArrayList<String> treeLayer = previousTreeLayer;
        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i++) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }
        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }
}
