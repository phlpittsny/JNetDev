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
package jNetDev;
import jNetDev.Headers.*;

/**
 * jND_Utility <br>
 * This is a class of all static methods that provide a variety of utility
 * functions.
 * <br><br>
 * @author	Pete Lutz
 */

public class jND_Utility {
	private static boolean debugFlag = true;

        /** Static counter of arps **/
	private static int arpNo = 0;
	
	/**
	 * Method to print debug info to stdout. Printing only occurs if
	 * the static int debugFlag is non-zero. Use the debugSet() method
	 * to set the value of debugFlag.
	 * @param		message - the string to be printed.
	 * @author	Pete LUtz
	 */
	public static void debug(String message) {
		if(debugFlag) System.err.println(message);;
	}
	
	/**
	 * Method to set the value of debugFlag. This flag turns debug
	 * messages printed via debug on or off. By default they are on.
	 * Passing in a non-zero value turns debugging on, while a
	 * zero turns it off.
	 * 
	 * @param	value - the new value of debugFlag
	 * @author Pete Lutz
	 */
	public static void debugSet(boolean value) {
		debugFlag = value;
	}
	
	/**
	 * Method to convert a 1-byte integer to a 2-digit hexadecimal string.
	 * @param		byte1 - the 1-byte integer to be converted.
	 * @return		a String containing the hex byte.
	 * @author	Pete Lutz
	 */
	public static String byte1ToHex(jND_BYTE1 byte1) throws jND_Exception {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		try {
			char[] buf = new char[2];

			byte b = byte1.toByte();
			int nib0 = ((int) b) & 0xf;
			int nib1 = (((int) b) >> 4) & 0xf;
			
			buf[0] = hex[nib1];
			buf[1] = hex[nib0];
			return new String(buf);
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.byte1ToHex : " + e.toString());
		}
	}
	
	/**
	 * Method to convert a 4-byte integer to a hexadecimal string. 
	 * Actually, the 'len' right most bytes of the 4-byte 
	 * integer are converted.
	 * 
	 * @param		byte4 - the 4-byte value to be converted.
	 * @param		len - the # of bytes to actually convert.
	 * @return		a String containing the hex value in 2*len digits.
	 * @author	Pete Lutz
	 */
	public static String byte4ToHex(jND_BYTE4 byte4, int len) {
	    try {
	        String retStr = "";

	        int b4 = byte4.toInt();
	        for(int i = 0; i < len; i++) {
	            byte b = (byte)(b4 & 0xff);
	            b4 = b4 >> 8;
				String tmp = jND_Utility.byte1ToHex(new jND_BYTE1(b));
	            tmp = tmp + retStr;
				retStr = tmp;
	        }
	        return retStr;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.byte4ToHex : " + e.toString());
		}
	}
	
	/**
	 * Method to convert an 8-byte integer to a hexadecimal string. 
	 * Actually, the 'len' right most bytes of the 8-byte integer 
	 * are converted.
	 * 
	 * @param		byte8 - the 8-byte value to be converted.
	 * @param		len - the # of bytes to actually convert.
	 * @return		a String containing the hex value in 2*len digits.
	 * @author	Pete Lutz
	 */
	public static String byte8ToHex(jND_BYTE8 byte8, int len)  {
	    try {
	        String retStr = "";

	        long b8 = byte8.toLong();
	        for(int i = 0; i < len; i++) {
	            byte b = (byte)(b8 & 0xff);
	            b8 = b8 >> 8;
				String tmp = jND_Utility.byte1ToHex(new jND_BYTE1(b));
	            tmp = tmp + retStr;
				retStr = tmp;
	        }
	        return retStr;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.byte8ToHex : " + e.toString());
		}
	} 

