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
public class Ping2 extends javax.swing.JFrame {
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
		Ping2 inst = new Ping2();
		
	    // Find out how many NICs are in this machine
	    int numNics = jND_NIC.numberOfNICs(); 

	    // For each NIC, add its description to the combo box
	    for(int i = 0; i < numNics; i++) {
	        jND_NIC nic = new jND_NIC(i);
	        inst.cmbNIC.addItem(nic.description());
	    }
		inst.setVisible(true);
	}
	
	public Ping2() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
		PingThread pt = new PingThread(this);
		Thread t = new Thread(pt);
		t.start();
	}
	
	// ACCESSORS/MUTATORS for the PingThread
	public String getIP() {
		return txtIP.getText();
	}
	
	public void popUp(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}
	
	public int getNICno() {
		return cmbNIC.getSelectedIndex();
	}
	
	public void log(String msg) {
		txtResults.append(msg);
	}

}
