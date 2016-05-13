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


import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.util.Date;
import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ComboBoxModel;
import javax.swing.WindowConstants;
import javax.swing.JOptionPane;
import jNetDev.*;
import jNetDev.Headers.*;


/**
* This code was generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* *************************************
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED
* for this machine, so Jigloo or this code cannot be used legally
* for any corporate or commercial purpose.
* *************************************
*/
public class Ping extends javax.swing.JFrame {
	private JPanel mainPanel;
	private JTextArea txtResults;
	private JScrollPane scrResults;
	private JLabel lblResults;
	private JButton btnSend;
	private JTextField txtIP;
	private JLabel lblIP;
	private JComboBox cmbNIC;
	private JLabel lblNIC;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
	    Ping inst = new Ping();
	    inst.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		
	    // Find out how many NICs are in this machine
	    int numNics = jND_NIC.numberOfNICs(); 

	    // For each NIC, add its description to the combo box
	    for(int i = 0; i < numNics; i++) {
	        jND_NIC nic = new jND_NIC(i);
	        inst.cmbNIC.addItem(nic.description());
	    }
	    inst.setVisible(true);
	}
	
	public Ping() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setTitle("Ping");
			{
				mainPanel = new JPanel();
				this.getContentPane().add(mainPanel, BorderLayout.CENTER);
				mainPanel.setLayout(null);
				mainPanel.setPreferredSize(new java.awt.Dimension(459, 405));
				{
					lblNIC = new JLabel();
					mainPanel.add(lblNIC);
					lblNIC.setText("Choose a NIC");
					lblNIC.setBounds(4, 1, 83, 30);
				}
				{
					ComboBoxModel cmbNICModel = new DefaultComboBoxModel();
					cmbNIC = new JComboBox();
					mainPanel.add(cmbNIC);
					cmbNIC.setModel(cmbNICModel);
					cmbNIC.setBounds(90, 3, 362, 30);
				}
				{
					lblIP = new JLabel();
					mainPanel.add(lblIP);
					lblIP.setText("IP to Ping");
					lblIP.setBounds(6, 35, 60, 30);
				}
				{
					txtIP = new JTextField();
					mainPanel.add(txtIP);
					txtIP.setBounds(90, 38, 262, 30);
				}
				{
					btnSend = new JButton();
					btnSend.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent evt) {
							doSend();
						}
					});
					mainPanel.add(btnSend);
					btnSend.setText("Send");
					btnSend.setBounds(381, 39, 70, 30);
				}
				{
					lblResults = new JLabel();
					mainPanel.add(lblResults);
					lblResults.setText("Results");
					lblResults.setBounds(5, 67, 60, 30);
				}
				{
					scrResults = new JScrollPane();
					mainPanel.add(scrResults);
					scrResults.setBounds(6, 95, 446, 260);
					{
						txtResults = new JTextArea();
						scrResults.setViewportView(txtResults);
					}
				}
			}
			pack();
			this.setSize(467, 395);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// The do Send button code
	private void doSend() {
	    if( txtIP.getText() == "" ) {
	    	JOptionPane.showMessageDialog(this, "You must enter an IP address first.");
	        return;
	    }
	    try {	    
	        // Get set to ping
	        final int NPINGS = 4;
		int ident = 0;
		
		// Get and parse the ip
		jND_IPv4Address ipaddr = new jND_IPv4Address(txtIP.getText());
		
		// Select an identifier for the pings
		Random rand = new Random(new Date().getTime());
		ident = rand.nextInt(1024);		// ID in the range 0..1023. This restriction is
										// not required, just chosen to keep things from
										// getting too large
								
		// Get the NIC to use
	        int nicIndex = cmbNIC.getSelectedIndex();
	        jND_NIC nicToUse = new jND_NIC(nicIndex);   // Instantiate the chosen NIC
	        nicToUse.open();
		
		// Ping the target
		txtResults.append("Pinging " + txtIP.getText() + "\n");
		for(int i = 0; i < NPINGS; i++) {
			String result = pingOnce(nicToUse, ipaddr, ident, i);
			txtResults.append("  " + result + "\n");
		}
		nicToUse.close();
	    } catch (Exception e) {
		txtResults.append("main: Unexpected exception: " + e.toString() + "\n");
		e.printStackTrace();
		System.exit(1);
	    }
	}		
	
	/** 
	 * Routine to send a single ping and await the response
	 */
	private String pingOnce(jND_NIC nic, jND_IPv4Address targetip, int ident, int seqno) {
		// Data to fill up the payload of the PING
		String data = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String pingFilter = "icmp[icmptype] = icmp-echoreply";
		
		// Start building the packet. Since each header becomes the
		// payload of the header that precedes it, it is easiest to
		// do this inside out ... i.e., build the innermost header
		// first.
		jND_ICMP icmp = new jND_ICMP();
		icmp.type(new jND_BYTE1(jND_ICMP.T_PING_REQUEST));	// Set the type field
		icmp.code(new jND_BYTE1(0));						// Code == 0 for a ping
		icmp.identifier(new jND_BYTE2(ident));
		icmp.sequenceNumber(new jND_BYTE2(seqno));
		icmp.payload(data.getBytes());
		byte[] icmpBA = icmp.build();	// Build the packet header
		
		// Now to the IP header
		jND_IPv4 ip = new jND_IPv4();
		ip.version(new jND_BYTE1(4));						// IP v4
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
					txtResults.append("pingOnce: Exception sleeping: " + e.toString() + "\n");
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
				cap.dispose();
				return "Reply received! Ping #" + icmp1.sequenceNumber() + "\n";
			}
		}
		cap.dispose();
		return "Request timed out!";
	}

}
