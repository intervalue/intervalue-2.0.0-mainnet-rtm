package one.inve.beans.dao;


import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class TransactionArray implements Serializable {
    /**
     * 交易信息
     */
	private List<Message> list;
	/**
	 * 表索引，代表第几个表 开始查询用0表示
	 */
	private BigInteger tableIndex = BigInteger.ZERO;
	/**
	 * 跳过多少条记录
	 */
	private Long offset;

	public TransactionArray() {
	}

	private TransactionArray(Builder builder) {
		setList(builder.list);
		setTableIndex(builder.tableIndex);
		setOffset(builder.offset);
	}

	public List<Message> getList() {
		return list;
	}

	public void setList(List<Message> list) {
		this.list = list;
	}

	public BigInteger getTableIndex() {
		return tableIndex;
	}

	public void setTableIndex(BigInteger tableIndex) {
		this.tableIndex = tableIndex;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}


	public static final class Builder {
		private List<Message> list;
		private BigInteger tableIndex;
		private Long offset;

		public Builder() {
		}

		public Builder list(List<Message> val) {
			list = val;
			return this;
		}

		public Builder tableIndex(BigInteger val) {
			tableIndex = val;
			return this;
		}

		public Builder offset(Long val) {
			offset = val;
			return this;
		}

		public TransactionArray build() {
			return new TransactionArray(this);
		}
	}
}
