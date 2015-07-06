package org.constretto.internal.provider;

import org.constretto.ConstrettoBuilder;
import org.constretto.ConstrettoConfiguration;
import org.constretto.annotation.Configuration;
import org.constretto.annotation.Configure;
import org.constretto.model.ClassPathResource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:kaare.nilsen@arktekk.no">Kaare Nilsen</a>
 */
public class DynamicReconfiguringTagsTest {
    private ConstrettoConfiguration config;

    @Before
    public void createConfiguration() {
        config = new ConstrettoBuilder()
                .createPropertiesStore()
                .addResource(new ClassPathResource("dynamic.properties"))
                .done().getConfiguration();
    }

    @Test
    public void whenAppendingTagsRuntimeVariablesShouldBeResolvedCorrectlyWhenUsingJavaApi() {
        assertEquals("default value", config.evaluateToString("stagedKey"));
        config.appendTag("test");
        assertEquals("test value", config.evaluateToString("stagedKey"));
        config.appendTag("prod");
        assertEquals("test value", config.evaluateToString("stagedKey"));
    }


    @Test
    public void whenPrependingTagsRuntimeVariablesShouldBeResolvedCorrectlyWhenUsingJavaApi() {
        assertEquals("default value", config.evaluateToString("stagedKey"));
        config.prependTag("test");
        assertEquals("test value", config.evaluateToString("stagedKey"));
        config.prependTag("prod");
        assertEquals("prod value", config.evaluateToString("stagedKey"));
    }

    @Test
    public void whenResetingTagsItResolvesBackToOriginalTags() {
        ConstrettoConfiguration configuration = new ConstrettoBuilder()
                .createPropertiesStore().addResource(new ClassPathResource("dynamic.properties")).done()
                .addCurrentTag("test")
                .getConfiguration();
        assertEquals("test value", configuration.evaluateToString("stagedKey"));
        configuration.prependTag("prod");
        assertEquals("prod value", configuration.evaluateToString("stagedKey"));
        configuration.resetTags(true);
        assertEquals("test value", configuration.evaluateToString("stagedKey"));
    }


    @Test
    public void whenClearingTagsItResolvesBackToDefaultValues() {
        ConstrettoConfiguration configuration = new ConstrettoBuilder()
                .createPropertiesStore().addResource(new ClassPathResource("dynamic.properties")).done()
                .addCurrentTag("test")
                .getConfiguration();
        assertEquals("test value", configuration.evaluateToString("stagedKey"));
        configuration.prependTag("prod");
        assertEquals("prod value", configuration.evaluateToString("stagedKey"));
        configuration.clearTags(true);
        assertEquals("default value", configuration.evaluateToString("stagedKey"));
    }

    private class ConfiguredClass {
        @Configuration
        private String stagedVariable;
        private String stagedKey;


        @Configure
        public void configure(String stagedKey) {
            this.stagedKey = stagedKey;
        }
    }
}
