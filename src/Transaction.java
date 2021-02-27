import java.security.*;
import java.util.ArrayList;

public class Transaction {
    // id = hash of transaction
    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public float value;
    // cryptic signature
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    // counter of transactions
    private static int sequence = 0;

    // constructor
    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    // hash calculator
    private String calculateHash() {
        sequence++;
        return BlockUtil.applySha256(BlockUtil.getStringFromKey(sender) +
                        BlockUtil.getStringFromKey(recipient) +
                        Float.toString(value) +
                        sequence
        );
    }

    // signs all data
    public void generateSignature(PrivateKey privateKey) {
        String data = BlockUtil.getStringFromKey(sender) +
                BlockUtil.getStringFromKey(recipient) +
                value;
        signature = BlockUtil.applyECDSASignature(privateKey, data);
    }

    // verification of signature
    public boolean verifySignature() {
        String data = BlockUtil.getStringFromKey(sender) +
                BlockUtil.getStringFromKey(recipient) +
                value;
        return BlockUtil.verifyECDSASignature(sender, data, signature);
    }

    // return true if transactions was completed
    public boolean processTransaction() {
        if(verifySignature() == false) {
            System.out.println("#Transaction Signature failed to be verified!");
            return false;
        }

        for(TransactionInput i : inputs) {
            i.UTXO = Block.UTXOs.get(i.transactionOutputId);
        }

        // check if trans. is valid
        if(getInputValue() < Block.minimumTransaction) {
            System.out.println("#Transaction input is too small, having only: " + getInputValue());
            return false;
        }

        // transaction output
        float leftOver = getInputValue() - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        // add to Unspent list
        for(TransactionOutput o : outputs) {
            Block.UTXOs.put(o.id , o);
        }

        // remove transaction inputs from UTXO lists
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            Block.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    //returns sum of inputs(UTXOs) values
    public float getInputValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
}
