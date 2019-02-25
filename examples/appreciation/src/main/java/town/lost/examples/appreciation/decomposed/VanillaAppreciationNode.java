package town.lost.examples.appreciation.decomposed;

import net.openhft.chronicle.decentred.remote.rpc.RPCServer;
import town.lost.examples.appreciation.api.AppreciationMessages;
import town.lost.examples.appreciation.api.AppreciationRequests;
import town.lost.examples.appreciation.benchmark.Node;

public abstract class VanillaAppreciationNode extends Node<AppreciationMessages, AppreciationRequests> {

    private final RPCServer<AppreciationMessages, AppreciationRequests> rpcServer;

    protected VanillaAppreciationNode(long seed) {
        super(seed, AppreciationMessages.class, AppreciationRequests.class);
        rpcServer = createRpcServer();
    }

    protected abstract RPCServer<AppreciationMessages, AppreciationRequests> createRpcServer();

    @Override
    protected void close() {
        rpcServer.close();
    }
}
