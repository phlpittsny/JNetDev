import jNetDev.jND_BYTE1;
import jNetDev.jND_BYTE2;
import jNetDev.jND_NIC;
import jNetDev.Headers.jND_UDP;
import jNetDev.jND_Utility;
import jNetDev.Headers.jND_EthernetAddress;
import jNetDev.Headers.jND_EthernetII;
import jNetDev.Headers.jND_IPv4;
import jNetDev.Headers.jND_IPv4Address;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.io.ByteArrayOutputStream;

import javax.swing.JComboBox;


public class UDPExtra extends JFrame {

	private static final long serialVersionUID = 1;
	private JPanel contentPane;
	private JTextField txtDstPort;
	private JTextField txtDestIP;
	private JTextArea txtLog;
	private JComboBox cmbNIC;
	
	private JTextField txtOvert;
	private JTextField txtCovert;
	private JTextField txtSrcPort;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UDPExtra frame = new UDPExtra();
					
				    // Find out how many NICs are in this machine
				    int numNics = jND_NIC.numberOfNICs(); 

				    // For each NIC, add its description to the combo box
				    for(int i = 0; i < numNics; i++) {
				        jND_NIC nic = new jND_NIC(i);
				        frame.cmbNIC.addItem(nic.description());
				    }
				    frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Build a UDP packet with data beyond the end of the IP length
	 * This will be a normal UDP packet to the given port with the UDP data portion being a text string
	 * that says "report usage" and with data past the end of the IP datagram that is the covert message.
	 */
	public void sendCovert(jND_NIC nicToUse, jND_IPv4Address srcIP, jND_IPv4Address dstIP, 
			int srcPort, int dstPort, 
			String overtMessage, String covertMessage) {
		// Start building the packet. Since each header becomes the
		// payload of the header that precedes it, it is easiest to
		// do this inside out ... i.e., build the innermost header
		// first.
		jND_UDP udp = new jND_UDP();
		udp.sourcePort(new jND_BYTE2(srcPort));	
		udp.destPort(new jND_BYTE2(dstPort));
		
		// Specify the two IP addresses ... these are used for the pseudo header
		// included in the checksum
		udp.destinationIP(dstIP);
		udp.sourceIP(srcIP);
		
		udp.payload(overtMessage.getBytes());
		byte[] udpBA = udp.build();	// Build the packet header
		txtLog.append("\n\n*** UDP portion length = " + udpBA.length + "\n");
		
		// Tack on the overt message
		ByteArrayOutputStream covert = new ByteArrayOutputStream();
		covert.write(udpBA, 0, udpBA.length);
		covert.write(covertMessage.getBytes(), 0, covertMessage.length());
		try {
			covert.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		txtLog.append("*** UDP + covert message length = " + covert.toByteArray().length + "\n");
		
		// Now to the IP header
		jND_IPv4 ip = new jND_IPv4();
		ip.version(new jND_BYTE1(4));						// IP v4
		// The tos, fragmentID, fragmentOffset and flags fields
		// all default to the correct values for us (and for most users)
		ip.ttl(new jND_BYTE1(255));							// maxed out ttl
		ip.protocol(new jND_BYTE1(jND_IPv4.P_UDP));		// ICMP header next
		ip.srcAddress(srcIP);
		ip.destAddress(dstIP);
		ip.payload(covert.toByteArray());
		byte[] ipBA = ip.build();
		txtLog.append("*** IP + covert + UDP length = " + ipBA.length + "\n");
		
		// Finally the Ethernet header
		jND_EthernetII enet = new jND_EthernetII();
		byte[] dstMAC_BA = jND_Utility.arpFor(nicToUse, dstIP.toByteArray());
		if(dstMAC_BA == null || dstMAC_BA.length != jND_EthernetAddress.ENET_ADDR_LEN) {
	    	JOptionPane.showMessageDialog(this, "Cannot resolve IP address to a MAC address.", "ARP Failed", JOptionPane.ERROR_MESSAGE);
	        return;
		}
		jND_EthernetAddress dstMAC = new jND_EthernetAddress(dstMAC_BA);
		enet.destAddress(dstMAC);
		enet.srcAddress(nicToUse.macAddress());
		enet.type(new jND_BYTE2(jND_EthernetII.T_IP));
		enet.payload(ipBA);
		byte[] enetBA = enet.build();	
		txtLog.append("Frame length (EnetII + IP + covert + UDP) = " + enetBA.length + "\n");
		
		// Now send it:
		nicToUse.inject(enetBA);
		
	}

	/**
	 * Do the SEND button.
	 */
	public void doSend(ActionEvent event) {
		int dstPort = 0;
		int srcPort = 0;
		
	    if( txtDestIP.getText().equals("")) {
	    	JOptionPane.showMessageDialog(this, "You must enter an IP address first", "Missing IP", JOptionPane.INFORMATION_MESSAGE);
	        return;
	    }
	    
	    if( txtSrcPort.getText().equals("")) {
	    	JOptionPane.showMessageDialog(this, "You must enter a source port number first", "Missing Port", JOptionPane.INFORMATION_MESSAGE);
	        return;
	    }
	    
	    if( txtDstPort.getText().equals("")) {
	    	JOptionPane.showMessageDialog(this, "You must enter a destination port number first", "Missing Port", JOptionPane.INFORMATION_MESSAGE);
	        return;
	    }
	    
	    try {
	    	dstPort = Integer.parseInt(txtDstPort.getText());
	    	srcPort = Integer.parseInt(txtSrcPort.getText());
	    } catch (Exception except) {
	    	JOptionPane.showMessageDialog(this, "The port numbers must be integers", "Illegal Port", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    try {	    
		    // Get and parse the destination IP
		    jND_IPv4Address dstIP = new jND_IPv4Address(txtDestIP.getText());
		
		    // Get the NIC to use
		    int nicIndex = cmbNIC.getSelectedIndex();
		    jND_NIC nicToUse = new jND_NIC(nicIndex);   // Instantiate the chosen NIC
		    nicToUse.open();
		    
		    // Get the source IP
		    jND_IPv4Address srcIP = nicToUse.ipAddress();
			
		    // Send covert message to the target
		    JOptionPane.showMessageDialog(this, "Sending to " + txtDestIP.getText() + "\n");
		    sendCovert(nicToUse, srcIP, dstIP, srcPort, dstPort, txtOvert.getText(), txtCovert.getText());
		    nicToUse.close();
	    } catch (Exception except) {
	    	JOptionPane.showMessageDialog(this, "IPExtra: Unexpected exception: " + except.toString() + "\n");
		    except.printStackTrace();
		    System.exit(1);
	    }
	}

	/**
	 * Create the frame.
	 */
	public UDPExtra() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 482, 369);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JLabel lblInterface = new JLabel("Interface:");
		lblInterface.setBounds(10, 11, 60, 14);
		panel.add(lblInterface);
		
		JButton btnSend = new JButton("Send");
		btnSend.setBounds(357, 7, 89, 23);
		panel.add(btnSend);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSend(e);
			}
		});
		
		JLabel lblDstPort = new JLabel("Dst Port:");
		lblDstPort.setBounds(10, 78, 60, 14);
		panel.add(lblDstPort);
		
		txtDstPort = new JTextField();
		txtDstPort.setBounds(88, 74, 86, 20);
		panel.add(txtDstPort);
		txtDstPort.setColumns(10);
		
		JLabel lblDestIp = new JLabel("Dest IP:");
		lblDestIp.setBounds(10, 119, 60, 14);
		panel.add(lblDestIp);
		
		txtDestIP = new JTextField();
		txtDestIP.setBounds(88, 110, 86, 20);
		panel.add(txtDestIP);
		txtDestIP.setColumns(10);
		
		JLabel lblLog =  new JLabel("Log");
		lblLog.setBounds(10, 147, 79, 14);
		panel.add(lblLog);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new LineBorder(Color.LIGHT_GRAY));
		scrollPane.setBounds(76, 152, 370, 131);
		panel.add(scrollPane);
		
		txtLog = new JTextArea();
		txtLog.setBounds(0, 0, 347, 131);
		scrollPane.setViewportView(txtLog);
		
		cmbNIC = new JComboBox();
		cmbNIC.setBounds(77, 7, 246, 22);
		panel.add(cmbNIC);
		
		JLabel lblOvertMessage = new JLabel("Overt message:");
		lblOvertMessage.setBounds(187, 42, 94, 14);
		panel.add(lblOvertMessage);
		
		JLabel lblCovertMessage = new JLabel("Covert message:");
		lblCovertMessage.setBounds(187, 80, 107, 14);
		panel.add(lblCovertMessage);
		
		txtOvert = new JTextField();
		txtOvert.setBounds(291, 39, 155, 20);
		panel.add(txtOvert);
		txtOvert.setColumns(10);
		
		txtCovert = new JTextField();
		txtCovert.setBounds(291, 75, 155, 20);
		panel.add(txtCovert);
		txtCovert.setColumns(10);
		
		JLabel lblSrcPort = new JLabel("Src Port:");
		lblSrcPort.setBounds(10, 42, 60, 14);
		panel.add(lblSrcPort);
		
		txtSrcPort = new JTextField();
		txtSrcPort.setBounds(88, 40, 86, 20);
		panel.add(txtSrcPort);
		txtSrcPort.setColumns(10);
	}
}
