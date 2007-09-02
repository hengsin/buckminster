/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.ui.general.editor.structured;

import org.eclipse.buckminster.ui.UiUtils;
import org.eclipse.buckminster.ui.general.editor.ValidatorException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * @author Karel Brezina
 *
 */
public class OnePageTableEditor<T> extends StructuredTableEditor<T>
{
	private final boolean m_disableNew; // disables New button

	private final boolean m_disableRemove; // disables Remove button
	
	public OnePageTableEditor(Composite parent, IStructuredTable<T> table, boolean swapButtonsFlag, int style)
	{
		this(parent, table, swapButtonsFlag, false, false, style);
	}

	public OnePageTableEditor(
			Composite parent, IStructuredTable<T> table, boolean swapButtonsFlag, boolean disableNew, boolean disableRemove, int style)
	{
		super(parent, table, swapButtonsFlag, style);
		m_disableNew = disableNew;
		m_disableRemove = disableRemove;
	}

	@Override
	protected void initComposite()
	{
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gridData);

		createTableGroup(this);

		createStackOptions(this);

		createStack(this);

		fillStackOptions();
	}

	@Override
	protected Composite createTableGroupComposite(Composite parent)
	{
		Composite componentTableGroup = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = gl.marginWidth = 0;
		componentTableGroup.setLayout(gl);
		componentTableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return componentTableGroup;
	}

	@Override
	protected Composite createTableButtonsComposite(Composite parent)
	{
		Composite buttonBox = new Composite(parent, SWT.NULL);
		buttonBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.marginWidth = layout.marginHeight = 0;
		layout.spacing = 3;
		buttonBox.setLayout(layout);

		return buttonBox;
	}

	@Override
	protected void createTableButtons(Composite parent)
	{
		Composite buttonBox = createTableButtonsComposite(parent);

		Composite buttonBox1 = new Composite(buttonBox, SWT.NULL);
		// buttonBox1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
		// false));
		FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginWidth = layout.marginHeight = 0;
		buttonBox1.setLayout(layout);

		setNewButton(UiUtils.createPushButton(buttonBox1, "New", new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				newRow();
			}
		}));

		setRemoveButton(UiUtils.createPushButton(buttonBox1, "Remove", new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				removeRow();
			}
		}));

		if(isSwapButtonAllowed())
		{
			Composite buttonBox2 = new Composite(buttonBox, SWT.NULL);
			// buttonBox2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
			// false));
			layout = new FillLayout(SWT.HORIZONTAL);
			layout.marginWidth = layout.marginHeight = 0;
			buttonBox2.setLayout(layout);

			setMoveUpButton(UiUtils.createPushButton(buttonBox2, "Move up", new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					swapAndReselect(0, -1);
				}
			}));

			setMoveDownButton(UiUtils.createPushButton(buttonBox2, "Move down", new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					swapAndReselect(1, 0);
				}
			}));
		}
	}

	@Override
	protected void newRow()
	{
		if(getSelectionIndex() >= 0)
			if(!save())
				return;

		T row = getTable().addEmptyRow();
		refreshTable();
		selectRow(row);
		updateLastRow();
		enableDisableButtonGroup();
		refreshRow();
		focusStackComposite();
	}

	@Override
	protected void editRow()
	{
		// automatic
	}

	public void cancelRow()
	{
		enableDisableButtonGroup();
		refreshRow();
	}

	public boolean save()
	{
		return save(null);
	}
	
	public boolean save(IActivator activator)
	{
		if(getTableViewer().getTable().getSelectionIndex() >= 0)
		{
			try
			{
				saveRow();
			}
			catch(ValidatorException e)
			{
				if(activator != null)
					activator.activate();
				MessageDialog.openError(getShell(), "Error", e.getMessage());
				getTableViewer().getTable().select(getLastSelectedRow());
				return false;
			}

			enableDisableButtonGroup();
		}
		
		return true;
	}

	@Override
	protected boolean rowSelectionEvent()
	{
		if(!save())
			return false;
				
		enableDisableButtonGroup();
		refreshRow();
		
		return true;
	}

	@Override
	public void refresh()
	{
		refreshTable();
		enableDisableButtonGroup();
		refreshRow();
	}

	@Override
	protected void enableDisableButtonGroup()
	{
		if(isEnabled())
		{
			Table table = getTableViewer().getTable();
			int top = table.getItemCount();
			int idx = table.getSelectionIndex();
			getNewButton().setEnabled(!m_disableNew);
			getRemoveButton().setEnabled(!m_disableRemove && (idx >= 0));
			if(isSwapButtonAllowed())
			{
				getMoveUpButton().setEnabled(idx > 0);
				getMoveDownButton().setEnabled(idx >= 0 && idx < top - 1);
			}
			enableFields(idx >= 0);
		} else
		{
			getNewButton().setEnabled(false);
			getRemoveButton().setEnabled(false);			
			if(isSwapButtonAllowed())
			{
				getMoveUpButton().setEnabled(false);
				getMoveDownButton().setEnabled(false);
			}
			enableFields(isEnabled());
		}
		
		getTableViewer().getTable().setEnabled(isEnabled());
	}
	
	public boolean show(T row, String tab)
	{
		int stackIdx = getTable().getStackKeys().indexOf(tab);
		
		if(stackIdx == -1)
			return false;
		
		if(!selectRow(row))
			return false;
				
		refreshRow();

		setStackOption(stackIdx);
		
		return true;
	}
}
