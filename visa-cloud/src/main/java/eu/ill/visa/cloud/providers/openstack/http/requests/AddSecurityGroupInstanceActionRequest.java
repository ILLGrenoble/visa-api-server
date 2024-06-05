package eu.ill.visa.cloud.providers.openstack.http.requests;

public class AddSecurityGroupInstanceActionRequest extends InstanceActionRequest  {
    public final SecurityGroup addSecurityGroup;

    public AddSecurityGroupInstanceActionRequest(final String securityGroup) {
        this.addSecurityGroup  = new SecurityGroup(securityGroup);
    }

    public record SecurityGroup(String name) {
    }
}
