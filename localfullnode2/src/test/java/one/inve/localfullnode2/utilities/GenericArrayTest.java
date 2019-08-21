package one.inve.localfullnode2.utilities;

import org.junit.Test;

public class GenericArrayTest {

	@Test
	public void testAppendAndGet() {
		GenericArray<String> stringArray = new GenericArray<>();

		String[] ab = { "a", "b" };
		String[] cde = { "c", "d", "e" };
		stringArray.append(ab);
		System.out.println(stringArray.get(0));

		stringArray.append(cde);

		System.out.println(stringArray.get(4));
	}
}
