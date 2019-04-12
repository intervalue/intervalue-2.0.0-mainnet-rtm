package one.inve.beans.splitAttack;

import java.util.Set;

public class SplitResult {

	/**
	 * 密钥
	 * Created by Max on 2018/6/10.
	 */
	    public long[] minSeq;
	    public Set<String> delKey;

	    public SplitResult() {
	    }

	    private SplitResult(Builder builder) {
	    	minSeq = builder.minSeq;
	    	delKey = builder.delKey;
	    }

	    public static final class Builder {
	        private long[] minSeq;
	        private Set<String> delKey;

	        public Builder() {
	        }

	        public Builder minSeq(long[] val) {
	        	minSeq = val;
	            return this;
	        }

	        public Builder delKey(Set<String> val) {
	        	delKey = val;
	            return this;
	        }

	        public SplitResult build() {
	            return new SplitResult(this);
	        }
	    }
}
