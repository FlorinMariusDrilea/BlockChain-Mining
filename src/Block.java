// imports
import java.util.ArrayList;
import java.util.Date;
import com.google.gson.GsonBuilder;

public class Block {
    // variables
    public String hashKey;
    public String previousHashKey;
    private String data;
    private long timeStamp;
    private int nonce;

    public static ArrayList<Block> blockChain = new ArrayList<>();
    public static int level = 4;

    // constructor
    public Block(String data, String previousHashKey){
        this.data = data;
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
        String target = new String(new char[level]).replace('\0', '0');
        while(!hashKey.substring(0, level).equals(target)) {
            nonce++;
            hashKey = calculateHashKey();
        }
        System.out.println("Mined a block chain! " + hashKey);
    }

    // validation of block chains
    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String target = new String(new char[level]).replace('\0', '0');
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
            i++;
        }
        return true;
    }

    public static void main(String[] args) {
        // mine blocks and add into the blockchain ArrayList

        blockChain.add(new Block("First Block", "0"));
        System.out.println("Mining block 1......");
        blockChain.get(0).miningBlocks(level);

        blockChain.add(new Block("Second Block", blockChain.get(blockChain.size() - 1).hashKey));
        System.out.println("Mining block 2......");
        blockChain.get(1).miningBlocks(level);

        blockChain.add(new Block("Third Block", blockChain.get(blockChain.size() - 1).hashKey));
        System.out.println("Mining block 3......");
        blockChain.get(2).miningBlocks(level);

        boolean answer = isChainValid();

        if(answer) {
            System.out.println("\nBlockchain is Valid!");
        } else {
            System.out.println("\nBlockchain is not Valid!");
        }

        String blockChainJsonFile = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        System.out.println("\nThe Block chain: ");
        System.out.println(blockChainJsonFile);
    }
}
