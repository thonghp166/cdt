/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.event;



/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIRegisterCreatedEvent extends MICreatedEvent {

	String regName;
	int regno;

	public MIRegisterCreatedEvent(String name, int number) {
		this(0, name, number);
	}

	public MIRegisterCreatedEvent(int token, String name, int number) {
		super(token);
		regName = name;
		regno = number;
	}

	public String getName() {
		return regName;
	}

	public int getNumber() {
		return regno;
	}

}
