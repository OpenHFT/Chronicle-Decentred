package town.lost.examples.appreciation.api;

import net.openhft.chronicle.bytes.MethodId;
import net.openhft.chronicle.decentred.api.SystemMessageListener;
import town.lost.examples.appreciation.dto.OnBalance;

public interface AppreciationResponses extends SystemMessageListener {
    @MethodId(0x3000)
    void onBalance(OnBalance onBalance);

}
