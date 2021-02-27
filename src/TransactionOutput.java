import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    // new owner
    public PublicKey reciepient;
    // amount of coins
    public float value;
    public String parentTransactionId;

    // constructor
    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = BlockUtil.applySha256(BlockUtil.getStringFromKey(reciepient) + value + parentTransactionId);
    }

    // check if coins are mine?!
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }

}