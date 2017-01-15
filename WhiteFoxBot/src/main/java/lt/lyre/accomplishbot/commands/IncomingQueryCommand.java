package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

public enum IncomingQueryCommand {

    INCOMING_QUERY_COMMAND_ADD_NEW_USER("add_new_user/"),
    INCOMING_QUERY_COMMAND_NOTIFICATION_DOOR_TOGGLE("notification_door_toggle/"),
    INCOMING_QUERY_COMMAND_GET_DOOR_STATE("get_door_state/"),
    INCOMING_QUERY_COMMAND_DOOR_CONTROLS("door_controls/"),
    INCOMING_QUERY_COMMAND_MAIN_MENU("main_menu/"),
    INCOMING_QUERY_COMMAND_BACK("back/"),
    INCOMING_QUERY_COMMAND_CANCEL("cancel/"),
    INCOMING_QUERY_COMMAND_LT("lt/"),
    INCOMING_QUERY_COMMAND_RU("ru/"),
    INCOMING_QUERY_COMMAND_EN("en/"),
    INCOMING_QUERY_COMMAND_LANGUAGE("language/"),



    ;

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

