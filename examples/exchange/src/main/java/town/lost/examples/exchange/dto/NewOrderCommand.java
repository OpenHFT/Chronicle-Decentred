package town.lost.examples.exchange.dto;

import town.lost.examples.exchange.api.Currency;

public class NewOrderCommand extends VanillaSignedMessage {

    private boolean buy;
    private double quantity;
    private double maxPrice;
    private Currency currency1;
    private Currency currency2;
    private long timeToLive; // in milliseconds

    public NewOrderCommand() {

    }

    public NewOrderCommand(boolean buy, double quantity, double maxPrice, Currency currency1, Currency currency2, long timeToLive) {
        this.buy = buy;
        this.quantity = quantity;
        this.maxPrice = maxPrice;
        this.currency1 = currency1;
        this.currency2 = currency2;
        this.timeToLive = timeToLive;
    }

}
