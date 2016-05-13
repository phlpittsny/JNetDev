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
 * jND_ARP <br>
 * Class to model an ARP header. This is ALSO used for RARP headers. It includes
 * the following fields:
 * <br><br>
 * <UL>
 * <LI>hwAddressType - 1 for ethernet, 6 for IEEE 802, etc.</LI>
 * <LI>protocolAddressType - 2048 for IP, 2053 for X.25, etc.</LI>
 * <LI>hwAddressLength - # of bytes in a hardware address</LI>
 * <LI>protocolAddressLength - # of bytes in a protocol address</LI>
 * <LI>opcode - 1 for ARP request, 2 for ARP reply, 3 for RARP request, 4 for
 * RARP reply</LI>
 * <LI>sourceHWAddress - hardware address of the sender of this packet</LI>
 * <LI>sourceProtocolAddress - protocol address of the sender of this packet
 * </LI>
 * <LI>targetHWAddress - hardware address of client (RARP) or returned to
 * client (ARP)</LI>
 * <LI>targetProtocolAddress - protocol address of the peer (ARP) or the client
 * (RARP)</LI>
 * <LI>payload - any data following this header ... normally none</LI>
 * </UL>
 * <pre>
 * 
 *  Header format:
 *     +--------------------------------+--------------------------------+
 *     |          hwAddressType         |      protocolAddressType       |
 *     +----------------16--------------+---------------16---------------+
 *     | hwAddressLength|protocol-      |             opcode             |
 *     |                |  AddressLength|                                |
 *     +--------8-------+--------8------+---------------16---------------+
 *     |                     sourceHWAddress (bytes 0-3)                 |
 *     +--------------------------------32-------------------------------+
 *     | sourceHWAddress (bytes 4-5)    |sourceProtocolAddress(bytes 0-1)|
 *     +----------------16--------------+---------------16---------------+
 *     |sourceProtocolAddress(bytes 2-3)|  targetHWAddress (bytes 0-1)   |
 *     +----------------16--------------+---------------16---------------+
 *     |                 targetHWAddress (bytes 2-5)                     |
 *     +--------------------------------32-------------------------------+
 *     |              targetProtocolAddress (bytes 0-3)                  |
 *     +--------------------------------32-------------------------------+
 *  
 * </pre>
 * As with all of the header modeling classes, this one contains accessors and
 * mutators for each field, plus the two key methods: parse and build.
 * <br><br>
 * Accessors are methods named for each field which return the value of that
 * field in the header being manipulated. Accessors take no parameters, they
 * allow the user to "access" a field.
 * <br><br>
 * Mutators return no value. Instead, they take a parameter which is the new
 * value of a field in the header. Mutators allow the user to change ("mutate")
 * a field.
 * <br><br>
 * The parse method accepts a byte array (byte[]) and an integer offset into
 * that byte array (defaults to 0) where the header begins. It then separates
 * out the fields into instance variables that can be retrieved with the
 * accessors. parse returns no value.
 * <br><br>
 * The build method requires no parameters. It builds a byte array from the
 * instance variables for the fields and returns that byte array as the result
 * of the call.
 */

public class jND_ARP {
	private int headerOffset;
	private int payloadOffset;
	private jND_BYTE2 hwAddrTypeNo;
	private jND_BYTE2 protAddrTypeNo;
	private jND_BYTE1 hwLenNo;
	private jND_BYTE1 protLenNo;
	private jND_BYTE2 opcodeNo;
	private byte[] srcHWAddr;
	private byte[] srcProtAddr;
	private byte[] targetHWAddr;
	private byte[] targetProtAddr;
	private byte[] pload;

	// Opcode values
	public static int ARP_REQUEST = 1;
	public static int ARP_REPLY = 2;
	public static int RARP_REQUEST = 3;
	public static int RARP_REPLY = 4;
	public static int HEADER_LEN = 28;
	public static short HW_MAX_LEN = 6;
	public static short PROT_MAX_LEN = 4;

	/**
	 * Method to build an empty ARP packet with default field values
	 * 
	 * @author Pete Lutz
	 */
	public jND_ARP() {
		try {
			headerOffset = 0;
			payloadOffset = 0;

			hwAddrTypeNo = new jND_BYTE2();
			protAddrTypeNo = new jND_BYTE2();
			hwLenNo = new jND_BYTE1();
			protLenNo = new jND_BYTE1();
			opcodeNo = new jND_BYTE2();

			srcHWAddr = new byte[]{ 0, 0, 0, 0, 0, 0 };
			srcProtAddr = new byte[]{ 0, 0, 0, 0 };
			targetHWAddr = new byte[]{ 0, 0, 0, 0, 0, 0 };
			targetProtAddr = new byte[]{ 0, 0, 0, 0 };
			pload = null;
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_ARP.constructor : " + e.toString());
		}
	}

