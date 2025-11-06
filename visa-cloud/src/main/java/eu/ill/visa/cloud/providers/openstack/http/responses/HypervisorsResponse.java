package eu.ill.visa.cloud.providers.openstack.http.responses;

import eu.ill.visa.cloud.domain.CloudHypervisor;

import java.util.List;

public record HypervisorsResponse(List<CloudHypervisor> hypervisors) {
}
