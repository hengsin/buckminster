/*******************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein
 * are the sole and exclusive property of Cloudsmith Inc. and may
 * not be disclosed, used, modified, copied or distributed without
 * prior written consent or license from Cloudsmith Inc.
 ******************************************************************/

package org.eclipse.buckminster.ui.general.editor.simple;

import org.eclipse.buckminster.ui.Messages;
import org.eclipse.buckminster.ui.UiUtils;
import org.eclipse.buckminster.ui.internal.DynamicTableLayout;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * General table editor. Needs data wrapped ITable (Table) instance. Prepare ITable instance and start using this
 * editor.
 * 
 * @author Karel Brezina
 */
public class SimpleTableEditor<T> extends Composite
{

	class TableContentProvider implements IStructuredContentProvider
	{
		public void dispose()
		{
			// Nothing to dispose
		}

		public Object[] getElements(Object inputElement)
		{
			return m_table.getRows().toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			// Nothing to do
		}
	}

	class TableLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		@SuppressWarnings("unchecked")
		public String getColumnText(Object element, int columnIndex)
		{
			Object field = m_table.getEditorField((T)element, columnIndex);
			return field == null
					? "" : field.toString(); //$NON-NLS-1$
		}
	}

	private final ISimpleTable<T> m_table;

	private final Image m_windowImage;

	private final String m_windowTitle;

	private final Image m_wizardImage;

	private final String m_helpURL;

	private TableViewer m_tableViewer;

	private Composite m_stackButtonComposite;

	private StackLayout m_stackButtonLayout;

	private Composite m_editButtonBox;

	private Composite m_viewButtonBox;

	private Button m_newButton;

	private Button m_editButton;

	private Button m_viewButton;

	private Button m_removeButton;

	private boolean m_enabled = true;

	/**
	 * Creates general table editor.
	 * 
	 * @param parent
	 *            parent composite
	 * @param table
	 *            wrapped editor data
	 * @param windowImage
	 *            window icon
	 * @param windowTitle
	 *            window title
	 * @param wizardImage
	 *            wizard image - bypass to row editor TableRowDialog
	 * @param helpURL
	 *            URL of help info - bypass to row editor TableRowDialog
	 * @param style
	 *            current composite style
	 */
	public SimpleTableEditor(Composite parent, ISimpleTable<T> table, Image windowImage, String windowTitle,
			Image wizardImage, String helpURL, int style)
	{
		super(parent, style);
		m_table = table;
		m_windowImage = windowImage;
		m_windowTitle = windowTitle;
		m_wizardImage = wizardImage;
		m_helpURL = helpURL;
		initComposite();
	}

	public void refresh()
	{
		m_tableViewer.setInput(m_table);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		m_enabled = enabled;

		// m_tableViewer.getTable().setEnabled(enabled);

		enableDisableButtonGroup();

		// m_tableViewer.getTable().setForeground(enabled
		// ? null
		// : m_tableViewer.getTable().getDisplay().getSystemColor(SWT.COLOR_GRAY));
	}

	private void createButtonBox(Composite parent)
	{
		m_stackButtonComposite = new Composite(parent, SWT.NONE);
		m_stackButtonComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		m_stackButtonLayout = new StackLayout();
		m_stackButtonLayout.marginHeight = m_stackButtonLayout.marginWidth = 0;
		m_stackButtonComposite.setLayout(m_stackButtonLayout);

		m_editButtonBox = new Composite(m_stackButtonComposite, SWT.None);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = gridLayout.marginWidth = gridLayout.verticalSpacing = 0;
		m_editButtonBox.setLayout(gridLayout);
		m_editButtonBox.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		m_newButton = UiUtils.createPushButton(m_editButtonBox, Messages.new_label, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				newRow();
			}
		});
		m_newButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		m_editButton = UiUtils.createPushButton(m_editButtonBox, Messages.edit, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				editRow(true);
			}
		});
		m_editButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		m_removeButton = UiUtils.createPushButton(m_editButtonBox, Messages.remove, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				removeRow();
			}
		});
		m_removeButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		m_viewButtonBox = new Composite(m_stackButtonComposite, SWT.NONE);
		gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		m_viewButtonBox.setLayout(gridLayout);
		m_viewButtonBox.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		m_viewButton = UiUtils.createPushButton(m_viewButtonBox, Messages.view, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				editRow(false);
			}
		});
		m_viewButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		enableDisableButtonGroup();
	}

	private void editRow(boolean enableChanges)
	{
		SimpleTableRowDialog<T> dialog = new SimpleTableRowDialog<T>(this.getShell(), m_windowImage, m_windowTitle,
				m_wizardImage, m_helpURL, m_table, m_tableViewer.getTable().getSelectionIndex(), enableChanges);

		if(dialog.open() == IDialogConstants.OK_ID)
		{
			refresh();
		}
	}

	private void enableDisableButtonGroup()
	{
		boolean rowSelected = m_tableViewer.getTable().getSelectionIndex() >= 0;

		if(m_enabled)
		{
			m_newButton.setEnabled(true);
			m_editButton.setEnabled(rowSelected);
			m_removeButton.setEnabled(rowSelected);

			m_stackButtonLayout.topControl = m_editButtonBox;
		}
		else
		{
			m_newButton.setEnabled(false);
			m_editButton.setEnabled(false);
			m_removeButton.setEnabled(false);

			m_stackButtonLayout.topControl = m_viewButtonBox;
		}

		m_viewButton.setEnabled(rowSelected);
		m_stackButtonComposite.layout();
	}

	private void initComposite()
	{
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Table table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		// table.setHeaderVisible(false);
		table.setHeaderVisible(true);
		DynamicTableLayout layout = new DynamicTableLayout(50);

		int tableIdx = 0;
		for(int idx = 0; idx < m_table.getColumns(); idx++)
		{
			if(m_table.getColumnWeights()[idx] > 0)
			{
				TableColumn tableColumn = new TableColumn(table, SWT.LEFT, tableIdx);
				tableColumn.setText(m_table.getColumnHeaders()[idx]);
				layout.addColumnData(new ColumnWeightData(m_table.getColumnWeights()[idx], true));
				tableIdx++;
			}
		}
		table.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// gridData.widthHint = 600;
		table.setLayoutData(gridData);

		m_tableViewer = new TableViewer(table);
		m_tableViewer.setLabelProvider(new TableLabelProvider());
		m_tableViewer.setContentProvider(new TableContentProvider());
		m_tableViewer.setInput(m_table);
		m_tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				enableDisableButtonGroup();
			}
		});
		m_tableViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				if(m_tableViewer.getTable().getSelectionIndex() >= 0)
					editRow(m_enabled);
			}
		});

		createButtonBox(this);
	}

	private void newRow()
	{
		SimpleTableRowDialog<T> dialog = new SimpleTableRowDialog<T>(this.getShell(), m_windowImage, m_windowTitle,
				m_wizardImage, m_helpURL, m_table, -1, true);

		if(dialog.open() == IDialogConstants.OK_ID)
		{
			refresh();
		}
	}

	private void removeRow()
	{
		m_table.removeRow(m_tableViewer.getTable().getSelectionIndex());
		refresh();
	}
}