	/**
	 * Method to convert an EthernetII frame type field to a printable string.
	 * @param		type - the type field to convert.
	 * @return		a String with the name of the type.
	 * @author	Pete Lutz
	 */
	public static String ethTypeToString(jND_BYTE2 type) {
		try {
			int numbers[] = {
				1536, 1632, 1633, 2048, 2049, 
				2050, 2051, 2052, 2053, 2054, 
				2055, 2056, 2076, 2184, 2185, 
				2186, 2304, 2560, 2561, 2989, 
				2990, 2991, 4096, 4097, 4098, 
				4099, 4100, 4101, 4102, 4103, 
				4104, 4105, 4106, 4107, 4108, 
				4109, 4110, 4111, 5632, 16962, 
				21000, 24576, 24577, 24578, 24579, 
				24580, 24581, 24582, 24583, 24584, 
				24585, 24592, 24593, 24594, 24595, 
				24596, 25944, 25945, 28672, 28674, 
				28704, 28705, 28706, 28707, 28708, 
				28709, 28710, 28711, 28712, 28713, 
				28720, 28724, 32771, 32772, 32773, 
				32774, 32776, 32784, 32787, 32788, 
				32789, 32790, 32793, 32814, 32815, 
				32821, 32822, 32824, 32825, 32826, 
				32827, 32828, 32829, 32830, 32831, 
				32832, 32833, 32834, 32836, 32838, 
				32839, 32841, 32859, 32860, 32861, 
				32864, 32866, 32869, 32870, 32871, 
				32872, 32873, 32874, 32876, 32877, 
				32878, 32879, 32880, 32881, 32882, 
				32883, 32884, 32885, 32886, 32887, 
				32890, 32891, 32892, 32893, 32894, 
				32895, 32896, 32897, 32898, 32899, 
				32923, 32924, 32925, 32926, 32927, 
				32931, 32932, 32933, 32934, 32935, 
				32936, 32937, 32938, 32939, 32940, 
				32941, 32942, 32943, 32944, 32945, 
				32946, 32947, 32960, 32964, 32965, 
				32966, 32967, 32968, 32969, 32970, 
				32971, 32972, 32973, 32974, 32975, 
				32976, 32977, 32978, 32979, 32980, 
				32981, 32989, 32990, 32991, 32992, 
				32993, 32994, 32995, 32996, 32997, 
				32998, 32999, 33000, 33001, 33002, 
				33003, 33004, 33005, 33006, 33007, 
				33008, 33010, 33011, 33012, 33013, 
				33015, 33023, 33024, 33025, 33026, 
				33027, 33031, 33032, 33033, 33072, 
				33073, 33074, 33075, 33076, 33077, 
				33078, 33079, 33080, 33081, 33082, 
				33083, 33084, 33085, 33096, 33097, 
				33098, 33100, 33101, 33102, 33103, 
				33104, 33105, 33106, 33107, 33116, 
				33117, 33118, 33124, 33125, 33126, 
				33149, 33150, 33152, 33153, 33154, 
				33155, 33156, 33157, 33158, 33159, 
				33160, 33161, 33162, 33163, 33164, 
				33165, 33178, 33179, 33180, 33181, 
				33182, 33183, 33184, 33185, 33186, 
				33187, 33188, 33189, 33190, 33191, 
				33192, 33193, 33194, 33195, 33196, 
				33197, 33198, 33207, 33208, 33209, 
				33228, 33229, 33230, 33231, 33232, 
				33233, 33234, 33235, 33236, 33237, 
				33238, 33239, 33240, 33241, 33242, 
				33243, 33244, 33245, 33254, 33255, 
				33256, 33257, 33258, 33259, 33260, 
				33261, 33262, 33263, 33264, 33265, 
				33266, 33267, 33268, 33269, 33270, 
				33271, 33272, 33283, 33284, 33285, 
				33313, 33314, 33342, 33343, 33344, 
				33407, 33408, 33409, 33410, 33379, 
				33380, 33381, 33382, 33383, 33384, 
				33385, 33386, 33434, 33435, 33436, 
				33437, 33438, 33439, 33440, 33441, 
				33442, 33443, 33444, 33445, 33446, 
				33447, 33448, 33449, 33450, 33451, 
				33452, 33453, 33454, 33455, 33456, 
				33457, 33458, 33459, 33460, 33461, 
				33462, 33463, 33464, 33465, 33466, 
				33467, 33468, 33469, 33470, 33471, 
				33472, 33473, 33474, 33475, 33476, 
				33477, 33478, 33479, 33480, 33481, 
				33482, 33483, 33484, 33485, 33486, 
				33487, 33488, 33489, 33490, 33491, 
				33492, 33493, 33494, 33495, 33496, 
				33497, 33498, 33499, 33500, 33501, 
				33502, 33503, 33504, 33505, 33506, 
				33507, 33508, 33509, 33510, 33511, 
				33512, 33513, 33514, 33515, 33516, 
				33517, 33518, 33519, 33520, 33521, 
				33522, 33523, 33524, 33525, 33526, 
				33527, 33528, 33529, 33530, 33531, 
				33532, 33533, 33534, 33535, 33536, 
				33537, 33538, 33539, 33540, 33541, 
				33542, 33543, 33544, 33545, 33546, 
				33547, 33548, 33549, 33550, 33551, 
				33552, 33553, 33554, 33555, 33556, 
				33557, 33558, 33559, 33560, 33561, 
				33562, 33563, 33564, 33565, 33566, 
				33567, 33568, 33569, 33570, 33571, 
				33572, 33573, 33574, 33575, 33576, 
				33577, 33578, 33579, 33580, 33581, 
				33582, 33583, 33584, 33585, 33586, 
				33587, 33588, 33589, 33590, 33591, 
				33592, 33593, 33594, 33595, 33596, 
				33597, 33598, 33599, 33600, 33601, 
				33602, 33603, 33604, 33605, 33606, 
				33607, 33608, 33609, 33610, 33611, 
				33612, 33613, 33614, 33615, 33616, 
				33617, 33618, 33619, 33620, 33621, 
				33622, 33623, 33624, 33625, 33626, 
				33627, 33628, 33629, 33630, 33631, 
				33632, 33633, 33634, 33635, 33636, 
				33637, 33638, 33639, 33640, 33641, 
				33642, 33643, 33644, 33645, 33646, 
				33647, 33648, 33649, 33650, 33651, 
				33652, 33653, 33654, 33655, 33656, 
				33657, 33658, 33659, 33660, 33661, 
				33662, 33663, 33664, 33665, 33666, 
				33667, 33668, 33669, 33670, 33671, 
				33672, 33673, 33674, 33675, 33676, 
				33677, 33678, 33679, 33680, 33681, 
				33682, 33683, 33684, 33685, 33686, 
				33687, 33688, 33689, 33690, 33691, 
				33692, 33693, 33694, 33695, 33696, 
				33697, 33698, 33699, 33700, 33701, 
				33702, 33703, 33704, 33705, 33706, 
				33707, 33708, 33709, 33710, 33711, 
				33712, 33713, 33714, 33715, 33716, 
				33717, 33718, 33719, 33720, 33721, 
				33722, 33723, 33724, 33725, 33726, 
				33727, 33728, 33729, 33730, 33731, 
				33732, 33733, 33734, 33735, 33736, 
				33737, 33738, 33739, 33740, 33741, 
				33742, 33743, 33744, 33745, 33746, 
				33747, 33748, 33749, 33750, 33751, 
				33752, 33753, 33754, 33755, 33756, 
				33757, 33758, 33759, 33760, 33761, 
				33762, 33763, 33764, 33765, 33766, 
				33767, 33768, 33769, 33770, 33771, 
				33772, 33773, 33774, 33775, 33776, 
				33777, 33778, 33779, 33780, 33781, 
				33782, 33783, 33784, 33785, 33786, 
				33787, 33788, 33789, 33790, 33791, 
				33792, 33793, 33794, 33795, 33796, 
				33797, 33798, 33799, 33800, 33801, 
				33802, 33803, 33804, 33805, 33806, 
				33807, 33808, 33809, 33810, 33811, 
				33812, 33813, 33814, 33815, 33816, 
				33817, 33818, 33819, 33820, 33821, 
				33822, 33823, 33824, 33825, 33826, 
				33827, 33828, 33829, 33830, 33831, 
				33832, 33833, 33834, 33835, 33836, 
				33837, 33838, 33839, 33840, 33841, 
				33842, 33843, 33844, 33845, 33846, 
				33847, 33848, 33849, 33850, 33851, 
				33852, 33853, 33854, 33855, 33856, 
				33857, 33858, 33859, 33860, 33861, 
				33862, 33863, 33864, 33865, 33866, 
				33867, 33868, 33869, 33870, 33871, 
				33872, 33873, 33874, 33875, 33876, 
				33877, 33878, 33879, 33880, 33881, 
				33882, 33883, 33884, 33885, 33886, 
				33887, 33888, 33889, 33890, 33891, 
				33892, 33893, 33894, 33895, 33896, 
				33897, 33898, 33899, 33900, 33901, 
				33902, 33903, 33904, 33905, 33906, 
				33907, 33908, 33909, 33910, 33911, 
				33912, 33913, 33914, 33915, 33916, 
				33917, 33918, 33919, 33920, 33921, 
				33922, 33923, 33924, 33925, 33926, 
				33927, 33928, 33929, 33930, 33931, 
				33932, 33933, 33934, 33935, 33936, 
				33937, 33938, 33939, 33940, 33941, 
				33942, 33943, 33944, 33945, 33946, 
				33947, 33948, 33949, 33950, 33951, 
				33952, 33953, 33954, 33955, 33956, 
				33957, 33958, 33959, 33960, 33961, 
				33962, 33963, 33964, 33965, 33966, 
				33967, 33968, 33969, 33970, 33971, 
				33972, 33973, 33974, 33975, 33976, 
				33977, 33978, 33979, 33980, 33981, 
				33982, 33983, 33984, 33985, 33986, 
				33987, 33988, 33989, 33990, 33991, 
				33992, 33993, 33994, 33995, 33996, 
				33997, 33998, 33999, 34000, 34001, 
				34002, 34003, 34004, 34005, 34006, 
				34007, 34008, 34009, 34010, 34011, 
				34012, 34013, 34014, 34015, 34016, 
				34017, 34018, 34019, 34020, 34021, 
				34022, 34023, 34024, 34025, 34026, 
				34027, 34028, 34029, 34030, 34031, 
				34032, 34033, 34034, 34035, 34036, 
				34037, 34038, 34039, 34040, 34041, 
				34042, 34043, 34044, 34045, 34046, 
				34047, 34048, 34049, 34050, 34051, 
				34052, 34053, 34054, 34055, 34056, 
				34057, 34058, 34059, 34060, 34061, 
				34062, 34063, 34064, 34065, 34066, 
				34067, 34068, 34069, 34070, 34071, 
				34072, 34073, 34074, 34075, 34076, 
				34077, 34078, 34079, 34080, 34081, 
				34082, 34083, 34084, 34085, 34086, 
				34087, 34088, 34089, 34090, 34091, 
				34092, 34093, 34094, 34095, 34096, 
				34097, 34098, 34099, 34100, 34101, 
				34102, 34103, 34104, 34105, 34106, 
				34107, 34108, 34109, 34110, 34111, 
				34112, 34113, 34114, 34115, 34116, 
				34117, 34118, 34119, 34120, 34121, 
				34122, 34123, 34124, 34125, 34126, 
				34127, 34128, 34129, 34130, 34131, 
				34132, 34133, 34134, 34135, 34136, 
				34137, 34138, 34139, 34140, 34141, 
				34142, 34143, 34144, 34145, 34146, 
				34147, 34148, 34149, 34150, 34151, 
				34152, 34153, 34154, 34155, 34156, 
				34157, 34158, 34159, 34160, 34161, 
				34162, 34163, 34164, 34165, 34166, 
				34167, 34168, 34169, 34170, 34171, 
				34172, 34173, 34174, 34175, 34176, 
				34177, 34178, 34179, 34180, 34181, 
				34182, 34183, 34184, 34185, 34186, 
				34187, 34188, 34189, 34190, 34191, 
				34192, 34193, 34194, 34195, 34196, 
				34197, 34198, 34199, 34200, 34201, 
				34202, 34203, 34204, 34205, 34206, 
				34207, 34208, 34209, 34210, 34211, 
				34212, 34213, 34214, 34215, 34216, 
				34217, 34218, 34219, 34220, 34221, 
				34222, 34223, 34224, 34225, 34226, 
				34227, 34228, 34229, 34230, 34231, 
				34232, 34233, 34234, 34235, 34236, 
				34237, 34238, 34239, 34240, 34241, 
				34242, 34243, 34244, 34245, 34246, 
				34247, 34248, 34249, 34250, 34251, 
				34252, 34253, 34254, 34255, 34256, 
				34257, 34258, 34259, 34260, 34261, 
				34262, 34263, 34264, 34265, 34266, 
				34267, 34268, 34269, 34270, 34271, 
				34272, 34273, 34274, 34275, 34276, 
				34277, 34278, 34279, 34280, 34281, 
				34282, 34283, 34284, 34285, 34286, 
				34287, 34288, 34289, 34290, 34291, 
				34292, 34293, 34294, 34295, 34296, 
				34297, 34298, 34299, 34300, 34301, 
				34302, 34303, 34304, 34305, 34306, 
				34307, 34308, 34309, 34310, 34311, 
				34312, 34313, 34314, 34315, 34316, 
				34317, 34318, 34319, 34320, 34321, 
				34322, 34323, 34324, 34325, 34326, 
				34327, 34328, 34329, 34330, 34331, 
				34332, 34333, 34334, 34335, 34336, 
				34337, 34338, 34339, 34340, 34341, 
				34342, 34343, 34344, 34345, 34346, 
				34347, 34348, 34349, 34350, 34351, 
				34352, 34353, 34354, 34355, 34356, 
				34357, 34358, 34359, 34360, 34361, 
				34362, 34363, 34364, 34365, 34366, 
				34367, 34368, 34369, 34370, 34371, 
				34372, 34373, 34374, 34375, 34376, 
				34377, 34378, 34379, 34380, 34381, 
				34382, 34383, 34384, 34385, 34386, 
				34387, 34388, 34389, 34390, 34391, 
				34392, 34393, 34394, 34395, 34396, 
				34397, 34398, 34399, 34400, 34401, 
				34402, 34403, 34404, 34405, 34406, 
				34407, 34408, 34409, 34410, 34411, 
				34412, 34413, 34414, 34415, 34416, 
				34417, 34418, 34419, 34420, 34421, 
				34422, 34423, 34424, 34425, 34426, 
				34427, 34428, 34429, 34430, 34431, 
				34432, 34433, 34434, 34435, 34436, 
				34437, 34438, 34439, 34440, 34441, 
				34442, 34443, 34444, 34445, 34446, 
				34447, 34448, 34449, 34450, 34451, 
				34452, 34453, 34454, 34455, 34456, 
				34457, 34458, 34459, 34460, 34461, 
				34462, 34463, 34464, 34465, 34467, 
				34468, 34469, 34470, 34471, 34472, 
				34473, 34474, 34475, 34476, 34523, 
				34526, 34525, 34527, 34528, 34529, 
				34530, 34531, 34532, 34533, 34534, 
				34535, 34536, 34537, 34538, 34539, 
				34540, 34541, 34542, 34543, 34560, 
				34561, 34562, 34563, 34564, 34565, 
				34566, 34567, 34568, 34569, 34570, 
				34571, 34572, 34573, 34574, 34575, 
				34576, 34667, 34668, 34669, 34827, 
				34887, 34888, 35478, 35479, 36864, 
				36865, 36866, 36867, 65280, 65280, 
				65281, 65282, 65283, 65284, 65285, 
				65286, 65287, 65288, 65289, 65290, 
				65291, 65292, 65293, 65294, 65295, 
				65535
			};

			String names[] = {
				"XEROX NS IDP", "DLOG", 
				"DLOG", "Internet IP (IPv4)", 
				"X.75 Internet", "NBS Internet", 
				"ECMA Internet", "Chaosnet", 
				"X.25 Level 3", "ARP", 
				"XNS Compatability", "Frame Relay ARP", 
				"Symbolics Private", "Xyplex", 
				"Xyplex", "Xyplex", 
				"Ungermann-Bass net debugr", "Xerox IEEE802.3 PUP", 
				"PUP Addr Trans", "Banyan VINES", 
				"VINES Loopback", "VINES Echo", 
				"Berkeley Trailer nego", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Berkeley Trailer encap/IP", "Berkeley Trailer encap/IP", 
				"Valid Systems", "PCS Basic Block Protocol", 
				"BBN Simnet", "DEC Unassigned (Exp.)", 
				"DEC MOP Dump/Load", "DEC MOP Remote Console", 
				"DEC DECNET Phase IV Route", "DEC LAT", 
				"DEC Diagnostic Protocol", "DEC Customer Protocol", 
				"DEC LAVC, SCA", "DEC Unassigned", 
				"DEC Unassigned", "3Com Corporation", 
				"3Com Corporation", "3Com Corporation", 
				"3Com Corporation", "3Com Corporation", 
				"Trans Ether Bridging", "Raw Frame Relay", 
				"Ungermann-Bass download", "Ungermann-Bass dia/loop", 
				"LRT", "LRT", 
				"LRT", "LRT", 
				"LRT", "LRT", 
				"LRT", "LRT", 
				"LRT", "LRT", 
				"Proteon", "Cabletron", 
				"Cronus VLN", "Cronus Direct", 
				"HP Probe", "Nestar", 
				"AT&T", "Excelan", 
				"SGI diagnostics", "SGI network games", 
				"SGI reserved", "SGI bounce server", 
				"Apollo Domain", "Tymshare", 
				"Tigan, Inc.", "Reverse ARP", 
				"Aeonic Systems", "DEC LANBridge", 
				"DEC Unassigned", "DEC Unassigned", 
				"DEC Unassigned", "DEC Unassigned", 
				"DEC Ethernet Encryption", "DEC Unassigned", 
				"DEC LAN Traffic Monitor", "DEC Unassigned", 
				"DEC Unassigned", "DEC Unassigned", 
				"Planning Research Corp.", "AT&T", 
				"AT&T", "ExperData", 
				"Stanford V Kernel exp.", "Stanford V Kernel prod.", 
				"Evans & Sutherland", "Little Machines", 
				"Counterpoint Computers", "Univ. of Mass. @ Amherst", 
				"Univ. of Mass. @ Amherst", "Veeco Integrated Auto.", 
				"General Dynamics", "AT&T", 
				"Autophon", "ComDesign", 
				"Computgraphic Corp.", "Landmark Graphics Corp.", 
				"Landmark Graphics Corp.", "Landmark Graphics Corp.", 
				"Landmark Graphics Corp.", "Landmark Graphics Corp.", 
				"Landmark Graphics Corp.", "Landmark Graphics Corp.", 
				"Landmark Graphics Corp.", "Landmark Graphics Corp.", 
				"Landmark Graphics Corp.", "Matra", 
				"Dansk Data Elektronik", "Merit Internodal", 
				"Vitalink Communications", "Vitalink Communications", 
				"Vitalink Communications", "Vitalink TransLAN III", 
				"Counterpoint Computers", "Counterpoint Computers", 
				"Counterpoint Computers", "Appletalk", 
				"Datability", "Datability", 
				"Datability", "Spider Systems Ltd.", 
				"Nixdorf Computers", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "Siemens Gammasonics Inc.", 
				"Siemens Gammasonics Inc.", "DCA Data Exchange Cluster", 
				"Banyan Systems", "Banyan Systems", 
				"Pacer Software", "Applitek Corporation", 
				"Intergraph Corporation", "Intergraph Corporation", 
				"Intergraph Corporation", "Intergraph Corporation", 
				"Intergraph Corporation", "Harris Corporation", 
				"Harris Corporation", "Taylor Instrument", 
				"Taylor Instrument", "Taylor Instrument", 
				"Taylor Instrument", "Rosemount Corporation", 
				"Rosemount Corporation", "IBM SNA Service on Ether", 
				"Varian Associates", "Integrated Solutions TRFS", 
				"Integrated Solutions TRFS", "Allen-Bradley", 
				"Allen-Bradley", "Allen-Bradley", 
				"Allen-Bradley", "Datability", 
				"Datability", "Datability", 
				"Datability", "Datability", 
				"Datability", "Datability", 
				"Datability", "Datability", 
				"Datability", "Datability", 
				"Datability", "Datability", 
				"Retix", "AppleTalk AARP (Kinetics)", 
				"Kinetics", "Kinetics", 
				"Apollo Computer", "Wellfleet Communications", 
				"Wellfleet Communications", "Wellfleet Communications", 
				"Wellfleet Communications", "Wellfleet Communications", 
				"Symbolics Private", "Symbolics Private", 
				"Symbolics Private", "Hayes Microcomputers", 
				"VG Laboratory Systems", "Bridge Communications", 
				"Bridge Communications", "Bridge Communications", 
				"Bridge Communications", "Bridge Communications", 
				"Novell, Inc.", "Novell, Inc.", 
				"KTI", "KTI", 
				"KTI", "KTI", 
				"KTI", "Logicraft", 
				"Network Computing Devices", "Alpha Micro", 
				"SNMP", "BIIN", 
				"BIIN", "Technically Elite Concept", 
				"Rational Corp", "Qualcomm", 
				"Qualcomm", "Qualcomm", 
				"Computer Protocol Pty Ltd", "Computer Protocol Pty Ltd", 
				"Computer Protocol Pty Ltd", "Charles River Data System", 
				"Charles River Data System", "Charles River Data System", 
				"XTP   ", "SGI/Time Warner prop.", 
				"HIPPI-FP encapsulation", "STP, HIPPI-ST", 
				"Reserved for HIPPI-6400", "Reserved for HIPPI-6400", 
				"Silicon Graphics prop.", "Silicon Graphics prop.", 
				"Silicon Graphics prop.", "Silicon Graphics prop.", 
				"Silicon Graphics prop.", "Silicon Graphics prop.", 
				"Silicon Graphics prop.", "Silicon Graphics prop.", 
				"Silicon Graphics prop.", "Motorola Computer", 
				"Qualcomm", "Qualcomm", 
				"Qualcomm", "Qualcomm", 
				"Qualcomm", "Qualcomm", 
				"Qualcomm", "Qualcomm", 
				"Qualcomm", "Qualcomm", 
				"ARAI Bunkichi", "RAD Network Devices", 
				"RAD Network Devices", "RAD Network Devices", 
				"RAD Network Devices", "RAD Network Devices", 
				"RAD Network Devices", "RAD Network Devices", 
				"RAD Network Devices", "RAD Network Devices", 
				"RAD Network Devices", "Xyplex", 
				"Xyplex", "Xyplex", 
				"Apricot Computers", "Apricot Computers", 
				"Apricot Computers", "Apricot Computers", 
				"Apricot Computers", "Apricot Computers", 
				"Apricot Computers", "Apricot Computers", 
				"Apricot Computers", "Apricot Computers", 
				"Artisoft", "Artisoft", 
				"Artisoft", "Artisoft", 
				"Artisoft", "Artisoft", 
				"Artisoft", "Artisoft", 
				"Polygon", "Polygon", 
				"Polygon", "Polygon", 
				"Polygon", "Polygon", 
				"Polygon", "Polygon", 
				"Polygon", "Polygon", 
				"Comsat Labs", "Comsat Labs", 
				"Comsat Labs", "SAIC", 
				"SAIC", "SAIC", 
				"VG Analytical", "VG Analytical", 
				"VG Analytical", "Quantum Software", 
				"Quantum Software", "Quantum Software", 
				"Ascom Banking Systems", "Ascom Banking Systems", 
				"Advanced Encryption System", "Advanced Encryption System", 
				"Advanced Encryption System", "Athena Programming", 
				"Athena Programming", "Athena Programming", 
				"Athena Programming", "Charles River Data System", 
				"Charles River Data System", "Charles River Data System", 
				"Charles River Data System", "Charles River Data System", 
				"Charles River Data System", "Charles River Data System", 
				"Charles River Data System", "Inst Ind Info Tech", 
				"Inst Ind Info Tech", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Taurus Controls", 
				"Taurus Controls", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Walker Richer & Quinn", 
				"Walker Richer & Quinn", "Idea Courier", 
				"Idea Courier", "Idea Courier", 
				"Idea Courier", "Idea Courier", 
				"Idea Courier", "Idea Courier", 
				"Idea Courier", "Idea Courier", 
				"Idea Courier", "Computer Network Tech", 
				"Computer Network Tech", "Computer Network Tech", 
				"Computer Network Tech", "Gateway Communications", 
				"Gateway Communications", "Gateway Communications", 
				"Gateway Communications", "Gateway Communications", 
				"Gateway Communications", "Gateway Communications", 
				"Gateway Communications", "Gateway Communications", 
				"Gateway Communications", "SECTRA", 
				"Delta Controls", "IPv6", 
				"ATOMIC", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Landis & Gyr Powers", 
				"Landis & Gyr Powers", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"Motorola", "Motorola", 
				"TCP/IP Compression", "IP Autonomous Systems", 
				"Secure Data", "PPP   ", 
				"MPLS Unicast  ", "MPLS Multicast", 
				"Invisible Software", "Invisible Software", 
				"Loopback", "3Com(Bridge) XNS Sys Mgmt", 
				"3Com(Bridge) TCP-IP Sys", "3Com(Bridge) loop detect", 
				"BBN VITAL-LanBridge cache", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "ISC Bunker Ramo", 
				"ISC Bunker Ramo", "Reserved", null
			};

			int tno = type.toInt();
			if(tno <= 1500) return "Length of 802.3 frame";

			int i = 0;
			while(names[i] != null) {
				if(numbers[i] == tno) return names[i];
				i++;
			}

			return "" + tno;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.ethTypeToString : " + e.toString());
		}
	}

