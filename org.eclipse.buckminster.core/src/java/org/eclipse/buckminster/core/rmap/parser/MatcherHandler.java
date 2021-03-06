/*******************************************************************************
 * Copyright (c) 2004, 2006
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.core.rmap.parser;

import org.eclipse.buckminster.core.parser.ExtensionAwareHandler;
import org.eclipse.buckminster.core.rmap.model.Locator;
import org.eclipse.buckminster.core.rmap.model.Matcher;
import org.eclipse.buckminster.core.rmap.model.Redirect;
import org.eclipse.buckminster.core.rmap.model.ResourceMap;
import org.eclipse.buckminster.osgi.filter.Filter;
import org.eclipse.buckminster.osgi.filter.FilterFactory;
import org.eclipse.buckminster.sax.AbstractHandler;
import org.osgi.framework.InvalidSyntaxException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Thomas Hallgren
 */
abstract class MatcherHandler extends ExtensionAwareHandler {
	static class LocatorHandler extends MatcherHandler {
		static final String TAG = Locator.TAG;

		public LocatorHandler(AbstractHandler parent) {
			super(parent);
		}

		@Override
		public void handleAttributes(Attributes attrs) throws SAXException {
			super.handleAttributes(attrs);
			ResourceMap rmap = getResourceMap();
			rmap.addMatcher(new Locator(rmap, getPattern(), getStringValue(attrs, Locator.ATTR_SEARCH_PATH_REF), resolutionFilter,
					getOptionalBooleanValue(attrs, Locator.ATTR_FAIL_ON_ERROR, true)));
		}
	}

	static class RedirectHandler extends MatcherHandler {
		static final String TAG = Redirect.TAG;

		public RedirectHandler(AbstractHandler parent) {
			super(parent);
		}

		@Override
		public void handleAttributes(Attributes attrs) throws SAXException {
			super.handleAttributes(attrs);
			ResourceMap rmap = getResourceMap();
			String href = getStringValue(attrs, Redirect.ATTR_HREF);
			rmap.addMatcher(new Redirect(rmap, getPattern(), resolutionFilter, href));
		}
	}

	private String pattern;

	Filter resolutionFilter;

	public MatcherHandler(AbstractHandler parent) {
		super(parent);
	}

	@Override
	public void handleAttributes(Attributes attrs) throws SAXException {
		pattern = getOptionalStringValue(attrs, "pattern"); //$NON-NLS-1$
		String resFilter = getOptionalStringValue(attrs, Matcher.ATTR_RESOLUTION_FILTER);
		if (resFilter != null) {
			try {
				resolutionFilter = FilterFactory.newInstance(resFilter);
			} catch (InvalidSyntaxException e) {
				throw new SAXParseException(e.getMessage(), getDocumentLocator(), e);
			}
		} else
			resolutionFilter = null;
	}

	final String getPattern() {
		return pattern;
	}

	final ResourceMap getResourceMap() {
		return ((ResourceMapHandler) getParentHandler()).getResourceMap();
	}
}
