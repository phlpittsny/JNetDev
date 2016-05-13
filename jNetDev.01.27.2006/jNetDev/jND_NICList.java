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
 * jND_NICList <br>
 * This class models the list of all NICs in the machine. Generally,
 * you should use jND_NIC instead of jND_NICList to access nics.
 * jND_NIC extends this class to provide NIC access.
 * <br><br>
 * @author	Pete Lutz
 */
public class jND_NICList {
	
	private static int ndevs;
	private static jND_Mutex special_mutex = new jND_Mutex();

	protected static String[] v_name;
	protected static String[] v_description;
	protected static jND_IPv4Address[] v_ipaddress;
	protected static jND_IPv4Address[] v_netmask;
	protected static jND_IPv4Address[] v_gateway;
	protected static jND_EthernetAddress[] v_macaddress;
	protected static boolean setupdone;	// indicates whether setup has been run.

	// ==========  B E G I N     N A T I V E     M E T H O D S  ==========
	
	/**
	 * Private method to obtain the number of NICs in this machine from the
	 * underlying operating system.
	 * @return		The number of NICs in this machine.
     * @author Pete Lutz
	 */
	private static native int os_numberOfNICs();

	/**
	 * Private method to obtain the name of a NIC. The name is a device name
	 * and varies from system to system.
	 * @param		nicno - the index of the NIC (0-based indexing).
	 * @return		The device name for the NIC.
     * @author Pete Lutz
	 */
	private static native String os_nicName(int nicno);

	/**
	 * Private method to obtain the description of a NIC. The description
	 * may be a human-consumable identifier with information such as the 
	 * manufacturer and model number or may be the same as the device
	 * name ... depending on the underlying OS.
	 * @param		nicno - the index of the NIC (0-based indexing).
	 * @return		The description of the NIC.
     * @author Pete Lutz
	 */
	private static native String os_nicDescription(int nicno);

	/**
	 * Private method to obtain the IP address bound to a NIC. This is obtained
	 * from the underlying operating system.
	 * @param		nicno - the index of the NIC (0-based indexing).
	 * @return		The IP address for the NIC.
     * @author Pete Lutz
	 */
	private static native String os_nicIpaddress(int nicno);

	/**
	 * Private method to obtain the netmask for the network that a NIC
	 * is connected to. This is obtained from the underlying operating system.
	 * @param		nicno - the index of the NIC (0-based indexing).
	 * @return		The netmask for the NIC.
     * @author Pete Lutz
	 */
	private static native String os_nicNetmask(int nicno);

	/**
	 * Private method to obtain the gateway (router) IP address for a NIC. 
	 * This is obtained from the underlying operating system.
	 * @param		nicno - the index of the NIC (0-based indexing).
	 * @return		The router address for the NIC.
     * @author Pete Lutz
	 */
	private static native String os_nicGateway(int nicno);

	/**
	 * Private method to obtain the MAC address bound to a NIC. This is obtained
	 * from the underlying operating system.
	 * @param		nicno - the index of the NIC (0-based indexing).
	 * @return		The MAC address for the NIC.
     * @author Pete Lutz
	 */
	private static native String os_nicMacaddress(int nicno);
	
	// ==========   E N D     N A T I V E     M E T H O D S   ==========

	/**
	 * Set up the NIC list data structures on first call to a method.
	 * @author Pete Lutz
	 */
	protected static void setup() {
		synchronized(special_mutex) {
			if(setupdone) {
				return;
			}

			System.loadLibrary("jNetDev");
			
			setupdone = true;
			try {
				int retn = 0;
				ndevs = os_numberOfNICs();
				
				v_name = new String[ndevs];
				v_description = new String[ndevs];
				v_ipaddress = new jND_IPv4Address[ndevs];
				v_netmask = new jND_IPv4Address[ndevs];
				v_gateway = new jND_IPv4Address[ndevs];
				v_macaddress = new jND_EthernetAddress[ndevs];
				
				int i;
				for(i = 0; i < ndevs; i++) {
					v_name[i] = os_nicName(i);
					v_description[i] = os_nicDescription(i);
					
					try {
						v_ipaddress[i] = new jND_IPv4Address(os_nicIpaddress(i));
					} catch( Exception e ) {
						v_ipaddress[i] = new jND_IPv4Address();
					}
					
					try {
						v_netmask[i] = new jND_IPv4Address(os_nicNetmask(i));
					} catch( Exception e ) {
						v_netmask[i] = new jND_IPv4Address();
					}
					
					try {
						v_gateway[i] = new jND_IPv4Address(os_nicGateway(i));
					} catch( Exception e ) {
						v_gateway[i] = new jND_IPv4Address();
					}
					
					try {
						v_macaddress[i] = new jND_EthernetAddress(os_nicMacaddress(i));
					} catch( Exception e ) {
						v_macaddress[i] = new jND_EthernetAddress();
					}
				}
			} catch (Exception e) {
				throw new jND_Exception("jND_NICList.setup : " + e.toString());
			}
		} // End of Synchronized						
	}

