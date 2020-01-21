Following the announcement of SSL update for RDS Instances, You must add new CA certificates to your trust stores, and RDS database instances must separately use new server certificates before the hard expiration date. I urge you to complete these changes before February 5, 2020. After February 5, 2020, AWS will begin scheduling certificate rotations for their database instances prior to the March 5, 2020 deadline.
 
 Also, any new RDS database instances created after January 14, 2020 will default to using the new certificates. If client applications have not been updated, these applications will fail to connect to any new instances created after this date. If you wish to temporarily modify new instances to use the old certificates, you can do so by using the AWS console, the RDS API, and the AWS CLI. Any instances created prior to January 14, 2020 will have the old certificates until they update them to the rds-ca-2019 version.
 
  If applications connect to RDS database instances using the SSL/TLS protocol, follow the detailed instructions below:
 
<UL><LI> https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.SSL.html </LI>
  <LI>     https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/UsingWithRDS.SSL.html</LI>
 <LI>     https://docs.aws.amazon.com/documentdb/latest/developerguide/ca-cert-rotation.html</LI>
 </UL>
 
  Test these steps in a development or staging environment before implementing them in production environments. If not completed, applications using SSL/TLS will fail to connect to existing DB instances as soon as RDS rotates the certificates on the database side prior to March 5, 2020.
 
  As a reminder update your application trust stores before performing the rotation and verify this rotation in a test environment first before applying it to production. Please open a case if you have any issues and let me know. 

<h2>Script to Update list and update RDS Instances</h2>
This repository is a simple java based tool that can be used to automate the process of listing and updating the Certificates.
<font color="red"><B>PLEASE USE THIS AFTER YOU HAVE TESTED YOUR CERTIFICATE UPDATES</B></font><BR>

<B>PRE-Requisite</B> 

<UL>
<LI><B>AWS CLI:</B>This tool expects that you have the AWS CLI installed (uses the credentials configureds as part of this tool)</LI>
<LI><B>Latest Java Run time:</B> Alternately you can also download JAVA JRE from here - Download it from https://adoptopenjdk.net/.</LI>
<LI><B>Maven:</B> You can install Maven from here https://maven.apache.org/install.html. This is required to build the tool from the source fcode </LI>
</UL>

<B>Developers</B>
If you are already having the above tools installed along with an IDE like Eclipse https://www.eclipse.org/downloads/ then you can simply copy the java code and run it. <B>Note that the there are a few console input steps that may not work if you run in an IDE</B>

<B>Build the Tool</B>
<UL>
<LI>Clone this repository</LI>
<LI>Open a Command window in the root folder on your local repository and run the following command 
<br> <code>mvn clean install dependency:copy-dependencies</code>
<LI> This will commpile and generate the jar files and all the dependent AWS SDK jar files.
<LI> cd to the target folder and run the following command <br><code>java -jar RDSCertificateUpdate-1.0.0.jar</code>

<br>Some of the example commands to run are below
<BR>
<code>java -jar RDSCertificateUpdate-1.0.0.jar list </code>(lists all the RDS instances running in all regions except Hong Kong and Bahrain)
<BR>
<code>java -jar RDSCertificateUpdate-1.0.0.jar list [region]</code>(lists all the RDS instances running in the given region)
<BR>
 <code>java -jar RDSCertificateUpdate-1.0.0.jar update [region] </code>(lists all the RDS instances running in all regions except Hong Kong and Bahrain)
<BR>
<code>java -jar RDSCertificateUpdate-1.0.0.jar</code>
<BR>
Prompts you to enter region name and then lists each RDS instance and whether to update the certificate configuration or not
 
 <B>If you want to ship it as an executable tool (With bundled Java Runtime etc) you can follow these steps. </B>
 <UL> 
 <LI> Create a Folder (for example RDSSSLUpdateTool)
 <LI> Copy the jar file created above into this folder
 <LI> Download the latest JRE for your OS from https://adoptopenjdk.net/ and save in this folder
 <LI> Create a batch / shell file (Depending on your OS) for example if you created for windows the folder structure may look like this. 
   <pre>
      RDSSSLUpdateTool
         |__jre
         |__RDSCertificateUpdate.jar
         |__Launch.bat
       
  </pre>
  <LI>The Launch file would contain the command as shown above but referencing the java from the folder the command would look like this<BR>
   <code>
     jre/bin/java -jar RDSCertificateUpdate-1.0.0.jar %*
 </code>
     </UL>
     


## License

This library is licensed under the MIT-0 License. See the LICENSE file.

