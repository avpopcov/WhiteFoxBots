package lt.lyre.accomplishbot.utils;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.utils.models.ParsedUserCommand;

public class UserCommandParser {
    
    public static final String DEFAULT_USER_DELIMITER = ",";

    private UserCommandParser() {}
    
    public static ParsedUserCommand parseUserInput(String input, String userDelimiter) {
        
        ParsedUserCommand result = new ParsedUserCommand();
        if (!StringUtils.isEmpty(input)) {
            //split input into command and params
            String[] commandAndParams = input.startsWith("/")? 
                    input.split("\\s+", 2) 
                    : new String[] {null, input};
                    
            //split params by user delimiter
            String delimiter = StringUtils.isEmpty(userDelimiter)? DEFAULT_USER_DELIMITER : userDelimiter;
            String[] paramsStrings = commandAndParams.length > 1? 
                    commandAndParams[1].split(delimiter + "\\s*") 
                    : new String[0];
            
            BotCommands command = BotCommands.getByCommandString(commandAndParams[0]);
            if (command != null) {
                result.setUserCommand(command.getCommandString());
            }
            List<String> parameters = result.getParameters();
            IntStream.range(0, paramsStrings.length)
                .filter(index -> !StringUtils.isEmpty(paramsStrings[index]))
                .mapToObj(index -> paramsStrings[index].trim())
                .forEach(part -> parameters.add(part));
        }
        return result;
    }

}
