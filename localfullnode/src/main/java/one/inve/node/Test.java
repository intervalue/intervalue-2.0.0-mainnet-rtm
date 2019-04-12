package one.inve.node;

import com.alibaba.fastjson.JSONArray;
import one.inve.core.Config;
import one.inve.util.PathUtils;

import java.io.File;

/**
 * @Description
 * @Author Clarelau61803@gmail.com
 * @Date 2018/10/24 0024 下午 3:03
 **/
public class Test {
    public static void main(String[] args) {

        final int startNum = 50;
        final int size = 4;
        for (int i=0; i<size; i++) {
            String[] args1 = new String[11];

            args1[0] = "--Ice.Config=src" + File.separator + "main"+File.separator+"config"+File.separator+"default.config";
            args1[1] = "-Dtest.clearDb=0";
            args1[2] = "-Dmyself.gossipPort=" + (20005 + (i+startNum)*10);
            args1[3] = "-Dmyself.rpcPort=" + (20004 + (i+startNum)*10);
            args1[4] = "-Dmyself.httpPort=" + (20003 + (i+startNum)*10);
            args1[5] = "-Ddatabase.reset=false";
            args1[6] = "-Ddatabase.dir=" + PathUtils.getDataFileDir() + "/contract/database" + i;
            args1[7] = "-Dmapping.buf.dir=" + PathUtils.getDataFileDir() + "/contract/buf" + i;
            args1[8] = "-Dtest.multiple=1";
            args1[9] = "-Dtest.prefix=" + i;

            if (i==0) {
                args1[10] = "-Dmnemonic='" + Config.FOUNDATION_MNEMONIC + "'";
            } else {
                args1[10] = "";
            }

            TestThread testThread = new TestThread(args1);
            testThread.start();

//            Thread.sleep(1500);
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
