package com.github.diamond.client.spring;


import com.github.diamond.client.event.ConfigurationEvent;
import com.github.diamond.client.event.ConfigurationListener;
import java.util.Collection;
import java.util.Iterator;
/**
 * @author Donaldhan
 * @create 2018-04-24 20:34
 * @desc
 **/

public class MessageConfigurationListener implements ConfigurationListener {
    private Collection<MessageConfCenterListener> listeners;

    public MessageConfigurationListener(Collection<MessageConfCenterListener> listeners) {
        this.listeners = listeners;
    }

    public void configurationChanged(ConfigurationEvent event) {
        if (this.listeners != null && this.listeners.size() > 0) {
            Iterator var2 = this.listeners.iterator();

            while(var2.hasNext()) {
                MessageConfCenterListener listener = (MessageConfCenterListener)var2.next();
                listener.notifyEvent(event);
            }
        }

    }
}

