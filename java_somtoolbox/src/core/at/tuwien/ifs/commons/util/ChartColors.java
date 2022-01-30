/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.commons.util;

import java.awt.Color;

/**
 * @author Rudolf Mayer
 * @version $Id: ChartColors.java 4020 2011-01-26 17:12:44Z mayer $
 */
public class ChartColors {
    private static Color[] defaultColours = { new Color(0xFF, 0x55, 0x55),//
            new Color(0x55, 0x55, 0xFF),//
            new Color(0x55, 0xFF, 0x55),//
            new Color(0xFF, 0xFF, 0x55),//
            new Color(0xFF, 0x55, 0xFF),//
            new Color(0x55, 0xFF, 0xFF),//
            Color.pink,//
            Color.gray,//
            new Color(0xc0, 0x00, 0x00), // DARK_RED
            new Color(0x00, 0x00, 0xC0), // DARK_BLUE
            new Color(0x00, 0xC0, 0x00),// DARK_GREEN
            new Color(0xC0, 0xC0, 0x00), // DARK_YELLOW
            new Color(0xC0, 0x00, 0xC0), // DARK_MAGENTA
            new Color(0x00, 0xC0, 0xC0), // DARK_CYAN
            Color.darkGray,//
            new Color(0xFF, 0x40, 0x40),// LIGHT_RED
            new Color(0x40, 0x40, 0xFF), // LIGHT_BLUE
            new Color(0x40, 0xFF, 0x40), // LIGHT_GREEN,
            new Color(0xFF, 0xFF, 0x40),// LIGHT_YELLOW,
            new Color(0xFF, 0x40, 0xFF),// LIGHT_MAGENTA,
            new Color(0x40, 0xFF, 0xFF), // LIGHT_CYAN,
            Color.lightGray,//
            new Color(0x80, 0x00, 0x00),// VERY_DARK_RED,
            new Color(0x00, 0x00, 0x80),// VERY_DARK_BLUE,
            new Color(0x00, 0x80, 0x00),// VERY_DARK_GREEN,
            new Color(0x80, 0x80, 0x00),// VERY_DARK_YELLOW,
            new Color(0x80, 0x00, 0x80),// VERY_DARK_MAGENTA,
            new Color(0x00, 0x80, 0x80),// VERY_DARK_CYAN,
            new Color(0xFF, 0x80, 0x80),// VERY_LIGHT_RED,
            new Color(0x80, 0x80, 0xFF),// VERY_LIGHT_BLUE,
            new Color(0x80, 0xFF, 0x80),// VERY_LIGHT_GREEN,
            new Color(0xFF, 0xFF, 0x80),// VERY_LIGHT_YELLOW,
            new Color(0xFF, 0x80, 0xFF),// VERY_LIGHT_MAGENTA,
            new Color(0x80, 0xFF, 0xFF),// VERY_LIGHT_CYAN
    };

    public static Color getDefaultColor(int index) {
        return defaultColours[index % defaultColours.length];
    }
}
