package ada.vcs.client.core.remotes;

import ada.commons.util.ResourceName;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@EqualsAndHashCode(doNotUseGetters = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Remotes {

    @JsonProperty
    private final List<Remote> remotes;

    @JsonProperty
    private final ResourceName upstream;

    @JsonCreator
    public static Remotes apply(
        @JsonProperty("remotes") List<Remote> remotes,
        @JsonProperty("upstream") ResourceName upstream) {

        return new Remotes(Lists.newArrayList(remotes), upstream);
    }

    public static Remotes apply(List<Remote> remotes) {
        return apply(remotes, null);
    }

    public static Remotes apply() {
        return apply(Lists.newArrayList());
    }

    public Optional<Remote> getRemote(String alias) {
        return getRemotes()
            .filter(remote -> remote.getAlias().getValue().equals(alias))
            .findFirst();
    }

    public Optional<Remote> getRemote(ResourceName alias) {
        return getRemote(alias.getValue());
    }

    @JsonIgnore
    public Stream<Remote> getRemotes() {
        return remotes.stream();
    }

    @JsonIgnore
    public Optional<Remote> getUpstream() {
        if (upstream == null) {
            return Optional.empty();
        } else {
            return remotes
                .stream()
                .filter(remote -> remote.getAlias().getValue().equals(upstream.getValue()))
                .findFirst();
        }
    }

    public Remotes add(Remote remote) {
        List<Remote> remotes = this.remotes
            .stream()
            .filter(r -> !r.getAlias().getValue().equals(remote.getAlias().getValue()))
            .collect(Collectors.toList());

        remotes.add(remote);

        if (remotes.size() == 1) {
            return apply(remotes, remote.getAlias());
        } else {
            return apply(remotes, upstream);
        }
    }

    public Remotes remove(Remote remote) {
        return remove(remote.getAlias().getValue());
    }

    public Remotes remove(String alias) {
        List<Remote> remotes = this.remotes
            .stream()
            .filter(r -> !r.getAlias().getValue().equals(alias))
            .collect(Collectors.toList());

        if (upstream != null && upstream.getValue().equals(alias)) {
            if (remotes.size() > 0) {
                return apply(remotes, remotes.get(0).getAlias());
            } else {
                return apply(remotes, null);
            }
        } else {
            return apply(remotes, upstream);
        }
    }

    public Remotes setUpstream(String alias) {
        return getRemote(alias)
            .map(remote -> Remotes.apply(remotes, remote.getAlias()))
            .orElse(this);
    }

}
