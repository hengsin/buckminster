/*******************************************************************************
 * Copyright (c) 2004, 2006
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/
package org.eclipse.buckminster.maven.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.buckminster.core.common.model.ExpandingProperties;
import org.eclipse.buckminster.core.cspec.WellKnownExports;
import org.eclipse.buckminster.core.cspec.builder.CSpecBuilder;
import org.eclipse.buckminster.core.cspec.builder.ComponentRequestBuilder;
import org.eclipse.buckminster.core.cspec.builder.GroupBuilder;
import org.eclipse.buckminster.core.cspec.model.ComponentName;
import org.eclipse.buckminster.core.cspec.model.PrerequisiteAlreadyDefinedException;
import org.eclipse.buckminster.core.ctype.AbstractComponentType;
import org.eclipse.buckminster.core.ctype.IResolutionBuilder;
import org.eclipse.buckminster.core.helpers.TextUtils;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.core.reader.IComponentReader;
import org.eclipse.buckminster.core.resolver.NodeQuery;
import org.eclipse.buckminster.core.rmap.model.Provider;
import org.eclipse.buckminster.core.version.MissingVersionTypeException;
import org.eclipse.buckminster.core.version.VersionHelper;
import org.eclipse.buckminster.core.version.VersionMatch;
import org.eclipse.buckminster.core.version.VersionType;
import org.eclipse.buckminster.maven.MavenPlugin;
import org.eclipse.buckminster.maven.Messages;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.buckminster.runtime.Trivial;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.metadata.IVersionFormat;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is preliminary. A lot of things remain that concerns scope,
 * plugins, exclusion in transitive dependencies etc.
 * 
 * @author Thomas Hallgren
 */
public class MavenComponentType extends AbstractComponentType {
	public static final String ID = "maven"; //$NON-NLS-1$

