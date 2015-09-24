package controllers.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import play.mvc.With;

@With(RestrictedAction.class)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Restricted {
	public static final String KEY = Restricted.class.getSimpleName();
	public static final String DELIMITER = ",";
	public enum Access {
		VIZ,
		READ,
		CHANGE
	}
	Access[] value();
}