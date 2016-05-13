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
 * Getting Started Example 1<br>
 * Capturing packets.<br>
 * This is a program to listen on a NIC for all ARP replies and report the
 * source protocol and mac addresses and the target protocol and mac
 * addresses
 */

import jNetDev.*;
import jNetDev.Headers.*;
import java.io.*;

public class GS1 {
	/**
	 * reportArps - Listen for and report arp replies.
	 * @param cap - the capture session to communicate with
	 */
	private static void reportArps(jND_CaptureSession cap) { 
	    try {
	    	jND_PacketQueue queue = cap.packetQueue();     // get the packet queue
		    while(true) {
		        // The next line pops one packet off the front of the queue and
		        // returns it. If the queue is empty, this is a BLOCKING call and
		        // your thread will wait until a packet arrives. If this is NOT 
		        // what you want in the future, you can interrogate the size 
		        // method of the queue to see if it is > 0.
		        byte[] pkt = queue.pop();                 // pops one packet off the queue
		        
		        // This line parses the packet, starting at byte 0, into an ethernet II
		        // frame header. All we want is the payload.
		        jND_EthernetII enet = new jND_EthernetII();
		        enet.parse(pkt);
		        
		        // Check to see that the payload is an ARP packet
		        if(enet.type().toInt() != jND_EthernetII.T_ARP) continue;
		        
		        // These lines parse the payload, starting at byte 0, into an ARP header
		        jND_ARP arp = new jND_ARP();
		        arp.parse(enet.payload());                 // parse the payload of the frame
		        
		        // Check for an ARP reply, ignoring ARP requests
		        if(arp.opcode().toInt() == jND_ARP.ARP_REPLY) {
		            try {
		            	System.out.println("Got a REPLY");
		            	// Got a reply ... print out salient information
			            System.out.println("\nARP REPLY\n");
			            System.out.println("  Source MAC: " +
			                new jND_EthernetAddress(arp.sourceHWAddress()).toString());
			            System.out.println("  Source IP:  " +
			                new jND_IPv4Address(arp.sourceProtocolAddress()).toString());
			            System.out.println("  Dest   MAC: " +
			                new jND_EthernetAddress(arp.targetHWAddress()).toString());
			            System.out.println("  Dest   IP:  " +
			                new jND_IPv4Address(arp.targetProtocolAddress()).toString());
					} catch (Exception e) {
						System.out.println("reportArps: printing ARP: Unexpected exception: " + e.toString());
						System.exit(1);
					}
		        }
		    }	    
		} catch (Exception e) {
			System.out.println("reportArps: main loop: " + e.toString());
			System.exit(1);
		}
	}
	
	/**
	 * main - the main program for this example
	 */
	public static void main(String[] args) {
	    try {
	    	// Find out how many NICs are in this machine
	    	int numNics = jND_NIC.numberOfNICs(); 
	 
	 		// Print out a menu of NICs
		    for(int i = 0; i < numNics; i++) {
		        jND_NIC nic = new jND_NIC(i);           // Instantiate the i-th NIC
		        
		        // NOTE: the description comes back as a String
		        System.out.println("" + (i+1) + ". " + nic.description());
		    }
		    
		    // Ask the user which NIC to use.
		    System.out.print("Which NIC do you wish to use? ");
		    
		    // Read the reply from the console
		    BufferedReader kbd = new BufferedReader(new InputStreamReader(System.in));
		    String line = kbd.readLine();
		    int nicIndex = Integer.parseInt(line);
		
		
		    jND_NIC nicToUse =  new jND_NIC(--nicIndex);   // Instantiate the chosen NIC
		    
		    // This instantiates a capture session on the nic. The 'true'
		    // says to capture in promiscuous mode. 'false' would not use
		    // promiscuous mode. NOTE: many wireless cards do not support
		    // promiscuous mode.
		
		 
		    // Promiscuous mode means capture ALL packets. Normally the
		    // NIC will only capture packets addressed to it (including
		    // broadcast packets)
		    jND_CaptureSession cap = new jND_CaptureSession(nicToUse, false);
		 
		    // Start the capture running. No packets are captured until
		    // this happens. This actually starts a jND_CaptureThread to
		    // do the capturing ... though you can remain ignorant of
		    // if you wish.
		    cap.start();
		    
		    // Call a function to report ARP packets
		    reportArps(cap);
	
		    // Dispose of the capture session. This, of course, happens when the
		    // main program exits ... but it is a good habit to destroy these
		    // when you are done with them, as capture sessions possess
		    // resources that are NOT recovered via garbage collection.
		    cap.dispose();
		 
	    // This exception handling will catch exceptions thrown by
	    // jNetDev (jND_Exceptions) and the underlying Java system. The
	    // jND_Exception class is derived from the RuntimeException 
	    // class, so the catch phrase will catch either kind of
	    // exception.
		 } catch (Exception e) {
			System.out.println("main: Unexpected exception: " + e.toString());
			System.exit(1);
		 }
	    
	}
};

