// Copyright 2013 Microsoft Corporation 
 
// Licensed under the Apache License, Version 2.0 (the "License"); 
// you may not use this file except in compliance with the License. 
// You may obtain a copy of the License at 
 
//    http://www.apache.org/licenses/LICENSE-2.0 
 
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, 
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and 
// limitations under the License. 

import java.rmi.*;
import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;


import bingads.bulk.*;
import bingads.bulk.adapi.*;

public class BulkDownloadUpload {

    private static java.lang.String _namespace = null;
    private static BasicHttpBinding_IBulkServiceStub _service = null;

    // Specify your credentials.

    private static java.lang.String UserName = "<UserNameGoesHere>";
    private static java.lang.String Password = "<PasswordGoesHere>";
    private static java.lang.String DeveloperToken = "<DeveloperTokenGoesHere>";
    private static long CustomerId = <CustomerIdGoesHere>;
    private static long AccountId = <AccountIdGoesHere>;
    private static long[] CampaignIds = { <CommaDelimitedCampaignIdsGoHere> };
    

    // The full path to the bulk file.

    private static java.lang.String BulkFilePath = "c:\\bulk\\campaigns.zip";

    // The full path to the extracted bulk file.

    private static java.lang.String ExtractedFilePath = "C:\\bulk\\extracted\\accounts.tsv";
    
    // The full path to the upload result file.

    private static java.lang.String UploadResultFilePath = "C:\\bulk\\uploadresults.zip";

    // Specifies the bulk file format. 

