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
/**
 * Simple PING program
 * Usage: ping IPADDRESS
 * NOTE: Names cannot be used, only IP addresses
 * Sends 4 pings to the destination and awaits replies.
 * Allows 2 seconds for each reply before reporting a failure.
 */

import jNetDev.*;
import jNetDev.Headers.*;
import java.io.*;
import java.util.*;

public class GS2 {
	/** Capture counter **/
	private static int capNo = 0;

	/**
	 * Routine to print a menu of NICs and await a reply
	 */
	private static int chooseNIC() {					
	    // Find out how many NICs are in this machine
	    int numNics = jND_NIC.numberOfNICs(); 			
	    int nicIndex = -1;
	    
	    while(nicIndex < 1 || nicIndex > numNics) { 
	 		// Print out a menu of NICs
		    for(int i = 0; i < numNics; i++) {
		        jND_NIC nic = new jND_NIC(i);           // Instantiate the i-th NIC
		        
		        // NOTE: the description comes back as a String
		        System.out.println("" + (i+1) + ". " + nic.description());
		    }
		    
		    // Ask the user which NIC to use.
		    System.out.print("\nWhich NIC do you wish to use? ");
			
		    // Read the user's choice
		    BufferedReader kbd = new BufferedReader(new InputStreamReader(System.in));
		    String line = null;
		    try {
			line = kbd.readLine();
		    }catch(Exception e) {
			System.out.println("Exception reading choice: " + e.toString());
			System.exit(1);
		    }
		    nicIndex = Integer.parseInt(line);
		}
		return (nicIndex-1);
	}
	
	/** 
	 * Routine to send a single ping and await the response
	 */
	private static String pingOnce(jND_NIC nic, jND_IPv4Address targetip, int ident, int seqno) {
		capNo++;
		// Data to fill up the payload of the PING
		String data = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String pingFilter = "icmp[icmptype] = icmp-echoreply";
		
		// Start building the packet. Since each header becomes the
		// payload of the header that precedes it, it is easiest to
		// do this inside out ... i.e., build the innermost header
		// first.
		jND_ICMP icmp = new jND_ICMP();
		icmp.type(new jND_BYTE1(jND_ICMP.T_PING_REQUEST));	// Set the type field
		icmp.code(new jND_BYTE1(0));				// Code == 0 for a ping
		icmp.identifier(new jND_BYTE2(ident));
		icmp.sequenceNumber(new jND_BYTE2(seqno));
		icmp.payload(data.getBytes());
		byte[] icmpBA = icmp.build();	// Build the packet header
		
		// Now to the IP header
		jND_IPv4 ip = new jND_IPv4();
		ip.version(new jND_BYTE1(4));	// IP v4
		// The tos, fragmentID, fragmentOffset and flags fields
		// all default to the correct values for us (and for most users)
		ip.ttl(new jND_BYTE1(255));							// maxed out ttl
		ip.protocol(new jND_BYTE1(jND_IPv4.P_ICMP));		// ICMP header next
		ip.srcAddress(nic.ipAddress());
		ip.destAddress(targetip);
		ip.payload(icmpBA);
		byte[] ipBA = ip.build();
		
		// Finally the Ethernet header
		jND_EthernetII enet = new jND_EthernetII();
		byte[] dstMAC_BA = jND_Utility.arpFor(nic, targetip.toByteArray());
		if(dstMAC_BA == null || dstMAC_BA.length != jND_EthernetAddress.ENET_ADDR_LEN)
			return "Cannot resolve IP address to a MAC address.";
		jND_EthernetAddress dstMAC = new jND_EthernetAddress(dstMAC_BA);
		enet.destAddress(dstMAC);
		enet.srcAddress(nic.macAddress());
		enet.type(new jND_BYTE2(jND_EthernetII.T_IP));
		enet.payload(ipBA);
		byte[] enetBA = enet.build();
		
		// Set up and start a capture session. This is done BEFORE injecting
		// the ping packet so a race condition does not result is lost packets.
		jND_CaptureSession cap = new jND_CaptureSession(nic, 1550, false, 10);	// Do not need promiscuous mode
		cap.filter(pingFilter, true, nic.netMask());
		cap.start();
		cap.getThread().setName("Capture-" + capNo);
		
		// Now send it:
		nic.inject(enetBA);
		
		// Now ... wait for the reply
		int timeout = 0;
		final int INCREMENT = 100;
		final int PERIOD = 5000;
		jND_PacketQueue pq = cap.packetQueue();
		
		// Timeout after 2 sec (PERIOD)
		while(timeout < PERIOD) {
			if(pq.size() == 0) {
				try {
					Thread.sleep(INCREMENT);
					timeout += INCREMENT;
				} catch(Exception e) {
					System.out.println("pingOnce: Exception sleeping: " + e.toString());
				}
			}
			else {				
				byte[] pkt = pq.pop();
				
				// Because of the filter, we already know this is an ICMP
				// ECHO REPLY. We need to check the destination IP, the
				// identifier, and the sequence number to be sure this is
				// OUR reply
				jND_EthernetII enet1 = new jND_EthernetII();
				enet1.parse(pkt);
				
				// Parse the ethernet payload as an IP header
				jND_IPv4 ip1 = new jND_IPv4();
				ip1.parse(enet1.payload());
				
				// Check that this is destined to me
				// (in case I am in promiscuous mode)
				jND_BYTE4 dstIP = ip1.destAddress().toByte4();
				jND_BYTE4 myIP = nic.ipAddress().toByte4(); 
				if(myIP.toLong() != dstIP.toLong()) continue;
				
				// Parse the ip payload as an ICMP header
				jND_ICMP icmp1 = new jND_ICMP();
				icmp1.parse(ip1.payload());
				
				// Check that this is my identifier and sequence number
				if(icmp1.identifier().toInt() != ident || icmp1.sequenceNumber().toInt() != seqno)
					continue;
					
				// Stop capturing and return result
				cap.stop();
				cap.dispose();
				return "Reply received! Ping #" + icmp1.sequenceNumber() + "\n";
			}
		}
		cap.stop();
		cap.dispose();
		return "Request timed out!";
	}
	
	/**
	 * m a i n - Main program for PING example
	 */ 
	public static void main(String[] args) {
		try {	    
		    // Get set to ping
		    final int NPINGS = 4;
		    int ident = 0;
		
		    // Check # of args
		    if(args.length != 1) {
			System.out.println("Usage: ping IPADDRESS");
			System.exit(1);
		    }
		
		    // Get and parse the ip
		    jND_IPv4Address ipaddr = new jND_IPv4Address(args[0]);
		
		    // Select an identifier for the pings
		    Random rand = new Random(new Date().getTime());
		    ident = rand.nextInt(1024);	// ID in the range 0..1023. This restriction is
							// not required, just chosen to keep things from
							// getting too large
					
		    // Get the NIC to use
		    int nicIndex = chooseNIC();
		    jND_NIC nicToUse = new jND_NIC(nicIndex);   // Instantiate the chosen NIC
		    nicToUse.open();
	    	
		    // Ping the target
		    System.out.println("Pinging " + args[0]);
		    for(int i = 0; i < NPINGS; i++) {
			String result = pingOnce(nicToUse, ipaddr, ident, i);
			System.out.println("  " + result);
		    }
		    nicToUse.close();
		} catch (Exception e) {
			System.out.println("main: Unexpected exception: " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}
};
