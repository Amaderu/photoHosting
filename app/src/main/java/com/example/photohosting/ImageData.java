package com.example.photohosting;

import android.text.format.DateFormat;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Date;

public class ImageData {
    public String name;
    public long fileSize;
    public String fileType;

    //15 дек. 2020 г., 09:01:07
    public long creatingTime;
    public long updateTime;

    public ImageData(String name, long fileSize, String fileType, long creatingTime, long updateTime) {
        this.name = name;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.creatingTime = creatingTime;
        this.updateTime = updateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getCreatingTime() {
        return creatingTime;
    }

    public void setCreatingTime(long creatingTime) {
        this.creatingTime = creatingTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    public String metadata(){
        return "Image{" +
                "name='" + name + '\'' + ", " +
                "creatingTime='" + fileSize + '\'' + ", " +
                "creatingTime='" + fileType + '\'' + ", " +
                "creatingTime='" + creatingTime + '\'' + ", " +
                "updateTime='" + updateTime + '\'' +
                '}';
    }

   /* @Override
    public String toString() {
        return "Получена метадата";
        *//*return "Image{" +
                "name='" + name + '\'' + ", " +
                "creatingTime='" + fileSize + '\'' + ", " +
                "creatingTime='" + fileType + '\'' + ", " +
                "creatingTime='" + creatingTime + '\'' + ", " +
                "updateTime='" + updateTime + '\'' +
                '}';*//*
    }*/

    public String MBfile() {
        /*if (fileSize.matches("^\\d*$")) {
            fileSize = MBfile(fileSize);
        }*/
        ///fileSize = fileSize.replace("\"", "");
        double result = fileSize / 1048576; //1024*1024
        String formattedDouble = new DecimalFormat("#0.00").format(result);
        return formattedDouble + "MB";
    }
    public String getStringSizeLengthFile() {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;


        if(fileSize < sizeMb) {
            Log.d("FileSize",df.format(fileSize / sizeKb)+ " Kb");
            return df.format(fileSize / sizeKb)+ " Kb";

        }
        else if(fileSize < sizeGb)
        {
            Log.d("FileSize",df.format(fileSize / sizeMb)+ " Mb");
            return df.format(fileSize / sizeMb) + " Mb";
        }
        else if(fileSize < sizeTerra)
        {
            Log.d("FileSize",df.format(fileSize / sizeGb)+ " Gb");
            return df.format(fileSize / sizeGb) + " Gb";
        }

        return "not convert";
    }
    public String dateConvert() {
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(creatingTime)).toString();
        return dateString;
    }

}

/*Collections.sort(dateList, new Comparator<Date>(){
public int compare(Date date1, Date date2){
        return date1.after(date2);
        }
        });*/


/*class CompareByDate implements Comparator<Image>{

    @Override
    public int compare(Image o1, Image o2) {
        return o1.creatingTime.compareTo(o2.creatingTime);
    }

}*/

class CompareByName implements Comparator<ImageData> {

    @Override
    public int compare(ImageData o1, ImageData o2) {
        return o1.name.compareTo(o2.name);
    }

}
