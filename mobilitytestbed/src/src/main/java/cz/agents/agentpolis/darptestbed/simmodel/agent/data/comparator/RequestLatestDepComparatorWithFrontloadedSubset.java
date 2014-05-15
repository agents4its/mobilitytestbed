package cz.agents.agentpolis.darptestbed.simmodel.agent.data.comparator;

import cz.agents.agentpolis.darptestbed.simmodel.agent.data.Request;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class RequestLatestDepComparatorWithFrontloadedSubset implements Comparator<Request> {

    private final RequestLatestDepComparator comparator;
    private final Set<Request> frontloadedRequests;

    public RequestLatestDepComparatorWithFrontloadedSubset(Set<Request> frontloadedRequests) {

        if (frontloadedRequests == null)
            frontloadedRequests = new HashSet<>();

        this.frontloadedRequests = frontloadedRequests;
        comparator = new RequestLatestDepComparator();
    }

    @Override
    public int compare(Request o1, Request o2) {
        if (frontloadedRequests.contains(o1)) {
            if (frontloadedRequests.contains(o2)) {
                return comparator.compare(o1, o2);
            } else {
                return -1;
            }
        } else {
            if (frontloadedRequests.contains(o2)) {
                return 1;
            } else {
                return comparator.compare(o1, o2);
            }
        }

    }
}
