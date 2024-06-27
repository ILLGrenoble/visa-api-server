package eu.ill.visa.vdi.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DesktopSession {

    private final String id;
    private final Long instanceId;
    private final String protocol;
    private boolean isLocked;

    private final List<DesktopSessionMember> members = new ArrayList<>();

    public DesktopSession(final Long instanceId, final String protocol) {
        this.id = UUID.randomUUID().toString();
        this.instanceId = instanceId;
        this.protocol = protocol;
    }

    public String getId() {
        return id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public String getProtocol() {
        return protocol;
    }

    public List<DesktopSessionMember> getMembers() {
        return members;
    }

    public Stream<DesktopSessionMember> filterMembers(final Predicate<DesktopSessionMember> predicate) {
        return members.stream().filter(predicate);
    }

    public void addMember(final DesktopSessionMember member) {
        members.add(member);
    }

    public void removeMember(final DesktopSessionMember desktopSessionMember) {
        members.remove(desktopSessionMember);
    }

    public DesktopSessionMember getMemberWithId(final String desktopSessionMemberId) {
        return this.members.stream().filter(member -> member.getId().equals(desktopSessionMemberId)).findAny().orElse(null);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DesktopSession that = (DesktopSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
