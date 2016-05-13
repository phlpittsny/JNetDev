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
#include <pcap.h>			// MUST BE FIRST to avoid conflicts$G$G
#include <jni.h>

#include <stdio.h>

#ifdef LINUX
#define UNIX
#endif /*LINUX*/

#ifdef OSX
#define UNIX
#endif /*OSX*/

/******* Low level includes for Win32 (CYGWIN) *******/
#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#define USE_SYS_TYPES_FD_SET
#include <winsock2.h>
#include <Windows.h>
#include <iphlpapi.h>

// I HATE THIS but ...
// pcap.h and packet32.h do not play together nicely
// and placing this here (from Packet32.h) was the
// only work around I could make work for me.
struct bpf_stat {
	UINT bs_recv;
	UINT bs_drop;
	UINT ps_ifdrop;
	UINT bs_capt;
};

#include <Packet32.h>
#include <ddk/ndis.h>
#endif /* WIN32 */
/******* End low level includes for Win32 *******/

/******* Low level includes for packet captures *******/
#ifdef UNIX
#ifdef OSX
/*#include <net/bpf.h>*/
#include <sys/sysctl.h>
#include <sys/socket.h>
#include <net/if_dl.h>
#include <sys/fcntl.h>
#include <sys/errno.h>
#include <net/route.h>

#include <stdlib.h>
#include <string.h>

/* From net/bpf.h ... missing from pcap/bpf.h */
#define BIOCSETIF       _IOW('B',108, struct ifreq)
#define BIOCVERSION     _IOR('B',113, struct bpf_version)
#define BIOCGHDRCMPLT   _IOR('B',116, u_int)

#endif /* OSX */

#include <net/if.h>

#ifdef LINUX
#include <linux/in.h>
#include <stdint.h>
#include <sys/socket.h>

#define _LINUX_IF_H
#include <linux/if.h>
#include <linux/if_arp.h>
#endif /*LINUX*/
#include <sys/ioctl.h>

extern int errno;

#endif /* UNIX */

/******* End low level includes for UNIX *******/

#include "jND_CaptureSession.h"
#include "jND_CaptureThread.h"
#include "jND_NICList.h"
#include "jND_NIC.h"

/*** NOTE These three constants MUST coincide 
 *** with those in jND_CaptureSession.java     ***/
#define jND_NONE	0
#define jND_LIVE	1
#define jND_OFFLINE	2

  
// ==========  General Utility Routines  ==========
#ifdef PCAP_NEXT_EX
#define JND_MAXPACKET 2048
struct udata {
	int *caplen;
	const u_char* pkt;
};

/** One packet arrived ... store it away
 **/
static void one_packet(
  u_char* uptr_in, 
  const struct pcap_pkthdr* hdr, 
  const u_char* pkt) {
	struct udata* uptr = (struct udata*)uptr_in;

	if(hdr->caplen > JND_MAXPACKET) {
		fprintf(stderr, "Captured packet > %d octets!\n", JND_MAXPACKET);
		exit(1);
	}

	*(uptr->caplen) = hdr->caplen;
	memcpy((u_char*)uptr->pkt, pkt, hdr->caplen);
}

/** Get the next packet from the session
 **/
static int pcap_next_ex(
  pcap_t* pcap_d, 
  struct pcap_pkthdr** hdr,
  const u_char** rawpacket) {

	// NOTE: Presumes that the packet store has been preallocated
	// and is jND_MAXPACKET octets long. If a packet arrives longer
	// than jND_MAXPACKET a fatal error occurs.
	struct udata user;
	user.caplen = &((*hdr)->caplen);
	user.pkt = *rawpacket;

	// Get one packet
	int n = pcap_dispatch(pcap_d, 1, one_packet, (u_char*) &(user));

	if(n == -1 && pcap_file(pcap_d) != NULL)
		return -1;
	return n;
}
#endif /*PCAP_NEXT_EX*/

/**
 * Function to format a reply as a byte array. The format of
 * the array is:
 * <pre>
 *  +-------+-------+--   --+-------+-------+--   --+
 *  | plen  |  ptr in 'size' octets | msg (to NULL) |
 *  +-------+-------+--   --+-------+-------+--   --+
 * </pre>
 * The 'plen' is always present as a single octet, even if zero.
 * The ptr is absent if plen == 0. The 'msg' is provided in its
 * entirety, NOT including the NULL at the end. 'mlen' is implicit 
 * in the size of the array and is not present in the array itself.
 */
static jbyteArray message(JNIEnv *env, 
  void* ptr, 
  int plen, 
  char* msg, 
  int mlen) {
		int totalLen = 1 + plen + mlen;
		jbyteArray newmsg = (*env)->NewByteArray(env, totalLen); 
		if(newmsg == 0) {
			printf("jND_osSupport: jni error: message out of memory(101)\n");
			exit(101);
		}
		char* ptrary = (char*) malloc(plen);
		unsigned long ptrtmp = (unsigned long)ptr;
		int i;
		for(i = plen-1; i >= 0; i--) {
			ptrary[i] = (char)(ptrtmp & 0xff);
			ptrtmp = ptrtmp >> 8;
		}
		u_char tlen = (u_char)(plen & 0xff);
		(*env)->SetByteArrayRegion(env, newmsg, 0, 1, (char*)&tlen);
		(*env)->SetByteArrayRegion(env, newmsg, 1, plen, ptrary);
		(*env)->SetByteArrayRegion(env, newmsg, 1+plen, mlen, (char*)msg);
		free(ptrary);
		return newmsg;
  }
  
/*
 * Function to put together a void* from a byte array. The void* is typically
 * a pcap_d (void*) or other pointer to be used with pcap.
 */
