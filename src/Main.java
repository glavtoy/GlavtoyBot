import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import task.BussinessTask;
import user.UserBase;
import util.SQLUtil;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws SQLException {
        SQLUtil sqlUtil = new SQLUtil("jdbc:postgresql://95.216.29.61/postgres", "postgres", "51699651Ng$T");
        UserBase.getInstance().load(sqlUtil);
        try {
            TelegramBot bot = new TelegramBot("glavtoybot", "5954808309:AAFibSVX0u9GTMxu1svOi5amTHSszLZF6Wk");
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            new BussinessTask().startTask(TimeUnit.HOURS, 1);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}