package net.openhft.chronicle.decentred.dto.base.trait;

import net.openhft.chronicle.decentred.dto.base.VanillaSignedMessage;

public interface HasWeekNumber<T extends VanillaSignedMessage<T>> {

    /**
     * Returns the week number for this message.
     *
     * @return the week number for this message
     */
    int weekNumber();

    /**
     * Sets the week number for this message.
     * <p>
     * The provided {@code weekNumber} must be non-negative and
     * smaller or equal to 65,536
     *
     * @param weekNumber used for setting this message's week number
     *
     * @return this message including the newly updated
     * week number
     */
    T weekNumber(int weekNumber);
}
