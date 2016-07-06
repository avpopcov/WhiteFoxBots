package lt.lyre.accomplishbot.commands;

import java.util.Arrays;

public enum BotCommands {
    
    CMD_START("/start")
    , CMD_LIST("/list")
    , CMD_LISTS("/lists")
    , CMD_ITEMS("/items")
    , CMD_ADD("/add")
    , CMD_FINISH("/finish")
    , CMD_SETTINGS("/settings")
    , CMD_ABOUT("/about")
    , CMD_FEEDBACK("/feedback")
    , CMD_REMOVE("/remove")
    
    ;
    
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
