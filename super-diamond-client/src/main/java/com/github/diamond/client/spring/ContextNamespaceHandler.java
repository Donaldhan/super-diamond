package com.github.diamond.client.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Donaldhan
 * @create 2018-04-24 20:22
 * @desc
 **/
public class ContextNamespaceHandler extends NamespaceHandlerSupport {
    private static final String PROPERTIES = "properties";

    public ContextNamespaceHandler() {
    }

    public void init() {
        this.registerBeanDefinitionParser("properties", new MessagePropertyPlaceholderBeanDefinitionParser());
    }
}

