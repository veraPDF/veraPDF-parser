/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015-2025, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.as;

import org.verapdf.cos.filters.COSFilterASCIIHexEncode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class represents predefined PDF name. Also it caches known PDF names.
 * Internally each ASAtom is represented as a byte array packed into Java String using ISO_8859_1 encoding
 *
 * @author Timur Kamalov
 */
public class ASAtom implements Comparable<ASAtom> {

    private static final Map<String, ASAtom> PREDEFINED_PDF_NAMES = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, ASAtom> CACHED_PDF_NAMES = Collections.synchronizedMap(new HashMap<>());

    // 3
    public static final ASAtom key3D = new ASAtom("3D");
    public static final ASAtom key3DD = new ASAtom("3DD");
    public static final ASAtom key3DRef = new ASAtom("3DRef");

    // A
    public static final ASAtom A = new ASAtom("A");
    public static final ASAtom A_LOWERCASE = new ASAtom("a");
    public static final ASAtom AA = new ASAtom("AA");
    public static final ASAtom ACRO_FORM = new ASAtom("AcroForm");
    public static final ASAtom ACTUAL_TEXT = new ASAtom("ActualText");
    public static final ASAtom ADBE_PKCS7_DETACHED = new ASAtom("adbe.pkcs7.detached");
    public static final ASAtom ADBE_PKCS7_SHA1 = new ASAtom("adbe.pkcs7.sha1");
    public static final ASAtom ADBE_X509_RSA_SHA1 = new ASAtom("adbe.x509.rsa_sha1");
    public static final ASAtom ADOBE_PPKLITE = new ASAtom("Adobe.PPKLite");
    public static final ASAtom AESV2 = new ASAtom("AESV2");
    public static final ASAtom AESV3 = new ASAtom("AESV3");
    public static final ASAtom AF = new ASAtom("AF");
    public static final ASAtom AF_RELATIONSHIP = new ASAtom("AFRelationship");
    public static final ASAtom AFTER = new ASAtom("After");
    public static final ASAtom AIS = new ASAtom("AIS");
    public static final ASAtom ALT = new ASAtom("Alt");
    public static final ASAtom ALPHA = new ASAtom("Alpha");
    public static final ASAtom ALTERNATE = new ASAtom("Alternate");
    public static final ASAtom ALTERNATES = new ASAtom("Alternates");
    public static final ASAtom ANNOT = new ASAtom("Annot");
    public static final ASAtom ANNOTS = new ASAtom("Annots");
    public static final ASAtom ANTI_ALIAS = new ASAtom("AntiAlias");
    public static final ASAtom AP = new ASAtom("AP");
    public static final ASAtom AP_REF = new ASAtom("APRef");
    public static final ASAtom APP = new ASAtom("App");
    public static final ASAtom ART_BOX = new ASAtom("ArtBox");
    public static final ASAtom ARTIFACT = new ASAtom("Artifact");
    public static final ASAtom AS = new ASAtom("AS");
    public static final ASAtom ASCENT = new ASAtom("Ascent");
    public static final ASAtom ASCII_HEX_DECODE = new ASAtom("ASCIIHexDecode");
    public static final ASAtom ASCII_HEX_DECODE_ABBREVIATION = new ASAtom("AHx");
    public static final ASAtom ASCII85_DECODE = new ASAtom("ASCII85Decode");
    public static final ASAtom ASCII85_DECODE_ABBREVIATION = new ASAtom("A85");
    public static final ASAtom ATTACHED = new ASAtom("Attached");
    public static final ASAtom AUTHOR = new ASAtom("Author");
    public static final ASAtom AVG_WIDTH = new ASAtom("AvgWidth");
    // B
    public static final ASAtom B = new ASAtom("B");
    public static final ASAtom BACKGROUND = new ASAtom("Background");
    public static final ASAtom BASE_ENCODING = new ASAtom("BaseEncoding");
    public static final ASAtom BASE_FONT = new ASAtom("BaseFont");
    public static final ASAtom BASE_STATE = new ASAtom("BaseState");
    public static final ASAtom BBOX = new ASAtom("BBox");
    public static final ASAtom BC = new ASAtom("BC");
    public static final ASAtom BE = new ASAtom("BE");
    public static final ASAtom BEFORE = new ASAtom("Before");
    public static final ASAtom BG = new ASAtom("BG");
    public static final ASAtom BITS_PER_COMPONENT = new ASAtom("BitsPerComponent");
    public static final ASAtom BITS_PER_COORDINATE = new ASAtom("BitsPerCoordinate");
    public static final ASAtom BITS_PER_FLAG = new ASAtom("BitsPerFlag");
    public static final ASAtom BITS_PER_SAMPLE = new ASAtom("BitsPerSample");
    public static final ASAtom BL_FOCUS = new ASAtom("Bl");
    public static final ASAtom BLACK_IS_1 = new ASAtom("BlackIs1");
    public static final ASAtom BLACK_POINT = new ASAtom("BlackPoint");
    public static final ASAtom BLEED_BOX = new ASAtom("BleedBox");
    public static final ASAtom BM = new ASAtom("BM");
    public static final ASAtom BOUNDS = new ASAtom("Bounds");
    public static final ASAtom BPC = new ASAtom("BPC");
    public static final ASAtom BS = new ASAtom("BS");
    //** Acro form field type for button fields.
    public static final ASAtom BTN = new ASAtom("Btn");
    public static final ASAtom BYTERANGE = new ASAtom("ByteRange");
    // C
    public static final ASAtom C = new ASAtom("C");
    public static final ASAtom C0 = new ASAtom("C0");
    public static final ASAtom C1 = new ASAtom("C1");
    public static final ASAtom CA = new ASAtom("CA");
    public static final ASAtom CA_NS = new ASAtom("ca");
    public static final ASAtom CALCMYK= new ASAtom("CalCMYK");
    public static final ASAtom CALGRAY = new ASAtom("CalGray");
    public static final ASAtom CALRGB = new ASAtom("CalRGB");
    public static final ASAtom CAP = new ASAtom("Cap");
    public static final ASAtom CAP_HEIGHT = new ASAtom("CapHeight");
    public static final ASAtom CATALOG = new ASAtom("Catalog");
    public static final ASAtom CCITTFAX_DECODE = new ASAtom("CCITTFaxDecode");
    public static final ASAtom CCITTFAX_DECODE_ABBREVIATION = new ASAtom("CCF");
    public static final ASAtom CENTER_WINDOW = new ASAtom("CenterWindow");
    public static final ASAtom CF = new ASAtom("CF");
    public static final ASAtom CFM = new ASAtom("CFM");
    public static final ASAtom CHECKSUM = new ASAtom("CheckSum");
    //** Acro form field type for choice fields.
    public static final ASAtom CH = new ASAtom("Ch");
    public static final ASAtom CHAR_PROCS = new ASAtom("CharProcs");
    public static final ASAtom CHAR_SET = new ASAtom("CharSet");
    public static final ASAtom CICI_SIGNIT = new ASAtom("CICI.SignIt");
    public static final ASAtom CID_FONT_TYPE0 = new ASAtom("CIDFontType0");
    public static final ASAtom CID_FONT_TYPE0C = new ASAtom("CIDFontType0C");
    public static final ASAtom CID_FONT_TYPE2 = new ASAtom("CIDFontType2");
    public static final ASAtom CID_TO_GID_MAP = new ASAtom("CIDToGIDMap");
    public static final ASAtom CID_SET = new ASAtom("CIDSet");
    public static final ASAtom CID_SYSTEM_INFO = new ASAtom("CIDSystemInfo");
    public static final ASAtom CLASS_MAP = new ASAtom("ClassMap");
    public static final ASAtom CLR_F = new ASAtom("ClrF");
    public static final ASAtom CLR_FF = new ASAtom("ClrFf");
    public static final ASAtom CMAP = new ASAtom("CMap");
    public static final ASAtom CMAPNAME = new ASAtom("CMapName");
    public static final ASAtom CMYK = new ASAtom("CMYK");
    public static final ASAtom CO = new ASAtom("CO");
    public static final ASAtom COL_SPAN = new ASAtom("ColSpan");
    public static final ASAtom COLOR_BURN = new ASAtom("ColorBurn");
    public static final ASAtom COLOR_DODGE = new ASAtom("ColorDodge");
    public static final ASAtom COLORANTS = new ASAtom("Colorants");
    public static final ASAtom COLORS = new ASAtom("Colors");
    public static final ASAtom COLORSPACE = new ASAtom("ColorSpace");
    public static final ASAtom COLUMNS = new ASAtom("Columns");
    public static final ASAtom COMPATIBLE = new ASAtom("Compatible");
    public static final ASAtom COMPONENTS = new ASAtom("Components");
    public static final ASAtom CONTACT_INFO = new ASAtom("ContactInfo");
    public static final ASAtom CONTENTS = new ASAtom("Contents");
    public static final ASAtom CONFIGS = new ASAtom("Configs");
    public static final ASAtom COORDS = new ASAtom("Coords");
    public static final ASAtom COUNT = new ASAtom("Count");
    public static final ASAtom COURIER = new ASAtom("Courier");
    public static final ASAtom COURIER_BOLD = new ASAtom("Courier-Bold");
    public static final ASAtom COURIER_BOLD_OBLIQUE = new ASAtom("Courier-BoldOblique");
    public static final ASAtom COURIER_OBLIQUE = new ASAtom("Courier-Oblique");
    public static final ASAtom CP = new ASAtom("CP");
    public static final ASAtom CREATION_DATE = new ASAtom("CreationDate");
    public static final ASAtom CREATOR = new ASAtom("Creator");
    public static final ASAtom CROP_BOX = new ASAtom("CropBox");
    public static final ASAtom CRYPT = new ASAtom("Crypt");
    public static final ASAtom CS = new ASAtom("CS");
    public static final ASAtom CT = new ASAtom("CT");
    // D
    public static final ASAtom D = new ASAtom("D");
    public static final ASAtom DA = new ASAtom("DA");
    public static final ASAtom DARKEN = new ASAtom("Darken");
    public static final ASAtom DATE = new ASAtom("Date");
    public static final ASAtom DCT_DECODE = new ASAtom("DCTDecode");
    public static final ASAtom DCT_DECODE_ABBREVIATION = new ASAtom("DCT");
    public static final ASAtom DECODE = new ASAtom("Decode");
    public static final ASAtom DECODE_PARMS = new ASAtom("DecodeParms");
    public static final ASAtom DEFAULT = new ASAtom("Default");
    public static final ASAtom DEFAULT_CMYK = new ASAtom("DefaultCMYK");
    public static final ASAtom DEFAULT_GRAY = new ASAtom("DefaultGray");
    public static final ASAtom DEFAULT_RGB = new ASAtom("DefaultRGB");
    public static final ASAtom DESC = new ASAtom("Desc");
    public static final ASAtom DESCENDANT_FONTS = new ASAtom("DescendantFonts");
    public static final ASAtom DESCENT = new ASAtom("Descent");
    public static final ASAtom DEST = new ASAtom("Dest");
    public static final ASAtom DEST_OUTPUT_PROFILE = new ASAtom("DestOutputProfile");
    public static final ASAtom DEST_OUTPUT_PROFILE_REF = new ASAtom("DestOutputProfileRef");
    public static final ASAtom DESTS = new ASAtom("Dests");
    public static final ASAtom DEVICECMYK = new ASAtom("DeviceCMYK");
    public static final ASAtom DEVICEGRAY = new ASAtom("DeviceGray");
    public static final ASAtom DEVICEN = new ASAtom("DeviceN");
    public static final ASAtom DEVICERGB = new ASAtom("DeviceRGB");
    public static final ASAtom DI = new ASAtom("Di");
    public static final ASAtom DIFFERENCE = new ASAtom("Difference");
    public static final ASAtom DIFFERENCES = new ASAtom("Differences");
    public static final ASAtom DIGEST_LOCATION = new ASAtom("DigestLocation");
    public static final ASAtom DIGEST_METHOD = new ASAtom("DigestMethod");
    public static final ASAtom DIGEST_RIPEMD160 = new ASAtom("RIPEMD160");
    public static final ASAtom DIGEST_SHA1 = new ASAtom("SHA1");
    public static final ASAtom DIGEST_SHA256 = new ASAtom("SHA256");
    public static final ASAtom DIGEST_SHA384 = new ASAtom("SHA384");
    public static final ASAtom DIGEST_SHA512 = new ASAtom("SHA512");
    public static final ASAtom DIGEST_VALUE = new ASAtom("DigestValue");
    public static final ASAtom DIRECTION = new ASAtom("Direction");
    public static final ASAtom DISPLAY_DOC_TITLE = new ASAtom("DisplayDocTitle");
    public static final ASAtom DL = new ASAtom("DL");
    public static final ASAtom DM = new ASAtom("Dm");
    public static final ASAtom DOC = new ASAtom("Doc");
    public static final ASAtom DOC_CHECKSUM = new ASAtom("DocChecksum");
    public static final ASAtom DOC_MDP = new ASAtom("DocMDP");
    public static final ASAtom DOC_TIME_STAMP = new ASAtom("DocTimeStamp");
    public static final ASAtom DOMAIN = new ASAtom("Domain");
    public static final ASAtom DOS = new ASAtom("DOS");
    public static final ASAtom DP = new ASAtom("DP");
    public static final ASAtom DR = new ASAtom("DR");
    public static final ASAtom DS = new ASAtom("DS");
    public static final ASAtom DUPLEX = new ASAtom("Duplex");
    public static final ASAtom DUR = new ASAtom("Dur");
    public static final ASAtom DV = new ASAtom("DV");
    public static final ASAtom DW = new ASAtom("DW");
    public static final ASAtom DW2 = new ASAtom("DW2");
    // E
    public static final ASAtom E = new ASAtom("E");
    public static final ASAtom EARLY_CHANGE = new ASAtom("EarlyChange");
    public static final ASAtom EF = new ASAtom("EF");
    public static final ASAtom EMBEDDED_FDFS = new ASAtom("EmbeddedFDFs");
    public static final ASAtom EMBEDDED_FILES = new ASAtom("EmbeddedFiles");
    public static final ASAtom EMPTY = new ASAtom("");
    public static final ASAtom ENCODE = new ASAtom("Encode");
    public static final ASAtom ENCODED_BYTE_ALIGN = new ASAtom("EncodedByteAlign");
    public static final ASAtom ENCODING = new ASAtom("Encoding");
    public static final ASAtom ENCODING_90MS_RKSJ_H = new ASAtom("90ms-RKSJ-H");
    public static final ASAtom ENCODING_90MS_RKSJ_V = new ASAtom("90ms-RKSJ-V");
    public static final ASAtom ENCODING_ETEN_B5_H = new ASAtom("ETen-B5-H");
    public static final ASAtom ENCODING_ETEN_B5_V = new ASAtom("ETen-B5-V");
    public static final ASAtom ENCRYPT = new ASAtom("Encrypt");
    public static final ASAtom ENCRYPT_META_DATA = new ASAtom("EncryptMetadata");
    public static final ASAtom END_OF_LINE = new ASAtom("EndOfLine");
    public static final ASAtom ENTRUST_PPKEF = new ASAtom("Entrust.PPKEF");
    public static final ASAtom EXCLUSION = new ASAtom("Exclusion");
    public static final ASAtom EXT_G_STATE = new ASAtom("ExtGState");
    public static final ASAtom EXTEND = new ASAtom("Extend");
    public static final ASAtom EXTENDS = new ASAtom("Extends");
    public static final ASAtom EVENT = new ASAtom("Event");
    // F
    public static final ASAtom F = new ASAtom("F");
    public static final ASAtom F_DECODE_PARMS = new ASAtom("FDecodeParms");
    public static final ASAtom F_FILTER = new ASAtom("FFilter");
    public static final ASAtom FB = new ASAtom("FB");
    public static final ASAtom FDF = new ASAtom("FDF");
    public static final ASAtom FF = new ASAtom("Ff");
    public static final ASAtom FIELDS = new ASAtom("Fields");
    public static final ASAtom FILESPEC = new ASAtom("Filespec");
    public static final ASAtom FILTER = new ASAtom("Filter");
    public static final ASAtom FIRST = new ASAtom("First");
    public static final ASAtom FIRST_CHAR = new ASAtom("FirstChar");
    public static final ASAtom FIT_WINDOW = new ASAtom("FitWindow");
    public static final ASAtom FL = new ASAtom("FL");
    public static final ASAtom FLAGS = new ASAtom("Flags");
    public static final ASAtom FLATE_DECODE = new ASAtom("FlateDecode");
    public static final ASAtom FLATE_DECODE_ABBREVIATION = new ASAtom("Fl");
    public static final ASAtom FOCUS_ABBREVIATION = new ASAtom("Fo");
    public static final ASAtom FONT = new ASAtom("Font");
    public static final ASAtom FONT_BBOX = new ASAtom("FontBBox");
    public static final ASAtom FONT_DESC = new ASAtom("FontDescriptor");
    public static final ASAtom FONT_FAMILY = new ASAtom("FontFamily");
    public static final ASAtom FONT_FILE = new ASAtom("FontFile");
    public static final ASAtom FONT_FILE2 = new ASAtom("FontFile2");
    public static final ASAtom FONT_FILE3 = new ASAtom("FontFile3");
    public static final ASAtom FONT_MATRIX = new ASAtom("FontMatrix");
    public static final ASAtom FONT_NAME = new ASAtom("FontName");
    public static final ASAtom FONT_STRETCH = new ASAtom("FontStretch");
    public static final ASAtom FONT_WEIGHT = new ASAtom("FontWeight");
    public static final ASAtom FORM = new ASAtom("Form");
    public static final ASAtom FORMTYPE = new ASAtom("FormType");
    public static final ASAtom FRM = new ASAtom("FRM");
    public static final ASAtom FS = new ASAtom("FS");
    public static final ASAtom FT = new ASAtom("FT");
    public static final ASAtom FUNCTION = new ASAtom("Function");
    public static final ASAtom FUNCTION_TYPE = new ASAtom("FunctionType");
    public static final ASAtom FUNCTIONS = new ASAtom("Functions");
    // G
    public static final ASAtom G = new ASAtom("G");
    public static final ASAtom GAMMA = new ASAtom("Gamma");
    public static final ASAtom GO_TO = new ASAtom("GoTo");
    public static final ASAtom GROUP = new ASAtom("Group");
    public static final ASAtom GTS_PDFA1 = new ASAtom("GTS_PDFA1");
    // H
    public static final ASAtom H = new ASAtom("H");
    public static final ASAtom HALFTONE_NAME = new ASAtom("HalftoneName");
    public static final ASAtom HALFTONE_TYPE = new ASAtom("HalftoneType");
    public static final ASAtom HARD_LIGHT = new ASAtom("HardLight");
    public static final ASAtom HEADERS = new ASAtom("Headers");
    public static final ASAtom HEIGHT = new ASAtom("Height");
    public static final ASAtom HELVETICA = new ASAtom("Helvetica");
    public static final ASAtom HELVETICA_BOLD = new ASAtom("Helvetica-Bold");
    public static final ASAtom HELVETICA_BOLD_OBLIQUE = new ASAtom("Helvetica-BoldOblique");
    public static final ASAtom HELVETICA_OBLIQUE = new ASAtom("Helvetica-Oblique");
    public static final ASAtom HIDE_MENUBAR = new ASAtom("HideMenubar");
    public static final ASAtom HIDE_TOOLBAR = new ASAtom("HideToolbar");
    public static final ASAtom HIDE_WINDOWUI = new ASAtom("HideWindowUI");
    public static final ASAtom HT = new ASAtom("HT");
    public static final ASAtom HTO = new ASAtom("HTO");
    public static final ASAtom HTP = new ASAtom("HTP");
    // I
    public static final ASAtom I = new ASAtom("I");
    public static final ASAtom IC = new ASAtom("IC");
    public static final ASAtom ICCBASED = new ASAtom("ICCBased");
    public static final ASAtom ID = new ASAtom("ID");
    public static final ASAtom ID_TREE = new ASAtom("IDTree");
    public static final ASAtom IDENTITY = new ASAtom("Identity");
    public static final ASAtom IDENTITY_H = new ASAtom("Identity-H");
    public static final ASAtom IF = new ASAtom("IF");
    public static final ASAtom IM = new ASAtom("IM");
    public static final ASAtom IMAGE = new ASAtom("Image");
    public static final ASAtom IMAGE_MASK = new ASAtom("ImageMask");
    public static final ASAtom INDEX = new ASAtom("Index");
    public static final ASAtom INDEXED = new ASAtom("Indexed");
    public static final ASAtom INFO = new ASAtom("Info");
    public static final ASAtom INKLIST = new ASAtom("InkList");
    public static final ASAtom INTENT = new ASAtom("Intent");
    public static final ASAtom INTERPOLATE = new ASAtom("Interpolate");
    public static final ASAtom IT = new ASAtom("IT");
    public static final ASAtom ITALIC_ANGLE = new ASAtom("ItalicAngle");
    // J
    public static final ASAtom JAVA_SCRIPT = new ASAtom("JavaScript");
    public static final ASAtom JBIG2_DECODE = new ASAtom("JBIG2Decode");
    public static final ASAtom JBIG2_GLOBALS = new ASAtom("JBIG2Globals");
    public static final ASAtom JPX_DECODE = new ASAtom("JPXDecode");
    public static final ASAtom JS = new ASAtom("JS");
    // K
    public static final ASAtom K = new ASAtom("K");
    public static final ASAtom KEYWORDS = new ASAtom("Keywords");
    public static final ASAtom KIDS = new ASAtom("Kids");
    // L
    public static final ASAtom L = new ASAtom("L");
    public static final ASAtom LAB = new ASAtom("Lab");
    public static final ASAtom LANG = new ASAtom("Lang");
    public static final ASAtom LAST = new ASAtom("Last");
    public static final ASAtom LAST_CHAR = new ASAtom("LastChar");
    public static final ASAtom LAST_MODIFIED = new ASAtom("LastModified");
    public static final ASAtom LC = new ASAtom("LC");
    public static final ASAtom LE = new ASAtom("LE");
    public static final ASAtom LEADING = new ASAtom("Leading");
    public static final ASAtom LEGAL_ATTESTATION = new ASAtom("LegalAttestation");
    public static final ASAtom LINEARIZED = new ASAtom("Linearized");
    public static final ASAtom LENGTH = new ASAtom("Length");
    public static final ASAtom LENGTH1 = new ASAtom("Length1");
    public static final ASAtom LENGTH2 = new ASAtom("Length2");
    public static final ASAtom LIGHTEN = new ASAtom("Lighten");
    public static final ASAtom LIMITS = new ASAtom("Limits");
    public static final ASAtom LIST_NUMBERING = new ASAtom("ListNumbering");
    public static final ASAtom LJ = new ASAtom("LJ");
    public static final ASAtom LL = new ASAtom("LL");
    public static final ASAtom LLE = new ASAtom("LLE");
    public static final ASAtom LLO = new ASAtom("LLO");
    public static final ASAtom LOCATION = new ASAtom("Location");
    public static final ASAtom LUMINOSITY = new ASAtom("Luminosity");
    public static final ASAtom LW = new ASAtom("LW");
    public static final ASAtom LZW_DECODE = new ASAtom("LZWDecode");
    public static final ASAtom LZW_DECODE_ABBREVIATION = new ASAtom("LZW");
    // M
    public static final ASAtom M = new ASAtom("M");
    public static final ASAtom MAC = new ASAtom("Mac");
    public static final ASAtom MAC_ROMAN_ENCODING = new ASAtom("MacRomanEncoding");
    public static final ASAtom MAC_EXPERT_ENCODING = new ASAtom("MacExpertEncoding");
    public static final ASAtom MARK_INFO = new ASAtom("MarkInfo");
    public static final ASAtom MARKED = new ASAtom("Marked");
    public static final ASAtom MASK = new ASAtom("Mask");
    public static final ASAtom MATRIX = new ASAtom("Matrix");
    public static final ASAtom MAX_LEN = new ASAtom("MaxLen");
    public static final ASAtom MAX_WIDTH = new ASAtom("MaxWidth");
    public static final ASAtom MCID = new ASAtom("MCID");
    public static final ASAtom MCR = new ASAtom("MCR");
    public static final ASAtom MDP = new ASAtom("MDP");
    public static final ASAtom MEDIA_BOX = new ASAtom("MediaBox");
    public static final ASAtom METADATA = new ASAtom("Metadata");
    public static final ASAtom MISSING_WIDTH = new ASAtom("MissingWidth");
    public static final ASAtom MK = new ASAtom("MK");
    public static final ASAtom ML = new ASAtom("ML");
    public static final ASAtom MM_TYPE1 = new ASAtom("MMType1");
    public static final ASAtom MOD_DATE = new ASAtom("ModDate");
    public static final ASAtom MULTIPLY = new ASAtom("Multiply");
    // N
    public static final ASAtom N = new ASAtom("N");
    public static final ASAtom NA = new ASAtom("NA");
    public static final ASAtom NAME = new ASAtom("Name");
    public static final ASAtom NAMES = new ASAtom("Names");
    public static final ASAtom NEED_APPEARANCES = new ASAtom("NeedAppearances");
    public static final ASAtom NEEDS_RENDERING = new ASAtom("NeedsRendering");
    public static final ASAtom NEXT = new ASAtom("Next");
    public static final ASAtom NM = new ASAtom("NM");
    public static final ASAtom NON_EFONT_NO_WARN = new ASAtom("NonEFontNoWarn");
    public static final ASAtom NON_FULL_SCREEN_PAGE_MODE = new ASAtom("NonFullScreenPageMode");
    public static final ASAtom NONE = new ASAtom("None");
    public static final ASAtom NOTE_TYPE = new ASAtom("NoteType");
    public static final ASAtom NORMAL = new ASAtom("Normal");
    public static final ASAtom NS = new ASAtom("NS");
    public static final ASAtom NUMS = new ASAtom("Nums");
    // O
    public static final ASAtom O = new ASAtom("O");
    public static final ASAtom OBJ = new ASAtom("Obj");
    public static final ASAtom OBJR = new ASAtom("OBJR");
    public static final ASAtom OBJ_STM = new ASAtom("ObjStm");
    public static final ASAtom OC = new ASAtom("OC");
    public static final ASAtom OCG = new ASAtom("OCG");
    public static final ASAtom OCGS = new ASAtom("OCGs");
    public static final ASAtom OCPROPERTIES = new ASAtom("OCProperties");
    public static final ASAtom OE = new ASAtom("OE");
    public static final ASAtom OFF = new ASAtom("OFF");
    public static final ASAtom ON = new ASAtom("ON");
    public static final ASAtom OP = new ASAtom("OP");
    public static final ASAtom OP_NS = new ASAtom("op");
    public static final ASAtom OPEN_ACTION = new ASAtom("OpenAction");
    public static final ASAtom OPEN_TYPE = new ASAtom("OpenType");
    public static final ASAtom OPI = new ASAtom("OPI");
    public static final ASAtom OPM = new ASAtom("OPM");
    public static final ASAtom OPT = new ASAtom("Opt");
    public static final ASAtom ORDER = new ASAtom("Order");
    public static final ASAtom ORDERING = new ASAtom("Ordering");
    public static final ASAtom OS = new ASAtom("OS");
    public static final ASAtom OUTLINES = new ASAtom("Outlines");
    public static final ASAtom OUTPUT_CONDITION = new ASAtom("OutputCondition");
    public static final ASAtom OUTPUT_CONDITION_IDENTIFIER = new ASAtom(
            "OutputConditionIdentifier");
    public static final ASAtom OUTPUT_INTENT = new ASAtom("OutputIntent");
    public static final ASAtom OUTPUT_INTENTS = new ASAtom("OutputIntents");
    public static final ASAtom OVERLAY = new ASAtom("Overlay");
    // P
    public static final ASAtom P = new ASAtom("P");
    public static final ASAtom PA = new ASAtom("PA");
    public static final ASAtom PAGE = new ASAtom("Page");
    public static final ASAtom PAGE_LABELS = new ASAtom("PageLabels");
    public static final ASAtom PAGE_LAYOUT = new ASAtom("PageLayout");
    public static final ASAtom PAGE_MODE = new ASAtom("PageMode");
    public static final ASAtom PAGES = new ASAtom("Pages");
    public static final ASAtom PAINT_TYPE = new ASAtom("PaintType");
    public static final ASAtom PANOSE = new ASAtom("Panose");
    public static final ASAtom PARAMS = new ASAtom("Params");
    public static final ASAtom PARENT = new ASAtom("Parent");
    public static final ASAtom PARENT_TREE = new ASAtom("ParentTree");
    public static final ASAtom PARENT_TREE_NEXT_KEY = new ASAtom("ParentTreeNextKey");
    public static final ASAtom PATTERN = new ASAtom("Pattern");
    public static final ASAtom PATTERN_TYPE = new ASAtom("PatternType");
    public static final ASAtom PC = new ASAtom("PC");
    public static final ASAtom PDF_DOC_ENCODING = new ASAtom("PDFDocEncoding");
    public static final ASAtom PERMS = new ASAtom("Perms");
    public static final ASAtom PG = new ASAtom("Pg");
    public static final ASAtom PI = new ASAtom("PI");
    public static final ASAtom PIECE_INFO = new ASAtom("PieceInfo");
    public static final ASAtom PO = new ASAtom("PO");
    public static final ASAtom POPUP = new ASAtom("Popup");
    public static final ASAtom PRE_RELEASE = new ASAtom("PreRelease");
    public static final ASAtom PREDICTOR = new ASAtom("Predictor");
    public static final ASAtom PRES_STEPS = new ASAtom("PresSteps");
    public static final ASAtom PREV = new ASAtom("Prev");
    public static final ASAtom PRINT_AREA = new ASAtom("PrintArea");
    public static final ASAtom PRINT_CLIP = new ASAtom("PrintClip");
    public static final ASAtom PRINT_SCALING = new ASAtom("PrintScaling");
    public static final ASAtom PROC_SET = new ASAtom("ProcSet");
    public static final ASAtom PROCESS = new ASAtom("Process");
    public static final ASAtom PRODUCER = new ASAtom("Producer");
    public static final ASAtom PROP_BUILD = new ASAtom("Prop_Build");
    public static final ASAtom PROPERTIES = new ASAtom("Properties");
    public static final ASAtom PS = new ASAtom("PS");
    public static final ASAtom PUB_SEC = new ASAtom("PubSec");
    public static final ASAtom PV = new ASAtom("PV");
    public static final ASAtom PZ = new ASAtom("PZ");
    // Q
    public static final ASAtom Q = new ASAtom("Q");
    public static final ASAtom QUADPOINTS = new ASAtom("QuadPoints");
    // R
    public static final ASAtom R = new ASAtom("R");
    public static final ASAtom R_LOWERCASE = new ASAtom("r");
    public static final ASAtom RANGE = new ASAtom("Range");
    public static final ASAtom RC = new ASAtom("RC");
    public static final ASAtom RD = new ASAtom("RD");
    public static final ASAtom REASON = new ASAtom("Reason");
    public static final ASAtom REASONS = new ASAtom("Reasons");
    public static final ASAtom RECIPIENTS = new ASAtom("Recipients");
    public static final ASAtom RECT = new ASAtom("Rect");
    public static final ASAtom REF = new ASAtom("Ref");
    public static final ASAtom REFERENCE = new ASAtom("Reference");
    public static final ASAtom REGISTRY = new ASAtom("Registry");
    public static final ASAtom REGISTRY_NAME = new ASAtom("RegistryName");
    public static final ASAtom RENAME = new ASAtom("Rename");
    public static final ASAtom RESOURCES = new ASAtom("Resources");
    public static final ASAtom REQUIREMENTS = new ASAtom("Requirements");
    public static final ASAtom RGB = new ASAtom("RGB");
    public static final ASAtom RI = new ASAtom("RI");
    public static final ASAtom ROLE = new ASAtom("Role");
    public static final ASAtom ROLE_MAP = new ASAtom("RoleMap");
    public static final ASAtom ROLE_MAP_NS = new ASAtom("RoleMapNS");
    public static final ASAtom ROOT = new ASAtom("Root");
    public static final ASAtom ROTATE = new ASAtom("Rotate");
    public static final ASAtom ROW_SPAN = new ASAtom("RowSpan");
    public static final ASAtom ROWS = new ASAtom("Rows");
    public static final ASAtom RUN_LENGTH_DECODE = new ASAtom("RunLengthDecode");
    public static final ASAtom RUN_LENGTH_DECODE_ABBREVIATION = new ASAtom("RL");
    public static final ASAtom RV = new ASAtom("RV");
    // S
    public static final ASAtom S = new ASAtom("S");
    public static final ASAtom SA = new ASAtom("SA");
    public static final ASAtom SCOPE = new ASAtom("Scope");
    public static final ASAtom SCREEN = new ASAtom("Screen");
    public static final ASAtom SD = new ASAtom("SD");
    public static final ASAtom SE = new ASAtom("SE");
    public static final ASAtom SEPARATION = new ASAtom("Separation");
    public static final ASAtom SET_F = new ASAtom("SetF");
    public static final ASAtom SET_FF = new ASAtom("SetFf");
    public static final ASAtom SHADING = new ASAtom("Shading");
    public static final ASAtom SHADING_TYPE = new ASAtom("ShadingType");
    public static final ASAtom SIG = new ASAtom("Sig");
    public static final ASAtom SIG_FLAGS = new ASAtom("SigFlags");
    public static final ASAtom SIZE = new ASAtom("Size");
    public static final ASAtom SM = new ASAtom("SM");
    public static final ASAtom SMASK = new ASAtom("SMask");
    public static final ASAtom SMASK_IN_DATA = new ASAtom("SMaskInData");
    public static final ASAtom SOFT_LIGHT = new ASAtom("SoftLight");
    public static final ASAtom SS = new ASAtom("SS");
    public static final ASAtom ST = new ASAtom("St");
    public static final ASAtom STANDARD = new ASAtom("Standard");
    public static final ASAtom STANDARD_ENCODING = new ASAtom("StandardEncoding");
    public static final ASAtom STATE = new ASAtom("State");
    public static final ASAtom STATE_MODEL = new ASAtom("StateModel");
    public static final ASAtom STATUS = new ASAtom("Status");
    public static final ASAtom STD_CF = new ASAtom("StdCF");
    public static final ASAtom STEM_H = new ASAtom("StemH");
    public static final ASAtom STEM_V = new ASAtom("StemV");
    public static final ASAtom STM = new ASAtom("Stm");
    public static final ASAtom STM_F = new ASAtom("StmF");
    public static final ASAtom STR_F = new ASAtom("StrF");
    public static final ASAtom STRUCT_ELEM = new ASAtom("StructElem");
    public static final ASAtom STRUCT_PARENT = new ASAtom("StructParent");
    public static final ASAtom STRUCT_PARENTS = new ASAtom("StructParents");
    public static final ASAtom STRUCT_TREE_ROOT = new ASAtom("StructTreeRoot");
    public static final ASAtom STYLE = new ASAtom("Style");
    public static final ASAtom SUB_FILTER = new ASAtom("SubFilter");
    public static final ASAtom SUBJ = new ASAtom("Subj");
    public static final ASAtom SUBJECT = new ASAtom("Subject");
    public static final ASAtom SUBTYPE = new ASAtom("Subtype");
    public static final ASAtom SUBTYPE_2 = new ASAtom("Subtype2");
    public static final ASAtom SUPPLEMENT = new ASAtom("Supplement");
    public static final ASAtom SUSPECTS = new ASAtom("Suspects");
    public static final ASAtom SV = new ASAtom("SV");
    public static final ASAtom SW = new ASAtom("SW");
    public static final ASAtom SY = new ASAtom("Sy");
    public static final ASAtom SYMBOL = new ASAtom("Symbol");
    // T
    public static final ASAtom T = new ASAtom("T");
    public static final ASAtom TABS = new ASAtom("Tabs");
    public static final ASAtom TARGET = new ASAtom("Target");
    public static final ASAtom TEMPLATES = new ASAtom("Templates");
    public static final ASAtom THREADS = new ASAtom("Threads");
    public static final ASAtom THUMB = new ASAtom("Thumb");
    public static final ASAtom TI = new ASAtom("TI");
    public static final ASAtom TILING_TYPE = new ASAtom("TilingType");
    public static final ASAtom TIMES_BOLD = new ASAtom("Times-Bold");
    public static final ASAtom TIMES_BOLD_ITALIC = new ASAtom("Times-BoldItalic");
    public static final ASAtom TIMES_ITALIC = new ASAtom("Times-Italic");
    public static final ASAtom TIMES_ROMAN = new ASAtom("Times-Roman");
    public static final ASAtom TIME_STAMP = new ASAtom("TimeStamp");
    public static final ASAtom TITLE = new ASAtom("Title");
    public static final ASAtom TK = new ASAtom("TK");
    public static final ASAtom TM = new ASAtom("TM");
    public static final ASAtom TO_UNICODE = new ASAtom("ToUnicode");
    public static final ASAtom TR = new ASAtom("TR");
    public static final ASAtom TR2 = new ASAtom("TR2");
    public static final ASAtom TRAPPED = new ASAtom("Trapped");
    public static final ASAtom TRANS = new ASAtom("Trans");
    public static final ASAtom TRANSFER_FUNCTION = new ASAtom("TransferFunction");
    public static final ASAtom TRANSPARENCY = new ASAtom("Transparency");
    public static final ASAtom TREF = new ASAtom("TRef");
    public static final ASAtom TRIM_BOX = new ASAtom("TrimBox");
    public static final ASAtom TRUE_TYPE = new ASAtom("TrueType");
    public static final ASAtom TRUSTED_MODE = new ASAtom("TrustedMode");
    public static final ASAtom TU = new ASAtom("TU");
    /** Acro form field type for text field. */
    public static final ASAtom TX = new ASAtom("Tx");
    public static final ASAtom TYPE = new ASAtom("Type");
    public static final ASAtom TYPE0 = new ASAtom("Type0");
    public static final ASAtom TYPE1 = new ASAtom("Type1");
    public static final ASAtom TYPE1C = new ASAtom("Type1C");
    public static final ASAtom TYPE3 = new ASAtom("Type3");
    // U
    public static final ASAtom U = new ASAtom("U");
    public static final ASAtom UE = new ASAtom("UE");
    public static final ASAtom UF = new ASAtom("UF");
    public static final ASAtom UNCHANGED = new ASAtom("Unchanged");
    public static final ASAtom UNIX = new ASAtom("Unix");
    public static final ASAtom URI = new ASAtom("URI");
    public static final ASAtom URL = new ASAtom("URL");
    public static final ASAtom USE_CMAP = new ASAtom("UseCMap");
    // V
    public static final ASAtom V = new ASAtom("V");
    public static final ASAtom VERISIGN_PPKVS = new ASAtom("VeriSign.PPKVS");
    public static final ASAtom VERSION = new ASAtom("Version");
    public static final ASAtom VERTICES = new ASAtom("Vertices");
    public static final ASAtom VERTICES_PER_ROW = new ASAtom("VerticesPerRow");
    public static final ASAtom VIEW_AREA = new ASAtom("ViewArea");
    public static final ASAtom VIEW_CLIP = new ASAtom("ViewClip");
    public static final ASAtom VIEWER_PREFERENCES = new ASAtom("ViewerPreferences");
    // W
    public static final ASAtom W = new ASAtom("W");
    public static final ASAtom W2 = new ASAtom("W2");
    public static final ASAtom WC = new ASAtom("WC");
    public static final ASAtom WHITE_POINT = new ASAtom("WhitePoint");
    public static final ASAtom WIDGET = new ASAtom("Widget");
    public static final ASAtom WIDTH = new ASAtom("Width");
    public static final ASAtom WIDTHS = new ASAtom("Widths");
    public static final ASAtom WIN_ANSI_ENCODING = new ASAtom("WinAnsiEncoding");
    public static final ASAtom WP = new ASAtom("WP");
    public static final ASAtom WS = new ASAtom("WS");
    public static final ASAtom W_MODE = new ASAtom("WMode");
    // X
    public static final ASAtom X = new ASAtom("X");
    public static final ASAtom XFA = new ASAtom("XFA");
    public static final ASAtom X_STEP = new ASAtom("XStep");
    public static final ASAtom XHEIGHT = new ASAtom("XHeight");
    public static final ASAtom XML = new ASAtom("XML");
    public static final ASAtom XOBJECT = new ASAtom("XObject");
    public static final ASAtom XREF = new ASAtom("XRef");
    public static final ASAtom XREF_STM = new ASAtom("XRefStm");
    // Y
    public static final ASAtom Y_STEP = new ASAtom("YStep");
    public static final ASAtom YES = new ASAtom("Yes");

