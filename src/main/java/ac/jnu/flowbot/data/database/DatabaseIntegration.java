package ac.jnu.flowbot.data.database;

public class DatabaseIntegration {

    private static DatabaseIntegration instance;

    public static DatabaseIntegration getInstance() {
        if(instance == null)
            instance = new DatabaseIntegration();
        return instance;
    }

    private DatabaseIntegration() {

    }

    public void insertMemberData(MemberData md) {

    }
}
