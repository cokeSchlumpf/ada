package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RemotesImpl implements Remotes {

    private final ObjectMapper om;

    private final List<Remote> remotes;

    private final ResourceName upstream;

    public static Remotes apply(ObjectMapper om, List<Remote> remotes, ResourceName upstream) {
        return new RemotesImpl(om, Lists.newArrayList(remotes), upstream);
    }

    public static Remotes apply(ObjectMapper om, List<Remote> remotes) {
        return apply(om, remotes, null);
    }

    public static Remotes apply(ObjectMapper om) {
        return apply(om, Lists.newArrayList());
    }

    @Override
    public Optional<Remote> getRemote(String alias) {
        return getRemotes()
            .filter(remote -> remote.alias().getValue().equals(alias))
            .findFirst();
    }

    @Override
    public Optional<Remote> getRemote(ResourceName alias) {
        return getRemote(alias.getValue());
    }

    @Override
    public Stream<Remote> getRemotes() {
        return remotes.stream();
    }

    @Override
    public Optional<Remote> getUpstream() {
        if (upstream == null) {
            return Optional.empty();
        } else {
            return remotes
                .stream()
                .filter(remote -> remote.alias().getValue().equals(upstream.getValue()))
                .findFirst();
        }
    }

    @Override
    public Remotes add(Remote remote) {
        List<Remote> remotes = this.remotes
            .stream()
            .filter(r -> !r.alias().getValue().equals(remote.alias().getValue()))
            .collect(Collectors.toList());

        remotes.add(remote);

        if (remotes.size() == 1) {
            return apply(om, remotes, remote.alias());
        } else {
            return apply(om, remotes, upstream);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof RemotesImpl) {
            return this.memento().equals(((RemotesImpl) other).memento());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return memento().hashCode();
    }

    @Override
    public Remotes remove(Remote remote) {
        return remove(remote.alias().getValue());
    }

    @Override
    public Remotes remove(String alias) {
        List<Remote> remotes = this.remotes
            .stream()
            .filter(r -> !r.alias().getValue().equals(alias))
            .collect(Collectors.toList());

        if (upstream != null && upstream.getValue().equals(alias)) {
            if (remotes.size() > 0) {
                return apply(om, remotes, remotes.get(0).alias());
            } else {
                return apply(om, remotes, null);
            }
        } else {
            return apply(om, remotes, upstream);
        }
    }

    @Override
    public Remotes setUpstream(String alias) {
        return getRemote(alias)
            .map(remote -> RemotesImpl.apply(om, remotes, remote.alias()))
            .orElse(this);
    }

    @Override
    public void writeTo(OutputStream os) throws IOException {
        om.writeValue(os, memento());
    }

    public RemotesMemento memento() {
        return RemotesMemento.apply(
            remotes.stream().map(Remote::memento).collect(Collectors.toList()),
            upstream);
    }

}
