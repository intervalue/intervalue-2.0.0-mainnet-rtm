package one.inve.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import one.inve.util.RSAEncrypt;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;


/**
 * HikariCP连接池配置
 */
public class DataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);


    /**
     * 初始化HikariCP连接池配置
     */
    public static DruidDataSource getDataSource(String dbId, Boolean isDrop) {
        String dataSourceUrl ="";
        try {

            dataSourceUrl = PropertyConstants.getPropertiesKey("spring.datasource.url" + dbId);
            String username = PropertyConstants.getPropertiesKey("spring.datasource.username" + dbId);
            String password = PropertyConstants.getPropertiesKey("spring.datasource.password" + dbId);
            if("default".equals(dataSourceUrl)){
                logger.error("初始化DruidDataSource连接池配置异常 url="+dataSourceUrl+";number="+dbId);
                dataSourceUrl="jdbc:mysql://localhost:3306/main"+dbId+"?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8";
                /*username="root";
                password="123456";*/
            }
            if(StringUtils.isNotBlank(password)){
                password= RSAEncrypt.decrypt(password);
            }
            String urlSubstring = dataSourceUrl.substring(0, dataSourceUrl.indexOf("?"));
            String url = urlSubstring.substring(0, urlSubstring.lastIndexOf("/"));
            String dataName = urlSubstring.substring(urlSubstring.lastIndexOf("/") + 1);
            DruidDataSource source = new DruidDataSource();
            source.setUrl(url);
            source.setUsername(username);
            source.setPassword(password);
            source.setDriverClassName("com.mysql.jdbc.Driver");
            Connection connection = source.getConnection();

            StringBuilder dropSql = new StringBuilder("drop database ").append(dataName);
            StringBuilder sql = new StringBuilder("CREATE DATABASE  IF NOT EXISTS ")
                    .append(dataName).append(" DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci");


            Statement statement = connection.createStatement();
            if (isDrop) {
                try {
                    statement.executeUpdate(dropSql.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            statement.executeUpdate(sql.toString());
            statement.close();
            connection.close();
            source.close();

            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(dataSourceUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");

                //configuration
            dataSource.setInitialSize(1);
            dataSource.setMinIdle(3);
            dataSource.setMaxActive(60000);
                //配置获取连接等待超时的时间
            dataSource.setMaxWait(60000);
                //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
            dataSource.setTimeBetweenEvictionRunsMillis(60000);
                //配置一个连接在池中最小生存的时间，单位是毫秒
            dataSource.setMinEvictableIdleTimeMillis(30000);
            dataSource.setValidationQuery("select 'x'");
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
                //打开PSCache，并且指定每个连接上PSCache的大小
            dataSource.setPoolPreparedStatements(true);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
                //配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
            dataSource.setFilters("stat,slf4j");
                //通过connectProperties属性来打开mergeSql功能；慢SQL记录
            dataSource.setConnectionProperties("druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000");

            return dataSource;
            }catch(Exception ex){
                logger.error("初始化DruidDataSource连接池配置异常 url="+dataSourceUrl+";number="+dbId ,ex);
                return null;
            }
    }

    /**
     * 初始化HikariCP连接池配置
     */
    public static DruidDataSource getDataSource(String dbId) {
        try {

            String dataSourceUrl = PropertyConstants.getPropertiesKey("spring.datasource.url" + dbId);
            String username = PropertyConstants.getPropertiesKey("spring.datasource.username" + dbId);
            String password = PropertyConstants.getPropertiesKey("spring.datasource.password" + dbId);
            if("default".equals(dataSourceUrl)){
                logger.error("初始化DruidDataSource连接池配置异常 url="+dataSourceUrl+";number="+dbId);
                dataSourceUrl="jdbc:mysql://localhost:3306/main"+dbId+"?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8";
                /*username="root";
                password="123456";*/
            }

            /**
             * 数据源
             */
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUrl(dataSourceUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");

            //configuration
            dataSource.setInitialSize(1);
            dataSource.setMinIdle(3);
            dataSource.setMaxActive(60000);
            //配置获取连接等待超时的时间
            dataSource.setMaxWait(60000);
            //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
            dataSource.setTimeBetweenEvictionRunsMillis(60000);
            //配置一个连接在池中最小生存的时间，单位是毫秒
            dataSource.setMinEvictableIdleTimeMillis(30000);
            dataSource.setValidationQuery("select 'x'");
            dataSource.setTestWhileIdle(true);
            dataSource.setTestOnBorrow(false);
            dataSource.setTestOnReturn(false);
            //打开PSCache，并且指定每个连接上PSCache的大小
            dataSource.setPoolPreparedStatements(true);
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
            //配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
            dataSource.setFilters("stat,slf4j");
            //通过connectProperties属性来打开mergeSql功能；慢SQL记录
            dataSource.setConnectionProperties("druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000");

            return dataSource;
        } catch (Exception ex) {
            logger.error("初始化DruidDataSource连接池配置异常", ex);
            return null;
        }
    }

}
