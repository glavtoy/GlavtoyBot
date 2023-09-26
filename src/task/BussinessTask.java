package task;

import user.User;
import user.UserBase;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BussinessTask implements Runnable {

    public void startTask(TimeUnit timeUnit, int period) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new BussinessTask(), 0, period, timeUnit);
    }

    @Override
    public void run() {
        for (User user : UserBase.getInstance().getUsersBase()) {
            int income = (user.getBussinessUpgrade() / 100);
            user.setBussinessIncome(user.getBussinessIncome() + income);
            user.addLevelExp(2);
            user.updateLevel();
        }
    }
}