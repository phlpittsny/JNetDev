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
import java.io.*;

/** 
 * jND_Ethernet802_3 <br>
 * Class to model an Ethernet 802.3 Frame. An Ethernet 802.3 frame has three fields,
 * the destination MAC address, the source MAC address, and the length. the
 * length field MUST be <= 1500. Otherwise, it is NOT a length but the type of
 * an EthernetII frame.
 * <pre>
 * Frame format:
 *          +-------------+-------------+--------+--------------------+
 *          | destAddress |sourceAddress| length |      payload       |
 *          +-----48------+-----48------+---16---+------variable------+
 * </pre>
 * As with all of the header modeling classes, this one contains 
 * accessors and mutators for each field, plus the two key methods:
 * parse and build.
 * <br><br>
 * Accessors are methods named
 * for each field which return the value of that field in the header
 * being manipulated. Accessors take no parameters, they allow the
 * user to "access" a field.
 * <br><br>
 * Mutators return no value. Instead, they take a parameter which is
 * the new value of a field in the header. Mutators allow the user
 * to change ("mutate") a field.
 * <br><br>
 * The parse method accepts a byte array (byte[])
 * and an integer offset into that byte array (defaults to 0) where
 * the header begins. It then separates out the fields into instance
 * variables that can be retrieved with the accessors. parse returns no
 * value.
 * <br><br>
 * The build method requires no parameters. It builds a byte array
 * from the instance variables for the fields and returns that byte
 * array as the result of the call.
 * <br><br>
 * @author	Pete Lutz
 */

public class jND_Ethernet802_3 {
	private int headerOffset;
	private int payloadOffset;
	private jND_EthernetAddress dstMAC;
	private jND_EthernetAddress srcMAC;
	private jND_BYTE2 type_len;
	private byte[] pload;

	/** 
	 * Construct an empty 802.3 frame.
	 * @author Pete Lutz
	 */
	public jND_Ethernet802_3(){
		try {
			headerOffset = -1;
			payloadOffset = -1;
			dstMAC = new jND_EthernetAddress();
			srcMAC = new jND_EthernetAddress();
			type_len =  new jND_BYTE2(0);;
			pload = null;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Ethernet802_3.constructor : " + e.toString());
		}
	}

	/**
	 * Method to retrieve the destination MAC address.
	 * @return		the MAC address as a jND_EthernetAddress
	 * @author	Pete Lutz
	 */
	public jND_EthernetAddress destAddress()  {
		return dstMAC;
	}

	/**
	 * Method to set the destination MAC address.
	 * @param		mac - the MAC address as a jND_EthernetAddress
	 * @author	Pete Lutz
	 */
	public void destAddress(jND_EthernetAddress mac) {
		dstMAC = mac;
	}

	/**
	 * Method to retrieve the source MAC address.
	 * @return		the MAC address as a jND_EthernetAddress
	 * @author	Pete Lutz
	 */
	public jND_EthernetAddress srcAddress()  {
		return srcMAC;
	}

	/**
	 * Method to set the source MAC address.
	 * @author	Pete Lutz
	 */
	public void srcAddress(jND_EthernetAddress mac) {
		srcMAC = mac;
	}

	/**
	 * Method to return the length field.
	 * @return		the length as an int.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 length()  {
		return type_len;
	}

	/**
	 * Method to set the length field.
	 * @param		len - the length as an int.
	 * @author	Pete Lutz
	 */
	public void length(jND_BYTE2 len) {
		type_len = len;
	}

	/**
	 * Method to retrieve the payload. This is expressed as
	 * a byte array (byte[]) and is all bytes
	 * following the header to the end of the packet.
	 * @return		the payload
	 * @author	Pete Lutz
	 */
	public byte[] payload()  {
		return pload;
	}

	/**
	 * Method to set the payload. This is expressed as
	 * a byte array (byte[]) and is all bytes
	 * following the header to the end of the packet.
	 * @param		pload - the payload.
	 * @author	Pete Lutz
	 */
	public void payload(byte[] pload) {
		this.pload = pload;
	}
	
	/**
	 * Method to parse a packet. This is a convenience method
	 * equivalent to parse(pkt, 0);
	 * @param		pkt - the packet to parse.
	 * @author	Pete Lutz
	 */
	public void parse(byte[] pkt) {
		parse(pkt, 0);
	}
	
	/**
	 * Method to set the parse a packet.
	 * @param		pkt - the packet to parse.
	 * @param		offset - where to start the parse in the
	 * 						byte array.
	 * @author	Pete Lutz
	 */
	public void parse(byte[] pkt, int offset) {
		try {
			headerOffset = offset;

			ByteArrayInputStream bais = new ByteArrayInputStream(pkt);
			DataInputStream pis = new DataInputStream(bais);
			pis.skipBytes(headerOffset);

			byte[] tmpmac = new byte[jND_EthernetAddress.ENET_ADDR_LEN];
			for(int i = 0; i < jND_EthernetAddress.ENET_ADDR_LEN; i++) {
				tmpmac[i] = pis.readByte();
			}
			dstMAC.address(tmpmac);

			for(int i = 0; i < jND_EthernetAddress.ENET_ADDR_LEN; i++) {
				tmpmac[i] = pis.readByte();
			}
			srcMAC.address(tmpmac);

			short tlno = pis.readShort();
			this.length(new jND_BYTE2(tlno));
			
			payloadOffset = headerOffset + 14;
			pload = new byte[pkt.length - payloadOffset];
			for(int i = 0; i < pload.length; i++)
				pload[i] = pis.readByte();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Ethernet802_3.parse : " + e.toString());
		}
	}

	/**
	 * Method to build a packet for sending from its parts. This
	 * assembles the fields into a byte array according to the
	 * header format.
	 * @return		a byte array which is the packet.
	 * @author	Pete Lutz
	 */
	public byte[] build() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream pos = new DataOutputStream(baos);

			byte[] tmp = this.destAddress().toByteArray();
			for(int i = 0; i < jND_EthernetAddress.ENET_ADDR_LEN; i++)
				if(i < tmp.length)
					pos.writeByte(tmp[i]);
				else
					pos.writeByte(0);

			tmp = this.srcAddress().toByteArray();
			for(int i = 0; i < jND_EthernetAddress.ENET_ADDR_LEN; i++)
				if(i < tmp.length)
					pos.writeByte(tmp[i]);
				else
					pos.writeByte(0);

			pos.writeShort(this.length().toShort());

			if(pload != null)
				for(int i = 0; i < pload.length; i++)
					pos.writeByte(pload[i]);

			pos.close();
			baos.close();
			return baos.toByteArray();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Ethernet802_3.build : " + e.toString());
		}
	}

};
