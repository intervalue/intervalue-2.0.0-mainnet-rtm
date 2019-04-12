package test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.TransactionMessage;
import one.inve.utils.SignUtil;

import java.math.BigInteger;

public class SignTest {
    public static void main(String[] args) {
        for(int i=0; i<1000; i++) {
            SignTestThread t = new SignTestThread();
            t.start();
        }
    }
}

class SignTestThread extends Thread {
    long num = 1000;

    @Override
    public void run() {
        while (num-->0) {
            TransactionMessage tm = null;
            try {
                tm = new TransactionMessage("shield salmon sport horse cool hole pool panda embark wrap fancy equip",
                        "4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7", "JJSLKKLHRSPB7XEXHPGSESUDJVN6KN7F",
                        new BigInteger("100000000"), BigInteger.valueOf(100));
            } catch (Exception e) {
                e.printStackTrace();
            }
            String message = tm.getMessage();
            JSONObject object = JSON.parseObject(message);
            boolean vefiry = false;
            try {
                vefiry = SignUtil.verify(object.getString("message"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(vefiry);
        }
    }
}