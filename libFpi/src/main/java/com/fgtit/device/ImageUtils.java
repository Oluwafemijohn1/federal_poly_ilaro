package com.fgtit.device;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import com.fgtit.data.wsq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


public class ImageUtils {
    /**
     * 灰度化
     * @param bmSrc
     * @return
     */
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    /*public static Bitmap Bernsen(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int windowSize = 0;
        int contrastThreshold = 0;
        //BufferedImage thresholded = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int min = Integer.MAX_VALUE;
                int max = Integer.MIN_VALUE;
                int centerPixel = image.getRGB(j, i);

                // get minimum and maximum intensity values in the neighborhood
                for (int u = i - windowSize / 2; u <= i + windowSize / 2; u++) {
                    for (int v = j - windowSize / 2; v <= j + windowSize / 2; v++) {
                        if (u >= 0 && u < height && v >= 0 && v < width) {
                            int pixel = image.getRGB(v, u);
                            min = Math.min(min, pixel & 0xff);
                            max = Math.max(max, pixel & 0xff);
                        }
                    }
                }

                // compute the threshold value
                int threshold = (min + max) / 2;

                // adjust the threshold based on contrast
                int contrast = max - min;
                if (contrast < contrastThreshold) {
                    threshold = (int) (min + contrast / 2);
                }

                // apply threshold to the center pixel
                int newPixel = (centerPixel & 0xff) > threshold ? 255 : 0;
                thresholded.setRGB(j, i, newPixel | (newPixel << 8) | (newPixel << 16));
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        //image.setImageBitmap(temp);
        return temp;
    }
*/

