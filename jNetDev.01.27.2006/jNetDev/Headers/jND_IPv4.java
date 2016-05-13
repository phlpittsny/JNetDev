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
 * jND_IPv4 <br>
 * Class to model an IP header. An IP header has the following fields:
 * <br><br>
 * <UL>
 * <LI>version - the version of IP in use (usually 4, possibly 6)</LI>
 * <LI>headerLength - low 4 bits of 1st byte. # of 4-byte words in the header.
 * </LI>
 * <LI>tos - type of service.</LI>
 * <LI>length - total length of the packet, including this header.</LI>
 * <LI>fragmentID - identifies fragments that belong to one IP datagram.</LI>
 * <LI>flags - Urgent, SYN, ACK, FIN</LI>
 * <LI>fragmentOffset - how far into the packet this fragment starts</LI>
 * <LI>ttl - time to live</LI>
 * <LI>protocol - type of the next header (TCP, UDP, ICMP, etc.)</LI>
 * <LI>headerChecksum - Checksum on the header alone.</LI>
 * <LI>srcAddress - source IP address as a <code>byte</code> array.</LI>
 * <LI>destAddress - destination IP address as a <code>byte</code> array.
 * </LI>
 * <LI>options - IP layer options</LI>
 * <LI>payload</LI>
 * </UL>
 * <pre>
 * 
 *  Header format:
 *      +------------------------------------------------------------------+
 *      | version|header- |       tos      |             length            |
 *      |        | Length |                |                               |
 *      +---4----+---4----+--------8-------+---------------16--------------+
 *      |            fragmentID            | flags|     fragmentOffset     |
 *      +----------------16----------------+---3--+-----------13-----------+
 *      |       ttl       |    protocol    |         headerChecksum        |
 *      +--------8--------+--------8-------+--------------16---------------+
 *      |                             srcAddress                           |
 *      +---------------------------------32-------------------------------+
 *      |                             destAddress                          |
 *      +---------------------------------32-------------------------------+
 *      |          options (if any ... only if IHL &gt; 20 bytes)             |
 *      +------------------------------variable----------------------------+
 *  
 * </pre>
 * NOTES: headerLength is the # of 4-byte words. Multiply this by 4 to get
 * bytes. headerLength includes the header only (up to the end of the options)
 * total length includes the entire datagram from start of the IP header to the
 * end of the payload
 * <br><br>
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
 * The parse method accepts a byte array (jND_ByteArray) and an integer offset
 * into that byte array (defaults to 0) where the header begins. It then
 * separates out the fields into instance variables that can be retrieved with
 * the accessors. parse returns no value.
 * <br><br>
 * The build method requires no parameters. It builds a byte array from the
 * instance variables for the fields and returns that byte array as the result
 * of the call.
 * <br><br>
 * @author Pete Lutz
 */
 

public class jND_IPv4 {
	private int headerOffset;
	private int payloadOffset;
	private jND_BYTE1 versionNo;
	private jND_BYTE1 hdrLenNo;
	private jND_BYTE1 tosNo;
	private jND_BYTE2 lengthNo;
	private jND_BYTE2 fragIDNo;
	private jND_BYTE1 flagsNo;
	private jND_BYTE2 fragOffNo;
	private jND_BYTE1 ttlNo;
	private jND_BYTE1 protocolNo;
	private jND_BYTE2 hdrCkSumNo;
	private jND_IPv4Address srcIP;
	private jND_IPv4Address dstIP;
	private byte[] optArray;
	private byte[] pload;
  
    // Values for protocol
    public static final int P_ICMP = 1;
    public static final int P_TCP = 6;
    public static final int P_UDP = 17;

	/**
	 * Construct an empty IPv4 datagram.
	 * 
	 * @author Pete Lutz
	 */
    public jND_IPv4() {
    	try {
    		headerOffset = -1;
    		payloadOffset = -1;
    		versionNo = new jND_BYTE1(0);
    		hdrLenNo = new jND_BYTE1(0);
    		tosNo = new jND_BYTE1(0);
    		lengthNo = new jND_BYTE2(0);
    		fragIDNo = new jND_BYTE2(0);
    		flagsNo = new jND_BYTE1(0);
    		fragOffNo = new jND_BYTE2(0);
    		ttlNo = new jND_BYTE1(0);
    		protocolNo = new jND_BYTE1(0);
    		hdrCkSumNo = new jND_BYTE2(0);
    		srcIP = new jND_IPv4Address();
    		dstIP = new jND_IPv4Address();
    		optArray = null;
    		pload = null;
    	} catch(jND_Exception nde) {
    		throw nde;
    	} catch (Exception e) {
    		throw new jND_Exception("jND_IPv4.constructor : " + e.toString());
    	}
    }

