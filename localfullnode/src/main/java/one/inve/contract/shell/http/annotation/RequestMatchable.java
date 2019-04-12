package one.inve.contract.shell.http.annotation;

public interface RequestMatchable {
	boolean isMatched(RequestMapper mapper);

	public static class RequestMatcher implements RequestMatchable {

		@Override
		public boolean isMatched(RequestMapper mapper) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
