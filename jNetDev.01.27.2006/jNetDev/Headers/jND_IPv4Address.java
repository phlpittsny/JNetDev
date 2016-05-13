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
 * jND_IPv4Address<br>
 * Class to model an IPv4 address. One can think of this largely as
 * a converter class. The constructors will make a jND_IPv4Address
 * out of a byte[] or a String or a jND_BYTE4. 
 * The toByteArray, toString and toByte4
 * methods to the inverse conversions.
 * <br><br>
 * In addition, the address() mutators allow one to change the underlying
 * address after it is constructed.
 * <br><br>
 * @author Pete Lutz
 */
public class jND_IPv4Address
{
	private byte[] v_ip;
	
    public static final int IP_ADDR_LEN = 4;
    
    /**
     * Default constructor: Constructs an 'empty' address
     * which is equivalent to 0.0.0.0.
     * @author Pete lutz
     */    
    public jND_IPv4Address()
    {
    	v_ip = new byte[IP_ADDR_LEN];
    	for(int i = 0; i < IP_ADDR_LEN; i++)
    		v_ip[i] = 0;
    }
	
	/**
	 * Construct an IP address from a byte array. If the length
	 * of the array is not jND_IPv4Address.IP_ADDR_LEN
	 * an exception is thrown.
	 * @param ip - the byte array containing the address.
	 * @author Pete Lutz
	 */
    public jND_IPv4Address(byte[] ip)
    {
    	v_ip = new byte[IP_ADDR_LEN];
    	address(ip);
    }
	
	/**
	 * Construct an IP address from a string. The string
	 * is the standard dotted quad format (e.g., 10.2.23.4). If the length
	 * of the address is not jND_IPv4Address.IP_ADDR_LEN
	 * an exception is thrown.
	 * @param dq - the string containing the address as a dotted quad.
	 * @author Pete Lutz
	 */
    public jND_IPv4Address(String dq)
    {
    	v_ip = new byte[IP_ADDR_LEN];
    	address(dq);
    }
	
	/**
	 * Construct an IP address from a 4-byte integer. 
	 * 
	 * @param ip - the 4-byte integer containing the address.
	 * @author Pete Lutz
	 */
    public jND_IPv4Address(jND_BYTE4 ip){
    	v_ip = new byte[IP_ADDR_LEN];
    	address(ip);
    }
	
	/**
	 * Mutate (change) the underlying address based on a
	 * byte array. If the length
	 * of the array is not jND_IPv4Address.IP_ADDR_LEN
	 * an exception is thrown.
	 * @param ip - the new value of the IP address
	 * @author Pete Lutz
	 */
    public void address(byte[] ip){
    	for(int i = 0; i < IP_ADDR_LEN; i++)
    		v_ip[i] = ip[i];
    }

	
	/**
	 * Mutate (change) the underlying address based on a string 
	 * parameter. The format of
	 * the string is a dotted quad. If the length
	 * of the address is not jND_IPv4Address.IP_ADDR_LEN
	 * an exception is thrown.
	 * @param dq - the string containing the address as a dotted quad.
	 * @author Pete Lutz
	 */
    public void address(String dq) throws jND_Exception
    {
    	try {
    		java.util.StringTokenizer strtok = new java.util.StringTokenizer(dq, ".");
    		if(strtok.countTokens() != IP_ADDR_LEN) 
    			throw new jND_Exception("jND_IPv4Address.address: ip must have 4 octets");    			
    			
    		v_ip[0] = (byte)Integer.parseInt(strtok.nextToken());
    		v_ip[1] = (byte)Integer.parseInt(strtok.nextToken());
    		v_ip[2] = (byte)Integer.parseInt(strtok.nextToken());
    		v_ip[3] = (byte)Integer.parseInt(strtok.nextToken());
    	} catch(jND_Exception nde) {
    		throw nde;
    	} catch (Exception e) {
    		throw new jND_Exception("jND_IPv4Address.address : " + e.toString());
    	}
    }
	
	/**
	 * Mutate (change) the underlying address based on an
	 * 4-byte integer. 
	 * @param ip - the new value of the IP address as a 4-byte integer.
	 * @author Pete Lutz
	 */
    public void address(jND_BYTE4 ip) {
    	int tmp = ip.toInt();
    	
    	v_ip[3] = (byte)(tmp & 0xff);
    	tmp = tmp >> 8;
    	v_ip[2] = (byte)(tmp & 0xff);
    	tmp = tmp >> 8;
    	v_ip[1] = (byte)(tmp & 0xff);
    	tmp = tmp >> 8;
    	v_ip[0] = (byte)(tmp & 0xff);
    }
	
	/**
	 * Convert the underlying IP address to a string in 
     * dotted quad form: 10.2.33.4
	 * @return the address as a string.
	 * @author Pete Lutz
	 */
    public String toString() throws jND_Exception {
    	try {
			String retStr = "" + (((int)v_ip[0]) & 0xff);
			for(int i = 1; i < 4; i++) {
				String tmp = "" + (((int)v_ip[i]) & 0xff);
				retStr = retStr + "." + tmp;
			}
			return retStr;
    	} catch(jND_Exception nde) {
    		throw nde;
    	} catch (Exception e) {
    		throw new jND_Exception("jND_IPv4Address.toString : " + e.toString());
    	}
    }
	
	/**
	 * Convert the underlying IP address to a byte array.
	 * @return the address as a byte array.
	 * @author Pete Lutz
	 */
    public byte[] toByteArray()  {
    	return v_ip;
    }
	
	/**
	 * Convert the underlying IP address to a 4-byte integer.
	 * @return the address as an 4-byte integer.
	 * @author Pete Lutz
	 */
    public jND_BYTE4 toByte4()  {
    	int retval = 0;
    	
    	retval |= (v_ip[0] & 0xff);
    	retval = retval << 8;
    	retval |= (v_ip[1] & 0xff);
    	retval = retval << 8;
    	retval |= (v_ip[2] & 0xff);
    	retval = retval << 8;
    	retval |= (v_ip[3] & 0xff);
    	return new jND_BYTE4(retval);
    }
};
