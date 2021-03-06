/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.rmap.maven.impl;

import java.util.Collection;

import org.eclipse.buckminster.rmap.maven.GroupAndArtifact;
import org.eclipse.buckminster.rmap.maven.MapEntry;
import org.eclipse.buckminster.rmap.maven.MavenPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Map Entry</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.buckminster.rmap.maven.impl.MapEntryImpl#getName <em>
 * Name</em>}</li>
 * <li>{@link org.eclipse.buckminster.rmap.maven.impl.MapEntryImpl#getAliases
 * <em>Aliases</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class MapEntryImpl extends GroupAndArtifactImpl implements MapEntry {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAliases() <em>Aliases</em>}'
	 * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #getAliases()
	 * @generated
	 * @ordered
	 */
	protected EList<GroupAndArtifact> aliases;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected MapEntryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MavenPackage.MAP_ENTRY__NAME:
				return getName();
			case MavenPackage.MAP_ENTRY__ALIASES:
				return getAliases();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case MavenPackage.MAP_ENTRY__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case MavenPackage.MAP_ENTRY__ALIASES:
				return aliases != null && !aliases.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MavenPackage.MAP_ENTRY__NAME:
				setName((String) newValue);
				return;
			case MavenPackage.MAP_ENTRY__ALIASES:
				getAliases().clear();
				getAliases().addAll((Collection<? extends GroupAndArtifact>) newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case MavenPackage.MAP_ENTRY__NAME:
				setName(NAME_EDEFAULT);
				return;
			case MavenPackage.MAP_ENTRY__ALIASES:
				getAliases().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public EList<GroupAndArtifact> getAliases() {
		if (aliases == null) {
			aliases = new EObjectEList<GroupAndArtifact>(GroupAndArtifact.class, this, MavenPackage.MAP_ENTRY__ALIASES);
		}
		return aliases;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MavenPackage.MAP_ENTRY__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(')');
		return result.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */

	@Override
	protected EClass eStaticClass() {
		return MavenPackage.Literals.MAP_ENTRY;
	}

} // MapEntryImpl
