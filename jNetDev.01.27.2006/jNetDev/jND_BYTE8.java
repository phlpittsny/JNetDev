/****************************************************************************
**
** Copyright (C) 2005 Peter H. Lutz. All rights reserved.
**
** This file is part of the jNetDev network development system.
**
** This file may be used under the terms of the GNU General Public
** License version 2.0 as published by the Free Software Foundation
** and appearing in the file LICENSE included in the packaging of
** this file.  Please review the LICENSE file to ensure that GNU
** General Public Licensing requirements are met
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/
/*
 * Created on Apr 18, 2005
 *
 * Wrapper class for an 8-byte integer ... similar to Long.
 */
package jNetDev;

/**
 * jND_BYTE8<br>
 * The lack of unsigned types in Java is a problem. As a stop gap solution
 * to this, the jND_BYTE* classes are provided. They provide wrappers for
 * 1-, 2-, 4-, and 8-byte values. Each provides a constructor that takes
 * an 8-byte value (or any upcast smaller value) and truncates it to the
 * approporiate length.
 * <br><br>
 * In addition, there are accessors to return the value as a byte, a short,
 * an int, or a long. Returning a jND_BYTE2, for example, shorter types
 * (byte) are truncated, returning just the low order information. Longer
 * types (int and long) are extended with zeros ... NOT sign extended which
 * is the norm.
 * @author phl
 */
public class jND_BYTE8 {
	private long v_long;
	
	public jND_BYTE8() {
		v_long = 0;
	}
	
	public jND_BYTE8(long b) {
		v_long = b;
	}
	
	public byte toByte() {
		return (byte) v_long;
	}
	
	public short toShort() {
		return (short) v_long;
	}
	
	public int toInt() {
		return ((int) v_long);
	}
	
	public long toLong() {
		return v_long;
	}
	
	public String toString() {
		return "" + toLong();
	}
}