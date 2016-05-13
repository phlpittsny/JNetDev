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
 * jND_Mutex <br>
 * Class to implement an object for synchronization. Actually, Object can
 * be used for this. This dummy class was included only for consistency with
 * cNetDev. A mutex implements one-at-a-time access to
 * critical sections of code.
 * <br>
 * To create a mutex, simply declare:
 * <pre>
 *         jND_Mutex mutex;
 * </pre>
 * Then, to enter and exit a critical section of code, use
 * <pre>
 *		synchronize(mutex) {
 *			// Critical section 
 *		}
 * </pre>
 * If multiple threads are sharing the same mutex, and employ
 * the above code, they will exhibit one-at-a-time access to
 * their critical sections.
 * <br><br>
 * @author	Pete Lutz
 */

public class jND_Mutex {
	public jND_Mutex() {
		
	}
};

