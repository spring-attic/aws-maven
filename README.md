# AWS Maven Wagon

This project is a fork from [https://github.com/spring-projects/aws-maven](https://github.com/spring-projects/aws-maven) to 
support development and operations at Finnish Brodcasting Company ( YLE ). No guarantees are made for support or updating
the component, but as long as we are using it actively we will update it as we need it.

## Building and deploying this wagon

mvn install

mvn deploy

## Usage
To publish Maven artifacts to S3 a build extension must be defined in a project's `pom.xml`.  The latest version of the wagon can 
be found from from YLE public mvn repository http://maven.c4.yle.fi/release

To get the dependency add to your pom:


```xml
<repositories>
    <repository>
      <id>yle-public</id>
      <name>Yle public repository</name>
      <url>http://maven.c4.yle.fi/release</url>
      <layout>default</layout>
    </repository>
</repositories>
```

And plugin dependency:

```xml
<project>
  ...
  <build>
    ...
    <extensions>
      ...
      <extension>
      <groupId>fi.yle.tools</groupId>
      <artifactId>aws-maven</artifactId>
      <version>1.0.0</version>
      </extension>
      ...
    </extensions>
    ...
  </build>
  ...
</project>
```

This allows then using dependencies from s3 repositories as well as publish to s3 repositories.

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

Finally the `~/.m2/settings.xml` should be updated to include access and secret keys for the account. The access key should 
be used to populate the `username` element, and the secret access key should be used to populate the `password` element.

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
* AWS-Profile ( Can be overridden with AWS_PROFILE variable )

## Using IAM roles

If users want to / or need to use roles while accessing services, an assumed role can be taken into use by defining two environment variables.

export AWS_ASSUME_ROLE_ARN="ARN:TO:ROLE"

export AWS_ASSUME_ROLE_NAME="session-name-for-role


## Making Artifacts Public
This wagon doesn't set an explict ACL for each artfact that is uploaded.  Instead you should create an AWS Bucket Policy to set 
permissions on objects.  A bucket policy can be set in the [AWS Console][console] and can be generated using the 
[AWS Policy Generator][policy-generator].

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

[cli]: http://aws.amazon.com/documentation/cli/
[console]: https://console.aws.amazon.com/s3
[env-var]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EnvironmentVariableCredentialsProvider.html
[instance-metadata]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/InstanceProfileCredentialsProvider.html
[policy-generator]: http://awspolicygen.s3.amazonaws.com/policygen.html
[s3]: http://aws.amazon.com/s3/
[sys-prop]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/SystemPropertiesCredentialsProvider.html
[wagon]: http://maven.apache.org/wagon/
