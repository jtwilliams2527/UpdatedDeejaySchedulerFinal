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

    public static void main(String[] args) {
        System.out.println("Welcome to DJ Scheduler!");

        djsList = readDataFile(DJ_FILE);
        bookingsList = readBookingsFile(BOOKING_FILE);
        waitingList = readWaitingListFile(WAITING_LIST_FILE);

        

        Scanner scanner = new Scanner(System.in);
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
            scanner.nextLine(); // Consume newline

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
    }

  private static void scheduleDeejay(Scanner scanner) {
      System.out.print("Enter customer name: ");
      String customerName = scanner.nextLine();

      System.out.print("Enter date and time (MM/DD/YYYY HH:mm): ");
      String dateTimeStr = scanner.nextLine();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

      // Get the event duration from the user
      System.out.print("Enter event duration (in hours, between 2 and 4): ");
      eventDuration = scanner.nextInt();
      scanner.nextLine(); // Consume newline


      // Check if the selected DJ is available for the given duration
      String dj = chooseAvailableDj(dateTime, eventDuration);
      if (dj != null) {
          bookingsList.add(new Booking(dj, dateTime, customerName, eventDuration));
          System.out.println("Booking successful for " + dj);
          fulfillWaitingListRequest(dj, eventDuration);

          saveBookingsFile(BOOKING_FILE, bookingsList);
      } else {
          System.out.println("No available DJs for the specified duration. Adding to the waiting list.");
          waitingList.offer(new Request(customerName, dateTime, eventDuration));
          saveWaitingListFile(WAITING_LIST_FILE, waitingList);
      }
  }









  private static String chooseAvailableDj(LocalDateTime dateTime, int eventDuration) {
      Map<String, Integer> djBookingCounts = new HashMap<>();

      // Initialize the counts for all DJs to 0
      for (String dj : djsList) {
          djBookingCounts.put(dj, 0);
      }

      // Count the number of overlapping bookings for each DJ
      for (Booking booking : bookingsList) {
          if (booking.getDateTime().isBefore(dateTime.plusHours(eventDuration)) &&
                  booking.getDateTime().plusHours(booking.getEventDuration()).isAfter(dateTime)) {
              String dj = booking.getDj();
              djBookingCounts.put(dj, djBookingCounts.get(dj) + 1);
          }
      }

      // Find the DJ with the least number of bookings
      String chosenDj = null;
      int minBookings = Integer.MAX_VALUE;

      for (Map.Entry<String, Integer> entry : djBookingCounts.entrySet()) {
          if (entry.getValue() < minBookings && isDjAvailable(entry.getKey(), dateTime, eventDuration)) {
              chosenDj = entry.getKey();
              minBookings = entry.getValue();
          }
      }

      return chosenDj;
  }






  private static void fulfillCancelledBookingsRequest(ArrayList<Booking> cancelledBookings, int eventDuration) {
      for (Booking booking : cancelledBookings) {
          if (djsList.isEmpty()) {
              System.out.println("No available DJs. Adding to the front of waiting list queue.");
              waitingList.offer(new Request(booking.getCustomerName(), booking.getDateTime(), eventDuration));
          }
          for (String dj : djsList) {
              boolean isAvailable = isDjAvailable(dj, booking.getDateTime(), eventDuration);
              if (isAvailable) {
                  bookingsList.add(new Booking(dj, booking.getDateTime(), booking.getCustomerName(), eventDuration));
                  System.out.println("Booking successful for DJ " + dj + " from the waiting list.");
              } else {
                  System.out.println("No available DJs for the waiting list request. Adding to the front of waiting list queue.");
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
          while (!waitingList.isEmpty()) {
              Request waitingBooking = waitingList.poll();

              // Check if the selected DJ is available for the given date and time
              String chosenDj = chooseAvailableDj(waitingBooking.getDateTime(), waitingBooking.getEventDuration());


              if (chosenDj != null) {
                  bookingsList.add(new Booking(chosenDj, waitingBooking.getDateTime(), waitingBooking.getCustomerName(), eventDuration));
                  System.out.println("Booking successful for DJ " + chosenDj + " from the waiting list.");
              } else {
                  // No available DJs for the waiting list request. Adding back to the waiting list.
                  System.out.println("No available DJs for the waiting list request. Adding to the front of the waiting list queue.");
                  waitingList.offer(waitingBooking);
              }
          }
          saveBookingsFile(BOOKING_FILE, bookingsList);
          saveWaitingListFile(WAITING_LIST_FILE, waitingList);
      }
  }




  private static boolean isDjAvailable(String dj, LocalDateTime dateTime, int eventDuration) {
      // Check if the DJ is available for the specified duration
      for (Booking booking : bookingsList) {
          if (booking.getDj().equals(dj) && booking.getDateTime().isBefore(dateTime.plusHours(eventDuration)) && booking.getDateTime().plusHours(booking.getEventDuration()).isAfter(dateTime)) {
              return false; // The DJ is not available for the requested duration
          }
      }
      return true; // The DJ is available for the requested duration
  }




  



  private static void cancelBooking(Scanner scanner) {
      System.out.print("Enter customer name: ");
      String customerName = scanner.nextLine();

      System.out.print("Enter date and time (MM/DD/YYYY HH:mm): ");
      String dateTimeStr = scanner.nextLine();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
      LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

      // Get the event duration from the user
      System.out.print("Enter event duration (in hours, between 2 and 4): ");
      int eventDuration = scanner.nextInt();
      scanner.nextLine(); // Consume newline

      // Remove the booking if found
      Booking canceledBooking = removeBooking(customerName, dateTime, eventDuration);

      saveBookingsFile(BOOKING_FILE, bookingsList);

      if (canceledBooking != null) {
          System.out.println("Booking for " + customerName + " on " + dateTime + " (for " + eventDuration + " hours) is canceled.");
          // Try to fulfill a request from the waiting list
          fulfillWaitingListRequest(canceledBooking.getDj(), canceledBooking.getEventDuration());

      } else {
          System.out.println("Booking not found for " + customerName + " on " + dateTime + " (for " + eventDuration + " hours).");
      }
  }


  // Helper method to remove a booking
  private static Booking removeBooking(String customerName, LocalDateTime dateTime, int eventDuration) {
      for (Booking booking : bookingsList) {
          if (booking.getCustomerName().equals(customerName) && booking.getDateTime().equals(dateTime) && booking.getEventDuration() == eventDuration) {
              bookingsList.remove(booking);
              return booking;
          }
      }
      return null;
  }



    // if multiple bookings cancelled, they are added to bottom, and moved to top by rotation
    private static ArrayList<Request> rotateWaitingList(int numOfRequestRemoved){ // turns waitlist into list that can be rotated
        java.util.List<String> myList = new ArrayList(waitingList);
        java.util.Collections.rotate(myList, numOfRequestRemoved); // rotates things from bottom to top
        return (ArrayList<Request>) new ArrayList(myList);
    }


  

    private static void signupDeejay(Scanner scanner, int eventDuration) {
        System.out.print("Enter the name of the new deejay: ");
        String newDj = scanner.nextLine();
        djsList.add(newDj);
        saveDataFile(DJ_FILE, djsList);
        System.out.println("New deejay " + newDj + " has signed up.");

        // Try to fulfill waiting list requests with the new deejay
        fulfillWaitingListRequest(newDj, eventDuration);

    }

    private static void dropoutDeejay(Scanner scanner) {
        System.out.print("Enter the name of the deejay who is dropping out: ");
        String droppedDj = scanner.nextLine();

        // Cancel all bookings for the dropped deejay
        ArrayList<Booking> canceledBookings = new ArrayList<>();
        for (Booking booking : bookingsList) {
            if (booking.getDj().equals(droppedDj)) {
                canceledBookings.add(booking);
            }
        }

        for (Booking canceledBooking : canceledBookings) {
            bookingsList.remove(canceledBooking);
        }
        saveBookingsFile(BOOKING_FILE,bookingsList);

        // Remove the dropped deejay from the list of deejays
        djsList.remove(droppedDj);
        saveDataFile(DJ_FILE, djsList);

        if (!canceledBookings.isEmpty()) {
            System.out.println("Bookings for deejay " + droppedDj + " are canceled.");
            // Try to fulfill waiting list requests
            //            fulfillWaitingListRequestFront();
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
                System.out.println(booking);
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
              System.out.println(booking);
          }
      }
  }


    private static ArrayList<String> readDataFile(String filename) {
        ArrayList<String> djsList = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                djsList.add(scanner.nextLine());
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
              if (parts.length == 5) {
                  String customerName = parts[0];
                  String dj = parts[1];
                  String[] date = parts[2].split("/");
                  int month = Integer.parseInt(date[0]);
                  int day = Integer.parseInt(date[1]);
                  String time = parts[3];
                  int eventHour = Integer.parseInt(time);
                  bookingsList.add(new Booking(parts[1], LocalDateTime.of(2023, month, day, eventHour, 0), parts[0], eventHour));
  

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
                  waitingList.add(new Request(customerName, LocalDateTime.of(2023, month, day, eventHour, 0), eventHour));
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