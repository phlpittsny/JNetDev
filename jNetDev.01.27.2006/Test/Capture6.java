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
import jNetDev.*;
import java.io.*;

public class Capture6 {
	public static void dumpPacket(byte[] pkt, int pno) {
		int cnt = 0;
		
		System.out.print("PACKET "  + pno + "\n");
		for(int i = 0; i < pkt.length; i++) {
			jND_BYTE1 octet = new jND_BYTE1(pkt[i]);
			System.out.print("0x" + jND_Utility.byte1ToHex(octet) + " ");
			cnt++;
			if(cnt == 16) {
				System.out.print("\n");
				cnt = 0;
			}
		}
		if(cnt != 0) System.out.print("\n");
		System.out.print("\n");
	}
	
	public static void doCapture(jND_CaptureSession cap) {
		try {
			jND_PacketQueue pq = cap.packetQueue();
			
			for(int i = 0; i < 6; i++) {
				byte[] pkt = pq.pop();
				dumpPacket(pkt, i+1);
			}
		} catch(Exception e) {
			System.out.println("Exception " + e.toString());
		} 
	}
	
	
	public static void main(String[] args) {
		try {
			BufferedReader rdr = null;
			try {
				rdr =
					new BufferedReader(new InputStreamReader(System.in));
			} catch(Exception e) {
				System.out.println("Cannot open input: " + e.toString());
			}
			
			int n = jND_NIC.numberOfNICs();
		
			System.out.print("There are " + n + " NICs in this computer.\n");
			for(int i = 0; i < n; i++) {
				jND_NIC nic = new jND_NIC(i);
				System.out.print("NIC " + i + " Name= " + nic.name()
					+ " Desc= " + nic.description() + "\n");
			}
			
			System.out.print("Choose a nic: ");
			int nicNo;
			String answer = rdr.readLine();
			nicNo = Integer.parseInt(answer);
	
			jND_NIC nic = new jND_NIC(nicNo);
			jND_CaptureSession cap = new jND_CaptureSession(nic, false);
			cap.start();
	
			doCapture(cap);	
			cap.stop();
		} catch(Exception e) {
			System.out.println("Capture6.main exception: " + e.toString() + "\n");
		}
		
	}
}