static void* getPtr(JNIEnv *env, jbyteArray jptr_ba) {
  	jbyte *ptr_ba = (*env)->GetByteArrayElements(env, jptr_ba, NULL);
	int i;
	if(ptr_ba == 0) {
		printf("jND_osSupport: jni error: getPtr out of memory(102)\n");
		exit(102);
	}
  	int size = (*env)->GetArrayLength(env, jptr_ba);
  	
  	unsigned long ptr = 0;
	for(i = 0; i < sizeof(void*); i++)
		if(i < size)
			ptr = (ptr << 8) | ((unsigned long)ptr_ba[i]) & 0xff;
  	(*env)->ReleaseByteArrayElements(env, jptr_ba, ptr_ba, 0);
  	return (void*)ptr;
}

// ==========  jND_CaptureSession Support  ==========

/*
 * Class:     jNetDev_jND_0005fCaptureSession
 * Method:    openCapture
 * Signature: (ILjava/lang/String;IZI)V
 */
JNIEXPORT jbyteArray JNICALL Java_jNetDev_jND_1CaptureSession_openCapture
  (JNIEnv *env, jclass class, 
  	jint mode, 
  	jstring devname, 
  	jint len, 
  	jboolean promisc, 
  	jint toms){
  		// Allocate locals
  		jbyteArray ret_msg;
		char ebuf[1024];
		
		// Get the string
		char *devstr = (char*)(*env)->GetStringUTFChars(env, devname, NULL);
		if(devstr == 0) {
			printf("jND_CaptureSession: jni error: openCapture out of memory(103)\n");
			exit(103);
		}

		// Live capture session
		pcap_t* pcapd = 0;
		if(mode == jND_LIVE) {
			pcapd = 
				(void*)pcap_open_live(devstr, len, promisc, toms, ebuf);
			if(pcapd == 0) {
				char msg[1024];
				sprintf(msg, "jND_CaptureSession.openCapture : %s", ebuf);
				ret_msg = message(env, 0, 0, msg, strlen(msg));
			}
			else {
				ret_msg = message(env, pcapd, sizeof(pcapd), "", 0);
			}
		}

		// Offline capture session
		else if(mode == jND_OFFLINE) {
			pcapd = 
				(void*)pcap_open_offline(devstr, ebuf);
			if(pcapd == 0) {
				char msg[1024];
				sprintf(msg, "jND_CaptureSession.openCapture: %s", ebuf);
				ret_msg = message(env, 0, 0, msg, strlen(msg));
			}
			else {
				ret_msg = message(env, pcapd, sizeof(pcapd), "", 0);
			}
		}
		(*env)->ReleaseStringUTFChars(env, devname, devstr);
		return ret_msg;
  }

/*
 * Class:     jNetDev_jND_0005fCaptureSession
 * Method:    closeCapture
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_jNetDev_jND_1CaptureSession_closeCapture
  (JNIEnv *env, jclass class, 
  jbyteArray jpcap_d_ba, 		// pcap_d
  jbyteArray jpfilter_ba, 		// p_filter
  jbyteArray jdumper_ba) {		// dumper
  	void* pcap_d = getPtr(env, jpcap_d_ba);
  	void* pfilter = getPtr(env, jpfilter_ba);
  	void* dumper = getPtr(env, jdumper_ba);
  	
	if(pcap_d != 0) {
		pcap_close((pcap_t*)pcap_d);
	}
	if(pfilter != 0) {
		pcap_freecode(pfilter);
		free(pfilter);
	}
	if(dumper != 0) {
		pcap_dump_close((pcap_dumper_t*)dumper);
	}
  }

/*
 * Class:     jNetDev_jND_0005fCaptureSession
 * Method:    dumpOpen
 * Signature: ([BLjava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jNetDev_jND_1CaptureSession_dumpOpen
  (JNIEnv *env, jclass class, jbyteArray jpcap_d_ba, jstring jfname){
  	void* pcap_d = getPtr(env, jpcap_d_ba);
  	char* fname = (char*)(*env)->GetStringUTFChars(env, jfname, NULL);
  	jbyteArray ret_msg;
  	
  	void* dumper = (void*)pcap_dump_open((pcap_t*)pcap_d, fname);
	if(dumper == 0) {
		char msg[1024];
		sprintf(msg, "jND_CaptureSession.dumpOpen: Cannot open dump session.");
		ret_msg = message(env, 0, 0, msg, strlen(msg));
	}
	else {
		ret_msg = message(env, dumper, sizeof(dumper), "", 0);
	}
  	
  	(*env)->ReleaseStringUTFChars(env, jfname, fname);
  	return ret_msg;
  }

/*
 * Class:     jNetDev_jND_0005fCaptureSession
 * Method:    dump1
 * Signature: ([B[B)V
 */
JNIEXPORT void JNICALL Java_jNetDev_jND_1CaptureSession_dump1
  (JNIEnv *env, jclass class, jbyteArray jdumper, jbyteArray jpkt){
  	void* dumper = getPtr(env, jdumper);
  	jbyte *pkt = (*env)->GetByteArrayElements(env, jpkt, NULL);
	struct pcap_pkthdr hdr;
	
	if(pkt == 0) {
		printf("jND_CaptureSession: jni error: dump1 out of memory(104)\n");
		exit(104);
	}
	
  	int size = (*env)->GetArrayLength(env, jpkt);
	hdr.caplen = hdr.len = size;
	pcap_dump((u_char *)dumper, &hdr, pkt);

  	(*env)->ReleaseByteArrayElements(env, jpkt, pkt, 0);
  }

