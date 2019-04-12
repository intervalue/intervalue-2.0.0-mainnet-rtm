package one.inve.node;

import com.alibaba.fastjson.JSONArray;

import java.io.File;

/**
 * @Description
 * @Author Clarelau61803@gmail.com
 * @Date 2018/10/11 0011 下午 4:29
 **/
public class Test {
    public static void main(String[] args) throws InterruptedException {
        final int startNum = 1;
        final int size = 3;
        final long interval = 20;
        String[][] argss = new String[size][];
        for (int i=0; i<size; i++) {
            argss[i] = new String[9];
            argss[i][0] = "--Ice.Config=src" + File.separator + "main"+File.separator+"config"+File.separator+"default.config";
            argss[i][1] = "-Denv=dev";
            argss[i][2] = "-Dmyself.gossipPort=" + (40005 + (i+startNum)*10);
            argss[i][3] = "-Dmyself.rpcPort=" + (40004 + (i+startNum)*10);
            argss[i][4] = "-Dmyself.httpPort=" + (40003 + (i+startNum)*10);
            argss[i][5] = "-Dshard.size=2";
            argss[i][6] = "-Dshard.node.size=4";
            argss[i][7] = "-Dtest.prefix="+(i+startNum);
            argss[i][8] = "-Dsharding.static=1";

            TestThread testThread = new TestThread(argss[i]);
            testThread.start();
            Thread.sleep(interval);
        }
    }
}

class TestThread extends Thread {
    String[] args;

    public TestThread(String[] args) {
        this.args = args;
    }

    @Override
    public void run() {
        System.out.println(JSONArray.toJSONString(args));
        Main.main(args);
    }
}
