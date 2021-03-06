/*******************************************************************************
 * Copyright (c) 2006-2013, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/
package org.eclipse.buckminster.core.version;

import java.util.Date;
import java.util.List;

import org.eclipse.buckminster.core.Messages;
import org.eclipse.buckminster.core.ctype.IComponentType;
import org.eclipse.buckminster.core.resolver.NodeQuery;
import org.eclipse.buckminster.core.resolver.ResolverDecisionType;
import org.eclipse.buckminster.core.rmap.model.Provider;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.osgi.util.NLS;

/**
 * @author Thomas Hallgren
 */
public abstract class AbstractSCCSVersionFinder extends AbstractVersionFinder {
	protected static class RevisionEntry {
		private final String entryName;

		private final String revision;

		private final Date timestamp;

		public RevisionEntry(String entryName, Date timestamp, long revision) {
			this.entryName = entryName;
			this.timestamp = timestamp;
			this.revision = revision == -1 ? null : Long.toString(revision);
		}

		public RevisionEntry(String entryName, Date timestamp, String revision) {
			this.entryName = entryName;
			this.timestamp = timestamp;
			this.revision = revision;
		}

		public String getEntryName() {
			return entryName;
		}

		public String getRevision() {
			return revision;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public boolean satisfiesRevision(String rev) {
			return VersionMatch.satisfiesRevision(rev, revision);
		}
	}

	protected AbstractSCCSVersionFinder(Provider provider, IComponentType componentType, NodeQuery query) {
		super(provider, componentType, query);
	}

	@Override
	public VersionMatch getBestVersion(IProgressMonitor monitor) throws CoreException {
		try {
			NodeQuery query = getQuery();

			VersionSelector[] branchTagPath = query.getBranchTagPath();
			int idx = branchTagPath.length;

			boolean branches = false;
			boolean tags = false;
			boolean trunk = (idx == 0); // Use trunk if no branches or tags has
										// been specified

			while (--idx >= 0) {
				VersionSelector branchOrTag = branchTagPath[idx];
				if (branchOrTag.getType() == VersionSelector.BRANCH)
					branches = true;
				else
					tags = true;
				if (branchOrTag.isDefault())
					trunk = true;
			}

			IVersionConverter versionConverter = getProvider().getVersionConverter();
			if (versionConverter != null) {
				// We are using a versionConverter. This rules out anything
				// found
				// on the trunk.
				//
				logDecision(ResolverDecisionType.USING_VERSION_CONVERTER, versionConverter.getId());

				trunk = false;

				// Exactly one of the tags or branches must be valid
				//
				if (versionConverter.getSelectorType() == VersionSelector.BRANCH) {
					if (tags == false)
						branches = true;
				} else {
					if (branches == false)
						tags = true;
				}

				// And perhaps this ruled out this finder all together?
				//
				if (!(branches || tags)) {
					MonitorUtils.complete(monitor);
					return null;
				}
			}

			int ticks = 0;
			if (trunk) {
				logDecision(ResolverDecisionType.SEARCHING_TRUNK);
				ticks += 10;
			}
			if (branches) {
				logDecision(ResolverDecisionType.SEARCHING_BRANCHES);
				ticks += 10;
			}
			if (tags) {
				logDecision(ResolverDecisionType.SEARCHING_TAGS);
				ticks += 10;
			}

			monitor.beginTask(null, ticks);
			VersionMatch best = null;
			if (branches)
				best = getBestBranchOrTagMatch(true, MonitorUtils.subMonitor(monitor, 10));

			if (tags) {
				VersionMatch match = getBestBranchOrTagMatch(false, MonitorUtils.subMonitor(monitor, 10));
				if (best == null)
					best = match;
				else if (match != null && query.compare(match, best) > 0) {
					logDecision(ResolverDecisionType.MATCH_REJECTED, best, NLS.bind(Messages._0_is_a_better_match, match));
					best = match;
				}
			}

			if (trunk) {
				VersionMatch match = getBestTrunkMatch(MonitorUtils.subMonitor(monitor, 10));
				if (best == null)
					best = match;
				else if (match != null && query.compare(match, best) > 0) {
					logDecision(ResolverDecisionType.MATCH_REJECTED, best, NLS.bind(Messages._0_is_a_better_match, match));
					best = match;
				}
			}
			return best;
		} finally {
			monitor.done();
		}
	}

