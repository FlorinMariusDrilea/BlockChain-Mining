// imports
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.GsonBuilder;

public class Block {
    // variables
    public String hashKey;
    public String previousHashKey;
    private String data;
    private long timeStamp;
    private int nonce;
    public String merkleRoot;
    public static Transaction genesisTransaction;
    public static float minimumTransaction = 0.1f;

    public static Wallet walletA;
    public static Wallet walletB;

    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
    public static ArrayList<Block> blockChain = new ArrayList<>();
    public static int level = 4;


    // constructor
    public Block(String previousHashKey){
        this.previousHashKey = previousHashKey;
        this.timeStamp = new Date().getTime();
        this.hashKey = calculateHashKey();
    }


    public String calculateHashKey() {
        String calculatedHashKey = BlockUtil.applySha256(
                previousHashKey +
                        timeStamp +
                        nonce +
                        data);
        return calculatedHashKey;
    }

    public void miningBlocks(int level) {
        merkleRoot = BlockUtil.getMerkleRoot(transactions);
        String target = new String(new char[level]).replace('\0', '0');
        while(!hashKey.substring(0, level).equals(target)) {
            nonce++;
            hashKey = calculateHashKey();
        }
        System.out.println("Mined a block chain! " + hashKey);
    }

    //Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((previousHashKey != "0")) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

    // validation of block chains
    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String target = new String(new char[level]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        int i = 1;
        
        // check through hashes using loop
        while(i < blockChain.size()) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i - 1);

            //comparing hash key and calculated hash key
            if(!currentBlock.hashKey.equals(currentBlock.calculateHashKey())) {
                System.out.println("Current hash keys are not equal!");
                return false;
            }

            if(!previousBlock.hashKey.equals(previousBlock.calculateHashKey())) {
                System.out.println("Previous hash keys are not equal!");
                return false;
            }

            //check if hash is solved
            if(!currentBlock.hashKey.substring(0, level).equals(target)) {
                System.out.println("This block was not mined!");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputValue() != currentTransaction.getOutputValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.recipient) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }
            i++;
        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.miningBlocks(level);
        blockchain.add(newBlock);
    }

    public static void main(String[] args) {
        //add our blocks to the blockchain ArrayList:
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

        //Create wallets:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //testing
        Block block1 = new Block(genesis.hashKey);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hashKey);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hashKey);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();
    }
}