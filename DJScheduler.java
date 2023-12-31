import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("unchecked") 
public class DJScheduler {
    private static final String DJ_FILE = "djs.txt";
    private static final String BOOKING_FILE = "bookings.txt";
    private static final String WAITING_LIST_FILE = "waitinglist.txt";
    private static ArrayList<String> djsList;
    private static ArrayList<Booking> bookingsList;
    private static Queue<Request> waitingList;
    private static int eventDuration = 0; 
    private static Map<String, Integer> djBookingCounts;
    private static Scanner scanner;



    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        System.out.println("Welcome to DJ Scheduler!");
       

        djsList = readDataFile(DJ_FILE);
        bookingsList = readBookingsFile(BOOKING_FILE);
        waitingList = readWaitingListFile(WAITING_LIST_FILE);
        djBookingCounts = new HashMap<>();
        updateBookingCounts(LocalDateTime.now(), 0);




        boolean running = true;

        while (running) {
            System.out.println("\nMenu Options:");
            System.out.println("1. Schedule");
            System.out.println("2. Cancel");
            System.out.println("3. Signup");
            System.out.println("4. Dropout");
            System.out.println("5. Deejay Status");
            System.out.println("6. Date Status");
            System.out.println("7. Quit");
            System.out.print("Select an option (1-7): ");

            int choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    scheduleDeejay(scanner);
                    break;
                case 2:
                    cancelBooking(scanner);
                    break;
                case 3:
                    signupDeejay(scanner, eventDuration);
                    break;
                case 4:
                    dropoutDeejay(scanner);
                    break;
                case 5:
                    showDeejayStatus(scanner);
                    break;
                case 6:
                    showDateStatus(scanner);
                    break;
                case 7:
                    saveDataFile(DJ_FILE, djsList);
                    saveBookingsFile(BOOKING_FILE, bookingsList);
                    saveWaitingListFile(WAITING_LIST_FILE, waitingList);
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please select an option from 1 to 7.");
            }
        }
        saveBookingsFile(BOOKING_FILE, bookingsList);
        saveWaitingListFile(WAITING_LIST_FILE, waitingList);


    }

  private static void scheduleDeejay(Scanner scanner) {
      System.out.print("Enter customer name: ");
      String customerName = scanner.nextLine();

      System.out.print("Enter date and time (MM/DD/YYYY HH:mm): ");
      String dateTimeStr = scanner.nextLine();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

      System.out.print("Enter event duration (in hours, between 2 and 4): ");
      int eventDurationInput = scanner.nextInt();
      scanner.nextLine(); 

      String dj = chooseAvailableDj(dateTime, eventDurationInput, scanner);
      if (dj != null) {
          bookingsList.add(new Booking(dj, dateTime, customerName, eventDurationInput));

          djBookingCounts.put(dj, djBookingCounts.get(dj) + 1);

          System.out.println("Booking successful for " + dj);
          fulfillWaitingListRequest(dj, eventDurationInput);
          saveBookingsFile(BOOKING_FILE, bookingsList);

          updateBookingCounts(dateTime, eventDurationInput);

      } else {
          System.out.println("No available DJs for the specified duration. Adding to the waiting list.");

          waitingList.offer(new Request(customerName, dateTime, eventDurationInput));
          saveWaitingListFile(WAITING_LIST_FILE, waitingList);
      }
  }




  private static void updateBookingCounts(LocalDateTime dateTime, int eventDuration) {
      if (djBookingCounts.isEmpty()) {
          for (String dj : djsList) {
              djBookingCounts.put(dj, 0);
          }
      }

      for (Booking booking : bookingsList) {
          LocalDateTime bookingEnd = booking.getDateTime().plusHours(booking.getEventDuration());
          if (booking.getDateTime().isBefore(dateTime.plusHours(eventDuration)) &&
              bookingEnd.isAfter(dateTime)) {
              String dj = booking.getDj();
              djBookingCounts.put(dj, djBookingCounts.get(dj) + 1);
          }
      }

      System.out.println("DJ Booking Counts: " + djBookingCounts);
  }

















  private static String chooseAvailableDj(LocalDateTime dateTime, int eventDuration, Scanner scanner) {
      updateBookingCounts(dateTime, eventDuration);

      String chosenDj = null;
      int minBookings = Integer.MAX_VALUE;

      for (Map.Entry<String, Integer> entry : djBookingCounts.entrySet()) {
          String dj = entry.getKey();

          if (isDjAvailable(dj, dateTime, eventDuration)) {
              System.out.println("DJ " + dj + " is available for the requested duration.");
              if (entry.getValue() < minBookings) {
                  chosenDj = dj;
                  minBookings = entry.getValue();
              }
          } else {
              System.out.println("DJ " + dj + " is NOT available for the requested duration.");
          }
      }

      System.out.println("Chosen DJ: " + chosenDj + ", Min Bookings: " + minBookings);

      if (chosenDj != null) {
          return chosenDj;
      } else {
          System.out.println("No available DJs for the specified duration. Adding to the waiting list.");
          saveWaitingListFile(WAITING_LIST_FILE, waitingList);
          return null;
      }
  }

  private static void fulfillCancelledBookingsRequest(ArrayList<Booking> cancelledBookings, int eventDuration) {
      for (Booking booking : cancelledBookings) {
          boolean isAvailable = false;

          if (djsList.isEmpty()) {
              System.out.println("No available DJs. Adding to the front of the waiting list queue.");
              waitingList.offer(new Request(booking.getCustomerName(), booking.getDateTime(), eventDuration));
          } else {
              for (String dj : djsList) {
                  isAvailable = isDjAvailable(dj, booking.getDateTime(), eventDuration);
                  if (isAvailable) {
                      bookingsList.add(new Booking(dj, booking.getDateTime(), booking.getCustomerName(), eventDuration));
                      System.out.println("Booking successful for DJ " + dj + " from the waiting list.");
                      break; 
                  }
              }
          }

        if (!isAvailable) {
                    System.out.println("No available DJs for the waiting list request. Adding to the front of the waiting list queue.");

                    
                    boolean isAlreadyInWaitingList = waitingList.stream()
                            .anyMatch(request -> request.getCustomerName().equals(booking.getCustomerName())
                                    && request.getDateTime().equals(booking.getDateTime())
                                    && request.getEventDuration() == eventDuration);

                    if (!isAlreadyInWaitingList) {
                        waitingList.offer(new Request(booking.getCustomerName(), booking.getDateTime(), eventDuration));
                    }
                }
            }

            waitingList = new LinkedList<>(rotateWaitingList(cancelledBookings.size()));
            saveBookingsFile(BOOKING_FILE, bookingsList);
            saveWaitingListFile(WAITING_LIST_FILE, waitingList);
        }





  private static void fulfillWaitingListRequest(String dj, int eventDuration) {
      if (!waitingList.isEmpty()) {
          List<Request> tempWaitingList = new ArrayList<>();

          while (!waitingList.isEmpty()) {
              Request waitingBooking = waitingList.poll();

              boolean isAvailable = isDjAvailable(dj, waitingBooking.getDateTime(), eventDuration);

              if (isAvailable) {
                  bookingsList.add(new Booking(dj, waitingBooking.getDateTime(), waitingBooking.getCustomerName(), eventDuration));

                  djBookingCounts.put(dj, djBookingCounts.get(dj) + 1);

                  System.out.println("Booking successful for DJ " + dj + " from the waiting list.");
              } else {
                 
                  tempWaitingList.add(waitingBooking);
              }
          }

          updateBookingCounts(LocalDateTime.now(), 0);

          waitingList.addAll(tempWaitingList);

          saveWaitingListFile(WAITING_LIST_FILE, waitingList);
          saveBookingsFile(BOOKING_FILE, bookingsList);
      }
  }










  private static boolean isDjAvailable(String dj, LocalDateTime dateTime, int eventDuration) {
      for (Booking booking : bookingsList) {
          if (booking.getDj().equals(dj) &&
              !dateTime.plusHours(eventDuration).isBefore(booking.getDateTime()) &&
              !booking.getDateTime().plusHours(booking.getEventDuration()).isBefore(dateTime)) {
              return false;
          }
      }
      return true; 
  }









  private static void cancelBooking(Scanner scanner) {
      System.out.print("Enter customer name: ");
      String customerName = scanner.nextLine();

      System.out.print("Enter date and time (MM/DD/YYYY HH:mm): ");
      String dateTimeStr = scanner.nextLine();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

      System.out.print("Enter event duration (in hours, between 2 and 4): ");
      int eventDuration = scanner.nextInt();
      scanner.nextLine(); 

      Booking canceledBooking = removeBooking(customerName, dateTime, eventDuration);

      saveBookingsFile(BOOKING_FILE, bookingsList);

      if (canceledBooking != null) {
          System.out.println("Booking for " + customerName + " on " + dateTime + " (for " + eventDuration + " hours) is canceled.");
          fulfillWaitingListRequest(canceledBooking.getDj(), canceledBooking.getEventDuration());

      } else {
          System.out.println("Booking not found for " + customerName + " on " + dateTime + " (for " + eventDuration + " hours).");
      }
  }


  private static Booking removeBooking(String customerName, LocalDateTime dateTime, int eventDuration) {
      Iterator<Booking> iterator = bookingsList.iterator();
      while (iterator.hasNext()) {
          Booking booking = iterator.next();
          if (booking.getCustomerName().equals(customerName) && booking.getDateTime().equals(dateTime) && booking.getEventDuration() == eventDuration) {
              iterator.remove();
              return booking;
          }
      }
      return null;
  }





  private static ArrayList<Request> rotateWaitingList(int numOfRequestRemoved) {
      List<Request> myList = new ArrayList<>(waitingList);
      Collections.rotate(myList, numOfRequestRemoved); 
      return new ArrayList<>(myList);
  }





  private static void signupDeejay(Scanner scanner, int eventDuration) {
      System.out.print("Enter the name of the new deejay: ");
      String newDj = scanner.nextLine();

      djsList.add(newDj);
      saveDataFile(DJ_FILE, djsList);
      System.out.println("New deejay " + newDj + " has signed up.");

      djBookingCounts.put(newDj, 0);

      fulfillWaitingListRequest(newDj, eventDuration);
  }

  private static void dropoutDeejay(Scanner scanner) {
      System.out.print("Enter the name of the deejay who is dropping out: ");
      String droppedDj = scanner.nextLine();

      ArrayList<Booking> canceledBookings = new ArrayList<>();

      Iterator<Booking> iterator = bookingsList.iterator();
      while (iterator.hasNext()) {
          Booking booking = iterator.next();
          if (booking.getDj().equals(droppedDj)) {
              canceledBookings.add(booking);
              System.out.println(canceledBookings);
              iterator.remove(); 
          }
      }

      saveBookingsFile(BOOKING_FILE, bookingsList);

      djsList.remove(droppedDj);
      saveDataFile(DJ_FILE, djsList);

      if (!canceledBookings.isEmpty()) {
          System.out.println("Bookings for deejay " + droppedDj + " are canceled.");
          fulfillCancelledBookingsRequest(canceledBookings, eventDuration);
      } else {
          System.out.println("No bookings found for deejay " + droppedDj + ".");
      }
  }


    private static void showDeejayStatus(Scanner scanner) {
        System.out.print("Enter the name of the deejay: ");
        String deejay = scanner.nextLine();

        System.out.println("Bookings for Deejay " + deejay + ":");
        for (Booking booking : bookingsList) {
            if (booking.getDj().equals(deejay)) {
                System.out.println(booking.toStringForFile());
            }
        }
    }

  private static void showDateStatus(Scanner scanner) {
      System.out.print("Enter the date (MM/DD): ");
      String date = scanner.nextLine();

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
      System.out.println("Bookings for Date " + date + ":");
      for (Booking booking : bookingsList) {
          if (booking.getDateTime().format(formatter).equals(date)) {
              System.out.println(booking.toStringForFile());
          }
      }
  }


  private static ArrayList<String> readDataFile(String filename) {
      ArrayList<String> djsList = new ArrayList<>();
      try (Scanner scanner = new Scanner(new File(filename))) {
          while (scanner.hasNextLine()) {
              String dj = scanner.nextLine().trim();  
              if (!dj.isEmpty()) {
                  djsList.add(dj);
              }
          }
      } catch (IOException e) {
          System.out.println("Could not read DJ data from file: " + filename);
      }
      return djsList;
  }

  private static ArrayList<Booking> readBookingsFile(String filename) {
      ArrayList<Booking> bookingsList = new ArrayList<>();
      try (Scanner scanner = new Scanner(new File(filename))) {
          while (scanner.hasNextLine()) {
              String line = scanner.nextLine();
              String[] parts = line.split(",");
              if (parts.length == 3) {
                  String customerName = parts[0];
                  String dj = parts[1];
                  String dateTimeStr = parts[2];
                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
                  LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
                  bookingsList.add(new Booking(dj, dateTime, customerName, eventDuration));
              }
          }
      } catch (IOException e) {
          System.out.println("Could not read booking data from file: " + filename);
      }
      return bookingsList;
  }



    private static Queue<Request> readWaitingListFile(String filename) {
        Queue<Request> waitingList = new LinkedList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String customerName = parts[0];
                    String[] date = parts[1].split("/");
                    int month = Integer.parseInt(date[0]);
                    int day = Integer.parseInt(date[1]);
                    int eventHour = Integer.parseInt(parts[2]);

                    int currentYear = LocalDateTime.now().getYear();
                    LocalDateTime eventDateTime = LocalDateTime.of(currentYear, month, day, eventHour, 0);

                    waitingList.add(new Request(customerName, eventDateTime, eventHour));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read waiting list data from file: " + filename);
        }
        return waitingList;
    }





    private static void saveDataFile(String filename, ArrayList<String> data) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            for (String item : data) {
                writer.println(item);
            }
        } catch (IOException e) {
            System.out.println("Could not write DJ data to file: " + filename);
        }
    }

  private static void saveBookingsFile(String filename, ArrayList<Booking> bookingsList) {
      try (PrintWriter writer = new PrintWriter(filename)) {
          for (Booking booking : bookingsList) {
              writer.println(booking.toStringForFile());
          }
      } catch (IOException e) {
          System.out.println("Could not write booking data to file: " + filename);
      }
  }


    private static void saveWaitingListFile(String filename, Queue<Request> waitingList) {
        try (PrintWriter writer = new PrintWriter(filename)) {
            for (Request request : waitingList) {
                writer.println(request.toStringForFile());
            }
        } catch (IOException e) {
            System.out.println("Could not write waiting list data to file: " + filename);
        }
    }
}