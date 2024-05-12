import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class TMUberRegistered
{
    // These variables are used to generate user account and driver ids
    private static int firstUserAccountID = 900;
    private static int firstDriverId = 700;

    // Generate a new user account id
    public static String generateUserAccountId(ArrayList<User> current)
    {
        return "" + firstUserAccountID + current.size();
    }

    // Generate a new driver id
    public static String generateDriverId(ArrayList<Driver> current)
    {
        return "" + firstDriverId + current.size();
    }

    // Database of Preregistered users
    // In Assignment 2 these will be loaded from a file
    // The test scripts and test outputs included with the skeleton code use these
    // users and drivers below. You may want to work with these to test your code (i.e. check your output with the
    // sample output provided). 
    public static ArrayList<User> loadPreregisteredUsers(String filename) throws IOException
    {
        ArrayList<User> users = new ArrayList<>();
        Scanner input = new Scanner(new File(filename));

        while(input.hasNextLine()){
            String name = input.nextLine();
            String address = input.nextLine();
            double wallet = Double.parseDouble(input.nextLine());

            users.add(new User(generateUserAccountId(users), name, address, wallet));
        }
        input.close();
        return users;
    }

    // Database of Preregistered users
    // In Assignment 2 these will be loaded from a file
    public static ArrayList<Driver> loadPreregisteredDrivers(String filename) throws IOException
    {
        ArrayList<Driver> drivers = new ArrayList<>();
        Scanner input = new Scanner(new File(filename));

        while (input.hasNextLine()) {
            String name = input.nextLine();
            String carModel = input.nextLine();
            String licensePlate = input.nextLine();
            String address = input.nextLine();

            drivers.add(new Driver(generateDriverId(drivers), name, carModel, licensePlate, address));
        }
        input.close();
        return drivers;
    }
}

