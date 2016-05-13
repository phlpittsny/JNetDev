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
 * jND_UDP <br>
 * Class to model a UDP header. A UDP header has the following fields:
 * <p>
 * <UL><LI>sourcePort - the port # of the sender.
 * </LI><LI>destPort - the port # of the intended receiver.
 * </LI><LI>dataLength - the length of the payload.
 * </LI><LI>checkSum
 * </LI></UL>
 * The payload of the packet is NOT, strictly speaking, part of the header, but is a
 * sequence of bytes following the header. 
 * <pre>
 * Header format:
 *    +--------------------------------+--------------------------------+
 *    |             sourcePort         |            destPort            |
 *    +----------------16--------------+---------------16---------------+
 *    |            dataLength          |            checkSum            |
 *    +----------------16--------------+---------------16---------------+
 * </pre>
 * @author Pete Lutz
 */

public class jND_UDP {
	private int headerOffset;
	private int payloadOffset;
	private jND_BYTE2 srcPortNo;
	private jND_BYTE2 dstPortNo;
	private jND_BYTE2 dataLengthNo;
	private jND_BYTE2 checkSumNo;
	private byte[] pload;

	// Needed to calculate the checksum
	private jND_IPv4Address srcIP;	// Network layer source address
	private jND_IPv4Address dstIP;	// Network layer dest address

	/**
	 * Construct an empty UDP header.
	 * @author	Pete Lutz
	 */
	public jND_UDP() {
		try {
			headerOffset = -1;
			payloadOffset = -1;
			srcPortNo = new jND_BYTE2(0);
			dstPortNo = new jND_BYTE2(0);
			dataLengthNo = new jND_BYTE2(0);
			checkSumNo = new jND_BYTE2(0);
			pload = null;
			srcIP = new jND_IPv4Address();
			dstIP = new jND_IPv4Address();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_UDP.constructor : " + e.toString());
		}
	}

	/**
	 * Method to retrieve the source port number.
	 * @return		the source port.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 sourcePort()  {
		return srcPortNo;
	}
	
	/**
	 * Method to set the source port number.
	 * @param		port - the source port.
	 * @author	Pete Lutz
	 */
	public void sourcePort(jND_BYTE2 port) {
		srcPortNo = port;
	}
	
	/**
	 * Method to retrieve the destination port number.
	 * @return		the destination port.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 destPort()  {
		return dstPortNo;
	}
	
	/**
	 * Method to set the destination port number.
	 * @param		port - the destination port.
	 * @author	Pete Lutz
	 */
	public void destPort(jND_BYTE2 port) {
		dstPortNo = port;
	}

	/**
	 * Method to retrieve the data length of the header.
	 * @return		the data length.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 dataLength()  {
		return dataLengthNo;
	}
	
	/**
	 * Method to set the data length of the packet. There is no actual
	 * reason to set this, as it is calculated when the packet is built
	 * and the calculated value overrides whatever is set via this method.
	 * @param		len - the data length
	 * @author	Pete Lutz
	 */
	public void dataLength(jND_BYTE2 len) {
		dataLengthNo = len;
	}

	/**
	 * Method to retrieve the checksum.
	 * @return		the checksum.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 checkSum()  {
		return checkSumNo;
	}
	
	/**
	 * Method to set the checksum. There is no actual reason
	 * to set this, as it is calculated when the packet is built
	 * and the calculated value overrides whatever is set via this method.
	 * @param		checksum - the checksum
	 * @author	Pete Lutz
	 */
	public void checkSum(jND_BYTE2 checksum) {
		checkSumNo = checksum;
	}

	/**
	 * Method to retrieve the payload of the packet.
	 * @return		the payload.
	 * @author	Pete Lutz
	 */
	public byte[] payload()  {
		return pload;
	}
	
	/**
	 * Method to set the payload of the packet.
	 * @author	Pete Lutz
	 */
	public void payload(byte[] pload) {
		this.pload = pload;
	}