	/**
	 * Method to retrieve the version number.
	 * 
	 * @return the version number.
	 * @author Pete Lutz
	 */
    public jND_BYTE1 version()  {
    	return versionNo;
    }
    
	/**
	 * Method to set the version number.
	 * 
	 * @param ver -
	 *            the version number.
	 * @author Pete Lutz
	 */
    public void version(jND_BYTE1 ver) {
    	versionNo = new jND_BYTE1(ver.toInt() & 0xf);
    }
	
	/**
	 * Method to retrieve the header length. Note, this is in 32-bit words. To
	 * get octets, multiply by 4.
	 * 
	 * @return the header length.
	 * @author Pete Lutz
	 */
    public jND_BYTE1 headerLength()  {
    	return hdrLenNo;
    }
    
	/**
	 * Method to retrieve the header length. Note, this is in 32-bit words. To
	 * get octets, multiply by 4.
	 * 
	 * @param hdrlen -
	 *            the header length.
	 * @author Pete Lutz
	 */
    public void headerLength(jND_BYTE1 hdrlen) {
    	hdrLenNo = new jND_BYTE1(hdrlen.toInt() & 0xf);
    }

	/**
	 * Method to retrieve the type of service.
	 * 
	 * @return the type of service.
	 * @author Pete Lutz
	 */
    public jND_BYTE1 tos()  {
    	return tosNo;
    }
    
	/**
	 * Method to set the type of service.
	 * 
	 * @param type -
	 *            the type of service.
	 * @author Pete Lutz
	 */
    public void tos(jND_BYTE1 type) {
    	tosNo = type;
    }

	/**
	 * Method to retrieve the total length of the packet. Unlike the
	 * headerLength, this is in octets and need not be scaled.
	 * 
	 * @return the length.
	 * @author Pete Lutz
	 */
    public jND_BYTE2 length()  {
    	return lengthNo;
    }
    
	/**
	 * Method to set the total length of the packet. Unlike the headerLength,
	 * this is in octets and need not be scaled. There is no real reason to set
	 * this, as it is calculated and overidden when the packet is built.
	 * 
	 * @param len -
	 *            the length
	 * @author Pete Lutz
	 */
    public void length( jND_BYTE2 len) {
    	lengthNo = (len);
    }

	/**
	 * Method to retrieve the fragmentID. This is used when a packet needs to be
	 * fragmented. Each fragment has the same ID to help sort them out on
	 * delivery.
	 * 
	 * @return the fragment ID
	 * @author Pete Lutz
	 */
    public jND_BYTE2 fragmentID()  {
    	return fragIDNo;
    }
    
	/**
	 * Method to set the fragmentID. This is used when a packet needs to be
	 * fragmented. Each fragment has the same ID to help sort them out on
	 * delivery.
	 * 
	 * @param id -
	 *            the fragment ID
	 * @author Pete Lutz
	 */
    public void fragmentID(jND_BYTE2 id) {
    	fragIDNo = id;
    }

	/**
	 * Method to retrieve the flags field.
	 * 
	 * @return the flags as a short.
	 * @author Pete Lutz
	 */
    public jND_BYTE1 flags()  {
    	return flagsNo;
    }
    
	/**
	 * Method to set the flags field.
	 * 
	 * @param flagbits -
	 *            the flags.
	 * @author Pete Lutz
	 */
    public void flags(jND_BYTE1 flagbits) {
    	flagsNo = new jND_BYTE1(flagbits.toInt() & 0x7);
    }

	/**
	 * Method to retrieve the fragment offset. This is an absolute offset into
	 * the payload of the IP header of where the payload of this packet should
	 * be placed.
	 * 
	 * @return the offset.
	 * @author Pete Lutz
	 */
    public jND_BYTE2 fragmentOffset()  {
    	return fragOffNo;
    }
    
