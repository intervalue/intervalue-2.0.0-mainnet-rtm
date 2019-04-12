package one.inve.contract.shell.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.METHOD)
@Retention(RUNTIME)
public @interface RequestMapper {
	String value();

	MethodEnum method() default MethodEnum.GET;

	String accept() default "application/json";
}
