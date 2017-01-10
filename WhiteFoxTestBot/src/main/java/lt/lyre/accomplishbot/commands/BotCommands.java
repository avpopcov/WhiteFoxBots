package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

public enum BotCommands {

    CMD_START("/start");

    private String commandString;

    private BotCommands(String commandString) {
        this.commandString = commandString;
    }

    public String getCommandString() {
        return commandString;
    }

    public static BotCommands getByCommandString(String commandString) {
        return Arrays.stream(values())
                .filter(cmd -> cmd.commandString.equalsIgnoreCase(commandString))
                .findFirst()
                .orElse(null);
    }
}
