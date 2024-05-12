
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Iterator;

import java.util.Set;

/*
 * 
 * This class contains the main logic of the system.
 * 
 *  It keeps track of all users, drivers and service requests (RIDE or DELIVERY)
 * 
 */
public class TMUberSystemManager
{
  private Map<String, User>   users;
  private ArrayList<Driver> drivers;
  private ArrayList<User> userList;
  private Queue<TMUberService>[] serviceRequests;


  public double totalRevenue; // Total revenues accumulated via rides and deliveries
  
  // Rates per city block
  private static final double DELIVERYRATE = 1.2;
  private static final double RIDERATE = 1.5;
  
  // Portion of a ride/delivery cost paid to the driver
  private static final double PAYRATE = 0.1;

  // These variables are used to generate user account and driver ids
  int userAccountId = 900;
  int driverId = 700;

  public TMUberSystemManager() {
    users   = new TreeMap<String, User>();
    drivers = new ArrayList<Driver>();
    userList = new ArrayList<>();
    serviceRequests = (Queue<TMUberService>[]) new Queue[4];
    serviceRequests[0] = new LinkedList<TMUberService>();
    serviceRequests[1] = new LinkedList<TMUberService>();
    serviceRequests[2] = new LinkedList<TMUberService>();
    serviceRequests[3] = new LinkedList<TMUberService>();
    totalRevenue = 0;
  }

  
  // Generate a new user account id
  private String generateUserAccountId(){return "" + userAccountId + users.size();}
  
  // Generate a new driver id
  private String generateDriverId(){return "" + driverId + drivers.size();}

  // Given user account id, find user in list of users
  public User getUser(String accountId) {
    if(users.containsKey(accountId)){
      return users.get(accountId);
    }
    return null;
  }

  //gets driver object given driver id
  public Driver getDriver(String accountId) {
    for(int i = 0; i < drivers.size(); i++) {
      if(drivers.get(i).getId().equals(accountId))
        return drivers.get(i);
    }
    return null;
  }
  
  // Check for duplicate user
 private boolean userExists(User user){return users.containsValue(user);}
  
 // Check for duplicate driver
 private boolean driverExists(Driver driver){return drivers.contains(driver);}
  
 
 // Given a user, check if user ride/delivery request already exists in service requests
 private boolean existingRequest(TMUberService req) {

   for(Queue<TMUberService> queue: serviceRequests){
    for(TMUberService service: queue){
      if(service.equals(req)){
        return true;
      }
    }
  }
  return false;
 }
 
  
  // Calculate the cost of a ride or of a delivery based on distance 
  private double getDeliveryCost(int distance){return distance * DELIVERYRATE;}

  private double getRideCost(int distance){return distance * RIDERATE;}

  // Go through all drivers and see if one is available
  // Choose the first available driver
  private Driver getAvailableDriver()
  {
    for (int i = 0; i < drivers.size(); i++)
    {
      Driver driver = drivers.get(i);
      if (driver.getStatus() == Driver.Status.AVAILABLE)
        return driver;
    }
    return null;
  }

  // Print Information (printInfo()) about all registered users in the system
  public void listAllUsers()
  {
    System.out.println();
    int count = 0;
    for (User u : userList) {
      count++;
      System.out.printf("%-2s. ", count);
      u.printInfo();
      System.out.println(); 

    }
  }

  // Print Information (printInfo()) about all registered drivers in the system
  public void listAllDrivers()
  {
    System.out.println();
    
    for (int i = 0; i < drivers.size(); i++)
    {
      int index = i + 1;
      System.out.println();
      System.out.printf("%-2s. ", index);
      drivers.get(i).printInfo();
      System.out.println(); 
    }
  }

  //lists all service requests
  public void listAllServiceRequests(){
    int count = 0;
    for (Queue<TMUberService> queue : serviceRequests) {
      System.out.println("\nZONE " + count);
      System.out.println("======");
      int index = 0;
      for (TMUberService service : queue) {
        index++;
        System.out.println();
        System.out.print(index + ". ");
        for (int j = 0; j < 60; j++)
          System.out.print("-");
        service.printInfo();
        System.out.println(); 
      }
      count++;
    }
  }

