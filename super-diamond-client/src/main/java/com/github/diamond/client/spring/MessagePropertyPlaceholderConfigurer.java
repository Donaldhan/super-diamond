package com.github.diamond.client.spring;

import com.github.diamond.client.PropertiesConfiguration;
import com.github.diamond.client.event.ConfigurationEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;
/**
 * @author Donaldhan
 * @create 2018-04-24 20:35
 * @desc
 **/
public class MessagePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements ApplicationContextAware, InitializingBean, EnvironmentAware {
    public static final String AUTOCONF_PROPERTIES = "autoconfProperties";
    private Logger logger = LoggerFactory.getLogger(MessagePropertyPlaceholderConfigurer.class);
    public static final int DEFAULT_FETCH_TIME = 15000;
    private ApplicationContext applicationContext;
    private Environment environment;
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
    private PropertiesConfiguration config;
    private static final String DEFAULT_AUTO_FILE_NAME = "autoconf.properties";
    private static final String DEFAULT_FILE;
    private static final String XML_FILE_EXTENSION = ".xml";
    private static final String CONF_FILE_NAME = "confFileName";
    private static final String SERVER_NAME = "serverName";
    private static final String PROFILE = "profile";
    private static final String CONF_ADDRESS = "confAddress";
    private static final String FETCH_TIME = "fetchtime";
    private static final String SEC_KEY = "secKey";
    private static final String SUPPORT_LOCAL = "local";
    public static final String CLASSPATH = "classpath:";
    public static final String LOCALCONF = "localconf";
    private String confFileName = "autoconf.properties";
    private String localconf;
    private String serverName;
    private String profile;
    private String confAddress;
    private String fetchTime;
    private String secKey;
    private boolean supportLocal;
    private static final Map<String, Object> contextProperties;
    private static final AtomicBoolean isInited;

    public MessagePropertyPlaceholderConfigurer() {
    }

    private void init() {
        Class var1 = MessagePropertyPlaceholderConfigurer.class;
        synchronized(MessagePropertyPlaceholderConfigurer.class) {
            if (isInited.compareAndSet(false, true)) {
                Properties props = new Properties();
                this.initConf(props);
                boolean localMode = props.getProperty("local") != null && Boolean.parseBoolean(props.getProperty("local"));
                if (localMode) {
                    this.setUpWithLocalPropertySource(props);
                } else {
                    this.setUpWithRemotePropertySource(props);
                }
            } else {
                this.logger.error("[ConfigCenter] Bean is inited allready!!!!");
                this.injectToSpring();
            }

        }
    }

    private void setUpWithLocalPropertySource(Properties props) {
        this.assertNotEmpty(this.getLocalconf(), "localconf is not set while 'local' option is true");
        Properties localProp = new Properties();

        try {
            fillProperties(localProp, new EncodedResource(this.resourceLoader.getResource(this.getLocalconf())), this.propertiesPersister);
            this.logger.debug("[ConfigCenter] Get all config from local conf file {} -->  [ {} ]", this.getLocalconf(), localProp);
            super.setProperties(localProp);
            this.mergeprops(localProp);
            this.configEnvironment();
        } catch (IOException var4) {
            throw new IllegalArgumentException("cannot load local conf: " + this.getLocalconf(), var4);
        }
    }

    private void setUpWithRemotePropertySource(Properties props) {
        String confAddress = props.getProperty("confAddress");
        this.validatePropParams(props);
        String[] addressAndPort = confAddress.trim().split(":");
        this.assertAddressNotNull(addressAndPort, "confAddress");
        this.config = new PropertiesConfiguration(addressAndPort[0], Integer.parseInt(addressAndPort[1].trim()), props.getProperty("serverName").trim(), props.getProperty("profile"));
        this.config.setSecKey(props.getProperty("secKey"));
        if (props.getProperty("local") != null) {
            this.config.setSupportLocal(Boolean.valueOf(props.getProperty("local")));
        }

        this.injectToSpring(this.config.getProperties());
        this.initListener(this.config);
    }

    private void injectToSpring(Properties properties) {
        this.logger.debug("[ConfigCenter] Get all config -->  [ " + properties + " ]");
        super.setProperties(properties);
        this.mergeprops(properties);
        this.configEnvironment();
    }

    private void configEnvironment() {
        if (this.environment != null && this.environment instanceof AbstractEnvironment) {
            LogMapPropertySource mapPropertySource = new LogMapPropertySource("autoconfProperties", contextProperties);
            ((AbstractEnvironment)this.environment).getPropertySources().addFirst(mapPropertySource);
        }

    }

    private void validatePropParams(Properties props) {
        this.assertNotEmpty(props.getProperty("confAddress"), "confAddress");
        this.assertNotEmpty(props.getProperty("serverName"), "serverName");
        this.assertNotEmpty(props.getProperty("profile"), "profile");
    }

