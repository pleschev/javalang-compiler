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
package org.walkmod.javalang.compiler.reflection;

import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.walkmod.javalang.compiler.Builder;
import org.walkmod.javalang.compiler.symbols.SymbolType;

/**
 * For a given generic letter (K, M, T,..) resolves if the contained class has a
 * value for that letter
 * 
 * @author rpau
 *
 */
public class GenericBuilderFromGenericClasses implements
		Builder<Map<String, SymbolType>> {

	private Class<?> clazz;

	private List<SymbolType> parameterizedTypes;

	public GenericBuilderFromGenericClasses(Class<?> clazz,
			List<SymbolType> parameterizedTypes) {
		this.clazz = clazz;
		this.parameterizedTypes = parameterizedTypes;
	}

	public GenericBuilderFromGenericClasses() {

	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public void setParameterizedTypes(List<SymbolType> parameterizedTypes) {
		this.parameterizedTypes = parameterizedTypes;
	}

	@Override
	public Map<String, SymbolType> build(Map<String, SymbolType> obj) {
		if (obj == null) {
			obj = new HashMap<String, SymbolType>();
		}
		TypeVariable<?>[] typeParams = clazz.getTypeParameters();

		if (typeParams != null) {

			for (int i = 0; i < typeParams.length; i++) {
				if (parameterizedTypes != null) {
					if (parameterizedTypes.get(i).getName() == null
							&& parameterizedTypes.get(i).hasBounds()) {
						obj.put(typeParams[i].getName(),
								parameterizedTypes.get(i));
					} else {
						if (!"java.lang.Object".equals(parameterizedTypes
								.get(i).getName())) {
							obj.put(typeParams[i].getName(),
									parameterizedTypes.get(i));
						} else {
							obj.put(typeParams[i].getName(), new SymbolType(
									"java.lang.Object"));
						}
					}

				} else {
					obj.put(typeParams[i].getName(), new SymbolType(
							"java.lang.Object"));
				}
			}
		}
		return obj;
	}
}
