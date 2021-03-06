package com.variamos.refas.core.sematicsmetamodel;

import com.variamos.refas.core.types.VariableType;
import com.variamos.refas.core.types.VariationScopeType;
import com.variamos.syntaxsupport.metamodel.InstEnumeration;
import com.variamos.syntaxsupport.metamodelsupport.SemanticAttribute;

/**
 * A class to represent the Semantics of Variables. Part of PhD work at
 * University of Paris 1
 * 
 * @author Juan C. Mu�oz Fern�ndez <jcmunoz@gmail.com>
 * 
 * @version 1.1
 * @since 2014-12-02
 * @see com.cfm.productline.
 */
public class SemanticVariable extends AbstractSemanticVertex {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5538738414024566452L;

	public static final String VAR_SCOPE = "scope",
			VAR_SCOPENAME = "Scope",
			VAR_SCOPECLASS = VariationScopeType.class.getCanonicalName(),
			
			VAR_CONTEXTTYPE = "contextType",
			VAR_CONTEXTTYPENAME = "Context Type",
			VAR_CONTEXTTYPECLASS = "com.variamos.refas.core.types.ContextType",
			
			VAR_NAME = "name",
			VAR_NAMENAME = "Name",
			
			VAR_VARIABLETYPE = "variableType",
			VAR_VARIABLETYPENAME = "Variable Type",
			VAR_VARIABLETYPECLASS = VariableType.class.getCanonicalName(),
			
			VAR_ENUMERATIONTYPE = "enumerationType",
			VAR_ENUMERATIONTYPENAME = "Enumeration",
			VAR_ENUMERATIONTYPECLASS = InstEnumeration.class.getCanonicalName();

	public SemanticVariable() {
		this(null, null);
	}

	public SemanticVariable(String name) {
		this(null,name);
	}

	public SemanticVariable(AbstractSemanticVertex parentConcept, String name) {
		super(parentConcept, name, true);
		defineSemanticAttributes();
	}

	private void defineSemanticAttributes() {

		putSemanticAttribute(VAR_SCOPE, new SemanticAttribute(VAR_SCOPE,
				"Enumeration", false, VAR_SCOPENAME, VAR_SCOPECLASS,  "user", ""));
		putSemanticAttribute(VAR_CONTEXTTYPE, new SemanticAttribute(
				VAR_CONTEXTTYPE, "Enumeration", false, VAR_CONTEXTTYPENAME, VAR_CONTEXTTYPECLASS,
				"profiled", ""));
		putSemanticAttribute(VAR_NAME, new SemanticAttribute(VAR_NAME,
				"String", false, VAR_NAMENAME,  ""));
		putSemanticAttribute(VAR_VARIABLETYPE, new SemanticAttribute(
				VAR_VARIABLETYPE, "Enumeration", true, VAR_VARIABLETYPENAME,  VAR_VARIABLETYPECLASS,
				 "String", ""));
		putSemanticAttribute(VAR_ENUMERATIONTYPE, new SemanticAttribute(
				VAR_ENUMERATIONTYPE, "Class", false, VAR_ENUMERATIONTYPENAME, VAR_ENUMERATIONTYPECLASS,"ME",
				"String", ""));

		this.addPropEditableAttribute("01#" + VAR_NAME);
		this.addPropEditableAttribute("02#" + VAR_VARIABLETYPE);
		this.addPropEditableAttribute("03#" + VAR_ENUMERATIONTYPE);
		this.addPropEditableAttribute("04#" + VAR_CONTEXTTYPE);
		this.addPropEditableAttribute("05#" + VAR_SCOPE);

		this.addPropVisibleAttribute("01#" + VAR_NAME);
		this.addPropVisibleAttribute("02#" + VAR_VARIABLETYPE);
		this.addPropVisibleAttribute("03#" + VAR_ENUMERATIONTYPE+"#"+VAR_VARIABLETYPE+"#==#"+"Enumeration");
		this.addPropVisibleAttribute("04#" + VAR_CONTEXTTYPE);
		this.addPropVisibleAttribute("05#" + VAR_SCOPE);
		
		this.addPanelVisibleAttribute("01#" + VAR_NAME);
		this.addPanelVisibleAttribute("02#" + VAR_VARIABLETYPE+"#"+VAR_VARIABLETYPE+"#!=#"+"Enumeration");
		this.addPanelVisibleAttribute("03#" + VAR_ENUMERATIONTYPE+"#"+VAR_VARIABLETYPE+"#==#"+"Enumeration");
		this.addPanelVisibleAttribute("04#" + VAR_CONTEXTTYPE);
		this.addPanelVisibleAttribute("05#" + VAR_SCOPE);

		this.addPanelSpacersAttribute("#" + VAR_NAME + "#\n");
		this.addPanelSpacersAttribute("{#" + VAR_VARIABLETYPE + "#} ");
		this.addPanelSpacersAttribute("{#" + VAR_CONTEXTTYPE + "#} ");
		this.addPanelSpacersAttribute("{#" + VAR_SCOPE + "#}");

	}

	public String toString() {

		return " VAR: " + super.toString();
	}
}
