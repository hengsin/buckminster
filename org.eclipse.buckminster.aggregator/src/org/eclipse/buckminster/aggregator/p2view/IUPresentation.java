/**
 * Copyright (c) 2006-2009, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *
 * $Id$
 */
package org.eclipse.buckminster.aggregator.p2view;

import java.util.Comparator;

import org.eclipse.buckminster.aggregator.p2.InstallableUnit;
import org.eclipse.buckminster.aggregator.p2.InstallableUnitType;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.equinox.internal.provisional.p2.core.Version;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>IU Presentation</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getId <em>Id</em>}</li>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getVersion <em>Version</em>}</li>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getName <em>Name</em>}</li>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getLabel <em>Label</em>}</li>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getDescription <em>Description</em>}</li>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getType <em>Type</em>}</li>
 * <li>{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getIu <em>Iu</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation()
 * @model abstract="true"
 * @generated
 */
public interface IUPresentation extends EObject
{
	// Compares IU presentation by name (ascending) and version (descending)
	static class IUPresentationComparator implements Comparator<IUPresentation>
	{

		public int compare(IUPresentation iu1, IUPresentation iu2)
		{
			if(iu1 != null)
				if(iu2 == null)
					return 1;
				else
				{
					String name1 = iu1.getName();
					String name2 = iu2.getName();

					if(name1 != null)
						if(name2 == null)
							return 1;
						else
						{
							int result = name1.compareTo(name2);

							if(result == 0)
							{
								// Order by version in descending order
								Version version1 = iu1.getVersion();
								Version version2 = iu2.getVersion();

								if(version1 != null)
									if(version2 == null)
										return -1;
									else
										return -version1.compareTo(version2);
								else if(version2 != null)
									return 1;
								else
									return 0;
							}

							return result;
						}
					else if(name2 != null)
						return -1;
					else
						return 0;
				}
			else if(iu2 != null)
				return -1;
			else
				return 0;
		}
	}

	static Comparator<IUPresentation> COMPARATOR = new IUPresentationComparator();

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Description</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Returns the value of the '<em><b>Iu</b></em>' reference. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Iu</em>' reference isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Iu</em>' reference.
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Iu()
	 * @model resolveProxies="false" changeable="false"
	 * @generated
	 */
	InstallableUnit getIu();

	/**
	 * Returns the value of the '<em><b>Label</b></em>' attribute. <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Label</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Label</em>' attribute.
	 * @see #setLabel(String)
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Label()
	 * @model
	 * @generated
	 */
	String getLabel();

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> ======= Returns the value of
	 * the '<em><b>Name</b></em>' attribute. <!-- begin-user-doc --> >>>>>>> .r10620
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * <<<<<<< .mine Returns the value of the '<em><b>Type</b></em>' attribute. The default value is <code>""</code>.
	 * The literals are from the enumeration {@link org.eclipse.buckminster.aggregator.p2.InstallableUnitType}. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear, there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.buckminster.aggregator.p2.InstallableUnitType
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Type()
	 * @model default="" required="true" changeable="false" volatile="true"
	 * @generated
	 */
	InstallableUnitType getType();

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute. <!-- begin-user-doc --> ======= Returns the value
	 * of the '<em><b>Version</b></em>' attribute. <!-- begin-user-doc --> >>>>>>> .r10620
	 * <p>
	 * If the meaning of the '<em>Version</em>' attribute isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(Version)
	 * @see org.eclipse.buckminster.aggregator.p2view.P2viewPackage#getIUPresentation_Version()
	 * @model dataType="org.eclipse.buckminster.aggregator.p2.Version"
	 * @generated
	 */
	Version getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getDescription
	 * <em>Description</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Sets the value of the '{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getId <em>Id</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Sets the value of the '{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getLabel <em>Label</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Label</em>' attribute.
	 * @see #getLabel()
	 * @generated
	 */
	void setLabel(String value);

	/**
	 * Sets the value of the '{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getName <em>Name</em>}'
	 * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Sets the value of the '{@link org.eclipse.buckminster.aggregator.p2view.IUPresentation#getVersion
	 * <em>Version</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(Version value);

} // IUPresentation