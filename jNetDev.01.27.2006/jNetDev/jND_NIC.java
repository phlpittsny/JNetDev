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
 * jND_NIC <br>
 * Class to model a NIC on the machine. Properties include the IP address 
 * of the NIC, the netMask, the gateway's IP address, adn the MAC 
 * address. There is also a name and a description. The name is 
 * the OS name for the NIC. The description is a human readable 
 * description of the NIC.
 * <br><br>
 * In addition, packets can be injected into the network via the
 * NIC. The NIC must be opened before injection and closed when
 * injection is finished.
 * <br><br>
 * SPECIAL NOTE: Once a NIC is opened, it must be closed. It is
 * the programmer's responsibility to close all NICs that s/he
 * opens.
 * <br><br>
 * Note: this is a derived class from the base class jND_NICList. 
 * jND_NICList is a class that contains arrays of information for
 * all NICs on the machine. A jND_NIC is essentially an object
 * with the index of the appropriate NIC bound to an instance 
 * variable. In addition, the jND_NIC class provides for packet
 * injection. Packet injection is not supported through jND_NICList.
 * <br><br>
 * @author	Pete Lutz
 */

public class jND_NIC extends jND_NICList {
	private int v_devNo;	
	private byte[] v_adapter;

	/**
	 * Default constructor - This is allowed, but only as a place
	 * holder before you assign another NIC to this one. As a result
	 * the device number is set to -1. An attempt to use a NIC
	 * constructed by this constructor will result in an exception.
	 */
	public jND_NIC() {
		v_devNo = -1;
		v_adapter = null;
	}

	/**
	 * Construct a jND_NIC, given its device number, which is an
	 * index. The index is between 0 and numberOfNICs() inclusive.
	 * @author	Pete Lutz
	 */
	public jND_NIC(int nicno) {
		setup();
		if(nicno < 0 || nicno >= numberOfNICs())
			throw new jND_Exception(
				"jND_NIC.jND_NIC : device number out of range - " + nicno);
		v_devNo = nicno;
		v_adapter = null;
	}
	
	/**
	 * Get the NIC's IP address as a jND_IPv4Address object.
	 * @return		An opject containing the IP address.
	 * @author	Pete Lutz
	 */
	public jND_IPv4Address ipAddress() {
		return nicIPAddress(v_devNo);
	}
	
	/**
	 * Get the NIC's netMask as a jND_IPv4Address object.
	 * @return		An object containing the netMask.
	 * @author	Pete Lutz
	 */
	public jND_IPv4Address netMask() {
		return nicNetMask(v_devNo);
	}
	
	/**
	 * Get the IP address of the gateway for the NIC as 
	 * a jND_IPv4Address object.
	 * @return		An object containing the gateway's IP address.
	 * @author	Pete Lutz
	 */
	public jND_IPv4Address gateway() {
		return nicGateway(v_devNo);
	}
	
	/**
	 * Get the NIC's MAC address as a jND_EthernetAddress object.
	 * @return		An object containing the MAC address.
	 * @author	Pete Lutz
	 */
	public jND_EthernetAddress macAddress() {
		return nicMACAddress(v_devNo);
	}
	
	/**
	 * Get the NIC's operating system name.
	 * @return		String containing the name.
	 * @author	Pete Lutz
	 */
	public String name() {
		return nicName(v_devNo);
	}
	
	/**
	 * Get the NIC's human readable description.
	 * @return		A String containing the description.
	 * @author	Pete Lutz
	 */
	public String description() {
		return nicDescription(v_devNo);
	}

	/**
	 * Opens the NIC for injection of packets.
	 * @author	Pete Lutz
	 */
	public void open() {
		byte[] reply = nic_open(nicName(v_devNo));
		// Check for empty reply
		if (reply == null || reply.length < 1) {
			throw new jND_Exception(
				"jND_NIC.open: Cannot parse reply from native method.");
		}

		// Get the size of the ptr (if any)
		int size = ((int) reply[0]) & 0xff;

		// If size == 0, get the message
		if (size == 0) {
			throw new jND_Exception(
				"jND_NIC.inject: " + new String(reply, 1, reply.length - 1));
		}
		v_adapter = new byte[size];
		for(int i = 0; i < size; i++)
			v_adapter[i] = reply[i+1];
		return; // All is OK
	}

	/**
	 * Closes the NIC for injection of packets.
	 * @author	Pete Lutz
	 */
	public void close() {
		nic_close(v_adapter);
		v_adapter = null;
	}

	/**
	 * Determine if the NIC is closed
	 * @author	Pete Lutz
	 */
	public boolean closed() {
		return v_adapter == null;
	}

	/**
	 * Injects a packet via this NIC. The reply from the native
	 * method, capture1, looks like this: 
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
	 * 0 (indicating an error). 
	 * 
	 * Actually, the pcapd object has no meaning, in this case, and the
	 * size is ONLY there to indicate the presence or absence of an
	 * error.
	 * 
	 * @param   packet      The packet to inject.
	 * @author	Pete Lutz
	 */
	public void inject(byte[] packet) {	
		byte[] reply = nic_inject(packet, v_adapter, v_devNo);	
		// Check for empty reply
		if (reply == null || reply.length < 1) {
			throw new jND_Exception(
				"jND_NIC.inject: Cannot parse reply from native method.");
		}

		// Get the size of the ptr (if any)
		int size = ((int) reply[0]) & 0xff;

		// If size == 0, get the message
		if (size == 0) {
			throw new jND_Exception(
				"jND_NIC.inject: " + new String(reply, 1, reply.length - 1));
		}
		return; // All is OK
	}
	
	/**
	 * Private native method to open a NIC via the OS.
	 * @param devname - the name of the NIC to open.
	 * @return an adapter structure as a byte array.
	 * @author Pete Lutz
	 */
	private static native byte[] nic_open(String devname);
	
	/**
	 * Private native method to close a NIC via the OS.
	 * @param adapter - the adapter to close ... returned previously.
	 * from open.
	 * @author Pete Lutz
	 */
	private static native void nic_close(byte[] adapter);
	
	/**
	 * Private native method to inject a packet via the OS.
	 * @param packet - the packet to inject.
	 * @param adapter - the adapter to use, previously returned
	 * from open.
	 * @author Pete Lutz
	 */
	private static native byte[] nic_inject(byte[] packet, byte[] adapter, int nicno);
};