	/**
	 * Method to convert an IPv4 protocol field to a printable string.
	 * @param		protocol - the protocol field to convert.
	 * @return		a String with the name of the protocol.
	 * @author	Pete Lutz
	 */
	public static String ipProtocolToString(jND_BYTE1 protocol) {
		try {
			String names[] = {
				"HOPOPT", "ICMP", "ICMP", "GGP", "IP",
				"SP", "TCP", "CBT", "EGP", "IGP", 
				"BBN-RCC-MON", "NVP-II", "PUP", "ARGUS","EMCON", 
				"XNET", "CHAOS", "UDP", "MUX", "DCN-MEAS", 
				"HMP", "PRM", "XNS-IDP", "TRUNK-1", "TRUNK-2", 
				"LEAF-1", "LEAF-2", "RDP", "IRTP", "ISO-TP4", 
				"NETBLT", "MFE-NSP", "MERIT-INP", "SEP", "3PC", 
				"IDPR", "XTP", "DDP", "IDPR-CMTP", "TP++",
				"IL", "IPv6", "SDRP", "IPv6-Route", "IPv6-Frag", 
				"IDRP", "RSVP", "GRE", "MHRP", "BNA", 
				"ESP", "AH", "I-NLSP", "SWIPE", "NARP",
				"MOBILE", "TLSP", "SKIP", "IPv6-ICMP", "IPv6-NoNxt", 
				"IPv6-Opts", "any-host-internal", "CFTP", "any-local-network", "SAT-EXPAK",
				"KRYPTOLAN", "RVD", "IPPC", "any-distr-FS", "SAT-MON", 
				"VISA", "IPCV", "CPNX", "CPHB", "WSN",
				"PVP", "BR-SAT-MON", "SUN-ND", "WB-MON", "WB-EXPAK",
				"ISO-IP", "VMTP", "SECURE-VMTP", "VINES", "TTP", 
				"NSFNET-IGP", "DGP", "TCF", "EIGRP", "OSPFIGP",
				"Sprite-RPC", "LARP", "MTP", "AX.25", "IPIP", 
				"MICP", "SCC-SP", "ETHERIP", "ENCAP", "any-private-encryption",
				"GMTP", "IFMP", "PNNI", "PIM", "ARIS", 
				"SCPS", "QNX", "A/N", "IPComp", "SNP",
				"Compaq-Peer", "IPX-in-IP", "VRRP", "PGM", "any 0-hop protocol", 
				"L2TP", "DDX", "IATP", "STP", "SRP",
				"UTI", "SMP", "SM", "PTP", "ISIS over IPv4", 
				"FIRE", "CRTP", "CRUDP", "SSCOPMCE", "IPLT",
				"SPS", "PIPE", "SCTP", "FC", "RSVP-E2E-IGNORE", 
				"Mobility Header", "UDPLite", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Unassigned", "Unassigned", 
				"Unassigned", "Unassigned", "Unassigned", "Use for experimentation and testing", 
				"Use for experimentation and testing", "Reserved", null};
			
			int pno = protocol.toInt();
			int size;
			for(size = 0; names[size] != null; size++) ;
			if(pno >= size) {
				return "Unassigned";
			}

			return names[pno];
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.ipProtocolToString : " + e.toString());
		}
	}

