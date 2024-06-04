package eu.ill.visa.cloud.providers.openstack.http.responses;

import java.util.List;

public record ServersResponse(List<Server> servers) {
}
