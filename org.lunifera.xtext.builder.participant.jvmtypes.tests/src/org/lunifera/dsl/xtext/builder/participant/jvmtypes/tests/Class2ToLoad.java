
/**
 * Copyright (c) 2011 - 2015, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *         Florian Pirchner - Initial implementation
 */


package org.lunifera.dsl.xtext.builder.participant.jvmtypes.tests;

public class Class2ToLoad {

	public Class2ToLoad() {

	}

	public Class2ToLoad(String value, Class2ToLoad other) {

	}

	public static class InnerClass {

		public InnerClass() {

		}

		public InnerClass(String value, Class2ToLoad.InnerClass other) {

		}

	}

}
