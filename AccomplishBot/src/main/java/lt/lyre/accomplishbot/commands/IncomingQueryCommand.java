package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

public enum IncomingQueryCommand {

    INCOMING_COMMAND_OPEN("open/"),
    INCOMING_COMMAND_CLOSE("close/");

    private String commandString;

    private IncomingQueryCommand(String commandString) {
        this.commandString = commandString;
    }

    public String getCommandString() {
        return commandString;
    }

    public static IncomingQueryCommand getByCommandString(String commandString) {
        return Arrays.stream(values())
                .filter(cmd -> cmd.commandString.equalsIgnoreCase(commandString))
                .findFirst()
                .orElse(null);
    }
}