	/**
	 * Method to convert an UDP/TCP port field to the name of the service.
	 * @param		port - the port field to convert.
	 * @return		a String with the name of the service.
	 * @author	Pete Lutz
	 */
	public static String portToString(jND_BYTE2 port) {
		try {
			String names[] = {
				"reserved", "tcpmux", 
				"compressnet", "compressnet", 
				"unassigned", "RJE", "unassigned", "Echo", "unassigned", 
				"discard", "unassigned", "Systat", "unassigned", 
				"Daytime", "unassigned", "unassigned", "unassigned", "QOTD", 
				"msp", "Chargen", 
				"FTP-data", "FTP", 
				"SSH", "Telnet", 
				"private-mail", "SMTP", "unassigned", 
				"nsw-fe", "unassigned", "msg-icp", "unassigned", 
				"msg-auth", "unassigned", "DSP", "unassigned", 
				"private-print", "unassigned", "TIME", 
				"RAP", "RIP", "unassigned", 
				"graphics", "nameserver", 
				"nicname", "mpm-flags", 
				"mpm", "mpm-snd", 
				"ni-ftp", "auditd", 
				"TACACS", "re-mail-ck", 
				"la-maint", "xns-time", 
				"DNS", "xns-ch", 
				"isi-gl", "xns-auth", 
				"private-terminal", "xns-mail", 
				"private-file", "Unassigned", 
				"ni-mail", "acas", 
				"whois++", "covia", 
				"TACACS-ds", "sql*net", 
				"BOOTP server", "BOOTP client", 
				"TFTP", "Gopher", 
				"netrjs-1", "netrjs-2", 
				"netrjs-3", "netrjs-4", 
				"private-dial", "deos", 
				"private-rje", "vettcp", 
				"Finger", "HTTP", 
				"hosts2-ns", "xfer", 
				"mit-ml-dev", "ctf", 
				"mit-ml-dev", "mfcobol", 
				"private-link", "kerberos", 
				"su-mit-tg", "dnsix", 
				"mit-dov", "npp", 
				"dcp", "objcall", 
				"supdup", "dixie", 
				"swift-rvf", "tacnews", 
				"metagram", "newacct", 
				"hostname", "iso-tsap", 
				"gppitnp", "acr-nema", 
				"csnet-ns", "3com-tsmux", 
				"rtelnet", "snagas", 
				"Pop2", "Pop3", 
				"sunrpc", "mcidas", 
				"AUTH", "unassigned", "SFTP", 
				"ansanotify", "uucp-path", 
				"sqlserv", "nntp", 
				"cfdptkt", "erpc", 
				"smakynet", "NTP", 
				"ansatrader", "locus-map", 
				"nxedit", "locus-con", 
				"gss-xlicen", "pwdgen", 
				"cisco-fna", "cisco-tna", 
				"cisco-sys", "statsrv", 
				"ingres-net", "epmap", 
				"profile", "netbios-ns", 
				"netbios-dgm", "netbios-ssn", 
				"emfis-data", "emfis-cntl", 
				"bl-idm", "imap", 
				"uma", "uaac", 
				"iso-tp0", "iso-ip", 
				"jargon", "aed-512", 
				"sql-net", "hems", 
				"bftp", "sgmp", 
				"netsc-prod", "netsc-dev", 
				"sqlsrv", "knet-cmp", 
				"pcmail-srv", "nss-routing", 
				"sgmp-traps", "snmp", 
				"snmptrap", "cmip-man", 
				"cmip-agent", "xns-courier", 
				"s-net", "namp", 
				"rsvd", "send", 
				"print-srv", "multiplex", 
				"cl/1", "xyplex-mux", 
				"mailq", "vmnet", 
				"genrad-mux", "xdmcp", 
				"nextstep", "bgp", 
				"RIS", "unify", 
				"audit", "ocbinder", 
				"ocserver", "remote-kis", 
				"kis", "aci", 
				"mumps", "qft", 
				"gacp", "prospero", 
				"osu-nms", "srmp", 
				"IRC", "dn6-nlm-aud", 
				"dn6-smm-red", "dls", 
				"dls-mon", "smux", 
				"src", "at-rtmp", 
				"at-nbp", "at-3", 
				"at-echo", "at-5", 
				"at-zis", "at-7", 
				"at-8", "qmtp", 
				"z39.50", "914c/g", 
				"anet", "ipx", 
				"vmpwscs", "softpc", 
				"CAIlic", "dbase", 
				"mpp", "uarps", 
				"IMAP3", "fln-spx", 
				"rsh-spx", "cdc", 
				"masqdialer", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "direct", 
				"sur-meas", "inbusiness", 
				"link", "dsp3270", 
				"subntbcst_tftp", "bhfhs", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"RAP", "set", 
				"yak-chat", "esro-gen", 
				"openport", "nsiiops", 
				"arcisdms", "hdap", 
				"bgmp", "x-bone-ctl", 
				"sst", "td-service", 
				"td-replica", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "http-mgmt", 
				"personal-link", "cableport-ax", 
				"rescap", "corerjd", "unassigned", 
				"fxp", "k-block", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", 
				"novastorbakcup", "entrusttime", 
				"bhmds", "asip-webadmin", 
				"vslmp", "magenta-logic", 
				"opalis-robot", "dpsi", 
				"decauth", "zannet", 
				"pkix-timestamp", "ptp-event", 
				"ptp-general", "pip", 
				"rtsps", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "texar", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", 
				"pdap", "pawserv", 
				"zserv", "fatserv", 
				"csi-sgwp", "mftp", 
				"matip-type-a", "matip-type-b", 
				"dtag-ste-sb", "ndsauth", 
				"bh611", "datex-asn", 
				"cloanto-net-1", "bhevent", 
				"shrinkwrap", "nsrmp", 
				"scoi2odialog", "semantix", 
				"srssend", "rsvp_tunnel", 
				"aurora-cmgr", "dtk", 
				"odmr", "mortgageware", 
				"qbikgdp", "rpc2portmap", 
				"codaauth2", "clearcase", 
				"ulistproc", "legent-1", 
				"legent-2", "hassle", 
				"nip", "tnETOS", 
				"dsETOS", "is99c", 
				"is99s", "hp-collector", 
				"hp-managed-node", "hp-alarm-mgr", 
				"arns", "ibm-app", 
				"asa", "aurp", 
				"unidata-ldm", "ldap", 
				"uis", "synotics-relay", 
				"synotics-broker", "meta5", 
				"embl-ndt", "netcp", 
				"netware-ip", "mptn", 
				"kryptolan", "iso-tsap-c2", 
				"work-sol", "ups", 
				"genie", "decap", 
				"nced", "ncld", 
				"imsp", "timbuktu", 
				"prm-sm", "prm-nm", 
				"decladebug", "rmt", 
				"synoptics-trap", "smsp", 
				"infoseek", "bnet", 
				"silverplatter", "onmux", 
				"hyper-g", "ariel1", 
				"smpte", "ariel2", 
				"ariel3", "opc-job-start", 
				"opc-job-track", "icad-el", 
				"smartsdp", "svrloc", 
				"ocs_cmu", "ocs_amu", 
				"utmpsd", "utmpcd", 
				"iasd", "nnsp", 
				"mobileip-agent", "mobilip-mn", 
				"dna-cml", "comscm", 
				"dsfgw", "dasp", 
				"sgcp", "decvms-sysmgt", 
				"cvc_hostd", "https", 
				"snpp", "microsoft-ds", 
				"ddm-rdb", "ddm-dfm", 
				"ddm-ssl", "as-servermap", 
				"tserver", "sfs-smp-net", 
				"sfs-config", "creativeserver", 
				"contentserver", "creativepartnr", 
				"macon-tcp", "scohelp", 
				"appleqtc", "ampr-rcmd", 
				"skronk", "datasurfsrv", 
				"datasurfsrvsec", "alpes", 
				"kpasswd", "urd", 
				"digital-vrc", "mylex-mapd", 
				"photuris", "rcp", 
				"scx-proxy", "mondex", 
				"ljk-login", "hybrid-pop", 
				"tn-tl-w1", "tcpnethaspsrv", 
				"tn-tl-fd1", "ss7ns", 
				"spsc", "iafserver", 
				"iafdbase", "ph", 
				"bgs-nsi", "ulpnet", 
				"integra-sme", "powerburst", 
				"avian", "saft", 
				"gss-http", "nest-protocol", 
				"micom-pfs", "go-login", 
				"ticf-1", "ticf-2", 
				"pov-ray", "intecourier", 
				"pim-rp-disc", "dantz", 
				"siam", "iso-ill", 
				"isakmp", "stmf", 
				"asa-appl-proto", "intrinsa", 
				"citadel", "mailbox-lm", 
				"ohimsrv", "crs", 
				"xvttp", "snare", 
				"fcp", "passgo", 
				"exec", "login", 
				"shell", "printer", 
				"videotex", "talk", 
				"ntalk", "utime", 
				"efs", "ripng", 
				"ulp", "ibm-db2", 
				"ncp", "timed", 
				"tempo", "stx", 
				"custix", "irc-serv", 
				"courier", "conference", 
				"netnews", "netwall", 
				"mm-admin", "iiop", 
				"opalis-rdv", "nmsp", 
				"gdomap", "apertus-ldp", 
				"uucp", "uucp-rlogin", 
				"commerce", "klogin", 
				"kshell", "appleqtcsrvr", 
				"dhcpv6-client", "dhcpv6-server", 
				"afpovertcp", "idfp", 
				"new-rwho", "cybercash", 
				"devshr-nts", "pirp", 
				"rtsp", "dsf", 
				"remotefs", "openvms-sysipc", 
				"sdnskmp", "teedtap", 
				"rmonitor", "monitor", 
				"chshell", "nntps", 
				"9pfs", "whoami", 
				"streettalk", "banyan-rpc", 
				"ms-shuttle", "ms-rome", 
				"meter", "meter", 
				"sonar", "banyan-vip", 
				"ftp-agent", "vemmi", 
				"ipcd", "vnas", 
				"ipdd", "decbsrv", 
				"sntp-heartbeat", "bdp", 
				"scc-security", "philips-vc", 
				"keyserver", "imap4-ssl", 
				"password-chg", "submission", 
				"cal", "eyelink", 
				"tns-cml", "http-alt", 
				"eudora-set", "http-rpc-epmap", 
				"tpip", "cab-protocol", 
				"smsd", "ptcnameservice", 
				"sco-websrvrmg3", "acp", 
				"ipcserver", "syslog-conn", 
				"xmlrpc-beep", "idxp", 
				"tunnel", "soap-beep", 
				"urm", "nqs", 
				"sift-uft", "npmp-trap", 
				"npmp-local", "npmp-gui", 
				"hmmp-ind", "hmmp-op", 
				"sshell", "sco-inetmgr", 
				"sco-sysmgr", "sco-dtmgr", 
				"dei-icda", "compaq-evm", 
				"sco-websrvrmgr", "escp-ip", 
				"collaborator", "asf-rmcp", 
				"cryptoadmin", "dec_dlm", 
				"asia", "passgo-tivoli", 
				"qmqp", "3com-amp3", 
				"rda", "ipp", 
				"bmpp", "servstat", 
				"ginad", "rlzdbase", 
				"ldaps", "lanserver", 
				"mcns-sec", "msdp", 
				"entrust-sps", "repcmd", 
				"esro-emsdp", "sanity", 
				"dwr", "pssc", 
				"ldp", "dhcp-failover", 
				"rrp", "cadview-3d", 
				"obex", "ieee-mms", 
				"hello-port", "repscmd", 
				"aodv", "tinc", 
				"spmp", "rmc", 
				"tenfold", "unassigned", "mac-srvr-admin", 
				"hap", "pftp", 
				"purenoise", "asf-secure-rmcp", 
				"sun-dr", "mdqs", 
				"disclose", "mecomm", 
				"meregister", "vacdsm-sws", 
				"vacdsm-app", "vpps-qua", 
				"cimplex", "acap", 
				"dctp", "vpps-via", 
				"vpp", "ggf-ncp", 
				"mrm", "entrust-aaas", 
				"entrust-aams", "xfr", 
				"corba-iiop", "corba-iiop-ssl", 
				"mdc-portmapper", "hcp-wismar", 
				"asipregistry", "realm-rusd", 
				"nmap", "vatp", 
				"msexch-routing", "hyperwave-isp", 
				"connendp", "ha-cluster", 
				"ieee-mms-ssl", "rushd", 
				"uuidgen", "olsr", 
				"accessnetwork", "epp", 
				"lmp", "unassigned", "unassigned", "elcsd", 
				"agentx", "silc", 
				"borland-dsj", "unassigned", "entrust-kmsh", 
				"entrust-ash", "cisco-tdp", 
				"tbrpf", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "netviewdm1", 
				"netviewdm2", "netviewdm3", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"netgw", "netrcs", "unassigned", 
				"flexlm", "unassigned", "unassigned", "fujitsu-dev", 
				"ris-cm", "kerberos-adm", 
				"rfile", "pump", 
				"qrh", "rrh", 
				"tell", "unassigned", "unassigned", "nlogin", 
				"con", "ns", 
				"rxe", "quotad", 
				"cycleserv", "omserv", 
				"webster", "unassigned", "phonebook", "unassigned", 
				"vid", "cadlock", 
				"rtip", "cycleserv2", 
				"submit", "rpasswd", 
				"entomb", "wpages", 
				"multiling-http", "unassigned", "unassigned", "wpgs", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", 
				"mdbs_daemon", "device", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", 
				"fcp-udp", "unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "itm-mcell-s", 
				"pkix-3-ca-ra", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "dhcp-failover2", 
				"gdoi", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "iscsi", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", 
				"rsync", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"iclcnet-locate", 
				"iclcnet_svinfo", "cddbp", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", 
				"omginitialrefs", "smpnameres", 
				"ideafarm-chat", "ideafarm-catch", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"xact-backup", "apex-mesh", 
				"apex-edge", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "ftps-data", 
				"ftps", "nas", 
				"telnets", "imaps", 
				"ircs", "pop3s", 
				"vsinet", "maitrd", 
				"busboy", "puprouter", 
				"cadlock2", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "unassigned", 
				"unassigned", "unassigned", "unassigned", "unassigned", "surf", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", "Reserved", "Reserved", 
				"Reserved", "Reserved", "Reserved", null };
			
			int pno = port.toInt();
			int size;
			for(size = 0; names[size] != null; size++) ;
			if(pno >= size) {
				return "unassigned";
			}
			return names[pno];
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.portToString : " + e.toString());
		}
	}

