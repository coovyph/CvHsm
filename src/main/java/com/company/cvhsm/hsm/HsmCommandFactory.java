package com.company.cvhsm.hsm;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves {@link HsmCommand} implementations by their HSM command code.
 */
@Component
public class HsmCommandFactory {

    private final Map<String, HsmCommand<?, ?>> commandsByCode;

    public HsmCommandFactory(List<HsmCommand<?, ?>> commands) {
        this.commandsByCode = new HashMap<>();
        for (HsmCommand<?, ?> command : commands) {
            HsmCommand<?, ?> existing = commandsByCode.put(command.getCommandCode(), command);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate HsmCommand registered for command code " + command.getCommandCode());
            }
        }
    }

    /**
     * Returns the command implementation for the given command code.
     *
     * @throws IllegalArgumentException if no command is registered for the code
     */
    @SuppressWarnings("unchecked")
    public <Req, Resp> HsmCommand<Req, Resp> getCommand(String commandCode) {
        HsmCommand<?, ?> command = commandsByCode.get(commandCode);
        if (command == null) {
            throw new IllegalArgumentException("No HsmCommand registered for command code " + commandCode);
        }
        return (HsmCommand<Req, Resp>) command;
    }

    public boolean hasCommand(String commandCode) {
        return commandsByCode.containsKey(commandCode);
    }
}
