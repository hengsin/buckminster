/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.equinox.p2.authoring.internal;

import java.io.File;

import org.eclipse.ui.IEditorInput;

/**
 * <code>ExternalFileEditorInput</code> that is derived from another <code>IEditorInput</code>
 * 
 * @author Karel Brezina
 */
public class DerivedExternalFileEditorInput extends ExternalFileEditorInput implements IDerivedEditorInput
{
	private IEditorInput m_originalInput;

	public DerivedExternalFileEditorInput(IEditorInput originalInput, File file, String label, String tooltipText)
	{
		super(file, label, tooltipText);
		m_originalInput = originalInput;
	}

	public IEditorInput getOriginalInput()
	{
		return m_originalInput;
	}

}