	/**
	 * Method to convert an ICMP type field to a printable string.
	 * @param		type - the type field to convert.
	 * @return		a String with the name of the type.
	 * @author	Pete Lutz
	 */
	public static String icmpTypeToString(jND_BYTE1 type)  {
		try {
			int numbers[] = {
				0, 3, 4, 5, 8, 9,
				10, 11, 12, 13, 14,
				17, 18, 30, 31, 37,
				38 };

			String names[] = {
				"Echo reply", "Destination unreachable", "Source quench",
				"Redirect", "Echo request", "Router advertisement",
				"Router solicitation", "Time exceeded", "Parameter problem",
				"Time stamp request", "Time stamp reply", "Address mask request",
				"Address mask reply", "Traceroute", "Conversion error",
				"Domain name request", "Domain name reply", null };

			int tno = type.toInt();
			int i = 0;
			while(names[i] != null) {
				if(numbers[i] == tno) return names[i];
				i++;
			}

			return "" + tno;
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.icmpTypeToString : " + e.toString());
		}
	}

	/**
	 * Method to compute a 16-bit checksum of a packet
	 * @param		pkt - the packet to sum.
	 * @param		length - the length of the packet.
	 * @return		the checksum as an int.
	 * @author	Pete Lutz
	 */
	public static int checkSum(byte[] pkt, int length) {
		try {
			long sum = 0;
			int word;
			int wdLen = (int) (length / 2);
			boolean pad = (length % 2) != 0;

			// Sum up the data as 16-bit words
			for(int i = 0; i < wdLen * 2; i += 2) {
				word = (((int) pkt[i] << 8) & 0xff00)
					| (((int) pkt[i+1]) & 0xff);
				sum += ((long)word) & 0xffff;
			}
		    
			// Take care of any padding 
			// (if the length of the packet is odd, add a 0 byte)
			if(pad) {
				word = ((((int) pkt[pkt.length-1]) << 8) & 0xff00);
				sum += word;
			}
		    
			// Normalize the carry, if any
			while(((sum >> 16) & 0xffff) != 0) {
				long carrybits = ((sum >> 16) & 0xffff);
				sum = (sum & 0xffff) + carrybits;
			}
		    
			// Return the 1's complement of the whole thing
			return (int) ((~sum) & 0xffff);
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.checkSum : " + e.toString());
		}
	}

