/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constretto.internal;

import org.constretto.ConstrettoConfiguration;
import org.constretto.exception.ConstrettoConversionException;
import org.constretto.exception.ConstrettoException;
import org.constretto.exception.ConstrettoExpressionException;
import static org.constretto.internal.converter.ValueConverterRegistry.convert;
import org.constretto.model.ConfigurationNode;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class DefaultConstrettoConfiguration implements ConstrettoConfiguration {
    private List<String> currentTags;
    private final ConfigurationNode configuration;

    public DefaultConstrettoConfiguration(ConfigurationNode configuration, List<String> currentTags) {
        this.configuration = configuration;
        this.currentTags = currentTags;
    }

    @SuppressWarnings("unchecked")
    public <K> K evaluateTo(String expression, K defaultValue) {
        ConfigurationNode node = findElementOrNull(expression);
        if (node == null) {
            return defaultValue;
        }
        K value;
        try {
            value = (K) convert(defaultValue.getClass(), node.getValue());
        } catch (ConstrettoConversionException e) {
            value = null;
        }
        return null != value ? value : defaultValue;
    }

    public <K> K evaluateTo(Class<K> targetClass, String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(targetClass, node.getValue());
    }

    public String evaluateToString(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return node.getValue();
    }

    public Boolean evaluateToBoolean(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Boolean.class, node.getValue());
    }

    public Double evaluateToDouble(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Double.class, node.getValue());
    }

    public Long evaluateToLong(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Long.class, node.getValue());
    }

    public Float evaluateToFloat(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Float.class, node.getValue());
    }

    public Integer evaluateToInt(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Integer.class, node.getValue());
    }

    public Short evaluateToShort(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Short.class, node.getValue());
    }

    public Byte evaluateToByte(String expression) throws ConstrettoExpressionException {
        ConfigurationNode node = findElementOrThrowException(expression);
        return convert(Byte.class, node.getValue());
    }

    public <T> T as(Class<T> configurationClass) throws ConstrettoException {
        T objectToConfigure;
        try {
            objectToConfigure = configurationClass.newInstance();
        } catch (Exception e) {
            throw new ConstrettoException("Could not instansiate class of type: " + configurationClass.getName()
                    + " when trying to inject it with configuration", e);
        }

        injectConfigurationUsingReflection(objectToConfigure);

        return objectToConfigure;
    }

    public <T> T applyOn(T objectToConfigure) throws ConstrettoException {
        injectConfigurationUsingReflection(objectToConfigure);
        return objectToConfigure;
    }

    public ConstrettoConfiguration at(String expression) {
        ConfigurationNode currentConfigurationNode = findElementOrThrowException(expression);
        return new DefaultConstrettoConfiguration(currentConfigurationNode, currentTags);
    }

    public boolean hasValue(String expression) {
        ConfigurationNode node = findElementOrNull(expression);
        return null != node;
    }


    //
    // Helper methods
    //
    private ConfigurationNode findElementOrThrowException(String expression) {
        List<ConfigurationNode> node = configuration.findAllBy(expression);
        ConfigurationNode resolvedNode = resolveMatch(node);
        if (resolvedNode == null) {
            throw new ConstrettoExpressionException(expression, currentTags, "not found in configuration");
        }
        return resolvedNode;
    }

    private ConfigurationNode findElementOrNull(String expression) {
        List<ConfigurationNode> node = configuration.findAllBy(expression);
        return resolveMatch(node);
    }

    private ConfigurationNode resolveMatch(List<ConfigurationNode> node) {
        ConfigurationNode bestMatch = null;
        for (ConfigurationNode configurationNode : node) {
            if (ConfigurationNode.DEFAULT_TAG.equals(configurationNode.getTag())) {
                if (bestMatch == null) {
                    bestMatch = configurationNode;
                }
            } else if (currentTags.contains(configurationNode.getTag())) {
                if (bestMatch == null) {
                    bestMatch = configurationNode;
                } else {
                    int previousFoundPriority =
                            ConfigurationNode.DEFAULT_TAG.equals(bestMatch.getTag()) ?
                                    Integer.MAX_VALUE : currentTags.indexOf(bestMatch.getTag());
                    if (currentTags.indexOf(configurationNode.getTag()) <= previousFoundPriority) {
                        bestMatch = configurationNode;
                    }
                }
            } else if (ConfigurationNode.ALL_TAG.equals(configurationNode.getTag())) {
                bestMatch = configurationNode;
            }
        }
        return bestMatch;
    }

    private <T> void injectConfigurationUsingReflection(T objectToConfigure) {
        Field[] fields = objectToConfigure.getClass().getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            ConfigurationNode node = findElementOrThrowException(name);
            try {
                field.set(objectToConfigure, convert(fieldType, node.getValue()));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}