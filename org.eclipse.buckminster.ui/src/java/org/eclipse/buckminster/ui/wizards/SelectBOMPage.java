/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.core.helpers.AccessibleByteArrayOutputStream;
import org.eclipse.buckminster.core.helpers.FileUtils;
import org.eclipse.buckminster.core.metadata.model.BillOfMaterials;
import org.eclipse.buckminster.core.mspec.builder.MaterializationSpecBuilder;
import org.eclipse.buckminster.core.mspec.model.MaterializationSpec;
import org.eclipse.buckminster.core.parser.IParserFactory;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.core.resolver.IResolver;
import org.eclipse.buckminster.core.resolver.MainResolver;
import org.eclipse.buckminster.core.resolver.ResolutionContext;
import org.eclipse.buckminster.download.DownloadManager;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.runtime.IOUtils;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.buckminster.runtime.URLUtils;
import org.eclipse.buckminster.ui.Messages;
import org.eclipse.buckminster.ui.UiUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Thomas Hallgren
 */
public class SelectBOMPage extends AbstractQueryPage
{
	private Label m_topComponentLabel;

	private Text m_fileNameText;

	private Text m_topComponent;

	private Button m_loadButton;

	private URL m_bomOrMSpecURL;

	public SelectBOMPage(IStructuredSelection selection)
	{
		super(""); //$NON-NLS-1$
		if(selection != null && selection.size() == 1)
		{
			Object selected = selection.getFirstElement();
			if(selected instanceof IFile)
			{
				IPath location = ((IFile)selected).getLocation();
				if(location != null)
				{
					try
					{
						m_bomOrMSpecURL = location.toFile().toURI().toURL();
					}
					catch(MalformedURLException e)
					{
						// ignore
					}
				}
			}
		}
	}

	@Override
	public void pageIsShowing()
	{
		QueryWizard qw = getQueryWizard();
		if(m_bomOrMSpecURL != null && !qw.hasBOM())
		{
			loadBomOrMSpec();
			if(m_bomOrMSpecURL != null)
				m_fileNameText.setText(m_bomOrMSpecURL.toString());
		}
	}

	@Override
	public void setErrorMessage(String message)
	{
		QueryWizard qw = getQueryWizard();
		if(message != null)
		{
			m_bomOrMSpecURL = null;
			qw.resetBOM();
		}

		super.setErrorMessage(message);
		if(qw.hasBOM())
		{
			if(!isPageComplete())
			{
				try
				{
					m_topComponent.setText(qw.getBOM().getViewName());
					m_topComponentLabel.setVisible(true);
					m_topComponent.setVisible(true);
					setPageComplete(true);
				}
				catch(CoreException e)
				{
					displayException(e);
					qw.resetBOM();
				}
			}
		}
		else
		{
			if(isPageComplete())
			{
				m_topComponentLabel.setVisible(false);
				m_topComponent.setVisible(false);
				setPageComplete(false);
			}
		}
	}

