/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 29, 2003
 */
package org.eclipse.cdt.core.search;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BasicSearchResultCollector implements ICSearchResultCollector {

	public void aboutToStart() {
		results = new LinkedList();
	}

	public void done() {
	}
	
	public IProgressMonitor getProgressMonitor() {
		return null;
	}

	public IMatch createMatch(Object fileResource, int start, int end, ISourceElementCallbackDelegate node, IASTScope parent) throws CoreException {
		BasicSearchMatch result = new BasicSearchMatch();
		
		if( fileResource instanceof IResource )
			result.resource = (IResource) fileResource;
		else if( fileResource instanceof IPath )
			result.path = (IPath) fileResource;
			
		result.startOffset = start;
		result.endOffset = end;
		
		result.parentName = "";
		if( parent instanceof IASTQualifiedNameElement ){
			String [] names = ((IASTQualifiedNameElement)parent).getFullyQualifiedName();
			for( int i = 0; i < names.length; i++ ){
				if( i > 0 )
					result.parentName += "::";
					
				result.parentName += names[ i ];
			}
		}
		
		IASTOffsetableNamedElement offsetable = null;
		
		if( node instanceof IASTReference ){
			offsetable = (IASTOffsetableNamedElement) ((IASTReference)node).getReferencedElement();
			result.name = ((IASTReference)node).getName();
		} else if( node instanceof IASTOffsetableNamedElement ){
			offsetable = (IASTOffsetableNamedElement)node;
			result.name = offsetable.getName();
		}
		
		setElementInfo( result, offsetable );
		
		return result;
	}


	public void acceptMatch(IMatch match) throws CoreException {
		results.add( match );
	}
	
	public List getSearchResults(){
		return results;
	}
	
	private void setElementInfo( BasicSearchMatch match, IASTOffsetableElement node ){
		//ImageDescriptor imageDescriptor = null;
		if( node instanceof IASTClassSpecifier ){
			ASTClassKind kind = ((IASTClassSpecifier)node).getClassKind();
			if( kind == ASTClassKind.CLASS ){
				match.type = ICElement.C_CLASS;
			} else if ( kind == ASTClassKind.STRUCT ){
				match.type = ICElement.C_STRUCT;
			} else if ( kind == ASTClassKind.UNION ){
				match.type = ICElement.C_UNION;
			}
		} else if ( node instanceof IASTNamespaceDefinition ){
			match.type = ICElement.C_NAMESPACE;
		} else if ( node instanceof IASTEnumerationSpecifier ){
			match.type = ICElement.C_ENUMERATION;
		} else if ( node instanceof IASTField ){
			match.type = ICElement.C_FIELD;
			IASTField  field = (IASTField)node;
			ASTAccessVisibility visibility = field.getVisiblity();
			if( visibility == ASTAccessVisibility.PUBLIC ){
				match.visibility = ICElement.CPP_PUBLIC;
			} else if ( visibility == ASTAccessVisibility.PRIVATE ) {
				match.visibility = ICElement.CPP_PRIVATE;
			} // else protected, there is no ICElement.CPP_PROTECTED
			match.isConst = field.getAbstractDeclaration().isConst();
			match.isStatic = field.isStatic();
		} else if ( node instanceof IASTVariable ){
			match.type = ICElement.C_VARIABLE;
			IASTVariable variable = (IASTVariable)node;
			match.isConst  = variable.getAbstractDeclaration().isConst();
		} else if ( node instanceof IASTEnumerator ){
			match.type = ICElement.C_ENUMERATOR;
		} else if ( node instanceof IASTMethod ){
			match.type = ICElement.C_METHOD;
			IASTMethod method = (IASTMethod) node;
			ASTAccessVisibility visibility = method.getVisiblity();
			if( visibility == ASTAccessVisibility.PUBLIC ){
				match.visibility = ICElement.CPP_PUBLIC;
			} else if ( visibility == ASTAccessVisibility.PRIVATE ) {
				match.visibility = ICElement.CPP_PRIVATE;
			} // else protected, there is no ICElement.CPP_PROTECTED
			match.isConst = method.isConst();
			match.isVolatile = method.isVolatile();
			match.isStatic = method.isStatic();
		} else if ( node instanceof IASTFunction ){
			match.type = ICElement.C_FUNCTION;
			IASTFunction function = (IASTFunction)node;
			match.isStatic = function.isStatic();
		}
	}
	
	private List results;

}
