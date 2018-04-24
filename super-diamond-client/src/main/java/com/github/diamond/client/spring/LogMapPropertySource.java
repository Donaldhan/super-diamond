package com.github.diamond.client.spring;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

/**
 * @author Donaldhan
 * @create 2018-04-24 20:32
 * @desc
 **/
public class LogMapPropertySource extends EnumerablePropertySource<Map<String, Object>> {
    private static final Logger logger = LoggerFactory.getLogger(LogMapPropertySource.class);

    public LogMapPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }

    public Object getProperty(String name) {
        Object o = ((Map)this.source).get(name);
        if (logger.isDebugEnabled() && o == null) {
            logger.debug("Autoconf key '{}' not exists", name);
        }

        return o;
    }
    public String[] getPropertyNames() {
        return StringUtils.toStringArray(((Map)this.source).keySet());
    }
}

