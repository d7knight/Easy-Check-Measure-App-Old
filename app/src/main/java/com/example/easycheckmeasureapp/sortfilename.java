package com.example.easycheckmeasureapp;
import java.io.File;
import java.util.Comparator;

//sorts based on the files name
public class sortfilename implements Comparator<File> {
  @Override
  public int compare(File f1, File f2) {
        return f1.getName().compareTo(f2.getName());
  }
}
