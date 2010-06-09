/*******************************************************************************
 * Copyright (c) 2008
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed below, as Initial Contributors under such license.
 * The text of such license is available at 
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 * 		Henrik Lindberg
 *******************************************************************************/

package org.eclipse.equinox.p2.authoring;

import java.util.EventObject;

import javax.swing.event.ChangeEvent;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.authoring.forms.IMasterDetailsController;
import org.eclipse.equinox.p2.authoring.forms.IPageMementoProvider;
import org.eclipse.equinox.p2.authoring.forms.TableMasterDetailsBlock;
import org.eclipse.equinox.p2.authoring.internal.IEditEventBusProvider;
import org.eclipse.equinox.p2.authoring.internal.IEditorListener;
import org.eclipse.equinox.p2.authoring.internal.IUndoOperationSupport;
import org.eclipse.equinox.p2.authoring.internal.InstallableUnitBuilder;
import org.eclipse.equinox.p2.authoring.internal.P2AuthoringLabelProvider;
import org.eclipse.equinox.p2.authoring.internal.InstallableUnitBuilder.ArtifactKeyBuilder;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A MasterDetails block for IU ArtifactKeys that displays the artifacts in a list and allows for editing
 * of elements in the list. Supports undo of operations.
 * 
 * @author Henrik Lindberg
 * 
 */
