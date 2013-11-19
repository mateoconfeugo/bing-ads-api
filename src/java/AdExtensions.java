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
import java.text.*;

import bingads.campaignmanagement.*;
import bingads.campaignmanagement.adapi.*;
import bingads.campaignmanagement.schemas.generic.*;

/**
 *
 */
public class AdExtensions {

     private static java.lang.String _namespace = null;
     private static BasicHttpBinding_ICampaignManagementServiceStub _service = null;

     // Specify API credentials.
     
     private static java.lang.String UserName = "<UserNameGoesHere>";
     private static java.lang.String Password = "<PasswordGoesHere>";
     private static java.lang.String DeveloperToken = "<DeveloperTokenGoesHere>";
     private static long CustomerId = <CustomerIdGoesHere>;
     private static long AccountId = <AccountIdGoesHere>;
     private static long CampaignId = <CampaignIdGoesHere>;


     /**
      */
     public static void main(java.lang.String[] args) {
         CampaignManagementServiceLocator locator = null;
         long[] ids = null;

         try
         {
             locator = new CampaignManagementServiceLocator();
             _namespace = locator.getServiceName().getNamespaceURI();
             _service = (BasicHttpBinding_ICampaignManagementServiceStub) locator.getBasicHttpBinding_ICampaignManagementService();

             // Set the header properties.

             _service.clearHeaders();
             _service.setHeader(_namespace, "CustomerId", CustomerId);
             _service.setHeader(_namespace, "CustomerAccountId", AccountId);
             _service.setHeader(_namespace, "DeveloperToken", DeveloperToken);
             _service.setHeader(_namespace, "UserName", UserName);
             _service.setHeader(_namespace, "Password", Password);
             
             // Specify the extensions.

             AdExtension[] adExtensions = new AdExtension[3];

             adExtensions[0] = new CallAdExtension();
             ((CallAdExtension)adExtensions[0]).setCountryCode("US");
             ((CallAdExtension)adExtensions[0]).setPhoneNumber("2065550100");
             ((CallAdExtension)adExtensions[0]).setIsCallOnly(false);

             adExtensions[1] = new LocationAdExtension();
             ((LocationAdExtension)adExtensions[1]).setPhoneNumber("206-555-0100");
             ((LocationAdExtension)adExtensions[1]).setCompanyName("Alpine Ski House");
             ((LocationAdExtension)adExtensions[1]).setIconMediaId(null); 
             ((LocationAdExtension)adExtensions[1]).setImageMediaId(null);
             Address address = new Address();
             address.setStreetAddress("1234 Washington Place");
             address.setStreetAddress2("Suite 1210");
             address.setCityName("Woodinville");
             address.setProvinceName("WA"); 
             address.setCountryCode("US");
             address.setPostalCode("98608");
             ((LocationAdExtension)adExtensions[1]).setAddress(address);
             
             adExtensions[2] = new SiteLinksAdExtension();
             SiteLink[] siteLinks = new SiteLink[1];
             siteLinks[0] = new SiteLink();
             siteLinks[0].setDestinationUrl("AplineSkiHouse.com/WinterGloveSale");
             siteLinks[0].setDisplayText("Winter Glove Sale");
             ((SiteLinksAdExtension)adExtensions[2]).setSiteLinks(siteLinks);

             // Add all extensions to the account's ad extension library
             AdExtensionIdentity[] adExtensionIdentities = AddAdExtensions(
                 AccountId,
                 adExtensions
                 );

             // DeleteAdExtensionsAssociations, SetAdExtensionsAssociations, and GetAdExtensionsEditorialReasons 
             // operations each require a list of type AdExtensionIdToEntityIdAssociation.
             AdExtensionIdToEntityIdAssociation[] adExtensionIdToEntityIdAssociations = new AdExtensionIdToEntityIdAssociation[adExtensionIdentities.length];

             // GetAdExtensionsByIds requires a list of type long.
             long[] adExtensionIds = new long[adExtensionIdentities.length];
             
             // Loop through the list of extension IDs and build any required data structures
             // for subsequent operations. 

             for (int i = 0; i < adExtensionIdentities.length; i++)
             {
                 adExtensionIdToEntityIdAssociations[i] = new AdExtensionIdToEntityIdAssociation();
                 adExtensionIdToEntityIdAssociations[i].setAdExtensionId(adExtensionIdentities[i].getId());
                 adExtensionIdToEntityIdAssociations[i].setEntityId(CampaignId);

                 adExtensionIds[i] = adExtensionIdentities[i].getId();
             }
             
             // Associate the specified ad extensions with the respective campaigns or ad groups. 
             SetAdExtensionsAssociations(
                 AccountId, 
                 adExtensionIdToEntityIdAssociations, 
                 AssociationType.Campaign
                 );

             // Get editorial rejection reasons for the respective ad extension and entity associations.
             AdExtensionEditorialReasonCollection[] adExtensionEditorialReasonCollection = GetAdExtensionsEditorialReasons(
                 AccountId, 
                 adExtensionIdToEntityIdAssociations, 
                 AssociationType.Campaign
                 );

             java.lang.String[] adExtensionsTypeFilter = new java.lang.String[] {
                     AdExtensionsTypeFilterNull._SiteLinksAdExtension,
                     AdExtensionsTypeFilterNull._CallAdExtension,
                     AdExtensionsTypeFilterNull._LocationAdExtension
                     };
             
             // Get the specified ad extensions from the account’s ad extension library.
             adExtensions = (AdExtension[]) GetAdExtensionsByIds(
                 AccountId,
                 adExtensionIds, 
                 adExtensionsTypeFilter
                 );

             int index = 0;                     
             
             for (AdExtension extension : adExtensions)
             {
                 if (extension == null || extension.getId() == null)
                 {
                     System.out.println("Extension is null or invalid.");
                 }
                 else
                 {
                     System.out.println("Ad extension ID: " + extension.getId());
                     System.out.println("Ad extension Type: " + extension.getType());

                     if (extension instanceof CallAdExtension)
                     {
                         System.out.println("Phone number: " + ((CallAdExtension)extension).getPhoneNumber());
                         System.out.println("Country: " + ((CallAdExtension)extension).getCountryCode());
                         System.out.println("Is only clickable item: " + ((CallAdExtension)extension).getIsCallOnly());
                         System.out.println();
                     }
                     else if (extension instanceof LocationAdExtension)
                     {
                         System.out.println("Company name: " + ((LocationAdExtension)extension).getCompanyName());
                         System.out.println("Phone number: " + ((LocationAdExtension)extension).getPhoneNumber());
                         System.out.println("Street: " + ((LocationAdExtension)extension).getAddress().getStreetAddress());
                         System.out.println("City: " + ((LocationAdExtension)extension).getAddress().getCityName());
                         System.out.println("State: " + ((LocationAdExtension)extension).getAddress().getProvinceName());
                         System.out.println("Country: " + ((LocationAdExtension)extension).getAddress().getCountryCode());
                         System.out.println("Zip code: " + ((LocationAdExtension)extension).getAddress().getPostalCode());
                         System.out.println("Business coordinates determined?: " + ((LocationAdExtension)extension).getGeoCodeStatus());
                         System.out.println("Map icon ID: " + ((LocationAdExtension)extension).getIconMediaId());
                         System.out.println("Business image ID: " + ((LocationAdExtension)extension).getImageMediaId());
                         System.out.println();
                     }
                     else if (extension instanceof SiteLinksAdExtension)
                     {
                         for (SiteLink siteLink : ((SiteLinksAdExtension)extension).getSiteLinks())
                         {
                             System.out.println("  Display URL: " + siteLink.getDisplayText());
                             System.out.println("  Destination URL: " + siteLink.getDestinationUrl());
                             System.out.println();
                         }
                     }
                     else
                     {
                         System.out.println("  Unknown extension type");
                     }
                     
                     if (adExtensionEditorialReasonCollection != null 
                             && adExtensionEditorialReasonCollection.length > 0
                             && adExtensionEditorialReasonCollection[index] != null)
                     {
                    	 System.out.println();
                         
                         // Print any editorial rejection reasons for the corresponding extension. This sample 
                         // assumes the same list index for adExtensions and adExtensionEditorialReasonCollection
                         // as defined above.

                         for (AdExtensionEditorialReason adExtensionEditorialReason : adExtensionEditorialReasonCollection[index].getReasons())
                         {
                             if (adExtensionEditorialReason != null &&
                                 adExtensionEditorialReason.getPublisherCountries() != null)
                             {
                            	 System.out.println("Editorial Rejection Location: " + adExtensionEditorialReason.getLocation());
                            	 System.out.println("Editorial Rejection PublisherCountries: ");
                                 for (java.lang.String publisherCountry : adExtensionEditorialReason.getPublisherCountries())
                                 {
                                	 System.out.println("  " + publisherCountry);
                                 }
                                 System.out.println("Editorial Rejection ReasonCode: " + adExtensionEditorialReason.getReasonCode());
                                 System.out.println("Editorial Rejection Term: " + adExtensionEditorialReason.getTerm());
                                 System.out.println();
                             }
                         }
                     }

                 }

                 System.out.println();
                 
                 index++;
             }
             
             // Remove the specified associations from the respective campaigns or ad groups. 
             // The extesions are still available in the account's extensions library. 
             DeleteAdExtensionsAssociations(
                 AccountId,
                 adExtensionIdToEntityIdAssociations,
                 AssociationType.Campaign
                 );

             // Deletes the ad extensions from the account’s ad extension library.
             DeleteAdExtensions(
                 AccountId,
                 adExtensionIds
                 );
             
         }
         // Campaign Management service operations can throw AdApiFaultDetail.
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
         // Campaign Management service operations can throw ApiFaultDetail.
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
         // Some Campaign Management service operations such as SetAdExtensionsAssociations can throw EditorialApiFaultDetail.
         catch (EditorialApiFaultDetail fault)
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
             
             // If the EditorialError array is not null, the following are examples of error codes that may be found.
             for (EditorialError error : fault.getEditorialErrors())
             {
                 System.out.printf("EditorialError at Index: %d\n\n", error.getIndex());
                 System.out.printf("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage());
                 System.out.printf("Appealable: %s\nDisapproved Text: %s\nCountry: %s\n\n", error.getAppealable(), error.getDisapprovedText(), error.getPublisherCountry());
                     
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
                  e.getCause() instanceof EditorialApiFaultDetail ||
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

     // Adds one or more ad extensions to the account's ad extension library.

     static AdExtensionIdentity[] AddAdExtensions(long accountId, AdExtension[] adExtensions) throws RemoteException, Exception
     {
         AddAdExtensionsRequest request = new AddAdExtensionsRequest();
         
         // Set the request information.

         request.setAccountId(accountId);
         request.setAdExtensions(adExtensions);

         return _service.addAdExtensions(request).getAdExtensionIdentities();
     }
     
     // Deletes one or more ad extensions from the account’s ad extension library.

     static void DeleteAdExtensions(long accountId, long[] adExtensionIds) throws RemoteException, Exception
     {
    	 DeleteAdExtensionsRequest request = new DeleteAdExtensionsRequest();
         
         // Set the request information.

         request.setAccountId(accountId);
         request.setAdExtensionIds(adExtensionIds);

         _service.deleteAdExtensions(request);
     }
     
     // Associates one or more extensions with the corresponding campaign or ad group entities.

     static void SetAdExtensionsAssociations(long accountId, AdExtensionIdToEntityIdAssociation[] associations, AssociationType associationType) throws RemoteException, Exception
     {
    	 SetAdExtensionsAssociationsRequest request = new SetAdExtensionsAssociationsRequest();
         
    	 // Set the request information.

         request.setAccountId(accountId);
         request.setAdExtensionIdToEntityIdAssociations(associations);
         request.setAssociationType(associationType);

         _service.setAdExtensionsAssociations(request);
     }
     
     // Removes the specified association from the respective campaigns or ad groups.

     static void DeleteAdExtensionsAssociations(long accountId, AdExtensionIdToEntityIdAssociation[] associations, AssociationType associationType) throws RemoteException, Exception
     {
    	 DeleteAdExtensionsAssociationsRequest request = new DeleteAdExtensionsAssociationsRequest();
         
    	 // Set the request information.

         request.setAccountId(accountId);
         request.setAdExtensionIdToEntityIdAssociations(associations);
         request.setAssociationType(associationType);

         _service.deleteAdExtensionsAssociations(request);
     }

     // Gets the specified ad extensions from the account's extension library.

     static AdExtension[] GetAdExtensionsByIds(long accountId, long[] adExtensionIds, java.lang.String[] adExtensionsTypeFilter) throws RemoteException, Exception
     {
         GetAdExtensionsByIdsRequest request = new GetAdExtensionsByIdsRequest();

         // Set the request information.

         request.setAccountId(accountId);
         request.setAdExtensionIds(adExtensionIds);
         request.setAdExtensionType(adExtensionsTypeFilter);

         return _service.getAdExtensionsByIds(request).getAdExtensions();
     }

     // Gets the reasons why the specified extension failed editorial when 
     // in the context of an associated campaign or ad group.

     private static AdExtensionEditorialReasonCollection[] GetAdExtensionsEditorialReasons(
         long accountId,
         AdExtensionIdToEntityIdAssociation[] associations,
         AssociationType associationType)  throws RemoteException, Exception
     {
    	 GetAdExtensionsEditorialReasonsRequest request = new GetAdExtensionsEditorialReasonsRequest();
         
         // Set the request information.

         request.setAccountId(accountId);
         request.setAdExtensionIdToEntityIdAssociations(associations);
         request.setAssociationType(associationType);
         
         return _service.getAdExtensionsEditorialReasons(request).getEditorialReasons();
     }     
 }