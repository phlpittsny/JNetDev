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
 * jND_CaptureThread <br>
 * Class to implement the thread used to capture packets during an
 * active capture session. This implements Runnable.
 * <br><br>
 * It is NOT intended that users of the jNetDev package use this
 * class. Rather, this class is used by the jND_CaptureSession
 * class, internally.
 * <br><br>
 * @author Pete Lutz
 */
public class jND_CaptureThread implements Runnable {
	private jND_CaptureSession session;

	// ========== B E G I N    N A T I V E    M E T H O D S ==========

	/**
	 * Private method to capture 1 packet via an open capture session.
	 * 
	 * @param pcap_d -
	 *            the pcap descriptor for the session.
	 * @param pq -
	 *            the packet queue to add the packet to.
	 * @return the number of packets captured (0 = EOF for an
	 * 				offline capture.
	 * @author Pete Lutz
	 */
	private static native int capture1(byte[] pcap_d, jND_PacketQueue pq);

	// ==========   E N D    N A T I V E    M E T H O D S   ==========

	/**
	 * Default constructor
	 * @author Pete Lutz
	 */
	public jND_CaptureThread() {
		session = null;
	}

	/**
	 * Constructor to connect the thread to its capture session.
	 * Calling this constructor is the norm.
	 * 
	 * @param		cap - the capture session
	 * @author Pete Lutz
	 */
	public jND_CaptureThread(jND_CaptureSession cap) {
		captureSession(cap);
	}
    
    /**
     * Mutator to change the capture session. This allows one thread to
     * process multiple capture sessions over time.
     * @param	cap - the new capture session for this thread.
     * @author Pete Lutz
     */
	public void captureSession(jND_CaptureSession cap) {
		session = cap;
	}
     
     /** 
      * Accessor to retrieve the capture session associated with
      * this capture thread.
      * 
      * @return	The jND_CaptureSession associated with this thread.
      * @author Pete Lutz
      */
	public jND_CaptureSession captureSession() {
		return session;
	}

	/**
	 * Overridden method from Runnable. When the thread is started
	 * (by calling the start method), this
	 * method is called. run() is effectively the main program of the thread.
	 * 
	 * This method dispatches the pcap system to capture 1 packet at a time
	 * with a callback to the private method 'callback' above. callback 
	 * places each captured packet in a packet queue.
	 * 
	 * NOTE: run should always be called indirectly by calling start().
	 * It rarely, if ever, makes sense for the programmer to call run()
	 * directly.
	 * 
	 * @author Pete Lutz
	 */
	public void run() throws jND_Exception {
		try {			
			while(session.captureStatus()) {
				// capture1 will stuff the packet in the packetQueue and
				// return a result flag
				int flag =
					capture1(session.pcap_d(), session.packetQueue());
				
				// Often, flag == 1 indicating 1 packet captured. It has already
				// been stuffed in the packet queue, in that case, by capture1. 
				// A flag of 0 means that pcap timeout and no packet was caught.
				// In either case, we just loop around, check the capture flag and continue
				if(flag == 0 || flag == 1) continue;
				
				// flag == -1 means error indicator from pcap, stop capturing
				if(flag == -1) {
					session.captureStatus(false);
					break;
				}
				
				// A -2 flag means EOF for an offline capture
				if(flag == -2 && session.inUseStatus() == jND_CaptureSession.jND_OFFLINE)
					session.captureStatus(false);
				
				// flag == -3 means bad pcap_d
				if(flag == -3) {
					session.captureStatus(false);
					throw new jND_Exception("jND_CaptureThread.run: Invalid pcap descriptor");
				}
			}
			if(session.getDelayedStop())
				session.stop();
			if(session.getDelayedDispose())
				session.dispose();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception ("jND_CaptureThread.run : " + e.toString());
		}
	}
};
