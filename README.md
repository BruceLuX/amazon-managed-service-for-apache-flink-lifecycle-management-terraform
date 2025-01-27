# Amazon Managed Service for Apache Flink Application Lifecycle Management with Terraform

This code sample demonstrates how to use Terraform to control the lifecycle of a Managed Service for Apache Flink (MSF) application using Docker. The process ensures consistent deployment environments and secure handling of AWS credentials.

## Architecture overview

<img src="resources/architecture_overview.png" alt="Architecture Overview">

## Pre-requisites

* [An AWS account](https://console.aws.amazon.com/console/home?nc2=h_ct&src=header-signin)
* [Java 11 or later](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
* [Apache Maven 3.9.6 or later](https://maven.apache.org/)
* [Docker](https://docs.docker.com/engine/install/) installed and running on your machine 
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) installed and configured with appropriate [AWS profile](https://docs.aws.amazon.com/cli/v1/userguide/cli-configure-files.html)

## Environment variables

Before starting, ensure you have the following environment variable set:

- `AWS_PROFILE`: Your AWS profile name that contains the necessary credentials

## Getting started

### 1. Clone the repository 

Clone the repository to your desired workspace and move into the repository:

```bash
git clone https://github.com/aws-samples/amazon-managed-service-for-apache-flink-lifecycle-management-terraform.git
```

### 2. Build the Docker image
Build the Docker image by running the following command:

```bash
docker build -t msf-terraform .
```

This command builds a Docker image named `msf-terraform` using the Dockerfile in the current directory. The image contains all necessary dependencies for running Terraform and AWS operations.

### 3. Export the AWS credentials
Make sure to put your correct AWS profile name in `$AWS_PROFILE`.

```bash
aws configure export-credentials --profile $AWS_PROFILE --format env-no-export > .env.docker
```

This step:
- Exports AWS credentials from your specified AWS profile
- Saves the temporary credentials in environment variable format to `.env.docker`  that will be used by the Docker container

This workflow ensures secure credential handling without exposing them in the command line.


### 4. Terraform state backend 
Amazon S3 is used to store the Terraform state and Amazon DynamoDB for state locking and consistency checking. Make sure the values in `terraform/backend.config` are correct. It is recommended to [enable Bucket Versioning](https://developer.hashicorp.com/terraform/language/backend/s3) on the S3 bucket to allow for state recovery. 
You need to create the resources in advance in your AWS account and provide the corresponding values inside the `terraform/backend.config`:
- S3 bucket name
- Key name, e.g. `terraform/terraform.tfstate`
- Region, e.g. `us-east-1`
- DynamoDB table name

### 5. Check the config variables
Check the config variables for your Flink application inside `terraform/config.tfvars.json` and change as desired. 

### 6. Run the deployment container

```bash
docker run --env-file .env.docker --rm -it \
  -v ./flink-s3:/home/flinkuser/flink-s3 \
  -v ./terraform:/home/flinkuser/terraform \
  -v ./build.sh:/home/flinkuser/build.sh \
  msf-terraform bash build.sh apply
```

This command:
- Runs the MSF Terraform container with AWS credentials
- Mounts necessary local directories into the container
- Executes the deployment script `build.sh`
- Builds the JAR file of the Flink application, uploads it to Amazon S3 and creates the required AWS resources using Terraform

Note that you have to pass the desired Terraform command at the end of the Docker run command, e.g. `init`, `plan`, `apply` or `destroy`. 

### 7. Update the deployment container 
Update a config variable inside `terraform/config.tfvars.json` and simply run: 

```bash
docker run --env-file .env.docker --rm -it \
  -v ./flink-s3:/home/flinkuser/flink-s3 \
  -v ./terraform:/home/flinkuser/terraform \
  -v ./build.sh:/home/flinkuser/build.sh \
  msf-terraform bash build.sh apply
```

### 8. Destroy the deployment container and resources  
Run the following command to destroy the created resources: 
```bash
docker run --env-file .env.docker --rm -it \
  -v ./flink-s3:/home/flinkuser/flink-s3 \
  -v ./terraform:/home/flinkuser/terraform \
  -v ./build.sh:/home/flinkuser/build.sh \
  msf-terraform bash build.sh destroy
```

Note that you have to destroy your DynamoDB table as well as the S3 bucket for Terraform state management separately. 

Run the following command to delete the created Docker image:
```bash
docker image rm msf-terraform
```

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file. 

## Authors

- [Felix John](https://github.com/Madabaru)
- [Lorenzo Nicora](https://github.com/nicusX)
- [Hamza Khalid](https://github.com/ihamzak)
