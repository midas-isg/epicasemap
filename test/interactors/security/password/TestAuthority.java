package interactors.security.password;

import static org.fest.assertions.Assertions.assertThat;
import interactors.security.SecurityFactory;
import interactors.security.password.HashSpec;
import interactors.security.password.HashedPassword;
import interactors.security.password.Hasher;
import interactors.security.password.Authority;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.spec.PBEKeySpec;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAuthority {
	private static final String appSalt = "app";
	private static Authority authority;
	
	@BeforeClass
	public static void createHashSpec(){
		authority = SecurityFactory.makePasswordAuthority();
	}
	
	@Test
	public void tesHash() throws Exception {
		final String password = "password";
		final Set<String> hashes = new HashSet<>();
		final int n = 10;
		for(int i = 0; i < n; i++) {
			final String hash = authority.hash(password, appSalt).getHash();
			assertThat(hash).isNotIn(hashes);
			hashes.add(hash);
		}
		assertThat(hashes).hasSize(n);
	}

	@Test
	public void testVerify() throws Exception {
        for(int i = 0; i < 10; i++){
            String password = "" + i;
            HashedPassword hp = authority.hash(password, appSalt);
            String wrongPassword = "" + (i + 1);
            assertThat(authority.verify(wrongPassword, hp, appSalt))
            	.describedAs("Wrong password accepted!")
            	.isFalse();
            assertThat(authority.verify(password, hp, "invalid"))
            	.describedAs("Wrong app salt accepted!")
            	.isFalse();
            assertThat(authority.verify(password, hp, appSalt))
        		.describedAs("Good password rejected!")
        		.isTrue();
        }
	}
	
	@Test
	public void testInvalidAlgorithm() throws Exception {
		final HashSpec spec = PasswordFactory.makeHashSpec();
		spec.setAlgorithm("Invalid");
		Authority  authority = PasswordFactory.makeAuthority(spec);
		assertRuntimeExceptionCausedBy(NoSuchAlgorithmException.class, () -> {
			authority.hash("password", "salt");
		});
	}
	
	@Test
	public void testInvalid() throws Exception {
		String password = "1";
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
		Hasher hasher = new Hasher(null);
		assertRuntimeExceptionCausedBy(InvalidKeySpecException.class, () -> {
			hasher.encode(keySpec, "PBKDF2WithHmacSHA1");
		});
	}
	
	private void assertRuntimeExceptionCausedBy(Class<? extends Throwable> cause, Runnable block){
			try {
				block.run();
				Assert.fail(cause.getSimpleName() + " expected but no Exception was thrown");
			} catch (RuntimeException re){
				assertThat(re.getCause()).isExactlyInstanceOf(cause);
			} catch (Throwable t){
				final String message = causedBy(RuntimeException.class, cause) + 
				" expected but " + causedBy(t) + "  was thrown" ;
				Assert.fail(message);
			}
	}

	private String causedBy(Throwable t){
		Class<? extends Throwable> causeClass = null; 
		final Throwable cause = t.getCause();
		if (cause != null){
			causeClass = cause.getClass();
		}
		return causedBy(t.getClass(), causeClass);
	}
	
	private String causedBy(Class<? extends Throwable> t, Class<? extends Throwable> cause) {
		String message = text(t);
		if (cause != null) 
			message += " caused by " + text(cause);
		return message; 
	}

	private String text(Class<? extends Throwable> t) {
		return "<" + t.getName() +	">";
	}
}