package com.example.printfrompdf;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class PDFTool {

    private static final String TAG = "PDFTools";
    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final String HTML_MIME_TYPE = "text/html";


    public static void printPDFUrl(final Context context, final String pdfUrl ) {

        downloadAndOpenPDF(context, pdfUrl);

    }

    public static void downloadAndOpenPDF(final Context context, final String pdfUrl) {
        // Get filename
        //final String filename = pdfUrl.substring( pdfUrl.lastIndexOf( "/" ) + 1 );
        String filename = "";
        try {
            filename = new GetFileInfo().execute(pdfUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // The place where the downloaded PDF file will be put
        final File tempFile = new File( context.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ), filename );
        Log.e(TAG,"File Path:"+tempFile);
        if ( tempFile.exists() ) {
            // If we have downloaded the file before, just go ahead and show it.
            Uri fileURI = null;
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                fileURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".OurFileProvider",tempFile);
            }else{
                fileURI = Uri.fromFile( tempFile );
            }
            sendPDF(context, fileURI);
            return;
        }

        // Show progress dialog while downloading
        final ProgressDialog progress = ProgressDialog.show( context, "Descarga de fichero", "Proceso de descarga", true );

        // Create the download request
        DownloadManager.Request r = new DownloadManager.Request( Uri.parse( pdfUrl ) );
        r.setDestinationInExternalFilesDir( context, Environment.DIRECTORY_DOWNLOADS, filename );
        final DownloadManager dm = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ( !progress.isShowing() ) {
                    return;
                }
                context.unregisterReceiver( this );

                progress.dismiss();
                long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, -1 );
                Cursor c = dm.query( new DownloadManager.Query().setFilterById( downloadId ) );

                if ( c.moveToFirst() ) {
                    int status = c.getInt( c.getColumnIndex( DownloadManager.COLUMN_STATUS ) );
                    if ( status == DownloadManager.STATUS_SUCCESSFUL ) {
                        //openPDF( context, Uri.fromFile( tempFile ) );
                        Uri fileURI = null;
                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                            fileURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".OurFileProvider",tempFile);
                        }else{
                            fileURI = Uri.fromFile( tempFile );
                        }
                        sendPDF(context, fileURI);


                    }
                }
                c.close();
            }
        };
        context.registerReceiver( onComplete, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE ) );

        // Enqueue the request
        dm.enqueue( r );
    }

    public static void sendPDF(Context ctx, Uri repFile) {

        Intent mIntent = new Intent(Intent.ACTION_SEND);
        mIntent.setClassName("es.rcti.printerplus","es.rcti.printerplus.SharedContent");
        mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mIntent.putExtra(Intent.EXTRA_STREAM, repFile);
        mIntent.setType("application/pdf");

        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ctx.startActivity( mIntent );
    }

    // get File name from url
    static class GetFileInfo extends AsyncTask<String, Integer, String>
    {
        protected String doInBackground(String... urls)
        {
            URL url;
            String filename = null;
            try {
                url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                conn.setInstanceFollowRedirects(false);
                if(conn.getHeaderField("Content-Disposition")!=null){
                    String depo = conn.getHeaderField("Content-Disposition");

                    String depoSplit[] = depo.split("filename=");
                    filename = depoSplit[1].replace("filename=", "").replace("\"", "").trim();
                }else{
                    filename = "download.pdf";
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
            }
            return filename;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // use result as file name
        }
    }

}