  // Add a new user to the system
  public void registerNewUser(String name, String address, double wallet)
  {
    // Check to ensure name is valid
    if (name == null || name.equals(""))
    {
      throw new InvalidUserNameException("Invalid User Name " + name);
    }
    // Check to ensure address is valid
    if (!CityMap.validAddress(address))
    {
      throw new InvalidAddressException("Invalid User Address " + address);
    }
    // Check to ensure wallet amount is valid
    if (wallet < 0)
    {
      throw new InvalidMoneyInWalletException("Invalid Money in Wallet");
    }
    // Check for duplicate user
    User user = new User(TMUberRegistered.generateUserAccountId(userList), name, address, wallet);
    if (userExists(user))
    {
      throw new UserAlreadyExistsException("User Already Exists in System");
    }
    userList.add(user);
    users.put(user.getAccountId(), user);  
  }

  // Add a new driver to the system
  public void registerNewDriver(String name, String carModel, String carLicencePlate, String address)
  {
    // Check to ensure name is valid
    if (name == null || name.equals(""))
    {
      throw new InvalidDriverNameException("Invalid Driver Name " + name);
    }
    // Check to ensure car models is valid
    if (carModel == null || carModel.equals(""))
    {
      throw new InvalidCarModelException("Invalid Car Model " + carModel);
    }
    // Check to ensure car licence plate is valid
    // i.e. not null or empty string
    if (carLicencePlate == null || carLicencePlate.equals(""))
    {
      throw new InvalidCarLicencePlateException("Invalid Car Licence Plate " + carLicencePlate);
    }
    if(!CityMap.validAddress(address))
    {
      throw new InvalidAddressException("Invalid Address " + address);
    }
    // Check for duplicate driver. If not a duplicate, add the driver to the drivers list
    Driver driver = new Driver(TMUberRegistered.generateDriverId(drivers), name, carModel, carLicencePlate, address);
    if (driverExists(driver))
    {
      throw new DriverAlreadyExistsException("Driver Already Exists in System");
    }
    drivers.add(driver);   
  }

  // Request a ride. User wallet will be reduced when drop off happens
  public void requestRide(String accountId, String from, String to)
  {
    // Check valid user account
    User user = getUser(accountId);
    if (user == null)
    {
      throw new UserAccountNotFoundException("User Account Not Found " + accountId);
    }
    // Check for a valid from and to addresses
    if (!CityMap.validAddress(from))
    {
      throw new InvalidAddressException("Invalid Address " + from);
    }
    if (!CityMap.validAddress(to))
    {
      throw new InvalidAddressException("Invalid Address " + to);
    }
    // Get the distance for this ride
    int distance = CityMap.getDistance(from, to);         // city blocks
    // Distance == 0 or == 1 is not accepted - walk!
    if (!(distance > 1))
    {
     throw new InsufficientTravelDistanceException("Insufficient Travel Distance");
    }
    // Check if user has enough money in wallet for this trip
    double cost = getRideCost(distance);
    if (user.getWallet() < cost)
    {
      throw new InsufficientFundsException("Insufficient Funds");
    }
    // Create the request
    TMUberRide req = new TMUberRide(from, to, user, distance, cost);
    
    // Check if existing ride request for this user - only one ride request per user at a time
    if (existingRequest(req))
    {
      throw new UserAlreadyHasRideRequestException("User Already Has Ride Request");
    }
    serviceRequests[CityMap.getCityZone(from)].add(req);
    user.addRide();
  }

  // Request a food delivery. User wallet will be reduced when drop off happens
  public void requestDelivery(String accountId, String from, String to, String restaurant, String foodOrderId)
  {
    // Check for valid user account
    User user = getUser(accountId);
    if (user == null)
    {
      throw new UserAccountNotFoundException("User Account Not Found " + accountId);
    }
    // Check for valid from and to address
    if (!CityMap.validAddress(from))
    {
      throw new InvalidAddressException("Invalid Address " + from);
    }
    if (!CityMap.validAddress(to))
    {
      throw new InvalidAddressException("Invalid Address " + to);
    }
    // Get the distance to travel
    int distance = CityMap.getDistance(from, to);         // city blocks
    // Distance must be at least 1 city block
    if (distance == 0)
    {
      throw new InsufficientTravelDistanceException("Insufficient Travel Distance");
    }
    // Check if user has enough money in wallet for this delivery
    double cost = getDeliveryCost(distance);
    if (user.getWallet() < cost)
    {
      throw new InsufficientFundsException("Insufficient Funds");
    }

    TMUberDelivery delivery = new TMUberDelivery(from, to, user, distance, cost, restaurant, foodOrderId); 
    // Check if existing delivery request for this user for this restaurant and food order #
    if (existingRequest(delivery))
    {
      throw new UserAlreadyHasDeliveryRequestException("User Already Has Delivery Request at Restaurant with this Food Order");
    }
    serviceRequests[CityMap.getCityZone(from)].add(delivery);
    user.addDelivery();
  }


