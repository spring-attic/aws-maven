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

Note that [Amazon IAM][iam] permissions need to be configured accordingly. The `s3:GetBucketLocation` action is required in addition to the bucket read/write permission.

	{
	  "Statement": [
	    {
	      "Effect": "Allow",
	      "Action": "s3:*",
	      "Resource": "arn:aws:s3:::distribution.bucket/*"
	    }
	  ],
	  "Statement": [
	    {
	      "Effect": "Allow",
	      "Action": "s3:GetBucketLocation",
	      "Resource": "arn:aws:s3:::*"
	    }
	  ]
	}

Note that this plugin doesn't set an explicit ACL for each artifact uploaded. Instead it is adviced that you create a
resource-based permission attached to the bucket. So if you set it to be public, all uploaded artifact become public.

To make your bucket public, you can use the [aws console](console.aws.amazon.com), or if you prefer command line
use [aws cli](http://aws.amazon.com/documentation/cli/). Installation is descrebed in the  [docs](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-set-up.html).

Here is a short script to make you bucket public
```
BUCKET=my-maven-repo-bucket
TIMESTAMP=$(date +%Y%m%d%H%M)
POLICY=$(cat<<EOF
{
    "Version": "2008-10-17",
    "Id": "s3-public-read-$TIMESTAMP",
    "Statement": [
        {
            "Sid": "Stmt-$TIMESTAMP",
            "Effect": "Allow",
            "Principal": {
                "AWS": "*"
            },
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::$BUCKET/*"
        }
    ]
}
EOF
)
 
aws s3api put-bucket-policy --bucket $BUCKET --policy "$POLICY"
```

[aws-maven]: http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.springframework.build%22%20AND%20a%3A%22aws-maven%22
[iam]: http://aws.amazon.com/iam/
