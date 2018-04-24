package com.github.diamond.client.spring;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Donaldhan
 * @create 2018-04-24 20:35
 * @desc
 **/
class MessagePropertyPlaceholderBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    public static final String LOCALCONF = "localconf";
    public static final String SERVER_NAME = "serverName";
    public static final String PROFILE = "profile";
    public static final String CONF_ADDRESS = "confAddress";
    public static final String FETCH_TIME = "fetchTime";
    private static final String CONF_FILE_NAME = "confFileName";

    MessagePropertyPlaceholderBeanDefinitionParser() {
    }

    protected boolean shouldGenerateId() {
        return true;
    }

    protected Class<MessagePropertyPlaceholderConfigurer> getBeanClass(Element element) {
        return MessagePropertyPlaceholderConfigurer.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String localconf = element.getAttribute("localconf");
        String serverName = element.getAttribute("serverName");
        String profile = element.getAttribute("profile");
        String confAddress = element.getAttribute("confAddress");
        String fetch = element.getAttribute("fetchTime");
        String confFileName = element.getAttribute("confFileName");
        if (StringUtils.hasText(localconf)) {
            builder.addPropertyValue("localconf", localconf);
        }

        if (StringUtils.hasText(serverName)) {
            builder.addPropertyValue("serverName", serverName);
        }

        if (StringUtils.hasText(profile)) {
            builder.addPropertyValue("profile", profile);
        }

        if (StringUtils.hasText(confAddress)) {
            builder.addPropertyValue("confAddress", confAddress);
        }

        if (StringUtils.hasText(fetch)) {
            builder.addPropertyValue("fetchTime", fetch);
        }

        if (StringUtils.hasText(confFileName)) {
            builder.addPropertyValue("confFileName", confFileName);
        }

        builder.addPropertyValue("ignoreUnresolvablePlaceholders", Boolean.valueOf(element.getAttribute("ignore-unresolvable")));
    }
}