  // Cancel an existing service request. 
  // parameter request is the index in the serviceRequests array list
  public void cancelServiceRequest(int request, int zone)
  {
    // Check if valid request #
    if(zone < 0 || zone > 3){
      throw new InvalidZoneException("Invalid Zone " + zone);
    }
    if(serviceRequests[zone].size() == 0){
      throw new NoServiceInZoneException("No Service In Zone " + zone);
    }
    if (request < 1 || request > serviceRequests[zone].size())
    {
      throw new InvalidRequestException("Invalid Request # " + request);
    }
    Iterator iter = serviceRequests[zone].iterator();
    int count = 0;
    while(iter.hasNext()){
      iter.next();
      if(count == request-1){
        iter.remove();
        break;
      }
      count++;
    }
    
  }
  

  //delivers based of driverid
  public void dropOff(String driverId){
    Driver d1 = getDriver(driverId);
    if(d1 == null){
      throw new DriverAccountNotFoundException("Driver Not Found " + driverId);
    }
    if(d1.getStatus() != Driver.Status.DRIVING){
      throw new DriverDrivingException("Driver is Not Driving");
    }
    TMUberService service = d1.getService();
    if(service == null){
      throw new ServiceNotFoundException("Service Not Found");
    }
    totalRevenue += service.getCost();
    d1.pay(service.getCost()*PAYRATE);
    totalRevenue -= service.getCost()*PAYRATE;
    d1.setStatus(Driver.Status.AVAILABLE);
    d1.setAddress(service.getTo());
    d1.setService(null);
    User user = service.getUser();
    user.payForService(service.getCost());

  }

  //picks up delvery/ride given driver id
  public void pickup(String driverId){
    Driver d1 = getDriver(driverId);
    if(d1 == null){
      throw new DriverAccountNotFoundException("Driver Account Not Found " + driverId);
    }
    if(serviceRequests[d1.getZone()].peek() == null){
      throw new ServiceNotFoundException("No Service Requests in Zone " + d1.getZone());
    }
    d1.setService(serviceRequests[d1.getZone()].poll());
    d1.setStatus(Driver.Status.DRIVING);
    d1.setAddress(d1.getService().getFrom());
    d1.setToAddress(d1.getService().getTo());
  }

  //sets arraylists of users to maps
  //add to userList as well
  public void setUsers(ArrayList<User> userList){
    for(User u: userList){
      this.userList.add(u);
      users.put(u.getAccountId(), u);
    }
  }
  //adds drivers to drivers list
  public void setDrivers(ArrayList<Driver> driverList){
    for(Driver d: driverList){
      drivers.add(d);
    }
  }
  //changes address of driver given driver id and address
  public void driveTo(String driverId, String address){
    Driver d1 = getDriver(driverId);
    if(d1 == null){ 
      throw new DriverAccountNotFoundException("Driver Account Not Found " + driverId);
    }
    if(d1.getStatus() != Driver.Status.AVAILABLE){
      throw new NoDriversAvailableException("Driver Not Available");
    }
    else if(!CityMap.validAddress(address)){
      throw new InvalidAddressException("Invalid Address " + address);
    }
    d1.setAddress(address);
  }

  // Sort users by name
  public void sortByUserName()
  {
    Collections.sort(userList, new NameComparator());
    listAllUsers();
  }

  private class NameComparator implements Comparator<User>
  {
    public int compare(User a, User b)
    {
      return a.getName().compareTo(b.getName());
    }
  }

  // Sort users by number amount in wallet
  public void sortByWallet()
  {
    Collections.sort(userList, new UserWalletComparator());
    listAllUsers();
  }

