package one.inve.beans.dao;

import java.io.Serializable;
import java.math.BigInteger;

public class TableInfo implements Serializable {
	private static final long serialVersionUID = -73608510184969238L;
	/**
	 * 表名
	 */
    private String tableName;
	/**
	 * 表索引
	 */
	private BigInteger tableIndex  = BigInteger.ZERO;
	/**
	 * 表前缀
	 */
	private String tableNamePrefix;
	/**
	 * 此表还能插入多少条数据
	 */
	private Integer total;


	public TableInfo() {
	}

	private TableInfo(Builder builder) {
		setTableName(builder.tableName);
		setTableIndex(builder.tableIndex);
		setTableNamePrefix(builder.tableNamePrefix);
		setTotal(builder.total);
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public BigInteger getTableIndex() {
		return tableIndex;
	}

	public void setTableIndex(BigInteger tableIndex) {
		this.tableIndex = tableIndex;
	}

	public String getTableNamePrefix() {
		return tableNamePrefix;
	}

	public void setTableNamePrefix(String tableNamePrefix) {
		this.tableNamePrefix = tableNamePrefix;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}


	public static final class Builder {
		private String tableName;
		private BigInteger tableIndex;
		private String tableNamePrefix;
		private Integer total;

		public Builder() {
		}

		public Builder tableName(String val) {
			tableName = val;
			return this;
		}

		public Builder tableIndex(BigInteger val) {
			tableIndex = val;
			return this;
		}

		public Builder tableNamePrefix(String val) {
			tableNamePrefix = val;
			return this;
		}

		public Builder total(Integer val) {
			total = val;
			return this;
		}

		public TableInfo build() {
			return new TableInfo(this);
		}
	}
}
