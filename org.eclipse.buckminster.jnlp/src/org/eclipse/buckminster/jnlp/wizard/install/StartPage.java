/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.jnlp.wizard.install;

import org.eclipse.buckminster.jnlp.MaterializationConstants;
import org.eclipse.buckminster.jnlp.Messages;
import org.eclipse.buckminster.jnlp.ui.UiUtils;
import org.eclipse.buckminster.jnlp.ui.general.wizard.AdvancedWizardDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Karel Brezina
 */
public class StartPage extends InstallWizardPage
{
	private Text m_artifactNameText;

	private Text m_artifactVersionText;

	private Text m_artifactDescriptionText;

	private Text m_artifactDocumentationText;

	private Text m_publisherInfoText;

	private IWizardPage m_nextPage;

	protected StartPage()
	{
		super(MaterializationConstants.STEP_START, Messages.materialization,
				Messages.please_verify_that_what_is_described_below_is_what_you_want_to_materialize, null);
	}

	public void createControl(Composite parent)
	{
		m_nextPage = getInstallWizard().getSelectDistroPage();

		Composite pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(new GridLayout(1, false));

		Group productGroup = new Group(pageComposite, SWT.NONE);
		productGroup.setText(Messages.product_summary);
		productGroup.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		productGroup.setLayoutData(gridData);

		new Label(productGroup, SWT.NONE).setText(Messages.name_with_colon);
		m_artifactNameText = new Text(productGroup, SWT.BORDER | SWT.NO_FOCUS | SWT.READ_ONLY | SWT.WRAP);
		m_artifactNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_artifactNameText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

		new Label(productGroup, SWT.NONE).setText(Messages.version_with_colon);
		m_artifactVersionText = new Text(productGroup, SWT.BORDER | SWT.NO_FOCUS | SWT.READ_ONLY | SWT.WRAP);
		m_artifactVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_artifactVersionText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

		new Label(productGroup, SWT.NONE).setText(Messages.description_with_colon);
		m_artifactDescriptionText = new Text(productGroup, SWT.BORDER | SWT.NO_FOCUS | SWT.READ_ONLY | SWT.WRAP);
		m_artifactDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		m_artifactDescriptionText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

		Label label = new Label(productGroup, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		gridData.verticalIndent = 2;
		label.setLayoutData(gridData);
		label.setText(Messages.documentation_with_colon);
		m_artifactDocumentationText = new Text(productGroup, SWT.BORDER | SWT.NO_FOCUS | SWT.MULTI | SWT.READ_ONLY
				| SWT.WRAP | SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 60;
		m_artifactDocumentationText.setLayoutData(gridData);
		m_artifactDocumentationText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

		new Label(pageComposite, SWT.NONE);

		Group publisherGroup = new Group(pageComposite, SWT.NONE);
		publisherGroup.setText(Messages.publisher_information);
		publisherGroup.setLayout(new GridLayout());
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		publisherGroup.setLayoutData(gridData);

		m_publisherInfoText = new Text(publisherGroup, SWT.BORDER | SWT.NO_FOCUS | SWT.MULTI | SWT.READ_ONLY | SWT.WRAP
				| SWT.V_SCROLL);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 30;
		m_publisherInfoText.setLayoutData(gridData);
		m_publisherInfoText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

		if(getInstallWizard().isLoginRequired()
				&& (!getInstallWizard().isLoggedIn() || getInstallWizard().isLoginPageRequested()))
		{
			Composite infoComposite = new Composite(pageComposite, SWT.NONE);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.horizontalSpan = 2;
			infoComposite.setLayoutData(data);
			infoComposite.setLayout(new GridLayout());

			Group infoGroup = new Group(pageComposite, SWT.BOTTOM);
			infoGroup.setText(Messages.info);
			FillLayout fillLayout = new FillLayout();
			fillLayout.marginHeight = fillLayout.marginWidth = 5;
			infoGroup.setLayout(fillLayout);
			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
			infoGroup.setLayoutData(data);

			final String message = Messages.note_that_on_request_of_the_publisher_of_this_material_you_will_be_asked_to_log_in_to;
			new Label(infoGroup, SWT.WRAP).setText(message + getInstallWizard().getServiceProvider());
		}

		setControl(pageComposite);
	}

	@Override
	public IWizardPage getNextPage()
	{
		return m_nextPage;
	}

	@Override
	public boolean isPageComplete()
	{
		return !getInstallWizard().isProblemInProperties();
	}

	@Override
	public boolean performPageCommit()
	{
		if(getInstallWizard().isLoginRequired()
				&& (!getInstallWizard().isLoggedIn() || getInstallWizard().isLoginPageRequested()))
			m_nextPage = getInstallWizard().getLoginPage();
		else if(getInstallWizard().isFolderRestrictionPageNeeded())
			m_nextPage = getInstallWizard().getFolderRestrictionPage();
		else
		{
			if(!getInstallWizard().isStackInfoRetrieved() && !getInstallWizard().isDistroRetrieved())
				getInstallWizard().retrieveStackInfo();

			if(getInstallWizard().isDistroRetrieved())
				m_nextPage = getInstallWizard().getDownloadPage();
			else
				m_nextPage = getInstallWizard().getSelectDistroPage();
		}

		return true;
	}

	@Override
	protected void beforeDisplaySetup()
	{
		m_artifactNameText.setText(getInstallWizard().getArtifactName());
		m_artifactVersionText.setText(getInstallWizard().getCSpecVersionString()
				+ " - " + getInstallWizard().getCSpecVersionType());//ArtifactVersion()); //$NON-NLS-1$
		m_artifactDescriptionText.setText(UiUtils.getNotNullString(getInstallWizard().getArtifactDescription()));
		m_artifactDocumentationText.setText(UiUtils.getNotNullString(getInstallWizard().getArtifactDocumentation()));
		m_publisherInfoText.setText(getInstallWizard().getBrandingString());

		// This might solve problems with redundant scroll bars on Mac
		((Composite)getControl()).layout();

		focusNextButton();
	}

	private void focusNextButton()
	{
		if(getInstallWizard().isProblemInProperties())
			((AdvancedWizardDialog)getContainer()).getButtonFromButtonArea(IDialogConstants.CANCEL_ID).setFocus();
		else
			((AdvancedWizardDialog)getContainer()).getButtonFromButtonArea(IDialogConstants.NEXT_ID).setFocus();
	}
}