    /**
     * OTSU二值化方法
     * @param img
     * @return
     */
    public static Bitmap binarization(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
        int average = 0;// 灰度平均值
        int graysum = 0;
        int graymean = 0;
        int grayfrontmean = 0;
        int graybackmean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        img.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) { // 不算边界行和列，为避免越界
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = pix[x] & 0xff;
                pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);// 计算每个坐标点的灰度
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graysum += pixelGray;
            }
        }
        graymean = (int) (graysum / area);// 整个图的灰度平均值
        average = graymean;
        Log.i(TAG,"Average:"+average);
        for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
        {
            for (int j = 0; j < height; j++) {
                if (((gray[i][j]) & (0x0000ff)) < graymean) {
                    graybackmean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayfrontmean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontvalue = (int) (grayfrontmean / front);// 前景中心
        int backvalue = (int) (graybackmean / back);// 背景中心
        float G[] = new float[frontvalue - backvalue + 1];// 方差数组
        int s = 0;
        Log.i(TAG,"Front:"+front+"**Frontvalue:"+frontvalue+"**Backvalue:"+backvalue);
        for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法（OTSU算法）
        {
            back = 0;
            front = 0;
            grayfrontmean = 0;
            graybackmean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        graybackmean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayfrontmean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayfrontmean = (int) (grayfrontmean / front);
            graybackmean = (int) (graybackmean / back);
            G[s] = (((float) back / area) * (graybackmean - average)
                    * (graybackmean - average) + ((float) front / area)
                    * (grayfrontmean - average) * (grayfrontmean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontvalue - backvalue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        //image.setImageBitmap(temp);
        return temp;
    }

    //bitmap生成bmp图片，并保存到本地
    public static byte[] ConvertBitmap2BMP(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int wWidth = w * 3 + w % 4;
        int bmpDateSize = h * wWidth;
        int size = 14 + 40 + bmpDateSize;
        byte buffer[] = new byte[size];


        // 1.BMP文件头 14
        buffer[0] = 0x42; //bfType 2bytes
        buffer[1] = 0x4D;
        buffer[2] = (byte) ((size >> 0) & 0xFF); //bfSize 4bytes
        buffer[3] = (byte) ((size >> 8) & 0xFF);
        buffer[4] = (byte) ((size >> 16) & 0xFF);
        buffer[5] = (byte) ((size >> 24) & 0xFF);
        buffer[6] = 0x00; //bfReserved1 2bytes
        buffer[7] = 0x00;
        buffer[8] = 0x00; //bfReserved2 2bytes
        buffer[9] = 0x00;
        buffer[10] = 0x36; //bfOffBits 14+40 4bytes
        buffer[11] = 0x00;
        buffer[12] = 0x00;
        buffer[13] = 0x00;
        // 2.BMP信息头 40
        buffer[14] = 0x28; //biSize 40 4bytes
        buffer[15] = 0x00;
        buffer[16] = 0x00;
        buffer[17] = 0x00;
        buffer[18] = (byte) ((w >> 0) & 0xFF); //biWidth 4bytes
        buffer[19] = (byte) ((w >> 8) & 0xFF);
        buffer[20] = (byte) ((w >> 16) & 0xFF);
        buffer[21] = (byte) ((w >> 24) & 0xFF);
        buffer[22] = (byte) ((h >> 0) & 0xFF); //biHeight 4bytes
        buffer[23] = (byte) ((h >> 8) & 0xFF);
        buffer[24] = (byte) ((h >> 16) & 0xFF);
        buffer[25] = (byte) ((h >> 24) & 0xFF);
        buffer[26] = 0x01; //biPlanes 2bytes
        buffer[27] = 0x00;
        buffer[28] = 0x18; //biBitCount 24位位图 2bytes
        buffer[29] = 0x00;
        buffer[30] = 0x00; //biCompression 4bytes
        buffer[31] = 0x00;
        buffer[32] = 0x00;
        buffer[33] = 0x00;
        buffer[34] = 0x00; //biSizeImage 4bytes
        buffer[35] = 0x00;
        buffer[36] = 0x00;
        buffer[37] = 0x00;
        buffer[38] = 0x00; //biXpelsPerMeter 4bytes
        buffer[39] = 0x00;
        buffer[40] = 0x00;
        buffer[41] = 0x00;
        buffer[42] = 0x00; //biYPelsPerMeter 4bytes
        buffer[43] = 0x00;
        buffer[44] = 0x00;
        buffer[45] = 0x00;
        buffer[46] = 0x00; //biClrUsed 4bytes
        buffer[47] = 0x00;
        buffer[48] = 0x00;
        buffer[49] = 0x00;
        buffer[50] = 0x00; //biClrImportant 4bytes
        buffer[51] = 0x00;
        buffer[52] = 0x00;
        buffer[53] = 0x00;

        byte bmpData[] = new byte[bmpDateSize];
        //Log.d("hello","bmpDateSize:"+bmpDateSize);
        for (int nCol = 0, nRealCol = h - 1; nCol < h; ++nCol, --nRealCol) {
            for (int wRow = 0, wByteIdex = 0; wRow < w; wRow++, wByteIdex += 3) {
                int clr = bitmap.getPixel(wRow, nCol);
                //clr = clr == 0 ? 0xFFFFFF : clr; //黑色背景转为白色
                bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
            }
        }

        System.arraycopy(bmpData, 0, buffer, 54, bmpDateSize);

        // 输出到sdcard查看
        //String dir = Environment.getExternalStorageDirectory();
        String imageFileName = String.valueOf(System.currentTimeMillis());
        String path =  Environment.getExternalStorageDirectory().getAbsolutePath();

        String sDir = Environment.getExternalStorageDirectory() + "/FPIB/Bitmap";
        File destDir = new File(sDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try {

            FileOutputStream fos = new FileOutputStream(new File( sDir+"/"+ imageFileName + ".bmp"));
            fos.write(buffer);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }


    //生成wsq格式文件，并保存到本地
    public static void SaveWsqFile(byte[] rawdata, int rawsize, String filename) {
        byte[] outdata = new byte[rawsize];
        int[] outsize = new int[1];

        if (rawsize == 73728) {
            wsq.getInstance().RawToWsq(rawdata, rawsize, 256, 288, outdata, outsize, 2.833755f);
        } else if (rawsize == 92160) {
            wsq.getInstance().RawToWsq(rawdata, rawsize, 256, 360, outdata, outsize, 2.833755f);
        }

        try {
            String sDir = Environment.getExternalStorageDirectory() + "/FP";
            File destDir = new File(sDir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File fs = new File(sDir+"/" + filename);
            if (fs.exists()) {
                fs.delete();
            }
            new File(sDir+"/" + filename);
            RandomAccessFile randomFile = new RandomAccessFile(sDir+"/" + filename, "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.write(outdata, 0, outsize[0]);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
