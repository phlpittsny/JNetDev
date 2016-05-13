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

import jNetDev.Headers.*;

/**
 * jND_CaptureSession <br>
 * A class to enable the capturing of packets. This class allows one to create a
 * capture session, which may be started and stopped. When started, the capture
 * process proceeds in a separate thread (see jND_CaptureThread).
 * <br><br>
 * The constructors set up the session and packets are captured when the start
 * method is called. Once started, capturing proceeds until the stop method is
 * called.
 * <br><br>
 * Another thread may obtain packets by querying and accessing the packet queue
 * (jND_PacketQueue), which is obtained via the getPacketQueue method.
 * <br><br>
 * The calling code must specify a NIC (network interface card) to use for the
 * capture. A list of available NICs may be obtained by via the jND_NIC class.
 * <br><br>
 * There is much to this class, including installing capture filters and
 * capturing to a file. Read through the methods for details.
 * <br><br>
 * SPECIAL NOTE: When a capture session is created, the underlying pcap library
 * is called to open a capture descriptor. The memory inherent in this is NEVER
 * released unless you call the dispose() method. It is the programmer's
 * responsibility to call dispose() when s/he is done with a Capture Session.
 * <br><br>
 * @author Pete Lutz
 */
public class jND_CaptureSession {
	// Constructor values
	private int mode;
	private String nicname;
	private int snaplen;
	private boolean promisc;
	private int to_ms;
	
	// sync access to static variable
	private static jND_Mutex special_mutex = new jND_Mutex(); 
	private static int nextCaptureID; // seq no for capture sessions
	private jND_Mutex mutex; // sync access to state variables
	private int currCaptureID; // seq no for THIS capture session
	private boolean capturing; // TRUE if capturing packets, FALSE if not
	private String currFilter; // last filter installed
	private byte[] p_filter; // compiled filter if any
	private jND_PacketQueue currPacketQueue; // packet queue for this session
	private byte[] currPcap_d; // libpcap descriptor
	private byte[] dumper; // descriptor for offline session
	private int inuse; // indicator: 0 = not in use,
	private boolean delayed_dispose = false; // true <=> dispose was called while thread was running
	private boolean delayed_stop = false; // true <=> stop was called while thread was running

	//    LIVE = live capture, OFFLINE = offline session
	private jND_CaptureThread capThread;
	private Thread javaThread = null;

	// Error message buffer
	private String captureSessionError = null;

	private int size; // The size in a native method reply.

	// Type of capture
	// *** NOTE: These three constants MUST coincide
	// *** with those in jNetDev.c
	public static final int jND_NONE = 0;
	public static final int jND_LIVE = 1;
	public static final int jND_OFFLINE = 2;

	// null ptr for jni pointers
	private static final byte[] NULLPTR = { 0, 0, 0, 0, 0, 0, 0, 0 };

	// ========== B E G I N   N A T I V E   M E T H O D S ==========

	/**
	 * Private method to open a capture session. Handles both LIVE and OFFLINE
	 * captures. The reply from this is a byte array in the following form:
	 * 
	 * <pre>
	 * 
	 *   +-------+-------+--   --+-------+-------+--   --+
	 *   | size  | pcapd in 'size' octets| message to end|
	 *   +-------+-------+--   --+-------+-------+--   --+
	 *  
	 * </pre>
	 * 
	 * The size is the # of octets in the pcapd object. The remainder of the
	 * byte array is an ASCII error message, which has a meaning if the size is
	 * 0 (indicating an error);
	 * 
	 * @param mode -
	 *            either jND_LIVE or jND_OFFLINE
	 * @param devname -
	 *            the device name of the NIC to use
	 * @param snaplen -
	 *            the size of the snapshot to capture ... often 1500
	 * @param promisc -
	 *            TRUE if we are capturing in promiscuous mode, otherwise FALSE
	 * @param to_ms -
	 *            the timeout in msec to be used while capturing
	 * @return a byte array as described above.
	 * @author Pete Lutz
	 */
	private static native byte[] openCapture(int mode, // 1 = LIVE, 2 = OFFLINE
			String devname, // NIC device name
			int snaplen, // snapshot length
			boolean promisc, // promiscuous mode
			int to_ms); // timeout in msec

