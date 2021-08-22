package helpers;

import com.realworld.springmongo.security.JwtConfig;
import com.realworld.springmongo.security.JwtSigner;
import com.realworld.springmongo.security.SecurityConfig;
import com.realworld.springmongo.security.TokenExtractor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import({SecurityConfig.class, TokenExtractor.class, JwtSigner.class, JwtConfig.class})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportAppSecurity {
}
