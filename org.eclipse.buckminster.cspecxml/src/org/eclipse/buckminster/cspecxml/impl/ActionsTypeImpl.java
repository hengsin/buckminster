/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.cspecxml.impl;

import java.util.Collection;

import org.eclipse.buckminster.cspecxml.IAction;
import org.eclipse.buckminster.cspecxml.IActionsType;
import org.eclipse.buckminster.cspecxml.ICSpecXMLPackage;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Actions Type</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link org.eclipse.buckminster.cspecxml.impl.ActionsTypeImpl#getGroup <em>Group</em>}</li>
 * <li>{@link org.eclipse.buckminster.cspecxml.impl.ActionsTypeImpl#getPublic <em>Public</em>}</li>
 * <li>{@link org.eclipse.buckminster.cspecxml.impl.ActionsTypeImpl#getPrivate <em>Private</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class ActionsTypeImpl extends EObjectImpl implements IActionsType
{
	/**
	 * The cached value of the '{@link #getGroup() <em>Group</em>}' attribute list. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @see #getGroup()
	 * @generated
	 * @ordered
	 */
	protected FeatureMap group;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ActionsTypeImpl()
	{
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType)
	{
		switch(featureID)
		{
		case ICSpecXMLPackage.ACTIONS_TYPE__GROUP:
			if(coreType)
				return getGroup();
			return ((FeatureMap.Internal)getGroup()).getWrapper();
		case ICSpecXMLPackage.ACTIONS_TYPE__PUBLIC:
			return getPublic();
		case ICSpecXMLPackage.ACTIONS_TYPE__PRIVATE:
			return getPrivate();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
	{
		switch(featureID)
		{
		case ICSpecXMLPackage.ACTIONS_TYPE__GROUP:
			return ((InternalEList<?>)getGroup()).basicRemove(otherEnd, msgs);
		case ICSpecXMLPackage.ACTIONS_TYPE__PUBLIC:
			return ((InternalEList<?>)getPublic()).basicRemove(otherEnd, msgs);
		case ICSpecXMLPackage.ACTIONS_TYPE__PRIVATE:
			return ((InternalEList<?>)getPrivate()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID)
	{
		switch(featureID)
		{
		case ICSpecXMLPackage.ACTIONS_TYPE__GROUP:
			return group != null && !group.isEmpty();
		case ICSpecXMLPackage.ACTIONS_TYPE__PUBLIC:
			return !getPublic().isEmpty();
		case ICSpecXMLPackage.ACTIONS_TYPE__PRIVATE:
			return !getPrivate().isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue)
	{
		switch(featureID)
		{
		case ICSpecXMLPackage.ACTIONS_TYPE__GROUP:
			((FeatureMap.Internal)getGroup()).set(newValue);
			return;
		case ICSpecXMLPackage.ACTIONS_TYPE__PUBLIC:
			getPublic().clear();
			getPublic().addAll((Collection<? extends IAction>)newValue);
			return;
		case ICSpecXMLPackage.ACTIONS_TYPE__PRIVATE:
			getPrivate().clear();
			getPrivate().addAll((Collection<? extends IAction>)newValue);
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
	public void eUnset(int featureID)
	{
		switch(featureID)
		{
		case ICSpecXMLPackage.ACTIONS_TYPE__GROUP:
			getGroup().clear();
			return;
		case ICSpecXMLPackage.ACTIONS_TYPE__PUBLIC:
			getPublic().clear();
			return;
		case ICSpecXMLPackage.ACTIONS_TYPE__PRIVATE:
			getPrivate().clear();
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public FeatureMap getGroup()
	{
		if(group == null)
		{
			group = new BasicFeatureMap(this, ICSpecXMLPackage.ACTIONS_TYPE__GROUP);
		}
		return group;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<IAction> getPrivate()
	{
		return getGroup().list(ICSpecXMLPackage.Literals.ACTIONS_TYPE__PRIVATE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EList<IAction> getPublic()
	{
		return getGroup().list(ICSpecXMLPackage.Literals.ACTIONS_TYPE__PUBLIC);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString()
	{
		if(eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (group: ");
		result.append(group);
		result.append(')');
		return result.toString();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass()
	{
		return ICSpecXMLPackage.Literals.ACTIONS_TYPE;
	}

} // ActionsTypeImpl