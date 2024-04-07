//package karpiuk.test.config;
//
//import org.testcontainers.containers.MySQLContainer;
//
//public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
//    private static final String DB_IMAGE = "mysql:8";
//    private static final String TEST_DB_URL = "TEST_DB_URL";
//    private static final String TEST_DB_USERNAME = "TEST_DB_USERNAME";
//    private static final String TEST_DB_PASSWORD = "TEST_DB_PASSWORD";
//    private static CustomMySqlContainer mySqlContainer;
//
//    private CustomMySqlContainer() {
//        super(DB_IMAGE);
//    }
//
//    public static synchronized CustomMySqlContainer getInstance() {
//        if (mySqlContainer == null) {
//            mySqlContainer = new CustomMySqlContainer();
//        }
//        return mySqlContainer;
//    }
//
//    @Override
//    public void start() {
//        super.start();
//        System.setProperty(TEST_DB_URL, mySqlContainer.getJdbcUrl());
//        System.setProperty(TEST_DB_USERNAME, mySqlContainer.getUsername());
//        System.setProperty(TEST_DB_PASSWORD, mySqlContainer.getPassword());
//    }
//
//    @Override
//    public void stop() {
//        super.stop();
//    }
//}