	private static final MavenCSpecBuilder builder = new MavenCSpecBuilder();

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US); //$NON-NLS-1$

	private static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd'.'HHmmss", Locale.US); //$NON-NLS-1$

	private static final Pattern timestampPattern = Pattern.compile(//
			"^((?:19|20)\\d{2}(?:0[1-9]|1[012])(?:0[1-9]|[12][0-9]|3[01]))" + // //$NON-NLS-1$
					"(?:\\.((?:[01][0-9]|2[0-3])[0-5][0-9][0-5][0-9]))?$"); //$NON-NLS-1$

	public static Version createVersion(String versionStr) throws CoreException {
		versionStr = TextUtils.notEmptyTrimmedString(versionStr);
		if (versionStr == null)
			return null;

		Matcher m = timestampPattern.matcher(versionStr);
		if (m.matches())
			return VersionHelper.getVersionType(VersionType.TIMESTAMP).getFormat().parse(versionStr);

		try {
			return getTripletFormat().parse(versionStr);
		} catch (IllegalArgumentException e) {
			return VersionHelper.getVersionType(VersionType.STRING).getFormat().parse(versionStr);
		}
	}

	static String addDependencies(IComponentReader reader, Document pomDoc, CSpecBuilder cspec, GroupBuilder archives,
			ExpandingProperties<String> properties) throws CoreException {
		Element project = pomDoc.getDocumentElement();
		Node parentNode = null;
		Node propertiesNode = null;
		Node dependenciesNode = null;
		String groupId = null;
		String artifactId = null;
		String versionStr = null;
		String packaging = "jar"; //$NON-NLS-1$

		for (Node child = project.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String nodeName = child.getNodeName();
			if ("parent".equals(nodeName)) //$NON-NLS-1$
				parentNode = child;
			else if ("properties".equals(nodeName)) //$NON-NLS-1$
				propertiesNode = child;
			else if ("dependencies".equals(nodeName)) //$NON-NLS-1$
				dependenciesNode = child;
			else if ("groupId".equals(nodeName)) //$NON-NLS-1$
				groupId = child.getTextContent().trim();
			else if ("artifactId".equals(nodeName)) //$NON-NLS-1$
				artifactId = child.getTextContent().trim();
			else if ("version".equals(nodeName)) //$NON-NLS-1$
				versionStr = child.getTextContent().trim();
			else if ("packaging".equals(nodeName)) //$NON-NLS-1$
				packaging = child.getTextContent().trim();
		}

		if (reader instanceof MavenReader && parentNode != null)
			processParentNode((MavenReader) reader, cspec, archives, properties, parentNode);

		if (groupId != null) {
			groupId = ExpandingProperties.expand(properties, groupId, 0);
			properties.put("project.groupId", groupId, true); //$NON-NLS-1$
			properties.put("pom.groupId", groupId, true); //$NON-NLS-1$
			properties.put("groupId", groupId, true); //$NON-NLS-1$
		}
		if (artifactId != null) {
			artifactId = ExpandingProperties.expand(properties, artifactId, 0);
			properties.put("project.artifactId", artifactId, true); //$NON-NLS-1$
			properties.put("pom.artifactId", artifactId, true); //$NON-NLS-1$
			properties.put("artifactId", artifactId, true); //$NON-NLS-1$
		}
		if (versionStr != null) {
			versionStr = ExpandingProperties.expand(properties, versionStr, 0);
			properties.put("project.version", versionStr, true); //$NON-NLS-1$
			properties.put("pom.version", versionStr, true); //$NON-NLS-1$
			properties.put("version", versionStr, true); //$NON-NLS-1$
		}

		if (propertiesNode != null)
			processProperties(properties, propertiesNode);

		if (dependenciesNode != null) {
			NodeQuery nq = reader.getNodeQuery();
			Provider provider = reader.getProviderMatch().getProvider();
			ComponentQuery query = nq.getComponentQuery();
			Map<String, ? extends Object> ctx = nq.getContext();
			boolean transitive = (provider instanceof MavenProvider) ? ((MavenProvider) provider).isTransitive() : MavenProvider
					.getDefaultTransitive();
			for (Node dep = dependenciesNode.getFirstChild(); dep != null; dep = dep.getNextSibling()) {
				if (dep.getNodeType() == Node.ELEMENT_NODE && "dependency".equals(dep.getNodeName()) && transitive) //$NON-NLS-1$
					addDependency(query, ctx, provider, cspec, archives, properties, dep);
			}
		}
		return packaging;
	}

	static Date createTimestamp(String date, String time) throws CoreException {
		try {
			return (time != null) ? timestampFormat.parse(date + '.' + time) : dateFormat.parse(date);
		} catch (ParseException e) {
			throw BuckminsterException.wrap(e);
		}
	}

	static VersionMatch createVersionMatch(String versionStr, String typeInfo) throws CoreException {
		Version version = createVersion(versionStr);
		if (version == null)
			//
			// No version at all. Treat as if it was an unversioned SNAPSHOT
			//
			return VersionMatch.DEFAULT;
		return new VersionMatch(version, null, -1, null, typeInfo);
	}

	static VersionRange createVersionRange(String versionStr) throws CoreException {
		if (versionStr == null || versionStr.length() == 0)
			return null;

		char leadIn = versionStr.charAt(0);
		if (leadIn == '[' || leadIn == '(') {
			if (leadIn == '[' && versionStr.endsWith(",)")) //$NON-NLS-1$
			{
				versionStr = versionStr.substring(1, versionStr.length() - 2);
				Version version = createVersion(versionStr);
				return (version == null) ? null : VersionHelper.greaterOrEqualRange(version);
			}
			return VersionHelper.createRange(getTripletFormat(), versionStr);
		}

		Version version = createVersion(versionStr);
		if (version == null)
			return null;

		return VersionHelper.exactRange(version);
	}

	static IVersionFormat getTripletFormat() {
		try {
			return VersionHelper.getVersionType(VersionType.TRIPLET).getFormat();
		} catch (MissingVersionTypeException e) {
			throw new RuntimeException(e);
		}
	}

	static boolean isSnapshotVersion(Version version) {
		return version != null && version.toString().endsWith("SNAPSHOT"); //$NON-NLS-1$
	}

	static Version stripFromSnapshot(Version version) {
		if (version == null)
			return null;

		String vstr = version.toString();
		if (vstr.endsWith("SNAPSHOT")) //$NON-NLS-1$
		{
			int stripLen = 8;
			if (vstr.charAt(vstr.length() - (stripLen + 1)) == '-')
				stripLen++;
			vstr = vstr.substring(0, vstr.length() - stripLen);
		}
		try {
			return version.getFormat().parse(vstr);
		} catch (IllegalArgumentException e) {
			return version;
		}
	}

	private static void addDependency(ComponentQuery query, Map<String, ? extends Object> context, Provider provider, CSpecBuilder cspec,
			GroupBuilder archives, ExpandingProperties<String> properties, Node dep) throws CoreException {
		String id = null;
		String groupId = null;
		String artifactId = null;
		String versionStr = null;
		String type = null;
		String scope = null;
		boolean optional = false;
		for (Node depChild = dep.getFirstChild(); depChild != null; depChild = depChild.getNextSibling()) {
			if (depChild.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String localName = depChild.getNodeName();
			String nodeValue = depChild.getTextContent().trim();
			if ("groupId".equals(localName)) //$NON-NLS-1$
				groupId = nodeValue;
			else if ("artifactId".equals(localName)) //$NON-NLS-1$
				artifactId = nodeValue;
			else if ("version".equals(localName)) //$NON-NLS-1$
				versionStr = nodeValue;
			else if ("id".equals(localName)) //$NON-NLS-1$
				id = nodeValue;
			else if ("type".equals(localName)) //$NON-NLS-1$
				type = nodeValue;
			else if ("optional".equals(localName)) //$NON-NLS-1$
				optional = Boolean.parseBoolean(nodeValue);
			else if ("scope".equals(localName)) //$NON-NLS-1$
				scope = nodeValue;
		}

		if (optional)
			//
			// Docs etc. We skip this here since we don't generate an
			// actions that can make use of it
			//
			return;

		boolean isScopeExcluded = (provider instanceof MavenProvider) ? ((MavenProvider) provider).isScopeExcluded(scope) : MavenProvider
				.getDefaultIsScopeExcluded();
		if (isScopeExcluded)
			//
			// Determine if the scope of this POM is one we should ignore
			//
			return;

		if (artifactId == null)
			artifactId = id;

		if (artifactId == null)
			return;

		if ("plugin".equals(type)) //$NON-NLS-1$
			//
			// Maven plugin (required for Maven builds). We don't want it.
			//
			return;

		if (groupId == null)
			groupId = artifactId;

		artifactId = ExpandingProperties.expand(properties, artifactId, 0);
		groupId = ExpandingProperties.expand(properties, groupId, 0);
		if (versionStr != null)
			versionStr = ExpandingProperties.expand(properties, versionStr, 0);

		String componentName = (provider instanceof MavenProvider) ? ((MavenProvider) provider).getComponentName(groupId, artifactId) : MavenProvider
				.getDefaultName(groupId, artifactId);

		if (componentName.contains("${")) //$NON-NLS-1$
		{
			// Unresolved property. We can't use this so skip it.
			//
			MavenPlugin.getLogger().warning(NLS.bind(Messages.unable_to_resolve_component_name_0_skipping_dependency, componentName));
			return;
		}

		ComponentName adviceKey = new ComponentName(componentName, ID);
		if (query.skipComponent(adviceKey, context))
			return;

		ComponentRequestBuilder depBld = cspec.createDependencyBuilder();
		depBld.setName(componentName);
		depBld.setComponentTypeID(ID);

		VersionRange vd = query.getVersionOverride(adviceKey, context);
		if (vd == null)
			vd = createVersionRange(versionStr);
		depBld.setVersionRange(vd);

		try {
			cspec.addDependency(depBld);
			archives.addExternalPrerequisite(depBld, WellKnownExports.JAVA_BINARIES);
		} catch (PrerequisiteAlreadyDefinedException e) {
			ComponentRequestBuilder oldDep = cspec.getRequiredDependency(depBld);
			if (!Trivial.equalsAllowNull(vd, oldDep.getVersionRange()))
				MavenPlugin.getLogger().warning(e.getMessage());
		}
	}

	private static void processParentNode(MavenReader reader, CSpecBuilder cspec, GroupBuilder archives, ExpandingProperties<String> properties,
			Node parent) throws CoreException {
		String groupId = null;
		String artifactId = null;
		String versionStr = null;
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			String localName = child.getNodeName();
			String nodeValue = child.getTextContent().trim();
			if ("groupId".equals(localName)) //$NON-NLS-1$
				groupId = nodeValue;
			else if ("artifactId".equals(localName)) //$NON-NLS-1$
				artifactId = nodeValue;
			else if ("version".equals(localName)) //$NON-NLS-1$
				versionStr = nodeValue;
		}

		if (groupId != null) {
			groupId = ExpandingProperties.expand(properties, groupId, 0);
			properties.put("project.groupId", groupId, true); //$NON-NLS-1$
			properties.put("pom.groupId", groupId, true); //$NON-NLS-1$
			properties.put("groupId", groupId, true); //$NON-NLS-1$
		}
		if (artifactId != null) {
			artifactId = ExpandingProperties.expand(properties, artifactId, 0);
			properties.put("project.artifactId", artifactId, true); //$NON-NLS-1$
			properties.put("pom.artifactId", artifactId, true); //$NON-NLS-1$
			properties.put("artifactId", artifactId, true); //$NON-NLS-1$
		}

		Provider provider = reader.getProviderMatch().getProvider();
		String componentName = (provider instanceof MavenProvider) ? ((MavenProvider) provider).getComponentName(groupId, artifactId) : MavenProvider
				.getDefaultName(groupId, artifactId);

		MapEntry entry = new MapEntry(componentName, groupId, artifactId, null);
		MavenReaderType mrt = (MavenReaderType) reader.getReaderType();
		VersionMatch vm = mrt.createVersionMatch(reader, entry, versionStr);
		IPath parentPath = mrt.getPomPath(entry, vm);

		MavenPlugin.getLogger().debug("Getting POM information for parent: %s - %s at path %s", groupId, artifactId, parentPath); //$NON-NLS-1$
		Document parentDoc = reader.getPOMDocument(entry, vm, parentPath, new NullProgressMonitor());
		if (parentDoc == null)
			return;

		addDependencies(reader, parentDoc, cspec, archives, properties);
	}

	private static void processProperties(ExpandingProperties<String> properties, Node node) {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeName = child.getNodeName();
			String nodeValue = child.getTextContent().trim();
			if (nodeValue.length() > 0)
				properties.put(nodeName, ExpandingProperties.expand(properties, nodeValue, 0), true);
			else
				properties.remove(nodeName);
		}
	}

	@Override
	public IResolutionBuilder getResolutionBuilder(IComponentReader reader, IProgressMonitor monitor) throws CoreException {
		MonitorUtils.complete(monitor);
		return builder;
	}

	@Override
	public VersionRange getTypeSpecificDesignator(VersionRange range) {
		if (range == null)
			return null;

		Version low = range.getMinimum();
		Version high = range.getMaximum();
		boolean lowIsSnapshot = isSnapshotVersion(low);
		boolean highIsSnapshot = (high != null && isSnapshotVersion(high));
		if (!(lowIsSnapshot || highIsSnapshot))
			return range;

		low = stripFromSnapshot(low);
		if (high != null)
			high = stripFromSnapshot(high);

		if (!(low.isOSGiCompatible() && low.getSegmentCount() >= 3))
			//
			// We cannot apply advanced semantics here so we just
			// strip the SNAPSHOT part and hope for the best.
			//
			return new VersionRange(low, range.getIncludeMinimum(), high, range.getIncludeMaximum());

		StringBuilder bld = new StringBuilder();
		bld.append(getTripletFormat());
		bld.append('/');
		if (lowIsSnapshot) {
			if (high == null || range.getIncludeMinimum()) {
				// [1.2.4-SNAPSHOT -> (1.2.3
				// >=1.2.4-SNAPSHOT -> (1.2.3
				//
				// Rationale:
				// In the triplet world the release 1.2.4 is higher then any
				// 1.2.4.SNAPSHOT
				// so we need something that is lower then 1.2.4 but higher then
				// 1.2.3. This
				// means "starting from 1.2.3 not including 1.2.3", i.e. (1.2.3.
				// We then want
				// to include everything up to the release of 1.2.4
				//
				// The >= calls for a very high limit. We get to that later.
				//
				bld.append('(');
				int major = ((Integer) low.getSegment(0)).intValue();
				int minor = ((Integer) low.getSegment(1)).intValue();
				int micro = ((Integer) low.getSegment(2)).intValue();
				if (minor > 0 || micro > 0) {
					bld.append(major);
					bld.append('.');
					if (micro > 0) {
						bld.append(minor);
						bld.append('.');
						bld.append(micro - 1);
					} else
						bld.append(minor - 1);
				} else
					bld.append(major - 1);
			} else {
				// (1.2.4.SNAPSHOT -> [1.2.4
				//
				// Rationale:
				// 1.2.4.SNAPSHOT is lower then the 1.2.4 release. We let
				// (1.2.4.SNAPSHOT
				// mean "starting from 1.2.4.SNAPSHOT but not including the
				// SNAPSHOT which
				// in essence, is the same as starting from, and including, the
				// 1.2.4 release
				//
				bld.append('[');
				bld.append(low);
			}
		} else {
			// The best we can do here is to always include the low version. We
			// don't
			// have any semantics to apply
			//
			bld.append('[');
			bld.append(low);
		}

		bld.append(',');
		if (range.getMinimum().equals(range.getMaximum())) {
			bld.append(low);
			bld.append(']');
		} else {
			if (high == null) {
				// Greater or equal. We need a ridiculously high version...
				//
				bld.append(Integer.MAX_VALUE);
				bld.append(']');
			} else {
				if (range.getIncludeMaximum()) {
					// 1.2.3.SNAPSHOT] -> 1.2.3]
					//
					// The upper bound is included and a SNAPSHOT
					// can resolve to a release.
					//
					bld.append(high);
					bld.append(']');
				} else {
					if (highIsSnapshot && high.isOSGiCompatible() && high.getSegmentCount() >= 3) {
						// ,1.2.3.SNAPSHOT) -> 1.2.2]
						//
						// We cannot use 1.2.3) here since that would
						// allow 1.2.3.xxx to be included since it's
						// lower. Instead, we include all up to the
						// 1.2.2 release
						//
						int major = ((Integer) high.getSegment(0)).intValue();
						int minor = ((Integer) high.getSegment(1)).intValue();
						int micro = ((Integer) high.getSegment(2)).intValue();
						if (minor > 0 || micro > 0) {
							bld.append(major);
							bld.append('.');
							if (micro > 0) {
								bld.append(minor);
								bld.append('.');
								bld.append(micro - 1);
							} else
								bld.append(minor - 1);
						} else
							bld.append(major - 1);
						bld.append(']');
					} else {
						bld.append(high);
						bld.append(')');
					}
				}
			}
		}
		try {
			return new VersionRange(bld.toString());
		} catch (IllegalArgumentException e) {
			return range;
		}
	}
}
