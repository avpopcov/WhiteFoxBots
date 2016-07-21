package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

public enum IncomingQueryCommand {

    INCOMING_QUERY_COMMAND_FINISH("finish/"),
    INCOMING_QUERY_COMMAND_REDO("redo/"),
    INCOMING_QUERY_COMMAND_MODIFY("modify/"),
    INCOMING_QUERY_COMMAND_REMOVE("remove/"),
    INCOMING_QUERY_COMMAND_LIST_ITEMS("list_items/"),
    INCOMING_QUERY_COMMAND_SELECT("select/"),
    INCOMING_QUERY_COMMAND_LT("lt/"),
    INCOMING_QUERY_COMMAND_RU("ru/"),
    INCOMING_QUERY_COMMAND_EN("en/"),
    INCOMING_QUERY_COMMAND_CANCEL("cancel/"),
    INCOMING_QUERY_COMMAND_BACK_TO_LIST("back_to_list/"),
    INCOMING_QUERY_COMMAND_VIEW("view/"),
    INCOMING_QUERY_COMMAND_EDIT("edit/"),
    INCOMING_QUERY_COMMAND_MORE("more/"),
    INCOMING_QUERY_COMMAND_LANGUAGE("language/"),
    INCOMING_QUERY_COMMAND_TEXT_DELIMITER("text_delimiter/");

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

