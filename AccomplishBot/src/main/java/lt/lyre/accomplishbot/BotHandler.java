package lt.lyre.accomplishbot;

import org.bson.types.ObjectId;
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

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.configuration.BotConfig;
import lt.lyre.accomplishbot.localization.Languages;
import lt.lyre.accomplishbot.localization.LocalizationManager;
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

    private LocalizationManager localizationManager;
    private MongoDbHandler mongo;

    public BotHandler() {
        mongo = new MongoDbHandler();
        localizationManager = new LocalizationManager();
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
        Integer telegramId = query.getFrom().getId();

        String[] data = query.getData().split(" ");
        String command = data.length > 0 ? data[0] : null;
        String item = data.length > 1 ? data[1] : null;
        String list = data.length > 2 ? data[2] : null;

        switch (command) {
            case "finish":
                mongo.finishListItem(new ObjectId(list), new ObjectId(item), telegramId);
                break;
            case "redo":
                mongo.redoListItem(new ObjectId(list), new ObjectId(item), telegramId);
                break;
            case "remove":
                mongo.removeListItem(new ObjectId(list), new ObjectId(item), telegramId);
                break;
            case "modify":
                // TODO: Implement modify list item feature.
                return;
            case "listItems":
                List<UserListItem> userListItems = mongo.getUserListsByTelegramId(telegramId).stream().filter(i -> i.getId().toString().equals(item)).findFirst().get().getItems();

                EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
                emrm.setChatId(query.getMessage().getChatId() + "");
                emrm.setMessageId(query.getMessage().getMessageId());
                emrm.setReplyMarkup(getMessageReplyMarkup(item, userListItems,
                    mongo.getUserByTelegramId(telegramId)));

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
                    selectedList = mongo.getUserListById(new ObjectId(item), telegramId);
                }
                mongo.setCurrentList(mongo.getUserByTelegramId(telegramId), selectedList);

                List<UserList> items = mongo.getUserListsByTelegramId(telegramId);

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


        List<UserListItem> items = mongo.getUserListById(new ObjectId(list), telegramId).getItems();

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(query.getMessage().getChatId() + "");
        editMessageReplyMarkup.setMessageId(query.getMessage().getMessageId());
        // Change view to "edit item" view when command was edit or modify.
        editMessageReplyMarkup.setReplyMarkup(getMessageReplyMarkup(list, items,
            command.equals("modify") || command.equals("edit"), mongo.getUserByTelegramId(telegramId)));

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
            command = BotCommands.getByCommandString(user.getLastCommand());
            //sendPlainMessage(message.getChatId().toString(), message.getMessageId(), localizationManager.getResource("unknownMessage", user.getLanguage()));
        } else {
            user.setLastCommand(command.getCommandString());
            mongo.updateUser(user);
        }
        String resultMessage;
        switch (command) {
            case CMD_ABOUT:
                sendPlainMessage(message.getChatId().toString(), message.getMessageId(), localizationManager.getResource("aboutMessage", user.getLanguage()));
                break;
            case CMD_ADD:
                addItem(parsedUserCommand, user, message);
                break;
            case CMD_FEEDBACK:
                sendPlainMessage(message.getChatId().toString(), message.getMessageId(), localizationManager.getResource("feedbackMessage", user.getLanguage()));
                break;
            case CMD_FINISH:
//                    String itemToFinish = parsedUserCommand.getParameters().stream().findFirst().orElse("");
//                    boolean isSuccessful = mongo.finishListItem("test", itemToFinish, message.getFrom().getId());
//                    if (isSuccessful) {
//                        resultMessage = String.format("Items: %s was successfully finished", itemToFinish);
//                    } else {
//                        resultMessage = String.format("Item %s was not found", itemToFinish);
//                    }
//                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
                break;
            case CMD_LISTS:
                List<UserList> userLists = mongo.getUserListsByTelegramId(message.getFrom().getId());

                String userListHeaderMessage = "";
                InlineKeyboardMarkup listMarkup = null;
                if (userLists == null || userLists.isEmpty()) {
                    userListHeaderMessage = localizationManager.getResource("emptyListMessage", user.getLanguage());
                } else {
                    listMarkup = getUserListReplyMarkup(userLists);
                    userListHeaderMessage = localizationManager.getResource("itemListMessage", user.getLanguage());
                }

                sendPlainMessage(message.getChatId().toString(), message.getMessageId(), userListHeaderMessage, listMarkup);
                break;
            case CMD_ITEMS:
            case CMD_LIST:
                showList(message, user);
                break;
            case CMD_SETTINGS:
                break;
            case CMD_REMOVE:
//                    String itemToRemove = parsedUserCommand.getParameters().stream().findFirst().orElse("");
//
//                    mongo.removeListItem("test", itemToRemove, message.getFrom().getId());
//
//                    resultMessage = String.format("Item %s was removed successfully", itemToRemove);
//
//                    sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
                break;
            case CMD_START:
                sendWelcomeMessage(user, message.getChatId().toString(), message.getMessageId(), null);
                break;
        }
    }

    private void showList(Message message, User user) {
        List<UserList> result = mongo.getUserListsByTelegramId(message.getFrom().getId());

        String messageText = "";
        InlineKeyboardMarkup rk = null;
        if (result == null || result.isEmpty()) {
            messageText = localizationManager.getResource("emptyListMessage", user.getLanguage());
        } else {
            List<UserListItem> items = result.stream().findAny().get().getItems();
            String listId = result.stream().findAny().get().getId().toString();
            rk = getMessageReplyMarkup(listId, items, user);
            messageText = localizationManager.getResource("itemListMessage", user.getLanguage());
        }

        sendPlainMessage(message.getChatId().toString(), message.getMessageId(), messageText, rk);
        user.setLastCommand(BotCommands.CMD_ADD.getCommandString());
        mongo.updateUser(user);

    }

    private void addItem(ParsedUserCommand parsedUserCommand, User user, Message message) {
        List<String> listItem = parsedUserCommand.getParameters();

        UserList currentList = user.getCurrentList();
        user.setCurrentList(mongo.insertListItem(currentList == null ? new ObjectId() : currentList.getId(), listItem, message.getFrom().getId()));

        mongo.updateUser(user); // Let's update user with current list

        showList(message, user);
        /*String resultMessage;
        if (listItem.size() > 1) {
            resultMessage = String.format(localizationManager.getResource("itemsAddedPluralMessage", user.getLanguage()), listItem.stream().collect(Collectors.joining(", ")));
        } else if (listItem.size() == 1) {
            resultMessage = String.format(localizationManager.getResource("itemAddedSingularMessage", user.getLanguage()), listItem.get(0));
        } else {
            resultMessage = localizationManager.getResource("noItemsAddedMessage", user.getLanguage());
        }

        sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);*/

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

            button.setCallbackData((isSelected ? "unselect " : "select") + " " + item.getId());
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText(item.getListName());
            button.setCallbackData("listItems " + item.getId());
            row.add(button);

            rows.add(row);
        }

        rk.setKeyboard(rows);

        return rk;
    }

    private InlineKeyboardMarkup getMessageReplyMarkup(String listId, List<UserListItem> items,
                                                       User user) {
        return getMessageReplyMarkup(listId, items, false, user);
    }

    private InlineKeyboardMarkup getMessageReplyMarkup(String listId, List<UserListItem> items,
                                                       boolean editItem, User user) {
        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        for (UserListItem item : items) {
            row = new ArrayList<>();

            button = new InlineKeyboardButton();
//            button.setText(item.isFinished() ? "\u2705" : "\u25AA");
//            // If task is finished, then clear it. Otherwise mark it as finished.
//            button.setCallbackData((item.isFinished() ? "redo " : "finish ") + item.getId()
//                    + " " + listId);
//            row.add(button);

            button = new InlineKeyboardButton();
            if (editItem) {
                button.setText("✏️ " + item.getItemName() +
                    localizationManager.getResource("spaces", Languages.ENGLISH));

                button.setCallbackData("modify " + item.getId() + " " + listId);
            } else {
                button.setText((item.isFinished() ? "\u2705" : "\u25AA") + " " + item.getItemName() +
                    localizationManager.getResource("spaces", Languages.ENGLISH));
//            button.setCallbackData("modify " + item.getId() + " " + listId);

                button.setCallbackData((item.isFinished() ? "redo " : "finish ") + item.getId()
                    + " " + listId);
            }
            row.add(button);

//            button = new InlineKeyboardButton();
//            button.setText("\uD83D\uDDD1");
//            button.setCallbackData("remove " + item.getId() + " " + listId);
//            row.add(button);

            rows.add(row);
        }
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        if (editItem) {
            button.setText(localizationManager.getResource("viewButton", user.getLanguage()));
            button.setCallbackData("view list " + listId);
        } else {
            button.setText(localizationManager.getResource("editButton", user.getLanguage()));
            button.setCallbackData("edit list " + listId);
        }
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("moreButton", user.getLanguage()) + " \u25B6");
        button.setCallbackData("more ");
        row.add(button);
        rows.add(row);

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
//        sendMessage.setReplayToMessageId(messageId);
        sendMessage.setReplayMarkup(replyKeyboardMarkup);
        sendMessage.setText(expression);

        try {
            sendMessage(sendMessage);

        } catch (TelegramApiException e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }

    private void sendWelcomeMessage(User user, String chatId, Integer messageId, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
//        sendMessage.setReplayToMessageId(messageId);

        if (replyKeyboardMarkup != null) {
            sendMessage.setReplayMarkup(replyKeyboardMarkup);
        }

        sendMessage.setText(localizationManager.getResource("welcomeMessage", user.getLanguage()));

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }
}