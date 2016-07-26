package lt.lyre.accomplishbot;

import lt.lyre.accomplishbot.commands.IncomingQueryCommand;
import lt.lyre.accomplishbot.commands.IncomingQueryType;
import lt.lyre.accomplishbot.models.UserListHeader;
import org.bson.types.ObjectId;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
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

import static lt.lyre.accomplishbot.commands.IncomingQueryCommand.*;
import static lt.lyre.accomplishbot.commands.IncomingQueryType.*;

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

        String[] data = query.getData().split("/");
        IncomingQueryType type = data.length > 0 ? IncomingQueryType.getByCommandString(data[0] + "/") : null;
        IncomingQueryCommand command = data.length > 1 ? IncomingQueryCommand.getByCommandString(data[1] + "/") : null;
        String item = data.length > 2 ? data[2] : null;
        String list = data.length > 3 ? data[3] : null;

        switch (type) {
            case INCOMING_QUERY_TYPE_LIST:
                manageListSelection(command, query.getMessage(), item, list);
                break;
            case INCOMING_QUERY_TYPE_LANGUAGE:
                manageLanguageSelection(command, query.getMessage());
                break;
            case INCOMING_QUERY_TYPE_SETTINGS:
                manageSettingsSelection(command, query.getMessage());
                break;
        }
    }

    private void manageLanguageSelection(IncomingQueryCommand command, Message message) {

        User user = mongo.getUserByTelegramId(message.getChat().getId());
        Languages language = Languages.ENGLISH;

        switch (command) {
            case INCOMING_QUERY_COMMAND_LT:
                language = Languages.LITHUANIAN;
                break;
            case INCOMING_QUERY_COMMAND_RU:
                language = Languages.RUSSIAN;
                break;
            default:
                break;
        }

        user.setLanguage(language);
        showWelcomeMessage(user, message);
        mongo.updateUser(user);

    }

    private void manageSettingsSelection(IncomingQueryCommand command, Message message) {

        User user = mongo.getUserByTelegramId(message.getChat().getId());
        Languages language;

        switch (command) {
            case INCOMING_QUERY_COMMAND_BACK_TO_LIST:
                showList(message, user, true);
                break;

            case INCOMING_QUERY_COMMAND_CANCEL:
                showSettings(message, user, true);
                break;

            case INCOMING_QUERY_COMMAND_TEXT_DELIMITER:
                showDelimiterSelect(user, message);
                break;

            case INCOMING_QUERY_COMMAND_LANGUAGE:
                showLanguageSelect(user, message, INCOMING_QUERY_TYPE_SETTINGS.getCommandString(), false);
                break;

            case INCOMING_QUERY_COMMAND_EN:
            case INCOMING_QUERY_COMMAND_RU:
            case INCOMING_QUERY_COMMAND_LT:

                language = Languages.ENGLISH;

                switch (command) {
                    case INCOMING_QUERY_COMMAND_LT:
                        language = Languages.LITHUANIAN;
                        break;
                    case INCOMING_QUERY_COMMAND_RU:
                        language = Languages.RUSSIAN;
                        break;
                    default:
                        break;
                }

                user.setLanguage(language);
                mongo.updateUser(user);
                showSettings(message, user, true);
                break;
        }
    }

    private void manageListSelection(IncomingQueryCommand command, Message message, String item, String list) {
        User user = mongo.getUserByTelegramId(message.getChat().getId());

        Long telegramId = message.getChat().getId();

        switch (command) {

            case INCOMING_QUERY_COMMAND_RENAME:
                showRename(user, message, item);
                break;
            case INCOMING_QUERY_COMMAND_MODIFY:
                UserList newList = mongo.getUserListById(new ObjectId(item));

                if (newList == null) {
                    newList = mongo.makeNewListFromItem(new ObjectId(item),
                            mongo.getUserListById(new ObjectId(list)), user.getTelegramId());
                }
                user.getLists().add(new UserListHeader(newList.getId(), newList.getListName()));
                user.setCurrentListId(newList.getId());
                break;
            case INCOMING_QUERY_COMMAND_DELETE:
                showDelete(user, message, list);
                break;
            case INCOMING_QUERY_COMMAND_CONFIRM_DELETE:
                deleteList(user, message, list);
                break;
            case INCOMING_QUERY_COMMAND_BACK_TO_LIST:
                showList(message, user, true);
                break;
            case INCOMING_QUERY_COMMAND_SELECT:
                break;
            case INCOMING_QUERY_COMMAND_TOGGLE_MORE:
                user.setVisibleMoreFunctions(!user.isVisibleMoreFunctions());
                break;
            case INCOMING_QUERY_COMMAND_FINISH:
                mongo.finishListItem(new ObjectId(list), new ObjectId(item), telegramId);
                break;
            case INCOMING_QUERY_COMMAND_REDO:
                mongo.redoListItem(new ObjectId(list), new ObjectId(item), telegramId);
                break;
            case INCOMING_QUERY_COMMAND_TOGGLE_VIEW:
                user.setManageView(!user.isManageView());
                break;
            case INCOMING_QUERY_COMMAND_TOGGLE_SHOW_COMPLETE_MESSAGES:
                // TODO: Implement modify list item feature.
                user.setVisibleCheckedMessages(!user.isVisibleCheckedMessages());
                break;
        }

        showList(message, user, true);
    }
    private void deleteList(User user, Message message, String listId) {
        ObjectId listOId = new ObjectId(listId);

        if(!mongo.getUserListById(listOId).getListName().equals("ROOT")){
            mongo.deleteList(listOId);
            goToListParent(user, message);
        }
    }

    private void showDelete(User user, Message message, String list) {

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("delete_button", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() + INCOMING_QUERY_COMMAND_CONFIRM_DELETE.getCommandString());
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("back", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() + INCOMING_QUERY_COMMAND_BACK_TO_LIST.getCommandString());
        row.add(button);

        rows.add(row);

        rk.setKeyboard(rows);

        mongo.getUserListById(new ObjectId(list)).getListName();
        String messageText = "Confirm Delete of list " +
                mongo.getUserListById(new ObjectId(list)).getListName();
        editMessage(message.getChatId(), user.getLastListMessageId(), messageText, rk);
    }

    private void handleIncomingMessage(Message message) throws InvalidObjectException {
        User user = mongo.getUserByTelegramId(message.getFrom().getId());
        if (user == null) {

            User newUser = new User();
            newUser.setTelegramId(message.getFrom().getId());
            newUser.setFirstName(message.getFrom().getFirstName());
            newUser.setUserName(message.getFrom().getUserName());
            newUser.setLastCommand(message.getText());

            UserList rootList = mongo.insertRootList(newUser.getTelegramId());
            newUser.setRootListId(rootList.getId());

            List<UserListHeader> lists = new ArrayList<UserListHeader>();
            lists.add(new UserListHeader(rootList.getId(), rootList.getListName()));

            UserList currentList = mongo.makeNewListFromItem(rootList.getItems().get(0).getId(), rootList, newUser.getTelegramId());
            newUser.setCurrentListId(currentList.getId());

            lists.add(new UserListHeader(currentList.getId(), currentList.getListName()));

            newUser.setLists(lists);
            mongo.insertUser(newUser);

            user = mongo.getUserByTelegramId(message.getFrom().getId());
        }

        ParsedUserCommand parsedUserCommand = UserCommandParser.parseUserInput(message.getText(), user);
        BotLogger.debug(BOT_LOG_TAG, "Parsed user command: \n" + parsedUserCommand);
        BotCommands command = BotCommands.getByCommandString(parsedUserCommand.getUserCommand());

        if (command == null) {
            command = BotCommands.getByCommandString(user.getLastCommand());
        } else {
            if (goToUserList(message, user)) {
                return;
            }
            user.setLastCommand(command.getCommandString());
            mongo.updateUser(user);
        }

        switch (command) {
            case CMD_ABOUT:
                sendPlainMessage(message.getChatId(), localizationManager.getResource("aboutMessage", user.getLanguage()));
                user.setLastCommand(BotCommands.CMD_ADD.getCommandString());
                mongo.updateUser(user);
                break;
            case CMD_ADD:
                addItem(parsedUserCommand, user, message);
                break;
            case CMD_FEEDBACK:
                sendPlainMessage(message.getChatId(), localizationManager.getResource("feedbackMessage", user.getLanguage()));
                user.setLastCommand(BotCommands.CMD_ADD.getCommandString());
                mongo.updateUser(user);
                break;
            case CMD_LIST:
                showList(message, user, false);
                break;
            case CMD_SETTINGS:
                if (user.getLastIncomingQueryCommand() != null &&
                        user.getLastIncomingQueryCommand().equals(INCOMING_QUERY_COMMAND_TEXT_DELIMITER.getCommandString())) {
                    setDelimiter(message, user);
                } else {
                    showSettings(message, user, false);
                }
                break;
            case CMD_START:
                showLanguageSelect(user, message, INCOMING_QUERY_TYPE_LANGUAGE.getCommandString(), true);
                user.setLastCommand(BotCommands.CMD_ADD.getCommandString());
                mongo.updateUser(user);
                break;
        }
    }

    private void goToListParent(User user, Message message){

        List<UserListHeader> listHeaders = user.getLists();


        UserListHeader listHeader = listHeaders.get(listHeaders.size()-2);
        listHeaders.remove(listHeaders.size()-1);
        user.setCurrentListId(listHeader.getId());
        mongo.insertUser(user);
    }

    private boolean goToUserList(Message message, User user) {

        String[] data = message.getText().split("/");
        String command =  data.length > 1 ? data[1] : null;

        if (command != null) {
            ObjectId listId = null;

            List<UserListHeader> listHeaders = user.getLists();
            List<UserListHeader> newListHeaders = new ArrayList<UserListHeader>();

            for (int i = 0; i < listHeaders.size() - 1; i++) {
                newListHeaders.add(listHeaders.get(i));
                if (listHeaders.get(i).getListName().equals(command)) {
                    listId = listHeaders.get(i).getId();
                    break;
                }
            }
            if (listId != null) {

                user.setLists(newListHeaders);
                user.setCurrentListId(listId);
                mongo.insertUser(user);
                showList(message, user, false);
                return true;
            }
        }
        return false;
    }

    private void showList(Message message, User user, boolean editExisting) {

        purgeLastListMessage(message, user);

        user.setLastListMessageId(message.getMessageId() + (editExisting ? 0 : 1));

        UserList list = mongo.getUserListById(user.getCurrentListId());

        String messageText;
        InlineKeyboardMarkup rk = null;
        if (list == null) {
            messageText = localizationManager.getResource("emptyListMessage", user.getLanguage());
        } else {
            List<UserListItem> items = list.getItems();
            String listId = list.getId().toString();
            rk = makeItemList(listId, items, user);
            messageText = getListPath(user) + list.getListName();
        }

        if (editExisting) {
            editMessage(message.getChatId(), user.getLastListMessageId(), messageText, rk);
        } else {
            sendList(message.getChatId(), user.getLastListMessageId(), messageText, rk);
        }
        user.setLastCommand(BotCommands.CMD_ADD.getCommandString());
        mongo.updateUser(user);
    }

    private String getListPath(User user) {

        String path = "";

        List<UserListHeader> listHeaders = user.getLists();

        for (int i = 0; i < listHeaders.size() - 1; i++) {
            path += "/" + listHeaders.get(i).getListName() + " ";
        }
        return path;
    }

    private void showSettings(Message message, User user, boolean reply) {

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        button = new InlineKeyboardButton();
        button.setText((localizationManager.getResource("text_delimiter", user.getLanguage())) + " (" + user.getDelimiter() + ")");
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_TEXT_DELIMITER.getCommandString());
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("language", user.getLanguage()) +
                " (" + localizationManager.getResource("language_short", user.getLanguage()) + ")");
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_LANGUAGE.getCommandString());
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("back", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_BACK_TO_LIST.getCommandString());
        row.add(button);

        rows.add(row);

        rk.setKeyboard(rows);

        String messageText = localizationManager.getResource("settings", user.getLanguage());

        if (reply) {
            editMessage(message.getChatId(), message.getMessageId(), messageText, rk);
        } else {
            sendPlainMessage(message.getChatId(), messageText, rk);
        }
    }

    private void purgeLastListMessage(Message message, User user) {
        if (user.getLastListMessageId() != null) {
            editMessageText(message.getChatId(), user.getLastListMessageId(), " \u25BA");
        }
    }

    private void addItem(ParsedUserCommand parsedUserCommand, User user, Message message) {
        List<String> listItem = parsedUserCommand.getParameters();

        ObjectId currentListId = user.getCurrentListId();
        user.setCurrentListId(mongo.insertListItem(currentListId == null ? new ObjectId() : currentListId, listItem, message.getFrom().getId()).getId());

        mongo.updateUser(user); // Let's update user with current list

        showList(message, user, false);
    }

    private InlineKeyboardMarkup makeItemList(String listId, List<UserListItem> items, User user) {
        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        boolean manageView = user.isManageView();


        List<UserListItem> finishedItems = new ArrayList<UserListItem>();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        for (UserListItem item : items) {
            if (item.isFinished()) {
                finishedItems.add(item);
            } else {
                rows.add(makeListItemButton(item, listId, manageView, " \u25AA"));
            }
        }
        //List Control buttons

        if (!finishedItems.isEmpty()) {
            row = new ArrayList<>();

            boolean showCompleteTasks = user.isVisibleCheckedMessages();
            button = new InlineKeyboardButton();
            button.setText(localizationManager.getResource(showCompleteTasks ? "complete_tasks" : "complete_tasks",
                    user.getLanguage()) + (showCompleteTasks ? " \u25BC" : " \u25BA"));
            button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                    INCOMING_QUERY_COMMAND_TOGGLE_SHOW_COMPLETE_MESSAGES.getCommandString() + "/" + listId);
            row.add(button);

            rows.add(row);

            if (showCompleteTasks) {
                for (UserListItem item : finishedItems) {
                    rows.add(makeListItemButton(item, listId, manageView, "\u2705"));
                }
            }
        }

        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource(manageView ? "manage_button" : "navigate_button", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                INCOMING_QUERY_COMMAND_TOGGLE_VIEW.getCommandString() + "/" + listId);
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("more_button", user.getLanguage()) +
                (user.isVisibleMoreFunctions() ? " \u25BC" : " \u25BA"));
        button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                INCOMING_QUERY_COMMAND_TOGGLE_MORE.getCommandString() + "/" + listId);
        row.add(button);

        if (user.isVisibleMoreFunctions()) {

            rows.add(row);

            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            button.setText(localizationManager.getResource("delete_button", user.getLanguage()));
            button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                    INCOMING_QUERY_COMMAND_DELETE.getCommandString() + "/" + listId);
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText(localizationManager.getResource("rename_button", user.getLanguage()));
            button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                    INCOMING_QUERY_COMMAND_RENAME.getCommandString() + "/" + listId);
            row.add(button);

            button = new InlineKeyboardButton();
            button.setText(localizationManager.getResource("select_button", user.getLanguage()));
            button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                    INCOMING_QUERY_COMMAND_SELECT.getCommandString() + "/" + listId);
            row.add(button);
        }

        rows.add(row);

        rk.setKeyboard(rows);

        return rk;
    }

    private List<InlineKeyboardButton> makeListItemButton(UserListItem item, String listId, boolean manageView, String icon) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        if (manageView) {
            button.setText("✏️ " + item.getItemName() +
                    localizationManager.getResource("spaces", Languages.ENGLISH));
            button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() +
                    INCOMING_QUERY_COMMAND_MODIFY.getCommandString() + item.getId() + "/" + listId);
        } else {
            button.setText(icon + item.getItemName() +
                    localizationManager.getResource("spaces", Languages.ENGLISH));

            button.setCallbackData(INCOMING_QUERY_TYPE_LIST.getCommandString() + (item.isFinished() ?
                    INCOMING_QUERY_COMMAND_REDO.getCommandString() : INCOMING_QUERY_COMMAND_FINISH.
                    getCommandString()) + item.getId() + "/" + listId);
        }
        row.add(button);

        return row;
    }

    private void showLanguageSelect(User user, Message message, String callbackCommand, boolean isIntro) {

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("language_short", Languages.ENGLISH));
        button.setCallbackData(callbackCommand + INCOMING_QUERY_COMMAND_EN.getCommandString());
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("language_short", Languages.RUSSIAN));
        button.setCallbackData(callbackCommand + INCOMING_QUERY_COMMAND_RU.getCommandString());
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("language_short", Languages.LITHUANIAN));
        button.setCallbackData(callbackCommand + INCOMING_QUERY_COMMAND_LT.getCommandString());
        row.add(button);

        if (!isIntro) {
            button = new InlineKeyboardButton();
            button.setText(localizationManager.getResource("cancel", user.getLanguage()));
            button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_CANCEL.getCommandString());
            row.add(button);
        }

        rows.add(row);

        rk.setKeyboard(rows);

        String messageText = localizationManager.getResource("select_language", Languages.ENGLISH);
        if (!isIntro) {
            editMessage(message.getChatId(), message.getMessageId(), messageText, rk);
        } else {
            sendPlainMessage(message.getChatId(), messageText, rk);
        }
    }

    private void showRename(User user, Message message, String item) {

        user.setLastIncomingQueryCommand(INCOMING_QUERY_COMMAND_RENAME.getCommandString());
        mongo.updateUser(user);

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("cancel", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_CANCEL.getCommandString());
        row.add(button);

        rows.add(row);

        rk.setKeyboard(rows);

        String messageText = localizationManager.getResource("input_new_name", Languages.ENGLISH) + item;
        messageText += " " + user.getDelimiter() + " )";
        editMessage(message.getChatId(), message.getMessageId(), messageText, rk);
    }

    private void showDelimiterSelect(User user, Message message) {

        user.setLastIncomingQueryCommand(INCOMING_QUERY_COMMAND_TEXT_DELIMITER.getCommandString());
        mongo.updateUser(user);

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("cancel", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_CANCEL.getCommandString());
        row.add(button);

        rows.add(row);

        rk.setKeyboard(rows);

        String messageText = localizationManager.getResource("input_new_delimiter", Languages.ENGLISH);
        messageText += " " + user.getDelimiter() + " )";
        editMessage(message.getChatId(), message.getMessageId(), messageText, rk);
    }

    private void setDelimiter(Message message, User user) {
        user.setDelimiter(message.getText());
        user.setLastIncomingQueryCommand("");
        mongo.updateUser(user);
        showSettings(message, user, false);
    }

    private void showWelcomeMessage(User user, Message message) {
        editMessageText(message.getChatId(), message.getMessageId(), localizationManager.getResource("welcomeMessage", user.getLanguage()));

        showList(message, user, false);
    }

    private void sendPlainMessage(Long chatId, String expression) {
        sendPlainMessage(chatId, expression, null);
    }

    private void sendList(Long chatId, Integer messageId, String messageText, InlineKeyboardMarkup rk) {
        sendPlainMessage(chatId, "list_is_loading", null);

        editMessage(chatId, messageId, messageText, rk);
    }

    private void sendPlainMessage(Long chatId, String expression,
                                  InlineKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId.toString());
        sendMessage.setReplayMarkup(replyKeyboardMarkup);
        sendMessage.setText(expression);

        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }

    private void editMessage(Long chatId, Integer messageId, String messageText, InlineKeyboardMarkup rk) {
        if (messageText != null) {
            editMessageText(chatId, messageId, messageText);
        }
        if (rk != null) {
            editMessageReplyMarkup(chatId, messageId, rk);
        }
    }

    private void editMessageText(Long chatId, Integer messageId, String messageText) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId.toString());
        editMessageText.setMessageId(messageId);
        editMessageText.setText(messageText);

        try {
            editMessageText(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editMessageReplyMarkup(Long chatId, Integer messageId, InlineKeyboardMarkup rk) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId.toString());
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(rk);

        try {
            editMessageReplyMarkup(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}