	/**
	 * Method to set the fragment offset. This is an absolute offset into the
	 * payload of the IP header of where the payload of this packet should be
	 * placed.
	 * 
	 * @param fragOffset -
	 *            the offset.
	 * @author Pete Lutz
	 */
    public void fragmentOffset(jND_BYTE2 fragOffset) {
    	fragOffNo = new jND_BYTE2(fragOffset.toInt() & 0x1fff);
    }

	/**
	 * Method to retrieve the TTL. This is the max. number of hops that this
	 * packet should be forwarded through.
	 * 
	 * @return the TTL.
	 * @author Pete Lutz
	 */
    public jND_BYTE1 ttl()  {
    	return ttlNo;
    }
    
	/**
	 * Method to set the TTL. This is the max. number of hops that this packet
	 * should be forwarded through.
	 * 
	 * @param newttl - the TTL.
	 * @author Pete Lutz
	 */
    public void ttl(jND_BYTE1 newttl) {
    	ttlNo = newttl;
    }

	/**
	 * Method to retrieve the protocol number. This indicates which transport
	 * layer protocol that follows the IP header.
	 * 
	 * @return the protocol number.
	 * @author Pete Lutz
	 */
    public jND_BYTE1 protocol()  {
    	return protocolNo;
    }
    
	/**
	 * Method to set the protocol number. This indicates which transport layer
	 * protocol that follows the IP header.
	 * 
	 * @param protocol -
	 *            the protocol number.
	 * @author Pete Lutz
	 */
    public void protocol(jND_BYTE1 protocol) {
    	protocolNo = protocol;
    }

	/**
	 * Method to retrieve the header checksum.
	 * 
	 * @return the header checksum.
	 * @author Pete Lutz
	 */
    public jND_BYTE2 headerCheckSum()  {
    	return hdrCkSumNo;
    }
    
	/**
	 * Method to set the header checksum. There is no real reason to set this,
	 * as it is calculated when the packet is built and the calculated value
	 * overrides this value.
	 * 
	 * @param newcksum -
	 *            the header checksum.
	 * @author Pete Lutz
	 */
    public void headerCheckSum(jND_BYTE2 newcksum) {
    	hdrCkSumNo = newcksum;
    }

	/**
	 * Method to retrieve the source IP address.
	 * 
	 * @return the source address.
	 * @author Pete Lutz
	 */
    public jND_IPv4Address srcAddress()  {
    	return srcIP;
    }
    
	/**
	 * Method to set the source IP address.
	 * 
	 * @param ip -
	 *            the source address.
	 * @author Pete Lutz
	 */
    public void srcAddress(jND_IPv4Address ip) {
    	srcIP = ip;
    }

	/**
	 * Method to retrieve the destination IP address.
	 * 
	 * @return the destination address.
	 * @author Pete Lutz
	 */
    public jND_IPv4Address destAddress()  {
    	return dstIP;
    }
    
	/**
	 * Method to set the destination IP address.
	 * 
	 * @param ip -
	 *            the destination address.
	 * @author Pete Lutz
	 */
    public void destAddress(jND_IPv4Address ip) {
    	dstIP = ip;
    }

	/**
	 * Method to retrieve the options field. This field contains any IP options
	 * that might accompany this packet.
	 * 
	 * @return the options.
	 * @author Pete Lutz
	 */
    public byte[] options()  {
    	return (byte[])optArray;
    }
    
	/**
	 * Method to set the options field. This field contains any IP options that
	 * might accompany this packet.
	 * 
	 * @param opt -
	 *            the options.
	 * @author Pete Lutz
	 */
    public void options(byte[] opt) {
    	optArray = opt;
    }

	/**
	 * Method to retrieve the payload of the packet.
	 * 
	 * @return the payload.
	 * @author Pete Lutz
	 */
    public byte[] payload()  {
    	return (byte[])pload;
    }
    
	/**
	 * Method to set the payload of the packet.
	 * 
	 * @param pload -
	 *            the payload.
	 * @author Pete Lutz
	 */
    public void payload(byte[] pload) {
    	this.pload = pload;
    }

	/**
	 * Method to parse a packet into its parts. This is a convenience method,
	 * equivalent to parse(pkt, 0);
	 * 
	 * @param pkt -
	 *            the packet to parse
	 * @author Pete Lutz
	 */
    public void parse(byte[] pkt) {
    	parse(pkt, 0);
    }