	/**
	 * Private method to close a capture session. It also closes an affiliated
	 * dump session and releases memory allocated for a filter, if any.
	 * 
	 * @param pcapd -
	 *            the pcap descriptor for the session.
	 * @param pfilter -
	 *            pointer to the filter installed on this session (null if
	 *            none).
	 * @param dumper -
	 *            pointer to the dump session running. (null if none).
	 * @author Pete Lutz
	 */
	private static native void closeCapture(byte[] pcapd, byte[] pfilter, byte[] dumper);

	/**
	 * Private method to open a dump session to a file.
	 * 
	 * @param pcapd -
	 *            packet capture descriptor from pcap.
	 * @param fname -
	 *            the name of the dump file to create.
	 * @return a dumper (byte array) that describes the dump session
	 * @author Pete Lutz
	 */
	private static native byte[] dumpOpen(byte[] pcapd, String fname);

	/**
	 * Private method to write one packet to a dump file.
	 * 
	 * @param dumper -
	 *            dump handle, returned by dumpOpen.
	 * @param pkt -
	 *            the packet to write to the dump file.
	 * @author Pete Lutz
	 */
	private static native void dump1(byte[] dumper, byte[] pkt);

	/**
	 * Private method to close a packet dump session.
	 * 
	 * @param dumper -
	 *            dump handle, returned by dumpOpen
	 * @author Pete Lutz
	 */
	private static native void dumpClose(byte[] dumper);

	/**
	 * Private method to install a filter on a capture session. The reply from
	 * this is a byte array in the following form:
	 * 
	 * <pre>
	 * 
	 *  	+-------+-------+--   --+-------+-------+--   --+
	 *   | size  |pfilter in 'size' bytes| message to end|
	 *   +-------+-------+--   --+-------+-------+--   --+
	 *  
	 * </pre>
	 * 
	 * The size is the # of octets in the pfilter object. The remainder of the
	 * byte array is an ASCII error message, which has a meaning if the size is
	 * 0 (indicating an error).
	 * 
	 * pfilter is a pointer to dynamic memory allocated for the compiled filter
	 * object. It must be passed in to successive calls so that the memory may
	 * be released. If pfilter == 0 there is no filter installed.
	 * 
	 * @param filterStr -
	 *            The filter as a string in tcpdump format.
	 * @param optimize -
	 *            whether or not to optimize the filter.
	 * @param netmask -
	 *            the netmask to use (required for some filters)
	 * @author Pete Lutz
	 */
	private static native byte[] installFilter(byte[] pcapd, byte[] pfilter,
			String filterStr, boolean optimize, int netmask);

	// ==========   E N D   N A T I V E   M E T H O D S   ==========

	/**
	 * Private method to parse the reply from a native method.
	 * 
	 * @author Pete Lutz
	 */
	private byte[] parseReply(byte[] reply) {
		size = 0;

		// Check for empty reply
		if (reply == null || reply.length < 1) {
			captureSessionError = "Cannot parse reply from native method.";
			return NULLPTR;
		}

		// Get the size of the ptr (if any)
		size = ((int) reply[0]) & 0xff;

		// If size == 0, get the message
		if (size == 0) {
			captureSessionError = new String(reply, 1, reply.length - 1);
			return NULLPTR;
		}

		// size != 0, get the ptr as a byte array
		byte[] ptr = new byte[size];
		for (int i = 0; i < size; i++)
			ptr[i] = reply[1 + i];
		return ptr;
	}

	/**
	 * Default constructor. This is disallowed and will result in throwing and
	 * exception. We need a NIC and the promiscuous flag as a minimum to start a
	 * capture session.
	 * 
	 * @author Pete Lutz
	 */
	public jND_CaptureSession() {
		int num = jND_NICList.numberOfNICs();	// Force fill of NIC info
		throw new jND_Exception(
				"jND_CaptureSession.constructor - default constructor now allowed");
	}