    private static DownloadFileType FileFormat = DownloadFileType.Tsv;

    
    /**
     * Bulk example that shows how to download and upload campaign data for
     * one or more campaigns.
     */
    public static void main(String[] args) {
        
        String folder = BulkFilePath.substring(0, BulkFilePath.lastIndexOf('\\'));
        File dir = new File(folder);

        // Confirm that the download folder exist; otherwise, exit.

        if (!dir.exists())
        {
            System.out.println("The download folder does not exist. Ensure that the " +
                "folder exists and try again.");
            return;
        }

        try
        {
        	BulkServiceLocator locator = new BulkServiceLocator();
            _namespace = locator.getServiceName().getNamespaceURI();
            _service = (BasicHttpBinding_IBulkServiceStub) locator.getBasicHttpBinding_IBulkService();

            // Set the header properties.

            _service.clearHeaders();
            _service.setHeader(_namespace, "DeveloperToken", DeveloperToken);
            _service.setHeader(_namespace, "UserName", UserName);
            _service.setHeader(_namespace, "Password", Password);
            _service.setHeader(_namespace, "CustomerId", CustomerId);
            _service.setHeader(_namespace, "CustomerAccountId", AccountId);
            
            // Use the bulk service to download a bulk file.
            
            Calendar lastSyncTimeInUtc = GetLastSyncTime(ExtractedFilePath);
            
            // The campaigns must all belong to the same account.

            ArrayList<CampaignScope> campaigns = new ArrayList<CampaignScope>();

            for (long id : CampaignIds)
            {
                CampaignScope scope = new CampaignScope();
                scope.setCampaignId(id);
                scope.setParentAccountId(AccountId);
                campaigns.add(scope);
            }
            
            java.lang.String[] dataScope = new String[] {
            		DataScopeNull._EntityData
            };

            java.lang.String[] entities = new String[] {
                BulkDownloadEntityNull._Ads,
                BulkDownloadEntityNull._AdGroups,
                BulkDownloadEntityNull._Campaigns,
                BulkDownloadEntityNull._Keywords
            };

            // You may include a non-null date range if the lastSyncTime is null, and the data scope includes   
            // either EntityPerformanceData, BidSuggestionsData, or QualityScoreData.  
            
            /*
            bingads.bulk.Date customDateRangeStart = new bingads.bulk.Date();
            customDateRangeStart.setDay(1);
            customDateRangeStart.setMonth(9);
            customDateRangeStart.setYear(2013);
            
            bingads.bulk.Date customDateRangeEnd = new bingads.bulk.Date();
            customDateRangeEnd.setDay(30);
            customDateRangeEnd.setMonth(9);
            customDateRangeEnd.setYear(2013);

            PerformanceStatsDateRange performanceStatsDateRange = new PerformanceStatsDateRange();
            performanceStatsDateRange.setCustomDateRangeStart(customDateRangeStart);
            performanceStatsDateRange.setCustomDateRangeEnd(customDateRangeEnd);
            */
            
            // GetDownloadRequestId helper method calls the corresponding Bing Ads service operation 
            // to request the download identifier.
            
            java.lang.String downloadRequestId = GetDownloadRequestId(
                (CampaignScope[])(campaigns.toArray(new CampaignScope[0])), 
                dataScope,
                entities,
                lastSyncTimeInUtc, 
                null //performanceStatsDateRange
                );


            System.out.printf("Download Request ID: %s\n\n", downloadRequestId);

            int waitTime = 1000 * 5 * 1; 
            boolean downloadSuccess = false;

            // This sample polls every 30 seconds up to 5 minutes.
            // In production you may poll the status every 1 to 2 minutes for up to one hour.
            // If the call succeeds, stop polling. If the call or 
            // download fails, the call throws a fault.

            for (int i = 0; i < 10; i++)
            {
                try {Thread.sleep(waitTime);}
                catch (InterruptedException ignore) {}

                // GetDownloadRequestStatus helper method calls the corresponding Bing Ads service operation 
                // to get the download status.
                java.lang.String downloadRequestStatus = GetDownloadRequestStatus(downloadRequestId);

                System.out.printf("Download Request Status: %s\n", downloadRequestStatus);
                
                if ((downloadRequestStatus != null) && ((downloadRequestStatus.equals("Completed"))
                        || (downloadRequestStatus.equals("CompletedWithErrors"))))
                {
                    downloadSuccess = true;
                    break;
                }
            }

            if (downloadSuccess)
            {
            	// GetDownloadUrl helper method calls the corresponding Bing Ads service operation 
                // to get the download Url.
                java.lang.String downloadUrl = GetDownloadUrl(downloadRequestId);
                System.out.printf("Downloading from %s.\n\n", downloadUrl);
                DownloadFile(downloadUrl, BulkFilePath);
                System.out.printf("The download file was written to %s.\n\n", BulkFilePath);
            }
            else  // InProgress
            {
                System.out.println("The request is taking longer than expected.");
                System.out.printf("Save the report ID (%s) and try again later.\n", downloadRequestId);
            }
            
            
            
            // You may unzip and update the downloaded bulk file or prepare a new file elsewhere.
            // Changes to the bulk file are not shown here.

            DecompressFile(BulkFilePath, ExtractedFilePath);
            CompressFile(ExtractedFilePath, BulkFilePath);


            // Use the bulk service to upload a bulk file.

            ResponseMode responseMode = ResponseMode.ErrorsAndResults;

            GetBulkUploadUrlResponse uploadResponse = GetBulkUploadUrl(responseMode);

            java.lang.String uploadRequestId = uploadResponse.getRequestId();
            java.lang.String uploadUrl = uploadResponse.getUploadUrl();

            System.out.printf("Uploading file from %s.\n\n", BulkFilePath);
            System.out.printf("Upload Request Id: %s\n\n", uploadRequestId);
            System.out.printf("Upload Url: %s\n\n", uploadUrl);

            UploadFile(uploadUrl, BulkFilePath);
            
            boolean uploadSuccess = false;

            // This sample polls every 30 seconds up to 5 minutes.
            // In production you may poll the status every 1 to 2 minutes for up to one hour.
            // If the call succeeds, stop polling. If the call or 
            // download fails, the call throws a fault.

            for (int i = 0; i < 10; i++)
            {
            	try {Thread.sleep(waitTime);}
                catch (InterruptedException ignore) {}

                // GetUploadRequestStatus helper method calls the corresponding Bing Ads service operation 
                // to get the upload status.
                java.lang.String uploadRequestStatus = GetUploadRequestStatus(uploadRequestId);
                
                System.out.printf("Upload Request Status: %s\n", uploadRequestStatus);
                
                if ((uploadRequestStatus != null) && ((uploadRequestStatus.equals("Completed"))
                    || (uploadRequestStatus.equals("CompletedWithErrors"))))
                {
                    uploadSuccess = true;
                    break;
                }
            }
            
            if (uploadSuccess)
            {
                // GetUploadResultFileUrl helper method calls the corresponding Bing Ads service operation 
                // to get the upload result file Url.
                java.lang.String uploadResultFileUrl = GetUploadResultFileUrl(uploadRequestId);
                DownloadFile(uploadResultFileUrl, UploadResultFilePath);
                System.out.printf("The upload result file was written to %s\n\n.", UploadResultFilePath);
            }
            else // PendingFileUpload
            {
            	System.out.printf("The request is taking longer than expected.\n" +
                                    "Save the upload ID (%s) and try again later.\n\n", uploadRequestId);
            }           
        }
        // Bulk service operations can throw AdApiFaultDetail.
        catch (AdApiFaultDetail fault)
        {
            // Log this fault.

            System.out.println("The operation failed with the following faults:\n");

            // If the AdApiError array is not null, the following are examples of error codes that may be found.
            for (AdApiError error : fault.getErrors())
            {
                System.out.printf("AdApiError\n");
                System.out.printf("Code: %d\nError Code: %s\nMessage: %s\n\n", error.getCode(), error.getErrorCode(), error.getMessage());

                switch (error.getCode())
                {
                    case 0:     // InternalError
                        break;
                    case 105:   // InvalidCredentials
                        break;
                    default:
                        System.out.println("Please see MSDN documentation for more details about the error code output above.");
                        break;
                }
            }
        }
        // Bulk service operations can throw ApiFaultDetail.
        catch (ApiFaultDetail fault)
        {
            // Log this fault.

            System.out.println("The operation failed with the following faults:\n");

            // If the BatchError array is not null, the following are examples of error codes that may be found.
            for (BatchError error : fault.getBatchErrors())
            {
                System.out.printf("BatchError at Index: %d\n", error.getIndex());
                System.out.printf("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());

                switch (error.getCode())
                {
                    case 0:     // InternalError
                        break;
                    default:
                        System.out.println("Please see MSDN documentation for more details about the error code output above.");
                        break;
                }
            }

            // If the OperationError array is not null, the following are examples of error codes that may be found.
            for (OperationError error : fault.getOperationErrors())
            {
                System.out.printf("OperationError\n");
                System.out.printf("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());

                switch (error.getCode())
                {
                    case 0:     // InternalError
                        break;
                    case 106:   // UserIsNotAuthorized
                        break;
                    default:
                        System.out.println("Please see MSDN documentation for more details about the error code output above.");
                        break;
                }
            }
        }
        catch (RemoteException e)
        {
            System.out.println("Service communication error encountered: ");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            // Ignore fault exceptions that we already caught.

            if ( e.getCause() instanceof AdApiFaultDetail ||
                 e.getCause() instanceof ApiFaultDetail )
            {
                ;
            }
            else
            {
                System.out.println("Error encountered: ");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // GetDownloadRequestId helper method calls the DownloadCampaignsByCampaignIds service operation 
    // to request the download identifier.

    private static java.lang.String GetDownloadRequestId(CampaignScope[] campaigns, java.lang.String[] dataScope, java.lang.String[] entities, 
    		Calendar lastSyncTime, PerformanceStatsDateRange dateRange) throws RemoteException, Exception
    {
        DownloadCampaignsByCampaignIdsRequest request = new DownloadCampaignsByCampaignIdsRequest();
        
        // Set the request information.

        request.setCampaigns(campaigns);
        request.setDataScope(dataScope);
        request.setDownloadFileType(FileFormat);
        request.setEntities(entities);
        request.setLastSyncTimeInUTC(lastSyncTime);
        request.setPerformanceStatsDateRange(dateRange);

        return _service.downloadCampaignsByCampaignIds(request).getDownloadRequestId();
    }

    // GetDownloadRequestStatus helper method calls the GetDownloadStatus service operation 
    // to get the download request status.

    private static java.lang.String GetDownloadRequestStatus(java.lang.String downloadRequestId)  throws RemoteException, Exception
    {
        GetDownloadStatusRequest request = new GetDownloadStatusRequest();
        
        // Set the request information.

        request.setDownloadRequestId(downloadRequestId);
                    
        return _service.getDownloadStatus(request).getRequestStatus();
    }
    
    // GetDownloadUrl helper method calls the GetDownloadStatus service operation 
    // to get the download Url.

    private static java.lang.String GetDownloadUrl(java.lang.String downloadRequestId)  throws RemoteException, Exception
    {
        GetDownloadStatusRequest request = new GetDownloadStatusRequest();
        
        // Set the request information.

        request.setDownloadRequestId(downloadRequestId);
                    
        return _service.getDownloadStatus(request).getDownloadUrl();
    }
    
    // GetBulkUploadUrl helper method calls the GetBulkUploadUrl service operation 
    // to request the upload identifier and upload Url via GetBulkUploadUrlResponse.

    private static GetBulkUploadUrlResponse GetBulkUploadUrl(ResponseMode responseMode)  throws RemoteException, Exception
    {
    	GetBulkUploadUrlRequest request = new GetBulkUploadUrlRequest();
        
        // Set the request information.

        request.setResponseMode(responseMode);
        request.setAccountId(AccountId);
                    
        return _service.getBulkUploadUrl(request);
    }
    
    // GetUploadRequestStatus helper method calls the GetBulkUploadStatus service operation 
    // to get the upload request status.

    private static java.lang.String GetUploadRequestStatus(java.lang.String requestId)  throws RemoteException, Exception
    {
    	GetBulkUploadStatusRequest request = new GetBulkUploadStatusRequest();
        
        // Set the request information.

        request.setRequestId(requestId);
                    
        return _service.getBulkUploadStatus(request).getRequestStatus();
    }
    
    // GetUploadResultFileUrl helper method calls the GetBulkUploadStatus service operation 
    // to get the upload result file Url.

    private static java.lang.String GetUploadResultFileUrl(java.lang.String requestId)  throws RemoteException, Exception
    {
    	GetBulkUploadStatusRequest request = new GetBulkUploadStatusRequest();
        
        // Set the request information.

        request.setRequestId(requestId);
                    
        return _service.getBulkUploadStatus(request).getResultFileUrl();
    }
    
    // Using the URL returned by the GetBulkUploadUrl operation, 
    // POST the bulk file using a HTTP client. 
    private static void UploadFile(java.lang.String uploadUrl, java.lang.String filePath) throws IOException
    {
        final java.lang.String CRLF = "\r\n";
        URL url = new URL(uploadUrl);
        HttpURLConnection connection = null;
        
        FileInputStream reader = null;
        OutputStream out = null;
        PrintWriter writer = null;
        
        try
        {   
        	// Set up the connection and headers
        	
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
        	
            connection.setRequestProperty("UserName", UserName);
            connection.setRequestProperty("Password", Password);
            connection.setRequestProperty("DeveloperToken", DeveloperToken);
            connection.setRequestProperty("CustomerId", Long.toString(CustomerId));
            connection.setRequestProperty("CustomerAccountId", Long.toString(AccountId));
        	
            // Content-Type must be multipart/form-data with custom boundary
        	
            java.lang.String boundary = "--------------------" + Long.toString(System.currentTimeMillis(), 16);
            java.lang.String contentType = "multipart/form-data; boundary=" + boundary;
            connection.setRequestProperty("Content-Type", contentType);
        	
            final int bufferSize = 100 * 1024;
            byte[] buffer = new byte[bufferSize];
            
            File file = new File(filePath);
            reader = new FileInputStream(file);
            out = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);
            
            // Add the file within the specified boundary
            
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append(CRLF);
            writer.append("Content-Type: application/zip").append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF);
            writer.flush();
            
            int count = 0;
            while ((count = reader.read(buffer)) != -1)
            {
            	out.write(buffer, 0, count);
            }
            
            out.flush();
            
            writer.append(CRLF).flush();
            writer.append("--" + boundary + "--").append(CRLF);
            writer.flush();
            
            System.out.printf("Upload Connection Response: %s\n", connection.getResponseMessage());
        }
        finally
        {
            reader.close();
            writer.flush();
            writer.close();
            out.flush();
            out.close();
        }
    }


    // Using the URL that the GetDownloadStatus operation returned,
    // send an HTTP request to get the download data and write it
    // to the specified ZIP file.

    private static java.lang.String DownloadFile(String downloadUrl, String filePath) throws IOException
    {
        URL url = new URL(downloadUrl);
        URLConnection request = null;
        int count = 0;
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;

        try
        {
            request = url.openConnection();

            reader = new BufferedInputStream(request.getInputStream());
            writer = new BufferedOutputStream(new FileOutputStream(filePath));

            final int bufferSize = 100 * 1024;
            byte[] buffer = new byte[bufferSize];

            while ((count = reader.read(buffer)) != -1)
            {
                writer.write(buffer, 0, count);
            }
        }
        finally
        {
            reader.close();
            writer.flush();
            writer.close();
        }

        return filePath;
    }
    
    // Decompresses a ZIP Archive and writes the contents to the specified file path.

    private static void DecompressFile(String fromZipArchive, String toExtractedFile) throws IOException
    {
    	ZipInputStream archive = new ZipInputStream(new FileInputStream(fromZipArchive));
    	FileOutputStream file = new FileOutputStream(toExtractedFile);
        
    	final int bufferSize = 100 * 1024;
        byte[] buffer = new byte[bufferSize];
        
        // Get the first entry in the ZIP input stream
        archive.getNextEntry();
        
    	// Move bytes from the ZIP archive
        int count = 0;
        while ((count = archive.read(buffer)) != -1) {
        	file.write(buffer, 0, count);
        }

        file.close();
        archive.closeEntry();
        archive.close();
    }

    // Compresses a bulk file to a ZIP Archive.

    private static void CompressFile(String fromExtractedFile, String toZipArchive) throws IOException
    {
    	FileInputStream file = new FileInputStream(fromExtractedFile);
    	ZipOutputStream archive = new ZipOutputStream(new FileOutputStream(toZipArchive));
    	       
        final int bufferSize = 100 * 1024;
        byte[] buffer = new byte[bufferSize];
           
        // Begin entry in the ZIP output stream
        archive.putNextEntry(new ZipEntry((new File(fromExtractedFile)).getName()));

        // Move bytes to the ZIP archive
        int count = 0;
        while ((count = file.read(buffer)) != -1) {
        	archive.write(buffer, 0, count);
        }

        archive.closeEntry();
        archive.flush();
        archive.close();
        file.close();
    }


    // Get the time stamp of the last download from the previous
    // download file. The SyncTime node contains the time stamp.

    private static Calendar GetLastSyncTime(String path) throws Exception
    {
        Calendar lastSyncTime = null;
        File downloadFile = new File(path);
        java.lang.String columnDelimiter = null;
        
        switch (FileFormat.getValue())
        {
            case DownloadFileType._Tsv:
                columnDelimiter = "\t";
                break;
        }

        if (downloadFile.exists())
        {
            BufferedReader reader = null;
            
            try
            {
                reader = new BufferedReader(new FileReader(path));

                int syncTimeColumn = 0;
                java.lang.String[] fields = null;

                // The first record contains column header information, for example "Type" and "Sync Time".
                java.lang.String record = reader.readLine();
                   
                if (record != null)
                {
                    fields = record.split(columnDelimiter);
                    int column = 0;

                    // Find the Sync Time column.
                    do
                    {
                        syncTimeColumn = (fields[column].equals("Sync Time")) ? column : syncTimeColumn;
                    } while (syncTimeColumn == 0 && (++column < fields.length));
                }

                // Look for the Account record after any other metadata.

                boolean isAccount = false;

                do
                {
                    record = reader.readLine();
                    fields = record.split(columnDelimiter);
                    if(fields[0].equals("Account"))
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY kk:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        lastSyncTime = sdf.getCalendar();
                        lastSyncTime.setTime(sdf.parse(fields[syncTimeColumn]));
                        isAccount = true;
                    }
                } while (!isAccount);
            }
            finally
            {
                reader.close();
            }
        }

        return lastSyncTime;
    }
}