	/**
	 * Method to parse a packet into its parts
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
    		pis.skipBytes(offset);;

    		int tmp = pis.readByte();
    		versionNo = new jND_BYTE1((tmp >> 4) & 0xf);
    		hdrLenNo = new jND_BYTE1(tmp & 0xf);
    		tosNo = new jND_BYTE1(pis.readByte());
    		lengthNo = new jND_BYTE2(pis.readShort());
    		fragIDNo = new jND_BYTE2(pis.readShort());
    		tmp = pis.readShort();
    		flagsNo = new jND_BYTE1((tmp >> 13) & 0x7);
    		fragOffNo = new jND_BYTE2(tmp & 0x1fff);
    		ttlNo = new jND_BYTE1(pis.readByte());
    		protocolNo = new jND_BYTE1(pis.readByte());
    		hdrCkSumNo = new jND_BYTE2(pis.readShort());

    		byte[] tmpip = new byte[jND_IPv4Address.IP_ADDR_LEN];
    		for(int i = 0; i < jND_IPv4Address.IP_ADDR_LEN; i++) {
    			tmpip[i] = pis.readByte();
    		}
    		srcIP.address(tmpip);

    		for(int i = 0; i < jND_IPv4Address.IP_ADDR_LEN; i++) {
    			tmpip[i] = pis.readByte();
    		}
    		dstIP.address(tmpip);

    		int hlen = hdrLenNo.toInt() * 4; // Header length in bytes
    		int optLen = hlen - 20; // # of bytes of options
    		if(optLen > 0) {
    			optArray = new byte[optLen];
    			for(int i = 0; i < optLen; i++) {
    				optArray[i] = pis.readByte();
    			}
    		}
    		
    		payloadOffset = headerOffset + hlen;
    		pload = new byte[pkt.length - payloadOffset];
    		for(int i = 0; i < pload.length; i++)
    			pload[i] = pis.readByte();
    	} catch(jND_Exception nde) {
    		throw nde;
    	} catch (Exception e) {
    		throw new jND_Exception("jND_IPv4.parse : " + e.toString());
    	}
    }

	/**
	 * Method to build a packet for sending from its parts
	 * 
	 * @return a byte array which is the packet.
	 * @author Pete Lutz
	 */
    public byte[] build() {
    	try {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		DataOutputStream pos = new DataOutputStream(baos);

    		short ver = version().toShort();
    		int hlen = 20;
    		if(options() != null) hlen += options().length;
    		hlen = hlen/4;
    		headerLength(new jND_BYTE1(hlen));
    		pos.writeByte((byte)((ver << 4) | hlen));
    		pos.writeByte(tos().toByte());
    		int len = headerLength().toInt()*4;
    		if(payload() != null) len += payload().length;
    		length(new jND_BYTE2(len));
    		pos.writeShort(length().toShort());
    		pos.writeShort(fragmentID().toShort());
    		short flgs = flags().toByte();
    		int fragoff = fragmentOffset().toShort();
    		pos.writeShort((short)(flgs << 13 | fragoff));
    		pos.writeByte(ttl().toByte());
    		pos.writeByte(protocol().toByte());
    		headerCheckSum(new jND_BYTE2(0));		// recalculated below
    		pos.writeShort(headerCheckSum().toShort());

    		byte[] tmp = srcAddress().toByteArray();
    		for(int i = 0; i < jND_IPv4Address.IP_ADDR_LEN; i++)
    			pos.writeByte(tmp[i]);

    		tmp = destAddress().toByteArray();
    		for(int i = 0; i < jND_IPv4Address.IP_ADDR_LEN; i++)
    			pos.writeByte(tmp[i]);

    		tmp = options();
    		if(tmp != null)
    			for(int i = 0; i < tmp.length; i++)
    				pos.writeByte(tmp[i]);

    		if(pload != null)
    			for(int i = 0; i < pload.length; i++)
    				pos.writeByte(pload[i]);
    		pos.close();
    		baos.close();

    		byte[] packet = baos.toByteArray();
    		int chksum = jND_Utility.checkSum(packet, headerLength().toInt()*4);
    		headerCheckSum(new jND_BYTE2(chksum));
    		packet[10] = (byte)((chksum >> 8) & 0xff);
    		packet[11] = (byte)(chksum & 0xff);

    		return packet;
    	} catch(jND_Exception nde) {
    		throw nde;
    	} catch (Exception e) {
    		throw new jND_Exception("jND_IPv4.build : " + e.toString());
    	}
    }

};