/*
 * Class:     jNetDev_jND_0005fCaptureSession
 * Method:    dumpClose
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_jNetDev_jND_1CaptureSession_dumpClose
  (JNIEnv *env, jclass class, jbyteArray jdumper){
  	void* dumper = getPtr(env, jdumper);
  	
	pcap_dump_close((pcap_dumper_t*)dumper);
  }


/*
 * Class:     jNetDev_jND_0005fCaptureSession
 * Method:    installFilter
 * Signature: ([B[BLjava/lang/String;ZI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jNetDev_jND_1CaptureSession_installFilter
  (JNIEnv *env , jclass class, 
  jbyteArray jpcap_d_ba, 
  jbyteArray jpfilter_ba,
  jstring jfilter, 
  jboolean optimize, 
  jint netmask) {
  	void* pcap_d = getPtr(env, jpcap_d_ba);
  	void* pfilter = getPtr(env, jpfilter_ba);
  	char* filter = (char*)(*env)->GetStringUTFChars(env, jfilter, NULL);
  	jbyteArray ret_msg;  	
  	
	int retn;
		
	/* Free any existing filter program */
	if(pfilter != 0) {
		pcap_freecode((struct bpf_program*)pfilter);
	}
	else {
		pfilter = (void*) malloc(sizeof(struct bpf_program));
		if(pfilter == 0) {
			char msg[1024];
			sprintf(msg, "jND_CaptureSession.installFilter: out of memory");
			ret_msg = message(env, 0, 0, msg, strlen(msg));
		}
	}

	if(pfilter != 0) {
		/* Install the new filter */
		retn = pcap_compile((pcap_t*)pcap_d,
			(struct bpf_program*)pfilter,
			filter,
			optimize,
			(bpf_u_int32)netmask);
	
		if(retn < 0) {
			char msg[1024];
			sprintf(msg, "jND_CaptureSession.installFilter: cannot compile filter");
			ret_msg = message(env, 0, 0, msg, strlen(msg));
			free(pfilter);
		}
		else {
			retn = pcap_setfilter((pcap_t*)pcap_d, (struct bpf_program*)pfilter);
	
			if(retn < 0) {
				char msg[1024];
				sprintf(msg, "jND_CaptureSession.installFilter: cannot install filter");
				ret_msg = message(env, 0, 0, msg, strlen(msg));
				pcap_freecode((struct bpf_program*)pfilter);
				free(pfilter);
			}
			else {
				ret_msg = message(env, pfilter, sizeof(void*), "", 0);
			}
		}
	}			
  	
  	(*env)->ReleaseStringUTFChars(env, jfilter, filter);
  	return ret_msg;
  }

// ==========  jND_CaptureThread Support ==========

/*
 * Class:     jNetDev_jND_0005fCaptureThread
 * Method:    capture1
 * Signature: ([BLjNetDev/jND_PacketQueue;)I
 */
JNIEXPORT jint JNICALL Java_jNetDev_jND_1CaptureThread_capture1
  (JNIEnv *env, jclass class, jbyteArray jpcap_d_ba, jobject jpktq) {

  	void* pcap_d = getPtr(env, jpcap_d_ba);
	if(pcap_d == 0)
		return -3;	// Bad capture descriptor

	// Returned values from pcap_next_ex
	int caplen = 0;
	int flag = 0;

#ifdef PCAP_NEXT_EX
	u_char tpacket[JND_MAXPACKET];
	u_char* packet = tpacket;
	struct pcap_pkthdr thdr;
	struct pcap_pkthdr* hdr = &thdr;
#else
	u_char* packet;
	struct pcap_pkthdr* hdr;
#endif

	// Get a packet
	flag = pcap_next_ex(pcap_d, &hdr, (const u_char**)&packet);
	caplen = hdr->caplen;
	
	if(flag != 1)
		return flag;

	// Allocate a new byte array the size of the packet
        jbyteArray pkt = (*env)->NewByteArray(env, (jsize)caplen);
	if(pkt == 0) {
		printf("jND_CaptureThread: jni error: capture1: out of memory for packet(105)\n");
		exit(105);
	}

	// Copy the packet into the byte array and call push
        (*env)->SetByteArrayRegion(env, pkt, 0, caplen, (jbyte *)packet);

	// Get the jND_PacketQueue class and the ID of the push method
	jclass qclass = (*env)->FindClass(env, "jNetDev/jND_PacketQueue");
	if(qclass == 0) {
		printf("jND_CaptureThread: jni error: capture1: cannot find jND_PacketQueue class(114)\n");
		exit(114);
	}

        jmethodID mid = (*env)->GetMethodID(env, qclass, "push", "([B)V");
	if(mid == 0) {
		printf("jND_CaptureThread: jni error: capture1: cannot find void push(byte[]) method(115)\n");
		exit(115);
	}

	// Add packet to queue
        (*env)->CallVoidMethod(env, jpktq, mid, pkt);

        return flag;
  }

// ==========  jND_NICList Support ==========

static int ndevs = -1;
static int setupdone = 0;

static char** v_name;
static char** v_description;
static u_char** v_ipaddress;
static u_char** v_netmask;
static u_char** v_gateway;
static u_char** v_macaddress;

