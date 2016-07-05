package lt.lyre.accomplishbot;

import java.io.InvalidObjectException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.logging.BotLogger;

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.configuration.BotConfig;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.models.UserList;
import lt.lyre.accomplishbot.models.UserListItem;
import lt.lyre.accomplishbot.utils.UserCommandParser;
import lt.lyre.accomplishbot.utils.models.ParsedUserCommand;

/**
 * Created by Dmitrij on 2016-06-18.
 */
public class BotHandler extends TelegramLongPollingBot {
    private static final String BOT_LOG_TAG = BotConfig.BOT_USERNAME + "_Log_Tag";
    private static final String WELCOME_MESSAGE = "Welcome to sample Lyre Calc Bot! Please input mathematical expression to keep you going.";

    private MongoDbHandler mongo;

    public BotHandler() {
        mongo = new MongoDbHandler();
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            Message message = update.getMessage();
            if (message != null && message.hasText()) {
                try {
                    handleIncomingMessage(message);
                } catch (InvalidObjectException e) {
                    BotLogger.severe(BOT_LOG_TAG, e);
                }
            }
        } catch (Exception e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }

    private void handleIncomingMessage(Message message) throws InvalidObjectException {
        User user = mongo.getUserByTelegramId(message.getFrom().getId());
        if (user == null) {
            User newUser = new User();
            newUser.setTelegramId(message.getFrom().getId());
            newUser.setUserName(message.getFrom().getUserName());
            newUser.setLastCommand(message.getText());

            mongo.insertUser(newUser);

            user = mongo.getUserByTelegramId(message.getFrom().getId());
        }

        ParsedUserCommand parsedUserCommand = UserCommandParser.parseUserInput(message.getText(), user.getDelimiter());
        BotLogger.debug(BOT_LOG_TAG, "Parsed user command: \n" + parsedUserCommand);
        if (parsedUserCommand.getUserCommand() != null) {
            mongo.logLastCommand(user, message.getText());
        }
        BotCommands command = BotCommands.getByCommandString(parsedUserCommand.getUserCommand());

        if (command == null) {
            sendPlainMessage(message.getChatId().toString(), message.getMessageId(),
                    "Did you say: " + message.getText() + "?. Unknown command. Try a different one.");
        } else {

            String resultMessage;
            switch (command) {
                case CMD_ABOUT:
                    break;
                case CMD_ADD:
                    List<String> listItem = parsedUserCommand.getParameters();

                    mongo.insertListItem("test", listItem, message.getFrom().getId());

                    if (listItem.size() > 1) {
                        resultMessage = String.format("Items: %s were added to the list.",
                                listItem.stream().collect(Collectors.joining(", ")));
                    } else if (listItem.size() == 1) {
                        resultMessage = String.format("Item %s was added to the list.", listItem.get(0));
                    } else {
                        resultMessage = "No items were added";
                    }

                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
                    break;
                case CMD_FEEDBACK:
                    break;
                case CMD_FINISH:

                    String itemToFinish = parsedUserCommand.getParameters().stream().findFirst().orElse("");
                    boolean isSuccessful = mongo.finishListItem("test", itemToFinish, message.getFrom().getId());
                    if (isSuccessful) {
                        resultMessage = String.format("Items: %s was successfully finished", itemToFinish);
                    } else {
                        resultMessage = String.format("Item %s was not found", itemToFinish);
                    }
                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
                    break;
                case CMD_LIST:
                    List<UserList> result = mongo.getUserListByTelegramId(message.getFrom().getId());

                    String messageText = "";
                    if (result == null || result.isEmpty()) {
                        messageText = String.format("Your lists are empty.");
                    } else {
                        // get longest item name for equal padding
                        int maxItemLength = result.stream()
                                .filter(userList -> userList.getItems() != null && !userList.getItems().isEmpty())
                                .map(UserList::getItems).flatMap(List::stream)
                                .mapToInt(item -> item.getItemName().length()).max().orElse(0) + 3;

                        // get longest item list for equal item number padding
                        int maxItemNumberLength = String.valueOf(
                                result.stream()
                                .filter(userList -> userList.getItems() != null && !userList.getItems().isEmpty())
                                .mapToInt(userList -> userList.getItems().size())
                                .max().orElse(0)
                        ).length();
                        
                        //format pattern for the list, given item lengths
                        String formatPattern = "%1$0" + maxItemNumberLength + "d" + ". %2$s (%3$1s)";
                        BotLogger.debug(BOT_LOG_TAG, "Format String: " + formatPattern);
                        
                        // string for list of lists, separated by 2 newlines
                        messageText = result.stream()
                                .filter(userList -> userList.getItems() != null && !userList.getItems().isEmpty())
                                .map(userList -> {
                                    List<UserListItem> items = userList.getItems();
                                    String itemsString = IntStream.range(0, items.size()).mapToObj(index -> {
                                        String isDone = items.get(index).isFinished() ? "X" : " ";
                                        String paddedItemName = StringUtils.rightPad(
                                                items.get(index).getItemName(), maxItemLength, '.');
                                        //index with +1 to avoid printing item 0
                                        return String.format(formatPattern, 
                                                index + 1, 
                                                paddedItemName,
                                                isDone);
                                    }).collect(Collectors.joining(System.lineSeparator()));

                                    return itemsString;
                                }).collect(Collectors.joining(System.lineSeparator()));
                    }

                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), messageText);
                    break;
                case CMD_SETTINGS:
                    break;
                case CMD_REMOVE:

                    String itemToRemove = parsedUserCommand.getParameters().stream().findFirst().orElse("");

                    mongo.removeListItem("test", itemToRemove, message.getFrom().getId());

                    resultMessage = String.format("Item %s was removed successfully", itemToRemove);

                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
                    break;
                case CMD_START:
                    sendWelcomeMessage(message.getChatId().toString(), message.getMessageId(), null);
                    break;
            }
        }
    }

    private void sendPlainMessage(String chatId, Integer messageId, String expression) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setReplayToMessageId(messageId);

        sendMessage.setText(expression);

        try {
            sendMessage(sendMessage);

        } catch (TelegramApiException e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }

    private void sendWelcomeMessage(String chatId, Integer messageId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setReplayToMessageId(messageId);

        if (replyKeyboardMarkup != null) {
            sendMessage.setReplayMarkup(replyKeyboardMarkup);
        }

        sendMessage.setText(WELCOME_MESSAGE);

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }
}