public class ArtifactsBodyBlock extends TableMasterDetailsBlock implements IDetailsPageProvider,
		IMasterDetailsController, IPageMementoProvider
{

	private IDetailsPage m_artifactPage;

	private IBaseLabelProvider m_labelProvider;

	private IStructuredContentProvider m_contentProvider;
	
	private MasterFormPart m_masterFormPart;

	public ArtifactsBodyBlock(FormPage page, Object layoutData)
	{
		super(page, layoutData, 350);
	}
	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent)
	{
		super.createMasterPart(managedForm, parent); // create the view
		m_masterFormPart = new MasterFormPart();	// create the manager of the view data lifecycle
		managedForm.addPart(m_masterFormPart);		// and make it part of the overall lifecycle
	}
	@Override
	public String getName()
	{
		return "Artifact Keys";
	}

	@Override
	public String getDescription()
	{
		return "Edit the artifact keys that make up the content of this this installable unit.";
	}
	/**
	 * Adds a default ArtifactKey for editing. Operation can be undone.
	 */
	public void add()
	{
		ArtifactKeyBuilder artifact = new ArtifactKeyBuilder( //
				"some.classifier", 					//$NON-NLS-1$
				"some.name", 			//
				"1.0.0"); 	// $NON-NLS-1$

		addRemove(artifact, true);
	}
	
	/**
	 * Method common to both add and remove. Operation can be undone.
	 * @param artifact what to add or remove
	 * @param add true if required should be added, false to remove
	 */
	private void addRemove(ArtifactKeyBuilder artifact, boolean add)
	{
		FormToolkit toolkit = m_formPage.getManagedForm().getToolkit();
		if(toolkit instanceof IUndoOperationSupport)
		{
			AddRemoveOperation op = new AddRemoveOperation(artifact, add);
			op.addContext(((IUndoOperationSupport)toolkit).getUndoContext());
			try
			{
				((IUndoOperationSupport)toolkit).getOperationHistory().execute(op, null, null);
			}
			catch(ExecutionException e)
			{
				// TODO Proper logging
				e.printStackTrace();
			}
		}
		else
		{
			// without undo support - just add it... (should not happen)
			InstallableUnitBuilder iu = ((InstallableUnitEditor)m_formPage.getEditor()).getInstallableUnit();
			iu.addArtifactKey(artifact);
		}
	}

	/**
	 * Configures the add button to have a menu with different add types
	 * TODO: clean up popup menu code if it will not be used
	 */
	@Override
	protected void configureAddButton(final Button b)
	{
		// Create a listener for menu items so that the correct
		// type of add operation is performed.
		//
		SelectionListener listener = new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e)
			{
				add();
				// // example of add menu - keep if there should be more ways to add something...
				// Object data = e.item.getData();
				// if("link".equals(data))
				// addLink();
			}

		};
		b.addSelectionListener(listener);
		// // example of add menu
		// Menu addMenu = new Menu(b.getShell(), SWT.POP_UP);
		// mi = new MenuItem(addMenu, SWT.PUSH);
		// mi.setText("Add Link");
		// mi.setData("link");
		// mi.addSelectionListener(listener);
		//
		// // attach menu to button (pops up on right mouse click)
		// b.setMenu(addMenu);
		// // attach listener to button so menu pops up on button click (left click)
		// b.addSelectionListener(new SelectionListener()
		// { public void widgetDefaultSelected(SelectionEvent e)
		// 	{ widgetSelected(e); }
		// 	public void widgetSelected(SelectionEvent e)
		// 	{ b.getMenu().setVisible(true);}
		// });
	}
	/**
	 * Moves selected required capability down in the list. Operation can be undone.
	 */
	public void down()
	{
		move(1);
	}
	/**
	 * Moves selected required capability up in the list. Operation can be undone.
	 */
	public void up()
	{
		move(-1);
	}
	
	/**
	 * Common move operation - moved required capability up or down in the list. Operation can be undone.
	 */
	private void move(int delta)
	{
		IStructuredSelection ssel = (IStructuredSelection)m_viewer.getSelection();
		if(ssel == null || ssel.size() != 1)
			return; // nothing to move (or too many)
		ArtifactKeyBuilder artifact = (ArtifactKeyBuilder)ssel.getFirstElement();

		FormToolkit toolkit = m_formPage.getManagedForm().getToolkit();
		if(toolkit instanceof IUndoOperationSupport)
		{
			MoveOperation op = new MoveOperation(artifact, delta);
			op.addContext(((IUndoOperationSupport)toolkit).getUndoContext());
			try
			{
				((IUndoOperationSupport)toolkit).getOperationHistory().execute(op, null, null);
			}
			catch(ExecutionException e)
			{
				// TODO Proper logging
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public IDetailsPageProvider getDetailsPageProvider()
	{
		return this;
	}

	public IDetailsPage getArtifactPage()
	{
		// TODO: no need to cache?  Master detail editor creates this on demand and keeps it,
		// but then the undo history may refer to deleted (and recreated elements).
		//
		if(m_artifactPage == null)
			m_artifactPage = new ArtifactPage();
		return m_artifactPage;
	}

	/**
	 * Returns a content provider for required capabilities from the editors installable unit.
	 */
	@Override
	public IStructuredContentProvider getMasterContentProvider()
	{
		if(m_contentProvider == null)
			m_contentProvider = new IStructuredContentProvider()
			{
				public Object[] getElements(Object inputElement)
				{
					InstallableUnitBuilder iu = ((InstallableUnitEditor)m_formPage.getEditor()).getInstallableUnit();
					return iu.getArtifacts();
				}

				public void dispose()
				{
				}

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
				{
				}

			};
		return m_contentProvider;
	}

	/**
	 * Returns 'this' as a handler of add, remove, up, down,...
	 */
	@Override
	public IMasterDetailsController getMasterDetailsController()
	{
		return this;
	}

	/**
	 * Returns a styled label provider using the {@link P2AuthoringLabelProvider} as the base label provider.
	 */
	@Override
	public IBaseLabelProvider getMasterLabelProvider()
	{
		if(m_labelProvider == null)
			m_labelProvider = new DelegatingStyledCellLabelProvider(new P2AuthoringLabelProvider());
		return m_labelProvider;
	}

	/**
	 * Returns a page for a page key returned by {@link #getPageKey(Object)}. Currently, there is only one type of
	 * details page.
	 */
	public IDetailsPage getPage(Object key)
	{
		return getArtifactPage();
	}

	/**
	 * Selects a page key for the selected object. See {@link #getPage(Object)}. Currently, there is only one type
	 * of details page key - "required".
	 */
	public Object getPageKey(Object object)
	{
		return "artifact";
	}

	/**
	 * Removes the selected required capability. The operation can be undone.
	 */
	public void remove()
	{
		// remove everything selected
		IStructuredSelection ssel = (IStructuredSelection)m_viewer.getSelection();
		if(ssel == null || ssel.getFirstElement() == null)
			return; // nothing to remove
		addRemove((ArtifactKeyBuilder)ssel.getFirstElement(), false);
	}

	
	private class MasterFormPart extends AbstractFormPart
	{

		@Override
		public void initialize(IManagedForm form)
		{
			super.initialize(form);
			// register a listener to Required Capability change events
			if(form.getToolkit() instanceof IEditEventBusProvider)
			{
				((IEditEventBusProvider)form.getToolkit()).getEventBus().addListener(new IEditorListener(){

					public void notify(EventObject o)
					{
						if(o instanceof ChangeEvent && o.getSource() instanceof ArtifactKeyBuilder)
							ArtifactsBodyBlock.this.m_viewer.refresh(o.getSource(), true);
					}
					
				});
			}
		}

		/**
		 * Refreshes the viewer with stale model changes
		 */
		@Override
		public void refresh()
		{
			ArtifactsBodyBlock.this.m_viewer.refresh();
			super.refresh();
		}
		
	}
	/**
	 * Returns a memento that restores this page selection.
	 */
	public Object getPageMemento()
	{
		return ((IStructuredSelection)m_viewer.getSelection()).getFirstElement();
	}
	/**
	 * Restores this page selection from the memento.
	 */
	public void setPageMemento(Object memento)
	{
		if(memento != null)
			m_viewer.setSelection(new StructuredSelection(memento), true);
	}
	/**
	 * Switches focus in the editor to the page where this required body block is.
	 */
	private void switchFocus(ArtifactKeyBuilder select)
	{
		FormEditor editor = m_formPage.getEditor();
		IFormPage currentPage = editor.getActivePageInstance();
		if(!m_formPage.getId().equals(currentPage.getId()))
			editor.setActivePage(m_formPage.getId());
		if(select != null)
			m_viewer.setSelection(new StructuredSelection(select), true);
	}

	/**
	 * Undoable operation for add/remove of required capability.
	 * @author Henrik Lindberg
	 *
	 */
	private class AddRemoveOperation extends AbstractOperation
	{
		private ArtifactKeyBuilder m_artifact;
		private boolean  m_add;
		private int m_index;
		
		public AddRemoveOperation(ArtifactKeyBuilder artifact, boolean add)
		{
			super((add ? "Add" : "Remove") + " Artifact Key"); 
			m_artifact = artifact;
			m_add = add;
		}
		private void updatePageState(boolean select)
		{
			m_masterFormPart.markStale();
			m_masterFormPart.markDirty();
			switchFocus(select ? m_artifact : null); // switch focus if on another page
		}
		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
		{
			return redo(monitor, info);
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
		{
			InstallableUnitBuilder iu = ((InstallableUnitEditor)m_formPage.getEditor()).getInstallableUnit();
			if(m_add)
				m_index = iu.addArtifactKey(m_artifact);
			else
				m_index = iu.removeArtifactKey(m_artifact);
			updatePageState(m_add);
			if(monitor != null)
				monitor.done();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
		{
			InstallableUnitBuilder iu = ((InstallableUnitEditor)m_formPage.getEditor()).getInstallableUnit();
			if(m_add)
				iu.removeArtifactKey(m_artifact);
			else
				iu.addArtifactKey(m_artifact, m_index);
			updatePageState(!m_add);
			if(monitor != null)
				monitor.done();
			return Status.OK_STATUS;
		}
		
	}
	/**
	 * Undoable operation class for moving artifact key.
	 * @author Henrik Lindberg
	 *
	 */
	private class MoveOperation extends AbstractOperation
	{
		private ArtifactKeyBuilder m_artifact;
		private int  m_delta;
		
		public MoveOperation(ArtifactKeyBuilder artifact, int delta)
		{
			super("Move Artifact Key"); 
			m_artifact = artifact;
			m_delta = delta;
		}
		private void updatePageState()
		{
			m_masterFormPart.markStale();
			m_masterFormPart.markDirty();
			switchFocus(m_artifact); // switch focus if on another page
		}
		@Override
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
		{
			return redo(monitor, info);
		}

		@Override
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
		{
			return xxdo(monitor, info, m_delta);
		}
		public IStatus xxdo(IProgressMonitor monitor, IAdaptable info, int delta) throws ExecutionException
		{
			InstallableUnitBuilder iu = ((InstallableUnitEditor)m_formPage.getEditor()).getInstallableUnit();
			iu.moveArtifactKey(m_artifact, delta);
			updatePageState();
			if(monitor != null)
				monitor.done();
			return Status.OK_STATUS;
		}

		@Override
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
		{
			return xxdo(monitor, info, -m_delta);		
		}
		
	}

}
