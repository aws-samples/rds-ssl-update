/**
Copyright 2020-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
**/
package com.amazonaws.rdscaupdate;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.ModifyDBInstanceRequest;

/**
 * This Single class utility helps you to list and interactively update the RDS SSL certificates
 * This can be used to speed up the process if you have multiple RDS Instances and would like to do it automatically.
 * 
 * Please note: Running this code calls the setApplyImmediately() with true, which will cause your RDS Instance to restart immediately. 
 * Please make sure that you have made proper tests on dev / staging environments before running on production.
 * 
 * This code is provided to help only automate the update only. 
 *
 */
public class RDSCAUpdate {
	
	private static List<String> regions= new ArrayList<String>();
	private static Map<String,String> regionMap = new HashMap<String, String>();
	private static boolean listOnly = false;
	static Scanner scan = new Scanner(System.in);
	private static boolean regionSpecified = false;
	private static Console cons= System.console();
	
	/**
	 * Place holders for the Certificate labels. Helps to re-use in future
	 */
	public static final String UPDATE_SSL_FROM= "rds-ca-2015";
	public static final String UPDATE_SSL_TO= "rds-ca-2019";
	
	
	//Initialize the Regions (Hardcoded)
	static {
		
		regions.add("us-east-1");
		regionMap.put("N Virginia", "us-east-1");
		regions.add("us-east-2");
		regionMap.put("Ohio", "us-east-2");
		regions.add("us-west-1");
		regionMap.put("N California", "us-west-1");
		regions.add("us-west-2");
		regionMap.put("Oregon", "us-west-2");
		regionMap.put("Hong Kong", "ap-east-1");
		regions.add("ap-south-1");
		regionMap.put("Mumbai", "ap-south-1");
		regions.add("ap-northeast-3");
		regionMap.put("Osaka-Local", "ap-northeast-3");
		regions.add("ap-northeast-2");
		regionMap.put("Seoul", "ap-northeast-2");
		regions.add("ap-southeast-1");
		regionMap.put("Singapore", "ap-southeast-1");
		regions.add("ap-southeast-2");
		regionMap.put("Sydney", "ap-southeast-2");
		regions.add("ap-northeast-1");
		regionMap.put("Tokyo", "ap-northeast-1");
		
		regions.add("ca-central-1");
		regionMap.put("Central", "ca-central-1");
		
		regions.add("eu-central-1");
		regionMap.put("Frankfurt", "eu-central-1");
		regions.add("eu-west-1");
		regionMap.put("Ireland", "eu-west-1");
		regions.add("eu-west-2");
		regionMap.put("London", "eu-west-2");
		regions.add("eu-west-3");
		regionMap.put("Paris", "eu-west-3");
		regions.add("eu-north-1");
		regionMap.put("Stockholm", "eu-north-1");
//		
		regionMap.put("Bahrain", "me-south-1");
		
		regions.add("sa-east-1");
		regionMap.put("Sao Paulo", "sa-east-1");
				
	}

