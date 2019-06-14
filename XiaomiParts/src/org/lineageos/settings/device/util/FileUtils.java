/*
 * Copyright (C) 2018 The Xiaomi-SDM660 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.lineageos.settings.device.util;

import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public final class FileUtils {

    public static boolean fileWritable(String filename) {
        return fileExists(filename) && new File(filename).canWrite();
    }

    private static boolean fileExists(String filename) {
        if (filename == null) {
            return false;
        }
        return new File(filename).exists();
    }

    public static boolean setValue(String path, int value) {
        if (fileWritable(path)) {
            if (path == null) {
                return false;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write(Integer.toString(value).getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean setValue(String path, boolean value) {
        if (fileWritable(path)) {
            if (path == null) {
                return false;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write((value ? "1" : "0").getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean setValue(String path, double value) {
        if (fileWritable(path)) {
            if (path == null) {
                return false;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write(Long.toString(Math.round(value)).getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean setValue(String path, String value) {
        if (fileWritable(path)) {
            if (path == null) {
                return false;
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File(path));
                fos.write(value.getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static String readLine(String filename) {
        if (filename == null) {
            return null;
        }
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(filename), 1024);
            line = br.readLine();
        } catch (IOException e) {
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return line;
    }

    public static boolean getValue(String filename, boolean defValue) {
        String fileValue = readLine(filename);
        if (fileValue != null) {
            return !fileValue.equals("0");
        }
        return defValue;
    }

    public static void setProp(String prop, boolean value) {
        if (value) {
            SystemProperties.set(prop, "1");
        } else {
            SystemProperties.set(prop, "0");
        }
    }

    public static void setProp(String prop, int value) {
        SystemProperties.set(prop, String.valueOf(value));
    }

    public static void setProp(String prop, String value) {
        SystemProperties.set(prop, value);
    }

    public static boolean getProp(String prop, boolean defaultValue) {
        return SystemProperties.getBoolean(prop, defaultValue);
    }

    public static int getProp(String prop, int defaultValue) {
        return SystemProperties.getInt(prop, defaultValue);
    }

    public static String getProp(String prop, String defaultValue) {
        return SystemProperties.get(prop, defaultValue);
    }
}