	/**
	 * Creates a new capture session. No filter is attached to this session at
	 * the outset. Use setFilter to apply a filter. Packet capturing does not
	 * begin until the start method is called.
	 * 
	 * @param nic
	 *            a jND_NIC object describing the nic on which the capture will
	 *            be made.
	 * @param snaplen
	 *            the snapshot length. This amounts the length of the buffer for
	 *            capturing.
	 * @param promisc
	 *            <code>true</code> if the capture is to be done in
	 *            promiscuous mode, <code>false</code> otherwise.
	 * @param to_ms
	 *            timeout to use when capturing. This is expressed in
	 *            milliseconds.
	 * @author Pete Lutz
	 */
	public jND_CaptureSession(jND_NIC nic, int snaplen, boolean promisc,
			int to_ms) {
		try {
			int num = jND_NICList.numberOfNICs();	// Force fill of NIC info
			byte[] reply = null;
			synchronized (special_mutex) {
				currCaptureID = nextCaptureID++;
			}
			p_filter = NULLPTR;
			capturing = false;
			currFilter = "";
			currPacketQueue = new jND_PacketQueue();
			currPcap_d = NULLPTR;
			dumper = NULLPTR;
			mutex = new jND_Mutex();

			delayed_dispose = false;
			delayed_stop = false;

			this.mode = jND_LIVE;
			this.nicname = nic.name();
			this.snaplen = snaplen;
			this.promisc = promisc;
			this.to_ms = to_ms;
			synchronized (mutex) {
				inuse = jND_LIVE;
				reply = openCapture(this.mode, this.nicname, this.snaplen, this.promisc, this.to_ms);
			}
			currPcap_d = parseReply(reply);
			if (currPcap_d == NULLPTR) {
				inuse = jND_NONE;
				throw new jND_Exception("jND_CaptuerSession.constructor : "
						+ captureSessionError);
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.constructor : "
					+ e.toString());
		}
	}

	/**
	 * Creates a new capture session. This is equivalent to
	 * jND_CaptureSession(nic, 1500, promisc, 1000)
	 * 
	 * @param nic
	 *            a jND_NIC object describing the nic on which the capture will
	 *            be made.
	 * @param promisc
	 *            <code>true</code> if the capture is to be done in
	 *            promiscuous mode, <code>false</code> otherwise.
	 * @author Pete Lutz
	 */
	public jND_CaptureSession(jND_NIC nic, boolean promisc) {
		try {
			int num = jND_NICList.numberOfNICs();	// Force fill of NIC info
			byte[] reply = null;
			synchronized (special_mutex) {
				currCaptureID = nextCaptureID++;
			}
			p_filter = NULLPTR;
			capturing = false;
			currFilter = "";
			currPacketQueue = new jND_PacketQueue();
			currPcap_d = NULLPTR;
			dumper = NULLPTR;
			mutex = new jND_Mutex();

			delayed_dispose = false;
			delayed_stop = false;

			this.mode = jND_LIVE;
			this.nicname = nic.name();
			this.snaplen = 1550;
			this.promisc = promisc;
			this.to_ms = 100;
			synchronized (mutex) {
				inuse = jND_LIVE;
				reply = openCapture(this.mode, this.nicname, this.snaplen, this.promisc, this.to_ms);
			}
			currPcap_d = parseReply(reply);
			if (currPcap_d == NULLPTR) {
				inuse = jND_NONE;
				throw new jND_Exception("jND_CaptureSession.constructor : "
						+ captureSessionError);
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.constructor : "
					+ e.toString());
		}

	}

