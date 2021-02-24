package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Card {
    private Long cardNumber;
    private Long pin;
    private Double balance;

    public Card() {
        this.balance = 0.0;
    }

    public Long getCardNumber() {
        return cardNumber;
    }

    public Long getPin() {
        return pin;
    }

    public Double getBalance() {
        return balance;
    }

    public Card setCardNumber(Long cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public Card setPin(Long pin) {
        this.pin = pin;
        return this;
    }

    public Card setBalance(Double balance) {
        this.balance = balance;
        return this;
    }

    public Double getBalanceFromDB() {
        String getBalanceQuery = "SELECT balance FROM card WHERE number LIKE ?";
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(Bank.url);

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(getBalanceQuery);
            preparedStatement.setString(1, cardNumber.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                balance = (double) resultSet.getInt("balance");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    public void addBalanceToDB(double amount) {
        String addBalanceQuery = "UPDATE card SET balance = balance + ? WHERE number = ?";
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(Bank.url);

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(addBalanceQuery);
            preparedStatement.setString(1, String.valueOf(amount));
            preparedStatement.setString(2, cardNumber.toString());
            try {
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteCardFromDB() {
        String deleteCardQuery = "DELETE FROM card WHERE number = ?";
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(Bank.url);
        boolean returnValue = false;

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(deleteCardQuery);
            preparedStatement.setString(1, cardNumber.toString());
            try {
                preparedStatement.executeUpdate();
                returnValue = true;
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

/*    public void makeTransactionInDB(long targetCardNumber, double moneyAmount){

    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return cardNumber.equals(card.cardNumber) &&
                pin.equals(card.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNumber, pin);
    }
}
