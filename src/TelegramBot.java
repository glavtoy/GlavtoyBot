import casino.Casino;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import user.User;
import user.UserBase;
import util.HashMapSorterUtil;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TelegramBot extends TelegramLongPollingBot {

    private String botUserName, botToken;
    private Executor executor = Executors.newCachedThreadPool();

    public TelegramBot(String botUserName, String botToken) {
        this.botUserName = botUserName;
        this.botToken = botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (message.getChat().getUserName() != null && !message.getChat().getUserName().equalsIgnoreCase("")) {

            executor.execute(() -> {

                User user = null;
                try {
                    user = new User(update.getMessage().getChat().getUserName());
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                if (update.hasMessage() && message.hasText()) {
                    String messageText = message.getText();
                    long chatId = update.getMessage().getChatId();
                    if (user.getId() == 0) {
                        user.setId(chatId);
                    }
                    try {
                        if (!UserBase.getInstance().containsUser(user)) {
                            UserBase.getInstance().addUserInDB(user);
                            UserBase.getInstance().reload();
                            System.out.println(UserBase.getInstance().getUsersBase());
                        } else {
                            user = UserBase.getInstance().getUserByName(user.getTelegramUserName());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    if (messageText.startsWith("/pay")) {
                        String[] args = messageText.split(" ");
                        if (args.length == 3) {
                            String target = args[1].replace("@", "");
                            try {
                                int amount = Integer.parseInt(args[2]);
                                User targetUser = UserBase.getInstance().getUserByName(target);
                                if (targetUser != null) {
                                    if (!user.getTelegramUserName().equalsIgnoreCase(target)) {
                                        if (user.getBalance() >= amount && amount > 0) {
                                            if (targetUser.getBalance() + amount < Integer.MAX_VALUE) {
                                                user.withdrawBalance(amount);
                                                targetUser.depositBalance(amount);
                                                sendMessage(user.getId(), "Монеты успешно переведены! ✅");
                                                sendMessage(targetUser.getId(), "Вы получили $" + amount + " от " + user.getTelegramUserName() + "! ✅");
                                                Random random = new Random();
                                                if (random.nextInt(101) <= 15 || random.nextInt(101) >= 85) {
                                                    user.addLevelExp(10);
                                                    user.updateLevel();
                                                }
                                            } else {
                                                sendMessage(user.getId(), "Превышен лимит баланса у " + target + "! ❌");
                                            }
                                        } else {
                                            sendMessage(user.getId(), "Недостаточно монет! ❌");
                                        }
                                    } else {
                                        sendMessage(user.getId(), "Вы не можете отправить монеты самому себе! ❌");
                                    }
                                } else {
                                    sendMessage(user.getId(), "Пользователь " + target + " отсутствует в базе! ❌");
                                }
                            } catch (NumberFormatException e) {
                                sendMessage(chatId, "Для перевода монет другому пользователю, используйте команду: /pay (имя) (сумма) \uD83D\uDCCC");
                            } catch (SQLException | NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            sendMessage(chatId, "Для перевода монет другому пользователю, используйте команду: /pay (имя) (сумма) \uD83D\uDCCC");
                        }
                    } else if (messageText.startsWith("/profile")) {
                        String[] args = messageText.split(" ");
                        if (args.length == 2) {
                            String target = args[1].replace("@", "");
                            try {
                                User targetUser = UserBase.getInstance().getUserByName(target);
                                if (targetUser != null) {
                                    int pos1 = UserBase.getInstance().getMoneyLeaderboardPosition(targetUser);
                                    int pos2 = UserBase.getInstance().getLevelLeaderboardPosition(targetUser);
                                    String pos1s, pos2s;
                                    if (pos1 == 1) {
                                        pos1s = "\uD83D\uDE80 Топ богачей: \uD83E\uDD47";
                                    } else if (pos1 == 2) {
                                        pos1s = "\uD83D\uDE80 Топ богачей: \uD83E\uDD48";
                                    } else if (pos1 == 3) {
                                        pos1s = "\uD83D\uDE80 Топ богачей: \uD83E\uDD49";
                                    } else {
                                        pos1s = "\uD83D\uDE80 Топ богачей: #" + pos1;
                                    }
                                    if (pos2 == 1) {
                                        pos2s = "\uD83D\uDE80 Топ уровня: \uD83E\uDD47";
                                    } else if (pos2 == 2) {
                                        pos2s = "\uD83D\uDE80 Топ уровня: \uD83E\uDD48";
                                    } else if (pos2 == 3) {
                                        pos2s = "\uD83D\uDE80 Топ уровня: \uD83E\uDD49";
                                    } else {
                                        pos2s = "\uD83D\uDE80 Топ уровня: #" + pos2;
                                    }
                                    sendMessage(user.getId(), "\uD83D\uDCAC Имя: @" + targetUser.getTelegramUserName() + "\n" + "\uD83D\uDCB5 Баланс: $" + targetUser.getBalance() + "\n" + "\uD83D\uDD30 Уровень: " + targetUser.getLevel() +
                                            "\n" + pos1s + "\n" + pos2s);
                                } else {
                                    sendMessage(user.getId(), "Пользователь " + target + " отсутствует в базе! ❌");
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            sendMessage(user.getId(), "Чтобы посмотреть профиль другого пользователя, используйте команду /profile (имя) \uD83D\uDCCC");
                        }
                    } else if (messageText.startsWith("/dice")) {
                        String[] args = messageText.split(" ");
                        if (args.length == 2) {
                            try {
                                int bet = Integer.parseInt(args[1]);
                                if (user.getBalance() >= bet) {
                                    if (bet > 0 && bet <= 10000000) {
                                        int ct = Casino.getInstance().generateGameResult();
                                        user.withdrawBalance(bet);
                                        if (ct != 0) {
                                            int win = bet * ct;
                                            if (user.getBalance() + win < Integer.MAX_VALUE) {
                                                user.depositBalance(win);
                                                sendMessage(user.getId(), "Вы выиграли $" + (bet * ct) + " (x" + ct + ")! ✅");
                                                user.addLevelExp(20);
                                                user.updateLevel();
                                            } else {
                                                sendMessage(user.getId(), "Выигрыш превышает лимит баланса! ❌");
                                            }
                                        } else {
                                            sendMessage(user.getId(), "Вы проиграли! ❌");
                                        }
                                    } else {
                                        sendMessage(user.getId(), "Ставка должна быть от $1 до $10000000! ❌");
                                    }
                                } else {
                                    sendMessage(user.getId(), "Недостаточно монет! ❌");
                                }
                            } catch (NumberFormatException e) {
                                sendMessage(user.getId(), "Сумма ставки должна быть числом! ❌");
                            } catch (SQLException | NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            sendMessage(user.getId(), "Чтобы сделать ставку, используйте команду: /dice (сумма) \uD83D\uDCCC");
                        }
                    } else {
                        switch (messageText) {
                            case "/start", "\uD83D\uDCBC Главное меню":
                                sendMainMenuMessage(user.getId());
                                break;
                            case "\uD83D\uDCB3 Баланс":
                                sendMessage(user.getId(), "\uD83D\uDCB5 Ваш баланс: $" + user.getBalance());
                                break;
                            case "\uD83D\uDCB8 Заработок":
                                sendEarnsMenu(user.getId());
                                break;
                            case "\uD83C\uDFE2 Бизнес":
                                sendBussinessEarnMenu(user.getId());
                                break;
                            case "⭐ Топы лучших":
                                sendLeaderboardsMenu(user.getId());
                                break;
                            case "\uD83C\uDFB0 Казино":
                                sendMessage(user.getId(), "Чтобы сделать ставку, используйте команду: /dice (сумма) \uD83D\uDCCC");
                                break;
                            case "\uD83C\uDFE7 Переводы":
                                sendMessage(user.getId(), "Для перевода монет другому пользователю, используйте команду: /pay (имя) (сумма) \uD83D\uDCCC");
                                break;
                            case "\uD83D\uDCB3 Прибыль":
                                sendMessage(user.getId(), "\uD83D\uDCB5 Ваша прибыль: $" + user.getBussinessIncome());
                                break;
                            case "\uD83D\uDD30 Уровень":
                                if (user.getBussinessLevel() < 25) {
                                    sendMessage(user.getId(), "\uD83D\uDD30 Уровень бизнеса: " + user.getBussinessLevel());
                                } else {
                                    sendMessage(user.getId(), "\uD83D\uDD30 Уровень бизнеса: " + user.getBussinessLevel() + " (МАКС)");
                                }
                                break;
                            case "\uD83D\uDD27 Улучшить":
                                int upgradeCost = user.getBussinessUpgrade();
                                if (user.getBussinessLevel() < 25) {
                                    if (user.getBalance() >= upgradeCost) {
                                        try {
                                            user.withdrawBalance(upgradeCost);
                                            user.setBussinessLevel(user.getBussinessLevel() + 1);
                                            user.setBussinessUpgrade(user.getBussinessUpgrade() * 2);
                                            sendMessage(user.getId(), "Вы улучшили бизнес до " + user.getBussinessLevel() + " уровня! ✅");
                                        } catch (SQLException | NoSuchAlgorithmException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        sendMessage(user.getId(), "Для улучшения вам не хватает $" + (user.getBussinessUpgrade() - user.getBalance()) + "! ❌");
                                    }
                                } else {
                                    sendMessage(user.getId(), "Вы достигли максимального уровня бизнеса! ❌");
                                }
                                break;
                            case "\uD83D\uDD25 Забрать прибыль":
                                int income = user.getBussinessIncome();
                                user.setBussinessIncome(0);
                                try {
                                    if (user.getBalance() + income < Integer.MAX_VALUE) {
                                        user.depositBalance(income);
                                        sendMessage(user.getId(), "Вы получили прибыль от бизнеса в размере $" + income + " ✅");
                                    } else {
                                        sendMessage(user.getId(), "Полученная прибыль превышает лимит баланса! ❌");
                                    }
                                } catch (SQLException | NoSuchAlgorithmException e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "\uD83D\uDCC4 Профиль":
                                int pos1 = UserBase.getInstance().getMoneyLeaderboardPosition(user);
                                int pos2 = UserBase.getInstance().getLevelLeaderboardPosition(user);
                                String pos1s, pos2s;
                                if (pos1 == 1) {
                                    pos1s = "\uD83D\uDE80 Топ богачей: \uD83E\uDD47";
                                } else if (pos1 == 2) {
                                    pos1s = "\uD83D\uDE80 Топ богачей: \uD83E\uDD48";
                                } else if (pos1 == 3) {
                                    pos1s = "\uD83D\uDE80 Топ богачей: \uD83E\uDD49";
                                } else {
                                    pos1s = "\uD83D\uDE80 Топ богачей: #" + pos1;
                                }
                                if (pos2 == 1) {
                                    pos2s = "\uD83D\uDE80 Топ уровня: \uD83E\uDD47";
                                } else if (pos2 == 2) {
                                    pos2s = "\uD83D\uDE80 Топ уровня: \uD83E\uDD48";
                                } else if (pos2 == 3) {
                                    pos2s = "\uD83D\uDE80 Топ уровня: \uD83E\uDD49";
                                } else {
                                    pos2s = "\uD83D\uDE80 Топ уровня: #" + pos2;
                                }
                                sendMessage(user.getId(), "\uD83D\uDCAC Ваше имя: @" + user.getTelegramUserName() + "\n" + "\uD83D\uDCB5 Баланс: $" + user.getBalance() + "\n" + "\uD83D\uDD30 Уровень: " + user.getLevel() +
                                        "\n" + pos1s + "\n" + pos2s);
                                sendMessage(user.getId(), "Чтобы посмотреть профиль другого пользователя, используйте команду /profile (имя) \uD83D\uDCCC");
                                break;
                            case "\uD83D\uDCB5 Топ богачей":
                                user.updateInBase();
                                try {
                                    UserBase.getInstance().updateLeaderboards();
                                } catch (SQLException | NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                                sendMoneyLeaderList(user.getId());
                                break;
                            case "\uD83D\uDD30 Топ уровня":
                                user.updateInBase();
                                try {
                                    UserBase.getInstance().updateLeaderboards();
                                } catch (SQLException | NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                }
                                sendLevelLeaderList(user.getId());
                                break;
                            default:
                                sendMessage(user.getId(), "Неизвестная команда! ❌");
                        }
                    }
                }
            });
        } else {
            sendMessage(message.getChatId(), "Для работы с ботом вы должны иметь никнейм! ❌");
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendMessage(Long chatId, String text) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);
        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {}
    }

    private void sendMoneyLeaderList(Long chatId) {
        HashMap<User, Integer> leaderList = HashMapSorterUtil.sort(UserBase.getInstance().getMoneyLeaderList());
        Iterator<Map.Entry<User, Integer>> iterator = leaderList.entrySet().iterator();
        int count = 1;
        String s1 = "", s2 = "", s3 = "";
        while (iterator.hasNext() && count < 4) {
            Map.Entry<User, Integer> entry = iterator.next();
            if (count == 1) {
                s1 = "\uD83E\uDD47 @" + entry.getKey().getTelegramUserName() + ": $" + entry.getKey().getBalance();
            } else if (count == 2) {
                s2 = "\uD83E\uDD48 @" + entry.getKey().getTelegramUserName() + ": $" + entry.getKey().getBalance();
            } else if (count == 3) {
                s3 = "\uD83E\uDD49 @" + entry.getKey().getTelegramUserName() + ": $" + entry.getKey().getBalance();
            }
            count++;
        }
        sendMessage(chatId, s1 + "\n" + s2 + "\n" + s3);
    }

    private void sendLevelLeaderList(Long chatId) {
        HashMap<User, Integer> leaderList = HashMapSorterUtil.sort(UserBase.getInstance().getLevelLeaderList());
        Iterator<Map.Entry<User, Integer>> iterator = leaderList.entrySet().iterator();
        int count = 1;
        String s1 = "", s2 = "", s3 = "";
        while (iterator.hasNext() && count < 4) {
            Map.Entry<User, Integer> entry = iterator.next();
            if (count == 1) {
                s1 = "\uD83E\uDD47 @" + entry.getKey().getTelegramUserName() + ": " + entry.getValue();
            } else if (count == 2) {
                s2 = "\uD83E\uDD48 @" + entry.getKey().getTelegramUserName() + ": " + entry.getValue();
            } else if (count == 3) {
                s3 = "\uD83E\uDD49 @" + entry.getKey().getTelegramUserName() + ": " + entry.getValue();
            }
            count++;
        }
        sendMessage(chatId, s1 + "\n" + s2 + "\n" + s3);
    }

    public void sendMainMenuMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Выберите действие:");
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText("\uD83D\uDCB3 Баланс");
        KeyboardButton keyboardButton2 = new KeyboardButton();
        keyboardButton2.setText("\uD83D\uDCB8 Заработок");
        KeyboardButton keyboardButton3 = new KeyboardButton();
        keyboardButton3.setText("\uD83C\uDFE7 Переводы");
        KeyboardButton keyboardButton4 = new KeyboardButton();
        keyboardButton4.setText("⭐ Топы лучших");
        KeyboardButton keyboardButton5 = new KeyboardButton();
        keyboardButton5.setText("\uD83D\uDCC4 Профиль");
        keyboardRow1.add(keyboardButton1);
        keyboardRow1.add(keyboardButton2);
        keyboardRow2.add(keyboardButton3);
        keyboardRow2.add(keyboardButton4);
        keyboardRow3.add(keyboardButton5);
        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        keyboardRowList.add(keyboardRow3);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {}
    }

    public void sendEarnsMenu(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Выберите заработок:");
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText("\uD83C\uDFE2 Бизнес");
        KeyboardButton keyboardButton2 = new KeyboardButton();
        keyboardButton2.setText("\uD83C\uDFB0 Казино");
        KeyboardButton keyboardButton3 = new KeyboardButton();
        keyboardButton3.setText("\uD83D\uDCBC Главное меню");
        keyboardRow1.add(keyboardButton1);
        keyboardRow1.add(keyboardButton2);
        keyboardRow2.add(keyboardButton3);
        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {}
    }

    public void sendLeaderboardsMenu(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Выберите вид таблицы:");
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText("\uD83D\uDCB5 Топ богачей");
        KeyboardButton keyboardButton2 = new KeyboardButton();
        keyboardButton2.setText("\uD83D\uDD30 Топ уровня");
        KeyboardButton keyboardButton3 = new KeyboardButton();
        keyboardButton3.setText("\uD83D\uDCBC Главное меню");
        keyboardRow1.add(keyboardButton1);
        keyboardRow1.add(keyboardButton2);
        keyboardRow2.add(keyboardButton3);
        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {}
    }

    public void sendBussinessEarnMenu(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Выберите действие:");
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardRow keyboardRow4 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText("\uD83D\uDD25 Забрать прибыль");
        KeyboardButton keyboardButton2 = new KeyboardButton();
        keyboardButton2.setText("\uD83D\uDCB3 Прибыль");
        KeyboardButton keyboardButton3 = new KeyboardButton();
        keyboardButton3.setText("\uD83D\uDD27 Улучшить");
        KeyboardButton keyboardButton4 = new KeyboardButton();
        keyboardButton4.setText("\uD83D\uDD30 Уровень");
        KeyboardButton keyboardButton5 = new KeyboardButton();
        keyboardButton5.setText("\uD83D\uDCBC Главное меню");
        keyboardRow1.add(keyboardButton2);
        keyboardRow1.add(keyboardButton4);
        keyboardRow2.add(keyboardButton3);
        keyboardRow3.add(keyboardButton1);
        keyboardRow4.add(keyboardButton5);
        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        keyboardRowList.add(keyboardRow3);
        keyboardRowList.add(keyboardRow4);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            executeAsync(sendMessage);
        } catch (TelegramApiException e) {}
    }
}
