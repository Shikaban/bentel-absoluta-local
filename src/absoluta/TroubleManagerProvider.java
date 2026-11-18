package absoluta;

import com.google.common.collect.ImmutableList;
import absoluta.spi.TroubleManagerProvider;

import absoluta.connection.PanelStatus;
import absoluta.connection.Trouble;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

class AbsolutaTroubleManagerProvider implements TroubleManagerProvider, PropertyChangeListener {
    private final AbsolutaPanelProvider panelProvider;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ImmutableList<String> troubles = ImmutableList.of();
    private boolean troublesInMemory; // True if at least one trouble is in memory only
    private boolean connected;        // True if the panel is connected

    AbsolutaTroubleManagerProvider(AbsolutaPanelProvider panelProvider) {
        this.panelProvider = Objects.requireNonNull(panelProvider);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        this.changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        this.changeSupport.removeChangeListener(listener);
    }

    @Override
    public synchronized ImmutableList<String> getTroubles() {
        return this.troubles;
    }

    @Override
    public void clean() {
        if (this.troublesInMemory && this.connected){
            this.panelProvider.cleanTroubles();
        }
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        PanelStatus panelStatus = (PanelStatus) evt.getSource();
        ImmutableList<String> oldTroubles = this.troubles;
        String propertyName = evt.getPropertyName();

        switch (propertyName) {
            case "TROUBLES":
            SortedSet<String> troubleSet = new TreeSet<>();
            this.troublesInMemory = false;

            // Update the trouble list and check whether at least one is memory-only
            for (Object obj : (Iterable<?>) evt.getNewValue()) {
                Trouble trouble = (Trouble) obj;
                troubleSet.add(trouble.toLocalizedString(panelStatus));
                this.troublesInMemory |= trouble.isInMemory();
            }
            this.troubles = ImmutableList.copyOf(troubleSet);
            break;

            case "CONNECTION_STATUS":
            PanelStatus.PanelConnStatus connStatus = (PanelStatus.PanelConnStatus) evt.getNewValue();
            this.connected = connStatus == PanelStatus.PanelConnStatus.CONNECTED;
            break;
        }

        // Notify listeners only if there was an actual change
        if (!oldTroubles.equals(this.troubles)) {
            this.changeSupport.fireChange();
        }
    }
}