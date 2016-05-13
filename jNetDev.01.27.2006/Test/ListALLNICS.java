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
import jNetDev.*;

public class ListALLNICS {

	public static void main(String[] args) {	
		try {
			System.out.print("TEST PROGRAM: ListALLNICS\n");
			int n = jND_NICList.numberOfNICs();
			
			System.out.print("There are " + n + " NICs in this computer.\n");
			for(int i = 0; i < n; i++) {
				jND_NIC nic = new jND_NIC(i);
				System.out.print("NIC " + i + " Name= " + nic.name()
					+ "  Desc= " + nic.description() + "\n");
			}		
		} catch(jND_Exception nde) {
			System.err.print("jND_Exception: " + nde.toString() + "\n");
		}
	}
}
	
