/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.github.caciocavallosilano.cacio.ctc;

import sun.misc.Unsafe;

import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

public class CTCPreloadAgent {
    static {
        try {
            Field toolkit = Toolkit.class.getDeclaredField("toolkit");
            toolkit.setAccessible(true);
            toolkit.set(null, new CTCToolkit());

            Field defaultHeadlessField = GraphicsEnvironment.class.getDeclaredField("defaultHeadless");
            defaultHeadlessField.setAccessible(true);
            defaultHeadlessField.set(null,Boolean.TRUE);
            Field headlessField = GraphicsEnvironment.class.getDeclaredField("headless");
            headlessField.setAccessible(true);
            headlessField.set(null,Boolean.TRUE);

            Class<?> geCls = Class.forName("java.awt.GraphicsEnvironment$LocalGE");
            Field ge = geCls.getDeclaredField("INSTANCE");
            ge.setAccessible(true);
            defaultHeadlessField.set(null, Boolean.FALSE);
            headlessField.set(null,Boolean.FALSE);

            setFinalStatic(ge, new CTCGraphicsEnvironment());

            String propertyFontManager = System.getProperty("cacio.font.fontmanager");
            if (propertyFontManager != null) {
                FontManagerUtil.setFontManager(propertyFontManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.setProperty("swing.defaultlaf", MetalLookAndFeel.class.getName());
    }

    // https://stackoverflow.com/a/71465198
    public static void setFinalStatic(Field field, Object value) throws Exception{
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        Object fieldBase = unsafe.staticFieldBase(field);
        long fieldOffset = unsafe.staticFieldOffset(field);

        unsafe.putObject(fieldBase, fieldOffset, value);
    }

    public static void premain(String args){}
}
