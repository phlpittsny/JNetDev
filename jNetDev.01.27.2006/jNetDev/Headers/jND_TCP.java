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
 * jND_TCP <br>
 * Class to model a TCP header. Fields are:
 * <p>
 * <UL><LI>sourcePort - port number of the sender.
 * </LI><LI>destPort - port number of the intended receiver.
 * </LI><LI>sequenceNumber - sequence # for this packet.
 * </LI><LI>ackNumber - ACK #
 * </LI><LI>offset - where the data starts relative to the start of the header
 * </LI><LI>reserved
 * </LI><LI>ecn
 * </LI><LI>flags - flags for the packet (SYN, FIN, ACK, etc)
 * </LI><LI>window - window size for TCP
 * </LI><LI>checkSum
 * </LI><LI>urgent - pointer to urgent data
 * </LI><LI>options
 * </LI><LI>payload
 * </LI></UL>
 * The payload of the packet is NOT, strictly speaking, part of the header, but is a
 * sequence of bytes following the header. 
 * <pre>
 * Header format:
 *    +---------------------------------+--------------------------------+
 *    |            sourcePort           |           destPort             |
 *    +----------------16---------------+---------------16---------------+
 *    |                          sequenceNumber                          |
 *    +--------------------------------32--------------------------------+
 *    |                             ackNumber                            |
 *    +--------------------------------32--------------------------------+
 *    | offset |reserved|ecn|   flags   |             window             +
 *    +----4---+---4----+-2-+-----6-----+---------------16---------------+
 *    |             checkSum            |             urgent             |
 *    +-----------------16--------------+---------------16---------------+
 *    |                      options                  |      padding     |
 *    +-----------------------------variable-----------------------------+   
 * </pre>
 * @author Pete Lutz
 */

public class jND_TCP {
	private int headerOffset;
	int payloadOffset;
	private jND_BYTE2 srcPortNo;
	private jND_BYTE2 dstPortNo;
	private jND_BYTE4 seqNumNo;
	private jND_BYTE4 ackNumNo;
	private jND_BYTE1 offsetNo;
	private jND_BYTE1 reservedNo;
	private jND_BYTE1 ecnNo;
	private jND_BYTE1 flagsNo;
	private jND_BYTE2 windowNo;
	private jND_BYTE2 checkSumNo;
	private jND_BYTE2 urgentNo;
	private byte[] opts;
	private byte[] pload;

	// Needed to calculate the checksum
	private jND_IPv4Address srcIP;	// Network layer source address
	private jND_IPv4Address dstIP;	// Network layer destination address

	/**
	 * Construct a TCP header.
	 * @author	Pete Lutz
	 */
	public jND_TCP() {
		try {
			headerOffset = -1;
			payloadOffset = -1;
			srcPortNo = new jND_BYTE2(0);
			dstPortNo = new jND_BYTE2(0);
			seqNumNo = new jND_BYTE4(0);
			ackNumNo = new jND_BYTE4(0);
			offsetNo = new jND_BYTE1(0);
			reservedNo = new jND_BYTE1(0);
			ecnNo = new jND_BYTE1(0);
			flagsNo = new jND_BYTE1(0);
			windowNo = new jND_BYTE2(0);
			checkSumNo = new jND_BYTE2(0);
			urgentNo = new jND_BYTE2(0);
			opts = null;
			pload = null;
			
			srcIP = new jND_IPv4Address();
			dstIP = new jND_IPv4Address();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_TCP.constructor : " + e.toString());
		}
	}

	/**
	 * Method to retrieve the source port of the header.
	 * @return		the source port number.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 sourcePort()  {
		return srcPortNo;
	}
	/**
	 * Method to set the source port of the header.
	 * @param		port - the source port number.
	 * @author	Pete Lutz
	 */
	public void sourcePort(jND_BYTE2 port) {
		srcPortNo = port;
	}
	
	/**
	 * Method to retrieve the destination port of the header.
	 * @return		the destination port number.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 destPort()  {
		return dstPortNo;
	}
	/**
	 * Method to set the destination port of the header.
	 * @param		port - the destination port number.
	 * @author	Pete Lutz
	 */
	public void destPort(jND_BYTE2 port) {
		dstPortNo = port;
	}

