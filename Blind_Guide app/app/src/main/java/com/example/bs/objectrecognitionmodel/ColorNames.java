package com.example.bs.objectrecognitionmodel;

import java.util.ArrayList;
public class ColorNames {

    /**
     * Initialize the color list that we have.
     */
    private ArrayList<ColorName> initColorList() {
        ArrayList<ColorName> colorList = new ArrayList<ColorName>();
        colorList.add(new ColorName("Blue", 0x00, 0x00, 0x8B));
        colorList.add(new ColorName("White", 0xFF, 0xFF, 0xFF));
        colorList.add(new ColorName("Black", 0x00, 0x00, 0x00));
        colorList.add(new ColorName("Brown", 0x80, 0x40, 0x00));
        colorList.add(new ColorName("Cyan", 0x00, 0xEE, 0xEE));
        colorList.add(new ColorName("Gold", 0xEE, 0xAD, 0x0E));
        colorList.add(new ColorName("Green", 0x22, 0x8B, 0x22));
        colorList.add(new ColorName("Orange", 0xFF, 0x45, 0x00));
        colorList.add(new ColorName("Red", 0xFF, 0x00, 0x00));
        colorList.add(new ColorName("Violet", 0x94, 0x00, 0xD3));
        colorList.add(new ColorName("Pink", 0xFF, 0x14, 0x93));
        colorList.add(new ColorName("White", 0xAD, 0xD8, 0xE6));
        colorList.add(new ColorName("Yellow", 0xFF, 0xFF, 0x00));
        colorList.add(new ColorName("Lemon", 0x00, 0xFF, 0x00));
        colorList.add(new ColorName("Purple", 0xA0, 0x20, 0xF0));
        colorList.add(new ColorName("Rose", 0xFF, 0xE4, 0xE1));
        colorList.add(new ColorName("Nabiti", 0xA5, 0x2A, 0x2A));
        colorList.add(new ColorName("Gray", 0x4D, 0x4D, 0x4D));
        return colorList;
    }

    /**
     * Get the closest color name from our list
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public String  getColorNameFromRgb(int r, int g, int b) {
        ArrayList<ColorName> colorList = initColorList();
        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;
        for (ColorName c : colorList) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return "No matched color name.";
        }
    }

    public class ColorName {
        public int r, g, b;
        public String name;

        public ColorName(String name, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.name = name;
        }

        public int computeMSE(int pixR, int pixG, int pixB) {
            return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }

        public int getR() {
            return r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        public String getName() {
            return name;
        }
    }
}