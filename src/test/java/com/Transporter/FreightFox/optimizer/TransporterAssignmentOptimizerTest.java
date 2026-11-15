package com.Transporter.FreightFox.optimizer;

import com.Transporter.FreightFox.entity.Lane;
import com.Transporter.FreightFox.entity.LaneQuote;
import com.Transporter.FreightFox.entity.Transporter;
import com.Transporter.FreightFox.exception.InfeasibleAssignmentException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransporterAssignmentOptimizerTest {

    private static final long[][] TEST_CASE_1_QUOTES = {
            {20835, 10512, 22105, 42481, 19862},
            {48844, 31326, 18640, 45828, 18297},
            {39020, 20648, 31438, 36447, 12789},
            {14400, 44514, 14316, 10678, 13032},
            {11601, 19760, 40870, 20635, 26421},
            {35095, 12494, 17808, 36210, 39444},
            {26070, 41098, 20932, 16897, 27938}
    };

    private static final long[][] TEST_CASE_2_QUOTES = {
            {1000, 1500, 0, 0, 0},
            {0, 0, 2000, 1800, 0},
            {0, 0, 0, 0, 1200},
            {1100, 0, 0, 1750, 0}
    };

    private final TransporterAssignmentOptimizer optimizer = new TransporterAssignmentOptimizer();

    @Test
    void testCase1FindsCheapestPlanWithinThreeTransporters() {
        OptimizationResult result = optimize(TEST_CASE_1_QUOTES, 3);

        assertThat(result.totalCost()).isEqualTo(60139);
        assertThat(result.selectedTransporters()).containsExactly(1L, 4L, 5L);
        assertThat(winners(result)).containsExactly(5L, 1L, 4L, 4L, 4L);
    }

    @Test
    void doesNotUseMoreTransportersThanNeededWhenLimitIsHigh() {
        OptimizationResult result = optimize(TEST_CASE_1_QUOTES, 7);

        assertThat(result.totalCost()).isEqualTo(59896);
        assertThat(result.selectedTransporters()).containsExactly(1L, 3L, 4L, 5L);
    }

    @Test
    void testCase2CoversAllLanesWithThreeTransporters() {
        OptimizationResult result = optimize(TEST_CASE_2_QUOTES, 3);

        assertThat(result.totalCost()).isEqualTo(7500);
        assertThat(result.selectedTransporters()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void testCase2UsesFourthTransporterWhenItLowersCost() {
        OptimizationResult result = optimize(TEST_CASE_2_QUOTES, 4);

        assertThat(result.totalCost()).isEqualTo(7450);
        assertThat(result.selectedTransporters()).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void prefersMoreTransportersWhenCostIsTied() {
        long[][] quotes = {
                {100, 100},
                {0, 100},
                {100, 0}
        };

        OptimizationResult result = optimize(quotes, 2);

        assertThat(result.totalCost()).isEqualTo(200);
        assertThat(result.selectedTransporters()).containsExactly(2L, 3L);
    }

    @Test
    void breaksRemainingTiesByLowestTransporterIds() {
        long[][] quotes = {
                {100, 0},
                {0, 100},
                {100, 0},
                {0, 100}
        };

        OptimizationResult result = optimize(quotes, 2);

        assertThat(result.selectedTransporters()).containsExactly(1L, 2L);
    }

    @Test
    void reportsMinimumTransportersWhenLimitIsTooLow() {
        assertThatThrownBy(() -> optimize(TEST_CASE_2_QUOTES, 2))
                .isInstanceOf(InfeasibleAssignmentException.class)
                .hasMessage("Cannot cover all lanes with 2 transporter(s); at least 3 are required.");
    }

    @Test
    void reportsLanesWithoutAnyQuote() {
        long[][] quotes = {
                {100, 0, 0}
        };

        assertThatThrownBy(() -> optimize(quotes, 3))
                .isInstanceOf(InfeasibleAssignmentException.class)
                .hasMessage("No transporter has quoted for lane ids [2, 3].");
    }

    private OptimizationResult optimize(long[][] quotes, int maxTransporters) {
        List<Lane> lanes = new ArrayList<>();
        for (int l = 0; l < quotes[0].length; l++) {
            lanes.add(new Lane(l + 1L, "Origin " + (l + 1), "Destination " + (l + 1)));
        }

        List<LaneQuote> laneQuotes = new ArrayList<>();
        for (int t = 0; t < quotes.length; t++) {
            Transporter transporter = new Transporter(t + 1L, "Transporter " + (t + 1));
            for (int l = 0; l < quotes[t].length; l++) {
                if (quotes[t][l] > 0) {
                    LaneQuote laneQuote = new LaneQuote(lanes.get(l), quotes[t][l]);
                    transporter.addLaneQuote(laneQuote);
                    laneQuotes.add(laneQuote);
                }
            }
        }
        return optimizer.optimize(lanes, laneQuotes, maxTransporters);
    }

    private List<Long> winners(OptimizationResult result) {
        return result.assignments().stream().map(assignment -> assignment.transporterId()).toList();
    }
}