	/**
	 * Method to retrieve the sequence number of the header.
	 * @return		the sequence number
	 * @author	Pete Lutz
	 */
	public jND_BYTE4 sequenceNumber()  {
		return seqNumNo;
	}
	/**
	 * Method to set the sequence number of the header.
	 * @param		seqNo - the sequence number.
	 * @author	Pete Lutz
	 */
	public void sequenceNumber(jND_BYTE4 seqNo) {
		seqNumNo = seqNo;
	}

	/**
	 * Method to retrieve the acknowledgement number of the header.
	 * @return		the acknowledgement number.
	 * @author	Pete Lutz
	 */
	public jND_BYTE4 ackNumber()  {
		return ackNumNo;
	}
	/**
	 * Method to set the acknowledgement number of the header.
	 * @param		port - the acknowledgement number.
	 * @author	Pete Lutz
	 */
	public void ackNumber(jND_BYTE4 port) {
		ackNumNo = port;
	}

	/**
	 * Method to retrieve the data offset of the header.
	 * @return		the data offset.
	 * @author	Pete Lutz
	 */
	public jND_BYTE1 offset()  {
		return offsetNo;
	}
	
	/**
	 * Method to set the data offset of the header. There is no actual reason
	 * to set the offset, as it is calculated when the packet is built. The
	 * calculated value overrides any value set via this method.
	 * @param		offset - the data offset.
	 * @author	Pete Lutz
	 */
	public void offset(jND_BYTE1 offset) {
		offsetNo = offset;
	}

	/**
	 * Method to retrieve the reserved field of the header.
	 * @return		the reserved field.
	 * @author	Pete Lutz
	 */
	public jND_BYTE1 reserved()  {
		return reservedNo;
	}
	
	/**
	 * Method to set the reserved field of the header.
	 * @param		reserved - the reserved feild.
	 * @author	Pete Lutz
	 */
	public void reserved(jND_BYTE1 reserved) {
		reservedNo = reserved;
	}

	/**
	 * Method to retrieve the ecn field of the header.
	 * @return		the ecn field.
	 * @author	Pete Lutz
	 */
	public jND_BYTE1 ecn()  {
		return ecnNo;
	}
	
	/**
	 * Method to set the ecn field of the header.
	 * @param		ecn - the ecn field.
	 * @author	Pete Lutz
	 */
	public void ecn(jND_BYTE1 ecn) {
		ecnNo = ecn;
	}

	/**
	 * Method to retrieve the flags field of the header.
	 * @return		the flags field.
	 * @author	Pete Lutz
	 */
	public jND_BYTE1 flags()  {
		return flagsNo;
	}
	
	/**
	 * Method to set the flags field of the header.
	 * @param		flags - the flags field.
	 * @author	Pete Lutz
	 */
	public void flags(jND_BYTE1 flags) {
		flagsNo = flags;
	}

	/**
	 * Method to retrieve the window size of the header.
	 * @return		the window size.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 window()  {
		return windowNo;
	}
	
	/**
	 * Method to set the window size of the header.
	 * @param		wsize - the window size.
	 * @author	Pete Lutz
	 */
	public void window(jND_BYTE2 wsize) {
		windowNo = wsize;
	}

	/**
	 * Method to retrieve the check sum of the header.
	 * @return		the check sum.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 checkSum()  {
		return checkSumNo;
	}
	
	/**
	 * Method to set the check sum of the header. There is no actual reason
	 * to set the checksum, as it is calculated when the packet is built. The
	 * calculated value overrides any value set via this method.
	 * @param		checksum - the check sum
	 * @author	Pete Lutz
	 */
	void checkSum(jND_BYTE2 checksum) {
		checkSumNo = checksum;
	}

	/**
	 * Method to retrieve the urgent pointer field of the header.
	 * @return		the urgent pointer field.
	 * @author	Pete Lutz
	 */
	public jND_BYTE2 urgent()  {
		return urgentNo;
	}
	/**
	 * Method to set the urgent pointer field of the header.
	 * @param		urgent - the urgent pointer field.
	 * @author	Pete Lutz
	 */
	public void urgent(jND_BYTE2 urgent) {
		urgentNo = urgent;
	}

	/**
	 * Method to retrieve the options field of the header.
	 * @return		the options field.
	 * @author	Pete Lutz
	 */
	public byte[] options()  {
		return (byte[])opts;
	}
	/**
	 * Method to set the options field of the header.
	 * @param		opt - the options field
	 * @author	Pete Lutz
	 */
	public void options(byte[] opt) {
		opts = opt;
	}

