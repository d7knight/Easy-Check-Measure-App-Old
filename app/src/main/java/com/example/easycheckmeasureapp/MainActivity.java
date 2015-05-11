package com.example.easycheckmeasureapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static String storageDir = Environment.getExternalStorageDirectory().getPath()+"/checkmeasures/";
	public static String filename;
	public static int cms;
	public TableLayout tbl;
	boolean correctionneeded;

    @Override
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        correctionneeded=false;
        Intent intent = getIntent();
        String editf=intent.getStringExtra("editcm");

        setContentView(R.layout.activity_main);
        tbl=(TableLayout)findViewById(R.id.TableLayout1);
        
        File f = new File(storageDir);
        cms = f.listFiles()!=null?f.listFiles().length:0; 
       if (editf==null){
        
        if (!f.exists()){f.mkdir();}
        
       }
       else{
       filename=editf.replaceFirst(storageDir,"");
        updatedata(editf);
    	
        }
       }
    public void addrow(View v){
   LayoutInflater inflater = getLayoutInflater();
   TableLayout scv = (TableLayout)inflater.inflate(R.layout.row,null);
   TableRow row=(TableRow) scv.getChildAt(0);
   ((TableLayout)row.getParent()).removeView(row);
   tbl.addView(row);
   
    }
    
    private void haltforcorrection(String error){
    	
    	 Context context = getApplicationContext();
		  CharSequence text = "There is likely an error in this " +
		  		"checkmeasure for the following measurement " +error + " please correct this error before sending your checkmeasure.";
		  int duration = Toast.LENGTH_LONG;
		  Toast toast = Toast.makeText(context, text, duration);
		  toast.show();
		  correctionneeded=true;
		  return;	
    	
    }
   private void nextfile(String company){
	  File f; 
	  int i=1;
	  while(true){
	  filename=company+"checkmeasure" + i + ".htm";
	  f=new File(storageDir + filename);
      if (!f.exists())break;
      i++;
	  } 
	   
   }
    
   private boolean needcorrection(String a){
	   int fraction=a.indexOf("/");
	   boolean basic=(fraction==-1);
	   if (basic)return false;
	   boolean numeratorsingledigit=fraction>0
			                        &&a.charAt(fraction-1)>='0'
			                        &&a.charAt(fraction-1)<='9'
			                        &&(fraction>1&&a.charAt(fraction-2)==' ' || fraction==1);
       boolean denominatorsingledigit=fraction<(a.length()-1)
					                &&a.charAt(fraction+1)>='0'
					                &&a.charAt(fraction+1)<='9'
					                &&(fraction<(a.length()-2)&&a.charAt(fraction+2)==' ' || fraction==(a.length()-2));
	   boolean numeratordoubledigit=fraction>1
					                        &&a.charAt(fraction-1)>='0'
					                        &&a.charAt(fraction-1)<='9'
					                        &&a.charAt(fraction-2)>='0'
							                &&a.charAt(fraction-2)<='9'
					                        &&(fraction>2&&a.charAt(fraction-3)==' ' || fraction==2);
	   boolean denominatordoubledigit=fraction<(a.length()-2)
							                &&a.charAt(fraction+1)>='0'
							                &&a.charAt(fraction+1)<='9'
							                &&a.charAt(fraction+2)>='0'
										    &&a.charAt(fraction+2)<='9'
							                &&((fraction<(a.length() - 3)&& a.charAt(fraction + 3) == ' ') || (fraction == (a.length() - 3)));
      if(!numeratorsingledigit&&!numeratordoubledigit
        ||!denominatorsingledigit&&!denominatordoubledigit)return true;	                
	  int numerator=0;
	  int denominator=0;
      
      if (numeratorsingledigit){
		  
		  if (denominatorsingledigit){
		  numerator=(int)a.charAt(fraction-1)-(int)'0';
		  denominator=(int)a.charAt(fraction+1)-(int)'0';
		  }
		  else if (denominatordoubledigit){
	      numerator=(int)a.charAt(fraction-1)-(int)'0';
		  denominator=((int)(a.charAt(fraction+1)-'0'))*10+(int)a.charAt(fraction+2)-(int)'0';  
		  }
	  }
      else if(numeratordoubledigit){
    	  if (denominatorsingledigit){
    		  numerator=((int)(a.charAt(fraction-2)-'0'))*10+(int)a.charAt(fraction-1)-(int)'0';
    		  denominator=(int)a.charAt(fraction+1)-(int)'0';
    		  }
         else if (denominatordoubledigit){
        	  numerator=((int)(a.charAt(fraction-2)-'0'))*10+(int)a.charAt(fraction-1)-(int)'0';
    		  denominator=((int)(a.charAt(fraction+1)-'0'))*10+(int)a.charAt(fraction+2)-(int)'0';  
    		  }
	   
      }
      boolean inproper=numerator>=denominator||numerator<1||denominator%2!=0;
      return inproper;
   }
    
    
    
    
    public void savedata(View v){
    	boolean wrotesomething=false;
    	int children=tbl.getChildCount();
    	TableRow tbr=(TableRow)tbl.getChildAt(1);
    	EditText company=(EditText)tbr.getChildAt(1);
    	
    	tbr=(TableRow)tbl.getChildAt(2);
    	EditText customer=(EditText)tbr.getChildAt(1);
    	tbr=(TableRow)tbl.getChildAt(3);
    	EditText address=(EditText)tbr.getChildAt(1);
    	String co=company.getText().toString();
    	String c=customer.getText().toString();
    	String a=address.getText().toString();
    	String l,w,r,n;
    	nextfile(co);
    	File thefile = new File(storageDir + filename);
    	 
    
    	try {
    		  if (thefile.exists()){
    			  nextfile(co);
    			  thefile = new File(storageDir + filename);
    			  thefile.createNewFile();
    		  }
    		  else thefile.createNewFile();
    		                         
    		 
    
    		FileOutputStream  out = new FileOutputStream(thefile);
    		OutputStreamWriter  outputStreamWriter = new OutputStreamWriter(out);
    		  
    		  
             SimpleDateFormat dfDate  = new SimpleDateFormat("EEE, MMM d yyyy hh:mm aaa");
              String data="";
              Calendar cal = Calendar.getInstance(); 
              data=dfDate.format(cal.getTime());
              outputStreamWriter.write("<html><body><p>Company: "
                         + co + "<br>" + "Customer: " 
                         + c + "<br>" + "Address: " + a + "<br>" 
                         + "Date: "+ data + "<br>"  + filename + "<br>" + "</p>"
                         +"<table border=1 style=background-color:white;" 
                         +"border:1px:black;width:80%;border-collapse:collapse;>"+
            		  	"<tr style=background-color:orange;color:black;>");
              if(!co.equals("")||!c.equals("")||!a.equals(""))wrotesomething=true;
              outputStreamWriter.write(
              "<th style=padding:3px;>Room</th>"+
              "<th style=padding:3px;>Bracket Type</th>" +
              "<th style=padding:3px;>Width</th>"+
              "<th style=padding:3px;>Length</th>"+
              "<th style=padding:3px;>Control</th>" +
              "<th style=padding:3px;>Special Note</th></tr>");
              LinearLayout buttons;
              String bracket;
              String control;
              for (int i=5; i<children; i++){
            	  tbr = (TableRow)tbl.getChildAt(i);
            	      	
                  r=((EditText)tbr.getChildAt(0)).getText().toString();
                  r=r.toUpperCase();
                  w=((EditText)tbr.getChildAt(1)).getText().toString();
                  l=((EditText)tbr.getChildAt(2)).getText().toString();
                  n=((EditText)tbr.getChildAt(3)).getText().toString();
                  buttons=(LinearLayout)tbr.getChildAt(4);
                  bracket=((Button)buttons.getChildAt(0)).getText().toString();
                  control=((Button)buttons.getChildAt(1)).getText().toString();
                  if (control.equals("L"))control="Left Control";
                  else if (control.equals("R")) control="Right Control";
                  if(!r.equals("")||!w.equals("")||!l.equals("")||!n.equals(""))wrotesomething=true;
                  boolean emptyrow=(r.equals("")&&w.equals("")&&l.equals("")&&n.equals(""));
                  correctionneeded=false;
                  if (!emptyrow){
                	  
                	  if((needcorrection(w))){
                		  
                		  haltforcorrection(w);
                		  ((EditText)tbr.getChildAt(1)).requestFocus();
                		  
                		  outputStreamWriter.flush();
                  		  outputStreamWriter.close();
                          out.close();
                		  return;
                		
                	  }
                	  if(needcorrection(l)){
                		  
                			 
                		  haltforcorrection(l);
                		  ((EditText)tbr.getChildAt(2)).requestFocus();
                		 
                		  outputStreamWriter.flush();
                  		  outputStreamWriter.close();
                          out.close();
                		  return; 
                	  }
                	
                	  
                	  
                	  
                	  
                  outputStreamWriter.write("<tr><td style=padding:3px;>"+r+"</td>");
                  outputStreamWriter.write("<td style=padding:3px;>"+bracket+"</td>");
                  outputStreamWriter.write("<td style=padding:3px;>"+w+"</td>");
                  outputStreamWriter.write("<td style=padding:3px;>"+l+"</td>");
                  outputStreamWriter.write("<td style=padding:3px;>"+control+"</td>");
                  outputStreamWriter.write("<td style=padding:3px;>"+n+"</td></tr>");
            	  }
                 
              }
              
              outputStreamWriter.write("</table></body></html>");  
                 
            	  outputStreamWriter.flush();
          
          		  outputStreamWriter.close();
                  out.close();
           
          }
          catch (IOException e) {
              Log.e("Exception", "File write failed: " + e.toString());
              
              }
    	
    	
    	
    	if(!wrotesomething)thefile.delete();
          } 
    	  
    
    public void send(View v) {
    	savedata(v);
    	if(!correctionneeded){
    	Intent i = new Intent(this, Individual.class);
  		startActivity(i);
    	}
}
    
    private void updatedata(String editf){

    	TableRow tbr=(TableRow)tbl.getChildAt(1);
    	EditText company=(EditText)tbr.getChildAt(1);
    	tbr=(TableRow)tbl.getChildAt(2);
    	EditText customer=(EditText)tbr.getChildAt(1);
    	tbr=(TableRow)tbl.getChildAt(3);
    	EditText address=(EditText)tbr.getChildAt(1);
       String filecontents="Read failed";
       try{
    	   filecontents=readfromFile(editf);
       }
        catch (IOException e) {
        Log.e("Exception", "File read failed: " + e.toString());
        return;
        } 
       
   	   String temp="Company: ";
   	   int b=filecontents.indexOf(temp)+temp.length();
	   if (filecontents.equals("")||b==-1)return;
	   temp="<br>";
   	   int e=filecontents.indexOf(temp);
   	   company.setText(filecontents.substring(b,e));
       filecontents=filecontents.substring(e+temp.length());
   	   temp="Customer: ";
   	   b=filecontents.indexOf(temp)+temp.length();
   	   temp="<br>";
   	   e=filecontents.indexOf(temp);

   	   customer.setText(filecontents.substring(b,e));
       filecontents=filecontents.substring(e+temp.length());
   	   temp= "Address: ";
   	   b=filecontents.indexOf(temp)+temp.length();
	   temp="<br>";
	   e=filecontents.indexOf(temp);  
	   address.setText(filecontents.substring(b,e));
    	
    	
	   LinearLayout buttons; 
	   String control;
              for (int i=5; ; i++){
            	  
            	  temp= "<td style=padding:3px;>";
              	  b=filecontents.indexOf(temp)+temp.length();
              	  if (filecontents.indexOf(temp)==-1)break;
            	  
            	  
            	  if (i>5)addrow(null);
            	  tbr=(TableRow)tbl.getChildAt(i);
            	  buttons=(LinearLayout)tbr.getChildAt(4);
            	  
            	 
           	      temp="</td>";
           	      e=filecontents.indexOf(temp);
           	      ((EditText)tbr.getChildAt(0)).setText(filecontents.substring(b,e));
                  filecontents=filecontents.substring(e+temp.length());
            	  
            	  
                  temp= "<td style=padding:3px;>";
              	  b=filecontents.indexOf(temp)+temp.length();
           	      temp="</td>";
           	      e=filecontents.indexOf(temp);
           	      ((Button)buttons.getChildAt(0)).setText(filecontents.substring(b,e));
                  filecontents=filecontents.substring(e+temp.length());
            	  
                  temp= "<td style=padding:3px;>";
              	  b=filecontents.indexOf(temp)+temp.length();
           	      temp="</td>";
           	      e=filecontents.indexOf(temp);
           	      ((EditText)tbr.getChildAt(1)).setText(filecontents.substring(b,e));
                  filecontents=filecontents.substring(e+temp.length());
            	  
            	 
            	  temp= "<td style=padding:3px;>";
              	  b=filecontents.indexOf(temp)+temp.length();
           	      temp="</td>";
           	      e=filecontents.indexOf(temp);
           	      ((EditText)tbr.getChildAt(2)).setText(filecontents.substring(b,e));
                  filecontents=filecontents.substring(e+temp.length());
            	  
            	  
       
            	 
            	  temp= "<td style=padding:3px;>";
              	  b=filecontents.indexOf(temp)+temp.length();
           	      temp="</td>";
           	      e=filecontents.indexOf(temp);
           	      control=filecontents.substring(b,e);
           	      if(control.equals("Left Control")) control="L";
           	      else if(control.equals("Right Control"))control="R";
           	      ((Button)buttons.getChildAt(1)).setText(control);
                  filecontents=filecontents.substring(e+temp.length());
            	 
                 
                 temp= "<td style=padding:3px;>";
             	  b=filecontents.indexOf(temp)+temp.length();
             	  if (b==-1)break;
          	      temp="</td>";
          	      e=filecontents.indexOf(temp);
          	      if (e==-1)break;
          	      ((EditText)tbr.getChildAt(3)).setText(filecontents.substring(b,e));
                  filecontents=filecontents.substring(e+temp.length());
            	  
            	  
            	  
                 
              }
           	      
            
          
        
    }
	public void viewer(View v) {
	   savedata(v);
	   if(!correctionneeded){
		Intent i = new Intent(this, Viewer.class);
		startActivity(i);
	   }
	}
	public void bracket(View v) {
		Button b=(Button)v;
		String text=b.getText().toString();
		if (text.equals("N/A"))b.setText("IB");
		if (text.equals("IB"))b.setText("OB");
		if (text.equals("OB"))b.setText("N/A");
		 
		}
	public void control(View v) {
		Button b=(Button)v;
		String text=b.getText().toString();
		if (text.equals("N/A"))b.setText("R");
		if (text.equals("R"))b.setText("L");
		if (text.equals("L"))b.setText("N/A");
		 
		}
 

	private static String readfromFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}
    
    
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
       
        return true;
    }
    
}
