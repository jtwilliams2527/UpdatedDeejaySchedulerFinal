import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


public class DJScheduler {
    private static final String DJ_FILE = "djs.txt";
    private static final String BOOKING_FILE = "bookings.txt";
    private static final String WAITING_LIST_FILE = "waitinglist.txt";
    private static ArrayList<String> djsList;
    private static ArrayList<Booking> bookingsList;
    private static Queue<Request> waitingList;

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
                    signupDeejay(scanner);
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

        System.out.print("Enter date (MM/DD): ");
        String dateStr = scanner.nextLine();
        String[] dateParts = dateStr.split("/");
        if (dateParts.length != 2) {
            System.out.println("Invalid date format. Please use MM/DD format.");
            return;
        }

        int month = Integer.parseInt(dateParts[0]);
        int day = Integer.parseInt(dateParts[1]);

        System.out.print("Enter time slot (1 for afternoon, 2 for evening, 3 for night): ");
        int timeSlot = scanner.nextInt();
        String timeSlot_s = getTimeOfDay(timeSlot);
        scanner.nextLine(); // Consume the newline character

        // Check if the selected DJ is available for the given date and time slot
        for (Booking booking : bookingsList) {
            if (booking.isOnSameDay(month, day) && isDjAvailable(booking.getDj(), month, day, timeSlot_s)) {
                // Add the time slot to the existing booking
                booking.addTimeSlot(timeSlot_s);
                bookingsList.add(new Booking(booking.getDj(), month, day, timeSlot_s, customerName));
                System.out.println("Booking successful for DJ " + booking.getDj());
                saveBookingsFile(BOOKING_FILE, bookingsList); // Save updated bookings to file
                return;
            }
        }

        // If no existing booking can accommodate the request, create a new booking
        for (String dj : djsList) {
            boolean isAvailable = isDjAvailable(dj, month, day, timeSlot_s);
            if (isAvailable) {
                bookingsList.add(new Booking(dj, month, day, timeSlot_s, customerName));
                System.out.println("Booking successful for " + dj);
                saveBookingsFile(BOOKING_FILE, bookingsList); // Save updated bookings to file
                return;
            }
        }

