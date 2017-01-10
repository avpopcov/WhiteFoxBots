package lt.lyre.accomplishbot.utils;

import java.util.List;
import java.util.stream.IntStream;

import lt.lyre.accomplishbot.models.User;
import org.apache.commons.lang3.StringUtils;

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.utils.models.ParsedUserCommand;

public class UserCommandParser {

    public static final String DEFAULT_USER_DELIMITER = ",";

    private UserCommandParser() {
    }

    public static ParsedUserCommand parseUserInput(String input, User user) {

        ParsedUserCommand result = new ParsedUserCommand();
        if (!StringUtils.isEmpty(input)) {
            //split input into command and params
            String[] commandAndParams = input.startsWith("/") ?
                    input.split("\\s+", 2)
                    : new String[]{null, input};

            BotCommands command = BotCommands.getByCommandString(commandAndParams[0]);
            if (command != null) {
                result.setUserCommand(command.getCommandString());

            }
            return result;
        }
        return result;
    }
}