/*****************************************************************************
 * Copyright (c) 2006-2013, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.builder;

/**
 * @author Thomas Hallgren
 */
public abstract class CSpecElementBuilder extends NamedElementBuilder {
	private final CSpecBuilder cspecBuilder;

	CSpecElementBuilder(CSpecBuilder cspecBuilder) {
		this.cspecBuilder = cspecBuilder;
	}

	public final CSpecBuilder getCSpecBuilder() {
		return cspecBuilder;
	}

	public final String getCSpecName() {
		return cspecBuilder.getName();
	}
}
