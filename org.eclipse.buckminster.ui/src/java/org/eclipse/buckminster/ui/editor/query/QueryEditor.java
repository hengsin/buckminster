/*******************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein
 * are the sole and exclusive property of Cloudsmith Inc. and may
 * not be disclosed, used, modified, copied or distributed without
 * prior written consent or license from Cloudsmith Inc.
 ******************************************************************/

package org.eclipse.buckminster.ui.editor.query;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.core.common.model.Documentation;
import org.eclipse.buckminster.core.cspec.IComponentRequest;
import org.eclipse.buckminster.core.cspec.model.ComponentRequest;
import org.eclipse.buckminster.core.ctype.AbstractComponentType;
import org.eclipse.buckminster.core.helpers.TextUtils;
import org.eclipse.buckminster.core.query.builder.AdvisorNodeBuilder;
import org.eclipse.buckminster.core.query.builder.ComponentQueryBuilder;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.core.query.model.MutableLevel;
import org.eclipse.buckminster.core.query.model.SourceLevel;
import org.eclipse.buckminster.core.version.VersionSelector;
import org.eclipse.buckminster.osgi.filter.Filter;
import org.eclipse.buckminster.osgi.filter.FilterFactory;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.runtime.IOUtils;
import org.eclipse.buckminster.runtime.Trivial;
import org.eclipse.buckminster.runtime.URLUtils;
import org.eclipse.buckminster.sax.Utils;
import org.eclipse.buckminster.ui.Messages;
import org.eclipse.buckminster.ui.SaveRunnable;
import org.eclipse.buckminster.ui.UiUtils;
import org.eclipse.buckminster.ui.actions.BlankQueryAction;
import org.eclipse.buckminster.ui.editor.ArtifactType;
import org.eclipse.buckminster.ui.editor.EditorUtils;
import org.eclipse.buckminster.ui.editor.IDerivedEditorInput;
import org.eclipse.buckminster.ui.editor.Properties;
import org.eclipse.buckminster.ui.editor.PropertiesModifyEvent;
import org.eclipse.buckminster.ui.editor.PropertiesModifyListener;
import org.eclipse.buckminster.ui.editor.VersionDesignator;
import org.eclipse.buckminster.ui.editor.VersionDesignatorEvent;
import org.eclipse.buckminster.ui.editor.VersionDesignatorListener;
import org.eclipse.buckminster.ui.general.editor.structured.IActivator;
import org.eclipse.buckminster.ui.internal.DynamicTableLayout;
import org.eclipse.buckminster.ui.internal.ResolveJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author Karel Brezina
 * 
 */