        // If no DJ is available, add the request to the waiting list
        System.out.println("No available DJs for the requested time slot. Adding to waiting list.");
        waitingList.add(new Request(customerName, month, day, timeSlot_s));
        saveWaitingListFile(WAITING_LIST_FILE, waitingList); // Save updated waiting list to file
    }

    private static String getTimeOfDay(int timeSlot){
        return switch (timeSlot) {
            case 1 -> "Afternoon";
            case 2 -> "Evening";
            case 3 -> "Night";
            default -> "";
        };
    }

    private static boolean isDjAvailable(String dj, int month, int day, String timeSlot) {
        for (Booking booking : bookingsList) {
            if (booking.getDj().equals(dj) && booking.isOnSameDay(month, day)) {
                // Check if the requested time slot is adjacent to any existing time slots for the DJ
                List<String> existingTimeSlots = booking.getTimeSlots();
                boolean isAdjacent = false;

                for (String existingSlot : existingTimeSlots) {
                    if (areTimeSlotsAdjacent(existingSlot, timeSlot)) {
                        isAdjacent = true;
                        break; // No need to continue checking if already found an adjacent slot
                    }
                }

                if (!isAdjacent) {
                    return false; // The DJ is not available for the requested time slot
                }
            }
        }
        return true; // The DJ is available for the requested time slot
    }

    private static boolean areTimeSlotsAdjacent(String timeSlot1, String timeSlot2) {
        // Define the order of time slots
        String[] timeSlotOrder = {"Afternoon", "Evening", "Night"};

        // Find the index of each time slot in the order
        int index1 = -1;
        int index2 = -1;
        for (int i = 0; i < timeSlotOrder.length; i++) {
            if (timeSlotOrder[i].equalsIgnoreCase(timeSlot1)) {
                index1 = i;
            }
            if (timeSlotOrder[i].equalsIgnoreCase(timeSlot2)) {
                index2 = i;
            }
        }

        // Check if the timeSlot2 comes immediately after timeSlot1
        return index1 != -1 && index2 != -1 && Math.abs(index2 - index1) == 1;
    }

    private static void cancelBooking(Scanner scanner) {
        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine();

        System.out.print("Enter date (MM/DD): ");
        String date = scanner.nextLine();

        System.out.print("Enter time slot (1 = afternoon, 2 = evening, 3 = night): ");
        int timeSlot = scanner.nextInt();
        String strTimeSlot = getTimeOfDay(timeSlot);

        Booking canceledBooking = null;

        for (Booking booking : bookingsList) {
            if (booking.getCustomerName().equals(customerName) && booking.getDate().equals(date) && booking.getTimeSlot().equals(strTimeSlot)) {
                canceledBooking = booking;
                bookingsList.remove(booking);
                break;
            }
        }

        saveBookingsFile(BOOKING_FILE, bookingsList);

        if (canceledBooking != null) {
            System.out.println("Booking for " + customerName + " on " + date + " (" + timeSlot + ") is canceled.");
            // Try to fulfill a request from the waiting list
            fulfillWaitingListRequest();
        } else {
            System.out.println("Booking not found for " + customerName + " on " + date + " (" + timeSlot + ").");
        }
    }

    // if multiple bookings cancelled, they are added to bottom, and moved to top by rotation
    private static ArrayList<Request> rotateWaitingList(int numOfRequestRemoved){ // turns waitlist into list that can be rotated
        java.util.List<String> myList = new ArrayList(waitingList);
        java.util.Collections.rotate(myList, numOfRequestRemoved); // rotates things from bottom to top
        return (ArrayList<Request>) new ArrayList(myList);
    }


    private static void fulfillWaitingListRequest() {
        if (!waitingList.isEmpty()) {
            int count = waitingList.size();
            boolean bookingFound = false;
            while(count > 0) {
                count--;
                boolean addToWaitList = true;
                Request waitingBooking = waitingList.poll();
                for (String dj : djsList) {
                    boolean isAvailable = isDjAvailable(dj, waitingBooking.getMonth(), waitingBooking.getDay(), waitingBooking.getTimeSlot());
                    if (isAvailable && !bookingFound) {
                        bookingsList.add(new Booking(dj, waitingBooking.getMonth(), waitingBooking.getDay(), waitingBooking.getTimeSlot(), waitingBooking.getCustomerName()));
                        System.out.println("Booking successful for DJ " + dj + " from the waiting list.");
                        //return;
                        bookingFound = true;
                        addToWaitList = false;
                    }
                }
                if(addToWaitList) {
                    waitingList.offer(waitingBooking);
                }
            }
            saveBookingsFile(BOOKING_FILE, bookingsList); // Save updated bookings to file
            saveWaitingListFile(WAITING_LIST_FILE, waitingList);
        }
    }

    private static void fulfillWaitingListRequest(String dj) {
        if (!waitingList.isEmpty()) {
            int count = waitingList.size();
            System.out.println("count: " + count);
            while (count > 0) {
                count--;
                System.out.println("count1: " + count);
                Request waitingBooking = waitingList.poll();
                System.out.println("waitingBooking: " + waitingBooking);
                boolean isAvailable = isDjAvailable(dj, waitingBooking.getMonth(), waitingBooking.getDay(), waitingBooking.getTimeSlot());
                if (isAvailable) {
                    bookingsList.add(new Booking(dj, waitingBooking.getMonth(), waitingBooking.getDay(), waitingBooking.getTimeSlot(), waitingBooking.getCustomerName()));
                    System.out.println("Booking successful for DJ " + dj + " from the waiting list.");
                } else {
                    System.out.println("No available DJs for the waiting list request.");
                    waitingList.offer(waitingBooking);
                }
            }
            saveBookingsFile(BOOKING_FILE, bookingsList); // Save updated bookings to file
            saveWaitingListFile(WAITING_LIST_FILE, waitingList);
        }
    }


    private static void fulfillCancelledBookingsRequest(ArrayList<Booking> cancelledBookings) {
        for (Booking booking : cancelledBookings) {
            if (djsList.isEmpty()) {
                System.out.println("No available DJs. Adding to the front of waiting list queue.");
                waitingList.offer(new Request(booking.getCustomerName(), booking.getMonth(), booking.getDay(), booking.getTime()));
                //waitingList = new LinkedList<>(addToFrontOfWaitlist(new Request(booking.getCustomerName(), booking.getMonth(), booking.getDay(), booking.getTime())));
            }
            for (String dj : djsList) {
                boolean isAvailable = isDjAvailable(dj, booking.getMonth(), booking.getDay(), booking.getTimeSlot());
                if (isAvailable) {
                    bookingsList.add(new Booking(dj, booking.getMonth(), booking.getDay(), booking.getTimeSlot(), booking.getCustomerName()));
                    System.out.println("Booking successful for DJ " + dj + " from the waiting list.");
                }
                else {
                    System.out.println("No available DJs for the waiting list request. Adding to the front of waiting list queue.");
                    waitingList.offer(new Request(booking.getCustomerName(), booking.getMonth(), booking.getDay(), booking.getTime()));
                    //waitingList = new LinkedList<>(addToFrontOfWaitlist(new Request(booking.getCustomerName(), booking.getMonth(), booking.getDay(), booking.getTime())));
                }
            }

        }
        waitingList = new LinkedList<>(rotateWaitingList(cancelledBookings.size()));
        saveBookingsFile(BOOKING_FILE, bookingsList); // Save updated bookings to file
        saveWaitingListFile(WAITING_LIST_FILE, waitingList);
    }

    private static void signupDeejay(Scanner scanner) {
        System.out.print("Enter the name of the new deejay: ");
        String newDj = scanner.nextLine();
        djsList.add(newDj);
        saveDataFile(DJ_FILE, djsList);
        System.out.println("New deejay " + newDj + " has signed up.");

        // Try to fulfill waiting list requests with the new deejay
        fulfillWaitingListRequest(newDj);
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
            fulfillCancelledBookingsRequest(canceledBookings);
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

        System.out.println("Bookings for Date " + date + ":");
        for (Booking booking : bookingsList) {
            if (booking.getDate().equals(date)) {
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
                if (parts.length == 4) {
                    String customerName = parts[0];
                    String dj = parts[1];
                    String[] date = parts[2].split("/");
                    int month = 0;
                    int day = 0;
                    if (date.length == 2){
                        month = Integer.parseInt(date[0]); // Parse the month
                        day = Integer.parseInt(date[1]); // Parse the day
                    } else {
                        System.out.println("Invalid date format. Please use MM/DD format.");
                    }
                    String time = parts[3];
                    bookingsList.add(new Booking(dj, month, day, time, customerName));
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
                if (parts.length == 3) {
                    String customerName = parts[0];
                    String[] date = parts[1].split("/");
                    int month = 0;
                    int day = 0;
                    if (date.length == 2){
                        month = Integer.parseInt(date[0]); // Parse the month
                        day = Integer.parseInt(date[1]); // Parse the day
                    } else {
                        System.out.println("Invalid date format. Please use MM/DD format.");
                    }
                    String time = parts[2];
                    waitingList.add(new Request(customerName, month, day, time));
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