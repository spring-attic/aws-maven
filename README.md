# AWS Maven Wagon
This project is a [Maven Wagon][wagon] for [Amazon S3][s3].  In order to to publish artifacts to an S3 bucket, the user (as identified by their access key) must be listed as an owner on the bucket.

## Usage
To publish Maven artifacts to S3 a build extension must be defined in a project's `pom.xml`.  The latest version of the wagon can be found on the [`aws-maven`][aws-maven] page in Maven Central.

```xml
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
```

Once the build extension is configured distribution management repositories can be defined in the `pom.xml` with an `s3://` scheme.

```xml
<project>
  ...
  <distributionManagement>
    <repository>
      <id>aws-release</id>
      <name>AWS Release Repository</name>
      <url>s3://<BUCKET>/release</url>
    </repository>
    <snapshotRepository>
      <id>aws-snapshot</id>
      <name>AWS Snapshot Repository</name>
      <url>s3://<BUCKET>/snapshot</url>
    </snapshotRepository>
  </distributionManagement>
  ...
</project>
```

Finally the `~/.m2/settings.xml` must be updated to include access and secret keys for the account. The access key should be used to populate the `username` element, and the secret access key should be used to populate the `password` element.

```xml
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
```

Alternatively, the access and secret keys for the account can be provided using

* `AWS_ACCESS_KEY_ID` (or `AWS_ACCESS_KEY`) and `AWS_SECRET_KEY` (or `AWS_SECRET_ACCESS_KEY`) [environment variables][env-var]
* `aws.accessKeyId` and `aws.secretKey` [system properties][sys-prop]
* The Amazon EC2 [Instance Metadata Service][instance-metadata]

## Making Artifacts Public
This wagon doesn't set an explict ACL for each artfact that is uploaded.  Instead you should create an AWS Bucket Policy to set permissions on objects.  A bucket policy can be set in the [AWS Console][console] and can be generated using the [AWS Policy Generator][policy-generator].

In order to make the contents of a bucket public you need to add statements with the following details to your policy:

| Effect  | Principal | Action       | Amazon Resource Name (ARN)
| ------- | --------- | ------------ | --------------------------
| `Allow` | `*`       | `ListBucket` | `arn:aws:s3:::<BUCKET>`
| `Allow` | `*`       | `GetObject`  | `arn:aws:s3:::<BUCKET>/*`

If your policy is setup properly it should look something like:

```json
{
  "Id": "Policy1397027253868",
  "Statement": [
    {
      "Sid": "Stmt1397027243665",
      "Action": [
        "s3:ListBucket"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::<BUCKET>",
      "Principal": {
        "AWS": [
          "*"
        ]
      }
    },
    {
      "Sid": "Stmt1397027177153",
      "Action": [
        "s3:GetObject"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::<BUCKET>/*",
      "Principal": {
        "AWS": [
          "*"
        ]
      }
    }
  ]
}
```

If you prefer to use the [command line][cli], you can use the following script to make the contents of a bucket public:

```bash
BUCKET=<BUCKET>
TIMESTAMP=$(date +%Y%m%d%H%M)
POLICY=$(cat<<EOF
{
  "Id": "public-read-policy-$TIMESTAMP",
  "Statement": [
    {
      "Sid": "list-bucket-$TIMESTAMP",
      "Action": [
        "s3:ListBucket"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::$BUCKET",
      "Principal": {
        "AWS": [
          "*"
        ]
      }
    },
    {
      "Sid": "get-object-$TIMESTAMP",
      "Action": [
        "s3:GetObject"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:s3:::$BUCKET/*",
      "Principal": {
        "AWS": [
          "*"
        ]
      }
    }
  ]
}
EOF
)

aws s3api put-bucket-policy --bucket $BUCKET --policy "$POLICY"
```

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
[cli]: http://aws.amazon.com/documentation/cli/
[console]: https://console.aws.amazon.com/s3
[env-var]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EnvironmentVariableCredentialsProvider.html
[instance-metadata]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/InstanceProfileCredentialsProvider.html
[policy-generator]: http://awspolicygen.s3.amazonaws.com/policygen.html
[s3]: http://aws.amazon.com/s3/
[sys-prop]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/SystemPropertiesCredentialsProvider.html
[wagon]: http://maven.apache.org/wagon/
