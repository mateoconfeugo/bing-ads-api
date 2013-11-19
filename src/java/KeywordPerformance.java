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

import bingads.reporting.*;
import bingads.reporting.adapi.*;

/**
 *
 */
public class KeywordPerformance {

    private static java.lang.String _namespace = null;
    private static BasicHttpBinding_IReportingServiceStub _service = null;

    // Specify your credentials.

    private static java.lang.String UserName = "";
    private static java.lang.String Password = "";
    private static java.lang.String DeveloperToken = "";
    private static long AccountId = "";
    private static long CampaignId = "";

    // Specify the file to download the report to. The file is
    // compressed so use the .zip file extension.

    private static java.lang.String DownloadPath = "./keywordperf.zip";

    //    public static void main(String[] args) {
    public  void doit(String[] args) {

    	// Confirm that the download folder exists; otherwise, exit.

    	String folder = DownloadPath.substring(0, DownloadPath.lastIndexOf('\\'));
        File dir = new File(folder);

        if (!dir.exists())
        {
            System.out.println("The output folder does not exist. Ensure that the " +
                "folder exists and try again.");
            return;
        }

        try
        {
        	ReportingServiceLocator locator = new ReportingServiceLocator();
            _namespace = locator.getServiceName().getNamespaceURI();
            _service = (BasicHttpBinding_IReportingServiceStub) locator.getBasicHttpBinding_IReportingService();

            // Set the header properties.

            service.clearHeaders();
            service.setHeader(namespace, "DeveloperToken", DeveloperToken);
            service.setHeader(namespace, "UserName", UserName);
            service.setHeader(namespace, "Password", Password);


            // Build a keyword performance report request, including Format, ReportName, Aggregation,
            // Scope, Time, Filter, and Columns.

            KeywordPerformanceReportRequest report = new KeywordPerformanceReportRequest();

            report.setFormat(ReportFormat.Tsv);
            report.setReportName("My Keyword Performance Report");
            report.setReturnOnlyCompleteData(false);
            report.setAggregation(ReportAggregation.Daily);

            report.setScope(new AccountThroughAdGroupReportScope());
            report.getScope().setAccountIds(null);
            report.getScope().setAdGroups(null);

            report.getScope().setCampaigns(new CampaignReportScope[1]);
            report.getScope().getCampaigns()[0] = new CampaignReportScope();
            report.getScope().getCampaigns()[0].setCampaignId(CampaignId);
            report.getScope().getCampaigns()[0].setAccountId(AccountId);

            report.setTime(new ReportTime());
            report.getTime().setPredefinedTime(ReportTimePeriod.Yesterday);

            // You may either use a custom date range or predefined time.
            //report.getTime().setCustomDateRangeStart(new Date());
            //report.getTime().getCustomDateRangeStart().setMonth(9);
            //report.getTime().getCustomDateRangeStart().setDay(1);
            //report.getTime().getCustomDateRangeStart().setYear(2013);
            //report.getTime().setCustomDateRangeEnd(new Date());
            //report.getTime().getCustomDateRangeEnd().setMonth(9);
            //report.getTime().getCustomDateRangeEnd().setDay(30);
            //report.getTime().getCustomDateRangeEnd().setYear(2013);

            report.setFilter(new KeywordPerformanceReportFilter());
            report.getFilter().setDeviceType(new String[] {
                DeviceTypeReportFilterNull._Computer,
                DeviceTypeReportFilterNull._SmartPhone
            });

            // Specify the attribute and data report columns.

            report.setColumns(new KeywordPerformanceReportColumn[] {
                KeywordPerformanceReportColumn.TimePeriod,
                KeywordPerformanceReportColumn.AccountId,
                KeywordPerformanceReportColumn.CampaignId,
                KeywordPerformanceReportColumn.Keyword,
                KeywordPerformanceReportColumn.KeywordId,
                KeywordPerformanceReportColumn.DeviceType,
                KeywordPerformanceReportColumn.BidMatchType,
                KeywordPerformanceReportColumn.Clicks,
                KeywordPerformanceReportColumn.Impressions,
                KeywordPerformanceReportColumn.Ctr,
                KeywordPerformanceReportColumn.AverageCpc,
                KeywordPerformanceReportColumn.Spend,
                KeywordPerformanceReportColumn.QualityScore
            });

            // You may optionally sort by any KeywordPerformanceReportColumn, and optionally
            // specify the maximum number of rows to return in the sorted report.

            KeywordPerformanceReportSort keywordPerformanceReportSort = new KeywordPerformanceReportSort();
            keywordPerformanceReportSort.setSortColumn(KeywordPerformanceReportColumn.Clicks);
            keywordPerformanceReportSort.setSortOrder(SortOrder.Ascending);
            report.setSort(new KeywordPerformanceReportSort[] { keywordPerformanceReportSort });

            report.setMaxRows(10);

            // SubmitGenerateReport helper method calls the corresponding Bing Ads _service operation
            // to request the report identifier. The identifier is used to check report generation status
            // before downloading the report.

            java.lang.String reportRequestId  = SubmitGenerateReport(report);

            System.out.println("Report Request ID: " + reportRequestId + "\n");

            int waitTime = 1000 * 30 * 1;
            ReportRequestStatus reportRequestStatus = null;

            // This sample polls every 30 seconds up to 5 minutes.
            // In production you may poll the status every 1 to 2 minutes for up to one hour.
            // If the call succeeds, stop polling. If the call or
            // download fails, the call throws a fault.


            for (int i = 0; i < 10; i++)
            {
                try {Thread.sleep(waitTime);}
                catch (InterruptedException ignore) {}

                reportRequestStatus = PollGenerateReport(reportRequestId);

                System.out.printf("Report Request Status: %s\n", reportRequestStatus);

                if (reportRequestStatus.getStatus() == ReportRequestStatusType.Success ||
                	reportRequestStatus.getStatus() == ReportRequestStatusType.Error)
                {
                    break;
                }
            }

            if (reportRequestStatus != null)
            {
                if (reportRequestStatus.getStatus() == ReportRequestStatusType.Success)
                {
                	java.lang.String reportDownloadUrl = reportRequestStatus.getReportDownloadUrl();
                	System.out.printf("Downloading from %s.\n", reportDownloadUrl);
                    DownloadReport(reportDownloadUrl, DownloadPath);
                    System.out.printf("The report was written to %s.\n", DownloadPath);
                }
                else if (reportRequestStatus.getStatus() == ReportRequestStatusType.Error)
                {
                    System.out.println("The request failed. Try requesting the report " +
                        "later.\nIf the request continues to fail, contact support.");
                }
                else  // Pending
                {
                    System.out.println("The request is taking longer than expected.");
                    System.out.printf("Save the report ID (%s) and try again later.", reportRequestId);
                }
            }
        }
        // Reporting service operations can throw AdApiFaultDetail.
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
        // Reporting service operations can throw ApiFaultDetail.
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


    // Request the report and returns the ReportRequestId that can be used to check report
    // status and then used to download the report.

    public static java.lang.String SubmitGenerateReport(ReportRequest report) throws RemoteException, Exception
    {
        SubmitGenerateReportRequest request = new SubmitGenerateReportRequest();

        request.setReportRequest(report);

        return service.submitGenerateReport(request).getReportRequestId();
    }

    // Checks the status of a report request. Returns a data object that contains both
    // report status and download URL.

    public static ReportRequestStatus PollGenerateReport(java.lang.String reportRequestId) throws RemoteException, Exception
    {
        PollGenerateReportRequest request = new PollGenerateReportRequest();

        request.setReportRequestId(reportRequestId);

        return service.pollGenerateReport(request).getReportRequestStatus();
    }

    // Using the URL that the PollGenerateReport operation returned,
    // send an HTTP request to get the report and write it to the specified
    // ZIP file.

    public static java.lang.String DownloadReport(java.lang.String reportDownloadUrl, java.lang.String downloadPath) throws IOException
    {
        URL url = new URL(reportDownloadUrl);
        URLConnection request = null;
        int count = 0;
        BufferedInputStream reader = null;
        BufferedOutputStream writer = null;

        try
        {
            request = url.openConnection();

            reader = new BufferedInputStream(request.getInputStream());
            writer = new BufferedOutputStream(new FileOutputStream(downloadPath));

            final int bufferSize = 100 * 1024;
            byte[] buffer = new byte[bufferSize];

            while (-1 != (count = reader.read(buffer, 0, bufferSize)))
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

        return downloadPath;
    }
}