	@Override
	protected Composite createControls(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label lbl = new Label(composite, SWT.NONE);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		lbl.setText(Messages.enter_url_of_mspec_bom_cquery);
		lbl.setToolTipText(Messages.enter_url_that_appoints_mspec_bom_cquery);

		m_fileNameText = new Text(composite, SWT.BORDER);
		m_fileNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		m_fileNameText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent me)
			{
				setBomOrMSpecFile(UiUtils.trimmedValue((Text)me.widget));
			}
		});

		Button browseButton = UiUtils.createPushButton(composite, Messages.browse_with_dots, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent se)
			{
				FileDialog dlg = new FileDialog(getShell());
				dlg.setFilterExtensions(new String[] { "*.mspec", "*.cquery", "*.bom" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String name = dlg.open();
				if(name != null)
					m_fileNameText.setText(name);
			}
		});
		browseButton.setLayoutData(new GridData(SWT.TRAIL, SWT.TOP, false, false));

		m_loadButton = UiUtils.createPushButton(composite, Messages.load, new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent se)
			{
				loadBomOrMSpec();
			}
		});
		m_loadButton.setLayoutData(new GridData(SWT.TRAIL, SWT.TOP, false, false));
		m_loadButton.setEnabled(false);

		m_topComponentLabel = new Label(composite, SWT.NONE);
		m_topComponentLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		m_topComponentLabel.setText(Messages.top_component_of_loaded_bom);
		m_topComponentLabel.setVisible(false);

		m_topComponent = new Text(composite, SWT.BORDER);
		m_topComponent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		m_topComponent.setEditable(false);
		m_topComponent.setVisible(false);

		setControl(composite);
		setPageComplete(false);
		return composite;
	}

	void loadBomOrMSpec()
	{
		try
		{
			getContainer().run(true, true, new IRunnableWithProgress()
			{
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					URL urlToParse = m_bomOrMSpecURL;
					InputStream input = null;
					monitor.beginTask(null, 100);
					try
					{
						AccessibleByteArrayOutputStream byteBld = new AccessibleByteArrayOutputStream();
						QueryWizard wizard = getQueryWizard();
						MaterializationSpecBuilder mspecBld = wizard.getMaterializationSpec();
						DownloadManager.readInto(urlToParse, null, byteBld, MonitorUtils.subMonitor(monitor, 20));
						input = byteBld.getInputStream();

						IParserFactory pf = CorePlugin.getDefault().getParserFactory();
						MaterializationSpec mspec = null;
						String path = urlToParse.getPath();
						if(!(path.endsWith(".cquery") || path.endsWith(".bom")))
						{
							try
							{
								mspec = pf.getMaterializationSpecParser(true).parse(urlToParse.toString(), input);
							}
							catch(CoreException e)
							{
								// Assume this was not an mspec
								//
								if(urlToParse.getPath().endsWith(".mspec"))
									throw e;
							}
						}
						MonitorUtils.worked(monitor, 5);

						if(mspec != null)
						{
							mspecBld.initFrom(mspec);
							urlToParse = mspec.getResolvedURL();
							byteBld.reset();
							DownloadManager.readInto(urlToParse, null, byteBld, MonitorUtils.subMonitor(monitor, 20));
						}
						else
						{
							mspecBld.setURL(urlToParse.toString());
							MonitorUtils.worked(monitor, 20);
						}
						input = byteBld.getInputStream();

						BillOfMaterials bom;
						ComponentQuery cquery = null;
						if(!urlToParse.getPath().endsWith(".bom"))
						{
							try
							{
								cquery = ComponentQuery.fromStream(urlToParse, null, input, true);
							}
							catch(CoreException e)
							{
								// Assume this was not a cquery, restart input
								//
								if(urlToParse.getPath().endsWith(".cquery"))
									throw e;
							}
						}
						MonitorUtils.worked(monitor, 5);

						if(cquery != null)
						{
							ResolutionContext ctx = new ResolutionContext(cquery);
							if(mspec != null)
							{
								// Add mspec properties but don't let them override
								//
								for(Map.Entry<String, String> entry : mspec.getProperties().entrySet())
								{
									String key = entry.getKey();
									if(!ctx.containsKey(key))
										ctx.put(key, entry.getValue());
								}
							}
							IResolver resolver = new MainResolver(ctx);
							resolver.getContext().setContinueOnError(true);
							bom = resolver.resolve(MonitorUtils.subMonitor(monitor, 40));
						}
						else
						{
							input = byteBld.getInputStream();
							bom = pf.getBillOfMaterialsParser(true).parse(urlToParse.toString(), input);
							MonitorUtils.worked(monitor, 40);
						}
						wizard.setBOM(bom);
						MonitorUtils.worked(monitor, 10);
					}
					catch(Throwable e)
					{
						throw new InvocationTargetException(e);
					}
					finally
					{
						IOUtils.close(input);
						monitor.done();
					}
				}
			});
			setErrorMessage(null);
		}
		catch(InterruptedException e)
		{
		}
		catch(Exception e)
		{
			displayException(BuckminsterException.wrap(e));
		}
	}

	void setBomOrMSpecFile(final String bomFile)
	{
		URL oldBomOrMSPecURL = m_bomOrMSpecURL;
		m_bomOrMSpecURL = null;
		if(bomFile == null)
		{
			setErrorMessage(null);
			m_loadButton.setEnabled(false);
			return;
		}

		URL url;
		try
		{
			url = URLUtils.normalizeToURL(bomFile);
			url = FileLocator.toFileURL(url);
		}
		catch(IOException e)
		{
			setErrorMessage(Messages.invalid_url_or_filename);
			m_loadButton.setEnabled(false);
			return;
		}

		File file = FileUtils.getFile(url);
		if(file == null)
		{
			// URL is remote. Let's assume it's OK for now
			//
			String path = url.getPath();
			if(path == null || path.length() == 0 || path.endsWith("/")) //$NON-NLS-1$
			{
				setErrorMessage(Messages.url_path_is_not_valid);
				m_loadButton.setEnabled(false);
			}
			else
			{
				m_bomOrMSpecURL = url;
				m_loadButton.setEnabled(true);
				setErrorMessage(null);
			}
		}
		else
		{
			m_loadButton.setEnabled(false);
			if(file.isFile() && file.canRead())
			{
				m_bomOrMSpecURL = url;
				if(!oldBomOrMSPecURL.equals(m_bomOrMSpecURL))
					loadBomOrMSpec();
			}
			else
				setErrorMessage(Messages.file_does_not_exist);
		}
	}
}
