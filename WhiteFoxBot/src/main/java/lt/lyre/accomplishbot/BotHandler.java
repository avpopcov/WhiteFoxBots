package lt.lyre.accomplishbot;

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.commands.IncomingQueryCommand;
import lt.lyre.accomplishbot.commands.IncomingQueryType;
import lt.lyre.accomplishbot.models.Door;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.utils.UserCommandParser;
import lt.lyre.accomplishbot.utils.models.ParsedUserCommand;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import lt.lyre.accomplishbot.configuration.BotConfig;
import lt.lyre.accomplishbot.localization.Languages;
import lt.lyre.accomplishbot.localization.LocalizationManager;

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
            case INCOMING_QUERY_TYPE_MENU:
                manageMenuSelection(command, query.getMessage());
                break;
            case INCOMING_QUERY_TYPE_LANGUAGE:
                manageLanguageSelection(command, query.getMessage());
                break;
            case INCOMING_QUERY_TYPE_SETTINGS:
                manageSettingsSelection(command, query.getMessage());
                break;
        }
    }

    private void manageMenuSelection(IncomingQueryCommand command, Message message) {
        User user = mongo.getUserByTelegramId(message.getChat().getId());

        Long telegramId = message.getChat().getId();

        switch (command) {
            case INCOMING_QUERY_COMMAND_GET_DOOR_STATE:
                showDoorState(message, user);
                break;
            case INCOMING_QUERY_COMMAND_DOOR_CONTROLS:
                showMenu(message, user, INCOMING_QUERY_COMMAND_DOOR_CONTROLS);
                break;
            case INCOMING_QUERY_COMMAND_BACK:
                showMenu(message, user, INCOMING_QUERY_COMMAND_MAIN_MENU);
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
            case INCOMING_QUERY_COMMAND_ADD_NEW_USER:
                setNewTrustedUser(user, message);
                break;

            case INCOMING_QUERY_COMMAND_CANCEL:
                user.setLastCommand("");
                user.setLastIncomingQueryCommand("");
                mongo.updateUser(user);
                showMenu(message, user, INCOMING_QUERY_COMMAND_MAIN_MENU);
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

    private void showDoorState(Message message, User user) {
        Door door = mongo.getCurrentDoorState();


        sendPlainMessage(message.getChatId(), door.isClosed()? "Closed": "Open");
    }

    private void showSettings(Message message, User user, boolean reply) {

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        if (user.isAdmin()) {
            button = new InlineKeyboardButton();
            button.setText("Add New Trusted User");
            button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_ADD_NEW_USER.getCommandString());
            row.add(button);
        }

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("language", user.getLanguage()) +
                " (" + localizationManager.getResource("language_short", user.getLanguage()) + ")");
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_LANGUAGE.getCommandString());
        row.add(button);

        button = new InlineKeyboardButton();
        button.setText(localizationManager.getResource("cancel", user.getLanguage()));
        button.setCallbackData(INCOMING_QUERY_TYPE_SETTINGS.getCommandString() + INCOMING_QUERY_COMMAND_CANCEL.getCommandString());
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

    private void handleIncomingMessage(Message message) throws InvalidObjectException {
        User user = mongo.getUserByTelegramId(message.getFrom().getId());
        if (user == null) {
            user = mongo.getUserByUserName(message.getFrom().getUserName());
            if (user != null) {
                user.setTelegramId(message.getFrom().getId());
                user.setFirstName(message.getFrom().getFirstName());
                user.setLastCommand(message.getText());

                mongo.insertUser(user);

                user = mongo.getUserByTelegramId(message.getFrom().getId());
            } else {
                sendPlainMessage(message.getChatId(), localizationManager.getResource("access_denied", Languages.ENGLISH));
            }
        }

        ParsedUserCommand parsedUserCommand = UserCommandParser.parseUserInput(message.getText(), user);
        BotLogger.debug(BOT_LOG_TAG, "Parsed user command: \n" + parsedUserCommand);
        BotCommands command = BotCommands.getByCommandString(parsedUserCommand.getUserCommand());

        if (command == null) {
            command = BotCommands.getByCommandString(user.getLastCommand());
        }

        user.setLastCommand(command.getCommandString());
        mongo.updateUser(user);

        if (command != null) {

            switch (command) {
                case CMD_SETTINGS:
                    if (user.getLastIncomingQueryCommand() != null &&
                            user.getLastIncomingQueryCommand().equals(INCOMING_QUERY_COMMAND_ADD_NEW_USER.getCommandString())) {
                        setNewTrustedUser(message, user);
                    } else {
                        showSettings(message, user, false);
                    }
                    break;
                case CMD_START:
                    showLanguageSelect(user, message, INCOMING_QUERY_TYPE_LANGUAGE.getCommandString(), true);
                    mongo.updateUser(user);
                    break;
            }
        }
    }

    private void showMenu(Message message, User user, IncomingQueryCommand command) {

        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        switch (command) {
            case INCOMING_QUERY_COMMAND_MAIN_MENU:
                button = new InlineKeyboardButton();
                button.setText("Door");
                button.setCallbackData(INCOMING_QUERY_TYPE_MENU.getCommandString() + INCOMING_QUERY_COMMAND_DOOR_CONTROLS.getCommandString());
                row.add(button);
                break;
            case INCOMING_QUERY_COMMAND_DOOR_CONTROLS:
                button = new InlineKeyboardButton();
                button.setText("Get Door State");
                button.setCallbackData(INCOMING_QUERY_TYPE_MENU.getCommandString() + INCOMING_QUERY_COMMAND_GET_DOOR_STATE.getCommandString());
                row.add(button);
                rows.add(row);
                button = new InlineKeyboardButton();
                row = new ArrayList<>();
                button.setText("Back");
                button.setCallbackData(INCOMING_QUERY_TYPE_MENU.getCommandString() + INCOMING_QUERY_COMMAND_BACK.getCommandString());
                row.add(button);
                break;
        }
            rows.add(row);

            rk.setKeyboard(rows);

            String messageText = "Menu";

            editMessage(message.getChatId(), message.getMessageId(), messageText, rk);
        }

    private void setNewTrustedUser(User user, Message message) {

        user.setLastIncomingQueryCommand(INCOMING_QUERY_COMMAND_ADD_NEW_USER.getCommandString());
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

        String messageText = localizationManager.getResource("input_username", Languages.ENGLISH);
        editMessage(message.getChatId(), message.getMessageId(), messageText, rk);
    }

    private void setNewTrustedUser(Message message, User user) {
        mongo.setNewTrustedUser(message.getText());
        user.setLastIncomingQueryCommand("");
        user.setLastCommand("");
        mongo.updateUser(user);
        sendPlainMessage(message.getChatId(), "New User Added");

    }

    private void showWelcomeMessage(User user, Message message) {
        editMessageText(message.getChatId(), message.getMessageId(), localizationManager.getResource("welcomeMessage", user.getLanguage()));
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

    private void showWelcomeMessage(Message message) {
        editMessageText(message.getChatId(), message.getMessageId(), "Welcome");
    }

    private void sendPlainMessage(Long chatId, String expression) {
        sendPlainMessage(chatId, expression, null);
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