    // Z
    public static final ASAtom ZAPF_DINGBATS = new ASAtom("ZapfDingbats");

    private String value;

    private ASAtom(String value) {
        this(value, true);
    }

    private ASAtom(String value, boolean predefinedValue) {
        this.value = value;
        if (predefinedValue) {
            PREDEFINED_PDF_NAMES.put(value, this);
        } else {
            if (!CACHED_PDF_NAMES.containsKey(value)) {
                CACHED_PDF_NAMES.put(value, this);
            }
        }
    }

    /**
     * Gets PDF name from string. Also caches it if necessary.
     *
     * @param value is PDF name as string.
     * @return PDF name as ASAtom.
     */
    public static ASAtom getASAtom(String value) {
        if (value == null) {
            return null;
        }

        if (PREDEFINED_PDF_NAMES.containsKey(value)) {
            return PREDEFINED_PDF_NAMES.get(value);
        }
        if (CACHED_PDF_NAMES.containsKey(value)) {
            return CACHED_PDF_NAMES.get(value);
        }
        ASAtom result = new ASAtom(value, false);
        CACHED_PDF_NAMES.put(value, result);
        return result;
    }

    /**
     * @return string value of ASAtom.
     */
    public String getValue() {
        return value;
    }

    private void setValue(String value) {
        this.value = value;
    }

    /**
     * @return string value of ASAtom with appended / character.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("/");
        for (int i = 0; i < value.length(); i++) {
            final int c = value.charAt(i);
            if (CharTable.isRegular(c) && c != '#' && c > 32 && c < 127) {
                result.append((char) c);
            } else {
                result.append('#');
                result.append(COSFilterASCIIHexEncode.ASCII_HEX_BIG[c]);
                result.append(COSFilterASCIIHexEncode.ASCII_HEX_LITTLE[c]);
            }
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ASAtom asAtom = (ASAtom) o;

        return Objects.equals(value, asAtom.value);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }


    @Override
    public int compareTo(ASAtom o) {
        return this.value.compareTo(o.value);
    }
}
