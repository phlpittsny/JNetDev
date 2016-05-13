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
package jNetDev;
/**
 * jND_Exception <br>
 * This class extends the standard Exception by adding a custom message. The
 * message generally starts with the name of the class that throws the exception.
 * <br><br>
 * @author Pete Lutz
 */

public class jND_Exception extends RuntimeException {
	public jND_Exception(String msg) {
		super(msg);
	}
};

