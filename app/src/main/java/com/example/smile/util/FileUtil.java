package com.example.smile.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class FileUtil {

    public static void coverAndWrite(Context context, String filename, String fileContent) {

        FileOutputStream fos = null;
        try {
            while(context==null);
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(fileContent.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBitmapFile(Context ctx, String fileName, Bitmap bitmap){
        FileOutputStream fos = null;
        try {
            fos = ctx.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Bitmap readBitmapFile(Context ctx, String fileName){
        try {
            FileInputStream fis = ctx.openFileInput(fileName);
            Bitmap photo = BitmapFactory.decodeStream(fis);
            return photo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            StringBuffer sb = new StringBuffer();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = bfr.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFileByUrl(String url) {
        try {
            FileInputStream fis = new FileInputStream(new File(url));
            StringBuffer sb = new StringBuffer();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = bfr.readLine()) != null) {
                sb.append(line+"\r");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean fileExists(Context context, String filename) {
        try {
            File f = new File(context.getFilesDir() + "/" + filename);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static File creatSDFile(Context ctx, String fileName) throws IOException {
        File file = null;
        try {
            file = new File(ctx.getExternalFilesDir("") + fileName);
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File creatSDDir(Context ctx, String dirName) {
        File dir;
        if(dirName.contains(ctx.getExternalFilesDir("").toString())) dir = new File(dirName);
        else dir = new File(ctx.getExternalFilesDir("") + dirName);
        dir.mkdirs();
        return dir;
    }

    public static boolean isSDFileExist(Context ctx, String fileName) {
        File file = new File(ctx.getExternalFilesDir("") + fileName);
        return file.exists();
    }

    public static List<File> getFileListByDirPath(String path, FileFilter filter) {
        File directory = new File(path);
        File[] files = directory.listFiles(filter);
        List<File> result = new ArrayList<>();
        if (files == null) {
            return new ArrayList<>();
        }

        for (int i = 0; i < files.length; i++) {
            result.add(files[i]);
        }
        Collections.sort(result, new FileComparator());
        return result;
    }

    public static List<File> getFolderListByDirPath(String path, FileFilter filter) {
        File directory = new File(path);
        File[] files = directory.listFiles(filter);
        List<File> result = new ArrayList<>();
        if (files == null) {
            return new ArrayList<>();
        }

        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory())
                result.add(files[i]);
        }
        Collections.sort(result, new FileComparator());
        return result;
    }

    public static String cutLastSegmentOfPath(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 获取文件长度
     *
     * @param file 文件
     * @return 文件长度
     */
    public static long getFileLength(final File file) {
        if (!isFile(file)) return -1;
        return file.length();
    }

    /**
     * 判断是否是文件
     *
     * @param file 文件
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isFile(final File file) {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * 根据地址获取当前地址下的所有目录和文件，并且排序,同时过滤掉不符合大小要求的文件
     *
     * @param path
     * @return List<File>
     */
    public static List<File> getFileList(String path, FileFilter filter, boolean isGreater, long targetSize) {
        List<File> list = FileUtil.getFileListByDirPath(path, filter);
        //进行过滤文件大小
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            File f = (File) iterator.next();
//            if (f.isFile()) {
                //获取当前文件大小
//                long size = FileUtil.getFileLength(f);
//                if (isGreater) {
//                    //当前想要留下大于指定大小的文件，所以过滤掉小于指定大小的文件
//                    if (size < targetSize) {
//                        iterator.remove();
//                    }
//                } else {
//                    //当前想要留下小于指定大小的文件，所以过滤掉大于指定大小的文件
//                    if (size > targetSize) {
//                        iterator.remove();
//                    }
//                }
//            }
        }
        return list;
    }

    static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            if(f1.isDirectory() && f2.isDirectory()) {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }

            if(f1.isDirectory() && f2.isFile()) {
                // Show directories above files
                return -1;
            }
            if(f1.isFile() && f2.isDirectory()) {
                // Show files below directories
                return 1;
            }

            if(f1.lastModified() != f2.lastModified()) {
                if(f1.lastModified() < f2.lastModified()) return 1;
                else return -1;
            }
            // Sort the directories alphabetically
            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

    /**
     * 拷贝Asset文件夹到sd卡
     *
     * @param context
     * @param fromDir 如果拷贝assets ，则传入 ""
     * @param destDir 目的地
     * @throws IOException
     */
    public static void copyAssetsDir(Context context, String fromDir, String destDir) throws IOException {
        String[] files = context.getAssets().list(fromDir);
        for (String f : files) {
            copyFile(context.getAssets().open(fromDir + File.separator + f), destDir + File.separator + f);
        }
    }

    public static void copyFile(InputStream in, String newPath) {
        new File(newPath).getParentFile().mkdirs();
        try (
                InputStream inStream = in;
                FileOutputStream fs = new FileOutputStream(newPath)
        ) {
            int byteread;
            byte[] buffer = new byte[4096];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
                fs.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}