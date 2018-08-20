package town.lost.examples.exchange.api;

public enum CurrencyPair {
    USDXCL(Currency.USD, Currency.XCL),
    EURXCL(Currency.EUR, Currency.XCL),
    GBPXCL(Currency.GBP, Currency.XCL),
    CHFXCL(Currency.CHF, Currency.XCL),
    KRWXCL(Currency.KRW, Currency.XCL);

    private final Currency ccy1;
    private final Currency ccy2;

    CurrencyPair(Currency ccy1, Currency ccy2) {
        this.ccy1 = ccy1;
        this.ccy2 = ccy2;
    }

    public Currency ccy1() {
        return ccy1;
    }

    public Currency ccy2() {
        return ccy2;
    }
}