	/**
	 * Method to get the hardware address type (01 - Ethernet).
	 * 
	 * @return the address type.
	 * @author Pete Lutz
	 */
	public jND_BYTE2 hwAddressType() {
		return hwAddrTypeNo;
	}

	/**
	 * Method to set the hardware address type (01 - Ethernet).
	 * 
	 * @param hwType
	 *            -the address type.
	 * @author Pete Lutz
	 */
	public void hwAddressType(jND_BYTE2 hwType) {
		hwAddrTypeNo = hwType;
	}

	/**
	 * Method to get the protocol address type (0x800 = IP).
	 * 
	 * @return the address type.
	 * @author Pete Lutz
	 */
	public jND_BYTE2 protocolAddressType() {
		return protAddrTypeNo;
	}

	/**
	 * Method to set the protocol address type (0x800 = IP).
	 * 
	 * @param protType -
	 *            the address type.
	 * @author Pete Lutz
	 */
	public void protocolAddressType(jND_BYTE2 protType) {
		protAddrTypeNo = protType;
	}

	/**
	 * Method to get the hardware address length.
	 * 
	 * @return the hardware address length.
	 * @author Pete Lutz
	 */
	public jND_BYTE1 hwAddressLength() {
		return hwLenNo;
	}

	/**
	 * Method to set the hardware address length.
	 * 
	 * @param hwLen
	 *            -the hardware address length.
	 * @author Pete Lutz
	 */
	public void hwAddressLength(jND_BYTE1 hwLen) {
		hwLenNo = hwLen;
	}

	/**
	 * Method to get the protocol address length.
	 * 
	 * @return the protocol address length.
	 * @author Pete Lutz
	 */
	public jND_BYTE1 protocolAddressLength() {
		return protLenNo;
	}

	/**
	 * Method to set the protocol address length.
	 * 
	 * @param protLen
	 *            -the protocol address length.
	 * @author Pete Lutz
	 */
	public void protocolAddressLength(jND_BYTE1 protLen) {
		protLenNo = protLen;
	}

	/**
	 * Method to get the opcode.
	 * 
	 * @return the opcode.
	 * @author Pete Lutz
	 */
	public jND_BYTE2 opcode() {
		return opcodeNo;
	}

	/**
	 * Method to set the opcode.
	 * 
	 * @param opcode
	 *            -the opcode.
	 * @author Pete Lutz
	 */
	public void opcode(jND_BYTE2 opcode) {
		opcodeNo = opcode;
	}

	/**
	 * Method to get the source hardware address
	 * 
	 * @return the source hardware address
	 * @author Pete Lutz
	 */
	public byte[] sourceHWAddress() {
		return (byte[]) srcHWAddr;
	}

	/**
	 * Method to set the source hardware address
	 * 
	 * @param hwaddr -
	 *            the source hardware address
	 * @author Pete Lutz
	 */
	public void sourceHWAddress(byte[] hwaddr) {
		srcHWAddr = hwaddr;
	}

	/**
	 * Method to get the source protocol address
	 * 
	 * @return the source protocol address
	 * @author Pete Lutz
	 */
	public byte[] sourceProtocolAddress() {
		return srcProtAddr;
	}

	/**
	 * Method to set the source protocol address
	 * 
	 * @param protaddr -
	 *            the source protocol address
	 * @author Pete Lutz
	 */
	public void sourceProtocolAddress(byte[] protaddr) {
		srcProtAddr = protaddr;
	}

	/**
	 * Method to get the target hardware address
	 * 
	 * @return the target hardware address
	 * @author Pete Lutz
	 */
	public byte[] targetHWAddress() {
		return targetHWAddr;
	}

	/**
	 * Method to set the target hardware address
	 * 
	 * @param hwaddr -
	 *            the target hardware address
	 * @author Pete Lutz
	 */
	public void targetHWAddress(byte[] hwaddr) {
		targetHWAddr = hwaddr;
	}

	/**
	 * Method to get the target protocol address
	 * 
	 * @return the target protocol address
	 * @author Pete Lutz
	 */
	public byte[] targetProtocolAddress() {
		return targetProtAddr;
	}

	/**
	 * Method to set the target protocol address
	 * 
	 * @param protaddr -
	 *            the target protocol address
	 * @author Pete Lutz
	 */
	public void targetProtocolAddress(byte[] protaddr) {
		targetProtAddr = protaddr;
	}

	/**
	 * Method to get the payload. This is any bytes following the header.
	 * 
	 * @return the payload
	 * @author Pete Lutz
	 */
	public byte[] payload() {
		return pload;
	}

	/**
	 * Method to set the payload. This is any bytes following the header.
	 * 
	 * @param pay -
	 *            the payload
	 * @author Pete Lutz
	 */
	public void payload(byte[] pay) {
		pload = pay;
	}

