package user;

import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;

public class User {

    private String telegramUserName;
    private Long id;
    private int balance, level, levelExp, bussinessIncome, bussinessUpgrade, bussinessLevel;

    public User(Long id, String telegramUserName, int balance, int level, int bussinessIncome, int bussinessUpgrade, int bussinessLevel, int levelExp) {
        this.telegramUserName = telegramUserName;
        this.balance = balance;
        this.id = id;
        this.level = level;
        this.bussinessIncome = bussinessIncome;
        this.bussinessUpgrade = bussinessUpgrade;
        this.bussinessLevel = bussinessLevel;
        this.levelExp = levelExp;
    }

    public User(Long id, String telegramUserName, int balance, int level, int bussinessIncome, int bussinessUpgrade, int bussinessLevel) {
        this.telegramUserName = telegramUserName;
        this.balance = balance;
        this.id = id;
        this.level = level;
        this.bussinessIncome = bussinessIncome;
        this.bussinessUpgrade = bussinessUpgrade;
        this.bussinessLevel = bussinessLevel;
    }

    public User(Long id, String telegramUserName, int balance, int level) {
        this.telegramUserName = telegramUserName;
        this.balance = balance;
        this.id = id;
        this.level = level;
        this.bussinessIncome = 50;
        this.bussinessUpgrade = 5000;
        this.bussinessLevel = 1;
    }

    public User(Long id, String telegramUserName, int balance) {
        this.telegramUserName = telegramUserName;
        this.balance = balance;
        this.id = id;
        this.level = 1;
        this.bussinessIncome = 50;
        this.bussinessUpgrade = 5000;
        this.bussinessLevel = 1;
    }

    public int getBussinessIncome() {
        return bussinessIncome;
    }

    public void setBussinessIncome(int bussinessIncome) {
        if (bussinessIncome >= 0) {
            this.bussinessIncome = bussinessIncome;
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET bussiness_income_amount='" + bussinessIncome + "' WHERE username='" + telegramUserName + "'");
        }
    }

    public int getBussinessUpgrade() {
        return bussinessUpgrade;
    }



    public void setBussinessUpgrade(int bussinessUpgrade) {
        if (bussinessUpgrade >= 0) {
            this.bussinessUpgrade = bussinessUpgrade;
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET bussiness_upgrade_cost='" + bussinessUpgrade + "' WHERE username='" + telegramUserName + "'");
        }
    }

    public int getBussinessLevel() {
        return bussinessLevel;
    }

    public void updateLevel() {
        if (levelExp >= 100) {
            if (level < 2) {
                setLevel(2);
            }
        } else if (levelExp >= 250) {
            if (level < 3) {
                setLevel(3);
            }
        } else if (levelExp >= 500) {
            if (level < 4) {
                setLevel(4);
            }
        } else if (levelExp >= 1000) {
            if (level < 5) {
                setLevel(5);
            }
        } else if (levelExp >= 2500) {
            if (level < 6) {
                setLevel(6);
            }
        } else if (levelExp >= 5000) {
            if (level < 7) {
                setLevel(7);
            }
        } else if (levelExp >= 10000) {
            if (level < 8) {
                setLevel(8);
            }
        } else if (levelExp >= 25000) {
            if (level < 9) {
                setLevel(9);
            }
        } else if (levelExp >= 50000) {
            if (level < 10) {
                setLevel(10);
            }
        }
    }

    public void setBussinessLevel(int bussinessLevel) {
        if (bussinessLevel >= 0) {
            this.bussinessLevel = bussinessLevel;
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET bussiness_level='" + bussinessLevel + "' WHERE username='" + telegramUserName + "'");
        }
    }

    public User(String telegramUserName, int balance) {
        this.telegramUserName = telegramUserName;
        this.balance = balance;
        this.id = 0L;
        this.level = 1;
        this.bussinessIncome = 50;
        this.bussinessUpgrade = 5000;
        this.bussinessLevel = 1;
    }

    public User(String telegramUserName) throws NoSuchAlgorithmException {
        this.telegramUserName = telegramUserName;
        this.balance = 150;
        this.id = 0L;
        this.level = 1;
        this.bussinessIncome = 50;
        this.bussinessUpgrade = 5000;
        this.bussinessLevel = 1;
    }

    public String getTelegramUserName() {
        return telegramUserName;
    }

    public void setTelegramUserName(String telegramUserName) {
        this.telegramUserName = telegramUserName;
        UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET username='" + telegramUserName + "' WHERE username='" + telegramUserName + "'");
    }

    public int getBalance() {
        return balance;
    }

    public void updateInBase() {
        try {
            for (User user : UserBase.getInstance().getUsersBase()) {
                if (user.getTelegramUserName().equalsIgnoreCase(getTelegramUserName())) {
                    UserBase.getInstance().getUsersBase().remove(user);
                    UserBase.getInstance().getUsersBase().add(this);
                }
            }
        } catch (ConcurrentModificationException e) {}
    }

    public void setBalance(int balance) {
        if (balance >= 0) {
            this.balance = balance;
            updateInBase();
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET balance=" + balance + " WHERE username='" + telegramUserName + "'");
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "telegramUserName='" + telegramUserName + '\'' +
                ", id=" + id +
                ", balance=" + balance +
                ", level=" + level +
                ", levelExp=" + levelExp +
                ", bussinessIncome=" + bussinessIncome +
                ", bussinessUpgrade=" + bussinessUpgrade +
                ", bussinessLevel=" + bussinessLevel +
                '}';
    }

    public void depositBalance(int amount) throws SQLException, NoSuchAlgorithmException {
        balance += amount;
        updateInBase();
        UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET balance=" + balance + " WHERE username='" + telegramUserName + "'");
    }

    public void withdrawBalance(int amount) throws SQLException, NoSuchAlgorithmException {
        if (balance >= amount) {
            balance -= amount;
            updateInBase();
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET balance=" + balance + " WHERE username='" + telegramUserName + "'");
        }
    }

    public void setId(Long id) {
        this.id = id;
        UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET id=" + id + " WHERE username='" + telegramUserName + "'");
    }

    public void subtractLevelExp(int amount) {
        if (levelExp >= amount) {
            levelExp -= amount;
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET level_exp=" + levelExp + " WHERE username='" + telegramUserName + "'");
        }
    }

    public void addLevelExp(int amount) {
        levelExp += amount;
        UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET level_exp=" + levelExp + " WHERE username='" + telegramUserName + "'");
    }

    public int getLevelExp() {
        return levelExp;
    }

    public void setLevelExp(int levelExp) {
        if (levelExp >= 0) {
            this.levelExp = levelExp;
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET level_exp=" + levelExp + " WHERE username='" + telegramUserName + "'");
        }
    }

    public Long getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level >= 0) {
            this.level = level;
            updateInBase();
            UserBase.getInstance().getSqlUtil().executeQuery("UPDATE users SET level=" + level + " WHERE username='" + telegramUserName + "'");
        }
    }
}
