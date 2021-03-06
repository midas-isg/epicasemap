package integrations.app;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.running;
import gateways.configuration.AppKey;
import interactors.ConfRule;

import org.junit.Test;

import controllers.Factory;

public class TestConf {
    @Test
    public void appNameAndVersionAreNotOverridden() {
        Runnable block = () -> {
        	ConfRule conf = Factory.makeConfRule();
    		String expectedName = "epidemap";
			String keyName = AppKey.NAME.key();
			assertThat(conf.readString(keyName)).isEqualTo(expectedName);
			
			for (AppKey key : AppKey.values()){
				String value = conf.readString(key.key());
				assertThat(value).as("Conf key=" + key.key()).isNotEmpty();
			}
        };
		running(App.newWithInMemoryDb(null).getFakeApp(), block);
    }
}