	/**
	 * Method to parse a raw packet into its parts. This convenience method is
	 * the same as parse(p, 0)
	 * 
	 * @param pkt -
	 *            the packet to parse
	 * @author Pete Lutz
	 */
	public void parse(byte[] pkt) {
		parse(pkt, 0);
	}

	/**
	 * Method to parse a raw packet into its parts. Normally one does this then
	 * uses the accessors for each field to interrogate the fields.
	 * 
	 * @param pkt -
	 *            the packet to parse
	 * @param offset -
	 *            where in the packet to start parsing
	 * @author Pete Lutz
	 */
	public void parse(byte[] pkt, int offset) {
		try {
			headerOffset = offset;

			ByteArrayInputStream bais = new ByteArrayInputStream(pkt);
			DataInputStream pis = new DataInputStream(bais);
			pis.skipBytes(offset);

			hwAddrTypeNo = new jND_BYTE2(pis.readShort());
			protAddrTypeNo = new jND_BYTE2(pis.readShort());
			hwLenNo = new jND_BYTE1(pis.readByte());

			short effHWLen = hwLenNo.toShort();
			if (effHWLen < 0 || effHWLen > HW_MAX_LEN)
				effHWLen = HW_MAX_LEN;

			protLenNo = new jND_BYTE1(pis.readByte());

			short effProtLen = protLenNo.toShort();
			if (effProtLen < 0 || effProtLen > PROT_MAX_LEN)
				effProtLen = PROT_MAX_LEN;

			opcodeNo = new jND_BYTE2(pis.readShort());

			srcHWAddr = new byte[effHWLen];
			for (int i = 0; i < HW_MAX_LEN; i++) {
				short tmp = pis.readByte();
				if (i < effHWLen)
					srcHWAddr[i] = new jND_BYTE1(tmp).toByte();
			}

			srcProtAddr = new byte[effProtLen];
			for (int i = 0; i < PROT_MAX_LEN; i++) {
				short tmp = pis.readByte();
				if (i < effProtLen)
					srcProtAddr[i] = new jND_BYTE1(tmp).toByte();
			}

			targetHWAddr = new byte[effHWLen];
			for (int i = 0; i < HW_MAX_LEN; i++) {
				short tmp = pis.readByte();
				if (i < effHWLen)
					targetHWAddr[i] = new jND_BYTE1(tmp).toByte();
			}

			targetProtAddr = new byte[effProtLen];
			for (int i = 0; i < PROT_MAX_LEN; i++) {
				short tmp = pis.readByte();
				if (i < effProtLen)
					targetProtAddr[i] = new jND_BYTE1(tmp).toByte();
			}

			payloadOffset = headerOffset + 28;
			pload = new byte[pkt.length - payloadOffset];
			for (int i = 0; i < pload.length; i++)
				pload[i] = pis.readByte();
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_ARP.parse : " + e.toString());
		}
	}

	/**
	 * Method to build a packet for sending. Normally one declares an empty
	 * header and sets the fields using the mutator. Then, calling build()
	 * returns a byte array which can be injected into the network.
	 * 
	 * @return a byte array which is the packet.
	 * @author Pete Lutz
	 */
	public byte[] build() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream pos = new DataOutputStream(baos);
			
			pos.writeShort(hwAddrTypeNo.toShort());
			pos.writeShort(protAddrTypeNo.toShort());
			pos.writeByte(hwLenNo.toByte());
			pos.writeByte(protLenNo.toByte());
			pos.writeShort(opcodeNo.toShort());

			for (int i = 0; i < HW_MAX_LEN; i++) {
				byte byte1 = 0;
				if (i < srcHWAddr.length)
					byte1 = srcHWAddr[i];
				pos.writeByte(byte1);
			}

			for (int i = 0; i < PROT_MAX_LEN; i++) {
				byte byte1 = 0;
				if (i < srcProtAddr.length)
					byte1 = srcProtAddr[i];
				pos.writeByte(byte1);
			}

			for (int i = 0; i < HW_MAX_LEN; i++) {
				byte byte1 = 0;
				if (i < targetHWAddr.length)
					byte1 = targetHWAddr[i];
				pos.writeByte(byte1);
			}

			for (int i = 0; i < PROT_MAX_LEN; i++) {
				byte byte1 = 0;
				if (i < targetProtAddr.length)
					byte1 = targetProtAddr[i];
				pos.writeByte(byte1);
			}

			if(pload != null)
				for (int i = 0; i < pload.length; i++)
					pos.writeByte(pload[i]);

			pos.close();
			baos.close();
			return baos.toByteArray();
		} catch (jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_ARP.build : " + e.toString());
		}
	}

};