	/**
	 * Method to compute a 16-bit transport checksum of a packet. The transport
	 * checksum is based on the packet plus a pseudo header made up of:
	 * <p>
     *      source IP address (4 bytes) <p>
     *      destination IP address (4 bytes) <p>
     *      length of the packet (2 bytes) <p>
     *      protocol number (udp=17) (2 bytes) <p>
     * 
	 * @param		protocol - the transport layer protocol number
	 * @param		insrcIP - the source IP address
	 * @param		indstIP - the destination IP addresss
	 * @param		pkt - the packet to sum
	 * @return		the checksum as an int
	 * @author	Pete Lutz
	 */
	public static int transportCheckSum(
	  int protocol, 
	  jND_IPv4Address insrcIP, 
	  jND_IPv4Address indstIP, 
	  byte[] pkt) {
		try {
			byte[] srcIP = insrcIP.toByteArray();
			byte[] dstIP = indstIP.toByteArray();
			long sum = 0;
			long word;
			int wdLen = (int) (pkt.length / 2);
			boolean pad = (pkt.length % 2) != 0;
		    
			// Sum up the data as 16-bit words
			for(int i = 0; i < wdLen * 2; i += 2) {
				word = ((((int) pkt[i]) << 8) & 0xff00)
					| (((int) pkt[i+1]) & 0xff);
				sum += word;
			}
		    
			// Take care of any padding 
			// (if the length of the packet is odd, add a 0 byte)
			if(pad) {
				word = ((((int) pkt[pkt.length-1]) << 8) & 0xff00);
				sum += word;
			}
		    
			// Add in the source IP
			for(int i = 0; i < 4; i += 2) {
				word = ((((int) srcIP[i]) << 8) & 0xff00)
					| (((int) srcIP[i+1]) & 0xff);
				sum += word;
			}
		    
			// Add in the destination IP
			for(int i = 0; i < 4; i += 2) {
				word = ((((int) dstIP[i]) << 8) & 0xff00)
					| (((int) dstIP[i+1]) & 0xff);
				sum += word;
			}
		    
			// Add in the length and the protocol
			sum += protocol + pkt.length;
		    
			// Normalize the carry, if any
			if((sum >> 16) != 0) {
				long carrybits = sum >> 16;
				sum = (sum & 0xffff) + carrybits;
			}
		    
			// Return the 1's complement of the whole thing
			return (int) ((~sum) & 0xffff);
		} catch(jND_Exception nde) {
			throw nde;
		} catch (Exception e) {
			throw new jND_Exception("jND_Utility.transportCheckSum : " + e.toString());
		}
	}    

