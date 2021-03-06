package lt.lyre.accomplishbot;

import lt.lyre.accomplishbot.commands.IncomingQueryCommand;
import lt.lyre.accomplishbot.models.User;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lt.lyre.accomplishbot.configuration.BotConfig;
import lt.lyre.accomplishbot.localization.Languages;
import lt.lyre.accomplishbot.localization.LocalizationManager;

import static lt.lyre.accomplishbot.commands.IncomingQueryCommand.INCOMING_COMMAND_CLOSE;
import static lt.lyre.accomplishbot.commands.IncomingQueryCommand.INCOMING_COMMAND_OPEN;

/**
 * Created by Dmitrij on 2016-06-18.
 */
public class BotHandler extends TelegramLongPollingBot {
    private static final String BOT_LOG_TAG = BotConfig.BOT_USERNAME + "_Log_Tag";

    private LocalizationManager localizationManager;
    private MongoDbHandler mongo;
    private volatile CloseableHttpClient httpclient;

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
                String[] data = query.getData().split("/");

                IncomingQueryCommand command = data.length > 0 ? IncomingQueryCommand.getByCommandString(data[0] + "/") : null;
                switch (command) {
                    case INCOMING_COMMAND_OPEN:
                        showDorControls(query.getMessage(), false);
                        mongo.setDoorState(new ObjectId(), false);
                        break;
                    case INCOMING_COMMAND_CLOSE:
                        showDorControls(query.getMessage(), true);
                        mongo.setDoorState(new ObjectId(), true);
                        break;
                }
            }
        } catch (Exception e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }


    private void handleIncomingMessage(Message message) throws InvalidObjectException {
        showDorControls(message, false);
    }

    private void showDorControls(Message message, boolean isClosed) {
        InlineKeyboardMarkup rk = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        InlineKeyboardButton button;
        row = new ArrayList<>();

        String messageText = "Door is ";
        button = new InlineKeyboardButton();
        if (isClosed) {
            messageText = messageText.concat("Closed. ");
            button.setText("Open");
            button.setCallbackData(INCOMING_COMMAND_OPEN.getCommandString());
            row.add(button);
        } else {
            messageText = messageText.concat("Open. ");
            button = new InlineKeyboardButton();
            button.setText("Close");
            button.setCallbackData(INCOMING_COMMAND_CLOSE.getCommandString());
            row.add(button);
        }
        rows.add(row);

        rk.setKeyboard(rows);

        messageText = messageText.concat("Update Door State:");

        sendPlainMessage(message.getChatId(), messageText, rk);
        for (User user : mongo.getOpenUsersChatId()) {
            if (user.getUserPreferences() != null && user.getUserPreferences().isShowNotificationsDoor()) {
                preparePrivatePlainMessage(user.getTelegramId(), (isClosed ? "The Door Wsa Closed" : "The Door Was Open"), null);
            }
        }
    }

    private void showWelcomeMessage(Message message) {
        editMessageText(message.getChatId(), message.getMessageId(), "Welcome");

        showDorControls(message, false);
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


    private void preparePrivatePlainMessage(Long chatId, String expression,
                                            InlineKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId.toString());
        sendMessage.setReplayMarkup(replyKeyboardMarkup);
        sendMessage.setText(expression);

        try {
            sendPrivateMessage(sendMessage);
        } catch (TelegramApiException e) {
            BotLogger.error(BOT_LOG_TAG, e);
        }
    }

    private Message sendPrivateMessage(SendMessage sendMessage) throws TelegramApiException {
        if (sendMessage == null) {
            throw new TelegramApiException("Parameter sendMessage can not be null");
        } else {
            return (Message) this.sendApiMethod(sendMessage);
        }
    }

    private <T extends Serializable> T sendApiMethod(BotApiMethod<T> method) throws TelegramApiException {
        String responseContent;
        try {

            this.httpclient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(70L, TimeUnit.SECONDS).setMaxConnTotal(100).build();
            String jsonObject = "https://api.telegram.org/bot" + "196800784:AAFYLMFGGdqzNSq6yZFwOWTarThnIbEOgaU" + "/" + "sendmessage";
            HttpPost httppost = new HttpPost(jsonObject);
            RequestConfig.Builder configBuilder = RequestConfig.copy(RequestConfig.custom().build());
            httppost.setConfig(configBuilder.setSocketTimeout(75000).setConnectTimeout(75000).setConnectionRequestTimeout(75000).build());
            httppost.addHeader("charset", StandardCharsets.UTF_8.name());
            httppost.setEntity(new StringEntity(method.toJson().toString(), ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = this.httpclient.execute(httppost);
            Throwable var6 = null;

            try {
                HttpEntity ht = response.getEntity();
                BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                responseContent = EntityUtils.toString(buf, StandardCharsets.UTF_8);
            } catch (Throwable var17) {
                var6 = var17;
                throw var17;
            } finally {
                if (response != null) {
                    if (var6 != null) {
                        try {
                            response.close();
                        } catch (Throwable var16) {
                            var6.addSuppressed(var16);
                        }
                    } else {
                        response.close();
                    }
                }

            }
        } catch (IOException var19) {
            throw new TelegramApiException("Unable to execute " + method.getPath() + " method", var19);
        }

        JSONObject jsonObject1 = new JSONObject(responseContent);
        if (!jsonObject1.getBoolean("ok")) {
            throw new TelegramApiException("Error at " + method.getPath(), jsonObject1.getString("description"), Integer.valueOf(jsonObject1.getInt("error_code")));
        } else {
            return method.deserializeResponse(jsonObject1);
        }
    }


}