public class QueryEditor extends EditorPart implements IEditorMatchingStrategy
{
	class AdvisorNodeLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		public String getColumnText(Object element, int columnIndex)
		{
			AdvisorNodeBuilder node = (AdvisorNodeBuilder)element;
			String lbl;
			switch(columnIndex)
			{
			case 0:
				lbl = node.getNamePattern() == null
						? "" : node.getNamePattern().toString(); //$NON-NLS-1$
				break;
			case 1:
				lbl = node.getComponentTypeID();
				break;
			case 2:
				lbl = node.getFilter() == null
						? "" : node.getFilter().toString(); //$NON-NLS-1$
				break;
			default:
				lbl = null;
			}
			return lbl;
		}
	}

	class CheckboxSelectionListener extends SelectionAdapter
	{
		private Control[] m_controlsToEnable;

		public CheckboxSelectionListener(Control[] controlsToEnable)
		{
			m_controlsToEnable = controlsToEnable;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			Button button = (Button)e.widget;
			boolean enable = button.getSelection();

			for(Control control : m_controlsToEnable)
			{
				control.setEnabled(enable);
			}
		}
	}

	class CompoundModifyListener implements VersionDesignatorListener, ModifyListener, PropertiesModifyListener,
			SelectionListener
	{

		public void modifyProperties(PropertiesModifyEvent e)
		{
			if(!m_suppressModifyListener)
				setDirty(true);
		}

		public void modifyText(ModifyEvent e)
		{
			if(!m_suppressModifyListener)
				setDirty(true);
		}

		public void modifyVersionDesignator(VersionDesignatorEvent e)
		{
			if(!m_suppressModifyListener)
				setDirty(true);
		}

		public void widgetDefaultSelected(SelectionEvent e)
		{
		}

		public void widgetSelected(SelectionEvent e)
		{
			if(!m_suppressModifyListener)
				setDirty(true);
		}
	}

	private static final IActivator EMPTY_ACTIVATOR = new IActivator()
	{

		public void activate()
		{
			// nothing to activate
		}
	};

	private final static int DONT_SAVE = -99;

	private CTabFolder m_tabFolder;

	private Text m_componentName;

	private Combo m_componentType;

	private VersionDesignator m_versionDesignator;

	private ComponentQueryBuilder m_componentQuery;

	private Button m_enableOverride;

	private boolean m_hasChanges;

	private Button m_resolveButton;

	private Button m_materializeButton;

	private Button m_externalSaveAsButton;

	private Button m_moveDownButton;

	private Button m_moveUpButton;

	private boolean m_mute;

	private Combo m_mutableLevel;

	private Text m_namePattern;

	private Text m_filter;

	private Combo m_category;

	private Text m_overlayFolder;

	private Button m_overlayBrowseButton;

	private Text m_wantedAttributes;

	private Button m_prune;

	private boolean m_needsRefresh;

	private Button m_newButton;

	private TableViewer m_nodeTable;

	private Button m_removeButton;

	private Button m_requestURLCheckbox;

	private Text m_requestURL;

	private Button m_propertyURLCheckbox;

	private Text m_propertyURL;

	private Tree m_nodeTree;

	private Combo m_sourceLevel;

	private Button m_skipComponent;

	private Button m_allowCircular;

	private Composite m_nodesStackComposite;

	private StackLayout m_nodesStackLayout;

	private HashMap<String, Control> m_nodesHash;

	private Button m_useTargetPlatform;

	private Button m_useWorkspace;

	private Button m_useMaterialization;

	private Button m_useResolutionService;

	private Text m_branchTagPath;

	private Text m_timestamp;

	private Text m_revision;

	private VersionDesignator m_versionOverride;

	private boolean m_continueOnError;

	private Properties m_nodeProperties;

	private Text m_nodeDocumentation;

	private Properties m_properties;

	private Text m_shortDesc;

	private Text m_documentation;

	private CTabItem m_xmlTab;

	private Text m_xml;

	private CompoundModifyListener m_compoundModifyListener;

	private final SimpleDateFormat m_timestampFormat = (SimpleDateFormat)DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);

	private int m_lastSelectedNode = -1;

	private int m_lastEditedNode = -1;

	private boolean m_suppressModifyListener = false;

	public String commitChanges(IComponentRequest[] requestRet)
	{
		String name = UiUtils.trimmedValue(m_componentName);
		if(name == null)
			return Messages.the_component_must_have_a_name;

		String category = null;
		int idx = m_componentType.getSelectionIndex();
		if(idx >= 0)
		{
			category = m_componentType.getItem(idx);
			if(category.length() == 0)
				category = null;
		}
		requestRet[0] = new ComponentRequest(name, category, m_versionDesignator.getVersionDesignator());
		return null;
	}

	@Override
	public void createPartControl(Composite parent)
	{
		Composite topComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = layout.marginWidth = 0;
		topComposite.setLayout(layout);

		m_tabFolder = new CTabFolder(topComposite, SWT.BOTTOM);
		m_tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final CTabItem mainTab = new CTabItem(m_tabFolder, SWT.NONE);
		mainTab.setText(Messages.main);
		mainTab.setControl(getMainTabControl(m_tabFolder));

		final CTabItem advisorTab = new CTabItem(m_tabFolder, SWT.NONE);
		advisorTab.setText(Messages.advisor_nodes);
		advisorTab.setControl(getAdvisorTabControl(m_tabFolder));

		CTabItem propertiesTab = new CTabItem(m_tabFolder, SWT.NONE);
		propertiesTab.setText(Messages.properties);
		propertiesTab.setControl(getPropertiesTabControl(m_tabFolder));

		CTabItem documentationTab = new CTabItem(m_tabFolder, SWT.NONE);
		documentationTab.setText(Messages.documentation);
		documentationTab.setControl(getDocumentationTabControl(m_tabFolder));

		m_xmlTab = new CTabItem(m_tabFolder, SWT.NONE);
		m_xmlTab.setText(Messages.xml_content);
		m_xmlTab.setControl(getXMLTabControl(m_tabFolder));

		m_tabFolder.addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent e)
			{
				if(m_tabFolder.getSelection() == null)
					m_tabFolder.setSelection(0);
			}
		});

		m_tabFolder.addSelectionListener(new SelectionAdapter()
		{
			private final IActivator NODE_TAB_ACTIVATOR = new IActivator()
			{
				public void activate()
				{
					m_tabFolder.setSelection(advisorTab);
				}
			};

			private CTabItem m_lastTab = mainTab;

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// save row
				if(m_lastTab != e.item)
				{
					if(m_lastTab == advisorTab)
						if(!saveLastNode(NODE_TAB_ACTIVATOR))
							return;
				}

				if(m_xmlTab == e.item)
				{
					if(!commitChangesToQuery())
						MessageDialog.openWarning(getSite().getShell(), null,
								Messages.xml_content_was_not_updated_due_to_errors);
					else
						m_xml.setText(getCQueryXML());
				}
				m_lastTab = (CTabItem)e.item;
			}
		});

		createActionButtons(topComposite);
	}

	public void doExternalSaveAs()
	{
		if(!commitChangesToQuery())
			return;
		FileDialog dlg = new FileDialog(getSite().getShell(), SWT.SAVE);
		dlg.setFilterExtensions(new String[] { "*.cquery" }); //$NON-NLS-1$
		final String location = dlg.open();
		if(location == null)
			return;
		saveToPath(new Path(location));
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		if(!commitChangesToQuery())
			return;

		IEditorInput input = getEditorInput();
		if(input == null)
			return;

		IPath path = (input instanceof ILocationProvider)
				? ((ILocationProvider)input).getPath(input)
				: ((IPathEditorInput)input).getPath();

		saveToPath(path);
	}

	@Override
	public void doSaveAs()
	{
		if(!commitChangesToQuery())
			return;

		IEditorInput input = getEditorInput();
		if(input == null)
			return;

		SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
		IFile original = (input instanceof IFileEditorInput)
				? ((IFileEditorInput)input).getFile()
				: null;
		if(original != null)
			dialog.setOriginalFile(original);

		if(dialog.open() == Window.CANCEL)
			return;

		IPath filePath = dialog.getResult();
		if(filePath == null)
			return;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile file = workspace.getRoot().getFile(filePath);
		saveToPath(file.getLocation());
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		if(!(input instanceof ILocationProvider || input instanceof IPathEditorInput || input instanceof IURIEditorInput))
			throw new PartInitException(Messages.invalid_input);

		setSite(site);

		if(input instanceof IURIEditorInput)
		{
			try
			{
				input = EditorUtils.getExternalFileEditorInput((IURIEditorInput)input, ArtifactType.CQUERY);
			}
			catch(Exception e)
			{
				throw new PartInitException(Messages.unable_to_open_editor, e);
			}
		}

		InputStream stream = null;
		try
		{
			IPath path = (input instanceof ILocationProvider)
					? ((ILocationProvider)input).getPath(input)
					: ((IPathEditorInput)input).getPath();

			File file = path.toFile();
			m_componentQuery = new ComponentQueryBuilder();
			if(file.length() == 0)
			{
				String defaultName = file.getName();
				if(defaultName.startsWith(BlankQueryAction.TEMP_FILE_PREFIX))
					defaultName = ""; //$NON-NLS-1$
				else
				{
					int lastDot = defaultName.lastIndexOf('.');
					if(lastDot > 0)
						defaultName = defaultName.substring(0, lastDot);
				}
				m_componentQuery.setRootRequest(new ComponentRequest(defaultName, null, null));
			}
			else
			{
				stream = new FileInputStream(file);
				URL contextURL;
				try
				{
					// The context URL is normally passed on as the tooltip text
					//
					contextURL = URLUtils.normalizeToURL(input.getToolTipText());
				}
				catch(MalformedURLException e)
				{
					contextURL = file.toURI().toURL();
				}
				m_componentQuery.initFrom(ComponentQuery.fromStream(contextURL, null, stream, true));
				CorePlugin.getLogger().debug("CQUERY Context URL set to %s", m_componentQuery.getContextURL()); //$NON-NLS-1$
			}
			m_needsRefresh = true;
			if(m_componentName != null)
			{
				refreshQuery();
			}
			setInputWithNotify(input);
			setPartName(input.getName());
		}
		catch(Throwable e)
		{
			CoreException ce = BuckminsterException.wrap(e);
			throw new PartInitException(Messages.unable_to_open_editor, ce);
		}
		finally
		{
			IOUtils.close(stream);
		}

		m_compoundModifyListener = new CompoundModifyListener();
	}

	@Override
	public boolean isDirty()
	{
		return m_hasChanges;
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return true;
	}

	public boolean matches(IEditorReference editorRef, IEditorInput input)
	{
		IEditorPart part = (IEditorPart)editorRef.getPart(false);
		if(part != null)
		{
			IEditorInput editorInput = part.getEditorInput();
			if(editorInput != null)
			{
				if(editorInput.equals(input))
					return true;

				if(editorInput instanceof IDerivedEditorInput)
				{
					IEditorInput originalEditorInput = ((IDerivedEditorInput)editorInput).getOriginalInput();
					if(originalEditorInput.equals(input))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setFocus()
	{
		m_tabFolder.setFocus();

		if(m_needsRefresh)
			refreshQuery();
	}

	private AdvisorNodeBuilder addEmptyNode()
	{
		AdvisorNodeBuilder node = new AdvisorNodeBuilder();
		node.setNamePattern(Pattern.compile("")); //$NON-NLS-1$
		m_componentQuery.addAdvisorNode(node);

		return node;
	}

	private void changeNodeSelection()
	{
		if(!saveLastNode())
		{
			if(m_lastSelectedNode != -1)
				m_nodeTable.getTable().setSelection(m_lastSelectedNode);

			return;
		}

		nodeSelectionEvent();
	}

	private boolean commitChangesToQuery()
	{
		if(m_nodeTable.getControl().isVisible())
			if(!saveLastNode())
				return false;

		m_componentQuery.setResourceMapURL(UiUtils.trimmedValue(m_requestURL));
		m_componentQuery.setPropertiesURL(UiUtils.trimmedValue(m_propertyURL));
		m_properties.fillProperties(m_componentQuery.getDeclaredProperties());

		String doc = UiUtils.trimmedValue(m_shortDesc);
		m_componentQuery.setShortDesc(doc);

		doc = UiUtils.trimmedValue(m_documentation);
		try
		{
			m_componentQuery.setDocumentation(doc == null
					? null
					: Documentation.parse(doc));
		}
		catch(CoreException e)
		{
			MessageDialog.openError(getSite().getShell(), null, e.getMessage());
			return false;
		}

		ComponentRequest[] requestRet = new ComponentRequest[1];
		String error = commitChanges(requestRet);
		if(error == null)
			m_componentQuery.setRootRequest(requestRet[0]);
		else
		{
			MessageDialog.openError(getSite().getShell(), null, error);
			return false;
		}
		return true;
	}

	private void createActionButtons(Composite parent)
	{
		Composite allButtonsBox = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		// layout.marginHeight = layout.marginWidth = 0;
		allButtonsBox.setLayout(layout);
		allButtonsBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		UiUtils.createCheckButton(allButtonsBox, Messages.continue_on_error, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				m_continueOnError = ((Button)e.getSource()).getSelection();
			}
		});

		Composite pressButtonsBox = new Composite(allButtonsBox, SWT.NONE);
		layout = new GridLayout(3, true);
		layout.marginHeight = layout.marginWidth = 0;
		pressButtonsBox.setLayout(layout);
		pressButtonsBox.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

		m_resolveButton = UiUtils.createPushButton(pressButtonsBox, Messages.resolve_to_wizard, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				loadComponent(false);
			}
		});
		m_resolveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		m_materializeButton = UiUtils.createPushButton(pressButtonsBox, Messages.resolve_and_materialize,
				new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						loadComponent(true);
					}
				});
		m_materializeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		m_externalSaveAsButton = UiUtils.createPushButton(pressButtonsBox, Messages.external_save_as,
				new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						doExternalSaveAs();
					}
				});
		m_externalSaveAsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	}

	private void createButtonBox(Composite parent)
	{
		Composite buttonBox = new Composite(parent, SWT.NULL);
		buttonBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.marginWidth = layout.marginHeight = 0;
		layout.spacing = 3;
		buttonBox.setLayout(layout);

		Composite buttonBox1 = new Composite(buttonBox, SWT.NULL);
		// buttonBox1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
		// false));
		layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginWidth = layout.marginHeight = 0;
		buttonBox1.setLayout(layout);

		Composite buttonBox2 = new Composite(buttonBox, SWT.NULL);
		// buttonBox2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
		// false));
		layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginWidth = layout.marginHeight = 0;
		buttonBox2.setLayout(layout);

		m_newButton = UiUtils.createPushButton(buttonBox1, Messages.new_label, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				newNode();
			}
		});

		m_removeButton = UiUtils.createPushButton(buttonBox1, Messages.remove, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				removeNode();
			}
		});

		m_moveUpButton = UiUtils.createPushButton(buttonBox2, Messages.move_up, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				swapAndReselect(0, -1);
			}
		});

		m_moveDownButton = UiUtils.createPushButton(buttonBox2, Messages.move_down, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				swapAndReselect(1, 0);
			}
		});
	}

	private void createNodeFields(Composite parent)
	{
		createNodeTree(parent);

		createNodeStack(parent);
	}

	private void createNodeStack(Composite parent)
	{
		m_nodesStackComposite = new Composite(parent, SWT.NONE);
		m_nodesStackComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		m_nodesStackLayout = new StackLayout();
		m_nodesStackLayout.marginHeight = m_nodesStackLayout.marginWidth = 0;
		m_nodesStackComposite.setLayout(m_nodesStackLayout);

		m_nodesHash = new HashMap<String, Control>();

		Composite geComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		geComposite.setLayout(layout);

		m_nodesHash.put(Messages.general, geComposite);

		EditorUtils.createHeaderLabel(geComposite, Messages.general, 2);

		UiUtils.createGridLabel(geComposite, Messages.name_pattern_with_colon, 1, 0, SWT.NONE);

		m_namePattern = UiUtils.createGridText(geComposite, 1, 0, SWT.NONE);
		m_namePattern.addModifyListener(m_compoundModifyListener);

		UiUtils.createGridLabel(geComposite, Messages.matched_component_type_with_colon, 1, 0, SWT.NONE);

		m_category = UiUtils.createGridCombo(geComposite, 1, 0, null, null, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SIMPLE);
		m_category.setItems(AbstractComponentType.getComponentTypeIDs(true));
		m_category.addModifyListener(m_compoundModifyListener);

		UiUtils.createGridLabel(geComposite, Messages.filter_with_colon, 1, 0, SWT.NONE);

		m_filter = UiUtils.createGridText(geComposite, 1, 0, SWT.NONE);
		m_filter.addModifyListener(m_compoundModifyListener);

		UiUtils.createGridLabel(geComposite, Messages.skip_component_with_colon, 1, 0, SWT.NONE);
		m_skipComponent = UiUtils.createCheckButton(geComposite, null, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				enableDisableSkipSensitive(false);
			}
		});
		m_skipComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		m_skipComponent.addSelectionListener(m_compoundModifyListener);

		UiUtils.createGridLabel(geComposite, Messages.allow_circular_dependency_with_colon, 1, 0, SWT.NONE);
		m_allowCircular = UiUtils.createCheckButton(geComposite, null, null);
		m_allowCircular.addSelectionListener(m_compoundModifyListener);

		Composite aqComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		aqComposite.setLayout(layout);

		m_nodesHash.put(Messages.attribute_qualification, aqComposite);

		EditorUtils.createHeaderLabel(aqComposite, Messages.attribute_qualification, 2);

		UiUtils.createGridLabel(aqComposite, Messages.attributes_with_colon, 1, 0, SWT.NONE);
		m_wantedAttributes = UiUtils.createGridText(aqComposite, 0, 0, SWT.NONE);
		m_wantedAttributes.addModifyListener(m_compoundModifyListener);
		UiUtils.createGridLabel(aqComposite, Messages.prune_according_to_attributes_with_colon, 1, 0, SWT.NONE);
		m_prune = UiUtils.createCheckButton(aqComposite, null, null);
		m_prune.addSelectionListener(m_compoundModifyListener);

		Composite srComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		srComposite.setLayout(layout);

		m_nodesHash.put(Messages.special_requirements, srComposite);

		EditorUtils.createHeaderLabel(srComposite, Messages.special_requirements, 2);

		UiUtils.createGridLabel(srComposite, Messages.mutable_level_with_colon, 1, 0, SWT.NONE);
		m_mutableLevel = UiUtils.createGridEnumCombo(srComposite, 0, 0, MutableLevel.values(), null, null, SWT.NONE);
		m_mutableLevel.addModifyListener(m_compoundModifyListener);
		UiUtils.createGridLabel(srComposite, Messages.source_level_with_colon, 1, 0, SWT.NONE);
		m_sourceLevel = UiUtils.createGridEnumCombo(srComposite, 0, 0, SourceLevel.values(), null, null, SWT.NONE);
		m_sourceLevel.addModifyListener(m_compoundModifyListener);

		Composite kuComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		kuComposite.setLayout(layout);

		m_nodesHash.put(Messages.resolution_scope, kuComposite);

		EditorUtils.createHeaderLabel(kuComposite, Messages.resolution_scope, 2);

		UiUtils.createGridLabel(kuComposite, Messages.target_platform_with_colon, 1, 0, SWT.NONE);
		m_useTargetPlatform = UiUtils.createCheckButton(kuComposite, null, null);
		m_useTargetPlatform.addSelectionListener(m_compoundModifyListener);
		UiUtils.createGridLabel(kuComposite, Messages.workspace_with_colon, 1, 0, SWT.NONE);
		m_useWorkspace = UiUtils.createCheckButton(kuComposite, null, null);
		m_useWorkspace.addSelectionListener(m_compoundModifyListener);
		UiUtils.createGridLabel(kuComposite, Messages.materialization_with_colon, 1, 0, SWT.NONE);
		m_useMaterialization = UiUtils.createCheckButton(kuComposite, null, null);
		m_useMaterialization.addSelectionListener(m_compoundModifyListener);
		UiUtils.createGridLabel(kuComposite, Messages.resolution_service_with_colon, 1, 0, SWT.NONE);
		m_useResolutionService = UiUtils.createCheckButton(kuComposite, null, null);
		m_useResolutionService.addSelectionListener(m_compoundModifyListener);

		Composite scComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		scComposite.setLayout(layout);

		m_nodesHash.put(Messages.selection_criteria, scComposite);
		EditorUtils.createHeaderLabel(scComposite, Messages.selection_criteria, 2);

		UiUtils.createGridLabel(scComposite, Messages.branch_or_tag_path_with_colon, 1, 0, SWT.NONE);
		m_branchTagPath = UiUtils.createGridText(scComposite, 1, 0, SWT.NONE);
		m_branchTagPath.addModifyListener(m_compoundModifyListener);

		UiUtils.createGridLabel(scComposite, Messages.timestamp_with_colon, 1, 0, SWT.NONE);
		m_timestamp = UiUtils.createGridText(scComposite, 1, 0, SWT.NONE);
		m_timestamp.addModifyListener(m_compoundModifyListener);
		UiUtils.createGridLabel(scComposite, Messages.revision_with_colon, 1, 0, SWT.NONE);
		m_revision = UiUtils.createGridText(scComposite, 1, 0, SWT.NONE);
		m_revision.addModifyListener(m_compoundModifyListener);

		Composite ovComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		ovComposite.setLayout(layout);

		m_nodesHash.put(Messages.override, ovComposite);

		EditorUtils.createHeaderLabel(ovComposite, Messages.override, 3);

		UiUtils.createGridLabel(ovComposite, Messages.override_version, 1, 0, SWT.NONE);
		m_enableOverride = UiUtils.createCheckButton(ovComposite, null, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				boolean selected = ((Button)e.getSource()).getSelection();
				m_versionOverride.setEnabled(selected);
			}
		});
		m_enableOverride.addSelectionListener(m_compoundModifyListener);
		UiUtils.createEmptyLabel(ovComposite);

		m_versionOverride = new VersionDesignator(ovComposite);
		m_versionOverride.addVersionDesignatorListener(m_compoundModifyListener);

		Composite ofComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		ofComposite.setLayout(layout);

		m_nodesHash.put(Messages.overlay_folder, ofComposite);

		EditorUtils.createHeaderLabel(ofComposite, Messages.overlay_folder_for_prototyping, 2);

		UiUtils.createGridLabel(ofComposite, Messages.folder_with_colon, 1, 0, SWT.NONE);
		m_overlayFolder = UiUtils.createGridText(ofComposite, 1, 0, SWT.NONE);
		m_overlayFolder.addModifyListener(m_compoundModifyListener);
		Label label = UiUtils.createEmptyLabel(ofComposite);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		m_overlayBrowseButton = new Button(ofComposite, SWT.PUSH);
		m_overlayBrowseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		m_overlayBrowseButton.setText(Messages.browse_with_dots);
		m_overlayBrowseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent se)
			{
				DirectoryDialog dlg = new DirectoryDialog(getSite().getShell());
				m_overlayFolder.setText(TextUtils.notNullString(dlg.open()));
			}
		});

		Composite prComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		prComposite.setLayout(layout);

		m_nodesHash.put(Messages.properties, prComposite);

		EditorUtils.createHeaderLabel(prComposite, Messages.properties, 1);

		m_nodeProperties = new Properties(prComposite, SWT.NONE);
		m_nodeProperties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		m_nodeProperties.addPropertiesModifyListener(m_compoundModifyListener);

		Composite docComposite = new Composite(m_nodesStackComposite, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = 0;
		docComposite.setLayout(layout);

		m_nodesHash.put(Messages.documentation, docComposite);

		EditorUtils.createHeaderLabel(docComposite, Messages.documentation, 1);

		m_nodeDocumentation = UiUtils.createGridText(docComposite, 1, 0, SWT.MULTI);
		m_nodeDocumentation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		m_nodeDocumentation.addModifyListener(m_compoundModifyListener);

		initStackControl();
		/*
		 * // set the same height for nodeTable and node Tree int height = m_nodeTree.computeSize(SWT.DEFAULT,
		 * SWT.DEFAULT).y + 35;
		 * 
		 * Table table = (Table) m_nodeTable.getControl(); GridData gridData = (GridData) table.getLayoutData();
		 * gridData.heightHint = height; table.setLayoutData(gridData);
		 * 
		 * gridData = (GridData) m_nodeTree.getLayoutData(); gridData.heightHint = height;
		 * m_nodeTree.setLayoutData(gridData);
		 * 
		 * gridData = (GridData) m_nodesStackComposite.getLayoutData(); gridData.heightHint = height + 21;
		 * m_nodesStackComposite.setLayoutData(gridData);
		 */
	}

	private void createNodeTableGroup(Composite parent)
	{
		Composite componentTableGroup = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, true);
		gl.marginHeight = gl.marginWidth = 0;
		componentTableGroup.setLayout(gl);
		componentTableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Table table = new Table(componentTableGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);

		table.setHeaderVisible(false);

		String[] columnNames = new String[] { Messages.name_pattern, Messages.component_type, Messages.filter };
		int[] columnWeights = new int[] { 10, 5, 5 };

		table.setHeaderVisible(true);
		DynamicTableLayout layout = new DynamicTableLayout(50);
		for(int idx = 0; idx < columnNames.length; idx++)
		{
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT, idx);
			tableColumn.setText(columnNames[idx]);
			layout.addColumnData(new ColumnWeightData(columnWeights[idx], true));
		}
		table.setLayout(layout);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		m_nodeTable = new TableViewer(table);
		m_nodeTable.setLabelProvider(new AdvisorNodeLabelProvider());
		m_nodeTable.setContentProvider(new ArrayContentProvider());
		m_nodeTable.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				changeNodeSelection();
			}
		});

		createButtonBox(componentTableGroup);
	}

	private void createNodeTree(Composite parent)
	{
		m_nodeTree = new Tree(parent, SWT.BORDER);
		m_nodeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		m_nodeTree.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if(e.item != null)
				{
					TreeItem item = (TreeItem)e.item;
					m_nodesStackLayout.topControl = m_nodesHash.get(item.getText());
					m_nodesStackComposite.layout();
				}
			}
		});

		TreeItem item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.general);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.attribute_qualification);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.special_requirements);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.resolution_scope);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.selection_criteria);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.override);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.overlay_folder);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.properties);

		item = new TreeItem(m_nodeTree, SWT.NONE);
		item.setText(Messages.documentation);
	}

	private void enableDisableButtonGroup()
	{
		Table table = m_nodeTable.getTable();
		int top = table.getItemCount();
		int idx = table.getSelectionIndex();
		m_newButton.setText(Messages.new_label);
		m_removeButton.setEnabled(idx >= 0);
		m_moveUpButton.setEnabled(idx > 0);
		m_moveDownButton.setEnabled(idx >= 0 && idx < top - 1);

		boolean disableFields = getSelectionIndex() == -1;
		m_namePattern.setEnabled(!disableFields);
		m_filter.setEnabled(!disableFields);
		m_category.setEnabled(!disableFields);
		m_skipComponent.setEnabled(!disableFields);
		m_nodeDocumentation.setEnabled(!disableFields);
		enableDisableSkipSensitive(disableFields);
	}

	private void enableDisableSkipSensitive(boolean forceDisable)
	{
		boolean enableRest = !forceDisable && !m_skipComponent.getSelection();

		m_allowCircular.setEnabled(enableRest);
		m_overlayFolder.setEnabled(enableRest);
		m_overlayBrowseButton.setEnabled(enableRest);
		m_wantedAttributes.setEnabled(enableRest);
		m_prune.setEnabled(enableRest);

		m_mutableLevel.setEnabled(enableRest);
		m_sourceLevel.setEnabled(enableRest);

		m_useTargetPlatform.setEnabled(enableRest);
		m_useWorkspace.setEnabled(enableRest);
		m_useMaterialization.setEnabled(enableRest);
		m_useResolutionService.setEnabled(enableRest);

		m_branchTagPath.setEnabled(enableRest);
		m_timestamp.setEnabled(enableRest);
		m_revision.setEnabled(enableRest);

		m_enableOverride.setEnabled(enableRest);
		m_versionOverride.setEnabled(enableRest && m_enableOverride.getSelection());

		m_nodeProperties.setEnabled(enableRest);
	}

	private Control getAdvisorTabControl(Composite parent)
	{
		Composite tabComposite = EditorUtils.getNamedTabComposite(parent, Messages.advisor_nodes);

		Composite advisorComposite = new Composite(tabComposite, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = layout.marginWidth = 0;
		advisorComposite.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		advisorComposite.setLayoutData(gridData);

		createNodeTableGroup(advisorComposite);

		createNodeFields(advisorComposite);

		return EditorUtils.getOptimizedControl(tabComposite);
	}

	private String getCQueryXML()
	{
		String cqueryXML = ""; //$NON-NLS-1$
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Utils.serialize(m_componentQuery.createComponentQuery(), baos);
			cqueryXML = baos.toString();
		}
		catch(Exception e)
		{
			// nothing
		}
		return cqueryXML;
	}

	private Control getDocumentationTabControl(Composite parent)
	{
		Composite tabComposite = EditorUtils.getNamedTabComposite(parent, Messages.documentation);

		Composite descComposite = new Composite(tabComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		descComposite.setLayout(layout);
		descComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		UiUtils.createGridLabel(descComposite, Messages.short_description_with_colon, 1, 0, SWT.NONE);
		m_shortDesc = UiUtils.createGridText(descComposite, 1, 0, SWT.NONE, m_compoundModifyListener);

		Label label = UiUtils.createGridLabel(descComposite, Messages.documentation_with_colon, 1, 0, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
		m_documentation = UiUtils.createGridText(descComposite, 1, 0, SWT.MULTI | SWT.V_SCROLL,
				m_compoundModifyListener);
		m_documentation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return EditorUtils.getOptimizedControl(tabComposite);
	}

	private Control getMainTabControl(CTabFolder parent)
	{
		Composite tabComposite = EditorUtils.getNamedTabComposite(parent, Messages.main);

		Composite nameComposite = new Composite(tabComposite, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginRight = 8;
		layout.marginHeight = layout.marginWidth = 0;
		nameComposite.setLayout(layout);
		nameComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		Label label = UiUtils.createGridLabel(nameComposite, Messages.component_name_with_colon, 1, 0, SWT.NONE);
		int labelWidth = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 5;
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.widthHint = labelWidth;
		label.setLayoutData(gridData);

		m_componentName = UiUtils.createGridText(nameComposite, 2, 0, SWT.NONE, m_compoundModifyListener);

		UiUtils.createGridLabel(nameComposite, Messages.component_type_with_colon, 1, 0, SWT.NONE);
		m_componentType = UiUtils.createGridCombo(nameComposite, 1, 0, null, null, SWT.DROP_DOWN | SWT.READ_ONLY
				| SWT.SIMPLE);

		m_componentType.setItems(AbstractComponentType.getComponentTypeIDs(true));
		m_componentType.addModifyListener(m_compoundModifyListener);

		// not nice but I had to make equal 2 columns form different Composites
		// the purpose of hlpComposite is to create empty space, the same size
		// as m_componentCategory
		UiUtils.createEmptyPanel(nameComposite);

		int textWidth = m_componentType.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		gridData = (GridData)m_componentType.getLayoutData();
		gridData.widthHint = textWidth;
		m_componentType.setLayoutData(gridData);

		Group versionGroup = new Group(tabComposite, SWT.NONE);
		versionGroup.setText(Messages.version);
		layout = new GridLayout(3, false);
		versionGroup.setLayout(layout);
		versionGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		m_versionDesignator = new VersionDesignator(versionGroup);
		m_versionDesignator.addVersionDesignatorListener(m_compoundModifyListener);

		Control control = m_versionDesignator.getVersionDsTypeLabel();
		gridData = (GridData)control.getLayoutData();
		gridData.widthHint = labelWidth - layout.marginWidth - 3;
		control.setLayoutData(gridData);

		control = m_versionDesignator.getVersionDsTypeCombo();
		gridData = (GridData)control.getLayoutData();
		gridData.widthHint = textWidth;
		control.setLayoutData(gridData);

		Group propertiesGroup = new Group(tabComposite, SWT.NO_RADIO_GROUP);

		propertiesGroup.setText(Messages.properties);
		layout = new GridLayout(2, false);
		propertiesGroup.setLayout(layout);
		propertiesGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		m_propertyURLCheckbox = UiUtils.createCheckButton(propertiesGroup, Messages.user_properties,
				new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						Button button = (Button)e.widget;

						if(!button.getSelection())
						{
							m_propertyURL.setText(""); //$NON-NLS-1$
						}
					}
				});
		gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gridData.horizontalSpan = 2;
		m_propertyURLCheckbox.setLayoutData(gridData);

		label = UiUtils.createGridLabel(propertiesGroup, Messages.properties_with_colon, 1, labelWidth
				- layout.marginWidth - 3, SWT.NONE);

		Composite propertiesComposite = new Composite(propertiesGroup, SWT.NONE);

		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		propertiesComposite.setLayout(layout);
		propertiesComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		m_propertyURL = UiUtils.createGridText(propertiesComposite, 1, 0, SWT.NONE, m_compoundModifyListener);
		Button browseButton = new Button(propertiesComposite, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		browseButton.setText(Messages.browse_with_dots);
		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent se)
			{
				FileDialog dlg = new FileDialog(getSite().getShell());
				dlg.setFilterExtensions(new String[] { "*.properties" }); //$NON-NLS-1$
				String name = dlg.open();
				if(name == null)
					return;
				try
				{
					m_propertyURL.setText(TextUtils.notNullString(new URL(name)));
				}
				catch(MalformedURLException e)
				{
					try
					{
						m_propertyURL.setText(TextUtils.notNullString(new File(name).toURI().toURL()));
					}
					catch(MalformedURLException e1)
					{
					}
				}
			}
		});

		m_propertyURLCheckbox.addSelectionListener(new CheckboxSelectionListener(new Control[] { label, m_propertyURL,
				browseButton }));

		Group rmapGroup = new Group(tabComposite, SWT.NO_RADIO_GROUP);
		rmapGroup.setText(Messages.resource_map);
		layout = new GridLayout(2, false);
		rmapGroup.setLayout(layout);
		rmapGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		m_requestURLCheckbox = UiUtils.createCheckButton(rmapGroup, Messages.use_resource_map, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Button button = (Button)e.widget;

				if(!button.getSelection())
				{
					m_requestURL.setText(""); //$NON-NLS-1$
				}
			}
		});
		gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gridData.horizontalSpan = 2;
		m_requestURLCheckbox.setLayoutData(gridData);

		label = UiUtils.createGridLabel(rmapGroup, Messages.rmap_url_with_colon, 1,
				labelWidth - layout.marginWidth - 3, SWT.NONE);

		Composite rmapComposite = new Composite(rmapGroup, SWT.NONE);

		layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		rmapComposite.setLayout(layout);
		rmapComposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		m_requestURL = UiUtils.createGridText(rmapComposite, 1, 0, SWT.NONE, m_compoundModifyListener);
		browseButton = new Button(rmapComposite, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		browseButton.setText(Messages.browse_with_dots);
		browseButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent se)
			{
				FileDialog dlg = new FileDialog(getSite().getShell());
				dlg.setFilterExtensions(new String[] { "*.rmap" }); //$NON-NLS-1$
				String name = dlg.open();
				if(name == null)
					return;
				try
				{
					m_requestURL.setText(TextUtils.notNullString(new URL(name)));
				}
				catch(MalformedURLException e)
				{
					try
					{
						m_requestURL.setText(TextUtils.notNullString(new File(name).toURI().toURL()));
					}
					catch(MalformedURLException e1)
					{
					}
				}
			}
		});

		m_requestURLCheckbox.addSelectionListener(new CheckboxSelectionListener(new Control[] { label, m_requestURL,
				browseButton }));

		return EditorUtils.getOptimizedControl(tabComposite);
	}

	private Control getPropertiesTabControl(Composite parent)
	{
		Composite tabComposite = EditorUtils.getNamedTabComposite(parent, Messages.properties);

		/*
		 * Group propertiesGroup = new Group(tabComposite, SWT.NONE); propertiesGroup.setText("Properties"); GridLayout
		 * layout = new GridLayout(1, false); propertiesGroup.setLayout(layout); propertiesGroup.setLayoutData(new
		 * GridData(GridData.FILL, GridData.FILL, true, true));
		 * 
		 * m_properties = UiUtils.createNoBorderGridText(propertiesGroup, 1, 0, compoundModifyListener, SWT.MULTI);
		 * m_properties.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		 */
		m_properties = new Properties(tabComposite, SWT.NONE);
		m_properties.addPropertiesModifyListener(m_compoundModifyListener);

		return EditorUtils.getOptimizedControl(tabComposite);
	}

	private AdvisorNodeBuilder getSelectedNode()
	{
		int idx = m_nodeTable.getTable().getSelectionIndex();
		return idx >= 0
				? (AdvisorNodeBuilder)m_nodeTable.getElementAt(idx)
				: null;
	}

	private int getSelectionIndex()
	{
		return m_nodeTable.getTable().getSelectionIndex();
	}

	private Control getXMLTabControl(Composite parent)
	{
		Composite tabComposite = EditorUtils.getNamedTabComposite(parent, Messages.xml_content);

		Composite xmlComposite = new Composite(tabComposite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		xmlComposite.setLayout(layout);
		xmlComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		m_xml = UiUtils.createGridText(xmlComposite, 1, 0, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY,
				null);
		m_xml.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return EditorUtils.getOptimizedControl(tabComposite);
	}

	private void initStackControl()
	{
		m_nodeTree.setSelection(m_nodeTree.getItem(0));
		m_nodesStackLayout.topControl = m_nodesHash.get(m_nodeTree.getItem(0).getText());
		m_nodesStackComposite.layout();
	}

	private void loadComponent(boolean materialize)
	{
		if(!commitChangesToQuery())
			return;

		try
		{
			ResolveJob resolveJob = new ResolveJob(m_componentQuery.createComponentQuery(), materialize, getSite(),
					m_continueOnError);
			resolveJob.schedule();
		}
		catch(CoreException e)
		{
			ErrorDialog.openError(getSite().getShell(), null, null, e.getStatus());
		}
	}

	private void newNode()
	{
		if(!saveLastNode())
			return;

		AdvisorNodeBuilder node = addEmptyNode();
		refreshList();
		selectRow(node);

		setDirty(true);

		nodeSelectionEvent();
	}

	private void nodeSelectionEvent()
	{
		updateLastNode();
		refreshNodeFields();
		enableDisableButtonGroup();
		initStackControl();
		m_namePattern.setFocus();
	}

	private void refreshList()
	{
		m_nodeTable.setInput(m_componentQuery.getAdvisoryNodes());
	}

	private void refreshNodeFields()
	{
		try
		{
			m_suppressModifyListener = true;

			AdvisorNodeBuilder node = getSelectedNode();
			if(node == null)
				//
				// Use an empty node as template to get the defaults right.
				//
				node = new AdvisorNodeBuilder();

			m_allowCircular.setSelection(node.allowCircularDependency());
			m_namePattern.setText(TextUtils.notNullString(node.getNamePattern()));
			m_filter.setText(TextUtils.notNullString(node.getFilter()));
			m_category.select(m_category.indexOf(TextUtils.notNullString(node.getComponentTypeID())));
			m_overlayFolder.setText(TextUtils.notNullString(node.getOverlayFolder()));
			m_wantedAttributes.setText(TextUtils.notNullString(TextUtils.concat(node.getAttributes(), ","))); //$NON-NLS-1$
			m_prune.setSelection(node.isPrune());
			m_mutableLevel.select(m_mutableLevel.indexOf(node.getMutableLevel().toString()));
			m_sourceLevel.select(m_sourceLevel.indexOf(node.getSourceLevel().toString()));
			m_skipComponent.setSelection(node.skipComponent());
			m_useTargetPlatform.setSelection(node.isUseTargetPlatform());
			m_useWorkspace.setSelection(node.isUseWorkspace());
			m_useMaterialization.setSelection(node.isUseMaterialization());
			m_useResolutionService.setSelection(node.isUseRemoteResolution());

			m_branchTagPath.setText(TextUtils.notNullString(VersionSelector.toString(node.getBranchTagPath())));
			m_revision.setText(TextUtils.notNullString(node.getRevision()));
			Date timestamp = node.getTimestamp();
			m_timestamp.setText(timestamp == null
					? "" : m_timestampFormat.format(timestamp)); //$NON-NLS-1$

			VersionRange vs = node.getVersionOverride();
			boolean enableOverride = (vs != null);
			m_enableOverride.setSelection(enableOverride);
			m_versionOverride.setEnabled(enableOverride);
			m_versionOverride.refreshValues(vs);

			m_nodeProperties.setProperties(node.getProperties());
			m_nodeProperties.refreshList();

			Documentation doc = node.getDocumentation();
			m_nodeDocumentation.setText(TextUtils.notNullString(doc == null
					? null
					: doc.toString()));
		}
		finally
		{
			m_suppressModifyListener = false;
		}
	}

	private void refreshQuery()
	{
		setDirty(false);
		m_mute = true;
		try
		{
			ComponentRequest request = m_componentQuery.getRootRequest();
			m_componentName.setText(TextUtils.notNullString(request.getName()));
			m_componentType.select(m_componentType.indexOf(TextUtils.notNullString(request.getComponentTypeID())));
			m_versionDesignator.refreshValues(request.getVersionRange());

			String string = TextUtils.notNullString(m_componentQuery.getPropertiesURL());
			m_propertyURL.setText(string);
			m_propertyURLCheckbox.setSelection(string.length() > 0);
			m_propertyURLCheckbox.notifyListeners(SWT.Selection, new Event());

			string = TextUtils.notNullString(m_componentQuery.getResourceMapURL());
			m_requestURL.setText(string);
			m_requestURLCheckbox.setSelection(string.length() > 0);
			m_requestURLCheckbox.notifyListeners(SWT.Selection, new Event());
			m_properties.setProperties(m_componentQuery.getDeclaredProperties());
			m_shortDesc.setText(TextUtils.notNullString(m_componentQuery.getShortDesc()));
			Documentation doc = m_componentQuery.getDocumentation();
			m_documentation.setText(TextUtils.notNullString(doc == null
					? "" //$NON-NLS-1$
					: doc.toString()));
			refreshList();
			m_properties.refreshList();
			m_needsRefresh = false;
			nodeSelectionEvent();
		}
		finally
		{
			m_mute = false;
		}
	}

	private void removeNode()
	{
		AdvisorNodeBuilder node = getSelectedNode();
		if(node != null)
		{
			int last_idx = getSelectionIndex();

			m_componentQuery.removeAdvisorNode(node);
			setDirty(true);
			m_lastEditedNode = DONT_SAVE;
			refreshList();

			if(m_componentQuery.getAdvisoryNodes().size() > last_idx)
			{
				m_nodeTable.getTable().setSelection(last_idx);
			}
			else if(m_componentQuery.getAdvisoryNodes().size() > 0)
			{
				m_nodeTable.getTable().setSelection(last_idx - 1);
			}
			else
			{
				m_nodeTable.getTable().deselectAll();
			}
			nodeSelectionEvent();
		}
	}

	private boolean saveLastNode()
	{
		return saveLastNode(EMPTY_ACTIVATOR);
	}

	private boolean saveLastNode(IActivator failureActivator)
	{
		if(m_lastEditedNode != -1 && m_lastEditedNode != DONT_SAVE)
			return saveNode(m_lastEditedNode, failureActivator);

		return true;
	}

	private boolean saveNode(int nodeIdx, IActivator failureActivator)
	{
		AdvisorNodeBuilder node = (AdvisorNodeBuilder)m_nodeTable.getElementAt(nodeIdx);
		boolean isNewNode = false;
		if(node == null)
		{
			node = new AdvisorNodeBuilder();
			isNewNode = true;
		}

		boolean refreshListNeeded = false;
		String patternStr = UiUtils.trimmedValue(m_namePattern);
		String category = m_category.getItem(m_category.getSelectionIndex());
		if(category.length() == 0)
			category = null;

		String filterStr = UiUtils.trimmedValue(m_filter);
		if(patternStr == null && filterStr == null && category == null)
		{
			failureActivator.activate();
			MessageDialog.openError(getSite().getShell(), null,
					Messages.name_pattern_component_type_or_filter_must_be_set);
			return false;
		}

		Pattern pattern = null;
		if(patternStr != null)
		{
			try
			{
				pattern = Pattern.compile(patternStr);
			}
			catch(PatternSyntaxException e)
			{
				failureActivator.activate();
				MessageDialog.openError(getSite().getShell(), null, e.getMessage());
				return false;
			}
		}

		Filter filter = null;
		if(filterStr != null)
		{
			try
			{
				filter = FilterFactory.newInstance(filterStr);
			}
			catch(InvalidSyntaxException e)
			{
				failureActivator.activate();
				MessageDialog.openError(getSite().getShell(), null, e.getMessage());
				return false;
			}
		}

		if(!(Trivial.equalsAllowNull(pattern, node.getNamePattern())
				&& Trivial.equalsAllowNull(category, node.getComponentTypeID()) && Trivial.equalsAllowNull(filter,
				node.getFilter())))
		{
			// Selection criteria changed. Verify that it's not a duplicate
			//
			AdvisorNodeBuilder patternEqual = m_componentQuery.getNodeByCriteria(pattern, category, filter);
			if(patternEqual != null)
			{
				failureActivator.activate();
				if(!MessageDialog.openQuestion(getSite().getShell(), null,
						Messages.overwrite_existing_node_with_same_criteria))
					return false;
				m_componentQuery.removeAdvisorNode(patternEqual);
			}
			refreshListNeeded = true;
		}
		node.setNamePattern(pattern);
		node.setFilter(filter);
		node.setComponentTypeID(category);
		node.setAllowCircularDependency(m_allowCircular.getSelection());

		boolean override = m_enableOverride.getSelection();
		VersionRange versionOverride = null;
		if(override)
			try
			{
				versionOverride = m_versionOverride.getDirectVersionDesignator();
			}
			catch(CoreException e1)
			{
				failureActivator.activate();
				ErrorDialog.openError(getSite().getShell(), null, null, e1.getStatus());
				return false;
			}

		try
		{
			String tmp = UiUtils.trimmedValue(m_overlayFolder);
			node.setOverlayFolder(tmp == null
					? null
					: URLUtils.normalizeToURL(tmp));
		}
		catch(Exception e)
		{
			failureActivator.activate();
			MessageDialog.openError(getSite().getShell(), null, e.getMessage());
			return false;
		}

		node.setSkipComponent(m_skipComponent.getSelection());

		node.getAttributes().clear();
		String tmp = UiUtils.trimmedValue(m_wantedAttributes);
		if(tmp != null)
			for(String attribute : tmp.split(",")) //$NON-NLS-1$
				node.addAttribute(attribute);
		node.setPrune(m_prune.getSelection());

		int idx = m_mutableLevel.getSelectionIndex();
		node.setMutableLevel(idx >= 0
				? MutableLevel.values()[idx]
				: null);

		idx = m_sourceLevel.getSelectionIndex();
		node.setSourceLevel(idx >= 0
				? SourceLevel.values()[idx]
				: null);

		node.setUseTargetPlatform(m_useTargetPlatform.getSelection());
		node.setUseWorkspace(m_useWorkspace.getSelection());
		node.setUseMaterialization(m_useMaterialization.getSelection());
		node.setUseRemoteResolution(m_useResolutionService.getSelection());

		node.setBranchTagPath(VersionSelector.fromPath(UiUtils.trimmedValue(m_branchTagPath)));

		node.setRevision(UiUtils.trimmedValue(m_revision));

		tmp = UiUtils.trimmedValue(m_timestamp);
		if(tmp != null)
		{
			try
			{
				node.setTimestamp(m_timestampFormat.parse(tmp));
			}
			catch(ParseException e)
			{
				failureActivator.activate();
				MessageDialog.openError(getSite().getShell(), null,
						Messages.timestamp_must_conform_to_format_with_colon + m_timestampFormat.toPattern());
				return false;
			}
		}
		else
			node.setTimestamp(null);

		node.setVersionOverride(versionOverride);

		m_nodeProperties.fillProperties(node.getProperties());

		String doc = UiUtils.trimmedValue(m_nodeDocumentation);

		try
		{
			node.setDocumentation(doc == null
					? null
					: Documentation.parse(doc));
		}
		catch(Exception e)
		{
			failureActivator.activate();
			MessageDialog.openError(getSite().getShell(), null, e.getMessage());
			return false;
		}

		if(isNewNode)
		{
			// This was an add operation
			//
			m_componentQuery.addAdvisorNode(node);
			refreshListNeeded = true;
		}
		if(refreshListNeeded)
		{
			refreshList();
			nodeSelectionEvent();
		}

		enableDisableButtonGroup();
		return true;
	}

	private void saveToPath(IPath path)
	{
		try
		{
			SaveRunnable sr = new SaveRunnable(m_componentQuery.createComponentQuery(), path);
			getSite().getWorkbenchWindow().run(true, true, sr);
			setInputWithNotify(sr.getSavedInput());
			setDirty(false);
			setPartName(path.lastSegment());
			firePropertyChange(IWorkbenchPart.PROP_TITLE);
		}
		catch(InvocationTargetException e)
		{
			CoreException t = BuckminsterException.wrap(e);
			String msg = Messages.unable_to_save_file_0 + path;
			CorePlugin.getLogger().error(t, msg);
			ErrorDialog.openError(getSite().getShell(), null, msg, t.getStatus());
		}
		catch(InterruptedException e)
		{
		}
	}

	private boolean selectRow(AdvisorNodeBuilder node)
	{
		int idx = m_componentQuery.getAdvisoryNodes().indexOf(node);

		if(idx == -1)
			return false;

		m_nodeTable.getTable().setSelection(idx);

		return true;
	}

	private void setDirty(boolean flag)
	{
		if(m_mute || m_hasChanges == flag)
			return;
		m_hasChanges = flag;
		firePropertyChange(PROP_DIRTY);
	}

	private void swapAndReselect(int idxOffset, int selectionOffset)
	{
		if(!saveLastNode())
		{
			return;
		}

		Table table = m_nodeTable.getTable();
		int idx = table.getSelectionIndex() + idxOffset;
		if(idx <= 0)
			return;

		List<AdvisorNodeBuilder> nl = m_componentQuery.getAdvisoryNodes();
		if(idx >= nl.size())
			return;

		nl.set(idx - 1, nl.set(idx, nl.get(idx - 1)));
		refreshList();
		table.select(idx + selectionOffset);
		nodeSelectionEvent();
		setDirty(true);
	}

	private void updateLastNode()
	{
		if(getSelectionIndex() != -1)
		{
			m_lastSelectedNode = getSelectionIndex();
		}

		m_lastEditedNode = getSelectionIndex();
	}
}