	/**
	 * Method to ARP for an IP address and return the MAC address
	 * as a byte array. If the IP address is on the same subnet as
	 * the NIC, the arp is for that IP. If the IP is on another subnet,
	 * then arpFor substitutes the IP of the gateway for that of the
	 * given IP address.
	 * 
	 * @param		nic - the NIC to use for the ARP
	 * @param		ipAddress - the protocol address to lookup
	 * @return		the MAC address corresponding the the 'ipAddr'
	 * @author	Pete Lutz
	 */
	public static byte[] arpFor(jND_NIC nic, byte[] ipAddress) {
	    arpNo++;

	    // My network or another network??
	    int myIP = nic.ipAddress().toByte4().toInt();
	    int hisIP = new jND_IPv4Address(ipAddress).toByte4().toInt();
	    int mask = nic.netMask().toByte4().toInt();
	    if((myIP & mask) != (hisIP & mask)) {
	        // different subnet, use router
			hisIP = nic.gateway().toByte4().toInt();
	    }
	    
	    // Build ARP packet
	    jND_ARP arp = new jND_ARP();
	    arp.hwAddressType(new jND_BYTE2(1));
	    arp.hwAddressLength(new jND_BYTE1(6));
		arp.protocolAddressType(new jND_BYTE2(jND_EthernetII.T_IP));
	    arp.protocolAddressLength(new jND_BYTE1(4));
		arp.opcode(new jND_BYTE2(jND_ARP.ARP_REQUEST));
	    arp.sourceHWAddress(nic.macAddress().toByteArray());
	    arp.sourceProtocolAddress(nic.ipAddress().toByteArray());
	    byte[] allZeros = new byte[6];
		for(int i = 0; i < 6; i++) allZeros[i] = 0;
	    arp.targetHWAddress(allZeros);
		arp.targetProtocolAddress(new jND_IPv4Address(
		    new jND_BYTE4(hisIP)).toByteArray());
	    
	    byte[] arpPkt = arp.build();
	    
	    // Build Ethernet frame
	    jND_EthernetII frame = new jND_EthernetII();
	    byte[] bcast = new byte[jND_ARP.HW_MAX_LEN];
		for(int i = 0; i < jND_ARP.HW_MAX_LEN; i++) bcast[i] = (byte)0xff;
	    frame.destAddress(new jND_EthernetAddress(bcast));
	    frame.srcAddress(nic.macAddress());
		frame.type(new jND_BYTE2(jND_EthernetII.T_ARP));
	    frame.payload(arpPkt);
	    
	    byte[] framePkt = frame.build();
	    
	    // Start listening for a reply
	    jND_CaptureSession cap = new jND_CaptureSession(nic, false);
		cap.filter("arp", false, nic.netMask());
	    cap.start();
	    cap.getThread().setName("ARP-" + arpNo);
	    
	    // Send arp
	    boolean closeNIC = false;
		if(nic.closed()) {
			closeNIC = true;
			nic.open();
		}
	    nic.inject(framePkt);
	    if(closeNIC)
	    	nic.close();
	    
	    // Wait for a reply
	    long accumTime = 0;
	    final int INCREMENT = 100;
	    
	    jND_PacketQueue pq = cap.packetQueue();
	    while(accumTime < 2000) {
	        if(pq.size() == 0) {
				try {
					Thread.sleep(INCREMENT);
				} catch(InterruptedException ie) {
					break;
				}
	            accumTime += INCREMENT;
	        }
	        else {
	            jND_EthernetII repEther = new jND_EthernetII();
	            byte[] pkt = pq.pop();
	            
				repEther.parse(pkt, 0);
				if(repEther.type().toInt() != jND_EthernetII.T_ARP) continue;
	            
	            jND_ARP reply = new jND_ARP();
				reply.parse(repEther.payload(), 0);
				if(reply.opcode().toShort() != jND_ARP.ARP_REPLY) continue;
	            
				int srcIP = new jND_IPv4Address(reply.sourceProtocolAddress()).toByte4().toInt();
	            if(srcIP != hisIP) continue;
	            
	    		cap.dispose();
	            return reply.sourceHWAddress();
	        }
	    }
	    cap.dispose();
	    return null;
	}

};


