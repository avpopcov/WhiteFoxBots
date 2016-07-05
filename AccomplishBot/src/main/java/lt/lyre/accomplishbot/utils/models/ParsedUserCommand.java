package lt.lyre.accomplishbot.utils.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ParsedUserCommand {
    
    private String userCommand;
    private List<String> parameters;
    
    public ParsedUserCommand() {
        super();
        parameters = new ArrayList<>();
    }
    public ParsedUserCommand(String userCommand, List<String> parameters) {
        this();
        this.userCommand = userCommand;
        this.parameters = parameters;
    }
    public String getUserCommand() {
        return userCommand;
    }
    public void setUserCommand(String userCommand) {
        this.userCommand = userCommand;
    }
    public List<String> getParameters() {
        return parameters;
    }
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Command: ");
        buffer.append(userCommand);
        buffer.append('\n');
        buffer.append("Parameters: \n");
        IntStream.range(0, parameters.size()).forEach(index -> {
            buffer.append(index + 1);
            buffer.append(". ");
            buffer.append(parameters.get(index));
            buffer.append("\n");
        });
        
        return buffer.toString();
    }
    
}