#ifdef UNIX
static void setup() {
	if(setupdone) return;		
	setupdone = 1;
		
	long numreqs = 30;
	struct ifconf ifc;
        struct ifreq *ifr;
        int n, err = -1;
        int skfd;
	int currdev = 0;
	int i;

	ndevs = 0;
	/** The code for figuring out how many NICS are in the machine, and
	 ** Getting addresses etc from them is due to ifconfig from Linux */
	/* SIOCGIFCONF currently seems to only work properly on AF_INET sockets
	(as of 2.1.128) */ 
	skfd = socket(AF_INET, SOCK_DGRAM, 0);
	if (skfd < 0) {
		ndevs = 0;
		return;
	}

	ifc.ifc_buf = NULL;
	for (;;) {
		ifc.ifc_len = sizeof(struct ifreq) * numreqs;
		ifc.ifc_buf = (char*)realloc(ifc.ifc_buf, ifc.ifc_len);
		if(ifc.ifc_buf == 0) {
		    close(skfd);
		    ndevs = 0;
		    return;
		}

		if (ioctl(skfd, SIOCGIFCONF, &ifc) < 0) {
		    fprintf(stderr, "SIOCGIFCONF: %s\n", strerror(errno));
		    free(ifc.ifc_buf);
		    close(skfd);
		    ndevs = 0;
		    return;
		}
		if (ifc.ifc_len == sizeof(struct ifreq) * numreqs) {
		    /* assume it overflowed and try again */
		    numreqs += 10;
		    continue;
		}
		break;
	}

	ifr = ifc.ifc_req;

	// Get the # of interfaces and instantiate arrays
        int offset = 0;
        ndevs = 0;
        struct ifreq ifr2;
#ifdef LINUX
        for(offset = 0;
                offset < ifc.ifc_len;
                offset += sizeof(ifr->ifr_name) +
			sizeof(struct sockaddr)) {
#endif /*LINUX*/

#ifdef OSX
        for(offset = 0;
                offset < ifc.ifc_len;
                offset += sizeof(ifr->ifr_name) +
                  (ifr->ifr_addr.sa_len > sizeof(struct sockaddr)
                    ? ifr->ifr_addr.sa_len : sizeof(struct sockaddr))) {
#endif /*OSX*/

                // Get one interface
                ifr = (struct ifreq*) (((char*) ifc.ifc_req)+offset);

                // If not IP, skip
printf("setup: interface %s address type = %d\n", ifr->ifr_name, ifr->ifr_addr.sa_family); fflush(stdout);
                if(ifr->ifr_addr.sa_family != AF_INET)
                        continue;

                // Otherwise count it
                ndevs++;
printf("setup: ndevs = %d\n", ndevs); fflush(stdout);
        }

	// Instantiate NIC arrays
	v_name = (char**)malloc(ndevs*sizeof(char*));
	v_description = (char**)malloc(ndevs*sizeof(char*));
	v_ipaddress = (u_char**)malloc(ndevs*sizeof(u_char*));
	v_netmask = (u_char**)malloc(ndevs*sizeof(u_char*));
	v_gateway = (u_char**)malloc(ndevs*sizeof(u_char*));
	v_macaddress = (u_char**)malloc(ndevs*sizeof(u_char*));

	// Check for malloc errors
	if(!v_name || !v_description || !v_ipaddress ||
            !v_netmask || !v_gateway || !v_macaddress) {
                fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(1)\n");
		exit(1);
	}

        // Populate the arrays
#ifdef LINUX
        for(offset = 0, currdev = 0;
                offset < ifc.ifc_len;
                offset += sizeof(ifr->ifr_name) +
                    sizeof(struct sockaddr)) {
#endif /*LINUX*/

#ifdef OSX
        for(offset = 0, currdev = 0;
                offset < ifc.ifc_len;
                offset += sizeof(ifr->ifr_name) +
                  (ifr->ifr_addr.sa_len > sizeof(struct sockaddr)
                    ? ifr->ifr_addr.sa_len : sizeof(struct sockaddr))) {
#endif /*OSX*/

		// Be careful not to overflow the arrays ... just in case
if(currdev == ndevs) {printf("currdev = %d ndevs = %d\n", currdev, ndevs); fflush(stdout);}
		if(currdev == ndevs) break;

                // Get one interface
                ifr = (struct ifreq*) (((char*) ifc.ifc_req)+offset);

                // if not IP skip it
                if(ifr->ifr_addr.sa_family != AF_INET) {
printf("currdev = %d name = %s AF_INET out\n", currdev, ifr->ifr_name); fflush(stdout);

                        continue;
		}

                // Get IP address
                strcpy(ifr2.ifr_name, ifr->ifr_name);
                ifr2.ifr_addr.sa_family = AF_INET;
                if(ioctl(skfd, SIOCGIFADDR, &ifr2) < 0 ||
                        ifr2.ifr_addr.sa_family != AF_INET)
				memset(ifr2.ifr_addr.sa_data+2, 0, 4);


                // Store the IP address
                u_char* p = (u_char*)ifr2.ifr_addr.sa_data+2;
		v_ipaddress[currdev] = (u_char*)malloc(4);
		if(v_ipaddress[currdev] == 0) {
			fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(2)\n");
			exit(1);
		}
		for(i = 0; i < 4; i++) v_ipaddress[currdev][i] = p[i];

		// Store the name and description
		v_name[currdev] = (char*)malloc(strlen(ifr->ifr_name)+1);
		v_description[currdev] = (char*)malloc(strlen(ifr->ifr_name)+1);
		if(!v_name[currdev] || !v_description[currdev]) {
			fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(3) - currdev=%d\n", currdev);
			exit(1);
		}
		strcpy(v_name[currdev], ifr->ifr_name);
printf("v_name[%d] = %s address = %x\n", currdev, v_name[currdev], v_name[currdev]); fflush(stdout);
		strcpy(v_description[currdev], ifr->ifr_name);

                // Get MAC address
                struct ifreq ifr2;
		v_macaddress[currdev] = (u_char*)malloc(6);
		if(v_macaddress[currdev] == 0) {
			fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(4)\n");
			exit(1);
		}
		for(i = 0; i < 6; i++) v_macaddress[currdev][i] = 0;
#ifdef LINUX
                strcpy(ifr2.ifr_name, ifr->ifr_name);
                if(ioctl(skfd, SIOCGIFHWADDR, &ifr2) >= 0) {
			p = (u_char*)ifr2.ifr_hwaddr.sa_data;
			for(i = 0; i < 6; i++) 
				v_macaddress[currdev][i] = p[i];
		}
#endif /*LINUX*/

#ifdef OSX
                int mib[6], len;
                mib[0] = CTL_NET;
                mib[1] = AF_ROUTE;
                mib[2] = 0;
                mib[3] = AF_LINK;
                mib[4] = NET_RT_IFLIST;
                mib[5] = if_nametoindex(ifr->ifr_name);

                if(mib[5] != 0)
                  if(sysctl(mib, 6, NULL, (size_t*)&len, NULL, 0) >= 0) {
                    char * macbuf = (char*)malloc(len);
                    if(sysctl(mib, 6, macbuf, (size_t*)&len, NULL, 0) >= 0) {
                        struct if_msghdr* ifm = (struct if_msghdr*) macbuf;
                        struct sockaddr_dl* sdl = (struct sockaddr_dl*) (ifm+1);
                        p = (u_char*)LLADDR(sdl);
			for(i = 0; i < 6; i++) 
				v_macaddress[currdev][i] = p[i];
                    }
                  }
#endif /*OSX*/

                // Get Netmask
		v_netmask[currdev] = (u_char*)malloc(4);
		if(v_netmask[currdev] == 0) {
			fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(5)\n");
			exit(1);
		}
		for(i = 0; i < 4; i++) v_netmask[currdev][i] = 0;

                strcpy(ifr2.ifr_name, ifr->ifr_name);
                if(ioctl(skfd, SIOCGIFNETMASK, &ifr2) >= 0) {
#ifdef LINUX
			p = ifr2.ifr_netmask.sa_data+2;
			for(i = 0; i < 4; i++) 
				v_netmask[currdev][i] = p[i];
#endif /*LINUX*/

#ifdef OSX
			p = ifr2.ifr_addr.sa_data+2;
			for(i = 0; i < 4; i++) 
				v_netmask[currdev][i] = p[i];
#endif /*OSX*/
		}

                // Set all gateways to zero, to start
		v_gateway[currdev] = (u_char*)malloc(4);
		if(v_gateway[currdev] == 0) {
			fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(6)\n");
			exit(1);
		}
		for(i = 0; i < 4; i++) v_gateway[currdev][i] = 0;

		// Done with this device, incr currdev
		currdev++;
        }
	close(skfd);

#ifdef LINUX
        // NOW ... do gateways. Look in /proc/net/route for lines with all 0's
        // in column 2. The value in column 3 is the IP address for the
        // NIC listed in column 1. NOTE: IP addresses are listed here in
        // Hex in reverse order.
        FILE* route = fopen("/proc/net/route", "r");
        if(route == NULL) return;

        char line[1024];
        if(!fgets(line, 1023, route)) return;   // Skip header line
        while(fgets(line, 1023, route)) {
                char *p = line;
                // Get field1 = NIC name
                char* tab = index(p, '\t');
                char field1[32];
                strncpy(field1, p, tab-p);
                field1[tab-p] = 0;
                p = tab+1;

                // Get field2 = destination (0 means default gateway)
                tab = index(p, '\t');
                char field2[32];
                strncpy(field2, p, tab-p);
                field2[tab-p] = 0;
                p = tab+1;

                // Get field3 = gateway
                tab = index(p, '\t');
                char field3[32];
                strncpy(field3, p, tab-p);
                field3[tab-p] = 0;

                if(strcmp(field2, "00000000") == 0) {   // A gateway
                        // Convert field3 to an IP address
			int gw[4];
                        sscanf(field3, "%2x%2x%2x%2x", &gw[3], &gw[2], &gw[1], &gw[0]);

                        // Look for matching NIC
                        for(currdev = 0; currdev < ndevs; currdev++) {
                                if(strcmp(v_name[currdev], field1) == 0)
					for(i = 0; i < 4; i++) 
						v_gateway[currdev][i] = (u_char)gw[i];
                        }
                }
        }
        fclose(route);
#endif /*LINUX*/

#ifdef OSX
printf("setup: filling in gateways\n"); fflush(stdout);
        // NOW ... do gateways. Call netstat -rn -f inet and look for
        // rows with 'default' in column 1. The IP in column 2 is the
        // gateway and column 6 contains the device name.
        FILE* netstat = popen("/usr/sbin/netstat -rn -f inet", "r");
        if(netstat == NULL) return;

        char line[1024];
	char* p;
        while(p = fgets(line, 1023, netstat)) {  // Skip header lines
                if(p == 0) return;
                if(strncmp(line, "Destination", strlen("Destination")) == 0)
                        break;
        }

        while(fgets(line, 1023, netstat)) {       // Look for default routes
                char destination[256], gateway[256], interface[256];
                int n =
                  sscanf(line,
                    "%[^ ]%*[ ]%[^ ]%*[ ]%*[^ ]%*[ ]%*[^ ]%*[ ]%*[^ ]%*[ ]%[^ \n]",
                    destination, gateway, interface);
                if(n != 3) continue;

                if(strcmp(destination, "default") == 0) {       // A gateway
                        // Convert gateway field to an IP address
			int gw[4];
                        int k = sscanf(gateway, "%d.%d.%d.%d", 
				&gw[0], &gw[1], &gw[2], &gw[3]);
                        if(k != 4) continue;

printf("***Step4\n"); fflush(stdout);
                        // Look for matching NIC
                        for(currdev = 0; currdev < ndevs; currdev++) {
printf("   ***Step 4.1a currdev = %d ndevs = %d\n", currdev, ndevs); fflush(stdout);

printf("   ***Step 4.1b v_name[currdev] %x interface %x\n", v_name[currdev], interface); fflush(stdout);
printf("   ***Step 4.1c checking %s against %s\n", v_name[currdev], interface); fflush(stdout);
                                if(strcmp(v_name[currdev], interface) == 0) {
					for(i = 0; i < 4; i++) {
printf("   ***Copying octet %d\n", i); fflush(stdout);
						v_gateway[currdev][i] = (u_char)gw[i];
					}
				}
                        }
                }
        }
printf ("setup: Closing the pipe\n"); fflush(stdout);
        pclose(netstat);
printf("setup: DONE\n"); fflush(stdout);
#endif /*OSX*/
}
#endif /*UNIX*/

#ifdef WIN32
static void* AdapterInfo;

static void setup() {
	if(setupdone) return;
	setupdone = 1;
	
setbuf(stdout, 0);
	AdapterInfo = (void*)malloc(16*sizeof(IP_ADAPTER_INFO));
	if(AdapterInfo == 0) {
                fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(0)\n");
		exit(1);
	}
	DWORD dwBufLen = 16*sizeof(IP_ADAPTER_INFO);	/* Save the memory size of buffer */
	
	DWORD dwStatus = GetAdaptersInfo(	/* Call GetAdapterInfo */
		(PIP_ADAPTER_INFO)AdapterInfo,	/* [out] buffer to receive data */
		&dwBufLen);						/* [in] size of receive data buffer */

	if(dwStatus != ERROR_SUCCESS)	/* Verify return value is valid */
		return;
			
	PIP_ADAPTER_INFO currdev = (PIP_ADAPTER_INFO)AdapterInfo;  /* Contains ptr to adapter info */

	// Count the adapters
	for(ndevs = 0;
		currdev != 0;
		ndevs++, currdev = currdev->Next) ;	

	// Instantiate NIC arrays
	v_name = (char**)malloc(ndevs*sizeof(char*));
	v_description = (char**)malloc(ndevs*sizeof(char*));
	v_ipaddress = (u_char**)malloc(ndevs*sizeof(u_char*));
	v_netmask = (u_char**)malloc(ndevs*sizeof(u_char*));
	v_gateway = (u_char**)malloc(ndevs*sizeof(u_char*));
	v_macaddress = (u_char**)malloc(ndevs*sizeof(u_char*));

	// Check for malloc errors
	if(!v_name || !v_description || !v_ipaddress ||
            !v_netmask || !v_gateway || !v_macaddress) {
                fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(1)\n");
		exit(1);
	}
	
	// Populate the arrays
	int nicno;
	currdev = (PIP_ADAPTER_INFO)AdapterInfo;  /* Contains ptr to adapter info */
	for(nicno = 0;
		currdev != 0;
		nicno++, currdev = currdev->Next) {	
			v_name[nicno] = 
				(char*)malloc(strlen("\\Device\\NPF_") + strlen(currdev->AdapterName)+1);
			v_description[nicno] = 
				(char*) malloc(strlen(currdev->Description)+1);
			if(!v_name[nicno] || !v_description[nicno]) {
				fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(2)\n");
				exit(1);
			}
			sprintf(v_name[nicno], "\\Device\\NPF_%s", currdev->AdapterName);
			strcpy(v_description[nicno], currdev->Description);

			// Allocate address fields
			v_ipaddress[nicno] = (u_char*)malloc(4);
			v_netmask[nicno] = (u_char*)malloc(4);
			v_gateway[nicno] = (u_char*)malloc(4);
			v_macaddress[nicno] = (u_char*)malloc(4);
			if(!v_ipaddress[nicno] || !v_netmask[nicno] ||
			   !v_gateway[nicno] || !v_macaddress[nicno]) {
				fprintf(stderr, "jNetDev: Cannot alloc mem in device setup(3)\n");
				exit(1);
			}

			// Zero out fields
			int i;
			for(i = 0; i < 4; i++) {
				v_ipaddress[nicno][i] = 0;
				v_netmask[nicno][i] = 0;
				v_gateway[nicno][i] = 0;
			}

			for(i = 0; i < 6; i++)
				v_macaddress[nicno][i] = 0;

			// IP address
			int ip[4];
                        int k = sscanf(
				currdev->IpAddressList.IpAddress.String, 
				"%d.%d.%d.%d", 
				&ip[0], &ip[1], &ip[2], &ip[3]);
			if(k == 4)
				for(i = 0; i < 4; i++)
					v_ipaddress[nicno][i] = ip[i];

			// NETMASK
                        k = sscanf(
				currdev->IpAddressList.IpMask.String, 
				"%d.%d.%d.%d", 
				&ip[0], &ip[1], &ip[2], &ip[3]);
			if(k == 4)
				for(i = 0; i < 4; i++)
					v_netmask[nicno][i] = ip[i];

			// GATEWAY
                        k = sscanf(
				currdev->GatewayList.IpAddress.String, 
				"%d.%d.%d.%d", 
				&ip[0], &ip[1], &ip[2], &ip[3]);
			if(k == 4)
				for(i = 0; i < 4; i++)
					v_gateway[nicno][i] = ip[i];

			// MAC address
			for(i = 0; i < 6; i++)
				v_macaddress[nicno][i] = ((u_char *) (currdev->Address))[i];
	}
}
#endif /*WIN32*/

/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_numberOfNICs
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_jNetDev_jND_1NICList_os_1numberOfNICs
  (JNIEnv *env, jclass class) {
	char cmd[1024];
	setup();
	return ndevs;
  }

/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_nicName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jNetDev_jND_1NICList_os_1nicName
  (JNIEnv *env, jclass class, jint nicno) {
	setup();
	char* outstr = 0;

	// Get the name
	outstr  = v_name[nicno];

	jstring retstr = (*env)->NewStringUTF(env, outstr);
	if(retstr == 0) {
		printf("jND_NICList: jni error: os_nicName: out of memory(106)\n");
		exit(106);
	}
	return retstr;
  }
  
/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_nicDescription
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jNetDev_jND_1NICList_os_1nicDescription
  (JNIEnv *env, jclass class, jint nicno) {
	setup();
	char* outstr = 0;

	// Get the name
	outstr  = v_description[nicno];

	jstring retstr = (*env)->NewStringUTF(env, outstr);
	if(retstr == 0) {
		printf("jND_NICList: jni error: os_nicDescription: out of memory(107)\n");
		exit(107);
	}
	return retstr;
  }

/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_nicIpaddress
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jNetDev_jND_1NICList_os_1nicIpaddress
  (JNIEnv *env, jclass class, jint nicno) {
	setup();

	char outstr[15];
	sprintf(outstr, "%d.%d.%d.%d", 
		(u_int)v_ipaddress[nicno][0],
		(u_int)v_ipaddress[nicno][1],
		(u_int)v_ipaddress[nicno][2],
		(u_int)v_ipaddress[nicno][3]);

	jstring retstr = (*env)->NewStringUTF(env, outstr);
	if(retstr == 0) {
		printf("jND_NICList: jni error: os_nicIpaddress: out of memory(108)\n");
		exit(108);
	}
	return retstr;
  }

/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_nicNetmask
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jNetDev_jND_1NICList_os_1nicNetmask
  (JNIEnv *env, jclass class, jint nicno) {
	setup();

	char outstr[15];
	sprintf(outstr, "%d.%d.%d.%d",
		(u_int)v_netmask[nicno][0],
		(u_int)v_netmask[nicno][1],
		(u_int)v_netmask[nicno][2],
		(u_int)v_netmask[nicno][3]);

	jstring retstr = (*env)->NewStringUTF(env, outstr);
	if(retstr == 0) {
		printf("jND_NICList: jni error: os_nicNetmask: out of memory(109)\n");
		exit(109);
	}
	return retstr;
  }

/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_nicGateway
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jNetDev_jND_1NICList_os_1nicGateway
  (JNIEnv *env, jclass class, jint nicno) {
	setup();

	char outstr[15];
	sprintf(outstr, "%d.%d.%d.%d", 
		(u_int)v_gateway[nicno][0],
		(u_int)v_gateway[nicno][1],
		(u_int)v_gateway[nicno][2],
		(u_int)v_gateway[nicno][3]);

	jstring retstr = (*env)->NewStringUTF(env, outstr);
	if(retstr == 0) {
		printf("jND_NICList: jni error: os_nicGateway: out of memory(110)\n");
		exit(110);
	}
	return retstr;
  }

/*
 * Class:     jNetDev_jND_0005fNICList
 * Method:    os_nicMacaddress
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jNetDev_jND_1NICList_os_1nicMacaddress
  (JNIEnv *env, jclass class, jint nicno) {
	setup();

	char outstr[20];
	sprintf(outstr, "%02.2x:%02.2x:%02.2x:%02.2x:%02.2x:%02.2x", 
		(u_int)v_macaddress[nicno][0],
		(u_int)v_macaddress[nicno][1],
		(u_int)v_macaddress[nicno][2],
		(u_int)v_macaddress[nicno][3],
		(u_int)v_macaddress[nicno][4],
		(u_int)v_macaddress[nicno][5]);

	jstring retstr = (*env)->NewStringUTF(env, outstr);
	if(retstr == 0) {
		printf("jND_NICList: jni error: os_nicMacaddress: out of memory(111)\n");
		exit(111);
	}
	return retstr;
  }

#ifdef OSX
/**
 * Opens an adapter via bpf
 */
static unsigned long bpf_open() {
        int i, fd;
        char bpfname[sizeof "/dev/bpf000"];

        i = 0;
        while(1) {
                sprintf(bpfname, "/dev/bpf%d", i++);
                fd = open(bpfname, O_RDWR);
                if(fd < 0 && errno == EBUSY) continue;
                break;
        }
        return fd;
}
#endif /*OSX*/


/*
 * Class:     jNetDev_jND_0005fNIC
 * Method:    nic_open
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_jNetDev_jND_1NIC_nic_1open
  (JNIEnv *env, jclass class, jstring jname)
  {
	jbyte* name = (jbyte*)(*env)->GetStringUTFChars(env, jname, 0);
	jbyteArray ret_msg;
#ifdef UNIX
	struct ifreq ifreq_s;
	unsigned long v_adapter;
	char* msg;
#ifdef LINUX
	v_adapter = socket(PF_INET, SOCK_PACKET, htons((uint16_t)ETH_P_ALL));
#endif /*LINUX*/

#ifdef OSX
	v_adapter = bpf_open();
#endif /*OSX*/

	if(v_adapter < 0)  {
		char* msg = "jND_NIC:open : Cannot open adapter.";
		ret_msg = message(env, 0, 0, msg, strlen(msg));
	}
	else {
#ifdef LINUX
		memset(&ifreq_s, 0, sizeof(ifreq_s));
		strncpy(ifreq_s.ifr_name, name, sizeof(ifreq_s.ifr_name)-1);
		ifreq_s.ifr_name[sizeof(ifreq_s.ifr_name)-1] = '\0';
		if(ioctl(v_adapter, SIOCGIFHWADDR, &ifreq_s) < 0) {
			char* msg = "jND_NIC:open : ioctl on adapter failed.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
		}
		else {
			switch(ifreq_s.ifr_hwaddr.sa_family) {
			case ARPHRD_ETHER:
			case ARPHRD_METRICOM:
			case ARPHRD_LOOPBACK:
				ret_msg = message(env, (void*)v_adapter, sizeof(void*), "", 0);
				break;
			default:
				msg = "jND_NIC:open : Unsupported link layer.";
				ret_msg = message(env, 0, 0, msg, strlen(msg));
			}
		}
#endif /*LINUX*/

#ifdef OSX
		struct bpf_version bpf_ver;
		u_int disable_auto_MAC = 1;
		int ok = 1;

		// Retrieve bpf version
		if(ioctl(v_adapter, BIOCVERSION, (caddr_t)&bpf_ver) < 0) {
			close(v_adapter); v_adapter = 0;
			char* msg = "jND_NIC:open : Cannot retrieve bpf version.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
			ok = 0;
		}

		// Check for bad filter
		if(ok && (bpf_ver.bv_major != BPF_MAJOR_VERSION ||
		    bpf_ver.bv_minor < BPF_MINOR_VERSION)) {
			close(v_adapter); v_adapter = 0;
			char* msg = "jND_NIC:open : Cannot bpf filter failure.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
			ok = 0;
		}

		if(ok) {
			// Hook interface to bpf device
			memset(&ifreq_s, 0, sizeof(ifreq_s));
			strncpy(ifreq_s.ifr_name, name, sizeof(ifreq_s.ifr_name)-1);
			ifreq_s.ifr_name[sizeof(ifreq_s.ifr_name)-1] = '\0';
			if(ioctl(v_adapter, BIOCSETIF, (caddr_t)&ifreq_s) < 0) {
				close(v_adapter); v_adapter = 0;
				char* msg = "jND_NIC:open : Cannot bind to device.";
				ret_msg = message(env, 0, 0, msg, strlen(msg));
				ok = 0;
			}
		}

		// Disable auto filling ethernet address
		if(ok && ioctl(v_adapter, BIOCGHDRCMPLT, &disable_auto_MAC) < 0) {
			close(v_adapter); v_adapter = 0;
			char* msg = "jND_NIC:open : Cannot disable autofill of MAC.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
			ok = 0;
		}
		else {  // At this point, we are all set, return the adapter
			ret_msg = message(env, (void*)v_adapter, sizeof(void*), "", 0);
		}

#endif /*OSX*/

	}
#endif /*UNIX*/

#ifdef WIN32
	void* v_adapter = 0;
	NetType IFType;
	int nlen = strlen(name);
	char *adapName = (char*)malloc(nlen+1);
	memcpy(adapName, name, nlen);
	adapName[nlen] = 0;
	v_adapter = (void*)PacketOpenAdapter((LPTSTR)(adapName));
	free(adapName);
	adapName = 0;

	if(v_adapter == 0
		&& ((LPADAPTER)v_adapter)->hFile == INVALID_HANDLE_VALUE) {
		free(((LPADAPTER)v_adapter));
		v_adapter = (void*)0;
		char* msg = "jND_NIC.open : Cannot open adapter.";
		ret_msg = message(env, 0, 0, msg, strlen(msg));
	}
	else {
		PacketSetBuff(((LPADAPTER)v_adapter), 512000);
		PacketGetNetType(((LPADAPTER)v_adapter), &IFType);
		if(IFType.LinkType != NdisMedium802_3) {
			PacketCloseAdapter(((LPADAPTER)v_adapter));
			v_adapter = (void*)0;
			char* msg = "jND_NIC.open : Medium not supported.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
		}
		if(!PacketSetNumWrites(((LPADAPTER)v_adapter), 1)) {
			char* msg = "jND_NIC.open : Cannot set NumWrites.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
		}
		ret_msg = message(env, v_adapter, sizeof(void*), "", 0);
	}
        /* ========== END WIN32 IMPLEMENTATION ========== */
#endif /* WIN32 */
	(*env)->ReleaseStringUTFChars(env, jname, NULL);
	return ret_msg;
  }

/*
 * Class:     jNetDev_jND_0005fNIC
 * Method:    nic_close
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_jNetDev_jND_1NIC_nic_1close
  (JNIEnv *env, jclass class, jbyteArray jadapter) {
	void* v_adapter = getPtr(env, jadapter);
#ifdef UNIX
	close(v_adapter);
#endif /* UNIX */
#ifdef WIN32
        /* ========== START WIN32 IMPLEMENTATION ========== */
	// Close the adapter
	if(((LPADAPTER)v_adapter) != 0) {
		PacketSetHwFilter(((LPADAPTER)v_adapter), NDIS_PACKET_TYPE_ALL_LOCAL);
		PacketCloseAdapter(((LPADAPTER)v_adapter));
		v_adapter = (void*)0;
	}
        /* ========== END WIN32 IMPLEMENTATION ========== */
#endif /* WIN32 */
  }

