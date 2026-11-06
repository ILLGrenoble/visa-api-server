package eu.ill.visa.vdi.domain.models;

import eu.ill.visa.common.vdi.domain.VdiInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DesktopSession {

    private final String connectionId;
    private final VdiInstance instance;
    private final String protocol;
    private boolean isLocked;

    private final List<DesktopSessionMember> members = new ArrayList<>();

    public DesktopSession(final String connectionId, final VdiInstance instance, final String protocol) {
        this.connectionId = connectionId;
        this.instance = instance;
        this.protocol = protocol;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public VdiInstance getInstance() {
        return instance;
    }

    public String getProtocol() {
        return protocol;
    }

    public List<DesktopSessionMember> getMembers() {
        return new ArrayList<>(members);
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

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int memberCount() {
        return members.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DesktopSession that = (DesktopSession) o;
        return Objects.equals(connectionId, that.connectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(connectionId);
    }
}
