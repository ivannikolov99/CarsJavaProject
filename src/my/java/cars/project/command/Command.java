package my.java.cars.project.command;

import my.java.cars.project.cars.Car;
import my.java.cars.project.exceptionlogger.ErrorLogger;
import my.java.cars.project.users.User;
import my.java.cars.project.users.UsersCollection;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Command {
    private static final String USERS_FILE = "users.txt";
    private static final String CARS_FILE = "cars.txt";

    private static UsersCollection users;
    private static Map<Integer, Car> cars;

    public Command() {
        users = UsersCollection.getInstance();
        cars = new HashMap<>();

        Path usersFilePath = Path.of(USERS_FILE);
        if (Files.exists(usersFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(usersFilePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] args = line.split(",");
                    String email = args[0];
                    String password = args[1];
                    double balance = Double.parseDouble(args[2]);
                    User user = new User(email, password);
                    user.setBalance(balance);
                    users.addRegisteredUser(user);
                }
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new RuntimeException("There is a problem with the users file", e);
            }
        }
        Path carsFilePath = Path.of(CARS_FILE);
        if (Files.exists(carsFilePath)) {
            try (BufferedReader br = Files.newBufferedReader(carsFilePath)) {
                String line;
                int i = 0;
                while ((line = br.readLine()) != null) {
                    String[] args = line.split(",");
                    String brand = args[0];
                    String model = args[1];
                    int power = Integer.parseInt(args[2]);
                    int manufactureYear = Integer.parseInt(args[3]);
                    double price = Double.parseDouble(args[4]);
                    cars.put(i, new Car(brand, model, power, manufactureYear, price));
                    i++;
                }
            } catch (IOException e) {
                ErrorLogger.logClientError(e);
                throw new RuntimeException("There is a problem with the cars file", e);
            }
        }


    }

    public String execute(String message, int scHash) {
        String response;
        if (message.equals("help")) {
            response = listCommands(message);
        } else if (message.startsWith("register")) {
            response = register(message, scHash);
        } else if (message.startsWith("login")) {
            response = login(message, scHash);
        } else if (message.startsWith("add-balance")) {
            response = addBalance(message, scHash);
        } else if (message.startsWith("list-all")) {
            response = listAllCars(message, scHash);
        } else if (message.startsWith("buy-car ")) {
            response = buyCar(message, scHash);
        } else if (message.equals("logout")) {
            response = logout(scHash);
        } else if (message.equals("disconnect")) {
            response = disconnect(scHash);
        } else {
            response = "Invalid command.";
        }

        return response + System.lineSeparator();
    }

    private String listCommands(String message) {
        if (validateCommand(message) != 1) {
            return "Invalid command";
        }

        return """
                Available commands:
                register <email> <password>
                login <email> <password>
                add-balance <sum>
                list-all
                buy-car <index of car>   
                logout
                disconnect
                """;
    }

    private String register(String message, int scHash) {

        if (checkIfLogged(scHash)) {
            return "You have to first logout before making new account.";
        }

        if (validateCommand(message) != 3) {
            return "Invalid command";
        }

        User user = getNewUser(message);
        String email = user.getEmail();
        String password = user.getPassword();

        if (invalidEmail(email)) {
            return "Email " + email + " is invalid, select a valid one.";
        }

        if (!users.addRegisteredUser(user)) {
            return " Email " + email + " is already taken, select another one.";
        }

        try (FileWriter fileWriter = new FileWriter(USERS_FILE, true);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            writer.println(email + "," + password + "," + user.getBalance());
        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to the users file", e);
        }

        users.addRegisteredUser(user);
        return " User with email " + email + " successfully registered.";
    }


    private String login(String message, int scHash) {
        if (checkIfLogged(scHash)) {
            return "You are already logged";
        }

        if (validateCommand(message) != 3) {
            return "Invalid command";
        }

        User user = getNewUser(message);
        User existingUser;

        if (!users.addRegisteredUser(user)) {
            existingUser = users.getRegisteredUser(user.getEmail());
            if (!users.checkIfAlreadyLogged(existingUser)) {
                return "Other user is logged in ,in the account";
            }
        } else {
            return "Invalid email";
        }
        if (existingUser.getPassword().equals(user.getPassword())) {
            users.addLoggedUser(scHash, users.getRegisteredUser(user.getEmail()));
            return " User with email " + user.getEmail() + " successfully logged in ";
        } else {
            return "Invalid password";
        }
    }

    private String addBalance(String message, int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You must login first";
        }

        if (validateCommand(message) != 2) {
            return "Invalid command";
        }

        try {
            double sum = Double.parseDouble(message.substring(message.indexOf(" ")).strip());
            users.getRegisteredUser(users.getLoggedUser(scHash).getEmail()).setBalance(sum);

            return "Balance updated successfully";

        } catch (NumberFormatException e) {
            ErrorLogger.logClientError(e);
            return "Invalid command";
        }
    }

    private String listAllCars(String message, int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You are not logged in";
        }

        if (validateCommand(message) != 1) {
            return "Invalid command";
        }

        if (cars.isEmpty()) {
            return "There aren't any cars left";
        }
        return cars.toString();
    }

    private String buyCar(String message, int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You are not logged in";
        }

        if (validateCommand(message) != 2) {
            return "Invalid command";
        }
        try {
            int index = Integer.parseInt(message.substring(message.indexOf(" ")).strip());
            if (cars.containsKey(index)) {
                if (cars.get(index).price() > users.getLoggedUser(scHash).getBalance()) {
                    return "Not enough balance";
                } else {
                    users.getRegisteredUser(users.getLoggedUser(scHash).getEmail()).setBalance(-(cars.get(index).price()));
                    cars.remove(index);
                    return "Car bought successfully";
                }
            }
        } catch (NumberFormatException e) {
            ErrorLogger.logClientError(e);
            return "Invalid command";
        }
        return "Invalid car index";
    }

    private String logout(int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You must login first";
        }

        if (!checkIfLogged(scHash)) {
            return "You are not logged in";
        }
        if (users.removeLoggedUser(scHash)) {
            return "You successfully logged out. ";
        }
        return "There's a problem with logging out.";
    }


    private String disconnect(int scHash) {
        if (!checkIfLogged(scHash)) {
            return "You must login first";
        }
        users.removeLoggedUser(scHash);

        try (FileWriter fileWriter = new FileWriter(USERS_FILE);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            for (User user : users.getAllRegisteredUsers()) {
                writer.println(user.getEmail() + "," + user.getPassword() + "," + user.getBalance());
            }

        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to the users file", e);
        }

        try (FileWriter fileWriter = new FileWriter(CARS_FILE);
             PrintWriter writer = new PrintWriter(fileWriter, true)) {
            for (Car car : cars.values()) {
                writer.println(car.brand() + "," + car.model() + "," + car.power() + ","
                        + car.manufactureYear() + "," + car.price());
            }

        } catch (IOException e) {
            ErrorLogger.logClientError(e);
            throw new IllegalStateException("A problem occurred while writing to the cars file", e);
        }

        return "Disconnected successfully";
    }


    private boolean invalidEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        return !email.matches(regex);
    }

    private User getNewUser(String s) {
        int firstWordSplitter = s.indexOf(" ");
        int SecondWordSplitter = s.substring(firstWordSplitter + 1).indexOf(" ")
                + firstWordSplitter + 1;

        String email = s.substring(firstWordSplitter + 1, SecondWordSplitter).strip();
        String password = s.substring(SecondWordSplitter + 1).strip();

        return new User(email, password);
    }

    private int validateCommand(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        String[] words = message.split(" ");
        return words.length;
    }

    private boolean checkIfLogged(int scHash) {
        return users.checkIfLogged(scHash);
    }


}
