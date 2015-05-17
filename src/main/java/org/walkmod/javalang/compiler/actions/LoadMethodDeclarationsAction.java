/*
 Copyright (C) 2015 Raquel Pau and Albert Coroleu.
 
Walkmod is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Walkmod is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.javalang.compiler.actions;

import java.util.List;

import org.walkmod.javalang.ast.Node;
import org.walkmod.javalang.ast.TypeParameter;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ConstructorDeclaration;
import org.walkmod.javalang.ast.body.EnumConstantDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.javalang.ast.expr.ObjectCreationExpr;
import org.walkmod.javalang.ast.type.ClassOrInterfaceType;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.compiler.providers.SymbolActionProvider;
import org.walkmod.javalang.compiler.symbols.ASTSymbolTypeResolver;
import org.walkmod.javalang.compiler.symbols.MethodSymbol;
import org.walkmod.javalang.compiler.symbols.Symbol;
import org.walkmod.javalang.compiler.symbols.SymbolAction;
import org.walkmod.javalang.compiler.symbols.SymbolTable;
import org.walkmod.javalang.compiler.symbols.SymbolType;
import org.walkmod.javalang.compiler.types.TypesLoaderVisitor;
import org.walkmod.javalang.compiler.types.TypeVisitorAdapter;

public class LoadMethodDeclarationsAction extends SymbolAction {

	private TypesLoaderVisitor<?> typeTable;
	private SymbolActionProvider actionProvider;
	private TypeVisitorAdapter<?> expressionTypeAnalyzer;

	public LoadMethodDeclarationsAction(TypesLoaderVisitor<?> typeTable,
			SymbolActionProvider actionProvider,
			TypeVisitorAdapter<?> expressionTypeAnalyzer) {
		this.typeTable = typeTable;
		this.actionProvider = actionProvider;
		this.expressionTypeAnalyzer = expressionTypeAnalyzer;
	}

	private void pushMethod(Symbol<?> symbol, SymbolTable table,
			MethodDeclaration md) throws Exception {

		Type type = md.getType();
		SymbolType resolvedType = ASTSymbolTypeResolver.getInstance().valueOf(
				type);
		if (resolvedType == null) {
			resolvedType = new SymbolType(Object.class);
		} else {
			resolvedType.setClazz(TypesLoaderVisitor.getClassLoader()
					.loadClass(resolvedType));
		}
		type.setSymbolData(resolvedType);

		List<Parameter> params = md.getParameters();
		List<TypeParameter> tps = md.getTypeParameters();
		SymbolType[] args = null;
		boolean hasDynamicArgs = false;
		if (params != null) {
			args = new SymbolType[params.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = ASTSymbolTypeResolver.getInstance().valueOf(
						params.get(i).getType(), tps);
				params.get(i).getType().setSymbolData(args[i]);
				if (i == args.length - 1) {
					hasDynamicArgs = params.get(i).isVarArgs();
				}
			}
		}
		List<SymbolAction> actions = null;
		if (actionProvider != null) {
			actions = actionProvider.getActions(md);
		}
		md.accept(expressionTypeAnalyzer, null);
		MethodSymbol method = new MethodSymbol(md.getName(), resolvedType, md,
				symbol.getType(), args, false, hasDynamicArgs, md
						.getSymbolData().getMethod(), actions);
		table.pushSymbol(method);
	}

	private void pushConstructor(Symbol<?> symbol, SymbolTable table,
			ConstructorDeclaration md) throws Exception {
		Type type = new ClassOrInterfaceType(md.getName());
		SymbolType resolvedType = ASTSymbolTypeResolver.getInstance().valueOf(
				type);
		type.setSymbolData(resolvedType);

		List<Parameter> params = md.getParameters();
		List<TypeParameter> tps = md.getTypeParameters();
		
		
		SymbolType[] args = null;
		boolean hasDynamicArgs = false;
		if (params != null) {
			args = new SymbolType[params.size()];
			for (int i = 0; i < args.length; i++) {
				
				args[i] = ASTSymbolTypeResolver.getInstance().valueOf(
						params.get(i).getType(), tps);
				
				params.get(i).getType().setSymbolData(args[i]);
				if (i == args.length - 1) {
					hasDynamicArgs = params.get(i).isVarArgs();
				}
			}
		}
		List<SymbolAction> actions = null;
		if (actionProvider != null) {
			actions = actionProvider.getActions(md);
		}
		md.accept(expressionTypeAnalyzer, null);

		MethodSymbol method = new MethodSymbol(md.getName(), resolvedType, md,
				symbol.getType(), args, false, hasDynamicArgs, md
						.getSymbolData().getConstructor(), actions);
		table.pushSymbol(method);
	}

	@Override
	public void doPush(Symbol<?> symbol, SymbolTable table) throws Exception {
		if (symbol.getName().equals("this")) {
			Node node = symbol.getLocation();
			List<BodyDeclaration> members = null;

			if (node instanceof TypeDeclaration) {
				TypeDeclaration n = (TypeDeclaration) node;
				members = n.getMembers();
			} else if (node instanceof ObjectCreationExpr) {
				members = ((ObjectCreationExpr) node).getAnonymousClassBody();
			}
			else if (node instanceof EnumConstantDeclaration) {
				members = ((EnumConstantDeclaration) node).getClassBody();
			}
			if (members != null) {

				for (BodyDeclaration member : members) {
					if (member instanceof MethodDeclaration) {
						MethodDeclaration md = (MethodDeclaration) member;
						pushMethod(symbol, table, md);

					} else if (member instanceof ConstructorDeclaration) {
						ConstructorDeclaration cd = (ConstructorDeclaration) member;
						pushConstructor(symbol, table, cd);
					}
				}
			}
		}

	}
}
