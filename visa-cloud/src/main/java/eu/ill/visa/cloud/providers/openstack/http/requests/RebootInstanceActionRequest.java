package eu.ill.visa.cloud.providers.openstack.http.requests;

public class RebootInstanceActionRequest extends InstanceActionRequest  {
    public RebootInstanceActionRequestInner reboot = new RebootInstanceActionRequestInner();

    public static final class RebootInstanceActionRequestInner {
        public String type = "HARD";

    }
}
