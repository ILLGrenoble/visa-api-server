package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.DesktopSessionRttSample;
import eu.ill.visa.persistence.repositories.DesktopSessionRttSampleRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Transactional
@Singleton
public class DesktopSessionRttSampleService {

    private static final Logger logger = LoggerFactory.getLogger(DesktopSessionRttSampleService.class);

    private final DesktopSessionRttSampleRepository repository;

    @Inject
    public DesktopSessionRttSampleService(final DesktopSessionRttSampleRepository repository) {
        this.repository = repository;
    }

    public List<DesktopSessionRttSample> getByInstanceSessionMemberId(Long instanceSessionMemberId) {
        return this.repository.getByInstanceSessionMemberId(instanceSessionMemberId);
    }

    public List<List<DesktopSessionRttSample>> getByInstanceSessionMemberIds(List<Long> instanceSessionMemberIds) {
        List<DesktopSessionRttSample> samples = this.repository.getByInstanceSessionMemberIds(instanceSessionMemberIds);
        return instanceSessionMemberIds.stream().map(id -> {
            return samples.stream().filter(sample -> sample.getInstanceSessionMemberId().equals(id)).toList();
        }).toList();
    }

    public List<DesktopSessionRttSample> getByInstanceId(Long instanceId) {
        return this.repository.getByInstanceId(instanceId);
    }

    public void save(DesktopSessionRttSample desktopSessionRttSample) {
        this.repository.save(desktopSessionRttSample);
    }
}
