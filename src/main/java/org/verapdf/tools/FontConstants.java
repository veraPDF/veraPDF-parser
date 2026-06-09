package org.verapdf.tools;

import java.util.Arrays;
import java.util.List;

public class FontConstants {
    public static final List<String> STYLE_SUFFIXES = Arrays.asList(
            // ---- Weight ----
            "Hairline", "Thin",
            "ExtraLight", "ExtLt", "Extra Light", "Extra-Light",
            "UltraLight", "UltLt", "Ultra Light", "Ultra-Light",
            "Light", "Lt",
            "Book",
            "Normal",
            "Regular", "Rg", "Roman",
            "Medium", "Md",
            "Demi", "DemiBold", "Demi Bold", "Demi-Bold",
            "SemiBold", "Semibold", "SemiBd", "Semi Bold", "Semi-Bold",
            "Bold", "Bd",
            "ExtraBold", "ExtBd", "Extra Bold", "Extra-Bold",
            "UltraBold", "UltBd", "Ultra Bold", "Ultra-Bold",
            "Black", "Blk",
            "Heavy", "Hv",
            "Ultra", "Fat", "Poster",

            // ---- Slope ----
            "Italic", "Ita", "It",
            "Oblique", "Obl", "Caps", "CapsI",
            "Backslant",

            // ---- Width ----
            "Compressed",
            "ExtraCondensed", "UltraCondensed",
            "Condensed", "Cond", "Cn",
            "SemiCondensed",
            "Narrow",
            "SemiExpanded",
            "Expanded", "Exp", "Extended",
            "ExtraExpanded", "UltraExpanded",
            "Wide",

            // ---- Optical size ----
            "Caption", "Text", "Subhead", "Deck", "Display", "Titling",

            // ---- Weight + Italic/Oblique (common combinations) ----
            "ThinItalic", "ThinIt",
            "LightItalic", "LightOblique", "LightIt",
            "BookItalic", "BookOblique",
            "MediumItalic", "MediumOblique", "MediumIt",
            "DemiItalic", "DemiOblique",
            "SemiBoldItalic", "SemiboldItalic", "SemiBoldIt", "SemiboldIt",
            "BoldItalic", "BoldIt", "BoldOblique", "BoldObl",
            "ExtraBoldItalic", "ExtraBoldIt",
            "BlackItalic", "BlackIt", "HeavyItalic",

            // ---- Width + Weight / Slope (common combos) ----
            "BoldCondensed", "BoldCond", "BoldCn",
            "BoldExpanded", "BoldExp", "BoldExtended",
            "BoldSemiExt", "SemiExt",
            "LightCondensed", "LightCond",
            "MediumCondensed", "MediumCond",
            "CondensedBold", "CondBold",
            "CondensedLight",
            "ExpandedBold", "ExtendedBold",

            // ---- SmallCaps & other variants ----
            "SmallCaps", "SC", "PetiteCaps",
            "RomanSmallCaps",
            "BoldSmallCaps",
            "Inline", "Outline", "Shadow",
            "Engraved", "Stencil", "Swash",
            "SuppSwashCaps", "SwashCaps",
            "Ornaments", "Symbols", "Icons", "Supp", "Small",

            // ---- Foundry / vendor tags ----
            "Std", "MT", "PS", "LT", "Com", "W1G", "EF", "CE"
    );
}
