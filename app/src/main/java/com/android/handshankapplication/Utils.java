package com.android.handshankapplication;

public class Utils {
    public static int byteArrayToInt(byte[] b, int off) {
        return b[off + 3] & 0xFF |
                (b[off + 2] & 0xFF) << 8 |
                (b[off + 1] & 0xFF) << 16 |
                (b[off] & 0xFF) << 24;
    }
//
//    public static byte[] intToByteArray(int a) {
//        return new byte[]{
//                (byte) ((a >> 24) & 0xFF),
//                (byte) ((a >> 16) & 0xFF),
//                (byte) ((a >> 8) & 0xFF),
//                (byte) (a & 0xFF)
//        };
//    }

//    public static int byteArrayToInt(byte[] bytes) {
//        int value = 0;
//        for (int i = 0; i < bytes.length; i++) {
//            int shift = (bytes.length - 1 - i) * 8;
//            value = (bytes[i] & 0x000000FF) << shift;
//        }
//        return value;
//
//    }


    /**
     * int到byte[]
     *
     * @param i
     * @return
     */

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);

        return result;

    }

    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }


    //将16进制字符串转换为byte[]
    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }
}
