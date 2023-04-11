package losonczylab.behaviormate;

public class Main {
    public static void main(String[] args) {
        try {
            BehaviorMate.main(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(99);
        }
    }
}
