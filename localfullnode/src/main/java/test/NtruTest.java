package test;

public class NtruTest {

    public static void main(String[] args) {
        for(int i=0; i<1000; i++) {
            NtruTestThread t = new NtruTestThread();
            t.start();
        }
    }
}

class NtruTestThread extends Thread {
    @Override
    public void run() {
//        Crypto crypto = new Crypto();
//        KeyPair keyPair = crypto.getKeyPair();
//        System.out.println("pubkey: " + DSA.encryptBASE64(keyPair.getPublicKey()));
    }
}
