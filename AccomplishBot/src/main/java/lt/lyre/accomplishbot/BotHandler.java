package lt.lyre.accomplishbot;

import lt.lyre.accomplishbot.configuration.BotConfig;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.models.UserList;
import lt.lyre.accomplishbot.utils.StringHelper;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.logging.BotLogger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dmitrij on 2016-06-18.
 */
public class BotHandler extends TelegramLongPollingBot {
    private static final String BOT_LOG_TAG = BotConfig.BOT_USERNAME + "_Log_Tag";
    private static final String WELCOME_MESSAGE = "Welcome to sample Lyre Calc Bot! Please input mathematical expression to keep you going.";

    private final ArrayList<String> acceptableCommands = new ArrayList<String>() {{
        add("/start");
        add("/list");
        add("/add");
        add("/finish");
    }};

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

        if (StringHelper.containsCommandPrefix(acceptableCommands, message.getText())) {
            mongo.logLastCommand(user, message.getText());
        }

        if (message.getText().startsWith("/start")) {
            sendWelcomeMessage(message.getChatId().toString(), message.getMessageId(), null);
        } else if (message.getText().startsWith("/add")) {
            String delimiter = user.getDelimiter();
            String text = message.getText();
            String withoutCommand = "";

            if (text.length() > 4) {
                withoutCommand = text.substring(5, text.length());
            }

            List<String> listItem = Arrays.stream(withoutCommand.split(delimiter)).filter(item -> !item.isEmpty()).collect(Collectors.toList());

            mongo.insertListItem("test", listItem, message.getFrom().getId());

            String resultMessage;

            if (listItem.size() > 1) {
                resultMessage = String.format("Items: %s were added to the list.", listItem.stream().collect(Collectors.joining(", ")));
            } else if (listItem.size() == 1) {
                resultMessage = String.format("Item %s was added to the list.", listItem.stream().findFirst().get());
            } else {
                resultMessage = "No items were added";
            }

            sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
        } else if (message.getText().startsWith("/list")) {
            List<UserList> result = mongo.getUserListByTelegramId(message.getFrom().getId());

            String messageText = "";
            if (result == null || result.isEmpty()) {
                messageText = String.format("Your lists are empty.");
            } else {
                messageText = result.stream().findAny().get().getItems().stream().map(item -> item.getItemName().toString()).collect(Collectors.joining(", "));
            }

            sendPlainMessage(message.getChatId().toString(), message.getMessageId(), messageText);
        }
        else if (message.getText().startsWith("/finish")) {
            String text = message.getText();
            String itemToFinish = "";

            if (text.length() > 7) {
                itemToFinish = text.substring(8, text.length());
            }

            boolean isSuccessful = mongo.finishListItem(itemToFinish, message.getFrom().getId());
            String resultMessage;

            if (isSuccessful) {
                resultMessage = String.format("Items: %s was successfully finished", itemToFinish);

            } else {
                resultMessage = String.format("Item %s was not found", itemToFinish);
            }

            sendPlainMessage(message.getChatId().toString(), message.getMessageId(), resultMessage);
        }
        else {
            sendMessage(message.getChatId().toString(), message.getMessageId(), message.getText());
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

    private void sendMessage(String chatId, Integer messageId, String expression) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setReplayToMessageId(messageId);

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");

        try {
            Object result = engine.eval(expression);

            if (result != null) {
                sendMessage.setText(String.valueOf(engine.eval(expression)));
            } else {
                sendMessage.setText(expression);
            }

        } catch (ScriptException e) {
            e.printStackTrace();
            sendMessage.setText("Did you say: " + expression + "?. Try again.");
        }

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