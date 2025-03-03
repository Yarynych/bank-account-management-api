package ua.yarynych.apiaccountmanagement.entity.enums;

public enum Currency {
    UAH,
    USD,
    EUR;

    public static boolean isValid(String value) {
        for (Currency currency : values()) {
            if (currency.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
