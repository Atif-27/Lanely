package com.Transporter.FreightFox.optimizer;

import com.Transporter.FreightFox.dto.response.LaneAssignment;
import com.Transporter.FreightFox.entity.Lane;
import com.Transporter.FreightFox.entity.LaneQuote;
import com.Transporter.FreightFox.exception.InfeasibleAssignmentException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransporterAssignmentOptimizer implements AssignmentOptimizer {

    private static final long NO_QUOTE = Long.MAX_VALUE;

    @Override
    public OptimizationResult optimize(List<Lane> lanes, List<LaneQuote> laneQuotes, int maxTransporters) {
        long[] laneIds = lanes.stream().mapToLong(Lane::getId).sorted().toArray();
        long[] transporterIds = laneQuotes.stream()
                .mapToLong(quote -> quote.getTransporter().getId())
                .distinct()
                .sorted()
                .toArray();
        long[][] quotes = buildQuoteMatrix(laneIds, transporterIds, laneQuotes);

        List<Long> unquotedLaneIds = findUnquotedLanes(laneIds, quotes);
        if (!unquotedLaneIds.isEmpty()) {
            throw new InfeasibleAssignmentException("No transporter has quoted for lane ids " + unquotedLaneIds + ".");
        }

        Plan best = findBestPlan(quotes, Math.min(maxTransporters, transporterIds.length));
        if (best == null) {
            int required = minimumTransportersRequired(quotes, maxTransporters + 1);
            throw new InfeasibleAssignmentException("Cannot cover all lanes with " + maxTransporters
                    + " transporter(s); at least " + required + " are required.");
        }
        return toResult(best, laneIds, transporterIds);
    }

    private long[][] buildQuoteMatrix(long[] laneIds, long[] transporterIds, List<LaneQuote> laneQuotes) {
        Map<Long, Integer> laneIndex = indexOf(laneIds);
        Map<Long, Integer> transporterIndex = indexOf(transporterIds);

        long[][] quotes = new long[transporterIds.length][laneIds.length];
        for (long[] row : quotes) {
            Arrays.fill(row, NO_QUOTE);
        }
        for (LaneQuote quote : laneQuotes) {
            int t = transporterIndex.get(quote.getTransporter().getId());
            int l = laneIndex.get(quote.getLane().getId());
            quotes[t][l] = quote.getQuote();
        }
        return quotes;
    }

    private Map<Long, Integer> indexOf(long[] ids) {
        Map<Long, Integer> index = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            index.put(ids[i], i);
        }
        return index;
    }

    private List<Long> findUnquotedLanes(long[] laneIds, long[][] quotes) {
        List<Long> unquoted = new ArrayList<>();
        for (int l = 0; l < laneIds.length; l++) {
            boolean quoted = false;
            for (long[] transporterQuotes : quotes) {
                if (transporterQuotes[l] != NO_QUOTE) {
                    quoted = true;
                    break;
                }
            }
            if (!quoted) {
                unquoted.add(laneIds[l]);
            }
        }
        return unquoted;
    }

    private Plan findBestPlan(long[][] quotes, int limit) {
        PlanSearch search = new PlanSearch(quotes, limit);
        search.explore(0, new ArrayList<>());
        return search.best;
    }

    private int minimumTransportersRequired(long[][] quotes, int startFrom) {
        for (int size = startFrom; size <= quotes.length; size++) {
            if (findBestPlan(quotes, size) != null) {
                return size;
            }
        }
        return quotes.length;
    }

    private OptimizationResult toResult(Plan plan, long[] laneIds, long[] transporterIds) {
        List<LaneAssignment> assignments = new ArrayList<>();
        for (int l = 0; l < laneIds.length; l++) {
            assignments.add(new LaneAssignment(laneIds[l], transporterIds[plan.laneWinners()[l]]));
        }
        List<Long> selected = Arrays.stream(plan.usedTransporters())
                .mapToObj(t -> transporterIds[t])
                .toList();
        return new OptimizationResult(plan.cost(), assignments, selected);
    }

    private record Plan(long cost, int[] laneWinners, int[] usedTransporters) {

        boolean isBetterThan(Plan other) {
            if (other == null) {
                return true;
            }
            if (cost != other.cost) {
                return cost < other.cost;
            }
            if (usedTransporters.length != other.usedTransporters.length) {
                return usedTransporters.length > other.usedTransporters.length;
            }
            return Arrays.compare(usedTransporters, other.usedTransporters) < 0;
        }
    }

    private static class PlanSearch {

        private final long[][] quotes;
        private final int limit;
        private final int laneCount;
        private Plan best;

        PlanSearch(long[][] quotes, int limit) {
            this.quotes = quotes;
            this.limit = limit;
            this.laneCount = quotes.length == 0 ? 0 : quotes[0].length;
        }

        void explore(int start, List<Integer> chosen) {
            if (!chosen.isEmpty()) {
                Plan plan = evaluate(chosen);
                if (plan != null && plan.isBetterThan(best)) {
                    best = plan;
                }
            }
            if (chosen.size() == limit) {
                return;
            }
            for (int t = start; t < quotes.length; t++) {
                chosen.add(t);
                explore(t + 1, chosen);
                chosen.removeLast();
            }
        }

        private Plan evaluate(List<Integer> chosen) {
            int[] laneWinners = new int[laneCount];
            long cost = 0;
            for (int l = 0; l < laneCount; l++) {
                int winner = -1;
                for (int t : chosen) {
                    if (quotes[t][l] != NO_QUOTE && (winner == -1 || quotes[t][l] < quotes[winner][l])) {
                        winner = t;
                    }
                }
                if (winner == -1) {
                    return null;
                }
                laneWinners[l] = winner;
                cost += quotes[winner][l];
            }
            int[] used = Arrays.stream(laneWinners).distinct().sorted().toArray();
            return new Plan(cost, laneWinners, used);
        }
    }
}