	/**
	 * Main Method.
	 * @param args
	 * Expects the following options below per invocation
	 * No Arguments : Prompts the user step by step for Region (type all for all regions) or specific Region name.
	 * List : Lists all the RDS Instances for all the regions
	 * {RegionName} for listing and updating per region
	 */
	public static void main(String[] args) {
		if(args.length==1) {
			String arg1=args[0];
			if(arg1.equalsIgnoreCase("list")) {
				System.out.println("Listing RDS instances in all regions\n\n");
				listOnly = true;
			}
		}
		else if(args.length>1) {
			parseArgs(args);
		} 
		if(!listOnly && !regionSpecified ) {
			System.out.println("Region not specified! Enter Region code \none of "+regions+""
					+ "\nNote : you have to run Hong kong and Middle-east individually (if you enabled these regions)");
//			Scanner scan = new Scanner(System.in);
			String s = scan.nextLine();
			//scan.close();
			System.out.println(s);
			if(s.equalsIgnoreCase("all")) {
				System.out.println("You specified all regions...running in all regions");
			} else {
				System.out.println("Running in specified region " + s);
				regions.clear();
				regions.add(s);
				
			}
		}
		String accesskey=null;
		String accessid=null;
	
		accessid = cons.readLine("Enter Access ID : "); 
		System.out.println("Enter Access Key : ");
		accesskey = new String(cons.readPassword());
//		System.out.println(accesskey);
		new RDSCAUpdate().listRDSInstancesInAllRegions(accessid, accesskey);
		System.out.println("Finished...Exiting!");
		scan.close();
	}
	
	
	private static void parseArgs(String[] args) {
		
		for (int i = 0;args.length>0 && i < args.length; i+=2) {
			String arg1 = args[i];
			String val = args[i+1];
			
			if(arg1.indexOf("list")>=0 || arg1.indexOf("update")>=0) {
				System.out.println("You entered to "+ arg1 +" in the following Regions : " + val.toUpperCase());
				if(arg1.indexOf("list")>=0) {
					listOnly=true;
				}
				if(arg1.toLowerCase().contains("update")) {
					regionSpecified=true;
				}
				regions.clear();
				regions.add(val);
			} 
		}
	}

	
	private void listRDSInstancesInAllRegions(String accessid, String accesskey){
		AmazonRDSClientBuilder clientB= AmazonRDSClientBuilder.standard();
		Iterator<String> RegionsIter = regions.iterator();
		AmazonRDS client;
		loop: while (RegionsIter.hasNext()) {
				String regionName = RegionsIter.next();
				clientB.setRegion(regionName);
				
				System.out.println("Checking..."+regionName);
				 
				if((accesskey!=null && accesskey.trim().length()>0) 
						&& (accessid!=null && accessid.trim().length()>0)) {
					try {
					clientB = AmazonRDSClientBuilder.standard().withRegion(Regions.fromName(regionName));
					BasicAWSCredentials creds = new BasicAWSCredentials(accessid, accesskey);
					clientB.setCredentials(new AWSStaticCredentialsProvider(creds));
					client = clientB.build();
					
					} catch (Exception e) {
						System.out.println("Error connecting to Region "+regionName+"! Skipping...");
						System.out.println("=======================================================");
						continue loop;
					}
					
				} else {
					try {
						System.out.println("No Credentials provided, assuming default credentials...");
						client = AmazonRDSClientBuilder.standard().withRegion(Regions.fromName(regionName)).build();
					} catch (Exception e) {
						//Error creating client for Region
						System.out.println("Error connecting to Region "+regionName+"! Skipping...");
						System.out.println("=======================================================");
						continue loop;
					}
					
				}
				
				try {
					
					DescribeDBInstancesResult result = client.describeDBInstances();
					List<DBInstance> rdsInstanceList = result.getDBInstances();
					if(rdsInstanceList.size()>0) {
						System.out.println("Region : "+regionName);
						Iterator<DBInstance> instances =rdsInstanceList.iterator();
						while (instances.hasNext()) {
							DBInstance rdsInstance = (DBInstance) instances.next();
							System.out.println(rdsInstance.getDBInstanceArn()+" : "+rdsInstance.getCACertificateIdentifier());
							if(!listOnly && !rdsInstance.getCACertificateIdentifier().equalsIgnoreCase(UPDATE_SSL_TO)){
							System.out.println("Do you want to update this instance?");
							
							String s = scan.nextLine();
							System.out.println(s);
							if(s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") ) {
								System.out.println("Updating...");
								ModifyDBInstanceRequest req=new ModifyDBInstanceRequest();
								req.setCACertificateIdentifier(UPDATE_SSL_TO);
								req.setDBInstanceIdentifier(rdsInstance.getDBInstanceIdentifier());
								req.setApplyImmediately(true);
								try {
									client.modifyDBInstance(req);	
								} catch (Exception e) {
									//TODO Handle error 
									e.printStackTrace();
								}
							}
						} else {
							System.out.println("The RDS Instance is already upto date "+rdsInstance.getCACertificateIdentifier());
						}
						}
						
					}
				} catch (Exception e) {
					//TODO Suppressing exception stack trace
					System.out.println("ERROR WHILE FETCHING DATA FROM "+regionName);
					System.out.println(e.getMessage());
					System.out.println("=======================================================");
				}
				System.out.println("=======================================================");
		}
	}

	
	public List<Region> getRegions(){
		List<Region> regions= RegionUtils.getRegions();
		return regions;
		
	}
}
