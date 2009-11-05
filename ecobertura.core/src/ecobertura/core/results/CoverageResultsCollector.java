package ecobertura.core.results;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;

import ecobertura.core.cobertura.CoberturaWrapper;
import ecobertura.core.cobertura.ICoberturaWrapper;

public class CoverageResultsCollector implements IDebugEventSetListener {
	
	private static final Logger logger = Logger.getLogger("ecobertura.core.results");
	
	private final List<CoverageResultsListener> listeners = new Vector<CoverageResultsListener>();
	private ILaunch currentLaunch;
	
	public static CoverageResultsCollector collect() {
		return new CoverageResultsCollector();
	}
	
	private CoverageResultsCollector() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	@Override
	public void handleDebugEvents(final DebugEvent[] events) {
		for (DebugEvent event : events) {
			if (isCoverageLaunchTerminationEvent(event)) {
				logger.fine("detected termination of covered launch");
				notifyListeners(retrieveCoverageData());
			}
		}
	}
	
	private boolean isCoverageLaunchTerminationEvent(DebugEvent event) {
		if (!isLaunchTerminationEvent(event)) {
			return false;
		}
		final ILaunch launch = ((IProcess) event.getSource()).getLaunch();
		return launch == currentLaunch;
	}

	private boolean isLaunchTerminationEvent(final DebugEvent event) {
		return event.getSource() instanceof IProcess 
				&& event.getKind() == DebugEvent.TERMINATE;
	}

	private ProjectData retrieveCoverageData() {
		return CoberturaWrapper.get().projectDataFromFile(
				ICoberturaWrapper.DEFAULT_COBERTURA_FILENAME);
	}

	private void notifyListeners(final ProjectData projectData) {
		for (CoverageResultsListener listener : listeners) {
			listener.coverageRunCompleted(projectData);
		}
	}

	public void coveredLaunchStarted(final ILaunch launch) {
		this.currentLaunch = launch;
	}
	
	public void addListener(final CoverageResultsListener listener) {
		listeners.add(listener);
	}

	public void removeListener(final CoverageResultsListener listener) {
		listeners.remove(listener);
	}

}
