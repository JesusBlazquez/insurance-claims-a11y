package io.github.jesusblazquez.insurance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.exception.InvalidPlateNumberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlateNumberTest {

    @Test
    @DisplayName("accepts the format in force since 2000")
    void acceptsCurrentFormat() {
        assertThat(PlateNumber.of("1234BCD").value()).isEqualTo("1234BCD");
    }

    @ParameterizedTest
    @DisplayName("accepts the separators people actually type")
    @ValueSource(strings = {"1234 BCD", "1234-bcd", " 1234bcd "})
    void normalisesSeparatorsAndCase(String value) {
        assertThat(PlateNumber.of(value).value()).isEqualTo("1234BCD");
    }

    @ParameterizedTest
    @DisplayName("rejects vowels and the letters excluded by the regulation")
    @ValueSource(strings = {"1234ABC", "1234BCQ", "1234BCE"})
    void rejectsForbiddenLetters(String value) {
        assertThatThrownBy(() -> PlateNumber.of(value)).isInstanceOf(InvalidPlateNumberException.class);
    }

    @ParameterizedTest
    @DisplayName("rejects malformed plates")
    @ValueSource(strings = {"123BCD", "12345BCD", "1234BC", "1234BCDE", "BCD1234", ""})
    void rejectsMalformedPlates(String value) {
        assertThatThrownBy(() -> PlateNumber.of(value)).isInstanceOf(InvalidPlateNumberException.class);
    }
}
