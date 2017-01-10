package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

/**
 * Created by avpop on 2017-01-10.
 */
public enum IncomingQueryType {

    INCOMING_QUERY_TYPE_MENU("menu/"),
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

