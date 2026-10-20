package io.github.jesusblazquez.insurance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.insurance.domain.exception.InvalidNifException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NifTest {

    @Test
    @DisplayName("accepts a NIF whose check letter matches the number")
    void acceptsValidNif() {
        assertThat(Nif.of("12345678Z").value()).isEqualTo("12345678Z");
        assertThat(Nif.of("00000000T").value()).isEqualTo("00000000T");
    }

    @Test
    @DisplayName("normalises case and surrounding spaces")
    void normalisesInput() {
        assertThat(Nif.of("  12345678z  ").value()).isEqualTo("12345678Z");
    }

    @Test
    @DisplayName("rejects a NIF whose check letter does not match, which is what catches typos")
    void rejectsWrongCheckLetter() {
        assertThatThrownBy(() -> Nif.of("12345678A"))
                .isInstanceOf(InvalidNifException.class)
                .hasMessageContaining("12345678A");
    }

    @ParameterizedTest
    @DisplayName("rejects anything that is not eight digits and a letter")
    @ValueSource(strings = {"1234567Z", "123456789Z", "ABCDEFGHZ", "12345678", "12345678ZZ", ""})
    void rejectsMalformedInput(String value) {
        assertThatThrownBy(() -> Nif.of(value)).isInstanceOf(InvalidNifException.class);
    }

    @Test
    @DisplayName("rejects null")
    void rejectsNull() {
        assertThatThrownBy(() -> Nif.of(null)).isInstanceOf(InvalidNifException.class);
    }
}
