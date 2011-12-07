AWS Maven is a [Maven Wagon][1] for [Amazon S3][2].

[1]: http://maven.apache.org/wagon/
[2]: http://aws.amazon.com/s3/

# Usage
To publish Maven artifacts to S3 a build extension must be defined in a project's `pom.xml`.

	<project>
	    ...
	    <build>
	        ...
	        <extensions>
	            ...
	            <extension>
	                <groupId>org.springframework.build.aws</groupId>
	                <artifactId>org.springframework.build.aws.maven</artifactId>
	                <version>4.0.0.RELEASE</version>
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

Finally the `~/.m2/settings.xml` must be updated to include access and secret keys for the account. The access key should be used to populate the `username` element, and the secret key should be used to populate the `passphrase` element.

	<settings>
	    ...
	    <servers>
	        ...
	        <server>
	            <id>aws-release</id>
	            <username>0123456789ABCDEFGHIJ</username>
	            <passphrase>0123456789abcdefghijklmnopqrstuvwxyzABCD</passphrase>
	        </server>
	        <server>
	            <id>aws-snapshot</id>
	            <username>0123456789ABCDEFGHIJ</username>
	            <passphrase>0123456789abcdefghijklmnopqrstuvwxyzABCD</passphrase>
	        </server>
	        ...
	    </servers>
	    ...
	</settings>