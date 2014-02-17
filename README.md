AWS Maven is a [Maven Wagon][wagon] for [Amazon S3][s3].

[wagon]: http://maven.apache.org/wagon/
[s3]: http://aws.amazon.com/s3/

# Usage
To publish Maven artifacts to S3 a build extension must be defined in a project's `pom.xml`.  The latest version of the wagon can be found on the [`aws-maven`][aws-maven] page in Maven Central.

	<project>
	    ...
	    <build>
	        ...
	        <extensions>
	            ...
	            <extension>
	                <groupId>org.springframework.build</groupId>
	                <artifactId>aws-maven</artifactId>
	                <version>5.0.0.RELEASE</version>
	            </extension>
	            ...
	        </extensions>
	        ...
	    </build>
	    ...
	</project>

Once the build extension is configured distribution management repositories can be defined in the `pom.xml` with an `s3://` scheme.

	<project>
	  ...
	  <distributionManagement>
	      <repository>
	          <id>aws-release</id>
	          <name>AWS Release Repository</name>
	          <url>s3://distribution.bucket/release</url>
	      </repository>
	      <snapshotRepository>
	          <id>aws-snapshot</id>
	          <name>AWS Snapshot Repository</name>
	          <url>s3://distribution.bucket/snapshot</url>
	      </snapshotRepository>
	  </distributionManagement>
	  ...
	</project>

Finally the `~/.m2/settings.xml` must be updated to include access and secret keys for the account. The access key should be used to populate the `username` element, and the secret key should be used to populate the `password` element.

	<settings>
	    ...
	    <servers>
	        ...
	        <server>
	            <id>aws-release</id>
	            <username>0123456789ABCDEFGHIJ</username>
	            <password>0123456789abcdefghijklmnopqrstuvwxyzABCD</password>
	        </server>
	        <server>
	            <id>aws-snapshot</id>
	            <username>0123456789ABCDEFGHIJ</username>
	            <password>0123456789abcdefghijklmnopqrstuvwxyzABCD</password>
	        </server>
	        ...
	    </servers>
	    ...
	</settings>

[aws-maven]: http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.springframework.build%22%20AND%20a%3A%22aws-maven%22
