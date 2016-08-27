package com.github.mosen.jssetcd;

import org.apache.log4j.Logger;
import com.jamfsoftware.eventnotifications.JAMFEventNotificationMonitor;
import com.jamfsoftware.eventnotifications.JAMFEventNotificationMonitorResponse;
import com.jamfsoftware.eventnotifications.JAMFEventNotificationParameter;
import com.jamfsoftware.eventnotifications.events.EventType;
import com.jamfsoftware.eventnotifications.shellobjects.JSSEventShell;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static com.jamfsoftware.eventnotifications.events.EventType.EventTypeIdentifier.JSSShutdown;
import static com.jamfsoftware.eventnotifications.events.EventType.EventTypeIdentifier.JSSStartup;

/**
 * The registrator registers the JSS instance with ETCD upon startup, and unregisters it on shutdown.
 */
public class Registrator implements JAMFEventNotificationMonitor
{
    private static final String ETCD_URL = "http://etcd:8001";
    static Logger log = Logger.getLogger(Registrator.class.getName());

    private Properties properties;
    private EtcdClient client;

    public Registrator() {
        properties = System.getProperties();
        properties.put("etcd_url", ETCD_URL);

        client = new EtcdClient(URI.create(ETCD_URL));
        log.info("jss-etcd started");
    }

    public JAMFEventNotificationMonitorResponse eventOccurred(JAMFEventNotificationParameter param) {
        JAMFEventNotificationMonitorResponse response = new JAMFEventNotificationMonitorResponse(this);
        EventType.EventTypeIdentifier eventId = param.getEventType().getIdentifier();
        JSSEventShell jssEvent = (JSSEventShell)param.getEventObject();

        switch (eventId) {
            case JSSStartup:
                register(jssEvent);
                break;
            case JSSShutdown:
                unregister(jssEvent);
                break;
        }

        return response;
    }

    public boolean isRegisteredForEvent(EventType.EventTypeIdentifier e) {
        return e == JSSStartup || e == JSSShutdown;
    }

    private void register(JSSEventShell evt) {
        log.info("Attempting to register the JSS with etcd");

        try {
            EtcdKeysResponse response;

            response = client.put("jss/hostaddr", evt.getHostAddress()).send().get();
            response = client.put("jss/institution", evt.getInstitution()).send().get();
            response = client.put("jss/url", evt.getJssUrl()).send().get();
            response = client.put("jss/path", evt.getWebApplicationPath()).send().get();
            response = client.put("jss/master", evt.getIsClusterMaster() ? "true" : "false").send().get();

        } catch (EtcdAuthenticationException e) {
            log.info("jssetcd failed to authenticate to etcd");
        } catch (EtcdException e) {
            log.info("jssetcd got etcd exception: " + e.getMessage());
        } catch (IOException e) {
            log.info("jssetcd got io exception, probably failed to connect.");
        } catch (TimeoutException e) {
            log.info("jssetcd connection to etcd server timed out.");
        }
    }

    private void unregister(JSSEventShell evt) {

    }
}
