package banking;

public class User {
    private Card card;
    private Boolean isLoggedIn;

    public Card getCard() {
        return card;
    }

    public void logIn(Card targetCard) {
        if (targetCard != null) {
            card = Bank.pullCardFromDB(targetCard.getCardNumber());
            isLoggedIn = true;
        }
    }

    public void logOut() {
        card = null;
        isLoggedIn = false;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public User() {
        card = null;
        isLoggedIn = false;
    }
}
