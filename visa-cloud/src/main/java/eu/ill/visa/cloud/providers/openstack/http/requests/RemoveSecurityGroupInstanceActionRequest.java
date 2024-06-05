package eu.ill.visa.cloud.providers.openstack.http.requests;

public class RemoveSecurityGroupInstanceActionRequest extends InstanceActionRequest  {
    public final SecurityGroup removeSecurityGroup;

    public RemoveSecurityGroupInstanceActionRequest(final String securityGroup) {
        this.removeSecurityGroup  = new SecurityGroup(securityGroup);
    }

    public record SecurityGroup(String name) {
    }
}
