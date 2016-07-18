package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

public enum IncomingQueryType {

    INCOMING_QUERY_TYPE_MESSAGE("message/"),
    INCOMING_QUERY_TYPE_LIST("list/"),
    INCOMING_QUERY_TYPE_LANGUAGE("language/"),
    INCOMING_QUERY_TYPE_SETTINGS("settings/");

    private String commandString;

    private IncomingQueryType(String commandString) {
        this.commandString = commandString;
    }

    public String getCommandString() {
        return commandString;
    }

    public static IncomingQueryType getByCommandString(String commandString) {
        return Arrays.stream(values())
                .filter(cmd -> cmd.commandString.equalsIgnoreCase(commandString))
                .findFirst()
                .orElse(null);

    }

}
