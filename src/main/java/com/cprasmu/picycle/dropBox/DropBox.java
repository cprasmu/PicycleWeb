package com.cprasmu.picycle.dropBox;
import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchBuilder;
import com.dropbox.core.v2.files.SearchMode;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.UploadSessionLookupErrorException;
import com.dropbox.core.v2.files.UploadSessionOffsetError;
import com.dropbox.core.v2.files.WriteMode;
import com.fasterxml.jackson.core.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DropBox {
	 // Adjust the chunk size based on your network speed and reliability. Larger chunk sizes will
    // result in fewer network requests, which will be faster. But if an error occurs, the entire
    // chunk will be lost and have to be re-uploaded. Use a multiple of 4MiB for your chunk size.
    private static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
    private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;
    private static final String ACCESS_TOKEN = "AKF4OAogRKYAAAAAAAAAPLXZY4Ptzla0hcozS8Nmc2mCDJQPmm40CZ6BkiIR4WII";
    /**
     * Uploads a file in a single request. This approach is preferred for small files since it
     * eliminates unnecessary round-trips to the servers.
     *
     * @param dbxClient Dropbox user authenticated client
     * @param localFIle local file to upload
     * @param dropboxPath Where to upload the file to within Dropbox
     */
    private static void uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        try (InputStream in = new FileInputStream(localFile)) {
            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                .withMode(WriteMode.ADD)
                .withClientModified(new Date(localFile.lastModified()))
                .uploadAndFinish(in);

            System.out.println(metadata.toStringMultiline());
        } catch (UploadErrorException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            System.exit(1);
        } catch (DbxException ex) {
            System.err.println("Error uploading to Dropbox: " + ex.getMessage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
            System.exit(1);
        }
    }
    

    /**
     * Uploads a file in chunks using multiple requests. This approach is preferred for larger files
     * since it allows for more efficient processing of the file contents on the server side and
     * also allows partial uploads to be retried (e.g. network connection problem will not cause you
     * to re-upload all the bytes).
     *
     * @param dbxClient Dropbox user authenticated client
     * @param localFIle local file to upload
     * @param dropboxPath Where to upload the file to within Dropbox
     */
    private static void chunkedUploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) {
        long size = localFile.length();

        // assert our file is at least the chunk upload size. We make this assumption in the code
        // below to simplify the logic.
        if (size < CHUNKED_UPLOAD_CHUNK_SIZE) {
            System.err.println("File too small, use upload() instead.");
            System.exit(1);
            return;
        }

        long uploaded = 0L;
        DbxException thrown = null;

        // Chunked uploads have 3 phases, each of which can accept uploaded bytes:
        //
        //    (1)  Start: initiate the upload and get an upload session ID
        //    (2) Append: upload chunks of the file to append to our session
        //    (3) Finish: commit the upload and close the session
        //
        // We track how many bytes we uploaded to determine which phase we should be in.
        String sessionId = null;
        for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i) {
            if (i > 0) {
                System.out.printf("Retrying chunked upload (%d / %d attempts)\n", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS);
            }

            try (InputStream in = new FileInputStream(localFile)) {
                // if this is a retry, make sure seek to the correct offset
                in.skip(uploaded);

                // (1) Start
                if (sessionId == null) {
                    sessionId = dbxClient.files().uploadSessionStart()
                        .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE)
                        .getSessionId();
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                }

                // (2) Append
                while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {
                    dbxClient.files().uploadSessionAppend(sessionId, uploaded)
                        .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE);
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                }

                // (3) Finish
                long remaining = size - uploaded;
                UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);
                CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .build();
                FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
                    .uploadAndFinish(in, remaining);

                System.out.println(metadata.toStringMultiline());
                return;
            } catch (RetryException ex) {
                thrown = ex;
                // RetryExceptions are never automatically retried by the client for uploads. Must
                // catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
                sleepQuietly(ex.getBackoffMillis());
                continue;
            } catch (NetworkIOException ex) {
                thrown = ex;
                // network issue with Dropbox (maybe a timeout?) try again
                continue;
            } catch (UploadSessionLookupErrorException ex) {
                if (ex.errorValue.isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                        .getIncorrectOffsetValue()
                        .getCorrectOffset();
                    continue;
                } else {
                    // Some other error occurred, give up.
                    System.err.println("Error uploading to Dropbox: " + ex.getMessage());
                    System.exit(1);
                    return;
                }
            } catch (UploadSessionFinishErrorException ex) {
                if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                        .getLookupFailedValue()
                        .getIncorrectOffsetValue()
                        .getCorrectOffset();
                    continue;
                } else {
                    // some other error occurred, give up.
                    System.err.println("Error uploading to Dropbox: " + ex.getMessage());
                    System.exit(1);
                    return;
                }
            } catch (DbxException ex) {
                System.err.println("Error uploading to Dropbox: " + ex.getMessage());
                System.exit(1);
                return;
            } catch (IOException ex) {
                System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
                System.exit(1);
                return;
            }
        }

        // if we made it here, then we must have run out of attempts
        System.err.println("Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage());
        System.exit(1);
    }

    private static void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // just exit
            System.err.println("Error uploading to Dropbox: interrupted during backoff.");
            System.exit(1);
        }
    }

    public static InputStream getInputStreamForFile(String filename) throws DownloadErrorException, DbxException {
    	
    	 Logger.getLogger("").setLevel(Level.WARNING);

         // Create a DbxClientV2, which is what you use to make API calls.
         String userLocale = Locale.getDefault().toString();
         DbxRequestConfig requestConfig = new DbxRequestConfig("PiCycle", userLocale);
         DbxClientV2 dbxClient = new DbxClientV2(requestConfig, ACCESS_TOKEN);
         
         DbxDownloader downloader = dbxClient.files().download(filename);
         
        return downloader.getInputStream();
    }
    
    
    public static <T> void sortList(List<T> list, final String propertyName) {

        if (list.size() > 0) {
            Collections.sort(list, new Comparator<T>() {
                @Override
                public int compare(final T object1, final T object2) {
                    String property1 = (String)""+ getSpecifiedFieldValue (propertyName, object1);
                    String property2 = (String)""+ getSpecifiedFieldValue (propertyName, object2);
                    return property1.compareToIgnoreCase (property2);
                }
            });
        }
    }


    public static Object getSpecifiedFieldValue (String property, Object obj) {

        Object result = null;

        try {
            Class<?> objectClass = obj.getClass();
            Field objectField = getDeclaredField(property, objectClass);
            if (objectField!=null) {
                objectField.setAccessible(true);
                result = objectField.get(obj);
            }
        } catch (Exception e) {         
        }
        return result;
    }

    public static Field getDeclaredField(String fieldName, Class<?> type) {

        Field result = null;
        try {
            result = type.getDeclaredField(fieldName);
        } catch (Exception e) {
        }       

        if (result == null) {
            Class<?> superclass = type.getSuperclass();     
            if (superclass != null && !superclass.getName().equals("java.lang.Object")) {       
                return getDeclaredField(fieldName, type.getSuperclass());
            }
        }
        return result;
    }
    
    public static List<Metadata> listFiles() throws ListFolderErrorException, DbxException {
    	
    	 Logger.getLogger("").setLevel(Level.WARNING);

         // Create a DbxClientV2, which is what you use to make API calls.
         String userLocale = Locale.getDefault().toString();
         DbxRequestConfig requestConfig = new DbxRequestConfig("PiCycle", userLocale);
         DbxClientV2 dbxClient = new DbxClientV2(requestConfig, ACCESS_TOKEN);
         
         ListFolderResult files = dbxClient.files().listFolder("");
         
         return files.getEntries();
    }
    
    public static boolean deleteFile(String path) {
    	
    	 Logger.getLogger("").setLevel(Level.WARNING);

         // Create a DbxClientV2, which is what you use to make API calls.
         String userLocale = Locale.getDefault().toString();
         DbxRequestConfig requestConfig = new DbxRequestConfig("PiCycle", userLocale);
         DbxClientV2 dbxClient = new DbxClientV2(requestConfig, ACCESS_TOKEN);
         
         if(path.equals("")){
        	 return false;
         }
         try {
			Metadata meta = dbxClient.files().delete(path);
			
		} catch (DeleteErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public static void transfer(String filename,InputStream in) throws IOException, UploadErrorException, DbxException {
        // Only display important log messages.
        Logger.getLogger("").setLevel(Level.WARNING);

        String argAuthFile = "/home/tomcat/dropbox.key";
       // String localPath = args[1];
       // String dropboxPath = "/PiCycle/" + filename ;
        String dropboxPath =  "/"+filename ;
        // Read auth info file.
        DbxAuthInfo authInfo;

        String pathError = DbxPathV2.findError(dropboxPath);
        if (pathError != null) {
            System.err.println("Invalid <dropbox-path>: " + pathError);
         //   System.exit(1);
       //     return;
        }


        // Create a DbxClientV2, which is what you use to make API calls.
        String userLocale = Locale.getDefault().toString();
        DbxRequestConfig requestConfig = new DbxRequestConfig("PiCycle", userLocale);
        DbxClientV2 dbxClient = new DbxClientV2(requestConfig, ACCESS_TOKEN);
        
       // ListFolderResult files = dbxClient.files().listFolder("/");
     //   DbxClientV2 dbxClient = new DbxClientV2(requestConfig, authInfo.getAccessToken(), authInfo.getHost());

        // upload the file with simple upload API if it is small enough, otherwise use chunked
        // upload API for better performance. Arbitrarily chose 2 times our chunk size as the
        // deciding factor. This should really depend on your network.
     //   if (localFile.length() <= (2 * CHUNKED_UPLOAD_CHUNK_SIZE)) {
      //  dbxClient.files().getMetadata("/PiCycle")
      //  try (InputStream in = new FileInputStream(localFile)) {
            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                .withMode(WriteMode.ADD).withAutorename(true)
                .withClientModified(new Date(System.currentTimeMillis()))
                .uploadAndFinish(in);

            System.out.println(metadata.toStringMultiline());
      //  } catch (UploadErrorException ex) {
      //      System.err.println("Error uploading to Dropbox: " + ex.getMessage());
      //      System.exit(1);
     //   } catch (DbxException ex) {
     //       System.err.println("Error uploading to Dropbox: " + ex.getMessage());
      //      System.exit(1);
     //   } catch (IOException ex) {
    //        System.err.println("Error reading from file \"" + localFile + "\": " + ex.getMessage());
    //        System.exit(1);
   //     }
        
     //   } else {
      ////      chunkedUploadFile(dbxClient, localFile, dropboxPath);
      //  }

      
    }
    
    public static void main(String [] args) throws ListFolderErrorException, DbxException{
    	System.out.println(DropBox.listFiles());
    }
}
