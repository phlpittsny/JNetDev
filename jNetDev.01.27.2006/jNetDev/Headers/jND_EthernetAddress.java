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
package jNetDev.Headers;
import jNetDev.*;

/** 
 * jND_EthernetAddress <br>
 * Class to model an Ethernet MAC address. One can think of this largely as
 * a converter class. The constructors will make a jND_EthernetAddress
 * out of a byte[] or a String or a jND_BYTE8. 
 * The toByteArray, toString and toByte8
 * methods to the inverse conversions.
 * <br><br>
 * In addition, the address() mutators allow one to change the underlying
 * address after it is constructed.
 * <br><br>
 * @author Pete Lutz
 */
 
public class jND_EthernetAddress extends Object
{
	private byte[] v_mac;
	
    public static final int ENET_ADDR_LEN = 6;
    
    /**
     * Default constructor: Constructs an 'empty' address
     * which is equivalent to 00:00:00:00:00:00.
     * @author Pete lutz
     */
	public jND_EthernetAddress()
	{
		v_mac = new byte[ENET_ADDR_LEN];
		for(int i = 0; i < ENET_ADDR_LEN; i++)
			v_mac[i] = 0;
	}
	
	/**
	 * Construct an Ethernet address from a byte array. If the length
	 * of the array is not jND_EthernetAddress.ENET_ADDR_LEN
	 * an exception is thrown.
	 * @param mac - the byte array containing the address.
	 * @author Pete Lutz
	 */
	public jND_EthernetAddress(byte[] mac) throws jND_Exception 
	{
		v_mac = new byte[ENET_ADDR_LEN];
		address(mac);
	}
	
	/**
	 * Construct an Ethernet address from a string. The format of
	 * the string is a sequence of 6, 2-digit hexadecimal numbers
	 * separated by colons or dashes. Thus, legal strings include
	 * aa:01:44:00:df:11 and 00-ba-d4-23-44-01. If the length
	 * of the address is not jND_EthernetAddress.ENET_ADDR_LEN
	 * an exception is thrown.
	 * @param hex - the string containing the address in hexadecimal.
	 * @author Pete Lutz
	 */
	public jND_EthernetAddress(String hex) throws jND_Exception {
		v_mac = new byte[ENET_ADDR_LEN];
		address(hex);
	}
	
	/**
	 * Construct an Ethernet address from a 8-byte integer. 
	 * 
	 * @param mac - the 8-byte integer containing the address.
	 * @author Pete Lutz
	 */
	public jND_EthernetAddress(jND_BYTE8 mac) {
		v_mac = new byte[ENET_ADDR_LEN];
		address(mac);
	}
	
	/**
	 * Mutate (change) the underlying address based on a
	 * byte array. If the length
	 * of the array is not jND_EthernetAddress.ENET_ADDR_LEN
	 * an exception is thrown.
	 * @param mac - the new value of the Ethernet address
	 * @author Pete Lutz
	 */
	public void address(byte[] mac) throws jND_Exception {
		if(mac.length != ENET_ADDR_LEN) 
			throw new jND_Exception("jND_EthernetAddress.address: mac must have 6 octets");
		for(int i = 0; i < ENET_ADDR_LEN; i++)
			v_mac[i] = mac[i];
	}
	
	/**
	 * Mutate (change) the underlying address based on a string 
	 * parameter. The format of
	 * the string is a sequence of 6, 2-digit hexadecimal numbers
	 * separated by colons or dashes. Thus, legal strings include
	 * aa:01:44:00:df:11 and 00-ba-d4-23-44-01. If the length
	 * of the address is not jND_EthernetAddress.ENET_ADDR_LEN
	 * an exception is thrown.
	 * @param hex - the string containing the address in hexadecimal.
	 * @author Pete Lutz
	 */
	public void address(String hex) throws jND_Exception {
		try {		
			java.util.StringTokenizer strtok = new java.util.StringTokenizer(hex, ":-");
			if(strtok.countTokens() != ENET_ADDR_LEN) 
				throw new jND_Exception("jND_EthernetAddress.address: mac must have 6 octets");

			v_mac[0] = (byte)Integer.parseInt(strtok.nextToken(), 16);
			v_mac[1] = (byte)Integer.parseInt(strtok.nextToken(), 16);
			v_mac[2] = (byte)Integer.parseInt(strtok.nextToken(), 16);
			v_mac[3] = (byte)Integer.parseInt(strtok.nextToken(), 16);
			v_mac[4] = (byte)Integer.parseInt(strtok.nextToken(), 16);
			v_mac[5] = (byte)Integer.parseInt(strtok.nextToken(), 16);
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_EthernetAddress.address : " + e.toString());
		}
	}
	
	/**
	 * Mutate (change) the underlying address based on an
	 * 8-byte integer. 
	 * @param mac - the new value of the Ethernet address as an 8-byte integer.
	 * @author Pete Lutz
	 */
	public void address(jND_BYTE8 mac){
		long tmp = mac.toLong();
		
		v_mac[5] = (byte)(tmp & 0xff);
		tmp = tmp >> 8;
		v_mac[4] = (byte)(tmp & 0xff);
		tmp = tmp >> 8;
		v_mac[3] = (byte)(tmp & 0xff);
		tmp = tmp >> 8;
		v_mac[2] = (byte)(tmp & 0xff);
		tmp = tmp >> 8;
		v_mac[1] = (byte)(tmp & 0xff);
		tmp = tmp >> 8;
		v_mac[0] = (byte)(tmp & 0xff);
	}
	
	/**
	 * Convert the underlying Ethernet address to a byte array.
	 * @return the address as a byte array.
	 * @author Pete Lutz
	 */
	public byte[] toByteArray()  {
		return v_mac;
	}
	
	/**
	 * Convert the underlying Ethernet address to a string in the
	 * form aa:01:44:00:df:11.
	 * @return the address as a string.
	 * @author Pete Lutz
	 */
	public String toString() throws jND_Exception {
		try {
			String retStr = jND_Utility.byte1ToHex(new jND_BYTE1(v_mac[0]));
			for(int i = 1; i < 6; i++) {
				String tmp = jND_Utility.byte1ToHex(new jND_BYTE1(v_mac[i]));
				retStr = retStr + ":" + tmp;
			}
			return retStr;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_EthernetAddress.toString : " + e.toString());
		}
	}
	
	/**
	 * Convert the underlying Ethernet address to an 8-byte integer.
	 * @return the address as an 8-byte integer.
	 * @author Pete Lutz
	 */
	public jND_BYTE8 toByte8()  {
		long retval = 0;
		
		retval |= (v_mac[0] & 0xff);
		retval = retval << 8;
		retval |= (v_mac[1] & 0xff);
		retval = retval << 8;
		retval |= (v_mac[2] & 0xff);
		retval = retval << 8;
		retval |= (v_mac[3] & 0xff);
		retval = retval << 8;
		retval |= (v_mac[4] & 0xff);
		retval = retval << 8;
		retval |= (v_mac[5] & 0xff);
		return new jND_BYTE8(retval);
	}


};