	/**
	 * Construct the NIC list information. Get most from pcap
	 * and in the case of Windows, get more info from the
	 * windows API.
	 * @author Pete Lutz
	 */
	public jND_NICList() {};

	/**
	 * Obtain the name of a device - this is the system name for the NIC.
	 * @param		devno - The # of the NIC whose name is requested.
	 * @return		The device name of the NIC.
	 * @author Pete Lutz
	 */
	public static String nicName(int devno) {
		setup();
		if(devno < 0 || devno >= ndevs)
			throw new jND_Exception(
				"jND_NICList.nicName : device number out of range - " + devno);
		return v_name[devno];
	}
	
	/**
	 * Obtain the description of the device. 
	 * This is a human readable name for the NIC.
	 * @param		devno - The # of the NIC whose name is requested.
	 * @return		The description of the NIC.
	 * @author Pete Lutz
	 */
	public static String nicDescription(int devno) {
		setup();
		if(devno < 0 || devno >= ndevs)
			throw new jND_Exception(
				"jND_NICList.nicDescription : device number out of range - " + devno);
		return v_description[devno];
	}
	
	/**
	 * Get IP address for a NIC.
	 * @return		The IP address for the NIC as a jND_IPv4Address object.
	 * @param		devno - The # of the NIC whose description is requested.
	 * @author Pete Lutz
	 */
	public static jND_IPv4Address nicIPAddress(int devno) {
		setup();
		if(devno < 0 || devno >= ndevs)
			throw new jND_Exception(
				"jND_NICList.nicIPAddress : device number out of range - " + devno);
		return v_ipaddress[devno];
	}
	
	/**
	 * Get Netmask for a NIC.
	 * @return		The netmask for the NIC as a jND_IPv4Address object.
	 * @param		devno - The # of the NIC whose IP address is requested.
	 * @author Pete Lutz
	 */
	public static jND_IPv4Address nicNetMask(int devno) {
		setup();
		if(devno < 0 || devno >= ndevs)
			throw new jND_Exception(
				"jND_NICList.nicNetMask : device number out of range - " + devno);
		return v_netmask[devno];
	}
	
	/**
	 * Get Gateway address for a NIC.
	 * @return		The IP of the gateway for the NIC 
	 * as a jND_IPv4Address object.
	 * @param		devno - The # of the NIC whose gateway is requested.
	 * @author Pete Lutz
	 */
	public static jND_IPv4Address nicGateway(int devno) {
		setup();
		if(devno < 0 || devno >= ndevs)
			throw new jND_Exception(
				"jND_NICList.nicGateway : device number out of range - " + devno);
		return v_gateway[devno];
	}
	
	/**
	 * Get MAC address for a NIC as a jND_EthernetAddress object.
	 * @param		devno - The # of the NIC whose MAC address
	 * 				is requested.
	 * @return		The MAC address of the NIC.
	 * @author Pete Lutz
	 */
	public static jND_EthernetAddress nicMACAddress(int devno) {
		setup();
		if(devno < 0 || devno >= ndevs)
			throw new jND_Exception(
				"jND_NICList.nicMACAddress : device number out of range - " + devno);
		return v_macaddress[devno];
	}
	
	/**
	 * Obtain the number of NIC in this machine.
	 * @return		The number of NICs in the machine.
	 * @author Pete Lutz
	 */
	public static int numberOfNICs() {
		setup();
		return ndevs;
	}
};

