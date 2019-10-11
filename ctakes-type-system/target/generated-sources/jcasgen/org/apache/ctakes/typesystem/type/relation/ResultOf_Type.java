
/* First created by JCasGen Tue Jul 16 10:40:58 CEST 2019 */
package org.apache.ctakes.typesystem.type.relation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** A UMLS relation between clinical elements.
 * Updated by JCasGen Tue Jul 16 10:40:58 CEST 2019
 * @generated */
public class ResultOf_Type extends ElementRelation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ResultOf.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.apache.ctakes.typesystem.type.relation.ResultOf");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ResultOf_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    