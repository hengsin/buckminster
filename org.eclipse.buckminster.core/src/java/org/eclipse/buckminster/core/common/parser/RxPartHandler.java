/*******************************************************************************
 * Copyright (c) 2004, 2005
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.core.common.parser;

import org.eclipse.buckminster.core.common.model.RxPart;
import org.eclipse.buckminster.core.parser.ExtensionAwareHandler;
import org.eclipse.buckminster.sax.AbstractHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Thomas Hallgren
 */
abstract class RxPartHandler extends ExtensionAwareHandler {
	private String name;

	private boolean optional;

	public RxPartHandler(AbstractHandler parent) {
		super(parent);
	}

	@Override
	public void handleAttributes(Attributes attrs) throws SAXException {
		name = getNameAttributeValue(attrs);
		optional = getOptionalBooleanValue(attrs, RxPart.ATTR_OPTIONAL, false);
	}

	protected abstract RxPart createPart();

	protected final String getName() {
		return name;
	}

	protected String getNameAttributeValue(Attributes attrs) throws SAXException {
		return getOptionalStringValue(attrs, RxPart.ATTR_NAME);
	}

	protected final boolean isOptional() {
		return optional;
	}
}
