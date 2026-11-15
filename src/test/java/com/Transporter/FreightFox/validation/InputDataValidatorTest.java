package com.Transporter.FreightFox.validation;

import com.Transporter.FreightFox.dto.request.InputDataRequest;
import com.Transporter.FreightFox.dto.request.LaneQuoteRequest;
import com.Transporter.FreightFox.dto.request.LaneRequest;
import com.Transporter.FreightFox.dto.request.TransporterRequest;
import com.Transporter.FreightFox.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputDataValidatorTest {

    private final InputDataValidator validator = new InputDataValidator();

    @Test
    void acceptsValidInput() {
        InputDataRequest request = new InputDataRequest(
                List.of(lane(1), lane(2)),
                List.of(transporter(1, quote(1), quote(2)))
        );

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void rejectsDuplicateLaneIds() {
        InputDataRequest request = new InputDataRequest(
                List.of(lane(1), lane(1)),
                List.of(transporter(1, quote(1)))
        );

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("duplicate lane id 1");
    }

    @Test
    void rejectsDuplicateTransporterIds() {
        InputDataRequest request = new InputDataRequest(
                List.of(lane(1)),
                List.of(transporter(1, quote(1)), transporter(1, quote(1)))
        );

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("duplicate transporter id 1");
    }

    @Test
    void rejectsQuotesForUnknownLanes() {
        InputDataRequest request = new InputDataRequest(
                List.of(lane(1)),
                List.of(transporter(1, quote(9)))
        );

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("transporter 1 quotes unknown lane id 9");
    }

    @Test
    void rejectsMultipleQuotesForSameLane() {
        InputDataRequest request = new InputDataRequest(
                List.of(lane(1)),
                List.of(transporter(1, quote(1), quote(1)))
        );

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("transporter 1 has multiple quotes for lane id 1");
    }

    private LaneRequest lane(long id) {
        return new LaneRequest(id, "Origin", "Destination");
    }

    private TransporterRequest transporter(long id, LaneQuoteRequest... quotes) {
        return new TransporterRequest(id, "Transporter " + id, List.of(quotes));
    }

    private LaneQuoteRequest quote(long laneId) {
        return new LaneQuoteRequest(laneId, 1000L);
    }
}
