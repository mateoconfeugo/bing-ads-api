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

import java.text.*;
import java.rmi.*;
import bingads.adintelligence.*;
import bingads.adintelligence.adapi.*;
import bingads.adintelligence.datacontracts.*;

/**
 *
 */
public class EstimatedBid {

    private static java.lang.String _namespace = null;
    private static BasicHttpBinding_IAdIntelligenceServiceStub _service = null;

    // Specify your credentials.

    private static java.lang.String UserName = "<UserNameGoesHere>";
    private static java.lang.String Password = "<PasswordGoesHere>";
    private static java.lang.String DeveloperToken = "<DeveloperTokenGoesHere>";
    private static long AccountId = <AccountIdGoesHere>;

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NumberFormat currencyFmt = NumberFormat.getCurrencyInstance();

        try
        {
            AdIntelligenceServiceLocator locator = new AdIntelligenceServiceLocator();
            _namespace = locator.getServiceName().getNamespaceURI();
            _service = (BasicHttpBinding_IAdIntelligenceServiceStub) locator.getBasicHttpBinding_IAdIntelligenceService();

            // Set the header properties.

            service.clearHeaders();
            service.setHeader(namespace, "DeveloperToken", DeveloperToken);
            service.setHeader(namespace, "UserName", UserName);
            service.setHeader(namespace, "Password", Password);
            service.setHeader(namespace, "CustomerAccountId", AccountId);

            // Set the Currency, Keywords, Language, PublisherCountries, and TargetPositionForAds
            // for the estimated bid by keywords request.
            
            Currency currency = Currency.USDollar;
            
            KeywordAndMatchType[] keywordAndMatchTypes = new KeywordAndMatchType[2];
            MatchType[] matchTypes = new MatchType[] { MatchType.Exact, MatchType.Broad, MatchType.Phrase };
            keywordAndMatchTypes[0] = new KeywordAndMatchType();
            keywordAndMatchTypes[0].setKeywordText("flower");
            keywordAndMatchTypes[0].setMatchTypes(matchTypes);
            keywordAndMatchTypes[1] = new KeywordAndMatchType();
            keywordAndMatchTypes[1].setKeywordText("delivery");
            keywordAndMatchTypes[1].setMatchTypes(matchTypes);
            
            java.lang.String language = "English";
            
            java.lang.String [] publisherCountries = new java.lang.String[] { "US" };
            
            TargetAdPosition targetPositionForAds = TargetAdPosition.SideBar;
            
            // GetKeywordEstimatedBidByKeywords helper method calls the corresponding Bing Ads _service operation 
            // to request the KeywordEstimatedBids.
            
            KeywordEstimatedBid[] keywordEstimatedBids = GetKeywordEstimatedBidByKeywords(
            	currency,
                keywordAndMatchTypes, 
                language, 
                publisherCountries, 
                targetPositionForAds
                );

            // GetAdGroupEstimatedBidByKeywords helper method calls the corresponding Bing Ads _service operation 
            // to request the AdGroupEstimatedBid.
            
            AdGroupEstimatedBid adGroupEstimatedBid = GetAdGroupEstimatedBidByKeywords(
                currency,
                keywordAndMatchTypes,
                language,
                publisherCountries,
                targetPositionForAds
                );
			
            // Print the KeywordEstimatedBids

            if (keywordEstimatedBids != null)
            {
            	System.out.println("KeywordEstimatedBids\n");
            	
                for (KeywordEstimatedBid bid : keywordEstimatedBids)
                {
                    if (bid == null)
                    {
                        System.out.println("The keyword is not valid.\n");
                    }
                    else
                    {
                        System.out.println(bid.getKeyword());

                        if (bid.getEstimatedBids().length == 0)
                        {
                            System.out.println("  There is no bid information available for the keyword.\n");
                        }
                        else
                        {
                            for (EstimatedBidAndTraffic estimatedBidAndTraffic : bid.getEstimatedBids())
                            {
                            	System.out.println("    Estimated Minimum Bid: " + 
                                        currencyFmt.format(estimatedBidAndTraffic.getEstimatedMinBid()));
                                System.out.println("  " + estimatedBidAndTraffic.getMatchType());
                                System.out.println("    Average CPC: " + 
                                        (estimatedBidAndTraffic.getAverageCPC() != null ? currencyFmt.format(estimatedBidAndTraffic.getAverageCPC()) : "null"));
                                System.out.printf("    Estimated clicks per week: %d to %d%n",
                                		estimatedBidAndTraffic.getMinClicksPerWeek(), estimatedBidAndTraffic.getMaxClicksPerWeek());
                                System.out.printf("    Estimated impressions per week: %d to %d%n",
                                		estimatedBidAndTraffic.getMinImpressionsPerWeek(), estimatedBidAndTraffic.getMaxImpressionsPerWeek());
                                System.out.printf("    Estimated cost per week: %s to %s%n",
                                    (estimatedBidAndTraffic.getMinTotalCostPerWeek() != null ? currencyFmt.format(estimatedBidAndTraffic.getMinTotalCostPerWeek()) : "null"),
                                    (estimatedBidAndTraffic.getMaxTotalCostPerWeek() != null ? currencyFmt.format(estimatedBidAndTraffic.getMaxTotalCostPerWeek()) : "null"));
                                System.out.println();
                            }
                        }
                    }
                }
            }
            
            // Print the AdGroupEstimatedBid
            
            System.out.println("AdGroupEstimatedBid\n");
            
            System.out.println("    Estimated Ad Group Bid: " + 
                    currencyFmt.format(adGroupEstimatedBid.getEstimatedAdGroupBid()));
            System.out.println("    Average CPC: " + 
                    (adGroupEstimatedBid.getAverageCPC() != null ? currencyFmt.format(adGroupEstimatedBid.getAverageCPC()) : "null"));
            System.out.printf("    Estimated clicks per week: %d to %d%n",
            		adGroupEstimatedBid.getMinClicksPerWeek(), adGroupEstimatedBid.getMaxClicksPerWeek());
            System.out.printf("    Estimated impressions per week: %d to %d%n",
            		adGroupEstimatedBid.getMinImpressionsPerWeek(), adGroupEstimatedBid.getMaxImpressionsPerWeek());
            System.out.printf("    Estimated cost per week: %s to %s%n",
                (adGroupEstimatedBid.getMinTotalCostPerWeek() != null ? currencyFmt.format(adGroupEstimatedBid.getMinTotalCostPerWeek()) : "null"),
                (adGroupEstimatedBid.getMaxTotalCostPerWeek() != null ? currencyFmt.format(adGroupEstimatedBid.getMaxTotalCostPerWeek()) : "null"));
            System.out.println();
        }
        // Ad Intelligence service operations can throw AdApiFaultDetail.
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
                    case 117:   // CallRateExceeded
                        break;
                    default:
                        System.out.println("Please see MSDN documentation for more details about the error code output above.");
                        break;
                }
            }
        }
        // Ad Intelligence service operations can throw ApiFaultDetail.
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

    /**
     * Get one or more keyword estimated bids corresponding to placement of your ad in the targeted position. 
     */

    public static KeywordEstimatedBid[] GetKeywordEstimatedBidByKeywords(Currency currency, KeywordAndMatchType[] keywordAndMatchTypes, 
    		java.lang.String language, java.lang.String[] publisherCountries, TargetAdPosition targetPositionForAds) throws RemoteException, Exception
    {
        GetEstimatedBidByKeywordsRequest request = new GetEstimatedBidByKeywordsRequest();
        
        // Set the Currency, Keywords, Language, PublisherCountries, and TargetPositionForAds
        // for the estimated bid by keywords request.
        
        request.setCurrency(currency);
        request.setGetBidsAtLevel(0); // Set GetBidsAtLevel to 0 to get a list of KeywordEstimatedBid.
        request.setKeywords(keywordAndMatchTypes);
        request.setLanguage(language);
        request.setPublisherCountries(publisherCountries);
        request.setTargetPositionForAds(targetPositionForAds);

        return service.getEstimatedBidByKeywords(request).getKeywordEstimatedBids();
    }

    /**
     * Get one or more ad group estimated bids corresponding to placement of your ad in the targeted position. 
     */

    public static AdGroupEstimatedBid GetAdGroupEstimatedBidByKeywords(Currency currency, KeywordAndMatchType[] keywordAndMatchTypes, 
    		java.lang.String language, java.lang.String[] publisherCountries, TargetAdPosition targetPositionForAds) throws RemoteException, Exception
    {
        GetEstimatedBidByKeywordsRequest request = new GetEstimatedBidByKeywordsRequest();
        
        // Set the Currency, Keywords, Language, PublisherCountries, and TargetPositionForAds
        // for the estimated bid by keywords request.
        
        request.setCurrency(currency);
        request.setGetBidsAtLevel(2); // Set GetBidsAtLevel to 2 to get one AdGroupEstimatedBid.
        request.setKeywords(keywordAndMatchTypes);
        request.setLanguage(language);
        request.setPublisherCountries(publisherCountries);
        request.setTargetPositionForAds(targetPositionForAds);

        return service.getEstimatedBidByKeywords(request).getAdGroupEstimatedBid();
    }
}