	protected abstract boolean checkComponentExistence(VersionMatch versionMatch, IProgressMonitor monitor) throws CoreException;

	protected VersionMatch getBestBranchOrTagMatch(boolean branches, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100);
		try {
			List<RevisionEntry> entries = getBranchesOrTags(branches, MonitorUtils.subMonitor(monitor, 50));
			return getBestBranchOrTagMatch(branches, entries, MonitorUtils.subMonitor(monitor, 50));
		} finally {
			monitor.done();
		}
	}

	protected VersionMatch getBestBranchOrTagMatch(boolean branches, List<RevisionEntry> entries, IProgressMonitor monitor) throws CoreException {
		int top = entries.size();
		if (top == 0) {
			logDecision(branches ? ResolverDecisionType.NO_BRANCHES_FOUND : ResolverDecisionType.NO_TAGS_FOUND);
			MonitorUtils.complete(monitor);
			return null;
		}

		IVersionConverter vConverter = getProvider().getVersionConverter();
		if (vConverter != null && vConverter.getSelectorType() != (branches ? VersionSelector.BRANCH : VersionSelector.TAG)) {
			// Version converter not for the desired type so we will not find
			// anything
			//
			if (branches)
				logDecision(ResolverDecisionType.VERSION_SELECTOR_MISMATCH, "tags", "branches"); //$NON-NLS-1$ //$NON-NLS-2$
			else
				logDecision(ResolverDecisionType.VERSION_SELECTOR_MISMATCH, "branches", "tags"); //$NON-NLS-1$ //$NON-NLS-2$
			MonitorUtils.complete(monitor);
			return null;
		}

		monitor.beginTask(null, top * 10);
		NodeQuery query = getQuery();
		VersionSelector[] branchTagPath = query.getBranchTagPath();
		VersionRange versionRange = query.getVersionRange();
		String revision = query.getRevision();
		Date timestamp = query.getTimestamp();
		VersionMatch best = null;
		for (RevisionEntry entry : entries) {
			// Rule out anything that is above a given revision
			//
			if (!entry.satisfiesRevision(revision)) {
				logDecision(ResolverDecisionType.REVISION_REJECTED, Long.valueOf(entry.getRevision()), "too high"); //$NON-NLS-1$
				continue;
			}

			// Rule out anything that is later then a given time
			//
			if (timestamp != null) {
				Date entryTs = entry.getTimestamp();
				if (entryTs != null && entryTs.compareTo(timestamp) > 0) {
					logDecision(ResolverDecisionType.TIMESTAMP_REJECTED, entryTs, "too young"); //$NON-NLS-1$
					continue;
				}
			}

			String name = entry.getEntryName();
			VersionSelector branchOrTag = branches ? VersionSelector.branch(name) : VersionSelector.tag(name);
			if (branchTagPath.length > 0 && VersionSelector.indexOf(branchTagPath, branchOrTag) < 0) {
				// This one will be discriminated anyway so there's no need to
				// include it
				//
				logDecision(branches ? ResolverDecisionType.BRANCH_REJECTED : ResolverDecisionType.TAG_REJECTED, branchOrTag, NLS.bind(
						Messages.r_not_in_path_0, VersionSelector.toString(branchTagPath)));
				continue;
			}

			Version version;
			VersionMatch match = null;
			if (vConverter != null) {
				version = vConverter.createVersion(branchOrTag);
				if (version == null) {
					// Converter could not make sense of this tag. Skip it
					//
					logDecision(branches ? ResolverDecisionType.BRANCH_REJECTED : ResolverDecisionType.TAG_REJECTED, branchOrTag,
							Messages.VersionSelector_cannot_make_sense_of_it);
					MonitorUtils.worked(monitor, 10);
					continue;
				}

				// We need to assert that the component really exists on this
				// branch
				// or with this tag.
				//
				match = new VersionMatch(version, branchOrTag, entry.getRevision(), entry.getTimestamp(), null);
				if (!checkComponentExistence(match, MonitorUtils.subMonitor(monitor, 10))) {
					logDecision(branches ? ResolverDecisionType.BRANCH_REJECTED : ResolverDecisionType.TAG_REJECTED, branchOrTag,
							Messages.No_component_was_found);
					continue;
				}
				logDecision(branches ? ResolverDecisionType.USING_BRANCH_CONVERTED_VERSION : ResolverDecisionType.USING_TAG_CONVERTED_VERSION,
						version, branchOrTag);
			} else {
				try {
					version = getVersionFromArtifacts(branchOrTag, MonitorUtils.subMonitor(monitor, 10));
				} catch (CoreException e) {
					// Something is not right with this entry. Skip it.
					//
					logDecision(branches ? ResolverDecisionType.BRANCH_REJECTED : ResolverDecisionType.TAG_REJECTED, branchOrTag, e.getMessage());
					continue;
				}
			}

			if (!(versionRange == null || versionRange.isIncluded(version))) {
				// Discriminated by our designator
				//
				logDecision(ResolverDecisionType.VERSION_REJECTED, version, NLS.bind(Messages.Not_designated_by_0, versionRange));
				continue;
			}

			if (match == null)
				match = new VersionMatch(version, branchOrTag, entry.getRevision(), entry.getTimestamp(), null);

			if (version == null) {
				// Unknown component type will not check the existence of any
				// artifacts. We need to
				// do that here
				//						
				if (!checkComponentExistence(match, MonitorUtils.subMonitor(monitor, 10))) {
					logDecision(branches ? ResolverDecisionType.BRANCH_REJECTED : ResolverDecisionType.TAG_REJECTED, branchOrTag,
							Messages.No_component_was_found);
					continue;
				}
			}

			if (best == null || query.compare(match, best) > 0) {
				if (best != null)
					logDecision(ResolverDecisionType.MATCH_REJECTED, best, NLS.bind(Messages._0_is_a_better_match, match));

				best = match;
				if (vConverter != null && versionRange != null && versionRange.getMinimum().equals(versionRange.getMaximum())) {
					// Explicit hit on a version converted tag or branch. This
					// will do just fine.
					//
					break;
				}
			}
		}
		monitor.done();
		return best;
	}

