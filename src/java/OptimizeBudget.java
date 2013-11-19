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
import java.util.List;

import bingads.optimizer.*;
import bingads.optimizer.adapi.*;
import bingads.optimizer.entities.*;
import bingads.optimizer.enums.*;

/**
 *
 */
public class OptimizeBudget{

    private static java.lang.String _namespace = null;
    private static BasicHttpBinding_IOptimizerServiceStub _service = null;

    // Specify your credentials.
    
    private static java.lang.String UserName = "<UserNameGoesHere>";
    private static java.lang.String Password = "<PasswordGoesHere>";
    private static java.lang.String DeveloperToken = "<DeveloperTokenGoesHere>";
    private static long AccountId = <AccountIdGoesHere>;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
    	try
        {
        	OptimizerServiceLocator locator = new OptimizerServiceLocator();
            _namespace = locator.getServiceName().getNamespaceURI();
            _service = (BasicHttpBinding_IOptimizerServiceStub) locator.getBasicHttpBinding_IOptimizerService();

            // Set the header properties.

            service.clearHeaders();
            service.setHeader(namespace, "DeveloperToken", DeveloperToken);
            service.setHeader(namespace, "UserName", UserName);
            service.setHeader(namespace, "Password", Password);
            service.setHeader(namespace, "CustomerAccountId", AccountId);

            
            // Get the budget opportunities which have not expired for the specified account.

            BudgetOpportunity[] opportunities = GetBudgetOpportunities(AccountId);
            java.lang.String[] opportunityKeys = new java.lang.String[opportunities.length];
            
            if (opportunities.length == 0)
            {
                System.out.println("There are no opportunities which have not yet expired for the specified account.");
            }
            else
            {
            	int count = 0;
            	
            	for (BudgetOpportunity budgetOpportunity : opportunities)
                {
            		// Add the opportunity keys to an array
                	
                    if (budgetOpportunity != null)
                    {
                        System.out.printf("OpportunityKey: %s", budgetOpportunity.getOpportunityKey());
                        opportunityKeys[count++] = budgetOpportunity.getOpportunityKey();
                    }
                }

                // Apply the suggested budget opportunities.
                
                ApplyOpportunities(AccountId, opportunityKeys);
            }
        }
        // Optimizer service operations can throw AdApiFaultDetail.
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
        // Optimizer service operations can throw ApiFaultDetail.
        catch (ApiFaultDetail fault)
        {
            // Log this fault.

            System.out.println("The operation failed with the following faults:\n");

            // If the BatchError array is not null, the following are examples of error codes that may be found.
            for (BatchError error : fault.getBatchErrors())
            {
                System.out.printf("BatchError at Index: %d\n", error.getIndex());
                System.out.printf("Code: %s\nMessage: %s\n\n", error.getCode(), error.getMessage());

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
                System.out.printf("Code: %s\nMessage: %s\n\n", error.getCode(), error.getMessage());

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

    // Gets the budget opportunities which have not expired for the specified account.

    public static BudgetOpportunity[] GetBudgetOpportunities(long accountId) throws RemoteException, Exception
    {
        GetBudgetOpportunitiesRequest request = new GetBudgetOpportunitiesRequest();
        
        // Specify request information.

        request.setAccountId(accountId);

        return service.getBudgetOpportunities(request).getOpportunities();
    }

    // Apply opportunties for the specified account.

    public static void ApplyOpportunities(long accountId, String[] opportunityKeys) throws RemoteException, Exception
    {
        ApplyOpportunitiesRequest request = new ApplyOpportunitiesRequest();
        
        // Specify request information.

        request.setAccountId(accountId);
        request.setOpportunityKeys(opportunityKeys);

        service.applyOpportunities(request);
    }
}