	/**
	 * Method to retrieve the source IP address. The parse() method
	 * does NOT set this, as it is not actually in the UDP header.
	 * To retrieve this value, you should use the appropriate accessor
	 * of the IPv4 header class.
	 * 
	 * @return		the source IP address
	 * @author	Pete Lutz
	 */
	public jND_IPv4Address sourceIP()  {
		return (jND_IPv4Address)srcIP;
	}
	
	/**
	 * Method to set the source IP address. This is not actually
	 * included in the UDP header. However, it must be provided so
	 * that the build() method can calculate the checksum. The checksum
	 * for transport layer headers includes a pseudo header with the
	 * source and destination IP addresses in it.
	 * 
	 * @param		srcIP - the source IP address
	 * @author	Pete Lutz
	 */
	public void sourceIP(jND_IPv4Address srcIP) {
		this.srcIP = srcIP;
	}

	/**
	 * Method to retrieve the destination IP address. The parse() method
	 * does NOT set this, as it is not actually in the UDP header.
	 * To retrieve this value, you should use the appropriate accessor
	 * of the IPv4 header class.
	 * 
	 * @return		the destination IP address
	 * @author	Pete Lutz
	 */
	public jND_IPv4Address destinationIP()  {
		return (jND_IPv4Address)dstIP;
	}
	
	/**
	 * Method to set the destination IP address. This is not actually
	 * included in the UDP header. However, it must be provided so
	 * that the build() method can calculate the checksum. The checksum
	 * for transport layer headers includes a pseudo header with the
	 * source and destination IP addresses in it.
	 * 
	 * @param		destIP - the destination IP address
	 * @author	Pete Lutz
	 */
	public void destinationIP(jND_IPv4Address destIP) {
		this.dstIP = destIP;
	}

	/**
	 * Method to parse a raw packet into its parts. This is a convenience
	 * method equivalent to parse(pkt, 0);
	 * @param		pkt - the packet to parse.
	 * @author	Pete Lutz
	 */
	public void parse(byte[] pkt) {
		parse(pkt, 0);
	}

	/**
	 * Method to parse a raw packet into its parts.
	 * @param		pkt - the packet to parse.
	 * @param		offset - where in the packet to start parsing.
	 * @author	Pete Lutz
	 */
	public void parse(byte[] pkt, int offset) {
		try {
			headerOffset = offset;

			ByteArrayInputStream bais = new ByteArrayInputStream(pkt);
			DataInputStream pis = new DataInputStream(bais);
			pis.skipBytes(headerOffset);

			srcPortNo = new jND_BYTE2(pis.readShort());
			dstPortNo = new jND_BYTE2(pis.readShort());
			dataLengthNo = new jND_BYTE2(pis.readShort());
			checkSumNo = new jND_BYTE2(pis.readShort());
			
			payloadOffset = headerOffset + 8;
			pload = new byte[pkt.length - payloadOffset];
			for(int i = 0; i < pload.length; i++)
				pload[i] = pis.readByte();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_UDP.parse : " + e.toString());
		}
	}

	/**
	 * Method to build a packet for sending, from its parts.
	 * @return		a byte array which is the packet.
	 * @author	Pete Lutz
	 */
	public byte[] build() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream pos = new DataOutputStream(baos);
			
			pos.writeShort(srcPortNo.toShort());
			pos.writeShort(dstPortNo.toShort());
			dataLengthNo = new jND_BYTE2(pload.length + 8);
			pos.writeShort(dataLengthNo.toShort());	// datalength
			pos.writeShort(0);					// dummy checksum

			if(pload != null)
				for(int i = 0; i < pload.length; i++)	// payload
					pos.writeByte(pload[i]);
			
			pos.close();
			baos.close();

			// Calculate and insert the checksum
			byte[] ary = baos.toByteArray();
			checkSumNo = new jND_BYTE2(
				jND_Utility.transportCheckSum(jND_IPv4.P_UDP, srcIP, dstIP, ary));
			byte msb = (byte) (((checkSumNo.toInt() >> 8) & 0xff));
			byte lsb = (byte) (byte)(checkSumNo.toInt() & 0xff); 
			ary[6] = msb;
			ary[7] = lsb;

			return ary;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_UDP.build : " + e.toString());
		}
	}

};

