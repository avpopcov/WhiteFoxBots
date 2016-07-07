package lt.lyre.accomplishbot;

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.configuration.BotConfig;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.models.UserList;
import lt.lyre.accomplishbot.models.UserListItem;
import lt.lyre.accomplishbot.utils.UserCommandParser;
import lt.lyre.accomplishbot.utils.models.ParsedUserCommand;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

            CallbackQuery query = update.getCallbackQuery();
            if (query != null) {
                handleIncomingQuery(query);
            }

        } catch (Exception e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }

    private void handleIncomingQuery(CallbackQuery query) {
        Integer id = query.getFrom().getId();

        String[] data = query.getData().split(" ");
        String command = data.length > 0 ? data[0] : null;
        String item = data.length > 1 ? data[1] : null;
        String list = data.length > 2 ? data[2] : null;

        switch (command) {
            case "finish":
                mongo.finishListItem(list, item, id);
                break;
            case "redo":
                mongo.redoListItem(list, item, id);
                break;
            case "modify":
                // TODO: Implement modify list item feature.
                break;
            case "remove":
                mongo.removeListItem(list, item, id);
                break;
            case "listItems":
                List<UserListItem> userListItems = mongo.getUserListsByTelegramId(id).stream().filter(i -> i.getListName().equals(item)).findFirst().get().getItems();

                EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
                emrm.setChatId(query.getMessage().getChatId() + "");
                emrm.setMessageId(query.getMessage().getMessageId());
                emrm.setReplyMarkup(getMessageReplyMarkup(item, userListItems));

                try {
                    editMessageReplyMarkup(emrm);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
            case "select":
            case "unselect":
                UserList selectedList = null;

                if (command.equals("select")) {
                    selectedList = mongo.getUserListByName(item, id);
                }
                mongo.setCurrentList(mongo.getUserByTelegramId(id), selectedList);

                List<UserList> items = mongo.getUserListsByTelegramId(id);

                EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
                editMessageReplyMarkup.setChatId(query.getMessage().getChatId() + "");
                editMessageReplyMarkup.setMessageId(query.getMessage().getMessageId());
                editMessageReplyMarkup.setReplyMarkup(getUserListReplyMarkup(items));

                try {
                    editMessageReplyMarkup(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                return;
        }


        List<UserListItem> items = mongo.getUserListByName(list, id).getItems();

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(query.getMessage().getChatId() + "");
        editMessageReplyMarkup.setMessageId(query.getMessage().getMessageId());
        editMessageReplyMarkup.setReplyMarkup(getMessageReplyMarkup(list, items));

        try {
            editMessageReplyMarkup(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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

        ParsedUserCommand parsedUserCommand = UserCommandParser.parseUserInput(message.getText(), user);
        BotLogger.debug(BOT_LOG_TAG, "Parsed user command: \n" + parsedUserCommand);
        BotCommands command = BotCommands.getByCommandString(parsedUserCommand.getUserCommand());

        if (command == null) {
            sendPlainMessage(message.getChatId().toString(), message.getMessageId(),
                    "Did you say: " + message.getText() + "?. Unknown command. Try a different one.");
        } else {
            user.setLastCommand(command.getCommandString());
            mongo.updateUser(user);
            
            String resultMessage;
            switch (command) {
                case CMD_ABOUT:
                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), "A set of programmers from Lyre Inc. made this. Be proud. Be grateful. Most of all be free.");
                    break;
                case CMD_ADD:
                    List<String> listItem = parsedUserCommand.getParameters();
                    user.setCurrentList(mongo.insertListItem("test", listItem, message.getFrom().getId()));

                    mongo.updateUser(user); // Let's update user with current list

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
                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), "feedback@lyre.lt");
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
                case CMD_LISTS:
                    List<UserList> userLists = mongo.getUserListsByTelegramId(message.getFrom().getId());

                    String userListHeaderMessage = "";
                    InlineKeyboardMarkup listMarkup = null;
                    if (userLists == null || userLists.isEmpty()) {
                        userListHeaderMessage = "Your lists are empty.";
                    } else {
                        listMarkup = getUserListReplyMarkup(userLists);
                        userListHeaderMessage = "Your item list:";
                    }

                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), userListHeaderMessage, listMarkup);
                    break;
                case CMD_ITEMS:
                case CMD_LIST:
                    List<UserList> result = mongo.getUserListsByTelegramId(message.getFrom().getId());

                    String messageText = "";
                    InlineKeyboardMarkup rk = null;
                    if (result == null || result.isEmpty()) {
                        messageText = String.format("Your lists are empty.");
                    } else {
                        List<UserListItem> items = result.stream().findAny().get().getItems();
                        String listName = result.stream().findAny().get().getListName();
                        rk = getMessageReplyMarkup(listName, items);
                        messageText = "Your item list:";
                    }

                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), messageText, rk);
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

    private InlineKeyboardMarkup getUserListReplyMarkup(List<UserList> items) {
        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (UserList item : items) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();

            boolean isSelected = false;

            User user = mongo.getUserByTelegramId(item.getTelegramId());
            if (user.getCurrentList() != null &&
                    user.getCurrentList().getId().equals(item.getId())) {
                isSelected = true;
            }

            button.setText(isSelected ? "\u2705" : "\u25AA");

            button.setCallbackData((isSelected ? "unselect " : "select") + " " + item.getListName());
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText(item.getListName());
            button.setCallbackData("listItems " + item.getListName());
            row.add(button);

            rows.add(row);
        }

        rk.setKeyboard(rows);

        return rk;
    }

    private InlineKeyboardMarkup getMessageReplyMarkup(String listName, List<UserListItem> items) {
        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (UserListItem item : items) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(item.isFinished() ? "\u2705" : "\u25AA");
            // If task is finished, then clear it. Otherwise mark it as finished.
            button.setCallbackData((item.isFinished() ? "redo " : "finish ") + item.getItemName()
                    + " " + listName);
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText(item.getItemName());
            button.setCallbackData("modify " + item.getItemName() + " " + listName);
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText("\uD83D\uDDD1");
            button.setCallbackData("remove " + item.getItemName() + " " + listName);
            row.add(button);

            rows.add(row);
        }

        rk.setKeyboard(rows);

        return rk;
    }

    private void sendPlainMessage(String chatId, Integer messageId, String expression) {
        sendPlainMessage(chatId, messageId, expression, null);
    }

    private void sendPlainMessage(String chatId, Integer messageId, String expression,
                                  InlineKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setReplayToMessageId(messageId);
        sendMessage.setReplayMarkup(replyKeyboardMarkup);
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