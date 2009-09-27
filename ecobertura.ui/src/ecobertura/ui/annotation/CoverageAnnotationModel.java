package ecobertura.ui.annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.ui.texteditor.ITextEditor;

// TODO fire events to listeners
// TODO react to document/editor changes 
// TODO react to coverage changes

public class CoverageAnnotationModel implements IAnnotationModel {
	private static final Logger logger = Logger.getLogger("ecobertura.ui.annotation"); //$NON-NLS-1$

	static class Key {}
	static final Key MODEL_ID = new Key();
	
	private final List<IAnnotationModelListener> listeners = 
		new CopyOnWriteArrayList<IAnnotationModelListener>();
	
	private final List<CoverageAnnotation> annotations =
		new ArrayList<CoverageAnnotation>(64);
	
	public static void attachTo(final ITextEditor editor) {
		new AnnotationModelAttacher().attachTo(editor);
	}

	static CoverageAnnotationModel createForEditorDocument(
			final ITextEditor editor, final IDocument document) {
		return new CoverageAnnotationModel(editor, document);
	}
	
	private CoverageAnnotationModel(final ITextEditor editor, final IDocument document) {
		logger.fine("CoverageAnnotationModel created."); //$NON-NLS-1$
		// TODO what about editor and document?
		initializeAnnotations();
	}

	private void initializeAnnotations() {
		// TODO correct stuff instead of dummy stuff
		CoverageAnnotation annotation = CoverageAnnotation.fromPosition(0, 10);
		annotations.add(annotation);
		AnnotationModelEvent event = new AnnotationModelEvent(this);
		event.annotationAdded(annotation);
		fireModelChanged(event);
	}
	
	@Override
	public void connect(final IDocument document) {
		logger.fine("CoverageAnnotationModel connected to " + document.get()); //$NON-NLS-1$

	    addAnnotationsTo(document);
		// TODO connecting
	}

	private void addAnnotationsTo(final IDocument document) {
		for (CoverageAnnotation ann : annotations) {
	        try {
	        	document.addPosition(ann.getPosition());
	        } catch (BadLocationException e) {
	        	logger.log(Level.WARNING, "unable to add annotation to document", e); //$NON-NLS-1$
	        }
	      }
	}

	@Override
	public void disconnect(final IDocument document) {
		// TODO disconnecting
		removeAnnotationsFrom(document);
		logger.fine("CoverageAnnotationModel disconnected from " + document.get()); //$NON-NLS-1$
	}

	private void removeAnnotationsFrom(IDocument document) {
		for (CoverageAnnotation ann : annotations) {
		      document.removePosition(ann.getPosition());
		}
	}

	@Override
	public Iterator<CoverageAnnotation> getAnnotationIterator() {
		return annotations.iterator();
	}

	@Override
	public Position getPosition(Annotation annotation) {
		if (annotation instanceof CoverageAnnotation) {
			return ((CoverageAnnotation) annotation).getPosition();
		} else {
			return null;
		}
	}

	@Override
	public void addAnnotation(Annotation annotation, Position position) {
		throw new UnsupportedOperationException(
				"adding annotations externally is not supported"); //$NON-NLS-1$
	}

	@Override
	public void removeAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException(
				"removing annotations externally is not supported"); //$NON-NLS-1$
	}

	@Override
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		listeners.add(listener);
		fireModelChanged(new AnnotationModelEvent(this, true));
	}

	@Override
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		listeners.remove(listener);
	}
	
	private void fireModelChanged(final AnnotationModelEvent event) {
		event.markSealed();
		if (event.isEmpty()) {
			return;
		}
		for (final IAnnotationModelListener listener : listeners) {
	        if (listener instanceof IAnnotationModelListenerExtension) {
	        	((IAnnotationModelListenerExtension) listener).modelChanged(event);
	        } else {
	        	listener.modelChanged(this);
	        }
		}
	}
}
