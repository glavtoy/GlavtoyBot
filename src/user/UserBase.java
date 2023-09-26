package user;

import util.HashMapSorterUtil;
import util.SQLUtil;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserBase {

    private static SQLUtil sqlUtil;

    private static UserBase instance;

    private static Set<User> users = new HashSet<>();

    private UserBase() {}

    public static UserBase getInstance() {
        if (instance == null) {
            instance = new UserBase();
        }
        return instance;
    }

    private static HashMap<User, Integer> moneyLeaderList = new HashMap<>();
    private static HashMap<User, Integer> levelLeaderList = new HashMap<>();

    public void setSqlUtil(SQLUtil sqlUtil) {
        this.sqlUtil = sqlUtil;
    }

    public SQLUtil getSqlUtil() {
        return sqlUtil;
    }

    public Set<User> getUsersBase() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public boolean containsUser(User user) throws SQLException {
        ResultSet resultSet = getSqlUtil().executeQueryWithResultSet("SELECT * FROM users");
        if (resultSet != null) {
            while (resultSet.next()) {
                if (resultSet.getString("username").equalsIgnoreCase(user.getTelegramUserName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsUserWithName(String name) throws SQLException {
        ResultSet resultSet = getSqlUtil().executeQueryWithResultSet("SELECT * FROM users");
        if (resultSet != null) {
            while (resultSet.next()) {
                if (resultSet.getString("username").equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashMap<User, Integer> getMoneyLeaderList() {
        return moneyLeaderList;
    }

    public HashMap<User, Integer> getLevelLeaderList() {
        return levelLeaderList;
    }

    public User getUserByName(String username) throws SQLException {
        ResultSet resultSet = getSqlUtil().executeQueryWithResultSet("SELECT * FROM users");
        if (resultSet != null) {
            while (resultSet.next()) {
                if (resultSet.getString("username").equalsIgnoreCase(username)) {
                    return new User(resultSet.getLong("id"), resultSet.getString("username"), resultSet.getInt("balance"), resultSet.getInt("level"), resultSet.getInt("bussiness_income_amount"), resultSet.getInt("bussiness_upgrade_cost"), resultSet.getInt("bussiness_level"), resultSet.getInt("level_exp"));
                }
            }
        }
        return null;
    }

    public int getMoneyLeaderboardPosition(User user) {
        int position = 1;
        updateUserInLeaderboards(user);
        HashMap<User, Integer> leaderList = HashMapSorterUtil.sort(UserBase.getInstance().getMoneyLeaderList());
        Iterator<Map.Entry<User, Integer>> iterator = leaderList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<User, Integer> entry = iterator.next();
            if (entry.getKey().getTelegramUserName().equalsIgnoreCase(user.getTelegramUserName())) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public int getLevelLeaderboardPosition(User user) {
        int position = 1;
        updateUserInLeaderboards(user);
        HashMap<User, Integer> leaderList = HashMapSorterUtil.sort(UserBase.getInstance().getLevelLeaderList());
        Iterator<Map.Entry<User, Integer>> iterator = leaderList.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<User, Integer> entry = iterator.next();
            if (entry.getKey().getTelegramUserName().equalsIgnoreCase(user.getTelegramUserName())) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public void addUserInDB(User user) throws SQLException {
        getSqlUtil().executeQuery("INSERT INTO users (id, username, balance, level, bussiness_income_amount, bussiness_upgrade_cost, bussiness_level, level_exp) VALUES ('" + user.getId() + "', '" + user.getTelegramUserName() + "', " + user.getBalance() + ", " + user.getLevel() + ", " + user.getBussinessIncome() + ", " + user.getBussinessUpgrade() + ", " + user.getBussinessLevel() + ", " + user.getLevelExp() + ");");
    }

    public void reload() throws SQLException {
        users.clear();
        moneyLeaderList.clear();
        levelLeaderList.clear();
        ResultSet resultSet = getSqlUtil().executeQueryWithResultSet("SELECT * FROM users");
        if (resultSet != null) {
            while (resultSet.next()) {
                User user = new User(resultSet.getLong("id"), resultSet.getString("username"), resultSet.getInt("balance"), resultSet.getInt("level"),
                        resultSet.getInt("bussiness_income_amount"), resultSet.getInt("bussiness_upgrade_cost"), resultSet.getInt("bussiness_level"), resultSet.getInt("level_exp"));
                addUser(user);
            }
        }
    }

    public void updateLeaderboards() throws SQLException, NoSuchAlgorithmException {
        moneyLeaderList.clear();
        levelLeaderList.clear();
        for (User user : users) {
            moneyLeaderList.put(user, user.getBalance());
            levelLeaderList.put(user, user.getLevel());
        }
    }

    public void updateUserInLeaderboards(User user) {
        levelLeaderList.remove(user);
        moneyLeaderList.put(user, user.getBalance());
        levelLeaderList.put(user, user.getLevel());
    }

    public void load(SQLUtil sqlUtil) throws SQLException {
        setSqlUtil(sqlUtil);
        ResultSet resultSet = getSqlUtil().executeQueryWithResultSet("SELECT * FROM users");
        if (resultSet != null) {
            while (resultSet.next()) {
                User user = new User(resultSet.getLong("id"), resultSet.getString("username"), resultSet.getInt("balance"), resultSet.getInt("level"),
                        resultSet.getInt("bussiness_income_amount"), resultSet.getInt("bussiness_upgrade_cost"), resultSet.getInt("bussiness_level"), resultSet.getInt("level_exp"));
                moneyLeaderList.put(user, user.getBalance());
                levelLeaderList.put(user, user.getLevel());
                addUser(user);
            }
        }
    }

    public void load() throws SQLException {
        if (sqlUtil != null) {
            ResultSet resultSet = getSqlUtil().executeQueryWithResultSet("SELECT * FROM users");
            if (resultSet != null) {
                while (resultSet.next()) {
                    User user = new User(resultSet.getLong("id"), resultSet.getString("username"), resultSet.getInt("balance"), resultSet.getInt("level"),
                            resultSet.getInt("bussiness_income_amount"), resultSet.getInt("bussiness_upgrade_cost"), resultSet.getInt("bussiness_level"), resultSet.getInt("level_exp"));
                    moneyLeaderList.put(user, user.getBalance());
                    levelLeaderList.put(user, user.getLevel());
                    addUser(user);
                }
            }
        }
    }
}
