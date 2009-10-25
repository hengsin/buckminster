/**
 * <copyright>
 * </copyright>
 *
 */
package org.eclipse.b3.beeLang;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>For Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.b3.beeLang.ForStatement#getInit <em>Init</em>}</li>
 *   <li>{@link org.eclipse.b3.beeLang.ForStatement#isRegular <em>Regular</em>}</li>
 *   <li>{@link org.eclipse.b3.beeLang.ForStatement#getCond <em>Cond</em>}</li>
 *   <li>{@link org.eclipse.b3.beeLang.ForStatement#getIterate <em>Iterate</em>}</li>
 *   <li>{@link org.eclipse.b3.beeLang.ForStatement#getBody <em>Body</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.b3.beeLang.BeeLangPackage#getForStatement()
 * @model
 * @generated
 */
public interface ForStatement extends Statement
{
  /**
   * Returns the value of the '<em><b>Init</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Init</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Init</em>' containment reference.
   * @see #setInit(VarExpressionList)
   * @see org.eclipse.b3.beeLang.BeeLangPackage#getForStatement_Init()
   * @model containment="true"
   * @generated
   */
  VarExpressionList getInit();

  /**
   * Sets the value of the '{@link org.eclipse.b3.beeLang.ForStatement#getInit <em>Init</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Init</em>' containment reference.
   * @see #getInit()
   * @generated
   */
  void setInit(VarExpressionList value);

  /**
   * Returns the value of the '<em><b>Regular</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Regular</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Regular</em>' attribute.
   * @see #setRegular(boolean)
   * @see org.eclipse.b3.beeLang.BeeLangPackage#getForStatement_Regular()
   * @model
   * @generated
   */
  boolean isRegular();

  /**
   * Sets the value of the '{@link org.eclipse.b3.beeLang.ForStatement#isRegular <em>Regular</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Regular</em>' attribute.
   * @see #isRegular()
   * @generated
   */
  void setRegular(boolean value);

  /**
   * Returns the value of the '<em><b>Cond</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Cond</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Cond</em>' containment reference.
   * @see #setCond(Expression)
   * @see org.eclipse.b3.beeLang.BeeLangPackage#getForStatement_Cond()
   * @model containment="true"
   * @generated
   */
  Expression getCond();

  /**
   * Sets the value of the '{@link org.eclipse.b3.beeLang.ForStatement#getCond <em>Cond</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Cond</em>' containment reference.
   * @see #getCond()
   * @generated
   */
  void setCond(Expression value);

  /**
   * Returns the value of the '<em><b>Iterate</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Iterate</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Iterate</em>' containment reference.
   * @see #setIterate(EObject)
   * @see org.eclipse.b3.beeLang.BeeLangPackage#getForStatement_Iterate()
   * @model containment="true"
   * @generated
   */
  EObject getIterate();

  /**
   * Sets the value of the '{@link org.eclipse.b3.beeLang.ForStatement#getIterate <em>Iterate</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Iterate</em>' containment reference.
   * @see #getIterate()
   * @generated
   */
  void setIterate(EObject value);

  /**
   * Returns the value of the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Body</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Body</em>' containment reference.
   * @see #setBody(Statement)
   * @see org.eclipse.b3.beeLang.BeeLangPackage#getForStatement_Body()
   * @model containment="true"
   * @generated
   */
  Statement getBody();

  /**
   * Sets the value of the '{@link org.eclipse.b3.beeLang.ForStatement#getBody <em>Body</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Body</em>' containment reference.
   * @see #getBody()
   * @generated
   */
  void setBody(Statement value);

} // ForStatement
