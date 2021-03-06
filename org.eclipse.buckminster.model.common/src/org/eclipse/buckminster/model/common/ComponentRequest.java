/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.buckminster.model.common;

import java.util.Map;
import org.eclipse.buckminster.osgi.filter.Filter;
import org.eclipse.equinox.p2.metadata.VersionRange;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Component Request</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.buckminster.model.common.ComponentRequest#getRange
 * <em>Range</em>}</li>
 * <li>{@link org.eclipse.buckminster.model.common.ComponentRequest#getFilter
 * <em>Filter</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.buckminster.model.common.CommonPackage#getComponentRequest()
 * @model
 * @generated
 */
public interface ComponentRequest extends ComponentName {
	static final Filter P2_OPTIONAL_FILTER = (Filter) CommonFactory.eINSTANCE.createFromString(CommonPackage.Literals.FILTER,
			CommonConstants.FILTER_ECLIPSE_P2_OPTIONAL);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model 
	 *        resultDataType="org.eclipse.buckminster.model.common.StringBuilder"
	 * @generated
	 */
	void appendViewName(StringBuilder result);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	boolean designates(ComponentIdentifier cid);

	/**
	 * Returns the value of the '<em><b>Filter</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Filter</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Filter</em>' attribute.
	 * @see #setFilter(Filter)
	 * @see org.eclipse.buckminster.model.common.CommonPackage#getComponentRequest_Filter()
	 * @model dataType="org.eclipse.buckminster.model.common.Filter"
	 * @generated
	 */
	Filter getFilter();

	/**
	 * Returns the value of the '<em><b>Range</b></em>' attribute. <!--
	 * begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Range</em>' attribute isn't clear, there
	 * really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Range</em>' attribute.
	 * @see #setRange(VersionRange)
	 * @see org.eclipse.buckminster.model.common.CommonPackage#getComponentRequest_Range()
	 * @model dataType="org.eclipse.buckminster.model.common.VersionRange"
	 * @generated
	 */
	VersionRange getRange();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	String getViewName();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	boolean isEnabled(Map<String, ? extends Object> properties);

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model kind="operation"
	 * @generated
	 */
	boolean isOptional();

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @model
	 * @generated
	 */
	ComponentRequest merge(ComponentRequest request);

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.buckminster.model.common.ComponentRequest#getFilter
	 * <em>Filter</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @param value
	 *            the new value of the '<em>Filter</em>' attribute.
	 * @see #getFilter()
	 * @generated
	 */
	void setFilter(Filter value);

	/**
	 * Sets the value of the '
	 * {@link org.eclipse.buckminster.model.common.ComponentRequest#getRange
	 * <em>Range</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Range</em>' attribute.
	 * @see #getRange()
	 * @generated
	 */
	void setRange(VersionRange value);

} // ComponentRequest
