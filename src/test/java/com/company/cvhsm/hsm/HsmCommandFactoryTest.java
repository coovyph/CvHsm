package com.company.cvhsm.hsm;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HsmCommandFactoryTest {

    @Test
    void resolvesCommandByCommandCode() {
        HsmCommand<String, String> echoCommand = new TestCommand("B2", "B3");
        HsmCommandFactory factory = new HsmCommandFactory(List.of(echoCommand));

        HsmCommand<String, String> resolved = factory.getCommand("B2");
        assertEquals("B2", resolved.getCommandCode());
        assertEquals("B3", resolved.getResponseCode());
    }

    @Test
    void detectsRegisteredCommand() {
        HsmCommand<String, String> command = new TestCommand("A0", "A1");
        HsmCommandFactory factory = new HsmCommandFactory(List.of(command));

        assertTrue(factory.hasCommand("A0"));
        assertFalse(factory.hasCommand("B2"));
    }

    @Test
    void throwsForUnknownCommandCode() {
        HsmCommandFactory factory = new HsmCommandFactory(List.of());

        assertThrows(IllegalArgumentException.class, () -> factory.getCommand("ZZ"));
    }

    @Test
    void rejectsDuplicateCommandCodes() {
        HsmCommand<String, String> first = new TestCommand("B2", "B3");
        HsmCommand<String, String> second = new TestCommand("B2", "B3");

        assertThrows(IllegalStateException.class, () -> new HsmCommandFactory(List.of(first, second)));
    }

    private static final class TestCommand implements HsmCommand<String, String> {

        private final String commandCode;
        private final String responseCode;

        TestCommand(String commandCode, String responseCode) {
            this.commandCode = commandCode;
            this.responseCode = responseCode;
        }

        @Override
        public String getCommandCode() {
            return commandCode;
        }

        @Override
        public String getResponseCode() {
            return responseCode;
        }

        @Override
        public byte[] packRequestBody(String request) {
            return request.getBytes(StandardCharsets.US_ASCII);
        }

        @Override
        public String unpackResponseBody(String errorCode,Object cache, byte[] body) {
            return new String(body, StandardCharsets.US_ASCII);
        }

		@Override
		public Object createCache(String request) {
			// TODO Auto-generated method stub
			return null;
		}
    }
}
