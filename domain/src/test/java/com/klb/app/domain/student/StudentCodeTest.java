package com.klb.app.domain.student;

import com.klb.app.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentCodeTest {

	@Test
	void parseTrimsAndAcceptsValid() {
		assertEquals("SV01", StudentCode.parse("  SV01 ").value());
	}

	@Test
	void parseRejectsBlank() {
		assertThrows(DomainException.class, () -> StudentCode.parse("   "));
		assertThrows(DomainException.class, () -> StudentCode.parse(null));
	}

	@Test
	void tryParseForImportReturnsEmptyWhenInvalid() {
		assertTrue(StudentCode.tryParseForImport("").isEmpty());
		assertTrue(StudentCode.tryParseForImport("   ").isEmpty());
		assertTrue(StudentCode.tryParseForImport(null).isEmpty());
	}

	@Test
	void tryParseForImportAcceptsTrimmed() {
		Optional<StudentCode> o = StudentCode.tryParseForImport(" x ");
		assertTrue(o.isPresent());
		assertEquals("x", o.get().value());
	}
}
