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
package at.tuwien.ifs.feature;

import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Rudolf Mayer
 * @version $Id: ContentType.java 4030 2011-01-29 22:29:19Z mayer $
 */
public class ContentType {
    private String mainType;

    private String subType;

    public static final ContentType AUDIO = new ContentType("audio");

    public static final ContentType AUDIO_RP = new ContentType("audio", "rp");

    public static final ContentType AUDIO_RH = new ContentType("audio", "rh");

    public static final ContentType AUDIO_SSD = new ContentType("audio", "ssd");

    public static final ContentType TEXT = new ContentType("text");

    public static final ContentType TEXT_TFIDF = new ContentType("text", "tfidf");

    public static final ContentType IMAGE = new ContentType("image");

    public static final ContentType UNKNOWN = new ContentType("unknown");

    private static final ContentType[] contentTypes = { AUDIO, AUDIO_RP, AUDIO_RH, AUDIO_SSD, IMAGE, TEXT, TEXT_TFIDF };

    private ContentType(String type) {
        this(type, null);
    }

    private ContentType(String mainType, String subtype) {
        this.mainType = mainType;
        this.subType = subtype;
    }

    public String getMainType() {
        return mainType;
    }

    public String getSubtype() {
        return subType;
    }

    @Override
    public String toString() {
        return mainType + (subType != null ? "-" + subType : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentType) {
            ContentType ct = (ContentType) obj;
            return StringUtils.equals(mainType, ct.mainType) && StringUtils.equals(subType, ct.subType);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mainType.hashCode() + (subType != null ? subType.hashCode() : 0);
    }

    public static ContentType parse(String s) {
        String[] parts = s.split("-", 2);
        String subType = parts.length > 1 ? parts[1] : null;
        int indexOf = ArrayUtils.indexOf(contentTypes, new ContentType(parts[0], subType));
        if (indexOf != -1) {
            return contentTypes[indexOf];
        } else {
            // check if we know the main type alone
            indexOf = ArrayUtils.indexOf(contentTypes, new ContentType(parts[0], null));
            if (indexOf != -1) {
                return contentTypes[indexOf];
            } else {
                Logger.getLogger("at.tuwien.ifs.somtoolbox").warning(
                        "Unknown content type '" + s + "', known values are " + Arrays.toString(contentTypes)
                                + ". Defaulting to '" + UNKNOWN + "'.");
                return UNKNOWN;
            }
        }
    }

    public boolean isAudio() {
        return mainType.equals("audio");
    }

    public boolean isText() {
        return mainType.equals("text");
    }

    public boolean isImage() {
        return mainType.equals("image");
    }

    public boolean isSparse() {
        return isText();
    }

    public static void main(String[] args) {
        System.out.println(ContentType.parse("audio"));
        System.out.println(ContentType.parse("audio-rp"));
        System.out.println(ContentType.parse("text"));
        System.out.println(ContentType.parse("text-bow"));
    }

}
