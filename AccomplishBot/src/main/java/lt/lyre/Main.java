package lt.lyre;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;

/**
 * Created by Dmitrij on 2016-06-18.
 */
public class Main {

    public static void main(String[] args) {

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new CalculatorBotHandler());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
