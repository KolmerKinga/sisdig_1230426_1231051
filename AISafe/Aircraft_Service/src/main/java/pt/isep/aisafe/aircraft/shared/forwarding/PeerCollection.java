package pt.isep.aisafe.aircraft.shared.forwarding;

import java.util.ArrayList;
import java.util.List;

public class PeerCollection<T> {
    private final List<T> items = new ArrayList<>();
    private final List<String> failedPeers = new ArrayList<>();

    public void addItems(List<T> newItems) {
        if (newItems != null) {
            this.items.addAll(newItems);
        }
    }

    public void addFailedPeer(String peerName) {
        this.failedPeers.add(peerName);
    }

    public List<T> getItems() { return items; }
    public List<String> getFailedPeers() { return failedPeers; }
}