	/**
	 * Method to retrieve the payload of the packet.
	 * @return		the payload.
	 * @author	Pete Lutz
	 */
	public byte[] payload()  {
		return (byte[])pload;
	}
	/**
	 * Method to set the payload of the packet.
	 * @param		pload - the payload.
	 * @author	Pete Lutz
	 */
	public void payload(byte[] pload) {
		this.pload = pload;
	}

	/**
	 * Method to retrieve the source IP address. The parse() method
	 * does NOT set this, as it is not actually in the TCP header.
	 * To retrieve this value, you should use the appropriate accessor
	 * of the IPv4 header class.
	 * 
	 * @return		the source IP address
	 * @author	Pete Lutz
	 */
	public jND_IPv4Address sourceIP()  {
		return srcIP;
	}
	
	/**
	 * Method to set the source IP address. This is not actually
	 * included in the TCP header. However, it must be provided so
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
	 * does NOT set this, as it is not actually in the TCP header.
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
	 * included in the TCP header. However, it must be provided so
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
			seqNumNo = new jND_BYTE4(pis.readInt());
			ackNumNo = new jND_BYTE4(pis.readInt());

			short tmp = pis.readByte();
			offsetNo = new jND_BYTE1((tmp >> 4) & 0xf);
			reservedNo = new jND_BYTE1(tmp & 0xf);

			tmp = pis.readByte();
			ecnNo = new jND_BYTE1((tmp >> 6) & 0x3);
			flagsNo = new jND_BYTE1(tmp & 0x3f);

			windowNo = new jND_BYTE2(pis.readShort());
			checkSumNo = new jND_BYTE2(pis.readShort());
			urgentNo = new jND_BYTE2(pis.readShort());

			int hdrlength = offsetNo.toInt() * 4;
			if(hdrlength > 20) {
				int optlen = hdrlength - 20;
				opts = new byte[optlen];
				for(int i = 0; i < optlen; i++)
					opts[i] = pis.readByte();
			}
			
			payloadOffset = headerOffset + hdrlength;
			pload = new byte[pkt.length - payloadOffset];
			for(int i = 0; i < pload.length; i++)
				pload[i] = pis.readByte();
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_TCP.parse : " + e.toString());
		}
	}

	/**
	 * Method to build a packet for sending from its parts.
	 * @return		a byte array which is the packet.
	 * @author	Pete Lutz
	 */
	public byte[] build() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream pos = new DataOutputStream(baos);
			pos.writeShort(srcPortNo.toShort());
			pos.writeShort(dstPortNo.toShort());
			pos.writeInt(seqNumNo.toInt());
			pos.writeInt(ackNumNo.toInt());

			offsetNo = new jND_BYTE1(20 + opts.length + (4 - opts.length % 4));
			byte tmp = (byte)(((offsetNo.toInt() & 0xf) << 4) 
					| (reservedNo.toInt() & 0xf));
			pos.writeByte(tmp);

			tmp = (byte)(((ecnNo.toInt() & 0x3) << 6) | (flagsNo.toInt() & 0x3f));
			pos.writeByte(tmp);

			pos.writeShort(windowNo.toShort());
			pos.writeShort(0);			// Dummy checksum - recalculated below
			pos.writeShort(urgentNo.toShort());

			if(opts != null) {
				int optlen = opts.length;
				for(int i = 0; i < optlen; i++)
					pos.writeByte(opts[i]);
				while(optlen % 4 != 0) {
					pos.writeByte(0);
					optlen++;
				}
			}

			if(pload != null)
				for(int i = 0; i < pload.length; i++)
					pos.writeByte(pload[i]);
			pos.close();
			baos.close();

			// Calculate and insert the checksum
			byte[] ary = baos.toByteArray();
			checkSumNo = new jND_BYTE2(
					jND_Utility.transportCheckSum(jND_IPv4.P_TCP, srcIP, dstIP, ary));
			byte msb = (byte) ((checkSumNo.toInt() >> 8) & 0xff);
			byte lsb = (byte) (checkSumNo.toInt() & 0xff); 
			ary[16] = msb;
			ary[17] = lsb;

			return ary;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_TCP.build : " + e.toString());
		}
	}

};