	/**
	 * Creates a new capture session. This session will read saved packets from
	 * the provided capture file, until EOF, when the capture will automatically
	 * stop. This is not a live capture, but a capture of saved packets from a
	 * tcpdump file.
	 * 
	 * @param file
	 *            a file containing saved packets (saved in tcpdump format).
	 * @author Pete Lutz
	 */
	public jND_CaptureSession(String file) {
		try {
			int num = jND_NICList.numberOfNICs();	// Force fill of NIC info
			byte[] reply = null;
			synchronized (special_mutex) {
				currCaptureID = nextCaptureID++;
			}
			p_filter = NULLPTR;
			capturing = false;
			currFilter = "";
			currPacketQueue = new jND_PacketQueue();
			currPcap_d = NULLPTR;
			dumper = NULLPTR;
			mutex = new jND_Mutex();

			delayed_dispose = false;
			delayed_stop = false;

			this.mode = jND_OFFLINE;
			this.nicname = file;
			this.snaplen = 0;
			this.promisc = false;
			this.to_ms = 0;
			synchronized (mutex) {
				inuse = jND_OFFLINE;
				reply = openCapture(this.mode, this.nicname, this.snaplen, this.promisc, this.to_ms);
			}
			currPcap_d = parseReply(reply);
			if (currPcap_d == NULLPTR) {
				inuse = jND_NONE;
				throw new jND_Exception("jND_CaptureSession.constructor : "
						+ captureSessionError);
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.constructor : "
					+ e.toString());
		}

	}

	/**
	 * Get underlying thread
	 */
	public Thread getThread() { return javaThread; }

	/**
	 * Get delayed dispose state.
	 * If we dispose of a capture when the capture thread is runing, this is actually
	 * delayed until a SECOND stop, called from the thread, when it completes.
	 */
	public boolean getDelayedDispose() { return delayed_dispose; }

