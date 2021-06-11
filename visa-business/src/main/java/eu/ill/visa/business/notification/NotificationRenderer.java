package eu.ill.visa.business.notification;

import eu.ill.visa.business.NotificationRendererException;

public interface NotificationRenderer {

    public String render() throws NotificationRendererException;
}