    private void initConf(Properties props) {
        Resource resource = null;
        if (!StringUtils.isEmpty(System.getProperty("localconf"))) {
            this.localconf = System.getProperty("localconf");
            resource = this.resourceLoader.getResource(this.localconf);
        }

        if (!StringUtils.isEmpty(System.getProperty("confFileName"))) {
            this.confFileName = System.getProperty("confFileName");
            this.logger.info("[ConfigCenter] Get confFileName param from system: {}.", this.confFileName);
        }

        if (!StringUtils.isEmpty(this.getConfFileName())) {
            props.put("confFileName", this.getConfFileName());
            this.confFileName = this.getConfFileName();
            this.logger.info("[ConfigCenter] Get confFileName param from configFile: {}.", this.confFileName);
        }

        if (resource != null && resource.exists()) {
            this.logger.info("[ConfigCenter] Get properties 'localconf' from  environment variable : {}", this.localconf);
        } else {
            try {
                resource = this.resourceLoader.getResource("classpath:" + this.confFileName);
            } catch (Throwable var5) {
                ;
            }

            if (resource != null && resource.exists()) {
                this.logger.info("[ConfigCenter] Get properties 'localconf' from {}.", "classpath:" + this.confFileName);
            } else {
                this.localconf = DEFAULT_FILE;
                resource = this.resourceLoader.getResource(this.localconf);
                this.logger.info("[ConfigCenter] Get properties 'localconf' from {}.", DEFAULT_FILE);
            }

            try {
                fillProperties(props, new EncodedResource(resource), this.propertiesPersister);
            } catch (IOException var4) {
                this.logger.error("load 'localconf' file exception.", var4);
            }
        }

        if (!StringUtils.isEmpty(this.getServerName())) {
            props.put("serverName", this.getServerName());
        }

        if (!StringUtils.isEmpty(this.getProfile())) {
            props.put("profile", this.getProfile());
        }

        if (!StringUtils.isEmpty(this.getConfAddress())) {
            props.put("confAddress", this.getConfAddress());
        }

        if (!StringUtils.isEmpty(this.getFetchTime())) {
            props.put("fetchtime", this.getFetchTime());
        }

        if (!StringUtils.isEmpty(this.getSecKey())) {
            props.put("secKey", this.getSecKey());
        }

    }

    private void initListener(PropertiesConfiguration config) {
        Map<String, MessageConfCenterListener> listeners = new HashMap();
        listeners.put("contextPropertiesListener", new MessageConfCenterListener() {
            public void notifyEvent(ConfigurationEvent event) {
                switch(event.getType()) {
                    case ADD:
                    case UPDATE:
                        MessagePropertyPlaceholderConfigurer.contextProperties.put(event.getPropertyName(), event.getPropertyValue());
                        break;
                    case CLEAR:
                        MessagePropertyPlaceholderConfigurer.contextProperties.remove(event.getPropertyName());
                }

            }
        });
        config.addConfigurationListener(new MessageConfigurationListener(listeners.values()));
    }

    private void assertNotEmpty(String value, String key) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("ConfigCenter init error!!!! " + key + " can't be empty.");
        } else {
            this.logger.info("[ConfigCenter] Get config '" + key + "' , value=" + value + " .");
        }
    }

    private void assertAddressNotNull(String[] address, String key) {
        if (address == null || StringUtils.isEmpty(address[0]) || StringUtils.isEmpty(address[0])) {
            throw new IllegalArgumentException("ConfigCenter init error!!!! " + key + " formal error.");
        }
    }

    public String getLocalconf() {
        return this.localconf;
    }

    public void setLocalconf(String localconf) {
        this.localconf = localconf;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getProfile() {
        return this.profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getConfAddress() {
        return this.confAddress;
    }

    public void setConfAddress(String confAddress) {
        this.confAddress = confAddress;
    }

    public String getFetchTime() {
        return this.fetchTime;
    }

    public void setFetchTime(String fetchTime) {
        this.fetchTime = fetchTime;
    }

    public String getSecKey() {
        return this.secKey;
    }

    public void setSecKey(String secKey) {
        this.secKey = secKey;
    }

    public boolean isSupportLocal() {
        return this.supportLocal;
    }

    public void setSupportLocal(boolean supportLocal) {
        this.supportLocal = supportLocal;
    }

    public String getConfFileName() {
        return this.confFileName;
    }

    public void setConfFileName(String confFileName) {
        this.confFileName = confFileName;
    }

    static void fillProperties(Properties props, EncodedResource resource, PropertiesPersister persister) throws IOException {
        InputStream stream = null;
        Reader reader = null;

        try {
            String filename = resource.getResource().getFilename();
            if (filename != null && filename.endsWith(".xml")) {
                stream = resource.getInputStream();
                persister.loadFromXml(props, stream);
            } else if (resource.requiresReader()) {
                reader = resource.getReader();
                persister.load(props, reader);
            } else {
                stream = resource.getInputStream();
                persister.load(props, stream);
            }
        } finally {
            if (stream != null) {
                stream.close();
            }

            if (reader != null) {
                reader.close();
            }

        }

    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        if (!isInited.get()) {
            this.init();
        } else {
            this.injectToSpring();
        }

    }

    private void injectToSpring() {
        Properties properties = new Properties();
        properties.putAll(contextProperties);
        this.injectToSpring(properties);
    }

    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        this.logger.debug("[process Properties] {} ", props);
        super.processProperties(beanFactoryToProcess, props);
        this.mergeprops(props);
    }

    public Object getContextProperty(String name) {
        return contextProperties.get(name);
    }

    private void mergeprops(Properties props) {
        Iterator var2 = props.keySet().iterator();

        while(var2.hasNext()) {
            Object key = var2.next();
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            contextProperties.put(keyStr, value);
        }

        this.logger.debug("[merged properties] {} ", props);
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    static {
        DEFAULT_FILE = "file:" + System.getProperty("user.home") + File.separator + "autoconf.properties";
        contextProperties = new ConcurrentHashMap();
        isInited = new AtomicBoolean(false);
    }
}