	/**
	 * Dispose of a capture session. This also closes any open dump
	 * sessions and destroys any compiled filters. After calling
	 * dispose, the capture session can no longer be used for 
	 * capturing packets. This is more drastic than stop(). After
	 * calling stop(), the session may be restarted with start();
	 * 
	 * @author Pete Lutz
	 */
	public void dispose() {
		try {
			// NOTE: Stopping a java thread is not easy. We control this via
			// the 'capture' field which signals the thread to stop when it is false.
			// The thread may not respond right away ... so if it needs the session
			// for a while, we need to delay the dispose.
			//
			// A dispose is delayed if there is a javaThread running. When it completes
			// it checks the delayed_dispose field and then disposes of itself ... which results
			// in a REAL dispose.
			if(javaThread != null && !delayed_dispose) {
				delayed_dispose = true;
				return;
			}

			// Do dispose
			if (currPcap_d != NULLPTR) {
				closeCapture(currPcap_d, p_filter, dumper);
				currPcap_d = NULLPTR;
				p_filter = NULLPTR;
				currFilter = "";
				dumper = NULLPTR;
			}
			captureStatus(false);
			delayed_dispose = false;
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.dispose : "
					+ e.toString());
		}
	}

	/**
	 * Packets can be saved to a dump file. See openDumpFile and dumpPacket for
	 * more information. This closes an open dump file.
	 * 
	 * @author Pete Lutz
	 */
	public void closeDumpFile() {
		try {
			synchronized (mutex) {
				if (dumper == NULLPTR) {
					throw new jND_Exception(
							"jND_CaptureSession: closeDumpFile: No dump session open.");
				} else {
					dumpClose(dumper);
					dumper = NULLPTR;
				}
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			new jND_Exception("jND_CaptureSession.closeDumpFile : "
					+ e.toString());
		}

	}

	/**
	 * Dumps one packet to an already open dumpfile.
	 * 
	 * @param pkt -
	 *            the packet to dump.
	 * @author Pete Lutz
	 */
	public void dumpPacket(byte[] pkt) {
		try {
			synchronized (mutex) {
				if (dumper == NULLPTR) {
					throw new jND_Exception(
							"jND_CaptureSession: dumpPacket: No dump session open.");
				} else {
					dump1(dumper, pkt);
				}
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.dumpPacket : "
					+ e.toString());
		}

	}

	/**
	 * Obtains the ID of this capture session.
	 * 
	 * @return the capture ID of the session. This is an integer and has no
	 *         meaning other than to differentiate between different sessions.
	 * @author Pete Lutz
	 */
	public int captureID() {
		return currCaptureID;
	}

	/**
	 * Obtains the pcap descriptor of this capture session.
	 * 
	 * @return the pcap descriptor of the session.
	 * @author Pete Lutz
	 */
	public byte[] pcap_d() {
		return currPcap_d;
	}

	/**
	 * Obtains the status of this capture session. If jND_NONE is returned, the
	 * capture session is not in use. If jND_LIVE is returned, this is a live
	 * capture session, capturing packets from a NIC. If jND_OFFLINE is
	 * returned, this is an OFFLINE capture, which reads packets from a
	 * previously created dump file.
	 * 
	 * jND_NONE, jND_LIVE and jND_OFFLINE are defined when jND_CaptureSession.h
	 * is included.
	 * 
	 * @return the capture mode: one of jND_NONE, jND_LIVE or jND_OFFLINE.
	 * @author Pete Lutz
	 */
	public int inUseStatus() {
		return inuse;
	}

	/**
	 * Obtains the capturing state of this capture session.
	 * 
	 * @return the capture state ... <code>true</code> if a capture is in
	 *         progress, <code>false</code> otherwise
	 * @author Pete Lutz
	 *  
	 */
	public boolean captureStatus() {
		return capturing;
	}

	/**
	 * Sets the status of this capture session. If <code>status == true</code>
	 * this means the the session is NOT capturing. If
	 * <code>status == false</code> then this means that the session IS
	 * capturing.
	 * 
	 * @param status -
	 *            the new status (capturing or note)
	 * @author Pete Lutz
	 */
	public void captureStatus(boolean status) {
		capturing = status;
	}

	/**
	 * Obtains the current filter String for this session.
	 * 
	 * @return a char string which is the currently installed filter. This is
	 *         the empty string if no filter is installed.
	 * @author Pete Lutz
	 */
	public String filter() {
		return currFilter;
	}

	/**
	 * Obtains the queue of packets associated with this capture session. As a
	 * capture proceeds, it stores packets in this queue. The thread wishing to
	 * examine or process the packets must retrieve them from this queue.
	 * 
	 * @return a queue of captured packets
	 * @author Pete Lutz
	 */
	public jND_PacketQueue packetQueue() {
		return currPacketQueue;
	}

	/**
	 * Opens a dump file for saving packets. Packets are saved to the file with
	 * the dumpPacket method.
	 * 
	 * @param fname -
	 *            the name of the dump file
	 * @author Pete Lutz
	 */
	public void openDumpFile(String fname) {
		try {
			byte[] reply = null;
			synchronized (mutex) {
				if (dumper != NULLPTR) {
					throw new jND_Exception(
							"jND_CaptureSession: openDumpFile: Dump session already open");
				} else {
					reply = dumpOpen(currPcap_d, fname);
				}
			}
			dumper = parseReply(reply);
			if (dumper == NULLPTR) {
				throw new jND_Exception("jND_CaptureSession.openDumpFile : "
						+ captureSessionError);
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.openDumpFile : "
					+ e.toString());
		}

	}

	/**
	 * Sets the filter for the capture session. It is OK to set the filter while
	 * the Capture Session is capturing. This method expects the netmask (below)
	 * to be an int. The netmask is necessary for some filter strings.
	 * 
	 * For details on filter strings, see the documentation of capture filters
	 * for tcpdump.
	 * 
	 * @param filterStr
	 *            a char string describing the filter using the syntax for
	 *            tcpdump.
	 * @param optimize
	 *            should the filter be optimized of not?
	 * @param netmask
	 *            the netmask used by the filter. Some filter elements require
	 *            this, others do not. Generally, those that involve IP
	 *            addresses are those that use the netmask. NOTE: The netmask
	 *            for a nic can be obtained from the associated jND_NIC object.
	 * @author Pete Lutz
	 */
	public void filter(String filterStr, boolean optimize, jND_BYTE4 netmask) {
		try {
			byte[] reply = null;
			currFilter = filterStr;

			synchronized (mutex) {
				reply = installFilter(currPcap_d, p_filter, currFilter,
						optimize, netmask.toInt());
			}
			p_filter = parseReply(reply);
			if (p_filter == NULLPTR)
				throw new jND_Exception("jND_CaptureSession.filter : "
						+ captureSessionError);
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.filter : "
					+ e.toString());
		}
	}

	/**
	 * Sets the filter for the capture session. It is OK to set the filter while
	 * the Capture Session is capturing. This method expects the netmask (below)
	 * to be an jND_BYTE1 array.
	 * 
	 * For details on filter strings, see the documentation of capture filters
	 * for tcpdump.
	 * 
	 * @param filterStr
	 *            a char string describing the filter. using the syntax for
	 *            tcpdump.
	 * @param optimize
	 *            should the filter be optimized of not?
	 * @param inmask
	 *            the netmask used by the filter. Some filter elements require
	 *            this, others do not. Generally, those that involve IP
	 *            addresses are those that use this netmask. NOTE: The netmask
	 *            for a nic can be obtained from the associated jND_NIC object.
	 * @author Pete Lutz
	 */
	public void filter(String filterStr, boolean optimize,
			jND_IPv4Address inmask) {
		try {
			byte[] reply = null;
			jND_BYTE4 netmask = inmask.toByte4();
			currFilter = filterStr;

			synchronized (mutex) {
				reply = installFilter(currPcap_d, p_filter, filterStr,
						optimize, inmask.toByte4().toInt());
			}
			p_filter = parseReply(reply);
			if (p_filter == NULLPTR)
				throw new jND_Exception("jND_CaptureSession.filter : "
						+ captureSessionError);
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.filter : "
					+ e.toString());
		}

	}

	/**
	 * Starts capturing packets. The capture proceeds in a separate thread. As
	 * packets arrive, they are placed in a packet queue (jND_PacketQueue) which
	 * may be obtained via the packetQueue method. Packets may be retrieved from
	 * this queue.
	 * 
	 * @author Pete Lutz
	 */
	public void start() {
		try {
			if (!capturing) {
				// Start a thread
				captureStatus(true);	
				capThread = new jND_CaptureThread(this);
				javaThread = new Thread(capThread);
				javaThread.start();
			} else {
				throw new jND_Exception(
						"jND_CaptureSession: start: only one capture thread allowed per jND_CaptureSession.");
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.start : "
					+ e.toString());
		}

	}

	/**
	 * Get delayed stop state.
	 * If we stop a capture when the capture thread is runing, this is actually
	 * delayed until a SECOND stop, called from the thread, when it completes.
	 */
	public boolean getDelayedStop() { return delayed_stop; }

	/**
	 * Stops capturing packets. This actually closes the session, then reopens
	 * it, as this is a sure fire way to cancel a waiting capture call. This is
	 * less drastic than dispose, as the packet queue remains intact and the
	 * session is reopened after being closed.
	 * 
	 * @author Pete Lutz
	 */
	public void stop() {
		try {
			byte[] reply;
			synchronized (mutex) {
				capturing = false;
				// NOTE: Stopping a java thread is not easy. We control this via
				// the 'capture' field which signals the thread to stop when it is false.
				// The thread may not respond right away ... so if it needs the session
				// for a while, we need to delay the stop.
				//
				// A stop is delayed if there is a javaThread running. When it completes
				// it checks the delayed_stop field and then stops itself ... which results
				// in a REAL stop.
				if(javaThread != null && !delayed_stop) {
					delayed_stop = true;
					return;
				}
				closeCapture(currPcap_d, p_filter, dumper);
				currPcap_d = NULLPTR;
				p_filter = NULLPTR;
				currFilter = "";
				dumper = NULLPTR;
				reply = openCapture(this.mode, this.nicname, this.snaplen, this.promisc, this.to_ms);
				delayed_stop = false;
			}
			currPcap_d = parseReply(reply);
			if (currPcap_d == NULLPTR) {
				inuse = jND_NONE;
				throw new jND_Exception("jND_CaptureSession.stop : Cannot restart session: "
						+ captureSessionError);
			}
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_CaptureSession.stop : " + e.toString());
		}

	}
};
