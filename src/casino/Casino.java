package casino;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Casino {

    private static Casino instance;

    private Casino() {}
    private static Random random = new Random();

    public static Casino getInstance() {
        if (instance == null) {
            instance = new Casino();
        }
        return instance;
    }

    public int generateGameResult() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(random.nextInt(101));
        }
        int a = random.nextInt(101);
        int b = 0;
        for (int c : list) {
            if (c == a) {
                b = c;
                break;
            }
        }
        if (b != 0) {
            if (b == 0 || b == 100) {
                return 100;
            } else if (b <= 5 && b >= 1) {
                return 25;
            } else if (b >= 95 && b <= 99) {
                return 25;
            } else if (b >= 45 && b <= 55) {
                return 10;
            } else if (b >= 70 && b <= 75) {
                return 5;
            } else if (b >= 20 && b <= 25) {
                return 5;
            } else {
                return 2;
            }
        }
        return 0;
    }
}
