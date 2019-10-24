#! /bin/sh

serverkeyPath="$SERVER_KEY_PATH"
clientKeyPath="$CLIENT_KEY_PATH"
s3User="$S3_USER"
s3Password="$S3_PASSWORD"
s3Host="$S3_HOST"
bucketName="$S3_BUCKET_NAME"
kafkaTopic="$KAFKA_TOPIC"
kafkaBootstrapServer="$KAFKA_BOOTSTRAP_SERVER"

echo k8-sftp-push-assembly-0.1.0.jar $serverkeyPath $clientKeyPath $s3User $s3Password $s3Host $bucketName $kafkaTopic $kafkaBootstrapServer

java -jar k8-sftp-push-assembly-0.1.0.jar $serverkeyPath $clientKeyPath $s3User $s3Password $s3Host $bucketName $kafkaTopic $kafkaBootstrapServer