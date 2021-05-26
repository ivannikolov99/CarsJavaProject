package my.java.cars.project.command;

import my.java.cars.project.exceptionlogger.ErrorLogger;
import my.java.cars.project.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandTest {
    private Command commandExecutor;
    private int dummyScHashCode;

    @Before
    public void makeInstance(){
        commandExecutor=new Command();
        dummyScHashCode=777;
    }
    @After
    public void deleteTestData() {
        String message="logout";
        commandExecutor.execute(message,dummyScHashCode);

        List<User> filteredUsers=new ArrayList<>();
        Path usersFilePath = Path.of("users.txt");
        if (Files.exists(usersFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(usersFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains("test")){
                        continue;
                    }
                    String[] args = line.split(",");
                    String email = args[0];
                    String password = args[1];
                    double balance = Double.parseDouble(args[2]);
                    User user = new User(email, password);
                    user.setBalance(balance);
                    filteredUsers.add(user);
                }
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }

        try (FileWriter fileWriter = new FileWriter("users.txt" , false);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            for(User user :filteredUsers) {
                writer.println(user.getEmail() + "," + user.getPassword()+","+user.getBalance());
            }
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to the users file", e);
        }
    }

    @Test
    public void executeHelpTest(){
        String message = "help";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse = """
                Available commands:
                register <email> <password>
                login <email> <password>
                add-balance <sum>
                list-all
                buy-car <index of car>   
                logout
                disconnect
                """+System.lineSeparator();

        assertEquals("'help' must succeed. ", expectedResponse,response);

    }

    @Test
    public void executeRegisterInvalidCommand(){
        String message="register testmisho@abv.bg";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'register' must fail", expectedResponse,response);
    }

    @Test
    public void executeRegisterAlreadyLoggedIn(){
        String message="register testmisho4@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho4@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="register testmisho2@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="You have to first logout before making new account."+System.lineSeparator();

        assertEquals("'register' must fail ", expectedResponse,response);
    }

    @Test
    public void executeRegisterInvalidEmail(){
        String message="register testmisho@g 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Email testmisho@g is invalid, select a valid one."+System.lineSeparator();

        assertEquals("'register' must fail ", expectedResponse,response);
    }

    @Test
    public void executeRegisterTakenEmail(){
        String message="register testmisho@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse =" Email testmisho@abv.bg is already taken, select another one."+System.lineSeparator();

        assertEquals("'register' must fail ", expectedResponse,response);
    }

    @Test
    public void executeRegisterSuccessfulRegister(){
        String message="register testmisho@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse =" User with email testmisho@abv.bg successfully registered."+System.lineSeparator();

        assertEquals("'register' must be successful ", expectedResponse,response);
    }

    @Test
    public void executeLoginAlreadyLoggedIn(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testmisho1@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="You are already logged"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginInvalidCommand(){
        String message="login kaq@abv.bg";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginOtherUserAlreadyLogged(){
        int otherUserHash=666;
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,otherUserHash);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,otherUserHash);
        message="login testmisho1@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Other user is logged in ,in the account"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginEmailNotFound(){
        String message="login testNotExisting@abv.bg 1";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid email"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeLoginWrongPassword(){
        String message="register testmisho3@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="login testmisho3@abv.bg wrong";
        String response = commandExecutor.execute(message,dummyScHashCode);

        String expectedResponse ="Invalid password"+System.lineSeparator();

        assertEquals("'login' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddBalanceNotLoggedIn(){
        String message="add-balance 2222";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="You must login first"+System.lineSeparator();

        assertEquals("'add-balance' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddBalanceInvalidNumberFormat(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-balance qwq";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'add-balance' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddBalanceInvalidCommand(){
        String message="register testmisho7@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho7@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-balance";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'add-balance' must fail ", expectedResponse,response);
    }

    @Test
    public void executeAddBalanceSuccessful(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="add-balance 2222";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Balance updated successfully"+System.lineSeparator();

        assertEquals("'add-balance' must succeed ", expectedResponse,response);
    }

    @Test
    public void executeListAllNotLoggedIn(){
        String message="list-all";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="You are not logged in"+System.lineSeparator();

        assertEquals("'list-all' must fail ", expectedResponse,response);
    }

    @Test
    public void executeListAllInvalidCommand(){
        String message="register testmisho12@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho12@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="list-all qwqw qwqw 1";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'list-all' must fail ", expectedResponse,response);
    }

    @Test
    public void executeBuyCarNotLoggedIn(){
        String message="buy-car 1";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="You are not logged in"+System.lineSeparator();

        assertEquals("'buy-car' must fail ", expectedResponse,response);
    }

    @Test
    public void executeBuyCarInvalidCommand(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="buy-car 1 11 1";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'buy-car' must fail ", expectedResponse,response);
    }

    @Test
    public void executeBuyCarNotEnoughBalance(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="buy-car 1";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Not enough balance"+System.lineSeparator();

        assertEquals("'buy-car' must fail ", expectedResponse,response);
    }

    @Test
    public void executeBuyCarInvalidNumberFormat(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="buy-car qq";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Invalid command"+System.lineSeparator();

        assertEquals("'buy-car' must fail ", expectedResponse,response);
    }

    @Test
    public void executeBuyCarInvalidCarIndex(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="buy-car -2020220";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Invalid car index"+System.lineSeparator();

        assertEquals("'buy-car' must fail ", expectedResponse,response);
    }


    @Test
    public void executeDisconnectNotLoggedIn(){
        String message="disconnect";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="You must login first"+System.lineSeparator();

        assertEquals("'disconnect' must fail ", expectedResponse,response);
    }

    @Test
    public void executeDisconnectSuccessful(){
        String message="register testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message = "login testmisho1@abv.bg 1";
        commandExecutor.execute(message,dummyScHashCode);
        message="disconnect";
        String response = commandExecutor.execute(message,dummyScHashCode);
        String expectedResponse ="Disconnected successfully"+System.lineSeparator();

        assertEquals("'disconnect' must fail ", expectedResponse,response);
    }

}
