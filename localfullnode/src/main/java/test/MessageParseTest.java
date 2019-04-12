package test;

import com.alibaba.fastjson.JSON;
import one.inve.bean.message.SnapshotMessage;

public class MessageParseTest {
    public static void main(String[] args) {
        String json = "{\"amount\":0,\"eHash\":\"BGIVVm6l09JB8RsP0QqJ1eChDvnq7ImNtTwDxjiMtE/IRzxEf+Q837qQANVvrNDn\",\"fee\":0,\"fromAddress\":\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\",\"hash\":\"33AOPTIrFX/as/U7DnEkMUt9pQ5JkE4bwDqm/UKIy//uWlEHmxNgaC2mtUg7p7b9EVFQGh0cQrgn4hZLhd4c8f/TA=\",\"id\":2,\"isStable\":true,\"isValid\":true,\"lastIdx\":true,\"message\":\"{\\\"preHash\\\":\\\"32azlurRclF6n/HdBJeVe/5/Aclg3oZCGykXyOHOYVi15MQA8pXuLdty6dex2gdfO2XBNwMeX8myEqiBDpaeZe5A==\\\",\\\"snapVersion\\\":2,\\\"signature\\\":\\\"33AOPTIrFX/as/U7DnEkMUt9pQ5JkE4bwDqm/UKIy//uWlEHmxNgaC2mtUg7p7b9EVFQGh0cQrgn4hZLhd4c8f/TA=\\\",\\\"vers\\\":\\\"1.0dev\\\",\\\"fromAddress\\\":\\\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\\\",\\\"type\\\":3,\\\"pubkey\\\":\\\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\\\",\\\"snapshotPoint\\\":{\\\"contributions\\\":{\\\"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7\\\":369,\\\"NYGOLH7MTYWWU2FKCGDQQJFTXP5XQIM7\\\":369,\\\"XXISHISWEEEWTBVEVFNWMCSZKV7EFMIG\\\":378,\\\"3EC2OMPD7WLZSF23G3PTKY74CXF2UEXF\\\":366,\\\"57GTNZAD2CWTQVHCAJ2PBOMOLTYKUMC3\\\":371,\\\"L4O735CWOWGM6KNBN246TXSRV47TQ6X5\\\":385,\\\"CWZUFRBOQQNFMR46AHTFPAOOCAYYJVC6\\\":370,\\\"ADOJF6MDMDH6D3NBSEXBWHSBUPSAHHPK\\\":385},\\\"rewardRatio\\\":0.3,\\\"msgMaxId\\\":1,\\\"totalFee\\\":0,\\\"msgHashTreeRoot\\\":\\\"32azlurRclF6n/HdBJeVe/5/Aclg3oZCGykXyOHOYVi15MQA8pXuLdty6dex2gdfO2XBNwMeX8myEqiBDpaeZe5A==\\\",\\\"eventBody\\\":{\\\"generation\\\":1440,\\\"famous\\\":true,\\\"otherId\\\":2,\\\"consTimestamp\\\":\\\"2019-01-21T07:20:35.288Z\\\",\\\"creatorSeq\\\":741,\\\"signature\\\":\\\"W02kgQX7ZFvTr59lCGyObzimPKq9cdBsZB784ESeSpkp//zlGHrvBck0l53qaKFQIcQwu97jI2FYu/ioPCGm1RguLB4Ybk5F7lclOUsVbNT0Acc5vruKxlSHV062M9tF4d8ebeHzC39GbR/dQBlbEbkSMsapRk5r5JaG9ot/VbI=\\\",\\\"creatorId\\\":1,\\\"shardId\\\":0,\\\"timeCreated\\\":\\\"2019-01-21T07:20:35.263Z\\\",\\\"hash\\\":\\\"zZNCw70vneQdYF+poDiy62SIQ7XOxTJjDKDyjqnqTBvwqptaqhqJTaqIyP97obC3\\\",\\\"otherSeq\\\":746}},\\\"timestamp\\\":1548055236261}\",\"pubkey\":\"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn\",\"signature\":\"33AOPTIrFX/as/U7DnEkMUt9pQ5JkE4bwDqm/UKIy//uWlEHmxNgaC2mtUg7p7b9EVFQGh0cQrgn4hZLhd4c8f/TA=\",\"snapVersion\":\"2\",\"timestamp\":1548055236261,\"type\":3,\"updateTime\":1548055238313,\"vers\":\"1.0dev\"}";
        SnapshotMessage snapshot = JSON.parseObject(JSON.parseObject(json).getString("message"), SnapshotMessage.class);
        System.out.println(json);
        System.out.println(JSON.toJSONString(snapshot));
        System.out.println(snapshot.getSnapshotPoint());
    }
}
