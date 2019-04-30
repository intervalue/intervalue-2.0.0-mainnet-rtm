package one.inve.localfullnode2.store.rocks;

import java.math.BigInteger;

public class TransactionSplit {
	/**
	 * 表名 如:transactions_0
	 */
	private String tableName;
	/**
	 * total  表中现在多少数量 不能超过TRANSACTIONS_SPIT_TOTAL
	 */
	private Integer total;
	/**
	 * tableIndex 当前分表到第几张表
	 */
	private BigInteger tableIndex = BigInteger.ZERO;
	/**
	 * tableNamePrefix 表名前缀  如:messages,system_auto_tx
	 */
	private String tableNamePrefix;

	public TransactionSplit() {
	}

	private TransactionSplit(Builder builder) {
		setTableName(builder.tableName);
		setTotal(builder.total);
		setTableIndex(builder.tableIndex);
		setTableNamePrefix(builder.tableNamePrefix);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
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

	@Override
	public String toString() {
		return "TransactionSplit{" +
				"tableName='" + tableName + '\'' +
				", total=" + total +
				", tableIndex=" + tableIndex +
				", tableNamePrefix='" + tableNamePrefix + '\'' +
				'}';
	}

	public static final class Builder {
		private String tableName;
		private Integer total;
		private BigInteger tableIndex;
		private String tableNamePrefix;

		public Builder() {
		}

		public Builder tableName(String val) {
			tableName = val;
			return this;
		}

		public Builder total(Integer val) {
			total = val;
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

		public TransactionSplit build() {
			return new TransactionSplit(this);
		}
	}
}
