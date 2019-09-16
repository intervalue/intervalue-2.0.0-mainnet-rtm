package one.inve.localfullnode2.store.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Mysql帮助类，直接创建该类示例，并调用相应的借口即可对Mysql数据库进行操作
 *
 * 本类基于 Mysql jdbc v56
 *
 *
 */
public class MysqlHelper {
	private static final Logger logger = LoggerFactory.getLogger(MysqlHelper.class);

	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	private DruidDataSource dataSource;
	public static Map<String, DruidDataSource> dataSourceMap = new HashMap<>();

	/**
	 * 构造函数
	 * 
	 * @throws SQLException
	 */
	public MysqlHelper(String dbId, Boolean isDrop) {
		if (dbId != null) {
			dataSource = dataSourceMap.get(dbId);
			if (dataSource == null) {
				dataSource = getDataSource(dbId, isDrop);
				dataSourceMap.put(dbId, dataSource);
			}
		}
	}

	public MysqlHelper(String newDbId, String oldDbId, Boolean isDrop) {
		if (newDbId != null && oldDbId != null) {
			dataSource = getDataSource(newDbId, isDrop);
			dataSourceMap.put(oldDbId, dataSource);
			dataSourceMap.put(newDbId, dataSource);
		}
	}

	/**
	 * 构造函数
	 *
	 * @throws SQLException
	 */
	public MysqlHelper(String dbId) {
		if (dbId != null) {
			dataSource = dataSourceMap.get(dbId);
			if (dataSource == null) {
				dataSource = getDataSource(dbId);
				dataSourceMap.put(dbId, dataSource);
			}
		}
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return 数据库连接
	 * @throws @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		connection = dataSource.getConnection();
		return connection;
	}

	/**
	 * 创建表用到
	 *
	 * @param dbId
	 * @return
	 */
	private DruidDataSource getDataSource(String dbId, Boolean isDrop) {
		return DataSourceConfig.getDataSource(dbId, isDrop);
	}

	/**
	 * 初始化会用到，创建库,表
	 *
	 * @param dbId
	 * @return
	 */
	private DruidDataSource getDataSource(String dbId) {
		return DataSourceConfig.getDataSource(dbId);
	}

	/**
	 * 执行sql查询 @param sql sql select 语句 @param rse 结果集处理类对象 @return 查询结果 @throws
	 * SQLException @throws
	 */
	public <T> T executeQuery(String sql, ResultSetExtractor<T> rse) throws SQLException {
		try {
			resultSet = getStatement().executeQuery(sql);
			T rs = rse.extractData(resultSet);
			return rs;
		} finally {
			// destroyed();
		}
	}

	/**
	 * 执行select查询，返回结果列表
	 *
	 * @param sql sql select 语句 @param rm 结果集的行数据处理类对象 @return @throws
	 *            SQLException @throws
	 */
	public <T> List<T> executeQuery(String sql, RowMapper<T> rm) throws SQLException {
		List<T> rsList = new ArrayList<T>();
		try {
			resultSet = getStatement().executeQuery(sql);
			while (resultSet.next()) {
				rsList.add(rm.mapRow(resultSet, resultSet.getRow()));
			}
		} finally {
			// destroyed();
		}
		return rsList;
	}

	/**
	 * 执行数据库更新sql语句 @param sql @return 更新行数 @throws SQLException @throws
	 */
	public int executeUpdate(String sql) throws SQLException {
		int c = getStatement().executeUpdate(sql);
		return c;

	}

	/**
	 * 执行数据库更新sql语句 ,返回自增id @param sql @return 更新行数 @throws SQLException @throws
	 */
	public int executeUpdateAuto(String sql) throws SQLException {
		int c = getStatement().executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		return c;

	}

	/**
	 * 执行多个sql更新语句 @param sqls @throws SQLException @throws
	 */
	public void executeUpdate(String... sqls) throws SQLException {
		try {
			for (String sql : sqls) {
				getStatement().executeUpdate(sql);
			}
		} finally {
			// destroyed();
		}
	}

