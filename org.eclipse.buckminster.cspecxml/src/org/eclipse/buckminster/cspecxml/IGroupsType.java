/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.cspecxml;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Groups Type</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.buckminster.cspecxml.IGroupsType#getGroup <em>Group
 * </em>}</li>
 * <li>{@link org.eclipse.buckminster.cspecxml.IGroupsType#getPublic <em>Public
 * </em>}</li>
 * <li>{@link org.eclipse.buckminster.cspecxml.IGroupsType#getPrivate <em>
 * Private</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.buckminster.cspecxml.ICSpecXMLPackage#getGroupsType()
 * @model extendedMetaData="name='groups_._type' kind='elementOnly'"
 * @generated
 */
public interface IGroupsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Group</b></em>' attribute list. The list
	 * contents are of type {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Group</em>' attribute list isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Group</em>' attribute list.
	 * @see org.eclipse.buckminster.cspecxml.ICSpecXMLPackage#getGroupsType_Group()
	 * @model unique="false" dataType="org.eclipse.emf.ecore.EFeatureMapEntry"
	 *        many="true" extendedMetaData="kind='group' name='group:0'"
	 * @generated
	 */
	FeatureMap getGroup();

	/**
	 * Returns the value of the '<em><b>Private</b></em>' containment reference
	 * list. The list contents are of type
	 * {@link org.eclipse.buckminster.cspecxml.IGroup}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Private</em>' containment reference list isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Private</em>' containment reference list.
	 * @see org.eclipse.buckminster.cspecxml.ICSpecXMLPackage#getGroupsType_Private()
	 * @model containment="true" transient="true" volatile="true" derived="true"
	 *        extendedMetaData=
	 *        "kind='element' name='private' namespace='##targetNamespace' group='#group:0'"
	 * @generated
	 */
	EList<IGroup> getPrivate();

	/**
	 * Returns the value of the '<em><b>Public</b></em>' containment reference
	 * list. The list contents are of type
	 * {@link org.eclipse.buckminster.cspecxml.IGroup}. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Public</em>' containment reference list isn't
	 * clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Public</em>' containment reference list.
	 * @see org.eclipse.buckminster.cspecxml.ICSpecXMLPackage#getGroupsType_Public()
	 * @model containment="true" transient="true" volatile="true" derived="true"
	 *        extendedMetaData=
	 *        "kind='element' name='public' namespace='##targetNamespace' group='#group:0'"
	 * @generated
	 */
	EList<IGroup> getPublic();

} // IGroupsType
