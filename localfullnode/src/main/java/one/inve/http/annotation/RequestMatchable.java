package one.inve.http.annotation;

public interface RequestMatchable {
	boolean isMatched(RequestMapper mapper);
}