	protected VersionMatch getBestTrunkMatch(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100);
		try {
			RevisionEntry entry = getTrunk(MonitorUtils.subMonitor(monitor, 50));
			if (entry == null)
				return null;

			NodeQuery query = getQuery();

			// Rule out anything that is above a given revision
			//
			String revision = query.getRevision();
			if (!entry.satisfiesRevision(revision)) {
				logDecision(ResolverDecisionType.REVISION_REJECTED, Long.valueOf(entry.getRevision()), Messages.Too_high);
				return null;
			}

			// Rule out anything that is later then a given time
			//
			Date timestamp = query.getTimestamp();
			if (timestamp != null) {
				Date entryTs = entry.getTimestamp();
				if (entryTs != null && entryTs.compareTo(timestamp) > 0) {
					logDecision(ResolverDecisionType.TIMESTAMP_REJECTED, entryTs, Messages.Too_young);
					return null;
				}
			}

			Version version = null;
			try {
				version = getVersionFromArtifacts(null, MonitorUtils.subMonitor(monitor, 50));
			} catch (CoreException e) {
				// Something is not right with this entry. Skip it.
				//
				logDecision(ResolverDecisionType.MAIN_REJECTED, e.getMessage());
				return null;
			}

			VersionRange versionDesignator = query.getVersionRange();
			if (!(versionDesignator == null || versionDesignator.isIncluded(version))) {
				// Discriminated by our designator
				//
				logDecision(ResolverDecisionType.VERSION_REJECTED, version, NLS.bind(Messages.Not_designated_by_0, versionDesignator));
				return null;
			}
			return new VersionMatch(version, null, entry.getRevision(), entry.getTimestamp(), null);
		} finally {
			monitor.done();
		}
	}

	protected abstract List<RevisionEntry> getBranchesOrTags(boolean branches, IProgressMonitor monitor) throws CoreException;

	protected abstract RevisionEntry getTrunk(IProgressMonitor monitor) throws CoreException;
}
