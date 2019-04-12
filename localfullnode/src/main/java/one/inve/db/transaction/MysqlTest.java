package one.inve.db.transaction;

import com.alibaba.fastjson.JSONArray;
import one.inve.beans.dao.TransactionArray;

import java.math.BigInteger;

public class MysqlTest {


    public static void main(String[] args) {
         TransactionArray array=new TransactionArray();
        try {
        	/*QueryTableSplit split=new QueryTableSplit();
            String eventInfo=split.queryTransactionEvent("0xadfalsdjfdfsgsdfgsdf32" );
        	System.out.println("========================"+eventInfo);*/
        	 
        	QueryTableSplit split=new QueryTableSplit();
        	BigInteger index = array.getTableIndex();
        	if(index==null) {
        		index=new BigInteger("11");
        	}
        	Long offset = 0L;
            array=split.queryTransaction(index, 0L, "4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7",
                    "1", "0_0");
        	System.out.println("========================"+ JSONArray.toJSONString(array));
        	/*while(true) {
        		array=split.queryTransaction(array.getIndex(), array.getOffset(), "0asdfghjklalsdfasdlfk11", null, fielPath);
            	System.out.println("========================"+array);
            	if(array.getIndex()>120) {
            		break;
            	}
        	}*/

            /*boolean isOk = true;
            int i = 0;
            while (isOk) {
                NewTableCreate table = new NewTableCreate(DataDirectoryUtils.getDataFileDir() + "50trans.hashnet.sqlite");
                List<Transaction> entityList = new ArrayList<Transaction>();
                for (int j = 0; j < 53; j++) {
                    Transaction entity = new Transaction.Builder()
                            .eHash("0xadfalsdjfdfsgsdfgsdf" + j)
                            .hash(j + "0xadfalsdjfdfsgsdfgsdf" + j)
                            .fromAddress("0asdfghjklalsdfasdlfk" + j)
                            .toAddress("0xasadsfasdfasd" + i)
                            .amount(j)
                            .fee(j + 100)
                            .time(System.currentTimeMillis())
                            .remark("")
                            .updateTime(Instant.now().toEpochMilli())
                            .isStable(true)
                            .isValid(true)
                            .build();
                    entityList.add(entity);
                }
                table.addTransactions(entityList);
                i++;
                if (i > 2000) {
                    isOk = false;
                }
            }*/
        	
        	/*SqliteTest test=new SqliteTest();
        	String tilePath=test.getClass().getClassLoader().getResource("initial.hashnet.sqlite").getPath();
            tilePath=DataDirectoryUtils.getDataFileDir() + LocalFullNode.transDbName;
        	String tilePath="D:\\localfullnode\\src\\main\\resources\\initial.hashnet.sqlite";
        	SqliteHelper h = new SqliteHelper(tilePath);
            h.executeUpdate("drop table if exists test;");
            h.executeUpdate("create table test(name varchar(20));");
            h.executeUpdate("insert into test values('sqliteHelper test');");
            h.executeUpdate("insert into test values('sqliteHelper test1');");
            h.executeUpdate("insert into test values('sqliteHelper test2');");
            h.executeUpdate("insert into test values('sqliteHelper test3');");
            h.executeUpdate("insert into test values('sqliteHelper test4');");
            h.executeUpdate("insert into test values('sqliteHelper test5');");
            List<String> sList = h.executeQuery("select name from test", new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int index)
                        throws SQLException {
                    return rs.getString("name");
                }
            });
            for(String name:sList) {
            System.out.println(name);
            }
            
            NewTableCreate newTable=new NewTableCreate();
            String tableName="transactions_test";
            newTable.createMessagesTable(h, tableName);
            h.executeUpdate("INSERT INTO \"main\".\""+tableName+"\" VALUES ('WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW', '4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7', 10000000000000000000000000000, 0, 'QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ', 1531962537000, 1, 1, 'default8rRMCcNdqPqjFWKIXM/07QdsK0AssRNF2GdlGZcNNBgAFag+yJ6cmrE', null, null);");
            String tableNameSplit="transactions_split";
//            newTable.createTableSplit(h);
//            h.executeUpdate("INSERT INTO \"main\".\""+tableNameSplit+"\" VALUES ('transactions_split_total',0,0,'test',"+System.currentTimeMillis()+","+System.currentTimeMillis()+")");
            sList = h.executeQuery("select total from transactions_split where tableName='transactions_split_total'", new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int index)
                        throws SQLException {
                    return rs.getString("total");
                }
            });
            for(String name:sList) {
            System.out.println(name);
            }
            String MESSAGES="transactions";
            newTable.createTableSplit(h);
			h.executeUpdate("INSERT INTO transactions_split  VALUES ('"+MESSAGES+"_0',0,0,'"+MESSAGES+"',"+System.currentTimeMillis()+","+System.currentTimeMillis()+")");
		    
            h.destroyed();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}