/*
 * Class:     jNetDev_jND_0005fNIC
 * Method:    nic_inject
 * Signature: ([B[BI)V
 */
JNIEXPORT jbyteArray JNICALL Java_jNetDev_jND_1NIC_nic_1inject
  (JNIEnv *env, jclass class , jbyteArray jpkt, jbyteArray jadapter, jint nicno) {
	void* v_adapter = getPtr(env, jadapter);
	jbyteArray ret_msg;
	jbyte* pkt = (*env)->GetByteArrayElements(env, jpkt, NULL);
	int size = (*env)->GetArrayLength(env, jpkt);
#ifdef UNIX
	int c;
#ifdef LINUX
	struct sockaddr addr;
	struct ifreq ifreq_s;
	memset(&addr, 0, sizeof(addr));
	strncpy(addr.sa_data, v_name[nicno], sizeof(addr.sa_data));
	c = sendto((int)v_adapter, pkt, size, 0, (struct sockaddr*)(&addr), sizeof(addr));
	if(c < 0) {
		char *msg = "jND_NIC.inject: sendto failed.";
		ret_msg = message(env, 0, 0, msg, strlen(msg));
	}
	else
		ret_msg = message(env, (void*)1, 1, "", 0);
#endif /*LINUX*/

#ifdef OSX
                c = write(v_adapter, pkt, size);
                if(c < 0) {
			char *msg = "jND_NIC.inject: write failed.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
		}
                else
			ret_msg = message(env, (void*)1, 1, "", 0);
#endif /*OSX*/

#endif /*UNIX*/

#ifdef WIN32
        /* ========== START WIN32 IMPLEMENTATION ========== */
        { // start a block so we can declare new variables
	LPPACKET packet;
	// Write the packet
	if((packet = PacketAllocatePacket()) == 0) {
		char *msg = "jND_NIC.inject : Cannot allocate space for packet.";
		ret_msg = message(env, 0, 0, msg, strlen(msg));
	}
	else {
		int sz = size;
		char* raw = (char*) malloc(sz);
		memcpy(raw, pkt, sz);
		PacketInitPacket(packet, raw, sz);
		if(!PacketSendPacket(((LPADAPTER)v_adapter), packet, FALSE)) {
			PacketFreePacket(packet);
			free(raw);
			char* msg = "jND_NIC.inject : Cannot send packet.";
			ret_msg = message(env, 0, 0, msg, strlen(msg));
		}
		PacketFreePacket(packet);
		free(raw);
		ret_msg = message(env, (void*)1, 1, "", 0);
	}

        } // End block
        /* ========== END WIN32 IMPLEMENTATION ========== */
#endif /* WIN32 */
	(*env)->ReleaseByteArrayElements(env, jpkt, pkt, 0);
	return ret_msg;
  }