	/**
	 * 执行数据库更新 sql List
	 * 
	 * @param sqls sql列表
	 * @throws SQLException
	 */
	public void executeUpdate(List<String> sqls) throws SQLException {
		try {
			for (String sql : sqls) {
				getStatement().executeUpdate(sql);
			}
		} finally {
			// destroyed();
		}
	}

	/**
	 * 批量执行sql 如果使用PreparedStatement必须使用此方法，因为数据库不是自动提交的，必须commit
	 * 
	 * @param sql
	 * @return
	 * @throws @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		if (connection == null) {
			connection = getConnection();
		}
		if (connection.getAutoCommit()) {
			connection.setAutoCommit(false);
		}
		return connection.prepareStatement(sql);

	}

	public Statement getStatement() throws SQLException {
		if (null == statement) {
			if (connection == null) {
				connection = dataSource.getConnection();
			}
			statement = connection.createStatement();
		}
		return statement;
	}

	/**
	 * 数据库资源关闭和释放 数据库是自动提交的不需要commit
	 */
	public void destroyed() {
		try {
			if (null != resultSet) {
				resultSet.close();
				resultSet = null;
			}
			if (null != statement) {
				statement.close();
				statement = null;
			}
			if (null != connection) {
				connection.close();
				connection = null;
			}

		} catch (SQLException e) {
			logger.error("error: {}", e);
		}
	}

	/**
	 * 数据库资源关闭和释放 如果使用PreparedStatement必须使用此方法，因为数据库不是自动提交的，必须commit
	 */
	public void destroyedPreparedStatement() {
		try {
			if (null != resultSet) {
				resultSet.close();
				resultSet = null;
			}
			if (null != statement) {
				statement.close();
				statement = null;
			}
			if (null != connection) {
				if (!connection.getAutoCommit()) {
					connection.commit();
				}
				connection.close();
				connection = null;
			}

		} catch (SQLException e) {
			logger.error("error: {}", e);
		}
	}

	/**
	 * 数据库资源关闭和释放 提交失败，回滚事物
	 */
	public void commitFail() {
		try {
			if (null != resultSet) {
				resultSet.close();
				resultSet = null;
			}
			if (null != statement) {
				statement.close();
				statement = null;
			}
			if (null != connection) {
				if (!connection.getAutoCommit()) {
					connection.rollback();
				}
				connection.close();
				connection = null;
			}

		} catch (SQLException e) {
			logger.error("error: {}", e);
		}
	}

	public List<MysqlTable> getTables() {
		List<MysqlTable> tables = new ArrayList<>();

		DatabaseMetaData metaData = null;
		ResultSet rs = null;
		ResultSet crs = null;
		try {
			metaData = this.getConnection().getMetaData();
//                System.out.println("------db: " + this.getConnection().getCatalog());
			rs = metaData.getTables(this.getConnection().getCatalog(), "root", null, new String[] { "TABLE" }); // ,
																												// "VIEW"
			while (rs.next()) {
				String tablename = rs.getString("TABLE_NAME");
//                System.out.println("------table: " + tablename);
				// 获取当前表的列
				crs = metaData.getColumns(this.getConnection().getCatalog(), "%", tablename, "%");
				List<MysqlTableField> fields = new ArrayList<>();
				while (crs.next()) {
					String columnname = crs.getString("COLUMN_NAME");
					String columntype = crs.getString("TYPE_NAME");
					int columnLen = crs.getInt("COLUMN_SIZE");
					int nullable = crs.getInt("NULLABLE");
					System.out.println("--------------columnname: " + columnname);
					System.out.println("--------------columntype: " + columntype);
					System.out.println("--------------columnLen : " + columnLen);
					System.out.println("--------------nullable  : " + nullable);

					fields.add(new MysqlTableField.Builder().name(columnname).type(columntype).size(columnLen)
							.nullable(nullable).build());
				}
				tables.add(new MysqlTable.Builder().name(tablename).fields(fields).build());
			}
			return tables;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}