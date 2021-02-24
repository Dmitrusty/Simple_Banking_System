package banking;

import org.sqlite.SQLiteDataSource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        User user = new User();

        if (args.length == 0) {
            System.out.print("\nMissing command line arguments!\nAnykey: >");
            scanner.nextLine();
        }

        String fileName = "";
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-fileName":
                    fileName = args[i + 1];
                    break;
                default:
                    System.out.print("\nIllegal command line arguments!\nAnykey: >");
                    scanner.nextLine();
            }
        }

        if (fileName.equals("")) {
            System.out.println("db filename is missing!\nAnykey: >");
            scanner.nextLine();
        } else {
            Bank.url = "jdbc:sqlite:" + fileName;
            if (!Files.isRegularFile(Paths.get(fileName))) {
                createCardsDatabase();
            }
        }

        mainMenu:
        for (; ; ) {
            System.out.print("1. Create an account\r\n" +
                    "2. Log into account\r\n" +
                    "0. Exit\r\n" +
                    ">");

            switch (scanner.nextInt()) {
                case 1:
                    printNewAccount(Bank.createAccount());
                    break;
                case 2:
                    user.logIn(showLogInDialogue());
                    if (user.isLoggedIn()) {
                        System.out.println("\n\rYou have successfully logged in!\n\r");
                        int returnTo = showCardDialogue(user.getCard());
                        user.logOut();
                        System.out.println("\n\rYou have successfully logged out!\n\r");
                        if (returnTo == 0) break mainMenu;
                    }
                    break;
                case 0:
                    break mainMenu;
                default:
                    System.out.println("Wrong number!\r\nTry again...\r\n\n");
            }
        }
        System.out.println("\r\nBye!");
    }


    public static void createCardsDatabase() {

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(Bank.url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER," +
                        "number TEXT," +
                        "pin TEXT," +
                        "balance INTEGER DEFAULT 0" +
                        ")");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void printNewAccount(Card card) {
        System.out.println("\n\rYour card has been created\n\rYour card number:");
        System.out.printf("%016d%n", card.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.printf("%04d%n%n", card.getPin());
    }


    public static Card showLogInDialogue() {
        System.out.print("\n\rEnter your card number:\n\r>");
        long cardNumber = scanner.nextLong();
        System.out.print("Enter your PIN:\n\r>");
        long pin = scanner.nextLong();

        Card card = new Card().setCardNumber(cardNumber).setPin(pin);

        if (Bank.getCardsFromDB().contains(card)) {
            return card;
        } else {
            System.out.println("\n\rWrong card number or PIN!\r\n");
            return null;
        }
    }

    public static int showCardDialogue(Card card) {
        for (; ; ) {
            System.out.print("\n1. Balance" +
                    "\n2. Add income" +
                    "\n3. Do transfer" +
                    "\n4. Close account" +
                    "\n5. Log out" +
                    "\n0. Exit" +
                    "\n>");

            switch (scanner.nextInt()) {
                case 2:
                    showAddIncomeDialogue(card);
                case 1:
                    System.out.println("\nBalance: " + card.getBalanceFromDB() + "\n");
                    break;
                case 3:
                    doTransferDialogue(card);
                    break;
                case 4:
                    card.deleteCardFromDB();
                    System.out.println("\nThe account has been closed!");
                    break;
                case 5:
                    return 2;
                case 0:
                    return 0;
                default:
                    System.out.println("Wrong number!\r\nTry again...\r\n\n");
            }
        }
    }

    public static void showAddIncomeDialogue(Card card) {
        double amount;
        for (; ; ) {
            System.out.print("Enter the amount:\n>");
            amount = scanner.nextInt();

            if (amount >= 0) break;
            else {
                System.out.println("Wrong number!\r\nTry again...\r\n\n");
            }
        }
        card.addBalanceToDB(amount);
    }


    public static void doTransferDialogue(Card card) {
        long targetCardNumber;
        double moneyAmount = 0;


        targetCardNumber = showEnterTargetCardNumberDialogue(card);
        if (targetCardNumber != 0) {
            moneyAmount = showEnterMoneyAmountDialogue(card);
        }

        if ((targetCardNumber != 0) && (moneyAmount != 0)) {
            // transaction
            Card targetCard;
            targetCard = Bank.pullCardFromDB(targetCardNumber);
            card.addBalanceToDB(-moneyAmount);
            targetCard.addBalanceToDB(moneyAmount);

            System.out.println("\nThe funds are transferred successfully.");
        }
    }


    public static long showEnterTargetCardNumberDialogue(Card card) {
        long targetCardNumber;

        for (; ; ) {
            System.out.print("\nEnter number of the card to which the funds are to be transferred," +
                    "\nor 0 to cancel the operation:\n>");
            targetCardNumber = scanner.nextLong();

            if (targetCardNumber == 0) {
                break;
            }
            if (targetCardNumber == card.getCardNumber()) {
                System.out.println("You can't transfer money to the same account!");
                targetCardNumber = 0;
                break;
            }
            if (!Bank.isPassedLuhn(targetCardNumber)) {
                System.out.println("Probably you made mistake in the card number. Please try again!");
                continue;
            }
            if (!Bank.cardIsInDB(targetCardNumber)) {
                System.out.println("Such a card does not exist.");
                targetCardNumber = 0;
                break;
            }
            break;
        }
        return targetCardNumber;
    }


    public static double showEnterMoneyAmountDialogue(Card card) {
        double moneyAmount;

        for (; ; ) {
            System.out.print("\nEnter how much money you want to transfer," +
                    "\nor 0 to cancel the operation:\n>");
            moneyAmount = scanner.nextDouble();

            if (moneyAmount < 0) {
                System.out.println("Please, enter positive number.");
                continue;
            }
            if (moneyAmount > card.getBalanceFromDB()) {
                System.out.println("Not enough money!");
                moneyAmount = 0;
                break;
            }
            break;
        }
        return moneyAmount;
    }
}

