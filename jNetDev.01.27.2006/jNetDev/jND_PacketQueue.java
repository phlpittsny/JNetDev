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
import java.util.Vector;
/**
 * jND_PacketQueue <br>
 * Class to implement a queue of packets to hold packets captured by 
 * the thread of a jND_CaptureSession, until retrieved by the user.
 * This is actually a queue of byte arrays. 
 * <br><br>
 * @author	Pete Lutz
 */

public class jND_PacketQueue {
	private jND_Mutex mutex;
	private java.util.Vector queue;

	/**
	 * Construct an empty packet queue.
	 * @author	Pete Lutz
	 */
	public jND_PacketQueue() {
		queue = new Vector();
		mutex = new jND_Mutex();
	}

	/**
	 * Add a packet pointer to the BACK of the queue.
	 * @param		pkt - a pointer to the packet to be added
	 * @author	Pete Lutz
	 */
	public void push(byte[] pkt) {
		synchronized(mutex) {
			queue.add(pkt);
			mutex.notify();
		}
	}

	/**
	 * Get the packet at the FRONT of the queue, and delete it
	 * from the queue..
	 * @author	Pete Lutz
	 */
	public byte[] pop() throws jND_Exception {
		byte[] pkt;
		synchronized(mutex) {
			while(queue.size() == 0)
				try {
					mutex.wait();
				} catch(InterruptedException ie) { 
					throw new jND_Exception("jND_PacketQueue.pop Interrupted: " + ie.toString());
				}
			pkt = (byte[])queue.get(0);
			queue.remove(0);
		}
		return pkt;
	}

	/**
	 * Get the size of the queue..
	 * @author	Pete Lutz
	 */
	public int size() {
		return queue.size();
	}
};
