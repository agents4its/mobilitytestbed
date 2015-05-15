package cz.agents.agentpolis.darptestbed.simmodel.agent.data.generator;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/**
 * Specifies the set of passengers that need to be picked up and another set of passengers that need to be dropped off on one specific node.
 * @see cz.agents.agentpolis.darptestbed.simmodel.agent.data.TripPlan
 */
public class PassengersInAndOutPair {

    private final Set<String> in;
    private final Set<String> off;

    public PassengersInAndOutPair(Set<String> in, Set<String> off) {
        this.in = new HashSet(in);
        this.off = new HashSet(off);
    }

    public PassengersInAndOutPair() {
        this.in = Sets.newHashSet();
        this.off = Sets.newHashSet();
    }

    public PassengersInAndOutPair(PassengersInAndOutPair toCopy) {
        this.in = new HashSet<>(toCopy.getIn());
        this.off = new HashSet<>(toCopy.getOff());
    }

    public void addAll(PassengersInAndOutPair source) {
        in.addAll(source.getIn());
        off.addAll(source.getOff());
    }

    public Set<String> getIn() {
        return in;
    }

    public Set<String> getOff() {
        return off;
    }

    public int size() {
        return getIn().size() + getOff().size();
    }

    public boolean isEmpty() {
        return getIn().isEmpty() && getOff().isEmpty();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[In: ");
        sb.append(in.toString());
        sb.append(" Off: ");
        sb.append(off.toString());
        sb.append("]");
        return sb.toString();
    }
}