  private class UserWalletComparator implements Comparator<User>
  {
    public int compare(User a, User b)
    {
      if (a.getWallet() > b.getWallet()) return 1;
      if (a.getWallet() < b.getWallet()) return -1; 
      return 0;
    }
  }



  //Exceptions
  private class InvalidUserAddressException extends RuntimeException {
      public InvalidUserAddressException() {
          super();
      }

      public InvalidUserAddressException(String message) {
          super(message);
      }
  }

  private class DriverDrivingException extends RuntimeException {
    public DriverDrivingException() {
        super();
    }

    public DriverDrivingException(String message) {
        super(message);
    }
}

  private class InvalidUserNameException extends RuntimeException {
      public InvalidUserNameException() {
          super();
      }

      public InvalidUserNameException(String message) {
          super(message);
      }
  }
  private class InvalidZoneException extends RuntimeException {
    public InvalidZoneException() {
        super();
    }

    public InvalidZoneException(String message) {
        super(message);
    }
}

  private class InvalidMoneyInWalletException extends RuntimeException {
      public InvalidMoneyInWalletException() {
          super();
      }

      public InvalidMoneyInWalletException(String message) {
          super(message);
      }
  }

  private class UserAlreadyExistsException extends RuntimeException {
      public UserAlreadyExistsException() {
          super();
      }

      public UserAlreadyExistsException(String message) {
          super(message);
      }
  }

  private class InvalidDriverNameException extends RuntimeException {
      public InvalidDriverNameException() {
          super();
      }

      public InvalidDriverNameException(String message) {
          super(message);
      }
  }

  private class InvalidCarModelException extends RuntimeException {
      public InvalidCarModelException() {
          super();
      }

      public InvalidCarModelException(String message) {
          super(message);
      }
  }

  private class InvalidCarLicencePlateException extends RuntimeException {
      public InvalidCarLicencePlateException() {
          super();
      }

      public InvalidCarLicencePlateException(String message) {
          super(message);
      }
  }

  private class DriverAlreadyExistsException extends RuntimeException {
      public DriverAlreadyExistsException() {
          super();
      }

      public DriverAlreadyExistsException(String message) {
          super(message);
      }
  }

  private class UserAccountNotFoundException extends RuntimeException {
      public UserAccountNotFoundException() {
          super();
      }

      public UserAccountNotFoundException(String message) {
          super(message);
      }
  }

  private class DriverAccountNotFoundException extends RuntimeException {
    public DriverAccountNotFoundException() {
        super();
    }

    public DriverAccountNotFoundException(String message) {
      super(message);
    }
}

  private class InvalidAddressException extends RuntimeException {
      public InvalidAddressException() {
          super();
      }

      public InvalidAddressException(String message) {
          super(message);
      }
  }

  private class InsufficientTravelDistanceException extends RuntimeException {
      public InsufficientTravelDistanceException() {
          super();
      }

      public InsufficientTravelDistanceException(String message) {
          super(message);
      }
  }

  private class InsufficientFundsException extends RuntimeException {
      public InsufficientFundsException() {
          super();
      }

      public InsufficientFundsException(String message) {
          super(message);
      }
  }

  private class NoDriversAvailableException extends RuntimeException {
      public NoDriversAvailableException() {
          super();
      }

      public NoDriversAvailableException(String message) {
          super(message);
      }
  }

  private class UserAlreadyHasRideRequestException extends RuntimeException {
      public UserAlreadyHasRideRequestException() {
          super();
      }

      public UserAlreadyHasRideRequestException(String message) {
          super(message);
      }
  }

  private class UserAlreadyHasDeliveryRequestException extends RuntimeException {
      public UserAlreadyHasDeliveryRequestException() {
          super();
      }

      public UserAlreadyHasDeliveryRequestException(String message) {
          super(message);
      }
  }

  private class NoServiceInZoneException extends RuntimeException {
    public NoServiceInZoneException() {
        super();
    }

    public NoServiceInZoneException(String message) {
        super(message);
    }
}

  private class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException() {
        super();
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }
}

  private class InvalidRequestException extends RuntimeException {
      public InvalidRequestException() {
          super();
      }

      public InvalidRequestException(String message) {
          super(message);
      }
  }  
}














