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


public class IPExtra extends JFrame {

	private static final long serialVersionUID = 1;
	private JPanel contentPane;
	private JTextField txtPort;
	private JTextField txtDestIP;
	private JTextArea txtMsg;
	private JComboBox cmbNIC;
	
	private final int SOURCE_PORT = 34;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					IPExtra frame = new IPExtra();
					
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
	public void sendCovert(jND_NIC nicToUse, jND_IPv4Address srcIP, jND_IPv4Address dstIP, int srcPort, int dstPort, String covertMessage) {
		// Start building the packet. Since each header becomes the
		// payload of the header that precedes it, it is easiest to
		// do this inside out ... i.e., build the innermost header
		// first.
		String overtMsg = "Mary Had a Little Lamb";
		jND_UDP udp = new jND_UDP();
		udp.sourcePort(new jND_BYTE2(srcPort));	
		udp.destPort(new jND_BYTE2(dstPort));
		
		// Specify the two IP addresses ... these are used for the pseudo header
		// included in the checksum
		udp.destinationIP(dstIP);
		udp.sourceIP(srcIP);
		
		udp.payload(overtMsg.getBytes());
		byte[] udpBA = udp.build();	// Build the packet header
		
		// Now to the IP header
		jND_IPv4 ip = new jND_IPv4();
		ip.version(new jND_BYTE1(4));						// IP v4
		// The tos, fragmentID, fragmentOffset and flags fields
		// all default to the correct values for us (and for most users)
		ip.ttl(new jND_BYTE1(255));							// maxed out ttl
		ip.protocol(new jND_BYTE1(jND_IPv4.P_UDP));		// ICMP header next
		ip.srcAddress(srcIP);
		ip.destAddress(dstIP);
		ip.payload(udpBA);
		byte[] ipBA = ip.build();
		
		// Tack on the overt message
		ByteArrayOutputStream covert = new ByteArrayOutputStream();
		covert.write(ipBA, 0, ipBA.length);
		covert.write(covertMessage.getBytes(), 0, covertMessage.length());
		try {
			covert.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
		enet.payload(covert.toByteArray());
		byte[] enetBA = enet.build();
		
		// Now send it:
		nicToUse.inject(enetBA);
		
	}

	/**
	 * Do the SEND button.
	 */
	public void doSend(ActionEvent event) {
		int portNo = 0;
		
	    if( txtDestIP.getText().equals("")) {
	    	JOptionPane.showMessageDialog(this, "You must enter an IP address first", "Missing IP", JOptionPane.INFORMATION_MESSAGE);
	        return;
	    }
	    
	    if( txtPort.getText().equals("")) {
	    	JOptionPane.showMessageDialog(this, "You must enter a port number first", "Missing Port", JOptionPane.INFORMATION_MESSAGE);
	        return;
	    }
	    
	    try {
	    	portNo = Integer.parseInt(txtPort.getText());
	    } catch (Exception except) {
	    	JOptionPane.showMessageDialog(this, "The port number must be an integer", "Illegal Port", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    try {	    
		    // Get and parse the destination IP
		    jND_IPv4Address ipaddr = new jND_IPv4Address(txtDestIP.getText());
		
		    // Get the NIC to use
		    int nicIndex = cmbNIC.getSelectedIndex();
		    jND_NIC nicToUse = new jND_NIC(nicIndex);   // Instantiate the chosen NIC
		    nicToUse.open();
		    
		    // Get the source IP
		    jND_IPv4Address myipaddr = nicToUse.ipAddress();
			
		    // Send covert message to the target
		    JOptionPane.showMessageDialog(this, "Sending to " + txtDestIP.getText() + "\n");
		    sendCovert(nicToUse, myipaddr, ipaddr, SOURCE_PORT, portNo, txtMsg.getText());
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
	public IPExtra() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 482, 307);
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
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(10, 42, 46, 14);
		panel.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setBounds(99, 39, 86, 20);
		panel.add(txtPort);
		txtPort.setColumns(10);
		
		JLabel lblDestIp = new JLabel("Dest IP:");
		lblDestIp.setBounds(10, 78, 46, 14);
		panel.add(lblDestIp);
		
		txtDestIP = new JTextField();
		txtDestIP.setBounds(99, 75, 86, 20);
		panel.add(txtDestIP);
		txtDestIP.setColumns(10);
		
		JLabel lblMessage =  new JLabel("Message:");
		lblMessage.setBounds(10, 115, 79, 14);
		panel.add(lblMessage);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new LineBorder(Color.LIGHT_GRAY));
		scrollPane.setBounds(99, 115, 347, 131);
		panel.add(scrollPane);
		
		txtMsg = new JTextArea();
		txtMsg.setBounds(0, 0, 347, 131);
		scrollPane.setViewportView(txtMsg);
		
		cmbNIC = new JComboBox();
		cmbNIC.setBounds(99, 7, 246, 22);
		panel.add(cmbNIC);
	}
}
